package de.duesseldorf.rrg.test;

import de.duesseldorf.factorizer.FactorizingInterface;
import de.duesseldorf.rrg.RRGTree;
import de.duesseldorf.rrg.io.TreeFromBracketedStringRetriever;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

class FactorizingInterfaceTest {

    @Test
    void factorizeStandardAndSubst() {
        RRGTree tree1 =  new TreeFromBracketedStringRetriever("sleep\t(CL (CO (RP )(NUC (V <>))))").createTree();
        RRGTree tree2 =  new TreeFromBracketedStringRetriever("sleep\t(CL (CO (RP )(N )(NUC (V <>))))").createTree();
        Set<RRGTree> trees = new HashSet<>();
        trees.add(tree1);
        trees.add(tree2);

        FactorizingInterface Fi = new FactorizingInterface();
        Fi.factorize(trees);
    }
}