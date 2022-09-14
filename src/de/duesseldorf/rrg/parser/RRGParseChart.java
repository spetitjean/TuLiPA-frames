/*
 *  File RRGParseChart.java
 *
 *  Authors:
 *     David Arps <david.arps@hhu.de
 *
 *  Copyright:
 *     David Arps, 2018
 *
 *  This file is part of the TuLiPA system
 *     https://github.com/spetitjean/TuLiPA-frames
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
package de.duesseldorf.rrg.parser;

import de.duesseldorf.rrg.RRGNode;

import java.util.*;

public class RRGParseChart {

    // map start index to Parse Items to their backpointers
    private Map<Integer, Map<RRGParseItem, Backpointer>> chart;
    private int sentencelength;
    private String axiom;

    public RRGParseChart(int sentencelength, String axiom) {

        this.sentencelength = sentencelength;
        this.axiom = (null == axiom) ? "" : axiom;
        // chart = new HashMap<RRGTree, HashMap<RRGNode, HashMap<Integer,
        // HashMap<Integer, HashMap<Boolean, HashSet<Gap>>>>>>();
        chart = new HashMap<>();
        for (int i = 0; i <= sentencelength; i++) {
            chart.put(i, new HashMap<>());
        }
    }

    public boolean containsItem(RRGParseItem item) {
        int startpos = item.startPos();
        System.out.println(
                "Simple...chart.containsItem() is not tested yet and might be wrong in some cases");
        return chart.get(startpos).containsKey(item);
    }

    /**
     * @return The Set of all goal items in the chart if the conditions are
     * met:<br>
     * - start = 0, end = sentencelength<br>
     * - ws is false<br>
     * - in TOP position in an STD root node<br>
     * - axiom fits
     */
    public Set<RRGParseItem> retrieveGoalItems() {
        Set<RRGParseItem> goals = new HashSet<>();
        for (RRGParseItem item : chart.get(0).keySet()) {
            boolean goalReqsFromItem = item.getEnd() == sentencelength && // end=n
                    // no more ws
                    !item.getwsflag() && item.getGaps().isEmpty()
                    // TOP position
                    && item.getEqClass().isTopClass()
                    // in a root
                    && item.getEqClass().isRoot()
                    // in an STD node
                    && RRGNode.RRGNodeType.STD == item.getEqClass().type
                    // no backpointers
                    && null == item.getGenwrappingjumpback();
            boolean axiomFits = axiom.isEmpty()
                    || item.getEqClass().cat.equals(axiom);
            if (goalReqsFromItem) {
                if (axiomFits) {
                    goals.add(item);
                } else {
                    System.out.println(
                            "item not taken as goal item because axiom did not fit: "
                                    + item);

                }
            }
        }
        return goals;
    }

    /**
     * @return A Set of the backpointers of item, i.e. a Set of all sets of
     * items that created the item.
     */
    public Backpointer getBackPointers(RRGParseItem item) {
        return chart.get(item.startPos()).get(item);
    }

    /**
     * @param model     find items in the chart that match the template given by
     *                  model. To construct the template, equip the item with
     *                  concrete models or to leave values unspecified <br>
     *                  - give null for {@code tree}, {@code node}, {@code nodePos},
     *                  {@code gaps}, {@code wsflag} <br>
     *                  - give -2 for {@code start}, {@code end}
     * @param gapSubSet are the gaps in the model only a subset of the gaps in the
     *                  item we look for?
     * @return
     */
    public Set<RRGParseItem> findUnderspecifiedItem(RRGParseItem model,
                                                    boolean gapSubSet) {
        Set<RRGParseItem> result = new HashSet<>();

        // collect all the items that might fit the model
        // first find out in which area of the chart to look
        Collection<RRGParseItem> toCheck = new HashSet<>();
        int startboundary = -2 == model.startPos() ? 0 : model.startPos();
        int endboundary = -2 == model.startPos() ? chart.size() - 1
                : startboundary;

        // then, look up in the chart
        for (int i = startboundary; i <= endboundary; i++) {
            toCheck.addAll(chart.get(i).keySet());
        }

        // this needs to be refactored!
        for (RRGParseItem s : toCheck) {
            boolean endCheck = -2 == model.getEnd()
                    || model.getEnd() == s.getEnd();
            if (endCheck) {
                boolean eqCheck = null == model.getEqClass()
                        || model.getEqClass().copyClass().equals(s.getEqClass().copyClass());
                if (eqCheck) {
                            // several cases: 1. no gaps given - gaps = null. 2.
                            // gaps given, equal to the gaps we look for
                            // (boolean is false), 3. gaps given, subset of the
                            // gaps we look for (boolean is true)

                            // case 1
                            boolean gapCheck = null == model.getGaps();
                            if (!gapCheck) {
                                // case 2
                                if (gapSubSet) {
                                    // case 3
                                    gapCheck = s.getGaps()
                                            .containsAll(model.getGaps());
                                    // System.out.print(gapCheck);
                                    // System.out.println("yay: "
                                    // + ((RRGParseItem) s).getGaps()
                                    // + model.getGaps());
                                } else {
                                    gapCheck = model.getGaps().equals(
                                            s.getGaps());
                                }
                            }

                            if (gapCheck) {
                                model
                                        .getwsflag();
                                boolean wsCheck = ((Boolean) model.getwsflag()).equals(
                                s.getwsflag());
                                if (wsCheck) {
                                    result.add(s);
                                }
                            }
                }
            }

        }
        return result;
    }

    /**
     * adds an item to the chart if it is not already in there.
     *
     * @param consequent  the item that should be added to the chart.
     * @param antecedents the antecedents from which this item was created.
     * @return true if the RRGParseItem was not already in the chart
     */
    public boolean addItem(RRGParseItem consequent, Operation operation,
                           RRGParseItem... antecedents) {
        Set<RRGParseItem> antes;
        if (0 < antecedents.length) {
            antes = new HashSet<>(Arrays.asList(antecedents));
        } else {
            antes = new HashSet<>();
        }
        int startpos = consequent.startPos();

        // was the item in the chart before?

        boolean alreadythere = chart.get(startpos).containsKey(consequent);
        if (alreadythere) {
            // just put the additional backpointers
            boolean antesAlreadyThere = null != chart.get(startpos)
                    .get(consequent)
                    && chart.get(startpos).get(consequent)
                    .getAntecedents(operation).contains(antes);
            if (!antesAlreadyThere) {
                chart.get(startpos).get(consequent).addToBackpointer(operation,
                        antes);
            } /*
             * else {
             * System.out
             * .println("equality! " + operation + consequent + antes);
             * }
             */
        } else {
            // add the consequent and a fresh set of backpointers
            Backpointer backpointer = new Backpointer();
            backpointer.addToBackpointer(operation, antes);
            chart.get(startpos).put(consequent, backpointer);
        }
        return !alreadythere;
    }

    public int computeSize() {
        int result = 0;
        for (Map.Entry<Integer, Map<RRGParseItem, Backpointer>> startingPos : chart
                .entrySet()) {
            result += startingPos.getValue().keySet().size();
        }
        return result;
    }

    @Override
    public String toString() {
        String alphaOmega = "----------------------------------------------------------------------\n----------------------------------------------------------------------";
        StringBuilder sb = new StringBuilder("Printing chart\n");
        sb.append(alphaOmega);
        for (Integer i = 0; i < chart.size(); i++) {
            sb.append("\nstart index ").append(i).append("\n");
            // print the items
            for (Map.Entry<RRGParseItem, Backpointer> chartEntry : chart.get(i)
                    .entrySet()) {
                sb.append(chartEntry.getKey().toString());

                // and their backpointers
                sb.append("\n\t");
                sb.append(chartEntry.getValue().toString());
                sb.append("\n");

            }
        }
        sb.append("size of the chart: ").append(computeSize()).append(" items.");
        sb.append(alphaOmega);
        return sb.toString();
    }
}
