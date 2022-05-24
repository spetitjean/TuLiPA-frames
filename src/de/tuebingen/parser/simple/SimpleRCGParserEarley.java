/*
 *  File SimpleRCGParserEarley.java
 *
 *  Authors:
 *     Johannes Dellert
 *
 *  Copyright:
 *     Johannes Dellert, 2009
 *
 *  Last modified:
 *     Do 16. Apr 09:55:36 CEST 2009
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
package de.tuebingen.parser.simple;

import java.io.File;
import java.util.*;

import de.tuebingen.rcg.*;
import de.tuebingen.tokenizer.BuiltinTokenizer;
import de.tuebingen.tokenizer.Tokenizer;
import de.tuebingen.tokenizer.Word;
import de.tuebingen.tree.Grammar;
import de.tuebingen.gui.graph.GraphExplorer;
import de.tuebingen.gui.graph.GraphModel;
import de.tuebingen.io.RCGReader;
import de.tuebingen.io.TextRCGReader;
import de.tuebingen.parser.*;

public class SimpleRCGParserEarley extends RCGParser {
    int verbose = 0;
    SimpleRCGIncrementalFrontier frontier;
    HashMap<String, Set<IncrementalEarleyItem>> goBackAgenda;
    HashMap<String, Set<IncrementalEarleyItem>> toResumeAgenda;
    Set<IncrementalEarleyItem> processed;
    Set<IncrementalEarleyGoalItem> goals;

    Map<Integer, List<int[]>> forest;
    Map<Integer, List<ClauseKey>> links;
    List<Integer> answers;

    public SimpleRCGParserEarley(Grammar g) {
        super(g);
    }

    public Map<Integer, List<int[]>> getForest() {
        return forest;
    }

    public List<Integer> getAns() {
        return answers;
    }

    public Map<Integer, List<ClauseKey>> getLinks() {
        return links;
    }

    public boolean recognize(List<Word> input) {
        List<Argument> al = new ArrayList<Argument>();
        al.add(Argument.argFromWord(input));
        if (verbose > 1) {
            System.err.println("parsing " + al.toString());
        }

        frontier = new SimpleRCGIncrementalFrontier();
        goBackAgenda = new HashMap<String, Set<IncrementalEarleyItem>>();
        toResumeAgenda = new HashMap<String, Set<IncrementalEarleyItem>>();
        processed = new HashSet<IncrementalEarleyItem>();
        goals = new HashSet<IncrementalEarleyGoalItem>();

        forest = new HashMap<Integer, List<int[]>>();
        links = new HashMap<Integer, List<ClauseKey>>();
        answers = new LinkedList<Integer>();

        RCG grammar = ((RCG) super.getGrammar());
        List<Clause> axioms = grammar.getClausesForLabel(grammar.getStartPredicateLabel());
        for (Clause axiom : axioms) {
            IncrementalEarleyItem axiomItem = new IncrementalEarleyItem(axiom);
            axiomItem.resetAllRanges();
            frontier.add(axiomItem);

            IncrementalEarleyGoalItem goalItem = new IncrementalEarleyGoalItem(axiom);
            goalItem.resetAllRanges();
            goalItem.pos = input.size();
            goalItem.i = 0;
            goalItem.j = axiom.getLhs().getArgs().get(0).getContent().size();
            goals.add(goalItem);

            if (verbose > 2) {
                System.err.println("registered goal item: " + goalItem);
            }
        }

        if (verbose > 2) {
            System.err.println("agenda start: " + frontier);
        }

        return incrementalParse(Tokenizer.tok2string(input));
    }

    public boolean incrementalParse(List<String> input) {
        RCG grammar = ((RCG) super.getGrammar());
        boolean foundParse = false;
        while (frontier.size() > 0) {
            if (verbose > 3) {
                System.err.println(frontier.size() + " items on frontier");
            }
            IncrementalEarleyItem it = frontier.next();
            it.computeFollowingArgProperties();
            if (verbose > 2) {
                System.err.println("Now processing item " + it);
            }
            for (IncrementalEarleyGoalItem goal : goals) {
                if (goal.equals(it)) {
                    if (verbose > 1) {
                        System.err.println("Goal item found! " + it);
                        // System.err.println(it.toXML());
                    }
                    foundParse = true;
                }
            }
            // scan epsilon
            if (((it.currentElementType == ArgContent.VAR) && it.hasEpsilonRHS()) || (it.currentElementType == ArgContent.EPSILON)) {
                IncrementalEarleyItem nit = new IncrementalEarleyItem(it.cl);
                nit.pos = it.pos;
                nit.i = it.i;
                nit.j = it.j + 1;
                nit.el = it.el + 1;
                nit.range = new int[it.range.length][2];
                for (int i = 0; i < nit.range.length; i++) {
                    nit.range[i][0] = it.range[i][0];
                    nit.range[i][1] = it.range[i][1];
                }
                nit.range[it.el][0] = nit.pos;
                nit.range[it.el][1] = nit.pos;
                nit.addItemOrigin("scanEpsilon", it, null);
                //nit.cl.getLhs().getArgs().get(it.i).getContent().get(it.j).setType(ArgContent.EPSILON);
                if (verbose > 2) {
                    System.err.println("\tScan epsilon! New item " + nit);
                }
                if (!processed.contains(nit)) {
                    registerItemIfComplete(nit, input);
                    frontier.add(nit);
                    processed.add(it);
                } else {
                    System.err.println("\tResulting item already processed: " + nit);
                }
            }
            // scan
            if (it.pos < input.size() && it.currentElementType > -2 && input.get(it.pos).equals(it.getFollowingArgContent().getName())) {
                IncrementalEarleyItem nit = new IncrementalEarleyItem(it.cl);
                nit.pos = it.pos + 1;
                nit.i = it.i;
                nit.j = it.j + 1;
                nit.el = it.el + 1;
                nit.range = new int[it.range.length][2];
                for (int i = 0; i < nit.range.length; i++) {
                    nit.range[i][0] = it.range[i][0];
                    nit.range[i][1] = it.range[i][1];
                }
                nit.range[it.el][0] = it.pos;
                nit.range[it.el][1] = nit.pos;
                nit.addItemOrigin("scan", it, null);
                if (verbose > 2) {
                    System.err.println("\tScan! New item " + nit);
                }
                if (!processed.contains(nit)) {
                    frontier.add(nit);
                    processed.add(it);
                } else {
                    System.err.println("\tResulting item already processed: " + nit);
                }
            }
            // predict
            if (it.currentElementType == ArgContent.VAR) {
                if (it.currentRHSArgument == 0) {
                    List<Clause> clauses = grammar.getClausesForLabel(it.currentRHSPredLabel);
                    if (clauses != null) {
                        for (Clause clause : clauses) {
                            IncrementalEarleyItem nit = new IncrementalEarleyItem(clause);
                            nit.pos = it.pos;
                            nit.resetAllRanges();
                            nit.addItemOrigin("predict", it, null);
                            if (verbose > 2) {
                                System.err.println("\tPredict! New item " + nit);
                            }
                            if (!processed.contains(nit)) {
                                frontier.add(nit);
                                processed.add(it);
                            } else {
                                if (verbose > 2) {
                                    System.err.println("\tResulting item already processed: " + nit);
                                }
                                nit.computeFollowingArgProperties();
                                if (nit.currentElementType == ArgContent.EPSILON) {
                                    if (verbose > 2) {
                                        System.err.println("\tThe epsilon case allows direct scan!");
                                    }
                                    nit = new IncrementalEarleyItem(it.cl);
                                    nit.pos = it.pos;
                                    nit.i = it.i;
                                    nit.j = it.j + 1;
                                    nit.el = it.el + 1;
                                    nit.range = new int[it.range.length][2];
                                    for (int i = 0; i < nit.range.length; i++) {
                                        nit.range[i][0] = it.range[i][0];
                                        nit.range[i][1] = it.range[i][1];
                                    }
                                    nit.range[it.el][0] = it.pos;
                                    nit.range[it.el][1] = nit.pos;
                                    nit.addItemOrigin("scan_epsilon", it, null);
                                    if (verbose > 2) {
                                        System.err.println("\tScan epsilon! New item " + nit);
                                    }
                                    if (!processed.contains(nit)) {
                                        registerItemIfComplete(nit, input);
                                        frontier.add(nit);
                                        processed.add(it);
                                    } else {
                                        System.err.println("\tResulting item already processed: " + nit);
                                    }
                                }
                            }
                        }
                        String goBackClass = it.pos + "." + it.currentRHSPredLabel + "." + 0;
                        if (goBackAgenda.get(goBackClass) == null) {
                            goBackAgenda.put(goBackClass, new HashSet<IncrementalEarleyItem>());
                        }
                        goBackAgenda.get(goBackClass).add(it);
                        if (verbose > 3) {
                            System.err.println("\tPredicting item filed under " + goBackClass);
                        }
                    }
                }
            }
            // suspend
            if (it.cl.getLhs().getArgs().get(it.i).size() == it.j) {
                // find all items which may have predicted this clause
                for (int pos = 0; pos <= it.pos; pos++) {
                    if (verbose > 3) {
                        System.err.println("\tLooking up via goBackID: " + pos + "." + it.cl.getLhs().getLabel() + "." + it.i);
                    }
                    Set<IncrementalEarleyItem> possibleParents = goBackAgenda.get(pos + "." + it.cl.getLhs().getLabel() + "." + it.i);
                    if (possibleParents != null) {
                        if (verbose > 3) {
                            System.err.println("\tFound " + possibleParents.size() + " possible predictors");
                        }
                        for (IncrementalEarleyItem oit : possibleParents) {
                            oit.computeFollowingArgProperties();
                            // check compatibility of ranges
                            if (compatible(it.range[it.el - it.j][0], it.range[it.el - it.j + it.cl.getLhs().getArgs().get(it.i).size() - 1][1], oit.pos, it.pos)) {
                                boolean compatible = true;
                                for (int m = 0, o = 0; m < it.i; m++) {
                                    int length = it.cl.getLhs().getArgs().get(m).size();
                                    // difficult to get: the positions of the arguments on the LHS of the parent node clause
                                    ArgContent var = oit.cl.getRhs().get(oit.currentRHSPredicate).getArgs().get(m).getContent().get(0);
                                    int varpos = oit.determineVariablePosition(var);
                                    if (!compatible(it.range[o][0], it.range[o + length - 1][1], oit.range[varpos][0], oit.range[varpos][1])) {
                                        compatible = false;
                                    }
                                    o += length;
                                }
                                // the current item is a candidate; perform the suspend operation
                                if (compatible) {
                                    // additional check (perhaps unnecessary?): can the dot in the other item still move on?
                                    if (oit.currentRHSElement != -1) {
                                        IncrementalEarleyItem nit = new IncrementalEarleyItem(oit.cl);
                                        nit.pos = it.pos;
                                        nit.i = oit.i;
                                        nit.j = oit.j + 1;
                                        nit.el = oit.el + 1;
                                        nit.range = new int[oit.range.length][2];
                                        for (int i = 0; i < nit.range.length; i++) {
                                            nit.range[i][0] = oit.range[i][0];
                                            nit.range[i][1] = oit.range[i][1];
                                        }
                                        nit.range[oit.el][0] = oit.pos;
                                        nit.range[oit.el][1] = it.pos;
                                        nit.addItemOrigin("suspend", it, oit);
                                        if (it.i < it.cl.getLhs().getArgs().size() - 1) {
                                            String toResumeClass = nit.pos + "." + oit.currentRHSPredLabel;
                                            if (toResumeAgenda.get(toResumeClass) == null) {
                                                toResumeAgenda.put(toResumeClass, new HashSet<IncrementalEarleyItem>());
                                            }
                                            toResumeAgenda.get(toResumeClass).add(it);
                                            if (verbose > 3) {
                                                System.err.println("\tFiled under resume ID " + toResumeClass);
                                            }
                                        }
                                        if (!processed.contains(nit)) {
                                            if (verbose > 2) {
                                                System.err.println("\tFound matching item to suspend: " + oit);
                                                System.err.println("\tSuspend! New item " + nit);
                                            }
                                            registerItemIfComplete(nit, input);
                                            processed.add(nit);
                                            frontier.add(nit);
                                        } else {
                                            if (verbose > 2) {
                                                System.err.println("\tFound matching item to suspend: " + oit);
                                                System.err.println("\tResulting item already processed: " + nit);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            // resume
            if (it.currentElementType == ArgContent.VAR) {
                if (it.currentRHSArgument > 0) {
                    for (int pos = 0; pos <= it.pos; pos++) {
                        if (verbose > 3) {
                            System.err.println("Looking up via resume ID: " + pos + "." + it.currentRHSPredLabel);
                        }
                        Set<IncrementalEarleyItem> possibleParents = toResumeAgenda.get(pos + "." + it.currentRHSPredLabel);
                        if (possibleParents != null) {
                            for (IncrementalEarleyItem oit : possibleParents) {
                                oit.computeFollowingArgProperties();
                                if (verbose > 3) {
                                    System.err.println("\tFound matching item to resume: " + oit);
                                }
                                // it RHS position must be oit LHS position!
                                if (oit.cl.getLhs().getArgs().get(oit.i).size() == oit.j) {
                                    boolean compatible = true;
                                    for (int m = 0, o = 0; m <= oit.i; m++) {
                                        int rangeLength = oit.cl.getLhs().getArgs().get(m).size();
                                        // get the position of the m-th argument in the active RHS predicate on the LHS of the parent node clause
                                        ArgContent var = it.cl.getRhs().get(it.currentRHSPredicate).getArgs().get(m).getContent().get(0);
                                        int varpos = it.determineVariablePosition(var);
                                        // make sure the bindings of the m-th argument are compatible
                                        if (!compatible(oit.range[o][0], oit.range[o + rangeLength - 1][1], it.range[varpos][0], it.range[varpos][1])) {
                                            compatible = false;
                                        }
                                        o += rangeLength;
                                    }
                                    // the current item is a candidate for resumption; perform the operation
                                    if (compatible) {
                                        IncrementalEarleyItem nit = new IncrementalEarleyItem(oit.cl);
                                        nit.pos = it.pos;
                                        nit.i = oit.i + 1;
                                        nit.j = 0;
                                        nit.el = oit.el;
                                        nit.range = new int[oit.range.length][2];
                                        for (int i = 0; i < nit.range.length; i++) {
                                            nit.range[i][0] = oit.range[i][0];
                                            nit.range[i][1] = oit.range[i][1];
                                        }
                                        nit.addItemOrigin("resume", it, oit);
                                        String goBackClass = it.pos + "." + it.currentRHSPredLabel + "." + it.currentRHSArgument;
                                        if (goBackAgenda.get(goBackClass) == null) {
                                            goBackAgenda.put(goBackClass, new HashSet<IncrementalEarleyItem>());
                                        }
                                        goBackAgenda.get(goBackClass).add(it);
                                        if (!processed.contains(nit)) {
                                            if (verbose > 2) {
                                                System.err.println("\tFound matching item to resume: " + oit);
                                                System.err.println("\tResume! New item " + nit);
                                            }
                                            if (verbose > 3) {
                                                System.err.println("\tPredicting item filed under " + goBackClass);
                                            }
                                            frontier.add(nit);
                                            processed.add(nit);
                                        } else {
                                            if (verbose > 2) {
                                                System.err.println("\tFound matching item to resume: " + oit);
                                                System.err.println("\tResulting item already processed: " + nit);
                                            }
                                            nit.computeFollowingArgProperties();
                                            if (nit.currentElementType == ArgContent.EPSILON) {
                                                if (verbose > 2) {
                                                    System.err.println("\tThe epsilon case allows direct scan!");
                                                }
                                                nit = new IncrementalEarleyItem(it.cl);
                                                nit.pos = it.pos;
                                                nit.i = it.i;
                                                nit.j = it.j + 1;
                                                nit.el = it.el + 1;
                                                nit.range = new int[it.range.length][2];
                                                for (int i = 0; i < nit.range.length; i++) {
                                                    nit.range[i][0] = it.range[i][0];
                                                    nit.range[i][1] = it.range[i][1];
                                                }
                                                nit.range[it.el][0] = it.pos;
                                                nit.range[it.el][1] = nit.pos;
                                                nit.addItemOrigin("scan_epsilon", it, null);
                                                if (verbose > 2) {
                                                    System.err.println("\tScan epsilon! New item " + nit);
                                                }
                                                if (!processed.contains(nit)) {
                                                    registerItemIfComplete(nit, input);
                                                    frontier.add(nit);
                                                    processed.add(it);
                                                } else {
                                                    System.err.println("\tResulting item already processed: " + nit);
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        //System.err.println(printForest());
        return foundParse;
    }

    public boolean compatible(int range1Start, int range1End, int range2Start, int range2End) {
        if (verbose > 4) {
            System.err.println("\t\tCompatibility check: " + range1Start + "," + range1End + " and " + range2Start + "," + range2End);
        }
        if (range1Start == -1 || range1End == -1 || range2Start == -1 || range2End == -1) return false;
        return (range1Start == range2Start && range1End == range2End);
    }

    public void registerItemIfComplete(IncrementalEarleyItem it, List<String> input) {
        //if the item is complete (dot in rightmost position)
        if (it.cl.getLhs().getArgs().size() == it.i + 1 && it.cl.getLhs().getArgs().get(it.i).size() == it.j) {
            try {
                //convert the item's range to a binding object
                Binding bd = new Binding();
                for (int i = 0; i < it.range.length; i++) {
                    ArgContent cont = getPredicateArgContent(it.cl.getLhs(), i);
                    //System.err.println(cont + ": Content type: " + cont.getType());
                    if (cont.getType() != ArgContent.TERM) {
                        int l = it.range[i][0];
                        int r = it.range[i][1];
                        ArgContent boundCt = new ArgContent(ArgContent.EPSILON, "Eps");
                        if (r - l == 1) {
                            boundCt = new ArgContent(ArgContent.TERM, input.get(l));
                        } else if (r - l > 1) {
                            List<ArgContent> list = new LinkedList<ArgContent>();
                            while (l < r) {
                                list.add(new ArgContent(ArgContent.TERM, input.get(l)));
                                l++;
                            }
                            boundCt = new ArgContent(list);
                        } else if (cont.getType() == ArgContent.EPSILON) {
                            //System.err.println("Setting to epsilon!");
                            //cont.setName("Eps");
                            boundCt = new ArgContent(ArgContent.EPSILON, "Eps");
                        }
                        bd.bind(false, cont, boundCt);
                    } else {
                        if (it.range[i][0] == it.range[i][1]) {
                            //System.err.println("Setting bad things to epsilon!");
                            cont.setType(ArgContent.EPSILON);
                            ArgContent boundCt = new ArgContent(ArgContent.EPSILON, "Eps");
                            bd.bind(false, cont, boundCt);
                        }
                    }
                }

                //System.err.println(it.cl);
                //System.err.println(bd);

                //construct a clause key with the bindings
                List<Argument> la = Predicate.instantiate(it.cl.getLhs(), bd);

                //Normalise the bindings to reflect the arguments (ie encapsulate bindings):
                // we need to rename the constants (c.f. they have been renamed for multiple occurrences)
                List<Argument> la2 = new LinkedList<Argument>();
                for (Argument arg : la) {
                    Argument arg2 = new Argument();
                    List<ArgContent> ac2 = new LinkedList<ArgContent>();
                    for (int k = 0; k < arg.getContent().size(); k++) {
                        ArgContent ac = arg.getContent().get(k);
                        //epsilon arguments must evaluate to "Eps" for correct hashing during forest expansion
                        if (ac.getType() == ArgContent.EPSILON) {
                            ac2.add(new ArgContent(ArgContent.EPSILON, "Eps"));
                        } else {
                            if ((k == 0 && arg.getContent().size() == 1) || (ac.getType() != ArgContent.EPSILON))
                                ac2.add(ac);
                        }
                    }
                    arg2.addArg(new ArgContent(ac2));
                    la2.add(arg2);
                }

                // The clauseKey now uses the la2 list of arguments:
                ClauseKey ck = new ClauseKey(it.cl.getCindex(), la2);

                //String itemHash = it.cl.getLhs().toString() + it.rangeString();
                String itemHash = it.cl.getLhs().getLabel().toString() + it.argumentRangeString();

                Integer key = new Integer(itemHash.hashCode());

                //System.err.println("Item Code: " + key + " Hashing: " + itemHash);

                // if the item concerns the start predicate, register it as a new parse forest head
                if (it.pos == input.size() && it.cl.getLhs().getLabel().equals(((RCG) super.getGrammar()).getStartPredicateLabel()) && !(answers.contains(key))) {
                    answers.add(key);
                }

                List<Predicate> lp = it.cl.getRhs();
                int[] rhs = new int[lp.size()];

                for (int pidx = 0; pidx < lp.size(); pidx++) {
                    Predicate p = lp.get(pidx);
                    String pred = p.getLabel().toString();

                    String ranges = "[";

                    for (Argument arg : p.getArgs()) {
                        for (ArgContent argc : arg.getContent()) {
                            int pos = it.determineVariablePosition(argc);
                            ranges += "[" + it.range[pos][0] + "," + it.range[pos][1] + "]";
                            ranges += ",";
                        }
                    }
                    if (ranges.length() > 1) {
                        ranges = ranges.substring(0, ranges.length() - 1);
                    }
                    ranges += "]";

                    String hash = pred + ranges;
                    rhs[pidx] = hash.hashCode();

                    //System.err.println("RHS Code: " + rhs[pidx] + " Hashing: " + hash);
                }

                addToParseForest(key, rhs, ck);
            } catch (RCGInstantiationException e) {
                System.err.println("Predicate instantiation error " + e.getMessage());
            }
        }
    }

    private void addToParseForest(Integer key, int[] value, ClauseKey ck) {
        List<int[]> li = forest.get(key);
        if (li == null) {
            li = new LinkedList<int[]>();
            forest.put(key, li);
            links.put(key, new ArrayList<ClauseKey>());
        }
        //if (!this.duplicated(key, ck))
        if (!this.duplicated(key, value)) {
            li.add(value);
            // finally, we store the clause key that led to this instantiation of a RHS
            ((ArrayList<ClauseKey>) links.get(key)).add(ck);
            //Collections.sort((ArrayList<ClauseKey>) links.get(key));
        }
    }

    private ArgContent getPredicateArgContent(Predicate p, int i) {
        int j = 0;
        List<Argument> args = p.getArgs();
        for (Argument arg : args) {
            List<ArgContent> cts = arg.getContent();
            for (ArgContent ct : cts) {
                if (j == i) return ct;
                else j++;
            }
        }
        return null;
    }

    public boolean parse(List<Word> input) {
        return (this.recognize(input));
    }

    @Override
    public boolean parseSentence(boolean v, List<Word> sentence) {
        return parse(sentence);
    }

    public List<ClauseKey> getAnswers() {
        return null;
    }

    public List<ClauseKey> getEmptyRHS() {
        return null;
    }

    public Hashtable<ClauseKey, DStep> getParse() {
        return null;
    }

    private boolean duplicated(Integer key, int[] v) //ClauseKey ck)
    {
        //return (links.containsKey(key) && links.get(key).contains(ck));
        boolean res = false;
        if (forest.containsKey(key)) {
            for (int[] x : forest.get(key)) {
                res |= Arrays.equals(x, v);
            }
        }
        return res;
    }

    public String printForest() {
        // this methods prints the content of the parseF structure:
        StringBuffer sb = new StringBuffer();
        sb.append("\nANSWERS:\n");
        for (Integer j : answers) {
            sb.append(String.valueOf(j));
            sb.append("\n");
        }
        sb.append("\nFOREST:\n");
        for (Integer i : forest.keySet()) {
            sb.append("hash: ");
            sb.append(String.valueOf(i));
            sb.append("\n\t");
            for (int k = 0; k < links.get(i).size(); k++) {
                ClauseKey ck = links.get(i).get(k);
                sb.append(ck.toString((RCG) super.getGrammar()));
                //sb.append(ck.toString());
                sb.append("\n\t");
                int[] tab = forest.get(i).get(k);
                for (int l = 0; l < tab.length; l++) {
                    sb.append(tab[l]);
                    sb.append(" ");
                }
                sb.append("\n\t");
            }
            sb.append("\n");
        }
        String res = sb.toString();
        return res;
    }


    @Override
    public ForestExtractorInitializer getForestExtractorInitializer() {
        ForestExtractorInitializer ret = new ForestExtractorInitializer();
        ret.addField(getAns());
        ret.addField(getForest());
        ret.addField(getLinks());
        return ret;
    }

    /*
     * just for testing
     */
    public static void main(String[] args) throws Exception {
        RCGReader rcggr = new TextRCGReader(new File("/home/jd/workspace/tulipa/trunk/test/rcg/ambiguous/g4.rcg"));
        RCG g = rcggr.getRCG();
        // RCG g = new RCG();
        // g.anbn();
        System.err.println(g.toString());
        // g.termtransform();
        // System.err.println(g.toString());
        Tokenizer tok = new BuiltinTokenizer();
        tok.setSentence("a b a b");
        List<Word> input = tok.tokenize();
        SimpleRCGParserEarley p = new SimpleRCGParserEarley(g);
        System.err.println(p.parseSentence(true, input));
        GraphModel gm = GraphModel.loadGraph(IncrementalEarleyItem.itemsAsGraph());
        GraphExplorer e = new GraphExplorer(gm);
        e.p.calculateDescendentGraph(false);
        // e.p.calculateTreeCoordinates(IncrementalEarleyItem.items.size() - 1);
        e.p.calculateAccessTable();
        e.p.repaint();
        e.setSize(1000, 750);
        e.setLocation(0, 0);
        e.setVisible(true);

        /*
         * g = new RCG(); g.copylanguage(); System.err.println(g.toString()); //g.termtransform(); //System.err.println(g.toString()); tok = new BuiltinTokenizer(); tok.setSentence("a a b a a b");
         * input = tok.tokenize(); p = new SimpleRCGParserEarley(g); System.err.println(p.parseSentence(true,input));
         */
    }

}
