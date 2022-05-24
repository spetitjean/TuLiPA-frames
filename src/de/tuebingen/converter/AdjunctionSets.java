/*
 *  File AdjunctionSets.java
 *
 *  Authors:
 *     Yannick Parmentier  <parmenti@sfs.uni-tuebingen.de>
 *
 *  Copyright:
 *     Yannick Parmentier, 2007
 *
 *  Last modified:
 *     Di 16. Okt 11:09:22 CEST 2007
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
package de.tuebingen.converter;

import java.util.*;

import de.tuebingen.tag.Tuple;
import de.tuebingen.tag.TagNode;
import de.tuebingen.tag.TagTree;

public class AdjunctionSets {
    /**
     * Class that computes both the adjunction sets and initial sets
     *
     * @att auxiliaries - an hashtable mapping categories to auxiliary head trees
     * initials    - an hashtable mapping categories to initial   head trees
     * allaux      - an hashtable mapping categories to auxiliary (head or arg) trees
     * arguments   - an hashtable mapping tuple ids to argument trees (for easier retrieval)
     * @author parmenti
     */

    private Hashtable<Object, LinkedList<Object>> auxiliaries;
    private Hashtable<Object, LinkedList<Object>> initials;
    private Hashtable<Object, LinkedList<Object>> allaux;
    private Hashtable<Object, LinkedList<Object>> arguments;

    public AdjunctionSets(List<Tuple> ttgrammar) {
        /**
         * Constructor that builds the adjunction sets
         * by computing the partition of head auxiliary tree
         * according to their root category
         *
         * @param ttgrammar
         * 			a list of anchored tuples
         */
        this.auxiliaries = new Hashtable<Object, LinkedList<Object>>();
        this.allaux = new Hashtable<Object, LinkedList<Object>>();
        this.initials = new Hashtable<Object, LinkedList<Object>>();
        this.arguments = new Hashtable<Object, LinkedList<Object>>();

        Iterator<Tuple> ti = ttgrammar.iterator();
        while (ti.hasNext()) {
            Tuple tu = (Tuple) ti.next();
            TagTree t = tu.getHead();
            if (t.hasFoot()) { // t is an auxiliary tree
                // the foot is then used for the adjunction set
                String footbotcat = ((TagNode) t.getFoot()).getAdjCategory(TagNode.BOT);
                String roottopcat = ((TagNode) t.getRoot()).getAdjCategory(TagNode.TOP);
                update(auxiliaries, t, (Object) new CatPairs(roottopcat, footbotcat));
                update(allaux, t, (Object) new CatPairs(roottopcat, footbotcat));
            } else { // t is an initial tree
                // the root is used for substitution set
                String rootcat = ((TagNode) t.getRoot()).getSubstCategory();
                update(initials, t, (Object) new CatPairs(rootcat, ""));
            }
            if (tu.getArguments() != null) {
                LinkedList<TagTree> args = (LinkedList<TagTree>) tu.getArguments();
                LinkedList<Object> args2 = new LinkedList<Object>();
                for (int i = 0; i < args.size(); i++) {
                    TagTree ta = args.get(i);
                    args2.add(ta);
                    if (ta.hasFoot()) {
                        // the foot is then used for the adjunction set
                        String footbotcat = ((TagNode) ta.getFoot()).getAdjCategory(TagNode.BOT);
                        String roottopcat = ((TagNode) ta.getRoot()).getAdjCategory(TagNode.TOP);
                        update(allaux, ta, (Object) new CatPairs(roottopcat, footbotcat));
                    }
                }
                if (args2.size() > 0) {
                    arguments.put(t.getTupleId(), args2);
                }
            }
        }
    }

    public static void update(Hashtable<Object, LinkedList<Object>> table, Object t, Object key) {
        if (table.containsKey(key)) {
            LinkedList<Object> l = table.get(key);
            l.add(t);
        } else {
            LinkedList<Object> l = new LinkedList<Object>();
            l.add(t);
            table.put(key, l);
        }
    }

    public static LinkedList<Object> getList(Hashtable<Object, LinkedList<Object>> table, Object key) {
        LinkedList<Object> res = null;
        if (table.containsKey(key)) {
            res = table.get(key);
        }
        return res;
    }

    public Hashtable<Object, LinkedList<Object>> getAuxiliaries() {
        return auxiliaries;
    }

    public void setAuxiliaries(Hashtable<Object, LinkedList<Object>> auxiliaries) {
        this.auxiliaries = auxiliaries;
    }

    public Hashtable<Object, LinkedList<Object>> getInitials() {
        return initials;
    }

    public void setInitials(Hashtable<Object, LinkedList<Object>> initials) {
        this.initials = initials;
    }

    public Hashtable<Object, LinkedList<Object>> getAllaux() {
        return allaux;
    }

    public void setAllaux(Hashtable<Object, LinkedList<Object>> allaux) {
        this.allaux = allaux;
    }

    public Hashtable<Object, LinkedList<Object>> getArguments() {
        return arguments;
    }

    public void setArguments(Hashtable<Object, LinkedList<Object>> arguments) {
        this.arguments = arguments;
    }

    public String toString(Hashtable<Object, LinkedList<Object>> table) {
        String res = "";
        Set<Object> keys = table.keySet();
        Iterator<Object> i = keys.iterator();
        while (i.hasNext()) {
            CatPairs k = (CatPairs) i.next();
            LinkedList<Object> l = table.get(k);
            res += "\n top cat : " + k.getTopCat();
            res += "\n bot cat : " + k.getBotCat();
            for (int j = 0; j < l.size(); j++) {
                res += "\n tree : " + ((TagTree) l.get(j)).getId();
            }
        }
        return res;
    }

}
