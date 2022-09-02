package de.duesseldorf.factorizer;

import de.duesseldorf.frames.Fs;
import de.duesseldorf.rrg.RRGNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EqClassTop extends EqClassBot{

    private List<EqClassBot> leftSisters;

    private Map<EqClassBot, Boolean> possibleMothers = new HashMap<>();

    //private ArrayList<EqClassBot> possibleMothers = new ArrayList<>();

    /**
     * Eq classes that are euqal in daughters and left sisters
     * @param daughters daughter bottom EQ classes
     * @param cat category of the node
     * @param type type of the node
     * @param id
     * @param leftSisters left sister bottom Eq classes
     */
    public EqClassTop(ArrayList<EqClassBot> daughters, String cat, RRGNode.RRGNodeType type, String id, Fs fs, List<EqClassBot> leftSisters) {
        super(daughters, cat, type, id, fs);
        this.leftSisters = leftSisters;
    }

    public EqClassTop(EqClassBot botClass, String id, List<EqClassBot> leftSisters) {
        super(botClass.getDaughterEQClasses(), botClass.cat, botClass.type, id, botClass.getFs());
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
}
