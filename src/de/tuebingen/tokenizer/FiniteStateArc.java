/*
 *  File FiniteStateArc.java
 *
 *  Authors:
 *     Wolfgang Maier  <wo.maier@uni-tuebingen.de>
 *
 *  Copyright:
 *     Wolfgang Maier, 2007
 *
 *  Last modified:
 *     Di 16. Okt 09:40:26 CEST 2007
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

/**
 * Represents a single arc in a Finite State Automaton/Transducer.
 *
 * @author wmaier
 */

public class FiniteStateArc {

    private Object from;
    private Object to;
    private Object input;
    private Object output;
    // for traversal only
    private boolean visited;

    public FiniteStateArc(Object from, Object to, Object input) {
        // default is identity transduction
        this(from, to, input, input);
    }

    public FiniteStateArc(Object from, Object to, Object input, Object output) {
        this.from = from;
        this.to = to;
        this.input = input;
        this.output = output;
        this.visited = false;
    }

    public Object getFrom() {
        return from;
    }

    public void setFrom(Object from) {
        this.from = from;
    }

    public Object getTo() {
        return to;
    }

    public void setTo(Object to) {
        this.to = to;
    }

    public Object getInput() {
        return input;
    }

    public void setInput(Object label) {
        this.input = label;
    }

    public Object getOutput() {
        return output;
    }

    public void setOutput(Object output) {
        this.output = output;
    }

    public boolean visited() {
        return visited;
    }

    public void setVisited(boolean visited) {
        this.visited = visited;
    }

    public String toString() {
        return from.toString() + " " + to.toString() + " " + input.toString() + " " + output.toString();
    }

}
