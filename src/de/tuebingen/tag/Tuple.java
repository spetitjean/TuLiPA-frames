/*
 *  File Tuple.java
 *
 *  Authors:
 *     Wolfgang Maier <wo.maier@uni-tuebingen.de>
 *     Yannick Parmentier <parmenti@sfs.uni-tuebingen.de>
 *
 *  Copyright:
 *     Wolfgang Maier, 2007
 *     Yannick Parmentier, 2007
 *
 *  Last modified:
 *     Di 16. Okt 09:49:45 CEST 2007
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
package de.tuebingen.tag;

import java.util.List;

public class Tuple {

    private TagTree head;
    private List<TagTree> arguments;
    private String id;
    private String family; // for anchoring
    private String originalId;

    public Tuple() {
        head = null;
        arguments = null;
        id = null;
        family = null;
        originalId = null;
    }

    public Tuple(String i) {
        head = null;
        arguments = null;
        id = i;
        family = null;
        originalId = null;
    }

    public Tuple(Tuple t) {
        head = t.getHead();
        arguments = t.getArguments();
        id = t.getId();
        family = t.getFamily();
        originalId = t.getOriginalId();
    }

    public String getFamily() {
        return family;
    }

    public void setFamily(String family) {
        this.family = family;
    }

    public Tuple(String i, TagTree head, List<TagTree> arguments) {
        this.head = head;
        this.arguments = arguments;
        id = i;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public TagTree getHead() {
        return head;
    }

    public void setHead(TagTree head) {
        this.head = head;
    }

    public List<TagTree> getArguments() {
        return arguments;
    }

    public void setArguments(List<TagTree> arguments) {
        this.arguments = arguments;
    }

    public String getOriginalId() {
        return (originalId == null) ? "" : originalId;
    }

    public void setOriginalId(String originalId) {
        this.originalId = originalId;
    }

    public String toString() {
        try {

            String h = head.toString("");
            String a = "";
            if (arguments != null) {
                for (int i = 0; i < arguments.size(); i++) {
                    a += arguments.get(i).toString("");
                }
            }
            return ("Tuple's id: " + id + "\n original Id: " + getOriginalId()
                    + "\n Tuple family: " + family + "\n Head: " + h
                    + "\n Arguments: " + a + "\n\n");
        } catch (Exception e) {
            return (// e.getMessage()
                    // + "\nSomething in tuple.toString went wrong. This is
                    // whats left...\n"
                    "Tuple's id: " + id + "\n original Id: " + getOriginalId()
                            + "\n Tuple family: " + family);
        }
    }

}
