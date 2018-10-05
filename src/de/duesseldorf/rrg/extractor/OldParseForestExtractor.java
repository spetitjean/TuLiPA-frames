package de.duesseldorf.rrg.extractor;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.duesseldorf.rrg.RRGParseTree;
import de.duesseldorf.rrg.parser.Operation;
import de.duesseldorf.rrg.parser.ParseItem;
import de.duesseldorf.rrg.parser.SimpleRRGParseChart;
import de.duesseldorf.rrg.parser.SimpleRRGParseItem;

/*
 *  File ParseForestExtractor.java
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
public class OldParseForestExtractor {

    private SimpleRRGParseChart chart;
    private List<String> tokSentence;
    private Set<RRGParseTree> parseTrees;

    public OldParseForestExtractor(SimpleRRGParseChart chart,
            List<String> toksentence) {
        this.chart = chart;
        this.tokSentence = toksentence;
    }

    public Set<RRGParseTree> extractParseTrees() {
        this.parseTrees = new HashSet<RRGParseTree>();
        Set<ParseItem> goals = chart.retrieveGoalItems();
        if (goals.isEmpty()) {
            System.out.println("no goal items!");
        } else {
            System.out.println("Goal items: " + chart.retrieveGoalItems());
            for (ParseItem goal : goals) {
                parseTrees.add(new RRGParseTree(
                        ((SimpleRRGParseItem) goal).getTree()));
                extract((SimpleRRGParseItem) goal);
            }
        }

        return parseTrees;

    }

    private void extract(SimpleRRGParseItem consequent) {
        extractNLS(consequent);
        extractMoveUp(consequent);
        extractCombineSisters(consequent);
        extractSubstitution(consequent);
        extractSisterAdjunction(consequent);
    }

    /**
     * @param consequent
     */
    private void extractSisterAdjunction(SimpleRRGParseItem consequent) {
        // left-sister adjunction
        Set<List<ParseItem>> leftsisadjSet = chart.getBackPointers(consequent)
                .getAntecedents(Operation.LEFTADJOIN);
        if (leftsisadjSet.size() > 1) {
            System.out.println(
                    "sth to do in left sister adjunction extraction for modifying multiple parse results!");
        }
        for (List<ParseItem> antecedentList : leftsisadjSet) {
            // items in the list of antecedents are ordered from left to right
            SimpleRRGParseItem auxRootItem = (SimpleRRGParseItem) antecedentList
                    .get(0);
            SimpleRRGParseItem targetSisterItem = (SimpleRRGParseItem) antecedentList
                    .get(1);
            System.out.println("TODOleftSisAdj " + auxRootItem.toString()
                    + targetSisterItem);
            for (RRGParseTree rrgParseTree : parseTrees) {
                // probably the wrong way to ask for an id
                // IDEA: make the containsElemTrees compare if the GornAddresses
                // of the target and the id in the parse Tree match. If they do,
                // addSubTree
                if (rrgParseTree.containsElementaryTree(
                        targetSisterItem.getTree().getId())) {
                    rrgParseTree.addSubTree(
                            targetSisterItem.getNode().getGornaddress(),
                            auxRootItem.getTree(), 0);
                }
            }
        }
    }

    /**
     * TODO: For a start, I assume that there is only one substitution item,
     * which is quite dumb. 16-04-2018
     * 
     * @param consequent
     */
    private void extractSubstitution(SimpleRRGParseItem consequent) {
        // substitute
        Set<List<ParseItem>> substituteSet = chart.getBackPointers(consequent)
                .getAntecedents(Operation.SUBSTITUTE);
        for (List<ParseItem> antecedentList : substituteSet) {
            if (substituteSet.size() > 1) {
                System.out.println("sth wrong in substitution extraction!");
            } else {
                SimpleRRGParseItem substTreeRootItem = (SimpleRRGParseItem) antecedentList
                        .get(0);
                for (RRGParseTree rrgParseTree : parseTrees) {
                    if (rrgParseTree.idequals(consequent.getTree())) {
                        System.out.println("SUBST! " + substTreeRootItem);
                        rrgParseTree
                                .findNode(consequent.getNode().getGornaddress())
                                .nodeUnification(substTreeRootItem.getNode());
                        extract(substTreeRootItem);
                    }
                }
            }
        }
    }

    /**
     * @param consequent
     */
    private void extractCombineSisters(SimpleRRGParseItem consequent) {
        Set<List<ParseItem>> combineSisSet = chart.getBackPointers(consequent)
                .getAntecedents(Operation.COMBINESIS);
        if (combineSisSet == null) {
            // if (combineSisSet.size() > 1) {
            System.out.println("something wrong with combineSis extraction!");
            // }
        }
        for (List<ParseItem> antecedentList : combineSisSet) {
            for (ParseItem antecedent : antecedentList) {
                // System.out.println("COMBINESIS cons: " + consequent);
                // System.out.println("COMBINESIS antecedent: " + antecedent);
                extract((SimpleRRGParseItem) antecedent);
            }
        }
    }

    /**
     * 
     * we assume that there is exactly one way to create an item using the
     * MOVEUP rule
     * 
     * @param consequent
     */
    private void extractMoveUp(SimpleRRGParseItem consequent) {
        Set<List<ParseItem>> moveUpSet = chart.getBackPointers(consequent)
                .getAntecedents(Operation.MOVEUP);
        for (List<ParseItem> antecedentList : moveUpSet) {
            // recursive call with the item that created consequent
            SimpleRRGParseItem antecedent = (SimpleRRGParseItem) antecedentList
                    .get(0);
            // System.out.println("MOVEUP cons: " + consequent);
            // System.out.println("MOVEUP antecedent: " + antecedent);
            extract(antecedent);
        }
    }

    /**
     * 
     * we assume that there is exactly one way to create an item using the
     * NLS rule
     * 
     * @param consequent
     */
    private void extractNLS(SimpleRRGParseItem consequent) {
        Set<List<ParseItem>> nlsset = chart.getBackPointers(consequent)
                .getAntecedents(Operation.NLS);

        for (List<ParseItem> antecedentList : nlsset) {
            // recursive call with the item that created consequent
            SimpleRRGParseItem antecedent = (SimpleRRGParseItem) antecedentList
                    .get(0);
            // System.out.println("NLS cons: " + consequent);
            // System.out.println("NLS antecedent: " + antecedent);
            extract(antecedent);
        }
    }
}