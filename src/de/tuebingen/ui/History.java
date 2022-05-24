/*
 *  File History.java
 *
 *  Authors:
 *     Yannick Parmentier  <parmenti@sfs.uni-tuebingen.de>
 *
 *  Copyright:
 *     Yannick Parmentier, 2008
 *
 *  Last modified:
 *     Di 16. Okt 09:29:49 CEST 2008
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
package de.tuebingen.ui;

import java.util.*;

public class History {

    private int cursor;
    private List<String> sentences;

    public History() {
        cursor = -1;
        sentences = new LinkedList<String>();
    }

    public void add(String s) {
        sentences.add(s);
        cursor++;
    }

    public String getPrevious() {
        if (cursor < 0) {
            return null;
        } else {
            if (cursor > 0) {
                return sentences.get(cursor--);
            } else {
                return sentences.get(cursor);
            }
        }
    }

    public String getNext() {
        if (cursor < 0) {
            return null;
        } else {
            if (cursor < (sentences.size() - 1)) {
                return sentences.get(++cursor);
            } else {
                return sentences.get(cursor);
            }
        }
    }

    public void setLast() {
        cursor = sentences.size() - 1;
    }

    public int getCursor() {
        return cursor;
    }

    public void setCursor(int cursor) {
        this.cursor = cursor;
    }

    public List<String> getSentences() {
        return sentences;
    }

    public void setSentences(List<String> sentences) {
        this.sentences = sentences;
    }

}
