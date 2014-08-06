package de.uulm.mi.mind.json;

import de.uulm.mi.mind.logger.Messenger;
import de.uulm.mi.mind.objects.DataList;
import de.uulm.mi.mind.objects.Interfaces.Sendable;
import org.apache.commons.lang3.StringEscapeUtils;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author Tamino Hartmann
 *         <p/>
 *         Class for mapping Java objects to JSON objects and vice versa. Custom code because we require a static $type
 *         field for identifiying the Java object type. A base Java class must be registered. Any specific object that
 *         may be sent must also be registered after constructing an instance of this JsonConverter. Ideally write a
 *         wrapper for this. Fields can be registered to be ignored, although the filter is applied to ALL objects.
 */
public class JsonConverter<E> {
    /**
     * Logging tag.
     */
    private final String TAG = "JsonConverter";
    /**
     * The quotation mark to use as escape value for values.
     */
    private final char ESCAPE = '"';
    /**
     * The basic type key written and read from JSON. Default is $type.
     */
    private final String TYPE_KEY;
    /**
     * For converting dates to and from JSON.
     */
    private SimpleDateFormat sdf;
    /**
     * Instance of log.
     */
    private Messenger log;
    /**
     * Hashmap of the registered typesClassString that the converter will convert with $TYPE_KEY.
     */
    private HashMap<Class<? extends E>, String> typesClassString;
    /**
     * Hashmap of the registered typesClassString that the converter will convert with $TYPE_KEY. Mirrored for easy
     * access. Both are kept in sync.
     */
    private HashMap<String, Class<? extends E>> typesStringClass;
    /**
     * A set of fields that is ignored in both directions. Applied to all objects!
     */
    private Set<String> ignoreFields;

    /**
     * JsonConverter default constructor. Type field is set to $type.
     */
    public JsonConverter() {
        this("$type");
    }

    /**
     * JsonConverter constructor where the type key value can be manually set. Note that it must be unique to work
     * reliably!
     *
     * @param typeKey The type key to set.
     */
    public JsonConverter(String typeKey) {
        log = Messenger.getInstance();
        this.TYPE_KEY = typeKey;
        typesClassString = new HashMap<>();
        typesStringClass = new HashMap<>();
        ignoreFields = new HashSet<>();
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
     * Method for telling the converter to ignore special fields when converting to JSON.
     *
     * @param fieldName The exact name of the field.
     */
    public void ignoreField(String fieldName) {
        ignoreFields.add(fieldName);
    }

    /**
     * Method that, given a registered object, creates the corresponding JSON string.
     *
     * @param object The object to convert.
     * @param <S>    The registered type.
     * @return The String containing the object, or null if failed.
     */
    public <S extends E> String toJson(S object) {
        try {
            return objectJson(object);
        } catch (IOException e) {
            // e.printStackTrace();
            log.error(TAG, "Failed to create JSON!");
            return null;
        }
    }

    /**
     * Abstracted away method for creating JSON objects from Java objects.
     *
     * @param object The object to convert.
     * @param <S>    The base class type.
     * @return The string of the object.
     * @throws IOException If something went wrong.
     */
    private <S extends E> String objectJson(S object) throws IOException {
        if (object == null) {
            throw new IOException(TAG + ": Null object passed!");
        }
        Class objectClass = object.getClass();
        // Check if registered TYPE_KEY
        if (!typesClassString.containsKey(objectClass)) {
            log.error(TAG, "Unregistered object! Register " + objectClass.getSimpleName() + "!");
            throw new IOException("Unregistered TYPE_KEY! Unable to parse to JSON. Register "
                    + objectClass.getCanonicalName() + "!");
        }
        // Get all fields and values recursively
        HashMap<Field, Object> fieldValueList = new HashMap<>();
        for (Class c = objectClass; c != null; c = c.getSuperclass()) {
            for (Field field : c.getDeclaredFields()) {
                // ignore fields listed
                if (ignoreFields.contains(field.getName())) {
                    continue;
                }
                // ignore final fields
                if (Modifier.isFinal(field.getModifiers())) {
                    log.error(TAG, "WARNING: Field " + field.getName() + " is final, skipping!");
                    continue;
                }
                // otherwise try to make the field accessible and set it
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
        jsonObject.append("{").append(pack(TYPE_KEY, typesClassString.get(objectClass)));
        // Now correctly handle each TYPE_KEY
        for (Map.Entry<Field, Object> entry : fieldValueList.entrySet()) {
            // add comma
            jsonObject.append(",");
            Field field = entry.getKey();
            String fieldName = field.getName();
            Object value = entry.getValue();
            // null value
            if (value == null) {
                jsonObject.append(ESCAPE).append(fieldName).append(ESCAPE).append(":null");
            }
            // collections && arrays
            else if (value instanceof Collection || value instanceof Object[]) {
                // start array
                jsonObject.append(ESCAPE).append(fieldName).append(ESCAPE).append(":[");
                // collection
                Object[] array;
                if (value instanceof Collection) {
                    array = ((Collection) value).toArray();
                } else {
                    array = (Object[]) value;
                }
                // recursively solve
                for (Object collectionObject : array) {
                    Class clazz = collectionObject.getClass();
                    if (clazz.isPrimitive() || String.class.isAssignableFrom(clazz)) {
                        // primitives are written as-is
                        jsonObject.append(ESCAPE).append(collectionObject).append(ESCAPE).append(",");
                    } else {
                        // objects are recursively resolved
                        jsonObject.append(objectJson(((E) collectionObject))).append(",");
                    }
                }
                // remove last comma if placed
                if (jsonObject.charAt(jsonObject.length() - 1) == ',') {
                    jsonObject.deleteCharAt(jsonObject.length() - 1);
                }
                jsonObject.append(']');
            }
            // strings must be escaped
            else if (value instanceof String) {
                jsonObject.append(pack(fieldName, StringEscapeUtils.escapeJson(value.toString())));
            }
            // string, numbers, enums
            else if (value instanceof Enum || value instanceof Byte || value instanceof Short || value instanceof Integer
                    || value instanceof Long || value instanceof Float || value instanceof Double) {
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
                jsonObject.append(ESCAPE).append(fieldName).append(ESCAPE).append(":").append(objectJson(((E) value)));
            }
        }
        // finish object
        jsonObject.append('}');
        return jsonObject.toString();
    }

    /**
     * Given a JSON formatted data string returns the object if registered represented by the data.
     *
     * @param json The string.
     * @return The object with all fields correctly set.
     */
    public E fromJson(String json) {
        try {
            return writeObject(json);
        } catch (IOException e) {
            e.printStackTrace();
            log.error(TAG, "Failed to create Java!");
            return null;
        }
    }

    /**
     * Abstracted method for reading a Java object from a JSON string.
     *
     * @param jsonObject The JSON formatted string.
     * @return The Java object identified and filled.
     * @throws IOException If something went wrong.
     */
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
            throw new IOException(TAG + ": Unregistered type found: " + typeValue + "!");
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
            log.error(TAG, "Default constructor missing for " + objectClass.toString() + "!");
            throw new IOException(TAG + ": Objects must have default constructor! May be private though.");
        }
        // apply all fields, leaving those we have no value for at their default value
        for (Map.Entry<String, String> entry : simplePairs.entrySet()) {
            Field f = null;
            try {
                f = objectClass.getDeclaredField(entry.getKey());
            } catch (NoSuchFieldException e) {
                // Silently ignore but warn
                log.error(TAG, "WARNING: Field " + entry.getKey() + " does not exist for object " + typeValue + "!");
                continue;
            }
            // catch final fields and static finals
            if (Modifier.isFinal(f.getModifiers())) {
                log.error(TAG, "WARNING: Field " + entry.getKey() + " is final, ignoring!");
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
    private Object typeCastParse(Class clazz, String value) throws IOException {
        if (clazz == boolean.class) {
            return !value.equals("null") && Boolean.parseBoolean(value);
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
            // deescape
            return StringEscapeUtils.unescapeJson(value);
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
                // e.printStackTrace();
                throw new IOException(TAG + ": JsonConverter failed to convert java.util.Date back! Format required " +
                        "is yyyy-MM-dd HH:mm:ss");
            }
        } else if (value.startsWith("[")) {
            ArrayList objects = new ArrayList();
            // remove []
            value = value.substring(1, value.length() - 1);
            // must split value into objects
            while (!value.isEmpty()) {
                int end = findEndBracket(value) + 1;
                String nextObject = value.substring(0, end);
                if (clazz.isAssignableFrom(ArrayList.class)) {
                    // remove "" because always primitive (no further parsing)
                    nextObject = nextObject.substring(1, nextObject.length() - 1);
                    // primitives (will always be string)
                    objects.add(nextObject);
                } else if (clazz == Object[].class || clazz.isAssignableFrom(DataList.class)) { //TODO || clazz == Data.class ?
                    // objects
                    objects.add(writeObject(nextObject));
                } else {
                    log.error(TAG, "Failed in creating list – field may not expect list!");
                    return null;
                }
                // remove finished object
                // some vodoo required to correctly move the string over
                if (value.length() <= end + 1) {
                    value = "";
                } else {
                    value = value.substring(end + 1);
                }
            }
            if (clazz == Object[].class) {
                return objects.toArray();
            } else if (clazz == DataList.class || clazz == Sendable.class) {
                DataList dataList = new DataList(objects);
                return dataList;
            } else {
                return objects;
            }
        } else {
            // this probably means object
            return writeObject(value);
        }
    }

    /**
     * Method for packing a value into ESCAPE.
     *
     * @param key   The key value to set.
     * @param value The value to write to the key.
     * @return The combined string.
     */
    private String pack(String key, String value) {
        return ESCAPE + key + ESCAPE + ":" + ESCAPE + value + ESCAPE;
    }

    /**
     * Helper function for getting a hash map of key-value pairs of an object. Note that sub-objects are not resolved.
     *
     * @param jsonObject The JSON formatted string.
     * @return The hash map containing all the key value pairs.
     * @throws IOException If something went wrong.
     */
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
            // check against ignoreField
            if (ignoreFields.contains(key)) {
                continue;
            }
            // add key:value to hashmap
            tree.put(key, value);
        }
        return tree;
    }

    /**
     * Helper function that will try to find the scope of the next value in the json-string. Detection is based upon
     * what the first char of the string is. Function is capable of keeping track of recursive objects and will try to
     * find the correct end mark. Legal marks are (with their end mark): "–", {–}, [–]. If none of these three, it will
     * return the the index before the next comma (for example when the string looks like thus: "null, ..."). Escaped
     * values in strings are ignored.
     *
     * @param string The string upon which to work
     * @return The index.
     */
    private int findEndBracket(String string) {
        // catch if the string is only one char long
        if (string.length() < 2) {
            // this can happen if a single number is sent without being bracketed by "". It'll work, but we'll warn
            // to be sure.
            log.error(TAG, "Warning: findEndBracket returned early due to small string! <" + string + ">");
            return string.length() - 1;
        }
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
            char charCheck = string.charAt(index);
            // get char before – this works always because index starts at 1
            char charBefore = string.charAt(index - 1);
            // check if char before current one is escape symbol – if yes, continue
            if (charBefore == '\\') {
                continue;
            }
            // else count levels
            if (level == 0 && charCheck == end) {
                break;
            } else if (charCheck == begin) {
                level++;
            } else if (charCheck == end) {
                level--;
            }
        }
        // System.out.println(string.substring(0, index + 1));
        return index;
    }
} // end JsonConverter
