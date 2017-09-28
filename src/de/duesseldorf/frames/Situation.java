package de.duesseldorf.frames;

import de.tuebingen.tree.Grammar;

/**
 * 
 * Stores a grammar, the corresponding frames and the type hierarchy to allow
 * easy access.
 * 
 * @author david
 *
 */
public class Situation {

    private Grammar g;
    private Grammar frameG;
    private TypeHierarchy tyHi;

    public Situation(Grammar g, Grammar frameG, TypeHierarchy tyHi) {
        this.g = g;
        this.frameG = frameG;
        this.tyHi = tyHi;
    }

    public Grammar getGrammar() {
        return this.g;
    }

    public Grammar getFrameGrammar() {
        return this.frameG;
    }

    public TypeHierarchy getTypeHierarchy() {
        return this.tyHi;
    }
}
