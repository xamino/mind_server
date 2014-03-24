package de.uulm.mi.mind.logger;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Stack;

/**
 * @author Tamino Hartmann
 *         <p/>
 *         This class implements a thread safe timer for multiple objects and
 *         standardized logging functions.
 */
public class Messenger {

    /**
     * Variable that stores singleton instance.
     */
    private static Messenger INSTANCE;
    /**
     * Super tag that is in all messages printed by Messenger.
     */
    private final String TAG = "Messenger";
    /**
     * The stack with which the TimerResult objects are managed.
     */
    private HashMap<Object, Stack<TimerResult>> timers;

    /**
     * Private constructor that prepares class. To get an instance of
     * Messenger, ust the getInstance Method.
     */
    private Messenger() {
        this.timers = new HashMap<Object, Stack<TimerResult>>();
    }

    /**
     * Method to return the singleton instance of Messenger.
     *
     * @return Instance of Messenger.
     */
    public synchronized static Messenger getInstance() {
        if (INSTANCE == null)
            INSTANCE = new Messenger();
        return INSTANCE;
    }

    /**
     * Method for logging a message to the console.
     *
     * @param tag     The tag used when logging after the internal tag.
     * @param content The content of the message to log.
     */
    public synchronized void log(final String tag, final String content) {
        System.out.println(TAG + "|" + tag + "::" + content);
    }

    /**
     * Method for logging errors to the console.
     *
     * @param tag     The tag used when logging after the internal tag.
     * @param content The content of the error to log.
     */
    public synchronized void error(final String tag, final String content) {
        System.err.println(TAG + "|" + tag + "::" + content);
    }

    /**
     * Method for placing a timer object on the stack with the given label.
     *
     * @param object Object reference to allow the correct stack to be used.
     * @param label  The label to remember for the timer.
     */
    public synchronized void pushTimer(Object object, final String label) {
        // Make sure that we don't keep too many objects:
        if (timers.size() > 32) {
            log("Messenger", "WARNING: Excessive amount of stacks required " +
                    "for timer function – possible memory leak!");
            Iterator<Map.Entry<Object, Stack<TimerResult>>> it = timers.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<Object, Stack<TimerResult>> pairs = it.next();
                if ((pairs.getValue()).isEmpty()) {
                    it.remove();
                }
            }
        }
        TimerResult data = new TimerResult(System.currentTimeMillis(), label);
        if (timers.containsKey(object)) {
            timers.get(object).push(data);
        } else {
            Stack<TimerResult> stack = new Stack<TimerResult>();
            stack.push(data);
            timers.put(object, stack);
        }
    }

    /**
     * Method for reading a timer object. Returns a TimerResult object which
     * contains the time spent between push and pop,
     * and the label placed originally.
     *
     * @param object Reference to object from which we will use the timer
     *               stack.
     * @return The TimerResult object containing the time difference and the
     * label. If the stack is empty (meaning more pops than pushes
     * were done), an object with time "-1" and label "EMPTY STACK" are
     * returned.
     */
    public synchronized TimerResult popTimer(Object object) {
        if (timers.containsKey(object)) {
            Stack<TimerResult> stack = timers.get(object);
            if (stack.isEmpty())
                return new TimerResult(-1, "EMPTY STACK");
            TimerResult poped = stack.pop();
            poped.time = System.currentTimeMillis() - poped.time;
            return poped;
        } else {
            return new TimerResult(-1, "OBJECT HAS NO STACK");
        }
    }
}
