package servlet;

import logger.Messenger;

/**
 * Created by tamino on 2/19/14.
 */
public class SanitationSecurity {

    /**
     * Private instance. Use getInstance() to get an object.
     */
    private static SanitationSecurity INSTANCE;

    /**
     * Returns a useable instance of the class.
     * @return The object.
     */
    public static SanitationSecurity getInstance() {
        if (INSTANCE == null)
            INSTANCE = new SanitationSecurity();
        return INSTANCE;
    }

    private Messenger log;
    private final String TAG = "SanitationSecurity";

    /**
     * Private constructor. Use getInstance() to get a reference.
     */
    private SanitationSecurity() {
        log = Messenger.getInstance();
    }

    /**
     *
     * @return
     */
    //todo! Add functionality
    public boolean secure() {
        log.log(TAG,"WARNING: secure has not been implemented yet!");
        return true;
    }
}
