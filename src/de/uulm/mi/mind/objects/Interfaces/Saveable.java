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
}
