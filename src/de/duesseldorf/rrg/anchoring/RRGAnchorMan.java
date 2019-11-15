package de.duesseldorf.rrg.anchoring;

import java.util.List;
import java.util.Map;

import de.duesseldorf.frames.Situation;
import de.tuebingen.lexicon.Lemma;
import de.tuebingen.lexicon.MorphEntry;

public class RRGAnchorMan {

    private List<String> tokenizedSentence;

    public RRGAnchorMan(List<String> tokenizedSentence) {
        this.tokenizedSentence = tokenizedSentence;
    }

    public void anchor() {
        Map<String, List<MorphEntry>> morphEntries = Situation.getGrammar()
                .getMorphEntries();
        Map<String, List<Lemma>> lemmas = Situation.getGrammar().getLemmas();

        System.out.println(morphEntries.values());
        System.out.println(lemmas.values());
    }
}
