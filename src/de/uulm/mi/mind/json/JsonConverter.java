package de.uulm.mi.mind.json;

import de.uulm.mi.mind.logger.Messenger;
import de.uulm.mi.mind.objects.DataList;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Tamino Hartmann
 *         <p/>
 */
// todo abstract away primitives and not-primitive-primitives (like Date)
public class JsonConverter<E> {

    private final String TAG = "JsonConverter";
    private final char ESCAPE = '"';
    private final String TYPE_KEY;
    private SimpleDateFormat sdf;
    private Messenger log;
    /**
     * Hashmap of the registered typesClassString that the converter will convert with $TYPE_KEY.
     */
    private HashMap<Class<? extends E>, String> typesClassString;
    private HashMap<String, Class<? extends E>> typesStringClass;

    public JsonConverter() {
        this("$type");
    }

    public JsonConverter(String typeKey) {
        log = Messenger.getInstance();
        this.TYPE_KEY = typeKey;
        typesClassString = new HashMap<>();
        typesStringClass = new HashMap<>();
        sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        log.log(TAG, "Created.");
    }

    /**
     * Method that registers an object for JSONating with $TYPE_KEY field.
     *
     * @param clazz The class which to register. Note that the name of the class is used WITHOUT the package name. This
     *              means that these should be unique!
     */
    public void registerType(Class<? extends E> clazz) {
        String name = clazz.getCanonicalName();
        name = name.substring(name.lastIndexOf('.') + 1, name.length());
        typesClassString.put(clazz, name);
        typesStringClass.put(name, clazz);
        log.log(TAG, "Registered " + TYPE_KEY + ":" + name + ".");
    }

    /**
     * @param object
     * @return
     */
    public <S extends E> String toJson(S object) throws IOException {
        if (object == null) {
            throw new IOException(TAG + ": Null object passed!");
        }
        Class objectClass = object.getClass();
        // Check if registered TYPE_KEY
        if (!typesClassString.containsKey(objectClass)) {
            throw new IOException("Unregistered TYPE_KEY! Unable to parse to JSON. Register "
                    + object.getClass().getSimpleName() + "!");
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
        StringBuffer jsonObject = new StringBuffer();
        jsonObject.append("{" + pack(TYPE_KEY, typesClassString.get(objectClass)));
        // Now correctly handle each TYPE_KEY
        // todo collection and array can contain primitive data
        for (Map.Entry<Field, Object> entry : fieldValueList.entrySet()) {
            // add comma
            jsonObject.append(",");
            Field field = entry.getKey();
            String fieldName = fieldName(field);
            Object value = entry.getValue();
            // null value
            if (value == null) {
                jsonObject.append(ESCAPE + fieldName + ESCAPE + ":null");
            }
            // collections && arrays
            else if (value instanceof Collection || value instanceof Object[]) {
                // start array
                jsonObject.append(ESCAPE + fieldName + ESCAPE + ":[");
                // collection
                Object[] array;
                if (value instanceof Collection) {
                    array = ((Collection) value).toArray();
                } else {
                    array = (Object[]) value;
                }
                // recursively solve
                for (Object collectionObject : array) {
                    jsonObject.append(toJson(((E) collectionObject)) + ",");
                }
                // remove last comma if placed
                if (jsonObject.charAt(jsonObject.length() - 1) == ',') {
                    jsonObject.deleteCharAt(jsonObject.length() - 1);
                }
                jsonObject.append(']');
            }
            // string, numbers, enums
            else if (value instanceof Enum || value instanceof String || value instanceof Byte || value instanceof Short
                    || value instanceof Integer || value instanceof Long || value instanceof Float
                    || value instanceof Double) {
                jsonObject.append(pack(fieldName, value.toString()));
            }
            // boolean
            else if (value instanceof Boolean) {
                jsonObject.append(pack(fieldName, value.toString()));
            }
            // date
            else if (value instanceof Date) {
                jsonObject.append(pack(fieldName, sdf.format(value)));
            }
            // probably object (if not registered it will throw an IOException on recursion)
            else {
                jsonObject.append(ESCAPE + fieldName + ESCAPE + ":" + toJson(((E) value)));
            }
        }
        // finish object
        jsonObject.append('}');
        return jsonObject.toString();
    }

    public E fromJson(String json) {
        try {
            return writeObject(json);
        } catch (IOException e) {
            e.printStackTrace();
            log.error(TAG, "Failed!");
            return null;
        }
    }

    private E writeObject(String jsonObject) throws IOException {
        // sanity: string starts and ends with {}
        if (!jsonObject.startsWith("{") || !jsonObject.endsWith("}")) {
            throw new IOException("String is not bracketed by {}!");
        }
        // get values
        HashMap<String, String> simplePairs = jsonValues(jsonObject);
        // check that we have our class type information
        String typeValue = simplePairs.get(TYPE_KEY);
        if (typeValue == null) {
            throw new IOException("Required field " + TYPE_KEY + " not found!");
        }
        // check that registered
        Class objectClass = typesStringClass.get(typeValue);
        if (objectClass == null) {
            throw new IOException(TAG + ": Unregistered type found!");
        }
        // remove TYPE_KEY
        simplePairs.remove(TYPE_KEY);
        // create java object
        E object;
        try {
            Constructor constructor = objectClass.getDeclaredConstructor(new Class[]{});
            constructor.setAccessible(true);
            object = (E) constructor.newInstance(new Class[]{});
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            e.printStackTrace();
            throw new IOException(TAG + ": Objects must have default constructor! May be private though.");
        }
        // apply all fields, leaving those we have no value for at their default value
        for (Map.Entry<String, String> entry : simplePairs.entrySet()) {
            Field f = null;
            try {
                f = objectClass.getDeclaredField(entry.getKey());
            } catch (NoSuchFieldException e) {
                // Silently ignore but warn
                log.error(TAG, "Field " + entry.getKey() + " does not exist for object " + typeValue + "!");
                continue;
            }
            f.setAccessible(true);
            Object parsedValue = typeCastParse(f.getType(), entry.getValue());
            try {
                // this is where we also parse the value to the correct type
                f.set(object, parsedValue);
            } catch (IllegalAccessException e) {
                throw new IOException(TAG + ": Failed to write fields!");
            }
        }
        return object;
    }

    /**
     * Method that returns the parsed value as the correct type of object for the given field.
     *
     * @param clazz The class to which to try and cast it.
     * @param value The string value to parse.
     * @return The parsed value.
     */
    // todo catch arrays and collections of primitives
    private Object typeCastParse(Class clazz, String value) throws IOException {
        if (clazz == boolean.class) {
            if (value.equals("null")) {
                return false;
            }
            return Boolean.parseBoolean(value);
        } else if (clazz == int.class) {
            if (value.equals("null")) {
                return 0;
            }
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
                throw new IOException(TAG + ": Not an integer: " + value);
            }
        } else if (clazz == byte.class) {
            if (value.equals("null")) {
                return 0;
            }
            return Byte.parseByte(value);
        } else if (clazz == short.class) {
            if (value.equals("null")) {
                return 0;
            }
            return Short.parseShort(value);
        } else if (clazz == long.class) {
            if (value.equals("null")) {
                return 0;
            }
            return Long.parseLong(value);
        } else if (clazz == float.class) {
            if (value.equals("null")) {
                return 0;
            }
            return Float.parseFloat(value);
        } else if (clazz == double.class) {
            if (value.equals("null")) {
                return 0;
            }
            return Double.parseDouble(value);
        } else if (clazz == String.class) {
            if (value.equals("null")) {
                return null;
            }
            return value;
        }
        // null is caught exactly here for a reason! --> int = null is impossible... :P
        else if (value.equals("null")) {
            return null;
        } else if (clazz.isEnum()) {
            return Enum.valueOf(clazz, value);
        } else if (clazz == Date.class) {
            // note that Date is considered a primitive type here... really. :P
            try {
                return sdf.parse(value);
            } catch (ParseException e) {
                e.printStackTrace();
                throw new IOException(TAG + ": JsonConverter failed to convert java.util.Date back! Format required " +
                        "is yyyy-MM-dd HH:mm:ss");
            }
        } else if (clazz == Object[].class || clazz.isInstance(Collection.class) || value.startsWith("[")) {
            DataList objects = new DataList();
            // remove []
            value = value.substring(1, value.length() - 1);
            // must split value into objects
            while (value.contains(",")) {
                int end = findEndBracket(value) + 1;
                String nextObject = value.substring(0, end);
                objects.add(writeObject(nextObject));
                // remove finished object
                // some vodoo required to correctly move the string over
                if (value.charAt(end - 1) == ',') {
                    value = value.substring(end);
                } else {
                    if (end + 1 >= value.length()) {
                        value = "";
                    } else {
                        value = value.substring(end + 1);
                    }
                }
            }
            if (clazz == Object[].class) {
                return objects.toArray();
            } else {
                return objects;
            }
        } else {
            // this probably means object
            return writeObject(value);
        }
    }

    private String pack(String key, String value) {
        return ESCAPE + key + ESCAPE + ":" + ESCAPE + value + ESCAPE;
    }

    private String fieldName(Field field) {
        return field.toString().substring(field.toString().lastIndexOf(".") + 1);
    }

    private HashMap<String, String> jsonValues(String jsonObject) throws IOException {
        HashMap<String, String> tree = new HashMap<>();
        // remove object brackets at start and end
        jsonObject = jsonObject.substring(1, jsonObject.length() - 1);
        while (!jsonObject.isEmpty()) {
            // reading the key is easy: it is always bracketed by " " and goes up to the ':'
            int splitIndex = jsonObject.indexOf(":");
            int keyStart = jsonObject.indexOf(ESCAPE) + 1;
            int keyStop = splitIndex - 1;
            if (keyStart >= keyStop) {
                throw new IOException("JsonConverter: messy JSON!");
            }
            String key = jsonObject.substring(keyStart, keyStop);
            // now the tricky part: getting the complete value, independent of type and complexity
            int valueStart = splitIndex + 1;
            // + valueStart because the method is relative
            int valueStop = valueStart + findEndBracket(jsonObject.substring(valueStart));
            // +1 because of substring exclusion
            String value = jsonObject.substring(valueStart, valueStop + 1);
            // must unpack value if only simple value (" ")
            if (value.startsWith("\"")) {
                value = value.substring(1, value.length() - 1);
            }
            // shift jsonObject to remove read json
            if (valueStop + 1 > jsonObject.length()) {
                // need to catch greater value if we're done as otherwise it is out of bounds
                jsonObject = "";
            } else {
                jsonObject = jsonObject.substring(valueStop + 1);
            }
            // System.out.println(key + ":" + value);
            // add key:value to hashmap
            tree.put(key, value);
        }
        return tree;
    }

    /**
     * Helper function that will try to find the scope of the next value in the json-string. Detection is based upon
     * what the first char of the string is. Function is capable of keeping track of recursive objects and will try to
     * find the correct end mark. Legal marks are (with their end mark): "–", {–}, [–]. If none of these three, it will
     * return the the index before the next comma (for example when the string looks like thus: "null, ...").
     *
     * @param string The string upon which to work
     * @return The index.
     */
    private int findEndBracket(String string) {
        // get starting char
        char begin = string.charAt(0);
        // set ending char we're searching for
        char end;
        switch (begin) {
            case ESCAPE:
                end = ESCAPE;
                break;
            case '{':
                end = '}';
                break;
            case '[':
                end = ']';
                break;
            default:
                // can happen when :null, so return value before comma
                int comma = string.indexOf(',');
                if (comma < 0) {
                    return string.length() - 1;
                } else {
                    return string.indexOf(',') - 1;
                }
        }
        // now walk through while counting the values
        int index = 1;
        int level = 0;
        for (; index < string.length(); index++) {
            char check = string.charAt(index);
            if (level == 0 && check == end) {
                break;
            } else if (check == begin) {
                level++;
            } else if (check == end) {
                level--;
            }
        }
        // System.out.println(string.substring(0, index + 1));
        return index;
    }
}
