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

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import de.duesseldorf.rrg.RRGNode.RRGNodeType;

public class RRGParseChart {

    // map start index to Parse Items to their backpointers
    private Map<Integer, Map<RRGParseItem, Backpointer>> chart;
    private int sentencelength;

    public RRGParseChart(int sentencelength) {
        this.sentencelength = sentencelength;
        // chart = new HashMap<RRGTree, HashMap<RRGNode, HashMap<Integer,
        // HashMap<Integer, HashMap<Boolean, HashSet<Gap>>>>>>();
        chart = new HashMap<Integer, Map<RRGParseItem, Backpointer>>();
        for (int i = 0; i <= sentencelength; i++) {
            chart.put(i, new HashMap<RRGParseItem, Backpointer>());
        }
    }

    public boolean containsItem(RRGParseItem item) {
        int startpos = item.startPos();
        System.out.println(
                "Simple...chart.containsItem() is not tested yet and might be wrong in some cases");
        return chart.get(startpos).containsKey(item);
    }

    /**
     * 
     * @return The Set of all goal items in the chart if the conditions are
     *         met:<br>
     *         - start = 0, end = sentencelength<br>
     *         - ws is false<br>
     *         - in TOP position in a STD root node
     */
    public Set<RRGParseItem> retrieveGoalItems() {
        Set<RRGParseItem> goals = new HashSet<RRGParseItem>();
        for (RRGParseItem item : chart.get(0).keySet()) {
            RRGParseItem rrgitem = (RRGParseItem) item;
            boolean goalReq = rrgitem.getEnd() == sentencelength && // end=n
            // no more ws
                    rrgitem.getwsflag() == false && rrgitem.getGaps().isEmpty()
                    // TOP position
                    && rrgitem.getNodePos().equals(RRGParseItem.NodePos.TOP)
                    // in a root
                    && rrgitem.getNode().getGornaddress().mother() == null
                    // in a STD node
                    && rrgitem.getNode().getType().equals(RRGNodeType.STD);
            if (goalReq) {
                goals.add(rrgitem);
            }
        }
        System.out.println("found goal items: " + goals);
        return goals;
    }

    /**
     * 
     * @param item
     * @return A Set of the backpointers of item, i.e. a Set of all sets of
     *         items that created the item.
     */
    public Backpointer getBackPointers(RRGParseItem item) {
        return chart.get(item.startPos()).get(item);
    }

    /**
     * 
     * @param model
     *            find items in the chart that match the template given by
     *            model. To construct the template, equip the item with
     *            concrete models or to leave values unspecified <br>
     *            - give null for {@code tree}, {@code node}, {@code nodePos},
     *            {@code gaps}, {@code wsflag} <br>
     *            - give -2 for {@code start}, {@code end}
     * @param gapSubSet
     *            are the gaps in the model only a subset of the gaps in the
     *            item we look for?
     * @return
     */
    public Set<RRGParseItem> findUnderspecifiedItem(RRGParseItem model,
            boolean gapSubSet) {
        Set<RRGParseItem> result = new HashSet<RRGParseItem>();

        // collect all the items that might fit the model
        // first find out in which area of the chart to look
        Set<RRGParseItem> toCheck = new HashSet<RRGParseItem>();
        int startboundary = model.startPos() == -2 ? 0 : model.startPos();
        int endboundary = model.startPos() == -2 ? chart.size() - 1
                : startboundary;

        // then, look up in the chart
        for (int i = startboundary; i <= endboundary; i++) {
            toCheck.addAll(chart.get(i).keySet());
        }

        // this needs to be refactored!
        for (RRGParseItem s : toCheck) {
            boolean endCheck = model.getEnd() == -2
                    || model.getEnd() == ((RRGParseItem) s).getEnd();
            if (endCheck) {
                boolean treeCheck = model.getTree() == null
                        || model.getTree().equals(((RRGParseItem) s).getTree());
                if (treeCheck) {
                    boolean nodeCheck = model.getNode() == null || model
                            .getNode().equals(((RRGParseItem) s).getNode());
                    if (nodeCheck) {
                        boolean posCheck = model.getNodePos() == null
                                || model.getNodePos().equals(
                                        ((RRGParseItem) s).getNodePos());
                        if (posCheck) {
                            // several cases: 1. no gaps given - gaps = null. 2.
                            // gaps given, equal to the gaps we look for
                            // (boolean is false), 3. gaps given, subset of the
                            // gaps we look for (boolean is true)

                            // case 1
                            boolean gapCheck = model.getGaps() == null;
                            if (!gapCheck) {
                                // case 2
                                if (!gapSubSet) {
                                    gapCheck = model.getGaps().equals(
                                            ((RRGParseItem) s).getGaps());
                                } else {
                                    // case 3
                                    gapCheck = ((RRGParseItem) s).getGaps()
                                            .containsAll(model.getGaps());
                                    // System.out.print(gapCheck);
                                    // System.out.println("yay: "
                                    // + ((RRGParseItem) s).getGaps()
                                    // + model.getGaps());
                                }
                            }

                            if (gapCheck) {
                                boolean wsCheck = (Boolean) model
                                        .getwsflag() == null
                                        || ((Boolean) model.getwsflag()).equals(
                                                ((RRGParseItem) s).getwsflag());
                                if (wsCheck) {
                                    result.add((RRGParseItem) s);
                                }
                            }
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
     * @param consequent
     *            the item that should be added to the chart.
     * @param antecedents
     *            the antecedents from which this item was created.
     * @return true if the RRGParseItem was not already in the chart
     */
    public boolean addItem(RRGParseItem consequent, Operation operation,
            RRGParseItem... antecedents) {
        System.out.println("chart.addItem: " + consequent);
        List<RRGParseItem> antes;
        if (antecedents.length > 0) {
            antes = new LinkedList<RRGParseItem>(Arrays.asList(antecedents));
        } else {
            antes = new LinkedList<RRGParseItem>();
        }
        int startpos = consequent.startPos();

        // was the item in the chart before?
        boolean alreadythere = chart.get(startpos).containsKey(consequent);
        if (alreadythere) {
            // just put the additional backpointers
            System.out.println("item already there, just put backpointers");
            chart.get(startpos).get(consequent).addToBackpointer(operation,
                    antes);
        } else {
            System.out.println("item not there yet");
            // add the consequent and a fresh set of backpointers
            Backpointer backpointer = new Backpointer();
            backpointer.addToBackpointer(operation, antes);
            chart.get(startpos).put(consequent, backpointer);
        }
        return !alreadythere;
    }

    public int computeSize() {
        int result = 0;
        for (Entry<Integer, Map<RRGParseItem, Backpointer>> startingPos : chart
                .entrySet()) {
            result += startingPos.getValue().keySet().size();
        }
        return result;
    }

    @Override
    public String toString() {
        String alphaOmega = "----------------------------------------------------------------------\n----------------------------------------------------------------------";
        StringBuffer sb = new StringBuffer("Printing chart\n");
        sb.append(alphaOmega);
        for (Integer i = 0; i < chart.size(); i++) {
            if (i < chart.size()) {
                sb.append("\nstart index " + i + "\n");
            }
            // print the items
            for (Entry<RRGParseItem, Backpointer> chartEntry : chart.get(i)
                    .entrySet()) {
                sb.append(chartEntry.getKey().toString());

                // and their backpointers
                sb.append("\n\t");
                sb.append(chartEntry.getValue().toString());
                sb.append("\n");

            }
        }
        sb.append("size of the chart: " + computeSize() + " items.");
        sb.append(alphaOmega);
        return sb.toString();
    }
}