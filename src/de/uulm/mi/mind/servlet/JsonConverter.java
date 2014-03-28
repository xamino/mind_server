package de.uulm.mi.mind.servlet;

import com.github.julman99.gsonfire.GsonFireBuilder;
import com.github.julman99.gsonfire.PostProcessor;
import com.github.julman99.gsonfire.TypeSelector;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import de.uulm.mi.mind.objects.Data;
import de.uulm.mi.mind.objects.messages.Error;
import de.uulm.mi.mind.objects.messages.Success;
import de.uulm.mi.mind.objects.*;
import de.uulm.mi.mind.logger.Messenger;

/**
 * @author Tamino Hartmann
 *         <p/>
 *         Our version of GSON. Implements the RuntimeTypeAdapterFactory to allow easy recursive JSON data transfer. Do not
 *         touch if you don't know what you are doing! Also note that ArrayList for example does NOT throw an error, will
 *         be converted to JSON, but does NOT contain the required $type field to be parsed again! Make sure to use
 *         objects that extend the Data interface for EVERYTHING you send.
 */
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
        GsonFireBuilder builder = new GsonFireBuilder();
        // register in switch
        builder.registerTypeSelector(Data.class, new TypeSelector<Data>() {
            @Override
            public Class<? extends Data> getClassForElement(JsonElement readElement) {
                String kind = readElement.getAsJsonObject().get("$type").getAsString();
                if (kind.equals("Area")) {
                    return Area.class;
                } else if (kind.equals("Arrival")) {
                    return Arrival.class;
                } else if (kind.equals("Departure")) {
                    return Departure.class;
                } else if (kind.equals("DataList")) {
                    return DataList.class;
                } else if (kind.equals("Error")) {
                    return Error.class;
                } else if (kind.equals("Location")) {
                    return Location.class;
                } else if (kind.equals("PublicDisplay")) {
                    return PublicDisplay.class;
                } else if (kind.equals("Success")) {
                    return Success.class;
                } else if (kind.equals("User")) {
                    return User.class;
                } else if (kind.equals("WifiMorsel")) {
                    return WifiMorsel.class;
                } else {
                    return null;
                }
            }
        });
        // add out switch
        builder.registerPostProcessor(Data.class, new PostProcessor<Data>() {
            @Override
            public void postDeserialize(Data data, JsonElement jsonElement, Gson gson) {
            }

            @Override
            public void postSerialize(JsonElement jsonElement, Data data, Gson gson) {
                // If it's just an array, return
                if (!jsonElement.isJsonObject()) {
                    return;
                }
                // if it is an object, write $type to it
                String name = data.getClass().getCanonicalName();
                name = name.substring(name.lastIndexOf('.') + 1, name.length());
                jsonElement.getAsJsonObject().add("$type", new JsonPrimitive(name));
            }
        });
        gson = builder.createGson();
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
    // TODO objects in lists are missing the $type descriptor!
    public String toJson(Data object) {
        try {
            return gson.toJson(object);
        } catch (JsonParseException e) {
            log.error(TAG, "Error parsing data to JSON! Maybe you're sending an array instead of an object?");
            log.error(TAG, "" + object.getClass().getCanonicalName());
            return null;
        }
    }

    /**
     * Converts a string to the correct Data implementing class.
     *
     * @param data The string containing the JSON data.
     * @return An object implementing Data with all the correct data. If an error happened, returns null.
     */
    public Data fromJson(String data) {
        // Returns the correct extended Data class, no need for casting.
        try {
            return gson.fromJson(data, Data.class);
        } catch (JsonParseException e) {
            log.error(TAG, "Error parsing data from JSON!");
            log.error(TAG, "" + data);
            return null;
        }
    }
}
