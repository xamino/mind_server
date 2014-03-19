package logic;

/**
 * Created by Cassio on 24.02.14.
 */
public interface Task {

    public enum User implements Task {
        CREATE, READ, UPDATE, DELETE,
        /**
         * Removes all Users from the database and restores the default admin
         */
        ANNIHILATE,
    }

    /**
     * This enum represents the actual, external API that can be called. Well, this and Sanitation. All tasks
     * must be listed here, with collision free names. Tasks should start with the name of their module, then
     * continue with an expressive name that describes what is done.
     */
    public enum API implements Task {
        /**
         * Simply returns the object sent to the server. Can be used for various parsing tests.
         */
        ECHO,
        // USER
        USER_READ,
        USER_UPDATE,
        USER_DELETE,
        // LOCATION
        LOCATION_READ,
        LOCATION_ADD,
        // LOCATION_UPDATE,
        LOCATION_REMOVE,
        // AREA
        AREA_READ,
        AREA_ADD,
        AREA_UPDATE,
        AREA_REMOVE,
        DISPLAY_READ,
        DISPLAY_ADD,
        DISPLAY_UPDATE,
        DISPLAY_REMOVE,
        /**
         * Given a location, find the position.
         */
        POSITION_FIND,
        // ADMIN
        READ_ALL_ADMIN,
        ADMIN_USER_READ,
        ADMIN_USER_ADD,
        ADMIN_USER_UPDATE,
        ADMIN_USER_DELETE,
        /**
         * Removes all areas and locations from the database and restores the default area
         */
        ADMIN_ANNIHILATE_AREA,
        /**
         * Removes all users from the database and restores the default admin
         */
        ADMIN_ANNIHILATE_USER,
        TOGGLE_ADMIN,
        /**
         * General error, especially when casting to a task doesn't work.
         */
        ERROR;

        /**
         * Use as a safe valueOf. Instead of throwing the error, incorrect values are returned as ERROR enum type. The
         * value can also be mixed case, as it will be cast to all upper case.
         *
         * @param value The string to try to convert.
         * @return The enum type. If unknown or incorrect value, ERROR.
         */
        public static API safeValueOf(String value) {
            try {
                return API.valueOf(value.toUpperCase());
            } catch (IllegalArgumentException e) {
                return ERROR;
            }
        }
    }

    public enum Sanitation implements Task {
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
        ERROR;

        /**
         * Use as a safe valueOf. Instead of throwing the error, incorrect values are returned as ERROR enum type. The
         * value can also be mixed case, as it will be cast to all upper case.
         *
         * @param value The string to try to convert.
         * @return The enum type. If unknown or incorrect value, ERROR.
         */
        public static Sanitation safeValueOf(String value) {
            try {
                return Sanitation.valueOf(value.toUpperCase());
            } catch (IllegalArgumentException e) {
                return ERROR;
            }
        }
    }

    public enum Position implements Task {
        FIND
    }

   /* public enum WifiMorsel implements Task {
        CREATE, READ, UPDATE, DELETE
    }*/

    public enum Location implements Task {
        CREATE, READ, UPDATE, DELETE, READ_MORSELS
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
        ADD_DISPLAY,
        UPDATE_DISPLAY,
        REMOVE_DISPLAY
    }
}


