package de.uulm.mi.mind.objects.messages;

import de.uulm.mi.mind.objects.enums.MsgEnum;

/**
 * @author Tamino Hartmann
 */
public class Success extends Information {

    public Success(String description) {
        super(Type.OK, description);
    }

    public Success(Type type, String description) {
        super(type, description);
    }

    public enum Type implements MsgEnum {
        /**
         * For when everything is really okay.
         */
        OK,
        /**
         * Okay, but note that it is a bit different!
         */
        NOTE
    }
}