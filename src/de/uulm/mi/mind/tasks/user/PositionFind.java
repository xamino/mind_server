package de.uulm.mi.mind.tasks.user;

import de.uulm.mi.mind.logger.permanent.FileLogWrapper;
import de.uulm.mi.mind.objects.*;
import de.uulm.mi.mind.objects.Interfaces.Sendable;
import de.uulm.mi.mind.objects.enums.DeviceClass;
import de.uulm.mi.mind.objects.messages.Success;
import de.uulm.mi.mind.security.Active;
import de.uulm.mi.mind.tasks.UserTask;

import java.util.ArrayList;

/**
 * @author Tamino Hartmann
 */
public class PositionFind extends UserTask<Arrival, Sendable> {

    /**
     * Level tolerance for wifimorsels against which to match.
     */
    private final static int LEVEL_TOLERANCE = 3;
    /**
     * Morsel number tolerance for negotiation of goodness.
     */
    private final static int MORSEL_TOLERANCE = 0;
    /**
     * this value holds the difference between the s4 mini and the s3 mini
     * wifimorsels' levels (has to be subtracted from s4 mini to become s3 mini)
     */
    private final int S4MINI_S3MINI_DIFF = 9;
    private final String LAST_POSITION = "lastPosition";
    private final String REAL_POSITION = "realPosition";
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
        boolean update = false;
        // this implements server-side fuzziness to avoid fluttering of position_find
        if (last == null || real == null) {
            // this means it is the first time in this session, so we don't apply fuzziness
            active.writeData(LAST_POSITION, area);
            active.writeData(REAL_POSITION, area);
            user.setPosition(area.getID());
            update = true;
        } else if (last.getID().equals(area.getID()) && !real.getID().equals(area.getID())) {
            // update user for position, but only if last was already the same and the previous db entry is different
            active.writeData(REAL_POSITION, area);
            user.setPosition(area.getID());
            update = true;
        } else {
            // this means the area is different than the one before, so change last but not real:
            active.writeData(LAST_POSITION, area);
        }
        // log on change only to keep spam down
        if (update) {
            // log.error(TAG, "Updating log!");
            FileLogWrapper.positionUpdate(((User) active.getAuthenticated()), area);
        }
        // everything okay, return real position area (must be freshly read because we might have written to it)
        return ((Area) active.readData(REAL_POSITION));
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
     * General function for calculating a matched location from the database to the one given.
     *
     * @param request The location to match against.
     * @return Location if found, else null.
     */
    private Location calculateLocation(Location request) {
        // STEP 1: Read and prepare device class
        // Should exists because we checked for existing uni wifi morsel before
        String requestDeviceModel = request.getWifiMorsels().get(0).getDeviceModel();
        DeviceClass requestDeviceClass = DeviceClass.getClass(requestDeviceModel);
        // use default class if unknown
        if (requestDeviceClass == DeviceClass.UNKNOWN) {
            log.log(TAG, "Unknown Device: " + requestDeviceModel);
            requestDeviceClass = DeviceClass.CLASS2;
        }
        //S4 MINI TO S3 MINI CONVERSION
        // if device class is Galaxy S4 mini
        else if (requestDeviceClass == DeviceClass.CLASS7) {
            //convert morsels to S3 mini morsels
            for (WifiMorsel morsel : request.getWifiMorsels()) {
                morsel.setWifiLevel(morsel.getWifiLevel() - S4MINI_S3MINI_DIFF);
            }
            //set class to S3 mini for later calculations
            requestDeviceClass = DeviceClass.CLASS2;
            //S4 mini will now be treated as S3 mini
        }
        //END S4 MINI TO S3 MINI CONVERSION

        long timer = System.currentTimeMillis();
        // Get University Area containing all locations from database
        DataList<Area> read = database.read(new Area("University"), 5);
        if (read == null || read.isEmpty() || read.size() != 1) {
            log.error(TAG, "Database could not read University Area!");
            return null;
        }
        DataList<Location> dataBaseLocations = read.get(0).getLocations();

        timer -= System.currentTimeMillis();
        System.out.println("read university: "+timer);
        timer = System.currentTimeMillis();

        // prepare locations for finding match (remove useless MACs, calculate average)
        dataBaseLocations = prepareLocations(dataBaseLocations, request, requestDeviceClass);

        timer -= System.currentTimeMillis();
        System.out.println("prepare locations: "+timer);
        timer = System.currentTimeMillis();

        // keep only wifimorsels that are near our request morsels using LEVEL_TOLERANCE
        dataBaseLocations = trimToleranceMorsels(dataBaseLocations, request.getWifiMorsels());

        timer -= System.currentTimeMillis();
        System.out.println("trim tolerance: "+timer);
        timer = System.currentTimeMillis();

        //FILTER - REMOVE ALL MATCHES WITH LESS THAN #leastMatches
        int leastMatches = 3;
        ArrayList<Location> locationsToRemove = new ArrayList<>();
        for (Location location : dataBaseLocations) {
            if (location.getWifiMorsels().size() < leastMatches) {
                locationsToRemove.add(location);
            }
        }
        // remove
        for (Location location : locationsToRemove) {
            // remove from both
            dataBaseLocations.remove(location);
        }
        //END FILTER - REMOVE ALL MATCHES WITH LESS THAN #leastMatches

        timer -= System.currentTimeMillis();
        System.out.println("filter: "+timer);
        timer = System.currentTimeMillis();

        // now we check if we have to negotiate an answer
        int test = 0;
        Location location = null;
        boolean negotiate = false;
        for (Location check : dataBaseLocations) {
            int goodness = check.getWifiMorsels().size();
            // log.log(TAG, "Goodness of " + goodness + ".");
            if (goodness > test) {
                // this means we set a new maximum goodness match
                negotiate = false;
                // raise the bar, so to speak
                test = goodness;
                // remember
                location = check;
            } else if (inRange(goodness, test, MORSEL_TOLERANCE)) {
                // if this is still set to true when we are done, then we have multiple good matches
                negotiate = true;
                // NOTE: inRange is here only below test, as goodness > test is checked before. This guarantees that
                // location is set to the highest match no matter what.
            }
        }
        // this means we have 2 or more of the same goodness
        if (negotiate) {
            log.log(TAG, "Negotiating!");
            // find conflicts
            ArrayList<Location> conflicts = new ArrayList<>();
            for (Location baseLocation : dataBaseLocations) {
                if (inRange(baseLocation.getWifiMorsels().size(), location.getWifiMorsels().size(), MORSEL_TOLERANCE)) {
                    conflicts.add(baseLocation);
                }
            }
            // get best match
            location = negotiate(conflicts, request.getWifiMorsels());
        }

        timer -= System.currentTimeMillis();
        System.out.println("negotiation: "+timer);

        return location;
    }

    /**
     * Helper function to check if unknown is near comparator with near being closer than range.
     *
     * @param unknown    The value to check.
     * @param comparator The point of origin.
     * @param range      The range the value must be in to return true.
     * @return True or false.
     */
    private boolean inRange(int unknown, int comparator, int range) {
        return !(unknown < comparator - range || unknown > comparator + range);
    }

    /**
     * Given a location will compare its wifiMorsels against the request and return the location containing only
     * the morsels that fit within the LEVEL_TOLERANCE. NOTE: The size of the wifiMorsel Datalist is the goodness of the
     * answer!
     *
     * @param locations   The locations for which to trim the morsels.
     * @param wifiMorsels The request morsels against which to check the trim morsels.
     * @return The LEVEL_TOLERANCE filtered locations.
     */
    private DataList<Location> trimToleranceMorsels(DataList<Location> locations, DataList<WifiMorsel> wifiMorsels) {
        DataList<Location> newLocations = new DataList<>();
        for (Location location : locations) {
            // find morsels to keep
            DataList<WifiMorsel> toKeep = new DataList<>();
            for (WifiMorsel original : wifiMorsels) {
                for (WifiMorsel morsel : location.getWifiMorsels()) {
                    // we're only interested in comparing same ones, but datalist.get(original) doesn't exist, so
                    // we check that manually here
                    if (!original.equals(morsel)) {
                        continue;
                    }
                    int levelDifference = Math.abs(original.getWifiLevel() - morsel.getWifiLevel());
                    if (levelDifference <= LEVEL_TOLERANCE) {
                        toKeep.add(morsel);
                    }
                }
            }
            // only add if any remain :P
            if (!toKeep.isEmpty()) {
                // set
                location.setWifiMorsels(toKeep);
                newLocations.add(location);
            }
        }
        return newLocations;
    }

    /**
     * Given a list of locations with the same goodness value will return the one with the least variance from the
     * original one.
     *
     * @param conflictLocations The list of possible locations that may fit.
     * @param requestMatch      The location against which we compare the conflicting locations.
     * @return The best fit location
     */
    private Location negotiate(ArrayList<Location> conflictLocations, final DataList<WifiMorsel> requestMatch) {
        float bestMatch = Float.MAX_VALUE;
        Location bestLocation = null;
        // for each location
        for (Location location : conflictLocations) {
            int diffValueSum = 0;
            for (WifiMorsel original : requestMatch) {
                for (WifiMorsel morsel : location.getWifiMorsels()) {
                    if (!original.equals(morsel)) {
                        continue;
                    }
                    // add difference
                    diffValueSum += Math.abs(original.getWifiLevel() - morsel.getWifiLevel());
                }
            }
            // calculate bestMatch candidate
            float candidate = diffValueSum / (float) location.getWifiMorsels().size();
            if (candidate < bestMatch) {
                // if yes, take
                bestMatch = candidate;
                bestLocation = location;
            }
        }
        return bestLocation;
    }

    /**
     * Calls filterMacAddresses and averageMorselsForLocation correctly.
     *
     * @param fullLocations  The original full locations with way too many morsels.
     * @param searchLocation The location we want to locate. Used as a filter.
     * @return The averaged, reduced locations we'll work with.
     */
    private DataList<Location> prepareLocations(DataList<Location> fullLocations, final Location searchLocation, final DeviceClass deviceClass) {
        DataList<Location> toKeep = new DataList<>();
        // first filter for only those morsels with mac addresses we can use
        for (Location fullLocation : fullLocations) {
            Location filtered = filterMACandCLASS(fullLocation, searchLocation, deviceClass);
            if (filtered == null) {
                continue;
            }
            // average morsels
            filtered.setWifiMorsels(averageMorselsForLocation(filtered.getWifiMorsels()));
            // finally set
            toKeep.add(filtered);
        }
        // not full anymore, of course
        return toKeep;
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
    public Class<Arrival> getInputType() {
        return Arrival.class;
    }

    @Override
    public Class<Sendable> getOutputType() {
        return Sendable.class;
    }
}
