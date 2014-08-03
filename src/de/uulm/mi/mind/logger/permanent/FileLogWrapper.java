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
    private static Anonymizer anonymizer = Anonymizer.getInstance();
    private static String SESSIONFILE = "session";
    private static String POSITIONFILE = "position";
    private static String POLLFILE = "polling";
    private static String IPFILE = "ips";

    /**
     * Given an Authenticated, log its login.
     *
     * @param data The Authenticated to login.
     * @param <E>  Object extending Authenticated.
     */
    public static <E extends Authenticated> void login(final E data) {
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
                        final String key = anonymizer.getKey(data);
                        return "+++ " + data.getClass().getSimpleName() + " " + key;
                    }
                };
            }
        });
    }

    /**
     * Log logout.
     *
     * @param data The Authenticated to logout.
     * @param <E>  Object extending Authenticated.
     */
    public static <E extends Authenticated> void logout(final E data) {
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
                        final String key = anonymizer.getKey(data);
                        return "--- " + data.getClass().getSimpleName() + " " + key;
                    }
                };
            }
        });
    }

    public static void positionUpdate(final User user, final Area area) {
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
                        final String userKey = anonymizer.getKey(user);
                        final String areaKey = anonymizer.getKey(area);
                        return "uuu " + userKey + " @ " + areaKey;
                    }
                };
            }
        });
    }

    public static void pollVote(final User user, final Poll poll, final ArrayList<String> added, final ArrayList<String> removed) {
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
                        StringBuilder builder = new StringBuilder();
                        for (String s : added) {
                            builder.append("+").append(s).append(" ");
                        }
                        for (String s : removed) {
                            builder.append("-").append(s).append(" ");
                        }
                        String options = builder.toString();
                        String userKey = anonymizer.getKey(user);
                        String pollKey = anonymizer.getKey(poll);
                        return "    " + userKey + " @ " + pollKey + " : " + options;
                    }
                };
            }
        });
    }

    // CAREFUL: MUST BE CALLED AFTER THE OBJECT EXISTS! (so not before session.write())
    public static void pollCreate(final User user, final Poll poll) {
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
                        String userKey = anonymizer.getKey(user);
                        String pollKey = anonymizer.getKey(poll);
                        return "+++ " + pollKey + " by " + userKey + ": " + poll.getQuestion();
                    }
                };
            }
        });
    }

    public static void pollRemove(final User user, final Poll poll) {
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
                        final String userKey = anonymizer.getKey(user);
                        final String pollKey = anonymizer.getKey(poll);
                        return "--- " + pollKey + " by " + userKey;
                    }
                };
            }
        });
    }

    public static void pollEnded(final Poll poll) {
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
                        final String pollKey = anonymizer.getKey(poll);
                        return "sss " + pollKey + " --> ended";
                    }
                };
            }
        });
    }

    public static void pollClosed(final Poll poll) {
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
                        final String pollKey = anonymizer.getKey(poll);
                        return "sss " + pollKey + " --> closed";
                    }
                };
            }
        });
    }

    public static void timeout(final Authenticated data) {
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
                        final String key = anonymizer.getKey(data);
                        return "~~~ " + data.getClass().getSimpleName() + " " + key + " timeout";
                    }
                };
            }
        });
    }

    public static void statusUpdate(final User user) {
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
                        final String key = anonymizer.getKey(user);
                        return "sss " + key + " set status to " + user.getStatus();
                    }
                };
            }
        });
    }
}
