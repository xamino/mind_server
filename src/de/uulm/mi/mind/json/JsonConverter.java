package de.uulm.mi.mind.json;

import de.uulm.mi.mind.logger.Messenger;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

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
        // todo check that the two hashmaps are always in sync (meaning catch double adding of classes)
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
        Class objectClass = object.getClass();
        // Check if registered TYPE_KEY
        if (!typesClassString.containsKey(objectClass)) {
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
        String jsonObject = "{" + pack(TYPE_KEY, typesClassString.get(objectClass));
        // Now correctly handle each TYPE_KEY
        // todo do it all in one loop?
        // todo collection and array can contain primitive data typesClassString... :P
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
            // boolean
            else if (value instanceof Boolean) {
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
        // use any constructor to build the object, we'll fill the fields correctly manually later
        // this means we need to feed some constructor some values
        // todo implement that all classes MUST have default constructor (public or private we don't care)
        Constructor constructor = objectClass.getConstructors()[0];
        List<Object> params = new ArrayList<Object>();
        for (Class<?> pType : constructor.getParameterTypes()) {
            try {
                if (pType == boolean.class) {
                    try {
                        params.add(Boolean.class.getConstructors()[0].newInstance(new Object[]{false}));
                    } catch (InvocationTargetException e) {
                        e.printStackTrace();
                        throw new IOException(TAG + ": Failed to construct object!");
                    }
                } else {
                    params.add((pType.isPrimitive()) ? pType.newInstance() : null);
                }
            } catch (InstantiationException e) {
                e.printStackTrace();
                throw new IOException(TAG + ": Failed to construct object!");
            } catch (IllegalAccessException e) {
                e.printStackTrace();
                throw new IOException(TAG + ": Failed to construct object!");
            }
        }
        // now get an instance
        try {
            object = (E) constructor.newInstance(params.toArray());
        } catch (InstantiationException e) {
            e.printStackTrace();
            throw new IOException(TAG + ": Failed to construct object!");
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            throw new IOException(TAG + ": Failed to construct object!");
        } catch (InvocationTargetException e) {
            e.printStackTrace();
            throw new IOException(TAG + ": Failed to construct object!");
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
            Object parsedValue = typeCastParse(f, entry.getValue());
            try {
                // this is where we also parse the value to the correct type
                f.set(object, parsedValue);
            } catch (IllegalAccessException e) {
                throw new IOException(TAG + ": Failed to write fields!");
            }
        }
        // todo json arrays instantiate in java based on field type in class
        return object;
    }

    /**
     * Method that returns the parsed value as the correct type of object for the given field.
     *
     * @param field The field where to set the value.
     * @param value The string value to parse.
     * @return The parsed value.
     */
    // todo why not set the field right away?
    // todo need to do something about value.equals("null")  --> null
    private Object typeCastParse(Field field, String value) throws IOException {
        Class type = field.getType();
        if (type == boolean.class) {
            return Boolean.parseBoolean(value);
        } else if (type == String.class) {
            return value;
        } else if (type == Integer.class) {
            return Integer.parseInt(value);
        } else if (type == Byte.class) {
            return Byte.parseByte(value);
        } else if (type == Short.class) {
            return Short.parseShort(value);
        } else if (type == Long.class) {
            return Long.parseLong(value);
        } else if (type == Float.class) {
            return Float.parseFloat(value);
        } else if (type == Double.class) {
            return Double.parseDouble(value);
        } else if (type.isEnum()) {
            return Enum.valueOf(type, value);
        } else if (type == Object[].class) {
            // todo
            return null;
        } else if (type.isInstance(Collection.class)) {
            // todo
            // todo can i just give back List?
            return null;
        } else {
            // this probably means object
            return fromJson(value);
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
            // findValueEndIndex + valueStart because the method is relative, +1 to get enclosing char back too
            int valueStop = 1 + valueStart + findValueEndIndex(jsonObject.charAt(valueStart), jsonObject.substring(valueStart + 1));
            String value = jsonObject.substring(valueStart, valueStop);
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

    private int findValueEndIndex(char begin, String rest) {
        // for example if null
        if (begin != '"' && begin != '\'' && begin != '{' && begin != '[') {
            int index = rest.indexOf(',');
            // this can happen when last value in object
            if (index < 0 ) {
                return rest.length();
            }
            return rest.indexOf(',');
        }
        char end = '"';
        if (begin == '{') {
            end = '}';
        } else if (begin == '[') {
            end = ']';
        }
        int levelCounter = 0;
        int index = 1;
        for (; index < rest.length(); index++) {
            char at = rest.charAt(index);
            // break if end and no levels down
            // todo ignore if escape slash before, like \{ or \"
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
        // +1 because substring(start, end) -> end is exclusive
        return index + 1;
    }
}
