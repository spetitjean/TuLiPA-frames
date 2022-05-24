/*
 *  File BuiltinTokenizer.java
 *
 *  Authors:
 *     Wolfgang Maier <wo.maier@uni-tuebingen.de>
 *
 *  Copyright:
 *     Wolfgang Maier, 2007
 *
 *  Last modified:
 *     Di 16. Okt 09:41:31 CEST 2007
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
package de.tuebingen.tokenizer;

import java.io.IOException;

/**
 * @author wmaier
 */
public class BuiltinTokenizer extends Tokenizer {

    public static final String GERMAN = "german";
    public String germanTest = "Hat die AWO Spendengeld veruntreut?";
    public String fstFile = "tokenizerFrench.fst";

    public static final String WHITESPACE = "whitespace";
    public static final String PRE = "pre";
    public String whitespaceTest = "Hat die AWO Spendengeld veruntreut ?";

    public BuiltinTokenizer() throws TokenizerException, IOException {
        this(GERMAN);
    }

    public BuiltinTokenizer(String mode)
            throws TokenizerException, IOException {
        mode = mode.toLowerCase();
        if (GERMAN.equals(mode)) {
            setSentence(germanTest);
            FiniteStateReader r = new FiniteStateReader(fstFile, true);
            setT(r.getTransducer());
        } else if (WHITESPACE.equals(mode)) {
            setSentence(whitespaceTest);
            setErasePunctuation(true);
        } else {
            // split at whitespace
            setSentence(whitespaceTest);
        }
    }

}
