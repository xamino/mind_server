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

    public enum Security implements Task {
        /**
         * Tries to log a user into the system.
         */
        LOGIN,
        /**
         * Registers a new user.
         */
        REGISTRATION,
        /**
         * If applicable logs a user out.
         */
        LOGOUT,
        /**
         * Check if a session is valid.
         */
        CHECK,
        ERROR, UPDATE;

        /**
         * Use as a safe valueOf. Instead of throwing the error, incorrect values are returned as ERROR enum type. The
         * value can also be mixed case, as it will be cast to all upper case.
         *
         * @param value The string to try to convert.
         * @return The enum type. If unknown or incorrect value, ERROR.
         */
        public static Security safeValueOf(String value) {
            try {
                return Security.valueOf(value.toUpperCase());
            } catch (IllegalArgumentException e) {
                return ERROR;
            }
        }
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


