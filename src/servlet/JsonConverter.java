package servlet;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import database.Data;
import database.objects.*;
import database.objects.Error;
import logger.Messenger;

/**
 * @author Tamino Hartmann
 */
public class JsonConverter {

    private static JsonConverter INSTANCE;
    private final String TAG = "JsonConverter";
    private Gson gson;
    private Messenger log;

    private JsonConverter() {
        log = Messenger.getInstance();
        // Base data type that we use for JSON data: ($type is the attribute name)
        RuntimeTypeAdapterFactory<Data> factory = RuntimeTypeAdapterFactory.of(Data.class, "$type");
        // Register all JSON-objects: (The strings are the types, must be unique!)
        factory.registerSubtype(Arrival.class, "Arrival");
        factory.registerSubtype(Area.class, "Area");
        factory.registerSubtype(Error.class, "Error");
        factory.registerSubtype(Location.class, "Location");
        factory.registerSubtype(Message.class, "Message");
        factory.registerSubtype(Success.class, "Success");
        factory.registerSubtype(User.class, "User");
        factory.registerSubtype(WifiMorsel.class, "WifiMorsel");
        // Register adapter
        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapterFactory(factory);
        gson = builder.create();
        log.log(TAG, "Created.");
    }

    public static JsonConverter getInstance() {
        if (INSTANCE == null)
            INSTANCE = new JsonConverter();
        return INSTANCE;
    }

    public String toJson(Data object) {
        log.log(TAG, "WRITE");
        return gson.toJson(object, Data.class);
    }

    public Data fromJson(String data) {
        log.log(TAG, "READ");
        return gson.fromJson(data, Data.class);
    }
}
