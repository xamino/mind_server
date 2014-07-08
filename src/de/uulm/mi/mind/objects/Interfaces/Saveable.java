package de.uulm.mi.mind.objects.Interfaces;

/**
 * @author Tamino Hartmann
 */
public interface Saveable extends Data {
    /**
     * Returns the objects unique identifier
     *
     * @return the unique identifier for this object or null if it is not applicable.
     */
    public String getKey();

    public Saveable deepClone();

    public String getUnique();

    public void setUnique(String unique);
}
