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
        RRGParseItem jumpBackItem = (leftItem.getGenwrappingjumpback() != null) ?
                leftItem.getGenwrappingjumpback() :
                rightItem.getGenwrappingjumpback();
        if (leftItem.getGenwrappingjumpback() != null && rightItem.getGenwrappingjumpback() != null) {
            System.out.println("something strange: two jumpback items not zero during CSis: l: " + leftItem + ", r: " + rightItem);
        }
        return new RRGParseItem.Builder().tree(rightItem.getTree().getInstance())
                .node(rightItem.getNode().copyNode()).nodepos(RRGParseItem.NodePos.TOP)
                .start(leftItem.startPos()).end(rightItem.getEnd()).gaps(gaps)
                .ws(false)
                .genwrappingjumpback(jumpBackItem).build();
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
        RRGNode mothernode = currentItem.getTree().findNode(motheraddress).copyNode();
        boolean newwsflag = mothernode.getType().equals(RRGNodeType.DDAUGHTER);
        Set<RRGParseItem> backpointers = new HashSet<RRGParseItem>();
        backpointers.add(currentItem);

        // Debug
        // System.out.println(motheraddress + " is the mother of "
        // + currentItem.getNode().getGornaddress());
        return new RRGParseItem.Builder().tree(currentItem.getTree().getInstance())
                .node(mothernode).nodepos(RRGParseItem.NodePos.BOT)
                .start(currentItem.startPos()).end(currentItem.getEnd())
                .gaps(currentItem.getGaps()).ws(newwsflag)
                .genwrappingjumpback(currentItem.getGenwrappingjumpback()).build();
    }

    /**
     * Assuming that {@code CurrentItem} is a fitting antecedent, apply the
     * noLeftSister deduction rule.
     *
     * @param currentItem
     * @return
     */
    public RRGParseItem applyNoLeftSister(RRGParseItem currentItem) {
        return new RRGParseItem.Builder().tree(currentItem.getTree().getInstance())
                .node(currentItem.getNode().copyNode()).nodepos(RRGParseItem.NodePos.TOP)
                .start(currentItem.startPos()).end(currentItem.getEnd())
                .gaps(currentItem.getGaps()).ws(currentItem.getwsflag())
                .genwrappingjumpback(currentItem.getGenwrappingjumpback())
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
        RRGParseItem jumpBackItem = (targetSister.getGenwrappingjumpback() != null) ?
                targetSister.getGenwrappingjumpback() :
                auxTreeRoot.getGenwrappingjumpback();
        if (targetSister.getGenwrappingjumpback() != null && auxTreeRoot.getGenwrappingjumpback() != null) {
            System.out.println("something strange: two jumpback items not zero during LeftSisadj: t: " + targetSister + ", a: " + auxTreeRoot);
        }
        return new RRGParseItem.Builder().tree(targetSister.getTree().getInstance())
                .node(targetSister.getNode().copyNode()).nodepos(targetSister.getNodePos())
                .start(auxTreeRoot.startPos()).end(targetSister.getEnd())
                .gaps(gaps).ws(false)
                .genwrappingjumpback(jumpBackItem).build();
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
        RRGParseItem jumpBackItem = (target.getGenwrappingjumpback() != null) ?
                target.getGenwrappingjumpback() :
                auxTreeRoot.getGenwrappingjumpback();
        if (target.getGenwrappingjumpback() != null && auxTreeRoot.getGenwrappingjumpback() != null) {
            System.out.println("something strange: two jumpback items not zero during LeftSisadj: t: " + target + ", a: " + auxTreeRoot);
        }
        // System.out.print(target.getTree());
        return new RRGParseItem.Builder().tree(target.getTree().getInstance())
                .node(target.getNode().copyNode()).nodepos(target.getNodePos())
                .start(target.startPos()).end(auxTreeRoot.getEnd()).gaps(gaps)
                .ws(target.getwsflag())
                .genwrappingjumpback(jumpBackItem).build();
    }

    public RRGParseItem applyGeneralizedCompleteWrapping(RRGParseItem targetItem, RRGParseItem fillerddaughterItem, Gap gap) {
        return applyCW(targetItem, fillerddaughterItem, gap, true);
    }

    public RRGParseItem applyCompleteWrapping(RRGParseItem targetRootItem,
                                              RRGParseItem fillerddaughterItem, Gap gap) {
        return applyCW(targetRootItem, fillerddaughterItem, gap, false);
    }

    private RRGParseItem applyCW(RRGParseItem targetItem, RRGParseItem fillerddaughterItem, Gap gap, boolean generalized) {
        Set<Gap> gaps = new HashSet<Gap>(targetItem.getGaps());
        gaps.remove(gap);
        gaps.addAll(fillerddaughterItem.getGaps());
        RRGParseItem.Builder builder = new RRGParseItem.Builder().tree(fillerddaughterItem.getTree().getInstance())
                .node(fillerddaughterItem.getNode().copyNode())
                .nodepos(fillerddaughterItem.getNodePos())
                .start(targetItem.startPos()).end(targetItem.getEnd())
                .gaps(gaps).ws(false);
        if (generalized) {
            builder = builder.genwrappingjumpback(targetItem);
        } else {
            builder.genwrappingjumpback(targetItem.getGenwrappingjumpback());
        }
        return builder.build();
    }

    public RRGParseItem applyJumpBackAfterGenWrapping(RRGParseItem currentItem) {
        RRGParseItem jumpBackItem = currentItem.getGenwrappingjumpback();
        Set<Gap> gaps = new HashSet<>(currentItem.getGaps());
        return new RRGParseItem.Builder().start(currentItem.startPos())
                .end(currentItem.getEnd())
                .tree(jumpBackItem.getTree())
                .node(jumpBackItem.getNode())
                .nodepos(RRGParseItem.NodePos.TOP)
                .gaps(gaps)
                .ws(false).build();
    }
}
