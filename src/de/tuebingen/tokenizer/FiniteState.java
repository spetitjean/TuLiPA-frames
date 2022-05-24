/*
 *  File FiniteState.java
 *
 *  Authors:
 *     Wolfgang Maier <wo.maier@uni-tuebingen.de>
 *
 *  Copyright:
 *     Wolfgang Maier, 2007
 *
 *  Last modified:
 *     Di 16. Okt 09:39:54 CEST 2007
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

import de.tuebingen.util.CollectionUtilities;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Represents a Deterministic Finite State Automaton/Transducer without
 * epsilon transitions.
 *
 * @author wmaier
 */
public class FiniteState {

    protected static final Object[] WHITESPACE = {' ', '\n', '\t'};
    protected static final String EPSILON = "epsilon";
    protected static final String SPACE = "space";
    protected static final String START = "start";
    protected static final String FINAL = "final";
    protected static final String COMMENT = "%";

    private Object currentState;
    private List<Object> currentOutput;
    private List<Object> currentPath;
    private Object startState;
    private Set<Object> arcs;
    private Map<Object, Set<Object>> mapSourceToArcs;
    private Map<Object, Set<Object>> mapInputToArcs;
    private Set<Object> finalStates;

    public FiniteState() {
        arcs = new HashSet<Object>();
        mapSourceToArcs = new HashMap<Object, Set<Object>>();
        mapInputToArcs = new HashMap<Object, Set<Object>>();
        finalStates = new HashSet<Object>();
        startState = START;
        currentState = null;
        currentOutput = new ArrayList<Object>();
        currentPath = new ArrayList<Object>();
    }

    /**
     * @return the current state of the automaton
     */
    public Object getState() {
        return currentState;
    }

    /**
     * @return the current (transduced) output of the automaton
     */
    public List<Object> getOutput() {
        return currentOutput;
    }

    /**
     * @return (transduced) output as formatted string
     */
    public String getStringOutput() {
        String ret = "";
        Iterator<Object> it = currentOutput.iterator();
        while (it.hasNext()) {
            String s = it.next().toString();
            if (!EPSILON.equals(s)) {
                ret += s;
            }
        }
        return ret;
    }

    /**
     * reset the current output to the empty list
     */
    public void reset() {
        currentState = startState;
        currentPath.clear();
        currentOutput.clear();
    }

    /**
     * @return the start state of the automaton
     */
    public Object getStartState() {
        return startState;
    }

    /**
     * set the start state of the automaton
     *
     * @param o the start state to set
     */
    public void setStartState(Object o) {
        startState = o;
    }

    /**
     * @return true if state is a final state
     */
    public boolean isFinalState(Object o) {
        return finalStates.contains(o);
    }

    /**
     * @return the finalStates of the automaton
     */
    public Set<Object> getFinalStates() {
        return finalStates;
    }

    /**
     * add a final state to the automaton
     *
     * @param o the final state to add
     */
    public void addFinalState(Object o) {
        finalStates.add(o);
    }

    /**
     * @param source a state in the fsm
     * @return all arcs with source as their source state
     */
    public Set<Object> getArcsForSource(Object source) {
        return mapSourceToArcs.get(source);
    }

    /**
     * @param input the input to an arc
     * @return all arcs with the given object as input
     */
    public Set<Object> getArcsForInput(Object input) {
        return mapInputToArcs.get(input);
    }

    /**
     * Add an arc to the FiniteState. Non-deterministic arcs are not allowed.
     * No epsilon transitions. All fields must be non-null.
     *
     * @param t the arc to add
     * @throws TokenizerException thrown if arc cannot be added
     */
    public void addArc(FiniteStateArc t) throws TokenizerException {
        // all fields must be non-null
        if (t.getFrom() == null || t.getTo() == null
                || t.getInput() == null || t.getOutput() == null) {
            throw new TokenizerException("Can't add arc to FSM: Some arc fields are empty.");
        }
        // we do not allow non-determinism
        Set<Object> ss = getArcsForSource(t.getFrom());
        Set<Object> is = getArcsForInput(t.getInput());
        if (ss != null && is != null) {
            ss = new HashSet<Object>(ss);
            ss.retainAll(is);
            if (ss.size() > 0) {
                throw new TokenizerException("Can't add nonderminstic arc [" + t + "] to FSM.");
            }
        }
        arcs.add(t);
        CollectionUtilities.addToValueSet(mapSourceToArcs, t.getFrom(), t);
        CollectionUtilities.addToValueSet(mapInputToArcs, t.getInput(), t);
    }

    /**
     * Move the automaton back one state ("un"-read last input).
     *
     * @return true if beforehand, we have called {@link read} at least once.
     */
    public boolean unread() {
        if (currentOutput.size() < 1 || currentPath.size() < 1) {
            return false;
        }
        currentOutput.remove(currentOutput.size() - 1);
        currentState = currentPath.get(currentPath.size() - 1);
        currentPath.remove(currentPath.size() - 1);
        return true;
    }

    /**
     * Read a single character
     *
     * @return true if character could be read
     */
    public boolean read(Object c) {
        if (currentState == null) {
            currentState = getStartState();
        }
        // System.err.println("Reading " + c + " in state " + currentState.toString());
        // grossly inefficient
        Set<Object> sl = null;
        Set<Object> il = null;
        // no arcs with the given input or no outgoing arcs from state
        if (getArcsForSource(currentState) == null) {
            return false;
        }
        sl = new HashSet<Object>(getArcsForSource(currentState));
        if (getArcsForInput(c) == null) {
            return false;
        }
        il = new HashSet<Object>(getArcsForInput(c));
        // intersect
        sl.retainAll(il);
        // no arcs: input can't be read at current position
        if (sl.size() == 0) {
            return false;
        }
        // first and only element of the intersection is the right arc
        FiniteStateArc myarc = (FiniteStateArc) (CollectionUtilities.getFirstCollectionMember(sl));
        currentState = myarc.getTo();
        currentOutput.add(myarc.getOutput());
        currentPath.add(myarc.getFrom());
        //System.err.println(".. success (transduced to " + myarc.getOutput().toString() + ")");
        return true;
    }

    /*
     * do a depth-first traversal of the automaton
     */
    private String doDepthFirst(Object state, Set<Object> visited) {
        if (visited.contains(state)) {
            return "";
        }
        visited.add(state);
        if (getArcsForSource(state) == null) {
            return "";
        }
        Set<Object> outboundArcs = new HashSet<Object>(getArcsForSource(state));
        String ret = "";
        Iterator<Object> it = outboundArcs.iterator();
        while (it.hasNext()) {
            FiniteStateArc a = (FiniteStateArc) it.next();
            ret += a.getFrom().toString() + " ";
            ret += a.getTo().toString() + " ";
            if (Arrays.asList(WHITESPACE).contains(a.getInput())) {
                ret += SPACE;
            } else {
                ret += a.getInput().toString();
            }
            if (Arrays.asList(WHITESPACE).contains(a.getOutput())) {
                ret += " " + SPACE;
            } else {
                ret += " " + a.getOutput().toString();
            }
            ret += "\n";
            ret += doDepthFirst(a.getTo(), visited);
        }
        return ret;
    }

    public String toString() {
        String ret = "";
        ret += COMMENT + "start state\n";
        ret += START + " " + getStartState() + "\n\n";
        ret += COMMENT + "final states\n";
        ret += FINAL;
        Iterator<Object> it = getFinalStates().iterator();
        while (it.hasNext()) {
            ret += " " + it.next().toString();
        }
        ret += "\n\n";
        // do depth-first
        ret += COMMENT + "transitions\n";
        Object state = getStartState();
        ret += doDepthFirst(state, new HashSet<Object>());
        return ret;
    }


    /*
     * just for testing
     */
	/*public static void main(String[] args) throws Exception {
		FiniteStateReader r = new FiniteStateReader(new File("/home/wmaier/test"));
		FiniteState f = r.getTransducer();
		System.err.println(f.toString());
	}*/


}
