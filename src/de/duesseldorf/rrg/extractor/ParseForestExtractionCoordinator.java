package de.duesseldorf.rrg.extractor;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

import de.duesseldorf.rrg.RRGParseResult;
import de.duesseldorf.rrg.RRGParseTree;
import de.duesseldorf.rrg.parser.RRGParseChart;
import de.duesseldorf.rrg.parser.RRGParseItem;
import de.duesseldorf.util.GornAddress;
import de.tuebingen.util.TextUtilities;

public class ParseForestExtractionCoordinator {

    private RRGParseChart parseChart;
    private List<String> toksentence;
    public boolean verbosePrintsToStdOut = false;
    private ConcurrentSkipListSet<RRGParseTree> resultingParses;

    public ParseForestExtractionCoordinator(RRGParseChart parseChart,
            List<String> toksentence) {
        this.parseChart = parseChart;
        this.toksentence = toksentence;
        this.resultingParses = new ConcurrentSkipListSet<RRGParseTree>();

    }

    public RRGParseResult extractParseTrees() {
        // find goal items in the chart. Extract them all and add the set of
        // parse trees derived from them to the resulting parses
        Set<RRGParseItem> goals = parseChart.retrieveGoalItems();
        if (verbosePrintsToStdOut) {
            System.out.println("goal items: " + goals);
        }
        goals.parallelStream().forEach((goal) -> {
            ParseForestExtractor extractor = new ParseForestExtractor(
                    parseChart, toksentence);

            ExtractionStep initExtrStep = initialExtractionStep(
                    (RRGParseItem) goal);
            Set<RRGParseTree> resultingTrees = extractor.extract(initExtrStep);
            addToResultingParses(resultingTrees);
        });

        return ParseForestPostProcessor
                .postProcessParseTreeSet(resultingParses);
    }

    /**
     * this method takes a
     * 
     * @param goal
     *            item and creates the initial extraction step from that item
     */
    ExtractionStep initialExtractionStep(RRGParseItem goal) {
        ExtractionStep result = new ExtractionStep(goal, new GornAddress(),
                new RRGParseTree(goal.getTree()), 0);
        return result;
    }

    private synchronized void addToResultingParses(
            Set<RRGParseTree> resultingTrees) {
        for (RRGParseTree resultingParseTree : resultingTrees) {
            String newId = TextUtilities.appendList(toksentence, "_")
                    + resultingParses.size();
            resultingParseTree.setId(newId);
            resultingParses.add(resultingParseTree);
        }
    }

}
