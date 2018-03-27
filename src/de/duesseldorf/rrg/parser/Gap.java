package de.duesseldorf.rrg.parser;

/**
 * represents the gaps in wrapping substitution (see deductino rules)
 * 
 * @author david
 *
 */
class Gap {
    final int start;
    final int end;
    final String nonterminal;

    Gap(int start, int end, String nonterminal) {
        this.start = start;
        this.end = end;
        this.nonterminal = nonterminal;
    }

    @Override
    public String toString() {
        return "(" + start + ", " + end + ", " + nonterminal + ")";
    }
}