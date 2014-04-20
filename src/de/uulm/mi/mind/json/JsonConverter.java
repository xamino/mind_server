package de.uulm.mi.mind.json;

import de.uulm.mi.mind.logger.Messenger;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Tamino Hartmann
 *         <p/>
 */
public class JsonConverter<E> {

    private final String TAG = "JsonConverter";
    private final String ESCAPE = "\"";
    private final String TYPE_KEY;
    private Messenger log;
    /**
     * Hashmap of the registered types that the converter will convert with $TYPE_KEY.
     */
    private HashMap<Class<? extends E>, String> types;

    public JsonConverter() {
        this("$type");
    }

    public JsonConverter(String typeKey) {
        log = Messenger.getInstance();
        this.TYPE_KEY = typeKey;
        types = new HashMap<>();
        log.log(TAG, "Created.");
    }

    /**
     * Method that registers an object for JSONating with $TYPE_KEY field.
     *
     * @param clazz The class which to register. Note that the name of the class is used WITHOUT the package name. This
     *              means that these should be unique!
     */
    public void registerType(Class<? extends E> clazz) {
        // todo check that classes do not clash with TYPE_KEY!!!
        String name = clazz.getCanonicalName();
        name = name.substring(name.lastIndexOf('.') + 1, name.length());
        types.put(clazz, name);
        log.log(TAG, "Registered " + TYPE_KEY + ":" + name + ".");
    }

    /**
     * @param object
     * @return
     */
    public <S extends E> String toJson(S object) throws IOException {
        Class objectClass = object.getClass();
        // Check if registered TYPE_KEY
        if (!types.containsKey(objectClass)) {
            throw new IOException("Unregistered TYPE_KEY! Unable to parse to JSON.");
        }
        // Get all fields and values recursively
        HashMap<Field, Object> fieldValueList = new HashMap<>();
        for (Class c = objectClass; c != null; c = c.getSuperclass()) {
            for (Field field : c.getDeclaredFields()) {
                try {
                    field.setAccessible(true);  // otherwise private won't work
                    fieldValueList.put(field, field.get(object));
                } catch (IllegalAccessException e) {
                    throw new IOException("IllegalAccessException inside " + TAG + "!");
                }
            }
        }
        // JSON object start
        // todo use stringbuffer to make faster?
        String jsonObject = "{" + pack(TYPE_KEY, types.get(objectClass));
        // Now correctly handle each TYPE_KEY
        // todo do it all in one loop?
        // todo collection and array can contain primitive data types... :P
        for (Map.Entry<Field, Object> entry : fieldValueList.entrySet()) {
            // add comma
            jsonObject += ",";
            Field field = entry.getKey();
            String fieldName = fieldName(field);
            Object value = entry.getValue();
            // collections
            if (value instanceof Collection) {
                // start array
                jsonObject += ESCAPE + fieldName + ESCAPE + ":[";
                // collection
                Collection collection = ((Collection) value);
                // recursively solve
                for (Object collectionObject : collection) {
                    jsonObject += toJson(((E) collectionObject)) + ",";
                }
                // remove last comma if placed
                if (jsonObject.endsWith(",")) {
                    jsonObject = jsonObject.substring(0, jsonObject.length() - 1);
                }
                jsonObject += "]";
            }
            // arrays
            else if (value instanceof Object[]) {
                // start array
                jsonObject += ESCAPE + fieldName + ESCAPE + ":[";
                // collection
                Object[] array = ((Object[]) value);
                // recursively solve
                for (Object collectionObject : array) {
                    jsonObject += toJson(((E) collectionObject)) + ",";
                }
                // remove last comma if placed
                if (jsonObject.endsWith(",")) {
                    jsonObject = jsonObject.substring(0, jsonObject.length() - 1);
                }
                jsonObject += "]";
            }
            // string
            else if (value instanceof String) {
                jsonObject += pack(fieldName, ((String) value));
                // todo how do i get subobjects?
            }
            // numbers
            else if (value instanceof Byte || value instanceof Short || value instanceof Integer ||
                    value instanceof Long || value instanceof Float || value instanceof Double) {
                jsonObject += pack(fieldName, value.toString());
            }
            // enums
            else if (value instanceof Enum) {
                jsonObject += pack(fieldName, value.toString());
            }
            // probably object (if not registered it will throw an IOException on recursion)
            else {
                jsonObject += ESCAPE + fieldName + ESCAPE + ":" + toJson(((E) value));
            }

        }
        // finish object
        jsonObject += "}";
        return jsonObject;
    }

    public E fromJson(String jsonObject) throws IOException {
        // sanity: string starts and ends with {}
        if (!jsonObject.startsWith("{") || !jsonObject.endsWith("}")) {
            throw new IOException("String is not bracketed by {}!");
        }
        // get values
        HashMap<String, String> simplePairs = jsonValues(jsonObject);
        // check that we have our class type information
        if (!jsonObject.contains(TYPE_KEY)) {
            throw new IOException("Required field " + TYPE_KEY + " not found!");
        }
        // check that registered
        // create java object
        // todo json arrays instantiate in java based on field type in class
        return null;
    }

    private String pack(String key, String value) {
        return ESCAPE + key + ESCAPE + ":" + ESCAPE + value + ESCAPE;
    }

    private String fieldName(Field field) {
        return field.toString().substring(field.toString().lastIndexOf(".") + 1);
    }

    private HashMap<String, String> jsonValues(String jsonObject) {
        HashMap<String, String> tree = new HashMap<>();
        // remove object brackets at start and end
        jsonObject = jsonObject.substring(1, jsonObject.length() - 1);
        while (!jsonObject.isEmpty()) {
            int splitIndex = jsonObject.indexOf(":");
            int keyStart = jsonObject.indexOf(ESCAPE) + 1;
            int keyStop = splitIndex - 1;
            String key = jsonObject.substring(keyStart, keyStop);
            int valueStart = splitIndex;
            int valueStop = findValueEndIndex(jsonObject.substring(valueStart)) + valueStart;
            String value = jsonObject.substring(valueStart, valueStop);
            jsonObject = jsonObject.substring(valueStop + 1);
            System.out.println(key + ":" + value);
        }
        return tree;
    }

    private int findValueEndIndex(String rest) {
        char begin = rest.charAt(0);
        // for example if null
        if (begin != '"' && begin != '\'' && begin != '{' && begin != '[') {
            return rest.indexOf(',') - 1;
        }
        char end = '"';
        if (begin == '{') {
            end = '}';
        } else if (begin == '[') {
            end = ']';
        } else {
            System.out.println("Nope");
        }
        int levelCounter = 0;
        int index = 1;
        for (; index < rest.length(); index++) {
            char at = rest.charAt(index);
            // break if end and no levels down
            if (at == end && levelCounter == 0) {
                break;
            }
            if (at == end && levelCounter > 0) {
                levelCounter--;
            }
            if (at == begin) {
                levelCounter++;
            }
        }
        return index;
    }
}
