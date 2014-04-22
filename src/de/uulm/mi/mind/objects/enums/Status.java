package de.uulm.mi.mind.objects.enums;

/**
 * Created by Cassio on 09.04.2014.
 */
public enum Status {
    AVAILABLE("Available"), AWAY("Away"), OCCUPIED("Occupied"), DO_NOT_DISTURB("DnD"), INVISIBLE("Invisible");
    private final String displayName;

    Status(String displayName) {
        this.displayName = displayName;
    }


    /**
     * Returns the status name for display purposes
     *
     * @return Name of the status
     */
    public String getDisplayName() {
        return displayName;
    }
}