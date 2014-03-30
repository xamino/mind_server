package de.uulm.mi.mind.objects.messages;


import de.uulm.mi.mind.objects.Data;

/**
 * @author Tamino Hartmann
 *         Interface for simple messages that are to be sent. Used to differentiate these from server data.
 */
public abstract class Information implements Data {
    @Override
    public String getKey() {
        return null;
    }
}
