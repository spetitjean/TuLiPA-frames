/*
 *  File FiniteStateReader.java
 *
 *  Authors:
 *     Wolfgang Maier  <wo.maier@uni-tuebingen.de>
 *
 *  Copyright:
 *     Wolfgang Maier, 2007
 *
 *  Last modified:
 *     Di 16. Okt 09:40:59 CEST 2007
 *
 *  This file is part of the TuLiPA system
 *     http://www.sfb441.uni-tuebingen.de/emmy-noether-kallmeyer/tulipa
 *
 *  TuLiPA is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  TuLiPA is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package de.tuebingen.tokenizer;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;

/**
 * A Reader for StringTransducers
 *
 * @author wmaier
 */
public class FiniteStateReader {

    private FiniteState transducer;

    /**
     * Read a transducer from a file.
     *
     * @param f   the file containing the transducer
     * @param ins true if the file is inside the package, false if in local file system
     * @throws TokenizerException if transducer in f cannot be interpreted
     */
    public FiniteStateReader(String f, boolean ins) throws IOException, TokenizerException {
        transducer = null;
        String transducerString = "";
        try {
            BufferedReader input = null;

            if (ins) {
                input = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream(f), "UTF-8"));
            } else {
                input = new BufferedReader(new FileReader(f));
            }
            int i = 0;

            while ((i = input.read()) != -1) {
                transducerString += String.valueOf((char) i);
            }
            input.close();
        } catch (IOException e) {
            throw new IOException("Error while reading tokenizer: " + e.getMessage());
        }

        //System.err.println("read: " + transducerString);
        readTransducer(transducerString);
    }

    /**
     * Read a transducer from a file within the package
     *
     * @param f the file containing the transducer
     * @throws IOException
     * @throws TokenizerException if we can't interpret the transducer in f
     */
    public FiniteStateReader(String f) throws IOException, TokenizerException {
        this(f, true);
    }

    /*
     * cut of comments in String s
     */
    private String helperProcessComments(String s) {
        String ret = s;
        int i = ret.indexOf(FiniteState.COMMENT);
        if (i > -1) {
            ret = ret.substring(0, i);
            return ret;
        }
        return s;
    }

    /*
     * collect final states in String s
     */
    private HashSet<String> helperProcessFinal(String s) {
        HashSet<String> ret = new HashSet<String>(10);
        String[] sarr = s.split(" ");
        for (int i = 1; i < sarr.length; ++i) {
            ret.add(sarr[i]);
        }
        return ret;
    }

    /*
     * find the start state definition in String s
     */
    private String helperProcessStart(String s) {
        s = s.trim();
        String ret = "";
        int i = s.indexOf(FiniteState.START);
        if (i > -1) {
            ret = s.substring(i);
            int j = ret.indexOf(" ");
            if (j > -1) {
                ret = ret.substring(j + 1);
            }
        }
        return ret;
    }

    /**
     * Read a transducer from a string.
     * Syntax:
     * <ul>
     * <li>keywords start, final mark the start states and the final states</li>
     * <li>comments are introduced with '%'</li>
     * <li>all other lines contain transitions and must be of the form
     * <pre>start end input output</pre>
     * where
     * <ul>
     *   <li><pre>output</pre> can be commited (default is identity transduction)
     *   <li><pre>input</pre> is either a single character or a string. A string is
     *   interpreted as a disjunction of characters.</li>
     * </ul>
     * </ul>
     *
     * @param sa the string
     * @return true if we succeed
     * @throws TokenizerException if some error occurs
     */
    public boolean readTransducer(String in) throws TokenizerException {
        Object sa[] = in.split("\n");
        transducer = new FiniteState();
        HashSet<String> finalstates = new HashSet<String>();
        for (int i = 0; i < sa.length; ++i) {
            String s = (String) sa[i];
            s = helperProcessComments(s);
            s = s.trim();
            // no comments: interpret line
            if (!s.equals("")) {
                // final states
                if (s.indexOf(FiniteState.FINAL) > -1) {
                    //System.err.println("Adding final state " + helperProcessFinal(s));
                    finalstates.addAll(helperProcessFinal(s));
                    // start state
                } else if (s.indexOf(FiniteState.START) > -1) {
                    if (transducer.getStartState() != FiniteState.START) {
                        throw new TokenizerException("It seems that there is more than one start state.");
                    }
                    //System.err.println("Setting start state to " + helperProcessStart(s));
                    transducer.setStartState(helperProcessStart(s));
                    // transition
                } else {
                    // System.err.println("Try this: " + s);
                    String[] linearr = s.split(" ");
                    if (linearr.length < 3) {
                        throw new TokenizerException("Error reading fsm. Can't interpret line " + s + ".");
                    }
                    // source state and destination state
                    Object from = linearr[0].toString();
                    Object to = linearr[1].toString();
                    // arc input and output
                    String is = linearr[2].toString();
                    boolean hasOut = false;
                    Object out = null;
                    if (linearr.length > 3) {
                        if (FiniteState.EPSILON.equals(linearr[3])) {
                            out = "";
                        } else {
                            out = linearr[3].toString().charAt(0);
                        }
                        hasOut = true;
                    }
                    int auxind = -1;
                    // space input
                    if (FiniteState.SPACE.equals(is)) {
                        if (out == null) {
                            out = ' ';
                        }
                        FiniteStateArc a = null;
                        for (int j = 0; j < FiniteState.WHITESPACE.length; ++j) {
                            a = new FiniteStateArc(from, to, FiniteState.WHITESPACE[j], out);
                            transducer.addArc(a);
                        }
                        // a single character, disjunction of characters (if is.length() > 1)
                    } else {
                        for (auxind = 0; auxind < is.length(); ++auxind) {
                            Character ic = is.charAt(auxind);
                            if (!hasOut) {
                                out = ic;
                            }
                            // try to add an arc
                            FiniteStateArc a = new FiniteStateArc(from, to, ic, out);
                            // System.err.println("Adding arc " + a.toString());
                            transducer.addArc(a);
                        }
                    }
                    // add final states
                    if (finalstates.contains(from)) {
                        transducer.addFinalState(from);
                    }
                    if (finalstates.contains(to)) {
                        transducer.addFinalState(to);
                    }
                }
            }
        }
        if (finalstates.isEmpty()) {
            throw new TokenizerException("Can't construct automaton without final states");
        }
        return true;
    }

    /**
     * @return the transducer read in the constructor(s)
     */
    public FiniteState getTransducer() {
        return transducer;
    }
}