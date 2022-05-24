package de.duesseldorf.rrg.extractor;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

import de.duesseldorf.frames.UnifyException;
import de.duesseldorf.frames.Value;
import de.duesseldorf.frames.ValueTools;
import de.duesseldorf.rrg.RRGNode;
import de.duesseldorf.rrg.RRGNode.RRGNodeType;
import de.duesseldorf.rrg.RRGParseResult;
import de.duesseldorf.rrg.RRGParseTree;
import de.duesseldorf.rrg.io.SystemLogger;
import de.duesseldorf.util.GornAddress;

public class EdgeFeatureUnifier {
    private static SystemLogger log = new SystemLogger(System.err, false);

    private Set<RRGParseTree> parseTreesWithoutUnification;
    private Set<RRGParseTree> unifiedTrees;
    private RRGParseTree result;

    public EdgeFeatureUnifier(Set<RRGParseTree> parseTrees) {
        this.parseTreesWithoutUnification = parseTrees;
        this.unifiedTrees = new HashSet<RRGParseTree>();

    }

    public RRGParseResult computeUnUnifiedAndUnifiedTrees() {
        Set<RRGParseTree> successFullyUnifiedTrees = new ConcurrentSkipListSet<RRGParseTree>();
        Set<RRGParseTree> treesWithMismatches = new ConcurrentSkipListSet<RRGParseTree>();
        parseTreesWithoutUnification.stream().forEach((ununifiedTree) -> {
            if (unifyEdgeFeatures(ununifiedTree)) {
                successFullyUnifiedTrees.add(result);
            } else {
                treesWithMismatches.add(ununifiedTree);
            }
        });
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

    private boolean unifyEdgeFeatures(RRGParseTree ununifiedTree) {
        // System.out.println("Unifying edge features in ");
        // System.out.println(ununifiedTree);

        this.result = new RRGParseTree(ununifiedTree);
        result.setId(result.getId() + "_edgesUnified");
        boolean unificationWorked = unifyEdgeFeatures(new GornAddress());
        // TODO remove root edge features
        // ((RRGNode) result.getRoot()).getNodeFs().removeFeat("l");
        // ((RRGNode) result.getRoot()).getNodeFs().removeFeat("r");
        // if (!unificationWorked) {
        // System.out.println("edge unification did not work: ");
        // System.out.println(
        // RRGTreeTools.recursivelyPrintNode(ununifiedTree.getRoot()));
        // }
        return unificationWorked;
    }

    private boolean unifyEdgeFeatures(GornAddress gornAddress) {
        RRGNode nodeWithGornAddress = result.findNode(gornAddress);
        if (nodeWithGornAddress == null) {
            // System.err.println("node in edgefeatureUnification null,
            // return");
            return false;
        }

        // do unification below all children
        for (int i = 0; i < nodeWithGornAddress.getChildren().size(); i++) {
            if (!unifyEdgeFeatures(gornAddress.ithDaughter(i))) {
                return false;
            }
        }

        // do unification between two children
        // if because there's no fs in between otherwise
        if (nodeWithGornAddress.getChildren().size() > 1) {
            // i refers to the index of the righter daughter (the one with the l
            // edge fs)
            for (int i = 1; i < nodeWithGornAddress.getChildren().size(); i++) {
                Value edgeValueFromRightDaughter = ((RRGNode) nodeWithGornAddress
                        .getChildren().get(i)).getNodeFs().getFeat("l");
                Value edgeValueFromLeftDaughter = ((RRGNode) nodeWithGornAddress
                        .getChildren().get(i - 1)).getNodeFs().getFeat("r");
                Value newEdgeValueForBoth;
                try {
                    newEdgeValueForBoth = ValueTools.unifyOrReplace(
                            edgeValueFromLeftDaughter,
                            edgeValueFromRightDaughter, result.getEnv());
                    if (newEdgeValueForBoth == null) {
                        // no edge fs's at all
                        continue;
                    }

                } catch (UnifyException e) {
                    // System.out.println(
                    // "TODO handle: unification exception while unifying edge
                    // features");
                    // System.out.println("feature mismatch right vs. left: ");
                    // System.out.println(edgeValueFromRightDaughter);
                    // System.out.println(edgeValueFromLeftDaughter);
                    return false;
                }
                ((RRGNode) nodeWithGornAddress.getChildren().get(i - 1))
                        .getNodeFs().setFeatWithReplaceIfValNotNull("r",
                                newEdgeValueForBoth);
                ((RRGNode) nodeWithGornAddress.getChildren().get(i)).getNodeFs()
                        .setFeatWithReplaceIfValNotNull("l",
                                newEdgeValueForBoth);
            }
        }

        // do unification of outermost edges below the current node and above
        // the current node if the current node is not a subst node
        // TODO what if the current node is a ddaughter?
        if (nodeWithGornAddress.getChildren().size() > 0
                && !nodeWithGornAddress.getType().equals(RRGNodeType.SUBST)) {
            // find outermost edge feature structures
            Value lowerLeftMost = ((RRGNode) nodeWithGornAddress.getChildren()
                    .get(0)).getNodeFs().getFeat("l");
            Value upperLeftMost = nodeWithGornAddress.getNodeFs().getFeat("l");

            Value lowerRightMost = ((RRGNode) nodeWithGornAddress.getChildren()
                    .get(nodeWithGornAddress.getChildren().size() - 1))
                    .getNodeFs().getFeat("r");
            Value upperRightMost = nodeWithGornAddress.getNodeFs().getFeat("r");

            try {
                // unify
                Value newLeftMost = ValueTools.unifyOrReplace(lowerLeftMost,
                        upperLeftMost, result.getEnv());
                Value newRightMost = ValueTools.unifyOrReplace(lowerRightMost,
                        upperRightMost, result.getEnv());
                // replace if unification was successfull
                // left
                ((RRGNode) nodeWithGornAddress.getChildren().get(0)).getNodeFs()
                        .setFeatWithReplaceIfValNotNull("l", newLeftMost);
                nodeWithGornAddress.getNodeFs()
                        .setFeatWithReplaceIfValNotNull("l", newLeftMost);
                // right
                ((RRGNode) nodeWithGornAddress.getChildren()
                        .get(nodeWithGornAddress.getChildren().size() - 1))
                        .getNodeFs().setFeatWithReplaceIfValNotNull("r",
                                newRightMost);
                nodeWithGornAddress.getNodeFs()
                        .setFeatWithReplaceIfValNotNull("r", newRightMost);
            } catch (UnifyException e) {
                // System.err.println(
                // "ERROR while unifying outermost feature structures");
                return false;
            }
        }
        return true;
    }
}
