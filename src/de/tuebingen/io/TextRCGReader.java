/*
 *  File TextRCGReader.java
 *
 *  Authors:
 *     Wolfgang Maier  <wo.maier.uni-tuebingen.de>
 *
 *  Copyright:
 *     Wolfgang Maier, 2007
 *
 *  Last modified:
 *     Di 16. Okt 10:45:12 CEST 2007
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
package de.tuebingen.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;

import de.tuebingen.rcg.ArgContent;
import de.tuebingen.rcg.Argument;
import de.tuebingen.rcg.Clause;
import de.tuebingen.rcg.PredStringLabel;
import de.tuebingen.rcg.Predicate;
import de.tuebingen.rcg.RCG;

/**
 * A reader for RCGs in text format. Variables must be prefixed by "_".
 * <p>
 * a([a][_X],[a][_Y]) --> a([_X],[_Y])
 * a([b][_X],[b][_Y]) --> a([_X],[_Y])
 * a([Eps],[Eps]) --> [Eps]
 * S([_X][_Y]) --> a([_X],[_Y])
 *
 * @author wmaier
 */
public class TextRCGReader extends BufferedReader implements RCGReader {

    // separator of lhs and rhs
    public static final String CLAUSESEP = "-->";
    // separator of single arguments of clauses
    public static final String ARGSEP = ",";
    // variable prefix
    public static final char VARPREF = '_';
    // epsilon
    public static final String EPSILON = "eps";

    public TextRCGReader(File f) throws FileNotFoundException {
        super(new FileReader(f));
    }

    public static ArgContent stringToArgContent(String argcontent) {
        //System.err.println("argcontent: " + argcontent);
        ArgContent ret = null;
        int type = 0;
        if (argcontent.length() > 0) {
            if (argcontent.charAt(0) == VARPREF) {
                // variable
                argcontent = argcontent.substring(1);
                type = ArgContent.VAR;
            } else if (EPSILON.equals(argcontent.toLowerCase())) {
                // 	epsilon
                type = ArgContent.EPSILON;
            } else {
                // 	constant
                type = ArgContent.TERM;
            }
            ret = new ArgContent(type, argcontent);
        }
        return ret;
    }

    public static Argument stringToArgument(String argument) {
        Argument ret = null;
        //System.err.println("argument :" + argument);
        ArgContent argcont = null;
        int argcontindl = 0;
        int argcontindr = argument.indexOf(']');
        while ((argcontindr + 1) > 0) {
            if (ret == null) {
                ret = new Argument();
            }
            if (argcontindl + 1 > argcontindr) {
                ret = null;
                break;
            }
            String currentargument = argument.substring(argcontindl + 1, argcontindr);
            if (currentargument.length() < 1) {
                ret = null;
                break;
            }
            argcont = stringToArgContent(currentargument);
            if (argcont == null) {
                ret = null;
                break;
            }
            //System.err.println("adding argcont " + argcont.toString());
            ret.addArg(argcont);
            argcontindl = argcontindr + 1;
            argcontindr = argument.indexOf(']', argcontindl);
        }
        //System.err.println("ret: " + ret.toString());
        return ret;
    }

    public static Predicate stringToPredicate(String pred) {
        // System.err.print("predicate " + pred);
        Predicate ret = null;
        // Identify head
        int oparind = pred.indexOf('(');
        if (oparind > -1) {
            String head = pred.substring(0, oparind);
            pred = pred.substring(oparind);
            // if its arguments are enclosed in parens
            if (pred.charAt(0) == '(' && pred.charAt(pred.length() - 1) == ')') {
                // skip parens
                pred = pred.substring(1, pred.length() - 1);
                if (pred.indexOf('(') == -1 && pred.indexOf('(') == -1) {
                    ret = new Predicate(new PredStringLabel(head));
                    // Identify single arguments
                    Argument arg = null;
                    int argindl = 0;
                    int argindr = pred.indexOf(ARGSEP) == -1 ? pred.length() : pred.indexOf(ARGSEP);
                    while ((argindr + 1) > 0) {
                        arg = stringToArgument(pred.substring(argindl, argindr).trim());
                        if (arg == null) {
                            ret = null;
                            break;
                        } else {
                            //System.err.println("adding arg " + arg.toString());
                            ret.addArg(arg);
                        }
                        argindl = argindr + 1;
                        argindr = pred.indexOf(ARGSEP, argindl);
                        if (argindr == -1 && argindl <= pred.length()) {
                            argindr = pred.length();
                        }
                    }
                }
            }
        }
        return ret;
    }

    public static Clause stringToClause(String line) throws Exception {
        Clause ret = null;
        int sepind = line.indexOf(CLAUSESEP);
        if (sepind > 0) {
            int lhsend = line.indexOf(')') + 1;
            if (lhsend > 0) {
                String lhs = line.substring(0, lhsend).trim();
                int rhsstart = sepind + CLAUSESEP.length();
                String rhs = line.substring(rhsstart).trim();
                //System.err.println("lhs: " + lhs + ", rhs: " + rhs);
                Predicate lhsp = stringToPredicate(lhs);
                //System.err.println("lhsp: " + lhsp);
                if (lhsp != null) {
                    ret = new Clause();
                    // add the lhs
                    ret.setLhs(lhsp);
                    // process the rhs
                    Predicate rhsp = null;
                    int rhsindl = 0;
                    int rhsindr = rhs.indexOf(')') + 1;
                    while (rhsindr > 0) {
                        String rhscurrent = rhs.substring(rhsindl, rhsindr).trim();
                        if (rhscurrent.length() < 6) {
                            throw new Exception("Could not parse " + line + ": This does not seem to be a predicate: " + rhscurrent);
                        }
                        rhsp = stringToPredicate(rhscurrent);
                        if (rhsp == null) {
                            throw new Exception("Could not parse " + line + ": Could not read rhs predicate " + rhscurrent);
                        } else {
                            ret.addToRhs(rhsp);
                        }
                        // jump to next predicate
                        rhsindl = rhsindr;
                        rhsindr = rhs.indexOf(')', rhsindl) + 1;
                    }
                } else {
                    throw new Exception("Could not parse " + line + ": Could not read lhs predicate " + lhs);
                }
            } else {
                throw new Exception("Could not parse " + line + ": Could not read lhs predicate, no closing parenthesis.");
            }
        } else {
            throw new Exception("Could not parse " + line + ": Separator '" + CLAUSESEP + "' not found.");
        }
        return ret;
    }

    public RCG getRCG() throws Exception {
        RCG ret = new RCG();
        String line = "";
        while ((line = super.readLine()) != null) {
            if (line.length() < 2) {
                continue;
            }
            line = line.trim();
            if (line.charAt(0) == '#') {
                line = line.substring(1).trim();
                if (line.indexOf("start") == 0) {
                    line = line.substring(5).trim();
                    if (line.length() > 0) {
                        ret.setStartPredicate(new PredStringLabel(line));
                    }
                }
            } else if (line.charAt(0) != '%') {
                Clause c = stringToClause(line);
                if (c != null) {
                    ret.addClause(c, null);
                }
            }
        }
        if (!ret.startPredicateDefined()) {
            ret.setStartPredicate(new PredStringLabel("S"));
        }
        if (ret.getClausesForLabel(ret.getStartPredicateLabel()) == null) {
            throw new Exception("Cannot read RCG: No predicate with start predicate label found.");
        }
        return ret;
    }

}
