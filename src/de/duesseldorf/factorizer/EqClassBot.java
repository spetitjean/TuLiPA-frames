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
import de.tuebingen.anchoring.NameFactory;
import de.tuebingen.tree.Node;

import java.util.*;

import static de.duesseldorf.rrg.RRGNode.RRGNodeType.STD;

/**
 * Equivalence class that are equal in daughters
 */
public class EqClassBot {

    private ArrayList<EqClassBot> daughterEQClasses;

    private ArrayList<EqClassTop> topClasses = new ArrayList<EqClassTop>();

    public Map<GornAddress, RRGTree> factorizedTrees;

    public int numDaughters;

    public String cat;

    public RRGNodeType type;

    private String id;

    private Fs fs;



    public EqClassBot(ArrayList<EqClassBot> daughters, Map<GornAddress, RRGTree> factorizedTrees,
                      String cat, RRGNodeType type, String id, Fs fs){
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
        this.factorizedTrees = factorizedTrees;
    }


    public String getId() {return id;}

    public Fs getFs() {return fs;}

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
    public boolean isTopClass(){return false;}

    /**
     *
     * @return true iff at least one Topclass has no left sisters
     */
    public boolean noLeftSisters() {
        ArrayList<Boolean> ba = new ArrayList<>();
        for(EqClassTop tc: topClasses){
            ba.add(Boolean.valueOf(tc.noLeftSisters()));
        }
        return ba.contains(Boolean.valueOf(true));
    }


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

    public EqClassTop checkTopClasses(List <EqClassBot> leftSisters, GornAddress gornaddress, RRGTree tree, NameFactory nf) {
        boolean root = gornaddress.mother() == null;
        for(EqClassTop topClass : topClasses) {
            if(topClass.belongs(leftSisters, root)) {
                topClass.add(gornaddress, tree);
                return topClass;
            }
        }
        EqClassTop newClass = new EqClassTop(this, nf.getUniqueName(),leftSisters, root);
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

    public void setDaughters(ArrayList<EqClassBot> daughters){this.daughterEQClasses = daughters;};

    public EqClassBot copyClass(){return this.copyClass(new NameFactory());};

    public EqClassBot copyClass(NameFactory nf) {
        EqClassBot newEqClass = new EqClassBot(new ArrayList<EqClassBot>(), this.factorizedTrees, this.cat, this.type, this.id, new Fs(this.fs, nf));
        ArrayList<EqClassBot> daughters = new ArrayList<EqClassBot>();
        if (this.numDaughters > 0) {
            for(EqClassBot eqClass : daughterEQClasses){
                daughters.add(eqClass.copyClass(nf));
            }
        }
        newEqClass.setDaughters(daughters);

        if(!this.topClasses.isEmpty()){
            for(EqClassTop topClass : this.getTopClasses()){
                newEqClass.add(topClass.copyClass(nf));
            }
        }
        return newEqClass;
    }

    public static class Builder<S extends Builder> {

        private String id = "";
        private ArrayList<EqClassBot> daughterEQClasses = new ArrayList<>();

        private ArrayList<EqClassTop> topClasses = new ArrayList<EqClassTop>();

        public Map<GornAddress, RRGTree> factorizedTrees = new HashMap<>();

        public int numDaughters = -1;

        public String cat = "";

        public RRGNodeType type = STD;

        private Fs fs = null;

        public Builder() {}

        public Builder(EqClassBot otherClass) {
            this.id = otherClass.getId();
            daughterEQClasses = otherClass.daughterEQClasses;
            this.topClasses = otherClass.topClasses;
            this.factorizedTrees = otherClass.factorizedTrees;
            this.numDaughters = otherClass.numDaughters;
            this.cat = otherClass.cat;
            this.type = otherClass.type;
            this.fs = otherClass.fs;
        }
        public S id(String id) {
            this.id = id;
            return (S) this;
        }

        public S daughters(List<EqClassBot> daughters) {
            this.daughterEQClasses = (ArrayList<EqClassBot>) daughters;
            this.numDaughters = daughters.size();
            return (S) this;
        }

        public S topClasses(List<EqClassTop> topClasses) {
            this.topClasses = (ArrayList<EqClassTop>) topClasses;
            return (S) this;
        }

        public S type(RRGNodeType type) {
            this.type = type;
            return (S) this;
        }

        public S cat(String cat) {
            this.cat = cat;
            return (S) this;
        }

        public S factorizedTrees(HashMap<GornAddress, RRGTree> factorizedTrees) {
            this.factorizedTrees = factorizedTrees;
            return (S) this;
        }

        public S fs(Fs fs) {
            this.fs = fs;
            return (S) this;
        }

        public EqClassBot build() {
            EqClassBot newClass = new EqClassBot(daughterEQClasses, factorizedTrees, cat, type, id, fs);
            for(EqClassTop tc : this.topClasses){
                newClass.addTopClasses(tc);
            }
            return newClass;
        }
    }
}
