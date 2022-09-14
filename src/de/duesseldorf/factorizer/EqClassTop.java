package de.duesseldorf.factorizer;

import de.duesseldorf.frames.Fs;
import de.duesseldorf.rrg.RRGNode;
import de.duesseldorf.rrg.RRGTree;
import de.duesseldorf.util.GornAddress;
import de.tuebingen.anchoring.NameFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EqClassTop extends EqClassBot{

    private List<EqClassBot> leftSisters;

    //Boolean = is this class the rightmost sister
    private Map<EqClassBot, Boolean> possibleMothers = new HashMap<>();

    private boolean root;

    /**
     * Eq classes that are equal in daughters and left sisters
     * @param daughters daughter bottom EQ classes
     * @param cat category of the node
     * @param type type of the node
     * @param id
     * @param leftSisters left sister bottom Eq classes
     */
    public EqClassTop(ArrayList<EqClassBot> daughters, Map<GornAddress, RRGTree> factorizedTrees, String cat, RRGNode.RRGNodeType type, String id, Fs fs, List<EqClassBot> leftSisters, boolean root) {
        super(daughters, factorizedTrees, cat, type, id, fs);
        this.leftSisters = leftSisters;
        this.root = root;
    }

    public EqClassTop(EqClassBot botClass, String id, List<EqClassBot> leftSisters, boolean root) {
        super(botClass.getDaughterEQClasses(), botClass.factorizedTrees, botClass.cat, botClass.type, id, botClass.getFs());
        this.leftSisters = leftSisters;
        this.root = root;
    }
    public boolean belongs(List<EqClassBot> leftSisters, boolean root){
        if(leftSisters.equals(this.leftSisters) && root == this.root){
            return true;
        }
        return false;
    }

    public boolean isRoot(){return root;}

    public List<EqClassBot> getLeftSisters() {
        return leftSisters;
    }

    public Map<EqClassBot, Boolean> getPossibleMothers() {
        return possibleMothers;
    }

    public boolean noLeftSisters() {
        if(leftSisters.isEmpty()) {return true;}
        return false;
    }

    public void addMother(EqClassBot bot, Boolean rightestSister){
        possibleMothers.put(bot, rightestSister);
    }

    @Override
    public boolean isBottomClass(){return false;}

    @Override
    public boolean isTopClass(){return true;}

    @Override
    public String toString() {
        String out = "{TOP Cat = "+ cat + " " + this.getId() + ", left sisters = ";
        for(EqClassBot bot: leftSisters) {
            out += bot.cat + " " + bot.getId() + ", ";
        }
        out += "\n";
        return out;
    }

    @Override
    public EqClassTop copyClass(NameFactory nf) {
        List<EqClassBot> sisters = new ArrayList<>();
        for(EqClassBot sis: leftSisters) {
            sisters.add(sis.copyClass());
        }
        List<EqClassBot> daughters = new ArrayList<>();
        for(EqClassBot kid: getDaughterEQClasses()) {
            daughters.add(kid.copyClass());
        }
        EqClassTop newEqClass = new EqClassTop(this.getDaughterEQClasses(), this.factorizedTrees, this.cat, this.type, this.getId(), new Fs(this.getFs(), nf), this.getLeftSisters(), isRoot());
        return newEqClass;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        EqClassBot c = (EqClassBot) o;
        if(c.isBottomClass()){return false;}

        EqClassTop t = (EqClassTop) o;
        boolean req = this.checkType(t.type)
                && this.getDaughterEQClasses().equals(t.getDaughterEQClasses())
                && this.getFs().equals(t.getFs())
                && this.getLeftSisters().equals(t.getLeftSisters())
                ;

        return req;
    }

    public static class Builder extends EqClassBot.Builder<Builder> {

        private List<EqClassBot> leftSisters = new ArrayList<>();

        private Map<EqClassBot, Boolean> possibleMothers = new HashMap<>();

        private boolean root = false;


        public Builder() {
        }

        public Builder(EqClassTop otherClass) {
            super(otherClass);
            this.leftSisters = otherClass.getLeftSisters();
            this.possibleMothers = otherClass.getPossibleMothers();
            this.root = otherClass.isRoot();
        }

        public Builder leftSisters(List<EqClassBot> leftSisters) {
            this.leftSisters = leftSisters;
            return this;
        }
        public Builder possibleMothers(Map<EqClassBot, Boolean> possibleMothers) {
            this.possibleMothers = possibleMothers;
            return this;
        }

        public Builder root(Boolean root) {
            this.root = root;
            return this;
        }
        @Override
        public EqClassTop build() {
            EqClassTop newClass = new EqClassTop(this.build().getDaughterEQClasses(), this.factorizedTrees, this.cat,
                    this.type, this.build().getId(), this.build().getFs(), this.leftSisters, this.root);
            for(Map.Entry<EqClassBot, Boolean> entry : this.possibleMothers.entrySet()) {
                newClass.addMother(entry.getKey(), entry.getValue());
            }
            return newClass;
        }
    }
}
