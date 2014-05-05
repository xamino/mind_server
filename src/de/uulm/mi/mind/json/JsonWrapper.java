package de.uulm.mi.mind.json;

import de.uulm.mi.mind.objects.*;
import de.uulm.mi.mind.objects.messages.Success;

/**
 * Created by Tamino Hartmann.
 */
public class JsonWrapper {
    private static JsonWrapper INSTANCE;
    private JsonConverter<Data> converter;

    private JsonWrapper() {
        converter = new JsonConverter<Data>("$type");
        // register JSONator types
        converter.registerType(Area.class);
        converter.registerType(Arrival.class);
        converter.registerType(Departure.class);
        converter.registerType(de.uulm.mi.mind.objects.messages.Error.class);
        converter.registerType(Location.class);
        converter.registerType(PublicDisplay.class);
        converter.registerType(Success.class);
        converter.registerType(SensedDevice.class);
        converter.registerType(User.class);
        converter.registerType(WifiMorsel.class);
        converter.registerType(WifiSensor.class);
    }

    public static JsonWrapper getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new JsonWrapper();
        }
        return INSTANCE;
    }

    public Data fromJson(String json) {
        return converter.fromJson(json);
    }

    public String toJson(Data object) {
        return converter.toJson(object);
    }
}