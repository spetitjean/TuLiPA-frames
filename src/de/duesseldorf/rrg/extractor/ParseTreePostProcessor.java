package de.duesseldorf.rrg.extractor;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import de.duesseldorf.frames.Fs;
import de.duesseldorf.frames.Value;
import de.duesseldorf.rrg.RRGNode;
import de.duesseldorf.rrg.RRGTree;
import de.tuebingen.derive.PrettyNameFactory;
import de.tuebingen.tree.Node;

public class ParseTreePostProcessor {

    private RRGTree tree;
    private PrettyNameFactory pnf;

    public ParseTreePostProcessor(RRGTree tree) {
        this.tree = tree;
        this.pnf = new PrettyNameFactory();
    }

    public RRGTree postProcessNodeFeatures() {

        // data structure for seen corefs
        // Might be an option:

        // go to all nodes of the tree (recursive)
        // go to all feature structures of nodes (recursive)
        postProcessRec((RRGNode) tree.getRoot());

        // rename the env of the tree:
        return tree;
    }

    private void postProcessRec(RRGNode root) {
        root.setNodeFs(renameFeatureStructure(root.getNodeFs()));
        for (Node child : root.getChildren()) {
            postProcessRec((RRGNode) child);
        }
    }

    private Fs renameFeatureStructure(Fs inputFs) {
        // replace the coref
        if (inputFs.getCoref() != null) {
            Value prettyCoref = findPrettyCoref(inputFs.getCoref());
            inputFs.setCoref(prettyCoref);
        }

        // go to all the features
        Map<String, Value> toReplace = new HashMap<String, Value>();
        for (Entry<String, Value> avpair : inputFs.getAVlist().entrySet()) {
            switch (avpair.getValue().getType()) {
            case AVM:
                Fs prettyFs = renameFeatureStructure(
                        avpair.getValue().getAvmVal());
                toReplace.put(avpair.getKey(), new Value(prettyFs));
            case VAR:
                toReplace.put(avpair.getKey(),
                        findPrettyCoref(avpair.getValue()));
            }
        }
        toReplace.entrySet().forEach((avPair) -> inputFs
                .replaceFeat(avPair.getKey(), avPair.getValue()));
        return inputFs;
    }

    /**
     * TODO: some corefs are AVMs themselves. Seems to mostly work at the moment
     * 
     * @param coref
     * @return
     */
    private Value findPrettyCoref(Value coref) {
        // System.out.println(coref.getType());
        // if (coref.is(Value.Kind.AVM)) {
        // System.out.println(coref.getAvmVal());
        // return coref;
        if (coref.is(Value.Kind.VAR)) {
            // System.out.println(coref.getVarVal());
            Value derefed = tree.getEnv().deref(coref);
            if (derefed.is(Value.Kind.VAR)) {
                return new Value(Value.Kind.VAR,
                        pnf.getName(derefed.getVarVal()));
            } else {
                return coref;
            }
            // }
            // else if (coref.is(Value.Kind.AVM) && coref.getVarVal() != null) {
            // Value maybeCorefInside = coref.getAvmVal().getCoref();
            // Value derefed = tree.getEnv().deref(maybeCorefInside);
            // return new Value(Value.Kind.VAR,
            // pnf.getName(derefed.getVarVal()));
        } else {
            // System.out.println("yay elsecase: " + coref);
            return coref;
        }
    }
}
