package de.duesseldorf.rrg.extractor;

import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

import de.duesseldorf.rrg.RRGParseResult;
import de.duesseldorf.rrg.RRGParseTree;
import de.duesseldorf.rrg.RRGTools;
import de.duesseldorf.rrg.RRGNode;

import de.duesseldorf.frames.UnifyException;
import de.tuebingen.anchoring.NameFactory;


public class ParseForestPostProcessor {
    /**
     * @return
     */
    static RRGParseResult postProcessParseTreeSet(
            Set<RRGParseTree> resultingParses) {

        System.out.printf("\n% 12d\tresulting trees after extraction%n",
                resultingParses.size());

        // 0 features percolation
        Set<RRGParseTree> percolatedParseTrees = new ConcurrentSkipListSet<RRGParseTree>();
        for (RRGParseTree parsetree : resultingParses) {
            try {
                if (((RRGNode) parsetree.getRoot()).postUpdateFeatures(parsetree.getEnv(), new NameFactory(), false)) {
                    if (((RRGNode) parsetree.getRoot()).postPostUpdateFeatures(parsetree.getEnv(), new NameFactory(), false)) {
                        ((RRGNode) parsetree.getRoot()).updateFS(parsetree.getEnv(), false);
                        percolatedParseTrees.add(parsetree);
                    }
                }
            } catch (UnifyException e) {
                System.err.println("Failed percolation");
            }
        }

        resultingParses = percolatedParseTrees;

        // 1 edge features
        // edge feature unification
        RRGParseResult resultingParsesEdgesUnified = new EdgeFeatureUnifier(
                resultingParses).computeUnUnifiedAndUnifiedTrees();
        System.out.printf(
                "% 12d\tsuccessful trees after edge feature unification%n",
                resultingParsesEdgesUnified.getSuccessfulParses().size());
        System.out.printf(
                "% 12d\tfailed trees after edge feature unification%n",
                resultingParsesEdgesUnified.getTreesWithEdgeFeatureMismatches()
                        .size());
        System.out.println(
                "\t\t\tnow make them beautiful and filter equal trees");
        // 2 make beautiful
        Set<RRGParseTree> resultingSuccessfulParsesBeautiful = new ConcurrentSkipListSet<RRGParseTree>();
        resultingParsesEdgesUnified.getSuccessfulParses().stream()
                .forEach((tree) -> {
                    RRGParseTree beautifulTree = (RRGParseTree) new ParseTreePostProcessor(
                            tree).postProcessNodeFeatures();
                    resultingSuccessfulParsesBeautiful.add(beautifulTree);
                });
        Set<RRGParseTree> resultingEdgeFailedParsesBeautiful = new ConcurrentSkipListSet<RRGParseTree>();
        resultingParsesEdgesUnified.getTreesWithEdgeFeatureMismatches().stream()
                .forEach((tree) -> {
                    RRGParseTree beautifulTree = (RRGParseTree) new ParseTreePostProcessor(
                            tree).postProcessNodeFeatures();
                    resultingEdgeFailedParsesBeautiful.add(beautifulTree);
                });
        // equality check

        Set<RRGParseTree> resultingSuccessfulParsesEqualityFiltered = RRGTools
                .removeDoubleParseTreesByWeakEqualsWithParseTrees(
                        resultingSuccessfulParsesBeautiful);
        Set<RRGParseTree> resultingEdgeFailedParsesEqualityFiltered = RRGTools
                .removeDoubleParseTreesByWeakEqualsWithParseTrees(
                        resultingEdgeFailedParsesBeautiful);

        System.out.printf("% 12d\tresulting successful trees%n",
                resultingSuccessfulParsesEqualityFiltered.size());
        System.out.printf("% 12d\tresulting failed trees%n",
                resultingEdgeFailedParsesEqualityFiltered.size());

        RRGParseResult.Builder parseResultBuilder = new RRGParseResult.Builder();
        parseResultBuilder = parseResultBuilder
                .successfulParses(resultingSuccessfulParsesEqualityFiltered)
                .treesWithEdgeFeatureMismatches(
                        resultingEdgeFailedParsesEqualityFiltered);
        return parseResultBuilder.build();
    }
}
