/**
 * File FSPrinter.java
 * 
 * Authors:
 * David Arps <david.arps@hhu.de>
 * Simon Petitjean <petitjean@phil.hhu.de>
 * 
 * Copyright
 * David Arps, 2017
 * Simon Petitjean, 2017
 * 
 * This file is part of the TuLiPA-frames system
 * https://github.com/spetitjean/TuLiPA-frames
 * 
 * 
 * TuLiPA is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * TuLiPA is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 * 
 */
package de.duesseldorf.frames;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import de.tuebingen.tag.Fs;
import de.tuebingen.tag.Value;

/**
 * 
 * @author david
 *
 */
public class FsTools {

    public static List<Fs> cleanup(List<Fs> frames) {
        List<Fs> noSimpleDoubleoccurence = new LinkedList<Fs>();

        // check for FS that occur multiple times and only keep one instance

        // keep track of corefs you have already seen in order to avoid multiple
        // addings
        Set<Value> seenCorefs = new HashSet<Value>();

        for (Fs fs : frames) {
            Value v = fs.getCoref();
            if (!seenCorefs.contains(v)) {
                noSimpleDoubleoccurence.add(fs);
                seenCorefs.add(v);
            }
        }

        // tmp:
        // return noSimpleDoubleoccurence;

        // only keep a FS if it is not a value of any other Fs
        // TODO somehow still not working completely
        List<Fs> clean = new LinkedList<Fs>();
        for (Fs fs : noSimpleDoubleoccurence) {
            Value fsv = fs.getCoref();
            // System.out.println("%%%%%%%%%%%%%%%%%%%%%%%%%%%\nFS: \nCoref: "
            // + fsv + "\n" + printFS(fs));
            boolean keep = true;
            for (Fs fscompare : noSimpleDoubleoccurence) {

                if (fs.getCoref() != fscompare.getCoref()) {
                    // System.out.println("%%\nFScompare: \n" +
                    // printFS(fscompare)
                    // + "\n\n\n");
                    if (included(fs, fscompare)) {
                        keep = false;
                        break;
                    }

                }
            }
            if (keep) {
                clean.add(fs);
            }
        }

        return clean;
    }

    public static boolean included(Fs fs1, Fs fs2) {
        for (Value v : fs2.getAVlist().values()) {
            if (v.is(Value.AVM)
                    && v.getAvmVal().getCoref().equals(fs1.getCoref())) {
                return true;
            } else {
                if (v.is(Value.AVM)) {
                    if (included(fs1, v.getAvmVal())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * returns a naive string representation of a (probably recursive) typed or
     * untyped FS
     * 
     * @param fs
     * @return
     */
    public static String printFS(Fs fs) {
        String res = "<p>";
        res += printFS(fs, 0);
        res += "</p>";
        return res;
    }

    private static String printFS(Fs fs, int recursiondepth) {
        StringBuffer sb = new StringBuffer();
        recursiondepth++;
        if (fs.isTyped()) {
            sb.append(fs.getCoref() + ":");
            sb.append("<br>");
            sb.append(nonBreakingSpace(recursiondepth));
            sb.append("type: ");
            sb.append(fs.getType().toString());
            sb.append("</br>");
        }

        Set<String> keys = fs.getAVlist().keySet();
        Iterator<String> i = keys.iterator();
        while (i.hasNext()) {
            String k = i.next();
            sb.append("<br>");
            sb.append(nonBreakingSpace(recursiondepth));
            sb.append(k);
            String sep = " = ";
            sb.append(sep);

            Value v = fs.getAVlist().get(k);

            if (v.is(Value.AVM)) {
                sb.append(printFS(v.getAvmVal(), recursiondepth + k.length()));
            } else if (v.is(Value.VAL)) {
                sb.append(v.getSVal());
                sb.append("</br>");
            } else if (v.is(Value.VAR)) {
                sb.append(v.getVarVal());
            } else {
                sb.append("minor FSPrinter fuckup: " + v.toString());
            }
        }
        return sb.toString();
    }

    /**
     * 
     * @param spaces
     *            amount of nbsp's
     * @return
     */
    private static String nonBreakingSpace(int spaces) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < spaces; i++) {
            sb.append("&nbsp;&nbsp;&nbsp;");
        }
        return sb.toString();
    }
}
