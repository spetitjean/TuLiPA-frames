package de.duesseldorf.rrg.parser;

import java.util.List;

import de.duesseldorf.frames.Situation;
import de.duesseldorf.rrg.RRG;
import de.duesseldorf.rrg.RRGTree;

public class RRGParser {

    private Situation situation;
    private SimpleRRGParseChart chart;

    public RRGParser(Situation sit) {
        // TODO Auto-generated constructor stub
        this.situation = sit;
        this.chart = new SimpleRRGParseChart();
    }

    public boolean parseSentence(List<String> toksentence) {
        scan(toksentence);
        return false;
    }

    /**
     * apply the scanning deduction rule
     */
    private void scan(List<String> sentence) {
        for (RRGTree tree : ((RRG) situation.getGrammar()).getTrees()) {
            for (String word : sentence) {
            }
        }
    }

}
