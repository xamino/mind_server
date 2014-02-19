package logger;

/**
 * @author Tamino Hartmann
 */

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;

import config.Configurator;
import config.IllegalTypeException;
import config.UnknownOptionException;

/**
 * This class implements the Logger for the complete program. All information
 * that could be useful for debugging or is required to check the correct
 * function of the program should be posted here. The standard setting is that
 * the log prints to the console AND writes to the log file. This class
 * implements the configurator.
 */
public class Log {

	/**
	 * Variable for storing the instance of Log. Access with the getInstance()
	 * method.
	 */
	private static Log instance;

	/**
	 * Variable for the single instance referencing the BufferedWriter. This is
	 * required so that deactivating the fileStream temporally will not reset
	 * the complete log file.
	 */
	private BufferedWriter fileStreamSaved;
	/**
	 * Variable for storing the reference to the BufferedWriter of the file. Can
	 * be null.
	 */
	private BufferedWriter fileStream;
	/**
	 * Variable for storing the reference to the PrintStream of System.out. Can
	 * be null.
	 */
	private PrintStream sysStream;
	/**
	 * Variable for storing time stamp of program start.
	 */
	private long timer;

	/**
	 * Instance of configurator.
	 */
	private Configurator config;
	/**
	 * Path where to place log file.
	 */
	private String logFilePath;

	/**
	 * Method for getting a valid instance of Log. The returned instance can
	 * then be used to write all the data.
	 * 
	 * @return The instance to work with.
	 */
	synchronized public static Log getInstance() {
		if (Log.instance == null)
			Log.instance = new Log();
		return Log.instance;
	}

	/**
	 * Private constructor to ensure singleton instance. Use getInstance() to
	 * get a working reference to the logger.
	 */
	private Log() {
		// Set initial start time:
		startTimer();
		// Get Configurator:
		config = Configurator.getInstance();
		// Set default writeTo:
		WriteTo set = WriteTo.ALL;
		// Set default logfile path:
		logFilePath = System.getProperty("user.home")
				+ System.getProperty("file.separator") + "log";
		// Get config data:
		try {
			// Read path:
			logFilePath = config.getPath("log");
			// Read where all to print output:
			switch (config.getInt("logWriteTo")) {
			case 0:
				set = WriteTo.NONE;
				break;
			case 1:
				set = WriteTo.SYSOUT;
				break;
			case 2:
				set = WriteTo.LOGFILE;
				break;
			case 3:
				set = WriteTo.ALL;
				break;
			default:
				break;
			}
		} catch (IllegalTypeException e) {

		} catch (UnknownOptionException e) {

		}
		// Initialize fileStreamSaved. Must happen AFTER conf has been read!
		fileStreamSaved = createFile();
		// Set default output stream. Must happen AFTER fileStreamSaved is
		// initialized!
		setOutputStream(set, true);
	}

	/**
	 * Standard method to post something in the log. The log accepts an object reference
	 * (the caller) and a message to show. Together with a timestamp from the start of the
	 * logger, these are then printed to the selected output streams.
	 * 
	 * @param caller
	 *            The name of the object that wants to log information.
	 * @param message
	 *            The message to show in the log.
	 */
	synchronized public void write(String caller, String message) {
		String time = getTimePassed() + "";
		if (getTimePassed() > 1000) {
			time = time.substring(0, time.length() - 3) + "."
					+ time.substring(time.length() - 3, time.length()) + "s";
		} else
			time += "ms";
		// Create log string:
		String output = "[" + time + ":" + caller + "] " + message;

		// Write to fileStream:
		if (fileStream != null) {
			try {
				fileStream.write(output);
				fileStream.newLine();
				fileStream.flush();
			} catch (IOException e) {
				setOutputStream(WriteTo.SYSOUT, true);
				write("LOG", "Error creating file. Setting stream to SYSOUT.");
			}
		}
		if (sysStream != null) {
			sysStream.println(output);
		}
	}

	/**
	 * This method promises to try to post the message only in the designated
	 * stream.
	 * 
	 * @param caller
	 *            The name of the object that wants to log information.
	 * @param message
	 *            The message to show in the log.
	 * @param stream
	 *            The stream to try to write to exclusively.
	 */
	synchronized public void write(String caller, String message, WriteTo stream) {
		String time = getTimePassed() + "";
		if (getTimePassed() > 1000) {
			time = time.substring(0, time.length() - 3) + "."
					+ time.substring(time.length() - 3, time.length()) + "s";
		} else
			time += "ms";
		// Create log string:
		String output = "[" + time + ":" + caller + "] " + message;
		switch (stream) {
		case ALL:
			try {
				fileStreamSaved.write(output);
				fileStreamSaved.newLine();
				fileStreamSaved.flush();
			} catch (IOException e) {
				// Do nothing if fails.
			}
			System.out.println(output);
			break;
		case LOGFILE:
			try {
				fileStreamSaved.write(output);
				fileStreamSaved.newLine();
				fileStreamSaved.flush();
			} catch (IOException e) {
				// Do nothing if fails.
			}
			break;
		case SYSOUT:
			System.out.println(output);
			break;
		case NONE:
			break;
		default:
			break;
		}
	}

	/**
	 * Method for changing where the stream goes. Should ideally only be called seldomly,
	 * as messages can be lost via switching to other streams.
	 * 
	 * @param stream
	 *            The variable that sets where the logs go.
	 * @param flag
	 *            When <code>true</code> activate writing to that stream, with
	 *            <code>false</code> deactivate. Has no effect with ALL or NONE.
	 */
	synchronized public void setOutputStream(WriteTo stream, boolean flag) {
		switch (stream) {
		case LOGFILE:
			fileStream = flag ? fileStreamSaved : null;
			break;
		case SYSOUT:
			sysStream = flag ? System.out : null;
			break;
		case ALL:
			sysStream = System.out;
			fileStream = fileStreamSaved;
			break;
		case NONE:
			sysStream = null;
			fileStream = null;
			break;
		default:
			break;
		}
	}

	/**
	 * Method that handles the correct setting of the BufferedWrite. Also
	 * handles the creation of the log file and handles some of the possible
	 * errors.
	 * 
	 * @return The BufferedWriter to write the log with.
	 */
	private BufferedWriter createFile() {
		// If a fileStream already exists, a file has already been created in
		// this session, so we will use it instead (basically appending
		// further). Otherwise, this is the first call of the method and we need
		// to create a new, empty log file – this might well replace an old one!
		if (fileStream != null)
			return fileStream;

		// Creates the file in the home directory in which the log will be
		// written.
		File logFile = new File(logFilePath);
		// If the file already exists, we need to delete it first:
		if (logFile.exists())
			logFile.delete();
		// Now create a new file and return the BufferedWriter:
		try {
			logFile.createNewFile();
			BufferedWriter buf = new BufferedWriter(new FileWriter(logFile));
			return buf;
		} catch (IOException e) {
			// On error, set to System.out and return null:
			setOutputStream(WriteTo.SYSOUT, true);
			write("LOG", "Error creating file. Setting stream to SYSOUT.");
			return null;
		}
	}

	/**
	 * Method for restarting the timer. Should not be called from outside – log
	 * does NOT provide custom timer implementations!
	 */
	private void startTimer() {
		timer = System.currentTimeMillis();
	}

	/**
	 * Method for reading the passed time since the start of the program.
	 * 
	 * @return Time passed in milliseconds.
	 */
	public long getTimePassed() {
		return System.currentTimeMillis() - timer;
	}
}
