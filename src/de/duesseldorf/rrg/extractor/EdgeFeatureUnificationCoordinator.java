package de.duesseldorf.rrg.extractor;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

import de.duesseldorf.rrg.RRGParseResult;
import de.duesseldorf.rrg.RRGParseTree;
import de.duesseldorf.rrg.io.SystemLogger;

public class EdgeFeatureUnificationCoordinator {
    private static SystemLogger log = new SystemLogger(System.err, false);

    private Set<RRGParseTree> parseTreesWithoutUnification;
    private Set<RRGParseTree> unifiedTrees;
    private RRGParseTree result;

    public EdgeFeatureUnificationCoordinator(Set<RRGParseTree> parseTrees) {
        this.parseTreesWithoutUnification = parseTrees;
        this.unifiedTrees = new HashSet<RRGParseTree>();
    }

    public RRGParseResult computeUnUnifiedAndUnifiedTrees() {
        Map<RRGParseTree, Boolean> unificationResults = new ConcurrentHashMap<>();
        Set<RRGParseTree> successFullyUnifiedTrees = new ConcurrentSkipListSet<RRGParseTree>();
        Set<RRGParseTree> treesWithMismatches = new ConcurrentSkipListSet<RRGParseTree>();
        parseTreesWithoutUnification.parallelStream()
                .forEach((ununifiedTree) -> {

                    EdgeFeatureUnifier unifier = new EdgeFeatureUnifier(
                            ununifiedTree);
                    if (unifier.unifyEdgeFeatures()) {
                        unificationResults.put(unifier.getResult(), true);
                        successFullyUnifiedTrees.add(unifier.getResult());
                    } else {
                        unificationResults.put(ununifiedTree, false);
                        treesWithMismatches.add(ununifiedTree);
                    }
                });
        // for (Map.Entry<RRGParseTree, Boolean> entry : unificationResults
        // .entrySet()) {
        // if (entry.getValue()) {
        // successFullyUnifiedTrees.add(entry.getKey());
        // } else {
        // treesWithMismatches.add(entry.getKey());
        // }
        // }
        //
        // Set<RRGParseTree> resultingTrees = new HashSet<RRGParseTree>(
        // unifiedTrees);
        // resultingTrees.addAll(parseTreesWithoutUnification);
        log.info("unifying edge features, removed "
                + (parseTreesWithoutUnification.size()
                        - successFullyUnifiedTrees.size())
                + " trees");
        log.info("there are " + successFullyUnifiedTrees.size()
                + " trees left.");

        RRGParseResult.Builder resBuilder = new RRGParseResult.Builder();
        resBuilder.successfulParses(successFullyUnifiedTrees);
        resBuilder.treesWithEdgeFeatureMismatches(treesWithMismatches);
        return resBuilder.build();
    }
}
