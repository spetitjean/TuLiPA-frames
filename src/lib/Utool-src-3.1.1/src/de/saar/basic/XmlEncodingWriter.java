/*
 * @(#)XmlEncodingWriter.java created 13.02.2006
 * 
 * Copyright (c) 2006 Alexander Koller
 *  
 */

package de.saar.basic;

import java.io.Writer;

/**
 * A <code>Writer</code> which replaces XML special characters
 * (such as &amp;amp;) by the respective XML entities. 
 * 
 * @author Alexander Koller
 *
 */
public class XmlEncodingWriter extends ReplacingWriter {

    /**
     * This constructor takes the underlying <code>writer</code> 
     * to which the modified strings should be written as its
     * argument.
     * 
     * @param writer the underlying writer
     */
    public XmlEncodingWriter(Writer writer) {
        super(writer);
        
        addReplacementRule("&", "&amp;");
        addReplacementRule("<", "&lt;");
        addReplacementRule(">", "&gt;");
        addReplacementRule("'", "&apos;");
        addReplacementRule("\"", "&quot;");
    }

}
