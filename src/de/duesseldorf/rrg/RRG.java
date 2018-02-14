package de.duesseldorf.rrg;

import java.util.List;
import java.util.Map;

import de.tuebingen.lexicon.Lemma;
import de.tuebingen.lexicon.MorphEntry;
import de.tuebingen.tag.Tuple;
import de.tuebingen.tree.Grammar;

public class RRG implements Grammar {

    public boolean needsAnchoring() {
        // TODO Auto-generated method stub
        return false;
    }

    public void setNeedsAnchoring(boolean b) {
        // TODO Auto-generated method stub

    }

    public void setMorphEntries(Map<String, List<MorphEntry>> morphEntries) {
        // TODO Auto-generated method stub

    }

    public void setLemmas(Map<String, List<Lemma>> lemmas) {
        // TODO Auto-generated method stub

    }

    public Map<String, List<Lemma>> getLemmas() {
        // TODO Auto-generated method stub
        return null;
    }

    public Map<String, List<MorphEntry>> getMorphEntries() {
        // TODO Auto-generated method stub
        return null;
    }

    public Map<String, List<Tuple>> getGrammar() {
        // TODO Auto-generated method stub
        return null;
    }

}
