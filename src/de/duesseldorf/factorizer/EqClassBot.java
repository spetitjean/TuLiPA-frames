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


import de.duesseldorf.rrg.RRGNode.RRGNodeType;
import de.duesseldorf.rrg.RRGNode;
import de.duesseldorf.rrg.RRGTree;
import de.duesseldorf.util.GornAddress;
import de.tuebingen.anchoring.NameFactory;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Equivalence class that are equal in daughters
 */
@SuppressWarnings("RedundantIfStatement")
public class EqClassBot {

    private ArrayList<EqClassBot> daughterEQClasses;

    private ArrayList<EqClassTop> topClasses = new ArrayList<>();

    public Map<GornAddress, RRGTree> factorizedTrees;

    public String cat;

    public RRGNodeType type;

    private String id;




    public EqClassBot(ArrayList<EqClassBot> daughters, Map<GornAddress, RRGTree> factorizedTrees,
                      String cat, RRGNodeType type, String id){
        this.id = id;
        daughterEQClasses = daughters;
        this.cat = cat;
        this.type = type;
        this.factorizedTrees = factorizedTrees;
    }


    public String getId() {return id;}


    public ArrayList<EqClassBot> getDaughterEQClasses() {
        return this.daughterEQClasses;
    }

    public Map<EqClassBot, List<EqClassTop>> getDaughterTOPClasses() {
        Map<EqClassBot, List<EqClassTop>> daughterTops = new HashMap<>();
        for (EqClassBot daughter : getDaughterEQClasses()){
            List<EqClassTop> topClasses = daughter.getTopClasses().stream()
                    .filter(tc -> tc.getPossibleMothers().keySet().contains(this))
                    .collect(Collectors.toList());
            daughterTops.put(daughter, topClasses);
        }
        return daughterTops;
    }

    public void addTopClasses(EqClassTop topClass) {
        topClasses.add(topClass);
    }

    public List<EqClassTop> getTopClasses() {
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
        Collection<Boolean> ba = new ArrayList<>();
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
    public boolean belongs(RRGNode node, Collection<EqClassBot> daughters) {
        if (daughters.isEmpty()) {
            if(cat.equals(node.getCategory())
                    && daughterEQClasses.isEmpty()
                    && checkType(node.getType())){return true;}

        } else {
            if(cat.equals(node.getCategory())
                    && daughters.equals(daughterEQClasses)
                    && checkType(node.getType())){return true;}
        }

        return false;
    }


    boolean checkType(RRGNodeType type) {
        if(type == this.type) {return true;}
        return false;
    }

    public Map<EqClassBot, Boolean> getAllPossibleMothers() {
        Map<EqClassBot, Boolean> possMoms = new HashMap<>();
        for(EqClassTop tc : topClasses) {
            possMoms.putAll(tc.getPossibleMothers());
        }
        return possMoms;
    }



    @Override
    public String toString() {
        StringBuilder out = new StringBuilder("{BOTTOM Cat = " + cat + " " + this.id + ", daughters = ");

        int i = 1;
        for(EqClassBot daughter : daughterEQClasses) {
            out.append(i).append(". Daughter = ").append(daughter.cat).append(" ").append(daughter.id).append("\n");
            i++;
        }
        out.append("}");
        return out.toString();
    }

    /**
     * CHeck if TopClass with given parameters already exists, add if not
     * @param leftSisters
     * @param gornaddress
     * @param tree
     * @param nf
     * @return
     */
    public EqClassTop checkTopClasses(List <EqClassTop> leftSisters, GornAddress gornaddress, RRGTree tree, NameFactory nf) {
        boolean root = null == gornaddress.mother();
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
        StringBuilder out = new StringBuilder();
        int i = 1;
        for(EqClassTop topClass: topClasses) {
            out.append(topClass.toString());
        }
        return out.toString();
    }

    /**
     *
     * @param leftSis current class
     * @return all right sisters
     */
    public Set<EqClassBot> findRightSisters(EqClassBot leftSis){
        Set<EqClassBot> rightSisters = new HashSet<>();

        int[] indices = IntStream.range(0, daughterEQClasses.size())
                .filter(i -> daughterEQClasses.get(i).getTopClasses().contains(leftSis))
                .toArray();
        for(int i : indices){
            if(i<daughterEQClasses.size()-1){rightSisters.add((daughterEQClasses.get(i+1)).copyClass());}
        }
        return rightSisters;
    }

    public void setDaughters(ArrayList<EqClassBot> daughters){
        this.daughterEQClasses = daughters;
    }

    public EqClassBot copyClass(){return this.copyClass(new NameFactory());}

    public EqClassBot copyClass(NameFactory nf) {
        EqClassBot newEqClass = new EqClassBot(new ArrayList<>(), this.factorizedTrees, this.cat, this.type, this.id);
        ArrayList<EqClassBot> daughters = new ArrayList<>();
        if (!daughterEQClasses.isEmpty()) {
            for(EqClassBot eqClass : daughterEQClasses){
                daughters.add(eqClass.copyClass(nf));
            }
        }
        newEqClass.daughterEQClasses = daughters;

        if(!this.topClasses.isEmpty()){
            for(EqClassTop topClass : this.getTopClasses()){
                newEqClass.add(topClass.copyClass(nf));
            }
        }
        return newEqClass;
    }

    public boolean isRoot() {
        for(EqClassTop tc: topClasses) {
            if(tc.isRoot()){return true;}
        }
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }

        EqClassBot c = (EqClassBot) o;
        if (!( c.isBottomClass())) {
            return false;
        }

        boolean req = this.checkType(c.type)
                && this.cat.equals(c.cat);

        if(!type.equals(RRGNodeType.SUBST)){
            req = req && this.daughterEQClasses.equals(c.daughterEQClasses);
        }

        return req;
    }


    @Override
    public int hashCode() {
        return Objects.hash(daughterEQClasses, cat, type);
    }

    public static class Builder<S extends Builder> {

        private String id = "";
        private ArrayList<EqClassBot> daughterEQClasses = new ArrayList<>();

        private Iterable<EqClassTop> topClasses = new ArrayList<>();

        public Map<GornAddress, RRGTree> factorizedTrees = new HashMap<>();

        public int numDaughters = -1;

        public String cat = "";

        public RRGNodeType type = RRGNodeType.STD;


        public Builder() {}

        public Builder(EqClassBot otherClass) {
            this.id = otherClass.getId();
            daughterEQClasses = otherClass.daughterEQClasses;
            this.topClasses = otherClass.topClasses;
            this.factorizedTrees = otherClass.factorizedTrees;
            this.cat = otherClass.cat;
            this.type = otherClass.type;
        }
        public S id(String id) {
            this.id = id;
            return (S) this;
        }

        public S daughters(Collection<EqClassBot> daughters) {
            this.daughterEQClasses = (ArrayList<EqClassBot>) daughters;
            this.numDaughters = daughters.size();
            return (S) this;
        }

        public S topClasses(List<EqClassTop> topClasses) {
            this.topClasses = topClasses;
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

        public S factorizedTrees(Map<GornAddress, RRGTree> factorizedTrees) {
            this.factorizedTrees = factorizedTrees;
            return (S) this;
        }

        public EqClassBot build() {
            EqClassBot newClass = new EqClassBot(daughterEQClasses, factorizedTrees, cat, type, id);
            for(EqClassTop tc : this.topClasses){
                newClass.addTopClasses(tc);
            }
            return newClass;
        }
    }
}
