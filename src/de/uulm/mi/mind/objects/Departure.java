package de.uulm.mi.mind.objects;

/**
 * @author Tamino Hartmann
 *         Wrapper object class for outgoing answers – required because lists for example can not simply be Jsonated.
 */
public class Departure implements Data {
    /**
     * The data object that is sent.
     */
    private Data object;

    private Departure() {
        
    }

    /**
     * Constructor.
     *
     * @param object The object to send.
     */
    public Departure(Data object) {
        this.object = object;
    }

    public Data getObject() {
        return object;
    }

    public void setObject(Data object) {
        this.object = object;
    }

    @Override
    public String getKey() {
        return null;
    }
}
