package de.uulm.mi.mind.objects.enums;

import java.util.Arrays;

/**
 * Created by Cassio on 05.05.2014.
 */
public enum DeviceClass {

    CLASS1, CLASS2, UNKNOWN;

    private static final String[] class1 = new String[]{"SONY LT25I", "LGE LG-D802"};
    private static final String[] class2 = new String[]{"SAMSUNG GT-I8190", "LGE NEXUS 5"};
    private static final String[] simulated = new String[]{};

    public static DeviceClass getClass(String requestDeviceModel) {
        if (requestDeviceModel == null) return UNKNOWN;
        if (Arrays.asList(class1).contains(requestDeviceModel.toUpperCase())) return CLASS1;
        else if (Arrays.asList(class2).contains(requestDeviceModel.toUpperCase())) return CLASS2;
        else return UNKNOWN;
    }

    public static boolean isSimulatedClass(String requestDeviceModel) {
        return Arrays.asList(simulated).contains(requestDeviceModel.toUpperCase());
    }
}
