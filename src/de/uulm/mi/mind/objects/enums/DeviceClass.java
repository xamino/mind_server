package de.uulm.mi.mind.objects.enums;

import java.util.Arrays;

/**
 * Created by Cassio on 05.05.2014.
 */
public enum DeviceClass {

    CLASS1, CLASS2, CLASS3, CLASS4, CLASS5, CLASS6, UNKNOWN;

    private static final String[] class1 = new String[]{"CLASS1", "SONY LT25I", "SONY C2105",}; // Sony Xperia 5
    private static final String[] class2 = new String[]{"CLASS2", "SAMSUNG GT-I8190"}; //S G S3 Mini
    private static final String[] class3 = new String[]{"CLASS3", "LGE NEXUS 5"};
    private static final String[] class4 = new String[]{"CLASS4", "LGE LG-D802"}; // LG G2
    private static final String[] class5 = new String[]{"CLASS5", "LGE NEXUS 4"};
    private static final String[] class6 = new String[]{"CLASS6", "SAMSUNG GALAXY NEXUS"}; // S Galaxy Nexus

    public static DeviceClass getClass(String requestDeviceModel) {
        if (requestDeviceModel == null) return UNKNOWN;
        if (Arrays.asList(class1).contains(requestDeviceModel.toUpperCase())) return CLASS1;
        else if (Arrays.asList(class2).contains(requestDeviceModel.toUpperCase())) return CLASS2;
        else if (Arrays.asList(class3).contains(requestDeviceModel.toUpperCase())) return CLASS3;
        else if (Arrays.asList(class4).contains(requestDeviceModel.toUpperCase())) return CLASS4;
        else if (Arrays.asList(class5).contains(requestDeviceModel.toUpperCase())) return CLASS5;
        else if (Arrays.asList(class6).contains(requestDeviceModel.toUpperCase())) return CLASS6;
        else return UNKNOWN;
    }
}
