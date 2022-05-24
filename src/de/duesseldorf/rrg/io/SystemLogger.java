package de.duesseldorf.rrg.io;

import java.io.PrintStream;

public class SystemLogger {

    private PrintStream outputstream;
    private boolean printOn;

    /**
     * @param outStream e.g. System.out or System.err
     */
    public SystemLogger(PrintStream outStream, boolean printOn) {
        this.outputstream = System.out;
        this.printOn = printOn;
    }

    public void info(String s) {
        if (printOn) {
            outputstream.println(s);
        }

    }

    /**
     * no need to call this in the beginning when the constructor was called
     * with printOn true
     */
    public void startLogging() {
        printOn = true;
    }

    public void stopLogging() {
        printOn = false;
    }
}
