package de.duesseldorf.rrg.extractor;

import de.duesseldorf.rrg.RRGParseTree;
import de.duesseldorf.rrg.parser.RRGParseItem;
import de.duesseldorf.util.GornAddress;

/**
 * File ExtractionStep.java
 * 
 * Authors:
 * David Arps <david.arps@hhu.de>
 * 
 * Copyright
 * David Arps, 2018
 * 
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
public class ExtractionStep {
    public GornAddress GAInParseTree;
    public RRGParseItem currentItem;
    public RRGParseTree currentParseTree;
    public int goToRightWhenGoingDown;

    public ExtractionStep(RRGParseItem currentItem, GornAddress GAInParseTree,
            RRGParseTree currentParseTree, int goToRightWhenGoingDown) {
        this.GAInParseTree = GAInParseTree;
        this.currentItem = currentItem;
        this.currentParseTree = currentParseTree;
        this.goToRightWhenGoingDown = goToRightWhenGoingDown;
    }

    public int getGoToRightWhenGoingDown() {
        return goToRightWhenGoingDown;
    }

    public GornAddress getGAInParseTree() {
        return this.GAInParseTree;
    }

    public RRGParseItem getCurrentItem() {
        return this.currentItem;
    }

    public RRGParseTree getCurrentParseTree() {
        return this.currentParseTree;
    }

    public String toString() {
        return getCurrentItem() + "\n" + getGAInParseTree()
                + "\nMove Right when going to daughter: "
                + goToRightWhenGoingDown + "\n" + getCurrentParseTree()
                + "\nwrapping subtrees:\n"
                + getCurrentParseTree().getWrappingSubTrees()
                + "\n-------------------------------\n";
    }
}