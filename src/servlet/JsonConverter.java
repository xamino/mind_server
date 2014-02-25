package servlet;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import database.Data;
import database.objects.*;
import database.objects.Error;
import logger.Messenger;

/**
 * @author Tamino Hartmann
 *         <p/>
 *         Our version of GSON. Implements the RuntimeTypeAdapterFactory to allow easy recursive JSON data transfer. Do not
 *         touch if you don't know what you are doing! Also note that ArrayList for example does NOT throw an error, will
 *         be converted to JSON, but does NOT contain the required $type field to be parsed again! Make sure to use
 *         objects that extend the Data interface for EVERYTHING you send.
 */
// TODO: can i require data interface for everything somehow, enforcing it in hard code? Would remove need for non-existant error messages. :P
public class JsonConverter {

    private static JsonConverter INSTANCE;
    private final String TAG = "JsonConverter";
    private Gson gson;
    private Messenger log;

    /**
     * Private constructor. Use getInstance() to get an instance of this class. Note that all subtypes that extend
     * Data that are to be JSONated MUST be registered here!
     */
    private JsonConverter() {
        log = Messenger.getInstance();
        // Base data type that we use for JSON data: ($type is the attribute name)
        RuntimeTypeAdapterFactory<Data> factory = RuntimeTypeAdapterFactory.of(Data.class, "$type");
        // Register all JSON-objects: (The strings are the types, must be unique!)
        factory.registerSubtype(Area.class, "Area");
        factory.registerSubtype(Arrival.class, "Arrival");
        factory.registerSubtype(DataList.class, "DataList");
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

    /**
     * Get the instance of this class.
     *
     * @return The instance of this class.
     */
    public static JsonConverter getInstance() {
        if (INSTANCE == null)
            INSTANCE = new JsonConverter();
        return INSTANCE;
    }

    /**
     * Converts any object to JSON. Note that it and ALL subtypes MUST implement the Data interface, otherwise
     * the all-important type information is not written to the JSON field and the data becomes machine-unreadable.
     *
     * @param object The object to write to JSON.
     * @return The string with the JSON representation.
     */
    public String toJson(Data object) {
        return gson.toJson(object, Data.class);
    }

    /**
     * Converts a string to the correct Data implementing class.
     *
     * @param data The string containing the JSON data.
     * @return An object implementing Data with all the correct data.
     */
    public Data fromJson(String data) {
        // Returns the correct extended Data class, no need for casting.
        return gson.fromJson(data, Data.class);
    }
}