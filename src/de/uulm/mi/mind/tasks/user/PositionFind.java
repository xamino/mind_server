package de.uulm.mi.mind.tasks.user;

import de.uulm.mi.mind.objects.*;
import de.uulm.mi.mind.objects.Interfaces.Sendable;
import de.uulm.mi.mind.objects.enums.DeviceClass;
import de.uulm.mi.mind.objects.messages.Success;
import de.uulm.mi.mind.security.Active;
import de.uulm.mi.mind.tasks.Task;

import java.util.*;

/**
 * @author Tamino Hartmann
 */
public class PositionFind extends Task<Arrival, Sendable> {

    private final String LAST_POSITION = "lastPosition";
    private final String REAL_POSITION = "realPosition";
    private final int tolerance = 3;
    private final String TAG = "PositionModule";

    @Override
    public boolean validateInput(Arrival object) {
        return (object.getObject() instanceof Location);
    }

    @Override
    public Sendable doWork(Active active, Arrival object, boolean compact) {
        User user = ((User) active.getAuthenticated());
        // find the area
        Sendable sendable = findPosition(((Location) object.getObject()));
        if (!(sendable instanceof Area)) {
            return sendable;
        }
        Area area = ((Area) sendable);
        // pull these out here to make checking if they exist easier
        Area last = ((Area) active.readData(LAST_POSITION));
        Area real = ((Area) active.readData(REAL_POSITION));
        // this implements server-side fuzziness to avoid fluttering of position_find
        if (last == null || real == null) {
            // this means it is the first time in this session, so we don't apply fuzziness
            active.writeData(LAST_POSITION, area);
            active.writeData(REAL_POSITION, area);
            user.setPosition(area.getID());
        } else if (last.getID().equals(area.getID())) {
            // update user for position, but only if last was already the same and the previous db entry is different
            active.writeData(REAL_POSITION, area);
            user.setPosition(area.getID());
        } else {
            // this means the area is different than the one before, so change last but not real:
            active.writeData(LAST_POSITION, area);
        }
        // everything okay, return real position area (must be freshly read because we might have written to it)
        return (Area) active.readData(REAL_POSITION);
    }

    /**
     * Takes an Arrival object for its IP address and Location and calculates a position from that.
     *
     * @param requestLocation Sensed location.
     * @return The Area with the calculated Location in it.
     */
    private Sendable findPosition(Location requestLocation) {
        // First check if there is any AP measurement from the university
        boolean isAtUniversity = false;
        String uniSSID = configuration.getUniversitySSID();
        for (WifiMorsel morsel : requestLocation.getWifiMorsels()) {
            if (morsel.getWifiName().equals(uniSSID)) {
                isAtUniversity = true;
                break;
            }
        }

        if (!isAtUniversity) {
            return new Success(Success.Type.NOTE, "Your position could not be found as you're not at the university.");
        }

        // Everything okay from here on out:
        long time = System.currentTimeMillis();
        Location location = calculateLocation(requestLocation);
        log.log(TAG, "calculateLocation " + (System.currentTimeMillis() - time) + "ms");
        if (location == null) {
            // this means the location could not be found in the DB but user is at University
            return new Area("University");
        }

        // get best area for location to return
        time = System.currentTimeMillis();
        Area area = getBestArea(location);
        log.log(TAG, "getBestArea " + (System.currentTimeMillis() - time) + "ms");
        if (area == null) {
            log.error(TAG, "NULL area for position_find – shouldn't happen as University should be returned at least!");
            return new Success(Success.Type.NOTE, "Your position could not be found.");
        }
        // log.log(TAG, "Algo sees device at " + area.getID() + ".");

        // send back the location that the server thinks you're at with the area
        DataList<Location> loca = new DataList<>();
        location.setWifiMorsels(null); // TODO are they required somewhere after this?
        loca.add(location);
        area.setLocations(loca);

        return area;
    }

    /**
     * @param request
     * @return Location if found, else null.
     */

    private Location calculateLocation(Location request) {
        // Morsels from the current request which are to be compared to the database values
        DataList<WifiMorsel> requestWifiMorsels = request.getWifiMorsels();

        // STEP 1: Read and prepare device class

        // Should exists because we checked for existing uni wifi morsel before
        String requestDeviceModel = requestWifiMorsels.get(0).getDeviceModel();
        DeviceClass requestDeviceClass = DeviceClass.getClass(requestDeviceModel);
        // use default class if unknown
        if (requestDeviceClass == DeviceClass.UNKNOWN) {
            log.log(TAG, "Unknown Device: " + requestDeviceModel);
            requestDeviceClass = DeviceClass.CLASS2;
        }

        // Get University Area containing all locations from database
        DataList<Area> read = database.read(new Area("University"), 5);

        if (read == null || read.isEmpty() || read.size() != 1) {
            log.error(TAG, "Database could not read University Area!");
            return null;
        }

        // from here on only objects with a valid key == single ones are queried
        Area uniArea = read.get(0);
        DataList<Location> dataBaseLocations = uniArea.getLocations();

        // prepare locations for finding match (remove useless MACs, calculate average)
        dataBaseLocations = prepareLocations(dataBaseLocations, request, requestDeviceClass);

        // A Map that describes how many matches there are for this location
        HashMap<Location, Integer> locationMatchesMap = new HashMap<>();

        //Map of the sum of all wifi levels under the tolerance for a location.
        HashMap<Location, Integer> locationLevelDifferenceSumMap = new HashMap<>();


        // For each request morsel, check if a morsel with the same mac address exists in a database location.
        // Then check how far wifi levels are apart. If it is below a tolerance value increase the goodness of that location.
        for (WifiMorsel currentRequestMorsel : requestWifiMorsels) {
            for (Location dataBaseLocation : dataBaseLocations) {
                DataList<WifiMorsel> dataBaseLocationMorsels = dataBaseLocation.getWifiMorsels();

                if (dataBaseLocationMorsels == null) {
                    log.error(TAG, "DB Location  " + dataBaseLocation.getKey() + " has null as Morsellist!");
                    continue;
                }

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
        ArrayList<Location> locationsToRemove = new ArrayList<>();
        for (Location location : locationMatchesMap.keySet()) {
            if (locationMatchesMap.get(location) < leastMatches) {
                locationsToRemove.add(location);
            }
        }
        // remove
        for (Location location : locationsToRemove) {
            // remove from both
            locationMatchesMap.remove(location);
            // if contained remove here too
            if (locationLevelDifferenceSumMap.containsKey(location)) {
                locationLevelDifferenceSumMap.remove(location);
            } else {
                // warn for us
                log.error(TAG, "WARNING — location could not be found in locationLevelDifferenceMap!");
            }
        }
        //END FILTER - REMOVE ALL MATCHES WITH LESS THAN #leastMatches


        return getFinalMatch(locationMatchesMap, locationLevelDifferenceSumMap);
    }

    /**
     * Calls filterMacAddresses and averageMorselsForLocation correctly.
     *
     * @param fullLocations  The original full locations with way too many morsels.
     * @param searchLocation The location we want to locate. Used as a filter.
     * @return The averaged, reduced locations we'll work with.
     */
    private DataList<Location> prepareLocations(DataList<Location> fullLocations, final Location searchLocation, final DeviceClass deviceClass) {
        // first filter for only those morsels with mac addresses we can use
        for (int i = fullLocations.size() - 1; i >= 0; i--) {
            Location filtered = filterMACandCLASS(fullLocations.get(i), searchLocation, deviceClass);
            if (filtered == null) {
                fullLocations.remove(i);
                continue;
            }
            // average morsels
            filtered.setWifiMorsels(averageMorselsForLocation(filtered.getWifiMorsels()));
            // finally set
            fullLocations.set(i, filtered);
        }
        // not full anymore, of course
        return fullLocations;
    }

    /**
     * Filters wifiMorsels to match MAC address from searchLocations.
     *
     * @param location       The location to filter.
     * @param searchLocation The location we want to locate. Used as a filter.
     * @return Null if no more morsels, else filtered location.
     */
    private Location filterMACandCLASS(Location location, final Location searchLocation, final DeviceClass deviceClass) {
        DataList<WifiMorsel> toKeep = new DataList<>();
        // find morsels to add
        for (WifiMorsel check : location.getWifiMorsels()) {
            for (WifiMorsel original : searchLocation.getWifiMorsels()) {
                if (check.getWifiMac().equals(original.getWifiMac()) && deviceClass == DeviceClass.getClass(check.getDeviceModel())) {
                    toKeep.add(check);
                }
            }
        }
        // if empty, we don't require this location anymore
        if (toKeep.isEmpty()) {
            return null;
        }
        location.setWifiMorsels(toKeep);
        return location;
    }

    /**
     * Averages the given locations based on MAC addresses.
     *
     * @param original The original list.
     * @return The averaged list.
     */
    private DataList<WifiMorsel> averageMorselsForLocation(final DataList<WifiMorsel> original) {
        DataList<WifiMorsel> averageMorsels = new DataList<>();
        for (WifiMorsel morsel : original) {
            // if the morsel mac already exists, skip (contains calls equals)
            if (averageMorsels.contains(morsel)) {
                continue;
            }
            // this is the first occurrence of this morsel
            // find all similar ones and average them out
            int summedLevel = 0;
            int counter = 0;
            for (WifiMorsel specific : original) {
                if (specific.equals(morsel)) {
                    // found a hit for averaging
                    summedLevel += specific.getWifiLevel();
                    counter++;
                }
            }
            int average = (int) (((float) summedLevel) / ((float) counter));
            averageMorsels.add(new WifiMorsel(morsel.getWifiMac(), morsel.getWifiName(), average, morsel.getWifiChannel(),
                    morsel.getDeviceModel(), morsel.getTimeStamp()));
        }
        return averageMorsels;
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
        // Get all areas
        long time = System.currentTimeMillis();
        DataList<Area> all = database.read(new Area(null), 1); // only area itself without children requred
        if (all == null) {
            log.error(TAG, "All areas: dbCall == null – shouldn't happen, FIX!");
            return null;
        }
        log.log(TAG, "read " + (System.currentTimeMillis() - time) + "ms");

        int x = all.indexOf(new Area("University"));
        Area finalArea = all.get(x);
        for (Area temp : all) {
            if (temp.getArea() < finalArea.getArea()
                    && temp.contains(location.getCoordinateX(), location.getCoordinateY())) {
                finalArea = temp;
            }
        }

        return finalArea;
    }

    @Override
    public String getTaskName() {
        return "position_find";
    }

    @Override
    public Set<String> getTaskPermission() {
        Set<String> permissible = new HashSet<>();
        permissible.add(User.class.getSimpleName());
        return permissible;
    }

    @Override
    public Class<Arrival> getInputType() {
        return Arrival.class;
    }

    @Override
    public Class<Sendable> getOutputType() {
        return Sendable.class;
    }

    @Override
    public boolean isAdminTask() {
        return false;
    }
}
