package de.duesseldorf.rrg.anchoring;

import de.duesseldorf.frames.Situation;
import de.duesseldorf.rrg.RRG;
import de.duesseldorf.rrg.RRGNode;
import de.duesseldorf.rrg.RRGTree;
import de.tuebingen.anchoring.TreeSelector;
import de.tuebingen.lexicon.Lemma;
import de.tuebingen.lexicon.MorphEntry;
import de.tuebingen.tokenizer.Word;

import java.util.*;

public class RRGAnchorMan {

    private List<String> tokenizedSentenceAsStrings;
    private List<Word> tokenizedSentenceAsWords;

    public RRGAnchorMan(List<String> tokenizedSentence) {
        this.tokenizedSentenceAsStrings = tokenizedSentence;
        convertTokenizedSentenceToListOfWords();
    }

    private void convertTokenizedSentenceToListOfWords() {
        this.tokenizedSentenceAsWords = new LinkedList<Word>();
        for (String wAsString : tokenizedSentenceAsStrings) {
            this.tokenizedSentenceAsWords.add(new Word(wAsString));
        }
    }

    /**
     * takes the input sentence and the grammar and @return the set of all trees
     * that (i) have an anchor node that matches the input, (ii) have no anchors
     * and no lexical nodes at all or (iii) have a lex node that matches a part
     * of the input
     */
    public Set<RRGTree> anchor() {
        Map<String, List<MorphEntry>> morphEntries = Situation.getGrammar()
                .getMorphEntries();
        Map<String, List<Lemma>> lemmas = Situation.getGrammar().getLemmas();

        if (Situation.getGrammar().needsAnchoring()) {

            TreeSelector ts = new TreeSelector(tokenizedSentenceAsWords, false);
            // TreeSelector adds all RRGTrees as anchoredTrees to the RRG that
            // contain an anchor node (and have lemmas in the input sentence?)
            ts.retrieve(new LinkedList<String>());
        }


        Set<RRGTree> tmpresult = ((RRG) Situation.getGrammar()).getAnchoredTrees();
        Set<RRGTree> resultWithNewIds = new HashSet<>();
        for (RRGTree tree : tmpresult) {
            RRGTree newTree = new RRGTree(tree);
            ((RRGNode) newTree.getRoot()).removeCategory();
            newTree.setId(tree.getId() + "_" + tree.getLexNodes().keySet().toString());
            resultWithNewIds.add(newTree);
        }
        Set<RRGTree> result = new HashSet<>(resultWithNewIds);
        // next, take care of the lexicaliized trees and trees without anchor
        // nodes

        ((RRG) Situation.getGrammar()).getTrees().stream().forEach((tree) -> {
            boolean noAnchNoLex = tree.getLexNodes().isEmpty()
                    && tree.getAnchorNode() == null;
            // anch node must be null and some word from the sentence must be in
            // the tree
            HashSet<String> intersection = new HashSet<String>(tokenizedSentenceAsStrings);
            intersection.retainAll(tree.getLexNodes().keySet());
            boolean lexNoAnch = tree.getAnchorNode() == null
                    && !intersection.isEmpty();
            if (noAnchNoLex || lexNoAnch) {
                result.add(tree);
            }
        });
        return result;

        // System.out.println(morphEntries.values());
        // System.out.println(lemmas.values());
    }
}
