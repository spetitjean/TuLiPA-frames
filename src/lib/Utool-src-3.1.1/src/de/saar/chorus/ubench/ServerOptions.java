package de.saar.chorus.ubench;

import java.io.PrintWriter;

/**
 * Class storing the server options set
 * in Ubench.
 * 
 * @author Michaela Regneri
 *
 */
public class ServerOptions {
	private static boolean logging = true;
	private static boolean warmup = false;
	private static int port = 2802;
	private static PrintWriter logwriter = new PrintWriter(System.err, true);
	
	/**
	 * @return Returns the logging.
	 */
	public static boolean isLogging() {
		return logging;
	}
	/**
	 * @param logging The logging to set.
	 */
	public static void setLogging(boolean logging) {
		ServerOptions.logging = logging;
	}
	/**
	 * @return Returns the logwriter.
	 */
	public static PrintWriter getLogwriter() {
		return logwriter;
	}
	/**
	 * @param logwriter The logwriter to set.
	 */
	public static void setLogwriter(PrintWriter logwriter) {
		ServerOptions.logwriter = logwriter;
	}
	/**
	 * @return Returns the port.
	 */
	public static int getPort() {
		return port;
	}
	/**
	 * @param port The port to set.
	 */
	public static void setPort(int port) {
		ServerOptions.port = port;
	}
	/**
	 * @return Returns the warmup.
	 */
	public static boolean isWarmup() {
		return warmup;
	}
	/**
	 * @param warmup The warmup to set.
	 */
	public static void setWarmup(boolean warmup) {
		ServerOptions.warmup = warmup;
	}
	
	

}
