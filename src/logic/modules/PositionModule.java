package logic.modules;

import database.Data;
import database.messages.Error;
import database.objects.Area;
import database.objects.DataList;
import database.objects.Location;
import database.objects.WifiMorsel;
import logger.Messenger;
import logic.EventModuleManager;
import logic.Module;
import logic.Task;

import java.util.*;

/**
 * @author Tamino Hartmann
 *         Module that calculates a position based on a given set of WifiMorsels in a Location object.
 */
public class PositionModule extends Module {

    private Messenger log;
    private final int tolerance = 3;  //TODO What value?

    public PositionModule() {
        log = Messenger.getInstance();
    }

    @Override
    public Data run(Task task, Data request) {
        if (!(task instanceof Task.Position)) {
            return new Error("WrongTaskType", "PositionModule was called with the wrong task type!");
        }
        if (!(request instanceof Location)) {
            return new Error("WrongObjectType", "PositionModule was called with the wrong object type!");
        }
        // Everything okay from here on out:
        // todo put algorithm here

        return calculateLocation((Location) request);


        //return new Message("PositionUnimplemented", "Position has not been implemented yet!");
    }

    private Location calculateLocation(Location request) {
        // Get Area containing all locations from database
        Area allArea = ((DataList<Area>) EventModuleManager.getInstance().handleTask(Task.Area.READ_ALL, null)).get(0); // TODO better way?

        // A Map that describes how many matches there are for this location
        HashMap<Location, Integer> locationMatchesMap = new HashMap<>();

        //Map of the sum of all wifi levels under the tolerance for a location.
        HashMap<Location, Integer> locationLevelDifferenceSumMap = new HashMap<>();

        DataList<Location> dataBaseLocations = allArea.getLocations();

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
                    sortedLocationCandidateList = sortedLocationCandidateList.subList(i+1, sortedLocationCandidateList.size());
                }
            }

            Set<Location> keySet = locationLevelDifferenceSumMap.keySet();
            for(Location loc : sortedLocationCandidateList){
                locationLevelDifferenceSumMap.remove(loc);
            }

            //locationLevelDifferenceSumMap now containes only the best candidates

            //Reduce to a single match if there are still more than one candidates.
            if (sortedLocationCandidateList.size() > 1) {
                //sort points in respect of level
                sortedLocationCandidateList  = locationMapToSortedList(locationLevelDifferenceSumMap);
                Collections.reverse(sortedLocationCandidateList); //reverse -> smallest to biggest

                //Trim list so it only contains best candidates (smallest levelDifferenceSum) TODO better way?
                for (int i = 0; i < sortedLocationCandidateList.size() - 1; i++) {
                    if (locationLevelDifferenceSumMap.get(sortedLocationCandidateList.get(i)) < locationLevelDifferenceSumMap.get(sortedLocationCandidateList.get(i + 1))) {
                        sortedLocationCandidateList = sortedLocationCandidateList.subList(0, i+1);
                    }
                }

                // Now there are only those locations left, that have the same amount of matches AND the same levelDifferenceSum
                if (sortedLocationCandidateList.size() > 1) {
                    int currentsum = Integer.MAX_VALUE; // TODO Something wrong here!

                    for (Location point : sortedLocationCandidateList) { //for each point with same diff level
                        if (currentsum < locationLevelDifferenceSumMap.get(point)) {
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

        // finalMatch is null if now matching location was found
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
}
