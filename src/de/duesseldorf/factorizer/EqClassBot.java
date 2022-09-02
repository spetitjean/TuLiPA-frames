package de.duesseldorf.factorizer;
/*
 *  File EqClass.java
 *
 *  Authors:
 *     Julia Block <julia.block@hhu.de>
 *
 *  Copyright:
 *     Julia Block, 2022
 *
 * Last modified:
 *     2022
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


import de.duesseldorf.frames.Fs;
import de.duesseldorf.rrg.RRGNode.RRGNodeType;
import de.duesseldorf.rrg.RRGNode;
import de.duesseldorf.rrg.RRGTree;
import de.duesseldorf.util.GornAddress;

import java.util.*;

/**
 * Equivalence class that are equal in daughters
 */
public class EqClassBot {

    private ArrayList<EqClassBot> daughterEQClasses;

    private ArrayList<EqClassTop> topClasses = new ArrayList<>();

    public Map<GornAddress, RRGTree> factorizedTrees = new HashMap<>();

    public int numDaughters;

    public String cat;

    public RRGNodeType type;

    private String id;

    private Fs fs;

    private boolean root = false;

    public EqClassBot(ArrayList<EqClassBot> daughters, String cat, RRGNodeType type, String id, Fs fs){
        this.id = id;
        daughterEQClasses = daughters;
        this.cat = cat;
        if(daughters.isEmpty()) {
            numDaughters = 0;
        } else {
            numDaughters = daughters.size();
        }
        this.type = type;
        this.fs = fs;
    }

    public boolean isRoot(){return root;}

    public void setRoot(boolean root){this.root = root;}

    public String getId() {return id;}

    public void setId(String id){this.id = id;}

    public ArrayList<EqClassBot> getDaughterEQClasses() {
        return this.daughterEQClasses;
    }

    public void addTopClasses(EqClassTop topClass) {
        topClasses.add(topClass);
    }

    public ArrayList<EqClassTop> getTopClasses() {
        return this.topClasses;
    }

    public void add(GornAddress address, RRGTree rootTree) {
        factorizedTrees.put(address, rootTree);
    }

    public void add(EqClassTop topClass){
        topClasses.add(topClass);
    }

    public boolean isBottomClass(){return true;}


    /**
     * Check if nodes belongs in this eq class, has to have right category, daughters and type
     * @param node node to be checked
     * @param daughters daughters of node
     * @return true if node belongs to this Eq class
     */
    public boolean belongs(RRGNode node, ArrayList<EqClassBot> daughters) {
        if (daughters.isEmpty()) {
            if(cat.equals(node.getCategory())
                    && daughterEQClasses.isEmpty()
                    && checkType(node.getType())) {return true;}

        } else {
            if(cat.equals(node.getCategory())
                    && daughters.equals(daughterEQClasses)
                    && checkType(node.getType())) {return true;}
        }
        return false;
    }


    private boolean checkType(RRGNodeType type) {
        if(type == this.type) {return true;}
        return false;
    }


    @Override
    public String toString() {
        String out = "{BOTTOM Cat = "+ cat + " " + this.id + ", daughters = ";
        if(numDaughters == 0) {
            out += "No Daughters \n";
        }
        int i = 1;
        for(EqClassBot daughter : daughterEQClasses) {
            out += i + ". Daughter = " + daughter.cat + " " + daughter.id + "\n";
            i++;
        }
        out += "}";
        return out;
    }

    public EqClassTop checkTopClasses(List <EqClassBot> leftSisters, GornAddress gornaddress, RRGTree tree) {
        for(EqClassTop topClass : topClasses) {
            if(topClass.belongs(leftSisters)) {
                topClass.add(gornaddress, tree);
                return topClass;
            }
        }
        EqClassTop newClass = new EqClassTop(this, "",leftSisters);
        newClass.add(gornaddress,tree);
        add(newClass);
        return newClass;
    }

    public String printTopClasses() {
        String out = "";
        int i = 1;
        for(EqClassTop topClass: topClasses) {
            out += topClass.toString();
        }
        return out;
    }

    public Fs getFs() {return fs;}
}
