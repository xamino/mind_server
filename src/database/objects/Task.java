package database.objects;

/**
 * Created by Cassio on 24.02.14.
 */
public interface Task {

    public enum User implements Task {
        CREATE, READ, UPDATE, DELETE, READ_ALL
    }

    public enum Server implements Task {
        // TODO comment
        LOGIN, REGISTRATION, TEST, LOGOUT, ECHO, ERROR;

        /**
         * Use as a safe valueOf. Instead of throwing the error, incorrect values are returned as ERROR enum type.
         *
         * @param value The string to try to convert.
         * @return The enum type. If unknown or incorrect value, ERROR.
         */
        public static Server safeValueOf(String value) {
            try {
                return Server.valueOf(value);
            } catch (IllegalArgumentException e) {
                return ERROR;
            }
        }
    }
}


