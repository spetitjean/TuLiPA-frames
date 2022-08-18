package de.duesseldorf.factorizer;

import de.duesseldorf.rrg.RRGNode;

import java.util.ArrayList;
import java.util.List;

public class EqClassTop extends EqClassBot{

    private List<EqClassBot> leftSisters;

    private ArrayList<EqClassBot> possibleMothers = new ArrayList<>();

    /**
     * Eq classes that are euqal in daughters and left sisters
     * @param daughters daughter bottom EQ classes
     * @param cat category of the node
     * @param type type of the node
     * @param id
     * @param leftSisters left sister bottom Eq classes
     */
    public EqClassTop(ArrayList<EqClassBot> daughters, String cat, RRGNode.RRGNodeType type, String id, List<EqClassBot> leftSisters) {
        super(daughters, cat, type, id);
        this.leftSisters = leftSisters;
    }

    public EqClassTop(EqClassBot botClass, String id, List<EqClassBot> leftSisters) {
        super(botClass.getDaughterEQClasses(), botClass.cat, botClass.type, id);
        this.leftSisters = leftSisters;
    }
    public boolean belongs(List<EqClassBot> leftSisters){
        if(leftSisters.equals(this.leftSisters)){return true;}
        return false;
    }

    public List<EqClassBot> getLeftSisters() {
        return leftSisters;
    }

    public boolean noLeftSisters() {
        if(leftSisters.isEmpty()) {return true;}
        return false;
    }

    public ArrayList<EqClassBot> getPossibleMothers() {
        return possibleMothers;
    }

    public void addMother(EqClassBot bot){
        if(!possibleMothers.contains(bot)){possibleMothers.add(bot);}
    }

    @Override
    public String toString() {
        String out = "{Cat = "+ cat + " " + this.getId() + ", left sisters = ";
        for(EqClassBot bot: leftSisters) {
            out += bot.cat + " " + bot.getId() + ", ";
        }
        out += "\n";
        return out;
    }
}
