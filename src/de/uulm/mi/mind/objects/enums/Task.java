package de.uulm.mi.mind.objects.enums;

/**
 * Created by Cassio on 24.02.14.
 */
public interface Task {

    public enum User implements Task {
        CREATE, READ, UPDATE, DELETE,
        /**
         * Removes all Users from the database and restores the default admin
         */
        ANNIHILATE
    }

    public enum Position implements Task {
        FIND, READ
    }

    public enum Location implements Task {
        CREATE, READ, UPDATE, DELETE,
        /**
         * Given a location, returns all areas that contain it.
         */
        SMALLEST_AREA_BY_LOCATION,
        READ_MORSELS
    }

    public enum Area implements Task {
        CREATE,
        READ,
        UPDATE,
        DELETE,
        /**
         * Task to fetch all Locations of an Area
         */
        READ_LOCATIONS,
        /**
         * Removes all areas and locations from the database and restores the default area
         */
        ANNIHILATE
    }

    public enum Display implements Task {
        CREATE,
        READ,
        UPDATE,
        DELETE
    }
}


