package de.saar.chorus.domgraph.codec;

import java.io.IOException;
import java.io.Writer;

public abstract class MultiOutputCodec extends OutputCodec {

    
    /**
     * Prints the beginning of a list in the concrete syntax
     * which the USR uses. This method is called after <code>print_header</code>,
     * but before any of the USRs. It is only called if we print 
     * more than one graph (e.g. in the solve command, but not the convert command).
     * 
     * @param writer the writer
     * @throws IOException if an I/O error occurred
     */
    abstract public void print_start_list(Writer writer) throws IOException;
    
    /**
     * Prints the end of a list in the concrete syntax
     * which the USR uses. This method is called before <code>print_footer</code>,
     * but after any of the USRs. It is only called if we print 
     * more than one graph (e.g. in the solve command, but not the convert command).
     * 
     * @param writer the writer
     * @throws IOException if an I/O error occurred
     */
    abstract public void print_end_list(Writer writer) throws IOException;
    
    /**
     * Prints the separator for separating different items
     * of a list in the concrete syntax which the USR uses.
     * 
     * @param writer the writer
     * @throws IOException if an I/O error occurred
     */
    abstract public void print_list_separator(Writer writer) throws IOException;
    
    
}
