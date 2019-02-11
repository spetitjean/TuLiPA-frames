package de.duesseldorf.rrg.parser;

import java.util.HashSet;
import java.util.Set;

import de.duesseldorf.rrg.RRGNode;
import de.duesseldorf.rrg.RRGNode.RRGNodeType;
import de.duesseldorf.util.GornAddress;

/*
 *  File Deducer.java
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
public class Deducer {

    /**
     * Assuming that both parameters are fitting antecedents, apply the
     * combineSisters deduction rule
     *
     * @param leftItem
     * @param rightItem
     * @return
     */
    public RRGParseItem applyCombineSisters(RRGParseItem leftItem,
            RRGParseItem rightItem) {
        Set<Gap> gaps = new HashSet<Gap>(leftItem.getGaps());
        gaps.addAll(rightItem.getGaps());
        return new RRGParseItem.Builder().tree(rightItem.getTree())
                .node(rightItem.getNode()).nodepos(RRGParseItem.NodePos.TOP)
                .start(leftItem.startPos()).end(rightItem.getEnd()).gaps(gaps)
                .ws(false).build();
    }

    /**
     * Assuming that the {@code currentItem} is a fitting antecedent, apply the
     * moveUp deduction rule
     *
     * @param currentItem
     * @return
     */
    public RRGParseItem applyMoveUp(RRGParseItem currentItem) {
        GornAddress motheraddress = currentItem.getNode().getGornaddress()
                .mother();
        RRGNode mothernode = currentItem.getTree().findNode(motheraddress);
        boolean newwsflag = mothernode.getType().equals(RRGNodeType.DDAUGHTER);
        Set<RRGParseItem> backpointers = new HashSet<RRGParseItem>();
        backpointers.add(currentItem);

        // Debug
        // System.out.println(motheraddress + " is the mother of "
        // + currentItem.getNode().getGornaddress());
        return new RRGParseItem.Builder().tree(currentItem.getTree())
                .node(mothernode).nodepos(RRGParseItem.NodePos.BOT)
                .start(currentItem.startPos()).end(currentItem.getEnd())
                .gaps(currentItem.getGaps()).ws(newwsflag).build();
    }

    /**
     * Assuming that {@code CurrentItem} is a fitting antecedent, apply the
     * noLeftSister deduction rule.
     *
     * @param currentItem
     * @return
     */
    public RRGParseItem applyNoLeftSister(RRGParseItem currentItem) {
        return new RRGParseItem.Builder().tree(currentItem.getTree())
                .node(currentItem.getNode()).nodepos(RRGParseItem.NodePos.TOP)
                .start(currentItem.startPos()).end(currentItem.getEnd())
                .gaps(currentItem.getGaps()).ws(currentItem.getwsflag())
                .build();
    }

    /**
     * Creates a new item by applying the left-adjoin deduction rule. The tree
     * in {@code auxTreeRoot} is sis-adjoined to the left of the
     * {@code targetSister} node.
     *
     * @param targetSister
     * @param auxTreeRoot
     * @return
     */
    public RRGParseItem applyLeftAdjoin(RRGParseItem targetSister,
            RRGParseItem auxTreeRoot) {
        // create the list of gaps of the consequent
        Set<Gap> gaps = new HashSet<Gap>(auxTreeRoot.getGaps());
        gaps.addAll(targetSister.getGaps());

        return new RRGParseItem.Builder().tree(targetSister.getTree())
                .node(targetSister.getNode()).nodepos(targetSister.getNodePos())
                .start(auxTreeRoot.startPos()).end(targetSister.getEnd())
                .gaps(gaps).ws(false).build();
    }

    /**
     * Creates a new item by applying the right-adjoin deduction rule. The tree
     * in {@code auxTreeRoot} is sis-adjoined to the right of the
     * {@code targetSister} node.
     *
     * @param target
     * @param auxTreeRoot
     * @return
     */
    public RRGParseItem applyRightAdjoin(RRGParseItem target,
            RRGParseItem auxTreeRoot) {
        // create the list of gaps of the consequent
        Set<Gap> gaps = new HashSet<Gap>(target.getGaps());
        gaps.addAll(auxTreeRoot.getGaps());

        // System.out.print(target.getTree());
        return new RRGParseItem.Builder().tree(target.getTree())
                .node(target.getNode()).nodepos(target.getNodePos())
                .start(target.startPos()).end(auxTreeRoot.getEnd()).gaps(gaps)
                .ws(target.getwsflag()).build();
    }

    public RRGParseItem applyCompleteWrapping(RRGParseItem targetRootItem,
            RRGParseItem fillerddaughterItem, Gap gap) {
        Set<Gap> gaps = new HashSet<Gap>(targetRootItem.getGaps());
        gaps.remove(gap);
        gaps.addAll(fillerddaughterItem.getGaps());
        return new RRGParseItem.Builder().tree(fillerddaughterItem.getTree())
                .node(fillerddaughterItem.getNode())
                .nodepos(fillerddaughterItem.getNodePos())
                .start(targetRootItem.startPos()).end(targetRootItem.getEnd())
                .gaps(gaps).ws(false).build();
    }
}
