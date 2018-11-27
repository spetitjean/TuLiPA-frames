/*
 *  File ParseTreeCollection.java
 *
 *  Authors:
 *     Johannes Dellert  <johannes.dellert@sfs.uni-tuebingen.de>
 *     David Arps <david.arps@hhu.de>
 *     Simon Petitjean <petitjean@phil.hhu.de>
 *     
 *  Copyright:
 *     Johannes Dellert, 2007
 *     David Arps, 2017
 *     Simon Petitjean, 2017
 *
 *  Last modified:
 *     2017
 *
 *  This file is part of the TuLiPA-frames system
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
package de.tuebingen.gui;

import java.util.ArrayList;
import java.util.List;

import de.duesseldorf.frames.Frame;
import de.tuebingen.expander.DOMWriter;
import de.tuebingen.tag.SemLit;

public class ParseTreeCollection {
    XMLViewTree derivationTree;
    XMLViewTree derivedTree;

    ArrayList<XMLViewTree> elementaryTrees;
    ArrayList<XMLViewTree> derivationSteps;

    String semantics;
    List<SemLit> realSemantics;
    String[] specifiedSemantics;
    // List<Fs> frames;
    Frame frameSem;

    // public ParseTreeCollection(XMLViewTree derivationTree,
    // XMLViewTree derivedTree, String semantics, List<SemLit> lsl,
    // List<Fs> frames, Frame frameSem, boolean noUtool) {
    public ParseTreeCollection(XMLViewTree derivationTree,
            XMLViewTree derivedTree, String semantics, List<SemLit> lsl,
            Frame frameSem, boolean noUtool) {
        this.derivationTree = derivationTree;
        this.derivedTree = derivedTree;
        elementaryTrees = null;
        derivationSteps = null;
        this.semantics = semantics;
        // this.frames = frames;
        this.frameSem = frameSem;
        // System.out.print("noUTool is: ");
        // System.out.println(noUtool);
        // System.out.println("semantics: " + semantics);
        // System.out.println("semlit size: " + lsl.size());
        // for (SemLit semLit : lsl) {
        // System.out.println("boo" + semLit);
        // }
        // David 20-07-2017: UTool stuff doesn't work. Commenting this out makes
        // no difference in our
        // case, lsl is empty anyway
        // if (!noUtool) {
        // this.specifiedSemantics = UToolRunner.process(lsl);
        // this.semantics += "<br>Reading(s):<br>";
        // if (specifiedSemantics != null) {
        // for (String s : specifiedSemantics) {
        // this.semantics += s + "<br>";
        // }
        // }
        // }

        this.realSemantics = lsl;

    }

    public String toString() {
        return derivationTree.description;
    }

    public String toXML() {
        String out = "";
        out += "  <parse>\n    <derivation>\n";
        out += DOMWriter.elementToString(derivationTree.domNodes.get(0), 3);
        out += "    </derivation>\n    <derived>\n";
        out += DOMWriter.elementToString(derivedTree.domNodes.get(0), 3);
        out += "    </derived>\n  </parse>\n";
        return out;
    }

    public XMLViewTree getDerivationTree() {
        return derivationTree;
    }

    public XMLViewTree getDerivedTree() {
        return derivedTree;
    }

    public List<SemLit> getSemantics() {
        return realSemantics;
    }

    public String[] getSpecifiedSemantics() {
        return specifiedSemantics;
    }

    // public List<Fs> getFrames() {
    // return frames;
    // }

    public Frame getFrameSem() {
        return frameSem;
    }

}
