/*
 * @(#)OzTermOutputCodec.java created 25.01.2006
 * 
 * Copyright (c) 2006 Alexander Koller
 *  
 */

package de.saar.chorus.domgraph.codec.term;

import de.saar.chorus.domgraph.codec.CodecMetadata;


/**
 * An output codec for terms in Oz syntax. See 
 * {@link de.saar.chorus.domgraph.codec.term.TermOutputCodec} for more details.<p>
 * 
 * An example output looks as follows:<br/>
 * {@code f(a g(b))}
 * 
 * @author Alexander Koller
 *
 */
@CodecMetadata(name="term-oz", extension=".t.oz")
public class OzTermOutputCodec extends TermOutputCodec {
    public OzTermOutputCodec() {
        super(" ");
    }
}
