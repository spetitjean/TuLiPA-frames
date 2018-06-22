package de.duesseldorf.rrg.extractor;

import de.duesseldorf.rrg.RRGParseTree;
import de.duesseldorf.rrg.parser.SimpleRRGParseItem;
import de.duesseldorf.util.GornAddress;

public class ExtractionStep {
    public GornAddress GAInParseTree;
    public SimpleRRGParseItem currentItem;
    public RRGParseTree currentParseTree;

    public ExtractionStep(SimpleRRGParseItem currentItem,
            GornAddress GAInParseTree, RRGParseTree currentParseTree) {
        this.GAInParseTree = GAInParseTree;
        this.currentItem = currentItem;
        this.currentParseTree = currentParseTree;
    }

    public GornAddress getGAInParseTree() {
        return this.GAInParseTree;
    }

    public SimpleRRGParseItem getCurrentItem() {
        return this.currentItem;
    }

    public RRGParseTree getCurrentParseTree() {
        return this.currentParseTree;
    }

    public String toString() {
        return getCurrentItem() + "\n" + getGAInParseTree() + "\n"
                + getCurrentParseTree() + "\n-------------------------------\n";
    }
}