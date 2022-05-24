/*
 *  File RCGParser.java
 *
 *  Authors:
 *     Wolfgang Maier  <wo.maier@uni-tuebingen.de>
 *
 *  Copyright:
 *     Wolfgang Maier, 2007
 *
 *  Last modified:
 *     Di 16. Okt 10:04:16 CEST 2007
 *
 *  This file is part of the TuLiPA system
 *     http://www.sfb441.uni-tuebingen.de/emmy-noether-kallmeyer/tulipa
 *
 *  TuLiPA is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  TuLiPA is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package de.tuebingen.parser;

import java.util.Hashtable;
import java.util.List;

import de.tuebingen.rcg.RCG;
import de.tuebingen.tokenizer.Word;
import de.tuebingen.tree.Grammar;

/**
 * A class that forks to different RCG Parsers.
 *
 * @author wmaier
 */
public abstract class RCGParser {

    private Grammar grammar;

    /**
     * Construct a parser given a grammar. We can't construct empty parsers.
     *
     * @param grammar - the grammar to use
     */
    public RCGParser(Grammar grammar) {
        this.grammar = grammar;
    }

    public Grammar getGrammar() {
        return grammar;
    }

    public void setGrammar(RCG grammar) {
        this.grammar = grammar;
    }

    /**
     * Recognize a sentence
     *
     * @param input - list of words
     * @return true if the sentence has been recognized
     */
    abstract public boolean recognize(List<Word> input);


    /**
     * Parse a sentence
     *
     * @param input - a list of words
     * @return the result status of parsing (success or failed)
     */
    abstract public boolean parse(List<Word> input);

    /**
     * Parse a sentence, with verbose mode switch
     */
    abstract public boolean parseSentence(boolean v, List<Word> sentence);

    /**
     * Print the parse forest
     *
     * @return
     */
    abstract public String printForest();

    abstract public Hashtable<ClauseKey, DStep> getParse();

    abstract public List<ClauseKey> getEmptyRHS();

    abstract public List<ClauseKey> getAnswers();

    abstract public ForestExtractorInitializer getForestExtractorInitializer();

}
