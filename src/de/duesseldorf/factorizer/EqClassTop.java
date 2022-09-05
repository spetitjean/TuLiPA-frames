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
        EqClassTop newEqClass = new EqClassTop(this.getDaughterEQClasses(), this.factorizedTrees, this.cat, this.type, this.getId(), this.getFs(), this.getLeftSisters(), isRoot());
        return newEqClass;
    }
}
