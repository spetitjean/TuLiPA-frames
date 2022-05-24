/*
 *  File NameFactory.java
 *
 *  Authors:
 *     Yannick Parmentier  <parmenti@sfs.uni-tuebingen.de>
 *
 *  Copyright:
 *     Yannick Parmentier, 2007
 *
 *  Last modified:
 *     Di 16. Okt 11:13:15 CEST 2007
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
package de.tuebingen.anchoring;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class NameFactory {

    private int index;
    private Map<String, String> dictionary;

    public NameFactory() {
        index = 0;
        dictionary = new Hashtable<String, String>();
    }

    public String getName(String in) {
        String out = "";
        if (dictionary.containsKey(in)) {
            out = dictionary.get(in);
        } else {
            out = "X" + this.hashCode() + "_" + index;
            index++;
            dictionary.put(in, out);
        }
        // System.err.println(" ---> old name: " + in + " ---> new name: " +
        // out);
        return out;
    }

    /*
     * public String getTreeName(String in){
     * String out = "";
     * if (dictionary.containsKey(in)) {
     * out = dictionary.get(in);
     * } else {
     * out = in + "__"+ this.hashCode() + "_" +index;
     * index++;
     * dictionary.put(in, out);
     * }
     * return out;
     * }
     */

    public String getUniqueName() {

        String out = "X" + this.hashCode() + "_" + index;
        index++;
        return out;
    }

    public boolean isIn(String in) {
        return dictionary.containsKey(in);
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public Map<String, String> getDictionary() {
        return dictionary;
    }

    public void setDictionary(Map<String, String> dictionary) {
        this.dictionary = dictionary;
    }

    public String toString() {
        String res = "";
        Set<String> keys = dictionary.keySet();
        Iterator<String> it = keys.iterator();
        while (it.hasNext()) {
            String k = it.next();
            res += " key: " + k + " - value: " + dictionary.get(k);
        }
        return res;
    }

    // @Override
    // public int hashCode() {
    // return Objects.hash(dictionary);
    // }

}
