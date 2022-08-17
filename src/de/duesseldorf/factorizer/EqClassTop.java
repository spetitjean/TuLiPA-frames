package de.duesseldorf.factorizer;

import de.duesseldorf.rrg.RRGNode;

import java.util.ArrayList;
import java.util.List;

public class EqClassTop extends EqClassBot{

    private List<EqClassBot> leftSisters;

    private ArrayList<EqClassBot> possibleMothers;


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

    public ArrayList<EqClassBot> getPossibleMothers() {
        return possibleMothers;
    }

    public void addMother(EqClassBot bot){possibleMothers.add(bot);}

    @Override
    public String toString() {
        String out = "{Cat = "+ cat + " " + this.getId() + ", left sisters = ";
        for(EqClassBot bot: leftSisters) {
            out += bot.cat + " ";
        }
        out += "\n";
        return out;
    }
}
