package de.duesseldorf.rrg.anchoring;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import de.duesseldorf.frames.Situation;
import de.tuebingen.anchoring.TreeSelector;
import de.tuebingen.lexicon.Lemma;
import de.tuebingen.lexicon.MorphEntry;
import de.tuebingen.tokenizer.Word;

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

    public void anchor() {
        Map<String, List<MorphEntry>> morphEntries = Situation.getGrammar()
                .getMorphEntries();
        Map<String, List<Lemma>> lemmas = Situation.getGrammar().getLemmas();

        TreeSelector ts = new TreeSelector(tokenizedSentenceAsWords, false);
        ts.retrieve(new LinkedList<String>());
        // System.out.println(morphEntries.values());
        // System.out.println(lemmas.values());
    }
}
