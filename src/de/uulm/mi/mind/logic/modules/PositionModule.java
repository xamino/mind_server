package de.uulm.mi.mind.logic.modules;

import de.uulm.mi.mind.logger.Messenger;
import de.uulm.mi.mind.logic.EventModuleManager;
import de.uulm.mi.mind.logic.Module;
import de.uulm.mi.mind.objects.*;
import de.uulm.mi.mind.objects.enums.Status;
import de.uulm.mi.mind.objects.enums.Task;
import de.uulm.mi.mind.objects.messages.Error;
import de.uulm.mi.mind.objects.messages.Success;
import de.uulm.mi.mind.security.Security;
import de.uulm.mi.mind.servlet.ServletFunctions;

import java.util.*;

/**
 * @author Tamino Hartmann
 *         Module that calculates a position based on a given set of WifiMorsels in a Location object.
 */
public class PositionModule extends Module {

    private final int tolerance = 3;
    /**
     * Timeout after which the position is reset to unknown for a user.
     */
    // TODO expose to admin?
    private final long POSITION_VALID_TIMEOUT = 10 * 60 * 1000;
    private final String TAG = "PositionModule";
    private Messenger log;
    private HashMap<String, SensedDevice> sensedDevices;

    public PositionModule() {
        log = Messenger.getInstance();
        sensedDevices = new HashMap<>();
    }

    @Override
    public Data run(Task task, Data request) {
        if (!(task instanceof Task.Position)) {
            return new Error(Error.Type.TASK, "PositionModule was called with the wrong task type!");
        }
        Task.Position todo = (Task.Position) task;
        switch (todo) {
            case FIND:
                if (!(request instanceof Location)) {
                    return new Error(Error.Type.WRONG_OBJECT, "PositionModule was called with the wrong object type!");
                }
                // Everything okay from here on out:
                Location location = calculateLocation((Location) request);
                if (location == null) {
                    // this means the location could not be found in the DB
                    return new Success(Success.Type.NOTE, "Your position could not be found.");
                }
                // get best area for location to return
                Area area = getBestArea(location);
                if (area == null) {
                    log.error(TAG, "NULL area for position_find – shouldn't happen as universe should be returned at least!");
                    return new Success(Success.Type.NOTE, "Your position could not be found.");
                }
                // todo add wifisensor functions
                // send back the location that the server thinks you're at with the area
                DataList<Location> loca = new DataList<>();
                loca.add(location);
                area.setLocations(loca); // TODO This causes the bug, but why?!
                return area;
            case READ:
                // read all users
                Data evtlUserList = read(new User(null));
                Data msg = ServletFunctions.getInstance().checkDataMessage(evtlUserList, DataList.class);
                if (msg != null) {
                    return msg;
                }
                DataList users = ((DataList) evtlUserList);
                // filter the list – apply status and set special cases
                DataList<User> sendUsers = new DataList<>();
                for (Object obj : users) {
                    User us = ((User) obj);
                    String position = us.getPosition();
                    // if lastPosition is null, the user may not be active in the system
                    if (position == null || !Security.readActives().contains(us)) { //TODO update user when logged out or keep position?
                        // so ignore
                        continue;
                    } else if (position.equals("universe")) {
                        position = null;
                    }
                    // filter by status, don't return invisible users
                    if (us.getStatus() == null || us.getStatus() == Status.INVISIBLE) {
                        continue;
                    } else if (us.getStatus() == Status.AWAY) { // TODO prob not needed as null should be mapped to away by client already.
                        position = null;
                    }

                    // filter based on time
                    // todo This is not the last position time – how do i do this better?
                    // todo Update lastPosition to null?
                    Date lastAccess = us.getAccessDate();
                    // if the user object has a position, it should have an access, so we warn for this as it could be
                    // a bug
                    if (lastAccess == null) {
                        log.error(TAG, "Read positions: user " + us.readIdentification() + " has position but no access! Probably a bug...");
                        continue;
                    }
                    Long timeDelta = System.currentTimeMillis() - lastAccess.getTime();
                    if (timeDelta > POSITION_VALID_TIMEOUT) {
                        // if last update is longer gone, then ignore
                        continue;
                    }
                    // Filter user object to only give name + position
                    User toSend = new User(us.getEmail(), us.getName());
                    toSend.setPosition(position);
                    sendUsers.add(toSend);
                }
                return sendUsers;
            case SENSOR_WRITE:
                if (!(request instanceof SensedDevice)) {
                    return new Error(Error.Type.WRONG_OBJECT, "SensorWrite was called with the wrong object type!");
                }
                // todo this always adds devices, when do we remove them from the sensedDevices HashMap? Timeout?
                SensedDevice device = ((SensedDevice) request);
                if (!sensedDevices.containsKey(device.getIpAddress())) {
                    // not sensed before, so just add it and continue
                    log.log(TAG, "Found new device to track: " + device.getIpAddress());
                    sensedDevices.put(device.getIpAddress(), device);
                    return new Success("New device added.");
                }
                // already in list, so check if to update if level is higher
                // todo: we should also check for update of location with a threshold
                // so that lvl40 but new room is taken over lvl41 old room
                int oldLevel = sensedDevices.get(device.getIpAddress()).getLevelValue();
                if (oldLevel <= device.getLevelValue()) {
                    log.log(TAG, "Updated location of " + device.getIpAddress() + " to " + device.getSensor() + ".");
                    sensedDevices.put(device.getIpAddress(), device);
                }
                return new Success("Updated lists.");
            default:
                log.error(TAG, "Unknown task #" + todo + "# sent to PositionModule! Shouldn't happen!");
                return new Error(Error.Type.TASK, "Unknown task sent to PositionModule!");
        }
    }

    /**
     * @param request
     * @return Location if found, else null.
     */
    private Location calculateLocation(Location request) {
        // Morsels from the current request which are to be compared to the database values
        DataList<WifiMorsel> requestWifiMorsels = request.getWifiMorsels();

        // Get universe Area containing all locations from database
        Area uniArea = (Area) ((DataList) EventModuleManager.getInstance().handleTask(Task.Area.READ, new Area("universe"))).get(0);
        DataList<Location> dataBaseLocations = uniArea.getLocations();
        DataList<Location> averagedDatabaseLocations = new DataList<>(); //TODO another DB overwrite Bug fix

        // Modify database List to contain the average Morsel signal strengths for each location
        for (Location databaseLocation : dataBaseLocations) {
            DataList<WifiMorsel> averageMorsels = new DataList<>();
            for (WifiMorsel morsel : databaseLocation.getWifiMorsels()) {
                if (averageMorsels.contains(morsel)) {
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
                int average = summedLevel / counter;
                averageMorsels.add(new WifiMorsel(morsel.getWifiMac(), morsel.getWifiName(), average, morsel.getWifiChannel()));
            }
            averagedDatabaseLocations.add(new Location(databaseLocation.getCoordinateX(), databaseLocation.getCoordinateY(), averageMorsels));
        }

        // TODO For testing the DB bug
        // Area uniArea1 = (Area) ((DataList) EventModuleManager.getInstance().handleTask(Task.Area.READ, new Area("universe"))).get(0);

        // A Map that describes how many matches there are for this location
        HashMap<Location, Integer> locationMatchesMap = new HashMap<>();

        //Map of the sum of all wifi levels under the tolerance for a location.
        HashMap<Location, Integer> locationLevelDifferenceSumMap = new HashMap<>();


        // For each request morsel, check if a morsel with the same mac address exists in a database location.
        // Then check how far wifi levels are apart. If it is below a tolerance value increase the goodness of that location.
        for (WifiMorsel currentRequestMorsel : requestWifiMorsels) {
            for (Location dataBaseLocation : averagedDatabaseLocations) {

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
        return getFinalMatch(locationMatchesMap, locationLevelDifferenceSumMap);
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

            if (finalMatch == null) { //if no final match evaluted (e.g. only 1 match-point was found)
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
     * @return Most assuredly at least universe, otherwise null. Usually you'll get a smaller area than universe.
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
