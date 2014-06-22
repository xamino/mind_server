package de.uulm.mi.mind.logger.permanent;

import de.uulm.mi.mind.logger.anonymizer.Anonymizer;
import de.uulm.mi.mind.objects.Area;
import de.uulm.mi.mind.objects.Poll;
import de.uulm.mi.mind.objects.User;
import de.uulm.mi.mind.security.Authenticated;

import java.util.ArrayList;

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
    private static String POLLFILE = "polling";

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

    public static void pollVote(User user, Poll poll, ArrayList<String> added, ArrayList<String> removed) {
        final String userKey = anonymizer.getKey(user);
        final String pollKey = anonymizer.getKey(poll);
        StringBuilder builder = new StringBuilder();
        for (String s : added) {
            builder.append("+").append(s).append(" ");
        }
        for (String s : removed) {
            builder.append("-").append(s).append(" ");
        }
        final String options = builder.toString();
        fileLog.log(new LogWorker() {
            @Override
            public LogObject logCreate() {
                return new LogObject() {
                    @Override
                    public String getFileName() {
                        return POLLFILE;
                    }

                    @Override
                    public String getContent() {
                        return userKey + " @ " + pollKey + " : " + options;
                    }
                };
            }
        });
    }

    public static void pollCreate(User user, final Poll poll) {
        final String userKey = anonymizer.getKey(user);
        final String pollKey = anonymizer.getKey(poll);
        fileLog.log(new LogWorker() {
            @Override
            public LogObject logCreate() {
                return new LogObject() {
                    @Override
                    public String getFileName() {
                        return POLLFILE;
                    }

                    @Override
                    public String getContent() {
                        return "+++ " + pollKey + " by " + userKey + ": " + poll.getQuestion();
                    }
                };
            }
        });
    }

    public static void pollRemove(User user, Poll poll) {
        final String userKey = anonymizer.getKey(user);
        final String pollKey = anonymizer.getKey(poll);
        fileLog.log(new LogWorker() {
            @Override
            public LogObject logCreate() {
                return new LogObject() {
                    @Override
                    public String getFileName() {
                        return POLLFILE;
                    }

                    @Override
                    public String getContent() {
                        return "--- " + pollKey + " by " + userKey;
                    }
                };
            }
        });
    }
}
