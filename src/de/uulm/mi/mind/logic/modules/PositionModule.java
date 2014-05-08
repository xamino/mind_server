package de.uulm.mi.mind.logic.modules;

import de.uulm.mi.mind.io.Configuration;
import de.uulm.mi.mind.logger.Messenger;
import de.uulm.mi.mind.logic.EventModuleManager;
import de.uulm.mi.mind.logic.Module;
import de.uulm.mi.mind.objects.*;
import de.uulm.mi.mind.objects.Interfaces.Data;
import de.uulm.mi.mind.objects.enums.DeviceClass;
import de.uulm.mi.mind.objects.enums.Status;
import de.uulm.mi.mind.objects.enums.Task;
import de.uulm.mi.mind.objects.messages.Error;
import de.uulm.mi.mind.objects.messages.Success;
import de.uulm.mi.mind.objects.unsendable.TimedQueue;
import de.uulm.mi.mind.security.Active;
import de.uulm.mi.mind.security.Security;

import java.util.*;

/**
 * @author Tamino Hartmann
 *         Module that calculates a position based on a given set of WifiMorsels in a Location object.
 */
public class PositionModule implements Module {

    private final int tolerance = 3;
    /**
     * Timeout after which the position is reset to unknown for a user.
     */
    // TODO expose to admin?
    private final String TAG = "PositionModule";
    private final String REAL_POSITION = "realPosition";
    private Messenger log;
    private TimedQueue<String, SensedDevice> sniffedDevices;

    public PositionModule() {
        log = Messenger.getInstance();
        sniffedDevices = new TimedQueue<String, SensedDevice>(5 * 60 * 1000);
    }

    @Override
    public Data run(Task task, Data request) {
        if (!(task instanceof Task.Position)) {
            return new Error(Error.Type.TASK, "PositionModule was called with the wrong task type!");
        }
        Task.Position todo = (Task.Position) task;
        switch (todo) {
            case FIND:
                return findPosition(request);
            case READ:
                return readPositions();
            case SENSOR_WRITE:
                if (!(request instanceof DataList)) {
                    return new Error(Error.Type.WRONG_OBJECT, "SensorWrite was called with the wrong object type!");
                }
                DataList<SensedDevice> receivedDevices = (DataList<SensedDevice>) request;
                for (SensedDevice device : receivedDevices) {
                    if (!sniffedDevices.contains(device.getIpAddress())) {
                        // not sensed before, so just add it and continue
                        log.log(TAG, "Found new device to track: " + device.getIpAddress());
                        sniffedDevices.add(device.getIpAddress(), device);
                        continue;
                    }
                    // already in list, so check if to update if level is higher
                    // todo: we should also check for update of location with a threshold
                    // so that lvl40 but new room is taken over lvl41 old room
                    int oldLevel = sniffedDevices.get(device.getIpAddress()).getLevelValue();
                    if (oldLevel <= device.getLevelValue()) {
                        log.log(TAG, "Updated location of " + device.getIpAddress() + " to " + device.getSensor() + ".");
                        sniffedDevices.add(device.getIpAddress(), device);
                    }
                }
                return new Success("Updated lists.");
            default:
                log.error(TAG, "Unknown task #" + todo + "# sent to PositionModule! Shouldn't happen!");
                return new Error(Error.Type.TASK, "Unknown task sent to PositionModule!");
        }
    }

    /**
     * Takes an Arrival object for its IP address and Location and calculates a position from that.
     *
     * @param request Arrival object containing IP and Location.
     * @return The Area with the calculated Location in it.
     */
    private Data findPosition(Data request) {
        if (!(request instanceof Arrival) || !(((Arrival) request).getObject() instanceof Location)) {
            return new Error(Error.Type.WRONG_OBJECT, "PositionModule was called with the wrong object type!");
        }
        // extract data we need
        Location requestLocation = ((Location) ((Arrival) request).getObject());
        String ip = ((Arrival) request).getIpAddress();

        // First check if there is any AP measurement from the university
        boolean isAtUniversity = false;
        for (WifiMorsel morsel : requestLocation.getWifiMorsels()) {
            if (morsel.getWifiName().equals(Configuration.getInstance().getUniversitySSID())) {
                isAtUniversity = true;
                break;
            }
        }

        if (!isAtUniversity) {
            return new Success(Success.Type.NOTE, "Your position could not be found, you don't seem to be at university.");
        }

        // Everything okay from here on out:
        Location location = calculateLocation(requestLocation);
        if (location == null) {
            // this means the location could not be found in the DB but user is at University
            return new Area("University");
        }

        // get best area for location to return
        Area area = getBestArea(location);
        if (area == null) {
            log.error(TAG, "NULL area for position_find – shouldn't happen as University should be returned at least!");
            return new Success(Success.Type.NOTE, "Your position could not be found.");
        }
        // check location against available sensor data
        if (sniffedDevices.contains(ip)) {
            log.log(TAG, "Sensors see device at " + sniffedDevices.get(ip).getPosition() + ".");
            /*
            SensedDevice device = sniffedDevices.get(ip);
            if (!device.getPosition().equals(area.getID())) {
                log.error(TAG, "Sensor and algorithm see different positions!");
                // todo resolve conflict -- how?
            } else {
                // todo remove this once the merge works
                log.log(TAG, "Sensor and algo are synced.");
            }
            */
        }
        log.log(TAG, "Algo sees device at " + area.getID() + ".");

        // send back the location that the server thinks you're at with the area
        DataList<Location> loca = new DataList<>();
        loca.add(location);
        area.setLocations(loca);

        return area;
    }

    private Data readPositions() {
        // read all users
        ArrayList<Active> users = Security.readActives();
        // filter the list – apply status and set special cases
        DataList<User> sendUsers = new DataList<>();
        for (Active active : users) {
            // check if user
            if (!(active.getAuthenticated() instanceof User)) {
                continue;
            }
            User us = ((User) active.getAuthenticated());
            Area area = ((Area) active.readData(REAL_POSITION));
            String position = (area == null ? null : area.getID());
            Status status = us.getStatus();

            // Handle logic as to what should be output
            boolean isAtUniversity = position != null && position.equals("University"); //TODO set in find
            boolean isAtLocation = position != null && !position.equals("University");
            boolean isInvisible = us.getStatus() == null || us.getStatus() == Status.INVISIBLE;

            // Continue means is not added to output
            // remove invisible users
            if (isInvisible) {
                //log.log(TAG,"Is invisible: " + us.getEmail());
                continue;
            }

            // user is somewhere at an unknown location at university
            if (isAtUniversity) {
                position = null; // "University" is not seen from outside but displayed as "away"
                status = Status.AWAY;
            } else if (isAtLocation) {
                // nothing to do?
            } else {
                //log.log(TAG,"Position null: " + us.getEmail());
                continue; // position was null
            }

            // Filter user object to only give name + position
            User toSend = new User(us.getEmail());
            toSend.setName(us.getName());
            toSend.setPosition(position);
            toSend.setStatus(status);
            sendUsers.add(toSend);
        }
        return sendUsers;
    }

    /**
     * @param request
     * @return Location if found, else null.
     */

    private Location calculateLocation(Location request) {
        // Morsels from the current request which are to be compared to the database values
        DataList<WifiMorsel> requestWifiMorsels = request.getWifiMorsels();

        // Should exists because we checked for existing uni wifi morsel before
        String requestDeviceModel = requestWifiMorsels.get(0).getDeviceModel();
        DeviceClass requestDeviceClass = DeviceClass.getClass(requestDeviceModel);

        // use default class if unknown
        if (requestDeviceClass == DeviceClass.UNKNOWN) {
            log.log(TAG, "Unknown Device: " + requestDeviceModel);
            requestDeviceClass = DeviceClass.CLASS2;
        }

        // Get University Area containing all locations from database
        Area uniArea = (Area) ((DataList) EventModuleManager.getInstance().handleTask(Task.Area.READ, new Area("University"))).get(0);
        DataList<Location> dataBaseLocations = uniArea.getLocations();

        // Modify database List to contain the average Morsel signal strengths for each location
        for (Location databaseLocation : dataBaseLocations) {
            DataList<WifiMorsel> averageMorsels = new DataList<>();
            for (WifiMorsel morsel : databaseLocation.getWifiMorsels()) {
                // if the morsel mac already exists, skip (contains calls equals)
                if (averageMorsels.contains(morsel)) {
                    continue;
                }
                // Morsel was not recorded with same device class, skip it
                if (requestDeviceClass != DeviceClass.getClass(morsel.getDeviceModel())) {
                    continue;
                }
                // this is the first occurrence of this morsel
                // find all similar ones and average them out
                int summedLevel = 0;
                int counter = 0;
                for (WifiMorsel specific : databaseLocation.getWifiMorsels()) {
                    if (specific.equals(morsel)) {
                        // found a hit for averaging
                        summedLevel += specific.getWifiLevel();
                        counter++;
                    }
                }
                int average = (int) (((float) summedLevel) / ((float) counter));
                //if (DeviceClass.isSimulatedClass(requestDeviceModel)) {
                //   average += DeviceClass.getSimulatedDifference(requestDeviceModel);
                //}
                averageMorsels.add(new WifiMorsel(morsel.getWifiMac(), morsel.getWifiName(), average, morsel.getWifiChannel(), morsel.getDeviceModel()));
            }
            databaseLocation.setWifiMorsels(averageMorsels);
        }

        // A Map that describes how many matches there are for this location
        HashMap<Location, Integer> locationMatchesMap = new HashMap<>();

        //Map of the sum of all wifi levels under the tolerance for a location.
        HashMap<Location, Integer> locationLevelDifferenceSumMap = new HashMap<>();


        // For each request morsel, check if a morsel with the same mac address exists in a database location.
        // Then check how far wifi levels are apart. If it is below a tolerance value increase the goodness of that location.
        for (WifiMorsel currentRequestMorsel : requestWifiMorsels) {
            for (Location dataBaseLocation : dataBaseLocations) {

                DataList<WifiMorsel> dataBaseLocationMorsels = dataBaseLocation.getWifiMorsels();

                // check if request morsel is contained in the current Locations Morsels
                int index;
                if ((index = dataBaseLocationMorsels.indexOf(currentRequestMorsel)) >= 0) {

                    int levelDifference = Math.abs(currentRequestMorsel.getWifiLevel() - dataBaseLocationMorsels.get(index).getWifiLevel());
                    // only morsels levels similar to the database levels are used in further processing
                    if (levelDifference <= tolerance) {
                        if (locationLevelDifferenceSumMap.get(dataBaseLocation) != null) {
                            levelDifference += locationLevelDifferenceSumMap.get(dataBaseLocation);
                        }
                        locationLevelDifferenceSumMap.put(dataBaseLocation, levelDifference);

                        int matchPointCounter = 1; // 1 for current match
                        if (locationMatchesMap.get(dataBaseLocation) != null) {
                            matchPointCounter += locationMatchesMap.get(dataBaseLocation);
                        }
                        locationMatchesMap.put(dataBaseLocation, matchPointCounter);
                    }
                }
            }
        }


        //FILTER - REMOVE ALL MATCHES WITH LESS THAN #leastMatches
        int leastMatches = 2;
        List<Location> locationsToRemove = new LinkedList<Location>();
        for (Location location : locationMatchesMap.keySet()) {
            if (locationMatchesMap.get(location) < leastMatches) {
                locationsToRemove.add(location);
            }
        }
        locationsToRemove.removeAll(locationsToRemove);
        locationLevelDifferenceSumMap.remove(locationsToRemove);
        //END FILTER - REMOVE ALL MATCHES WITH LESS THAN #leastMatches


        return getFinalMatch(locationMatchesMap, locationLevelDifferenceSumMap);
    }

    private boolean sameDeviceClass(String requestDeviceModel, String dbDeviceModel1) {
        boolean sameDevice;
        if (requestDeviceModel == null && dbDeviceModel1 == null) sameDevice = true;
        else if (requestDeviceModel != null && dbDeviceModel1 != null) {
            sameDevice = DeviceClass.getClass(dbDeviceModel1) == DeviceClass.getClass(requestDeviceModel);
        } else sameDevice = false;
        return sameDevice;
    }

    /**
     * Determines a final matching location.
     *
     * @param locationMatchesMap
     * @param locationLevelDifferenceSumMap
     * @return
     */
    private Location getFinalMatch(HashMap<Location, Integer> locationMatchesMap, HashMap<Location, Integer> locationLevelDifferenceSumMap) {
        List<Location> sortedLocationCandidateList;
        Location finalMatch = null;

        if (locationMatchesMap.size() > 0) {

            // Convert the HashMap to a List. It is sorted by the number of matches. Most matches are listed first.
            sortedLocationCandidateList = locationMapToSortedList(locationMatchesMap);

            //Trim list so it only contains non-best candidates
            for (int i = 0; i < sortedLocationCandidateList.size() - 1; i++) {
                if (locationMatchesMap.get(sortedLocationCandidateList.get(i)) > locationMatchesMap.get(sortedLocationCandidateList.get(i + 1))) {
                    sortedLocationCandidateList = sortedLocationCandidateList.subList(i + 1, sortedLocationCandidateList.size());
                }
            }

            for (Location loc : sortedLocationCandidateList) {
                locationLevelDifferenceSumMap.remove(loc);
            }

            //locationLevelDifferenceSumMap now containes only the best candidates

            //Reduce to a single match if there are still more than one candidates.
            if (sortedLocationCandidateList.size() > 1) {
                //sort points in respect of level
                sortedLocationCandidateList = locationMapToSortedList(locationLevelDifferenceSumMap);
                Collections.reverse(sortedLocationCandidateList); //reverse -> smallest to biggest

                //Trim list so it only contains best candidates (smallest levelDifferenceSum)
                for (int i = 0; i < sortedLocationCandidateList.size() - 1; i++) {
                    if (locationLevelDifferenceSumMap.get(sortedLocationCandidateList.get(i)) < locationLevelDifferenceSumMap.get(sortedLocationCandidateList.get(i + 1))) {
                        sortedLocationCandidateList = sortedLocationCandidateList.subList(0, i + 1);
                    }
                }

                // Now there are only those locations left, that have the same amount of matches AND the same levelDifferenceSum
                //e.g. -50 is a stronger dBm value (better signal) than -90
                if (sortedLocationCandidateList.size() > 1) {
                    //LAST CHANGE - MAX_VALUE TO MIN_VALUE & < TO >
                    int currentsum = Integer.MIN_VALUE;

                    for (Location point : sortedLocationCandidateList) { //for each point with same diff level
                        if (locationLevelDifferenceSumMap.get(point) > currentsum) {
                            //get point with max total level
                            currentsum = locationLevelDifferenceSumMap.get(point);
                            finalMatch = point;
                        }

                    }
                }
            }

            if (finalMatch == null && sortedLocationCandidateList.size() > 0) { //if no final match evaluted (e.g. only 1 match-point was found)
                finalMatch = sortedLocationCandidateList.get(0);
            }
        }
        // finalMatch is null if no matching location was found
        return finalMatch;
    }

    /**
     * Converts a HashMap of a Location to a List and sorts the keys by their value. (descending)
     */
    private List<Location> locationMapToSortedList(HashMap<Location, Integer> unsortedMap) {

        List<Location> sortedPoints = new LinkedList<>();

        List<Integer> list = new LinkedList<>(unsortedMap.values());
        Collections.sort(list);
        Set<Location> keys = unsortedMap.keySet();
        List<Location> usedPoints = new LinkedList<>();

        for (int value : list) {
            for (Location key : keys) {
                if (!usedPoints.contains(key) && unsortedMap.get(key) == value) {
                    sortedPoints.add(key);
                    usedPoints.add(key);
                }
            }
        }

        Collections.reverse(sortedPoints);

        return sortedPoints;
    }

    /**
     * Method that calls and handles finding of the best suited area to return based on the found location.
     *
     * @param location The location for which to find an area.
     * @return Most assuredly at least University, otherwise null. Usually you'll get a smaller area than University.
     */
    private Area getBestArea(Location location) {
        EventModuleManager eventModuleManager = EventModuleManager.getInstance();
        Data check = eventModuleManager.handleTask(Task.Location.SMALLEST_AREA_BY_LOCATION, location);
        if (!(check instanceof Area)) {
            return null;
        } else {
            return (Area) check;
        }
    }
}
