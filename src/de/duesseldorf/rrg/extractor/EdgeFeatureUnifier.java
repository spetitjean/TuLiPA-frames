package de.duesseldorf.rrg.extractor;

import java.util.HashSet;
import java.util.Set;

import de.duesseldorf.frames.UnifyException;
import de.duesseldorf.frames.Value;
import de.duesseldorf.frames.ValueTools;
import de.duesseldorf.rrg.RRGNode;
import de.duesseldorf.rrg.RRGNode.RRGNodeType;
import de.duesseldorf.rrg.RRGParseTree;
import de.duesseldorf.util.GornAddress;
import de.tuebingen.tag.Environment;

public class EdgeFeatureUnifier {

    private Set<RRGParseTree> parseTreesWithoutUnification;
    private Set<RRGParseTree> unifiedTrees;
    private RRGParseTree result;

    public EdgeFeatureUnifier(Set<RRGParseTree> parseTrees) {
        this.parseTreesWithoutUnification = parseTrees;
        this.unifiedTrees = new HashSet<RRGParseTree>();

    }

    public Set<RRGParseTree> computeUnUnifiedAndUnifiedTrees() {
        Set<RRGParseTree> unifiedTrees = new HashSet<RRGParseTree>();
        for (RRGParseTree ununifiedTree : parseTreesWithoutUnification) {
            RRGParseTree unifiedTree = unifyEdgeFeatures(ununifiedTree);
            unifiedTrees.add(unifiedTree);
        }

        Set<RRGParseTree> result = new HashSet<RRGParseTree>(unifiedTrees);
        result.addAll(parseTreesWithoutUnification);
        return result;
    }

    private RRGParseTree unifyEdgeFeatures(RRGParseTree ununifiedTree) {
        this.result = new RRGParseTree(ununifiedTree);
        result.setId(result.getId() + "_edgesUnified");

        unifyEdgeFeatures(new GornAddress());

        return result;
    }

    private void unifyEdgeFeatures(GornAddress gornAddress) {
        RRGNode nodeWithGornAddress = result.findNode(gornAddress);
        if (nodeWithGornAddress == null) {
            System.err.println("node in edgefeatureUnification null, return");
            return;
        }

        // do unification below all children
        for (int i = 0; i < nodeWithGornAddress.getChildren().size(); i++) {
            unifyEdgeFeatures(gornAddress.ithDaughter(i));
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
                            edgeValueFromRightDaughter, new Environment(0));
                    if (newEdgeValueForBoth == null) {
                        // no edge fs's at all
                        continue;
                    }

                } catch (UnifyException e) {
                    System.out.println(
                            "TODO handle: unification exception while unifying edge features");
                    return;
                }
                ((RRGNode) nodeWithGornAddress.getChildren().get(i - 1))
                        .getNodeFs().setFeat("r", newEdgeValueForBoth);
                ((RRGNode) nodeWithGornAddress.getChildren().get(i)).getNodeFs()
                        .setFeat("l", newEdgeValueForBoth);
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
                        upperLeftMost, new Environment(0));
                Value newRightMost = ValueTools.unifyOrReplace(lowerRightMost,
                        upperRightMost, new Environment(0));
                // replace if unification was successfull
                // left
                ((RRGNode) nodeWithGornAddress.getChildren().get(0)).getNodeFs()
                        .setFeat("l", newLeftMost);
                nodeWithGornAddress.getNodeFs().setFeat("l", newLeftMost);
                // right
                ((RRGNode) nodeWithGornAddress.getChildren()
                        .get(nodeWithGornAddress.getChildren().size() - 1))
                                .getNodeFs().setFeat("r", newRightMost);
                nodeWithGornAddress.getNodeFs().setFeat("r", newRightMost);
            } catch (UnifyException e) {
                System.err.println(
                        "ERROR while unifying outermost feature structures");
            }

        }
    }

}
