/*
 *  File Tokenizer.java
 *
 *  Authors:
 *     Wolfgang Maier <wo.maier@uni-tuebingen.de>
 *     
 *  Copyright:
 *     Wolfgang Maier, 2008
 *
 *  Last modified:
 *     Di 18. Feb 09:41:31 CEST 2008
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public abstract class Tokenizer {

    protected static final int MAXSENTLEN = 30;
    protected static final String punctstring = ",.!?;";

    private String sentence;
    private FiniteState t;
    // erase left-over punctuation from result
    private boolean erasePunctuation;

    public Tokenizer() {
        super();
        sentence = "";
        t = null;
        erasePunctuation = false;
    }

    private Collection<String> punctuation() {
        ArrayList<String> ret = new ArrayList<String>();
        String[] puncts = punctstring.split("");
        for (int i = 0; i < puncts.length; ++i) {
            ret.add(puncts[i]);
        }
        return ret;
    }

    /**
     * @return a list of words converted into a list of String
     */
    public static List<String> tok2string(List<Word> toks) {
        List<String> res = new ArrayList<String>();
        for (int i = 0; i < toks.size(); i++) {
            Word w = toks.get(i);
            res.add(w.getWord());
        }
        return res;
    }

    /**
     * @return a list of words converted into a list of String with position
     *         suffixed
     */
    public static List<String> tok2stringPos(List<Word> toks) {
        List<String> res = new ArrayList<String>();
        for (int i = 0; i < toks.size(); i++) {
            Word w = toks.get(i);
            res.add(w.getWord() + "" + w.getEnd());
        }
        return res;
    }

    /**
     * @return an array of strings converted into a list of words
     * @throws TokenizerException
     *             if a string (a word) contains whitespace
     */
    public static List<Word> strings2words(String[] strings) {
        List<Word> al = new ArrayList<Word>(strings.length);
        for (int i = 0; i < strings.length; ++i) {
            String word = strings[i].trim();
            if (word.length() > 0) {
                Word w = new Word(word);
                w.setStart(i);
                w.setEnd(i + 1);
                al.add(w);
            }
        }
        return al;
    }

    public FiniteState getT() {
        return t;
    }

    public void setT(FiniteState t) {
        this.t = t;
    }

    public boolean hasTransducer() {
        return t != null;
    }

    public boolean isErasePunctuation() {
        return erasePunctuation;
    }

    public void setErasePunctuation(boolean erasePunctuation) {
        this.erasePunctuation = erasePunctuation;
    }

    /**
     * get the sentence
     * 
     * @return the sentence
     */
    public String getSentence() {
        return sentence;
    }

    /**
     * set the sentence
     * 
     * @param sentence
     *            the sentence
     */
    public void setSentence(String sentence) {
        this.sentence = sentence;
    }

    /**
     * Tokenize a sentence.
     */
    public List<Word> tokenize() throws TokenizerException {
        ArrayList<Word> ret = null;

        if (hasTransducer()) {
            ret = new ArrayList<Word>(MAXSENTLEN);
            int i = 0;
            String sw = "";
            Word word = new Word();
            while (i < sentence.length()) {
                // we try to read as far as we can
                while (i < sentence.length() && t.read(sentence.charAt(i))) {
                    ++i;
                }
                // we are in start state, there's input left and we couldn't
                // read:
                // unknown input symbol! Fast forward to next whitespace.
                if (t.getStartState().equals(t.getState())
                        && i < sentence.length()) {
                    int auxind = i;
                    while (auxind < sentence.length() && !Character
                            .isWhitespace(sentence.charAt(auxind))) {
                        ++auxind;
                    }
                    // append to last word if possible
                    if (ret.size() > 0) {
                        sw = ret.get(ret.size() - 1).getWord();
                        ret.remove(ret.size() - 1);
                    }
                    // set flag in Word to indicate unknown character
                    sw += sentence.substring(i, auxind);
                    i = auxind;
                    word.setStrangeToken(true);
                    // go back to the last final state (or start state if there
                    // is
                    // no final state on the path)
                } else {
                    while (!t.isFinalState(t.getState()) && t.unread()) {
                        --i;
                    }
                    sw = t.getStringOutput();
                }
                if (!sw.equals("")) {
                    // save the output
                    word.setWord(sw);
                    word.setStart(ret.size());
                    word.setEnd(ret.size() + 1);
                    ret.add(word);
                    word = new Word();
                    sw = "";
                }
                // new word: reset automaton to start state and empty output
                t.reset();
            }
            // if there is only one word, try to split again by whitespace
            if (ret.size() == 1) {
                String[] result = sentence.split("\\s+");
                ret = (ArrayList<Word>) strings2words(result);
            }
        } else { // if (hasTranducer())
            // no transducer -> whitespace
            String[] result = sentence.split("\\s+");
            ret = (ArrayList<Word>) strings2words(result);
        }

        if (isErasePunctuation()) {
            Collection<String> punctuation = punctuation();
            ArrayList<Word> newret = new ArrayList<Word>();
            int cnt = 0;
            Iterator<Word> it = ret.iterator();
            while (it.hasNext()) {
                Word w = it.next();
                if (!punctuation.contains(w.getWord())) {
                    w.setStart(cnt);
                    w.setEnd(cnt + 1);
                    newret.add(w);
                    ++cnt;
                }
            }
            ret = newret;
        }

        return ret;
    }

    /**
     * @return toString() of the underlying transducer
     */
    public String toString() {
        return t.toString();
    }

}