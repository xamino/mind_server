package de.uulm.mi.mind.logic.modules;

import de.uulm.mi.mind.logger.Messenger;
import de.uulm.mi.mind.logic.EventModuleManager;
import de.uulm.mi.mind.logic.Module;
import de.uulm.mi.mind.objects.*;
import de.uulm.mi.mind.objects.enums.Task;
import de.uulm.mi.mind.objects.messages.Error;
import de.uulm.mi.mind.objects.messages.Success;
import de.uulm.mi.mind.servlet.ServletFunctions;

import java.util.*;

/**
 * @author Tamino Hartmann
 *         Module that calculates a position based on a given set of WifiMorsels in a Location object.
 */
public class PositionModule extends Module {

    private final int tolerance = 3;  //TODO What value?
    /**
     * Timeout after which the position is reset to unknown for a user.
     */
    // TODO expose to admin?
    private final long POSITION_VALID_TIMEOUT = 10 * 60 * 1000;
    private final String TAG = "PositionModule";
    private Messenger log;

    public PositionModule() {
        log = Messenger.getInstance();
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
                // send back the location that the server thinks you're at with the area
                DataList loca = new DataList();
                loca.add(location);
                area.setLocations(loca);
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
                DataList sendUsers = new DataList();
                for (Object obj : users) {
                    User us = ((User) obj);
                    // if lastPosition is null, the user may not be active in the system
                    if (us.getPosition() == null) {
                        // so ignore
                        continue;
                    }
                    // TODO filter based on status!

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
                    toSend.setPosition(us.getPosition());
                    sendUsers.add(toSend);
                }
                return sendUsers;
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
        // Get Area containing all locations from database
        Area uniArea = (Area) ((DataList) EventModuleManager.getInstance().handleTask(Task.Area.READ, new Area("universe", null, 0, 0, 0, 0))).get(0);

        // A Map that describes how many matches there are for this location
        HashMap<Location, Integer> locationMatchesMap = new HashMap<>();

        //Map of the sum of all wifi levels under the tolerance for a location.
        HashMap<Location, Integer> locationLevelDifferenceSumMap = new HashMap<>();

        DataList<Location> dataBaseLocations = uniArea.getLocations();

        DataList<WifiMorsel> requestWifiMorsels = request.getWifiMorsels();

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

            //Trim list so it only contains non-best candidates TODO better way?
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

                //Trim list so it only contains best candidates (smallest levelDifferenceSum) TODO better way?
                for (int i = 0; i < sortedLocationCandidateList.size() - 1; i++) {
                    if (locationLevelDifferenceSumMap.get(sortedLocationCandidateList.get(i)) < locationLevelDifferenceSumMap.get(sortedLocationCandidateList.get(i + 1))) {
                        sortedLocationCandidateList = sortedLocationCandidateList.subList(0, i + 1);
                    }
                }

                // Now there are only those locations left, that have the same amount of matches AND the same levelDifferenceSum
                if (sortedLocationCandidateList.size() > 1) {
                    int currentsum = Integer.MAX_VALUE; // TODO Something wrong here!

                    for (Location point : sortedLocationCandidateList) { //for each point with same diff level
                        if (locationLevelDifferenceSumMap.get(point) < currentsum) {
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
