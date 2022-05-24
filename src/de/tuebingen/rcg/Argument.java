/*
 *  File Argument.java
 *
 *  Authors:
 *     Wolfgang Maier <wo.maier@uni-tuebingen.de>
 *
 *  Copyright:
 *     Wolfgang Maier, 2007
 *
 *  Last modified:
 *     Di 16. Okt 09:52:48 CEST 2007
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
package de.tuebingen.rcg;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import de.tuebingen.tokenizer.Word;

/**
 * This class represents a single argument of a predicate, i.e. a sequence of
 * Argument. A single Range in turn is either a range variable or a terminal
 * (constant).
 *
 * @author wmaier
 */
public class Argument implements Iterable<ArgContent> {

    // private Object content;
    private List<ArgContent> content;

    public Argument() {
        content = new LinkedList<ArgContent>();
    }

    /**
     * Copy constructor. We know how to copy the content field
     * if it is a list or a string. It shouldn't be anything else.
     *
     * @param a - the instance to copy from
     */
    public Argument(Argument arg) {
        this();
        for (int i = 0; i < arg.size(); i++) {
            content.add(new ArgContent(arg.get(i)));
        }
    }

    public Argument(ArgContent a) {
        this();
        content.add(a);
    }

    public Argument(List<ArgContent> la) {
        content = la;
    }

    /**
     * Construct an Argument from a Word
     *
     * @param w - the word
     */
    public static Argument argFromWord(List<Word> wl) {
        Argument arg = new Argument();
        for (int i = 0; i < wl.size(); i++) {
            ArgContent ac = new ArgContent(ArgContent.TERM, wl.get(i).getWord());
            arg.addArg(ac);
        }
        return arg;
    }

    public void addArg(ArgContent r) {
        content.add(r);
    }

    public String toString() {
        String ret = "";
        List<ArgContent> al = content;
        Iterator<ArgContent> it = al.iterator();
        while (it.hasNext()) {
            ArgContent a = it.next();
            ret += "[" + a.toString() + "]";
        }
        return ret;
    }

    public String toStringRenamed(Map<String, String> names) {
        String ret = "";
        List<ArgContent> al = content;
        Iterator<ArgContent> it = al.iterator();
        while (it.hasNext()) {
            ArgContent a = it.next();
            ret += "[" + a.toStringRenamed(names) + "]";
        }
        return ret;
    }

    public ArgContent get(int i) {
        return content.get(i);
    }

    public int size() {
        return content.size();
    }

    public int getSize() {
        int res = 0;
        for (ArgContent ac : content) {
            res += ac.getSize();
        }
        return res;
    }

    public ArgContent getRec(int i) {
        ArgContent tmp = new ArgContent(content);
        return tmp.getRec(i);
    }

    public int hashCode() {
        return toString().hashCode();
    }

    public boolean equals(Object o) {
        return this.hashCode() == o.hashCode();
    }

    public List<ArgContent> getContent() {
        return content;
    }

    public void setContent(List<ArgContent> content) {
        this.content = content;
    }

    public Iterator<ArgContent> iterator() {
        return content.iterator();
    }

    public boolean isEpsilon() {
        boolean res = true;
        for (ArgContent ac : content) {
            res &= (ac.getType() == ArgContent.EPSILON);
        }
        return res;
    }
}
