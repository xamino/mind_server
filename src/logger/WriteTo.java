package logger;

/**
 * @author Tamino Hartmann
 * 
 */
public enum WriteTo {
	/**
	 * Write to System.out.
	 */
	SYSOUT,
	/**
	 * Write to the file.
	 */
	LOGFILE,
	/**
	 * Write to all available channels.
	 */
	ALL,
	/**
	 * Don't write at all. All input is discarded.
	 */
	NONE
}
