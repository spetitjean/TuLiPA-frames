/*
 *  File RCGParserEarleyNoTerm.java
 *
 *  Authors:
 *     Wolfgang Maier <wo.maier@uni-tuebingen.de>
 *
 *  Copyright:
 *     Wolfgang Maier, 2008
 *
 *  Last modified:
 *     Mi 8. Okt 10:21:32 CET 2008
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


package de.tuebingen.parser.termtransform;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.tuebingen.parser.ClauseKey;
import de.tuebingen.parser.DStep;
import de.tuebingen.parser.ForestExtractorInitializer;
import de.tuebingen.parser.RCGParser;
import de.tuebingen.rcg.ArgContent;
import de.tuebingen.rcg.Argument;
import de.tuebingen.rcg.Clause;
import de.tuebingen.rcg.PredLabel;
import de.tuebingen.rcg.Predicate;
import de.tuebingen.rcg.RCG;
import de.tuebingen.tokenizer.BuiltinTokenizer;
import de.tuebingen.tokenizer.Tokenizer;
import de.tuebingen.tokenizer.Word;
import de.tuebingen.tree.Grammar;

public class RCGParserEarleyNoTerm extends RCGParser {

    private int verbose = 0;

    private Set<Item> predpred;
    private Set<Item> predrule;
    private Set<Item> complete;
    private Set<Item> scan;
    private Map<PredLabel, List<Item>> complByLabel;
    private PredLabel skey;

    public RCGParserEarleyNoTerm(Grammar g) {
        super(g);
        predpred = new HashSet<Item>();
        predrule = new HashSet<Item>();
        complete = new HashSet<Item>();
        scan = new HashSet<Item>();
        complByLabel = new HashMap<PredLabel, List<Item>>();
        skey = ((RCG) g).getStartPredicateLabel();
    }

    @Override
    public boolean parseSentence(boolean v, List<Word> sentence) {
        return parse(sentence);
    }

    public boolean parse(List<Word> input) {
        return (this.recognize(input));
    }

    public boolean recognize(List<Word> input) {
        if (!((RCG) super.getGrammar()).isTermtransformed()) {
            ((RCG) super.getGrammar()).termtransform();
        }
        List<Argument> al = new ArrayList<Argument>();
        al.add(Argument.argFromWord(input));
        if (verbose > 1) {
            System.err.println("parsing " + al.toString());
        }
        Item axiom = new Item();
        axiom.setPredLabel(((RCG) super.getGrammar()).getStartPredicateLabel());
        axiom.setRcv(new RangeConstraintVector(1));
        axiom.getRcv().update(axiom.getRcv().left(0), 0);
        axiom.getRcv().update(axiom.getRcv().right(0), input.size());
        predrule.add(axiom);
        if (verbose > 2) {
            System.err.println("agenda start: " + predrule);
        }

        Item goalitem = new Item();
        goalitem.setPredLabel(axiom.getPredLabel());
        goalitem.setCompleted(true);
        goalitem.setRcv(new RangeConstraintVector(axiom.getRcv()));

        if (verbose > 2) {
            System.err.println("axiom: " + axiom);
            System.err.println("goal : " + goalitem);
        }

        return earleyparse(goalitem, Tokenizer.tok2string(input));
    }

    private boolean earleyparse(Item goalitem, List<String> input) {
        //operations: predict-rule, predict-pred, scan, complete, convert
        boolean found = false;
        boolean stop = false;
        ArrayList<Item> transport = new ArrayList<Item>();
        Iterator<Item> agendait = null;

        // avoid multiple operations on the same items. preliminary.
        Set<Item> predblock = new HashSet<Item>();
        Set<String> complblock = new HashSet<String>();

        int count = 0;
        while (!found && !stop) {
            ++count;

            // predict-pred ***********************************************************************************
            boolean donepredict = false;
            agendait = predpred.iterator();
            while (agendait.hasNext()) {
                Item item = agendait.next();
                if (verbose > 2) {
                    System.err.println("pred-pred:" + item);
                }
                Predicate upred = null;
                if (item.getClause().getRhs().size() == 0) {
                    upred = item.getClause().getLhs();
                } else if (!item.isDotAtEnd()) {
                    upred = item.getPredicateAtDot();
                }
                if (upred != null) {
                    Item predicted = new Item();
                    predicted.setActive(false);
                    predicted.setClause(item.getClause());
                    predicted.setPredLabel(upred.getLabel());
                    predicted.setRcv(new RangeConstraintVector(upred.getArity()));
                    List<RangeBoundary> ul = new ArrayList<RangeBoundary>();
                    for (int i = 0; i < upred.getArgs().size(); ++i) {
                        Argument arg = upred.getArgs().get(i);
                        ul.add(new RangeBoundary(item.getRcv().left(arg.get(0))));
                        ul.add(new RangeBoundary(item.getRcv().right(arg.get(arg.size() - 1))));
                    }
                    predicted.getRcv().updateNoCheck(ul);
                    predicted.getRcv().resetNumbering();
                    predrule.add(predicted);
                    donepredict = true;
                    if (verbose > 3) {
                        System.err.println("  R:" + predicted);
                    }
                }
            }
            predpred = new HashSet<Item>();

            // predict-rule ***********************************************************************************
            agendait = predrule.iterator();
            while (agendait.hasNext()) {
                Item item = agendait.next();
                // passive
                if (verbose > 2) {
                    System.err.println("pred-rule:" + item);
                }
                for (Clause cl : ((RCG) super.getGrammar()).getClausesForLabel(item.getPredLabel())) {
                    if (cl.getRhs().size() == 0) {
                        item.setIsterminal(true);
                        scan.add(item);
                        continue;
                    }
                    Item predicted = new Item();
                    predicted.setActive(true);
                    predicted.setDotpos(0);
                    predicted.setClause(cl);
                    predicted.setRcv(cl.getRangeConstraintVector());

                    //System.err.println("clause: " + cl);
                    //System.err.println("vcv: " + predicted.rcv);
                    // assure variable names don't overlap of old item and vcv of new item
                    RangeConstraintVector itemrcv = new RangeConstraintVector(item.getRcv());
                    for (int i = 0; i < itemrcv.bound.size(); ++i) {
                        itemrcv.bound.get(i).addToId(predicted.getRcv().bound.size());
                    }
                    //System.err.println("itemrcv: " + itemrcv);
                    // update vcv with information from old item
                    boolean isok = true;
                    List<Argument> lhsargs = cl.getLhs().getArgs();

                    // update variables first
                    for (int i = 0; isok && i < cl.getLhs().getArity(); ++i) {
                        ArgContent lmost = lhsargs.get(i).get(0);
                        ArgContent rmost = lhsargs.get(i).get(lhsargs.get(i).size() - 1);
                        RangeBoundary lnew = itemrcv.left(i);
                        if (lnew.getVal() == -1) {
                            RangeBoundary lold = predicted.getRcv().left(lmost);
                            isok &= predicted.getRcv().update(lold, lnew);
                        }
                        RangeBoundary rnew = itemrcv.right(i);
                        if (rnew.getVal() == -1) {
                            RangeBoundary rold = predicted.getRcv().right(rmost);
                            isok &= predicted.getRcv().update(rold, rnew);
                        }
                    }
                    if (isok) {
                        // then update boundaries
                        for (int i = 0; isok && i < cl.getLhs().getArity(); ++i) {
                            ArgContent lmost = lhsargs.get(i).get(0);
                            ArgContent rmost = lhsargs.get(i).get(lhsargs.get(i).size() - 1);
                            RangeBoundary lold = predicted.getRcv().left(lmost);
                            RangeBoundary rold = predicted.getRcv().right(rmost);
                            RangeBoundary lnew = itemrcv.left(i);
                            RangeBoundary rnew = itemrcv.right(i);
                            if (lnew.getVal() > -1) {
                                isok &= predicted.getRcv().update(lold, lnew.getVal());
                            }
                            if (rnew.getVal() > -1) {
                                isok &= predicted.getRcv().update(rold, rnew.getVal());
                            }
                        }
                    }
                    // update numbering
                    if (isok) {
                        predicted.getRcv().resetNumbering();
                        donepredict = true;
                        if (!predblock.contains(predicted)) {
                            predpred.add(predicted);
                            predblock.add(predicted);
                            complete.add(predicted);
                        }
                        if (verbose > 3) {
                            System.err.println("  R:" + predicted);
                        }
                    }
                }
            }
            predrule.clear();

            // scan *******************************************************************************************

            /*
             * FIXME: Scan only scans clauses with a single terminal as LHS argument.
             */

            boolean donescan = false;
            agendait = scan.iterator();
            while (agendait.hasNext()) {
                Item item = agendait.next();
                ArrayList<Item> scantransport = new ArrayList<Item>();
                if (!item.isActive() && !item.isCompleted() && item.isIsterminal()) {
                    if (verbose > 2) {
                        System.err.println("scan:" + item);
                    }
                    Clause cl = ((RCG) super.getGrammar()).getClauseForLabel(item.getPredLabel(), 0);
                    RangeBoundary l = item.getRcv().left(0);
                    RangeBoundary r = item.getRcv().right(0);
                    String terminal = "";
                    Item scanned = null;
                    if (l.getVal() <= input.size() && r.getVal() <= input.size()
                            && (r.getVal() == -1 || r.getVal() >= l.getVal())) {
                        if (!cl.getLhs().getArgs().get(0).isEpsilon()) {
                            // argument is not epsilon

                            terminal = cl.getLhs().getArgs().get(0).get(0).getName();
                            // get the boundaries of the terminal
                            if (l.getVal() > -1 && l.getVal() < input.size() && (r.getVal() == l.getVal() + 1 || r.getVal() == -1)) {
                                if (input.size() > 0 && terminal.equals(input.get(l.getVal()))) {
                                    scanned = new Item();
                                    scanned.setCompleted(true);
                                    scanned.setIsterminal(item.isIsterminal());
                                    scanned.setPredLabel(item.getPredLabel());
                                    //predblock.add(scanned);
                                    scanned.setRcv(new RangeConstraintVector(item.getRcv()));
                                    // right bound unset
                                    if (r.getVal() == -1) {
                                        scanned.getRcv().update(r, l.getVal() + 1);
                                    }
                                    scantransport.add(scanned);
                                    if (!complByLabel.containsKey(scanned.getPredLabel())) {
                                        complByLabel.put(scanned.getPredLabel(), new ArrayList<Item>());
                                    }
                                    complByLabel.get(scanned.getPredLabel()).add(scanned);
                                }
                            } else if (l.getVal() == -1 && r.getVal() == -1) {
                                // both boundaries unset
                                for (int i = 0; i < input.size(); ++i) {
                                    if (terminal.equals(input.get(i))) {
                                        scanned = new Item();
                                        scanned.setCompleted(true);
                                        scanned.setIsterminal(item.isIsterminal());
                                        scanned.setPredLabel(item.getPredLabel());
                                        //predblock.add(scanned);
                                        scanned.setRcv(new RangeConstraintVector(item.getRcv()));
                                        scanned.getRcv().update(l, i);
                                        scanned.getRcv().update(r, i + 1);
                                        scantransport.add(scanned);
                                        if (!complByLabel.containsKey(scanned.getPredLabel())) {
                                            complByLabel.put(scanned.getPredLabel(), new ArrayList<Item>());
                                        }
                                        complByLabel.get(scanned.getPredLabel()).add(scanned);
                                    }
                                }
                            }

                        }
                        // epsilon is the argument
                        else {
                            if ((l.getVal() > -1 && r.getVal() == -1) || r.getVal() == l.getVal()) {
                                scanned = new Item();
                                scanned.setCompleted(true);
                                scanned.setIsterminal(item.isIsterminal());
                                scanned.setPredLabel(item.getPredLabel());
                                //predblock.add(scanned);
                                scanned.setRcv(new RangeConstraintVector(item.getRcv()));
                                // right bound unset
                                if (r.getVal() == -1) {
                                    scanned.getRcv().update(r, l.getVal());
                                }
                                scantransport.add(scanned);
                                if (!complByLabel.containsKey(scanned.getPredLabel())) {
                                    complByLabel.put(scanned.getPredLabel(), new ArrayList<Item>());
                                }
                                complByLabel.get(scanned.getPredLabel()).add(scanned);

                            } else if (l.getVal() == -1 && r.getVal() == -1) {
                                for (int i = 0; i < input.size(); ++i) {
                                    scanned = new Item();
                                    scanned.setCompleted(true);
                                    scanned.setIsterminal(item.isIsterminal());
                                    scanned.setPredLabel(item.getPredLabel());
                                    //predblock.add(scanned);
                                    scanned.setRcv(new RangeConstraintVector(item.getRcv()));
                                    scanned.getRcv().update(l, i);
                                    scanned.getRcv().update(r, i);
                                    scantransport.add(scanned);
                                    if (!complByLabel.containsKey(scanned.getPredLabel())) {
                                        complByLabel.put(scanned.getPredLabel(), new ArrayList<Item>());
                                    }
                                    complByLabel.get(scanned.getPredLabel()).add(scanned);
                                }
                            }
                        }
                    } else {
                        System.err.println("Right boundary set (nyi): " + item);
                        System.exit(1);
                    }
                    if (scanned != null) {
                        donescan = true;
                        if (verbose > 3) {
                            System.err.println("  R:" + scantransport);
                        }
                    }
                }
            }
            scan = new HashSet<Item>();

            // complete ***************************************************************************************
            boolean donecomplete = false;
            Map<PredLabel, List<Item>> complTransport = new HashMap<PredLabel, List<Item>>();
            agendait = complete.iterator();
            while (agendait.hasNext()) {
                Item item = agendait.next();
                //if (item.active && item.dotpos < item.cl.getRhs().size()) {
                //System.err.println("item is active: " + String.valueOf(item.active));
                Predicate rhsp = item.getPredicateAtDot();
                Iterator<Item> completorit = null;
                if (complByLabel.containsKey(rhsp.getLabel())) {
                    completorit = complByLabel.get(rhsp.getLabel()).iterator();
                }
                while (completorit != null && completorit.hasNext()) {
                    Item ctor = completorit.next();
                    String signature = ctor + "###" + item;
                    //if (!complblock.contains(signature)	&& !ctor.active && ctor.completed && ctor.pl.equals(rhsp.getLabel())) {
                    if (!complblock.contains(signature)) {
                        complblock.add(signature);
                        Item compl = new Item();
                        if (verbose > 2) {
                            System.err.println("compl:" + item + " with " + ctor);
                        }
                        compl.setClause(item.getClause());

                        boolean isok = true;
                        compl.setRcv(new RangeConstraintVector(item.getRcv()));
                        for (int i = 0; isok && i < rhsp.getArity(); ++i) {
                            ArgContent lmost = rhsp.getArgs().get(i).get(0);
                            ArgContent rmost = rhsp.getArgs().get(i).get(rhsp.getArgs().get(i).size() - 1);
                            isok &= compl.getRcv().update(compl.getRcv().left(lmost), ctor.getRcv().left(i).getVal());
                            if (isok) {
                                isok &= compl.getRcv().update(compl.getRcv().right(rmost), ctor.getRcv().right(i).getVal());
                            }
                        }


                        if (isok) {
                            compl.setDotpos(item.getDotpos() + 1);
                            compl.setActive(true);
                            compl.setCompleted(false);
                            donecomplete = true;
                            if (verbose > 3) {
                                System.err.println("  R:" + compl);
                            }
                            if (compl.isDotAtEnd()) {
                                Item converted = convert(compl);
                                //transport.add(converted);
                                if (!complTransport.containsKey(converted.getPredLabel())) {
                                    complTransport.put(converted.getPredLabel(), new ArrayList<Item>());
                                }
                                complTransport.get(converted.getPredLabel()).add(converted);
                            } else {
                                transport.add(compl);
                                if (!predblock.contains(compl)) {
                                    predpred.add(compl);
                                    predblock.add(compl);
                                }
                            }
                        }
                    }
                    //}
                }
            }

            Iterator<PredLabel> compltit = complTransport.keySet().iterator();
            while (compltit.hasNext()) {
                PredLabel pl = compltit.next();
                if (!complByLabel.containsKey(pl)) {
                    complByLabel.put(pl, new ArrayList<Item>());
                }
                complByLabel.get(pl).addAll(complTransport.get(pl));
            }
            complete.addAll(transport);
            transport = new ArrayList<Item>();

            found = complByLabel.containsKey(skey) && complByLabel.get(skey).contains(goalitem);
            stop = !donepredict && !donescan && !donecomplete;

        }

        // no success: no operation has yielded new items in the last iteration
        if (verbose > 3) {
            if (stop) {
                System.err.println("halted after " + String.valueOf(count) + " iterations.\n");//Agenda state:");
				/*Iterator<Item> itemit = agenda.iterator();
				while(itemit.hasNext()) {
					System.err.println(itemit.next());
				}*/
            }
        }

        System.err.println(String.valueOf(found));
        return found;
    }

    private Item convert(Item item) {
        Item conv = null;
        if (verbose > 2) {
            System.err.println("conv:" + item);
        }
        Predicate lhs = item.getClause().getLhs();
        conv = new Item();
        conv.setPredLabel(lhs.getLabel());
        conv.setCompleted(true);
        conv.setActive(false);
        conv.setRcv(new RangeConstraintVector(lhs.getArity()));
        for (int j = 0; j < lhs.getArity(); ++j) {
            Argument jth = lhs.getArgs().get(j);
            ArgContent lmostargcont = jth.get(0);
            ArgContent rmostargcont = jth.get(jth.size() - 1);
            conv.getRcv().setKey(j, j);
            conv.getRcv().bound.set(j * 2, item.getRcv().left(lmostargcont));
            conv.getRcv().bound.set(j * 2 + 1, item.getRcv().right(rmostargcont));
        }
        conv.getRcv().resetNumbering();
        if (verbose > 3) {
            System.err.println("  R:" + conv);
        }
        return conv;
    }

    public String printForest() {
        System.err.println("Not yet implemented.");
        return "";
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

    public ForestExtractorInitializer getForestExtractorInitializer() {
        return null;
    }

    /*
     * just for testing
     */
    public static void main(String[] args) throws Exception {
        //RCGReader rcggr  = new TextRCGReader(new File("/home/wmaier/Desktop/rcg.txt"));
        //RCG g = rcggr.getRCG();
        RCG g = new RCG();
        g.copylanguage();
        System.err.println(g.toString());
        g.termtransform();
        System.err.println(g.toString());
        Tokenizer tok = new BuiltinTokenizer();
        tok.setSentence("a a a a b b b b a a a a b b b b");
        List<Word> input = tok.tokenize();
        RCGParser p = new RCGParserEarleyNoTerm(g);
        p.parseSentence(true, input);
    }


}
