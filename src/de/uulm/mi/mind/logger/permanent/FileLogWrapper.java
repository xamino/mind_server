package de.uulm.mi.mind.logger.permanent;

import de.uulm.mi.mind.logger.anonymizer.Anonymizer;
import de.uulm.mi.mind.objects.Area;
import de.uulm.mi.mind.objects.User;
import de.uulm.mi.mind.security.Authenticated;

/**
 * @author Tamino Hartmann
 *         This class wraps the FileLog so that we can easily work with it in the server via a simple one line method
 *         call. We also implement the anonymous
 */
public class FileLogWrapper {

    private static FileLog fileLog = FileLog.getInstance();
    // todo generate a new anonymizer when the day changes
    private static Anonymizer anonymizer = Anonymizer.getInstance();
    private static String SESSIONFILE = "session";
    private static String POSITIONFILE = "position";

    public static <E extends Authenticated> void login(final E data) {
        final String key = anonymizer.getKey(data);
        fileLog.log(new LogWorker() {
            @Override
            public LogObject logCreate() {
                return new LogObject() {
                    @Override
                    public String getFileName() {
                        return SESSIONFILE;
                    }

                    @Override
                    public String getContent() {
                        return "+++ " + data.getClass().getSimpleName() + " " + key;
                    }
                };
            }
        });
    }

    public static <E extends Authenticated> void logout(final E data) {
        final String key = anonymizer.getKey(data);
        fileLog.log(new LogWorker() {
            @Override
            public LogObject logCreate() {
                return new LogObject() {
                    @Override
                    public String getFileName() {
                        return SESSIONFILE;
                    }

                    @Override
                    public String getContent() {
                        return "--- " + data.getClass().getSimpleName() + " " + key;
                    }
                };
            }
        });
        anonymizer.removeKey(data);
    }

    public static void positionUpdate(User user, Area area) {
        // get keys for log
        final String userKey = anonymizer.getKey(user);
        final String areaKey = anonymizer.getKey(area);
        // write
        fileLog.log(new LogWorker() {
            @Override
            public LogObject logCreate() {
                return new LogObject() {
                    @Override
                    public String getFileName() {
                        return POSITIONFILE;
                    }

                    @Override
                    public String getContent() {
                        return userKey + " @ " + areaKey;
                    }
                };
            }
        });
    }
}
