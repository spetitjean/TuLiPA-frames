package de.duesseldorf.rrg;

import java.util.HashSet;
import java.util.Set;

import de.duesseldorf.rrg.io.SystemLogger;

public class RRGTools {

    private static SystemLogger log = new SystemLogger(System.err, true);

    /**
     * temporary fix: copied from method below and added idMap
     * 
     * @param trees
     * @return
     */
    public static Set<RRGParseTree> removeDoubleParseTreesByWeakEqualsWithParseTrees(
            Set<RRGParseTree> trees) {
        Set<RRGParseTree> result = new HashSet<RRGParseTree>();
        int i = 0;
        for (RRGParseTree treeFromResource : trees) {
            if (i % 10000 == 0 && i > 0) {
                log.info("filtered the first " + i + " trees and obtained "
                        + result.size());
            }
            i = i + 1;
            if (result.isEmpty()) {
                result.add(treeFromResource);
                continue;
            }
            boolean treeIsAlreadyIn = false;
            for (RRGParseTree treeFromResult : result) {
                boolean traceWeakEquals = treeFromResource.getIds()
                        .size() == treeFromResult.getIds().size()
                        && treeFromResource.getIds()
                                .containsAll(treeFromResult.getIds());
                if (traceWeakEquals) {
                    boolean nodesWeakEquals = ((RRGNode) treeFromResult
                            .getRoot()).weakEquals(
                                    (RRGNode) treeFromResource.getRoot());
                    if (nodesWeakEquals) {
                        treeIsAlreadyIn = true;
                        // log.info("found double tree: " + treeFromResource);
                        break;
                    }
                }
            }
            if (!treeIsAlreadyIn) {
                result.add(treeFromResource);
            }
        }
        log.info("number of equal trees that were filtered out: "
                + (trees.size() - result.size()));
        log.info("there are " + result.size() + " trees left.");
        return result;
    }

    /**
     * return the set of trees that is the input set of trees but without
     * lookalikes that are detected by the weakEquals method
     * 
     * @param trees
     * @return
     */
    public static Set<RRGTree> removeDoubleTreesByWeakEquals(
            Set<RRGTree> trees) {
        Set<RRGTree> result = new HashSet<RRGTree>();
        int i = 0;
        for (RRGTree treeFromResource : trees) {
            if (i % 10000 == 0 && i > 0) {
                log.info("filtered the first " + i + " trees and obtained "
                        + result.size());
            }
            i = i + 1;
            if (result.isEmpty()) {
                result.add(treeFromResource);
                continue;
            }
            boolean treeIsAlreadyIn = false;
            for (RRGTree treeFromResult : result) {
                if (((RRGNode) treeFromResult.getRoot())
                        .weakEquals((RRGNode) treeFromResource.getRoot())) {
                    treeIsAlreadyIn = true;
                    // log.info("found double tree: " + treeFromResource);
                    break;
                }
            }
            if (!treeIsAlreadyIn) {
                result.add(treeFromResource);
            }
        }
        log.info("number of equal trees that were filtered out: "
                + (trees.size() - result.size()));
        log.info("there are " + result.size() + " trees left.");
        return result;
    }

    public static Set<RRGParseTree> filterDoublesByIdMap(
            Set<RRGParseTree> unfilteredTrees) {
        Set<RRGParseTree> result = new HashSet<RRGParseTree>();
        for (RRGParseTree candidate : unfilteredTrees) {
            if (result.isEmpty()) {
                result.add(candidate);
            } else {
                boolean addCandidate = true;
                for (RRGParseTree alreadyFilteredTree : result) {
                    if (alreadyFilteredTree.getIds()
                            .equals(candidate.getIds())) {
                        addCandidate = false;
                    }
                }
                if (addCandidate) {
                    result.add(candidate);
                }
            }
        }
        return result;
    }

    public static Set<RRGParseTree> filterRRGParseTrees(Set<RRGTree> input) {
        Set<RRGParseTree> result = new HashSet<RRGParseTree>();
        for (RRGTree tree : input) {
            if (tree instanceof RRGParseTree) {
                result.add((RRGParseTree) tree);
            }
        }
        return result;
    }

    public static Set<RRGParseTree> convertTreeSet(Set<RRGTree> input) {
        Set<RRGParseTree> result = new HashSet<RRGParseTree>();
        for (RRGTree tree : input) {
            result.add(new RRGParseTree(tree));
        }
        return result;
    }

    public static Set<RRGTree> convertTreeSetParseToGeneral(
            Set<RRGParseTree> input) {
        Set<RRGTree> result = new HashSet<RRGTree>();
        for (RRGTree tree : input) {
            result.add(new RRGTree(tree));
        }
        return result;
    }

}
