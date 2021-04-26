package de.duesseldorf.rrg.io;

/**
 * Everything used for parsing the input RRG is listed here
 * 
 * File XMLRRGTag.java
 *
 * Authors:
 * David Arps <david.arps@hhu.de
 * 
 * Copyright:
 * David Arps, 2018
 *
 * This file is part of the TuLiPA system
 * https://github.com/spetitjean/TuLiPA-frames
 *
 * TuLiPA is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * TuLiPA is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 */
public enum XMLRRGTag {

    // tags
    GRAMMAR("grammar"), // root element
    ENTRY("entry"), // grammar entry
    FAMILY("family"), // family the entry belongs to
    TRACE("trace"), // trace
    CLASS("class"), // part of a trace
    FRAME("frame"), // semantic representation as typed FS
    TREE("tree"), // tree - part of an entry
    NODE("node"), // node in a tree
    NARG("narg"), // stores the fs in a node
    FEATURESTRUCTURE("fs"), // feature structure
    FEATURE("f"), // a single feature
    CTYPE("ctype"), // conjunctive type (typed FS)
    RELATION("relation"),// relation (frame)
    SYM("sym"), // and its value

    // attributes
    NAME("name"), // of an entry or node
    ID("id"), // of a tree
    TYPE("type"), // of a node, e.g. substitution nodes or anchors (see below)
    COREF("coref"), // coreference of a FS
    VALUE("value"), // in a FS
    VAL("val"), // in a FS type
    CAT("cat"), // of a node, either a terminal sequence or the 'label' of the
                // node, e.g. RP, S, NP,...

    // node types
    XMLSTDNode("std"), // standard node
    XMLLEXNode("lex"), // lexical node
    XMLSUBSTNode("subst"), // substitution node
    XMLDDAUGHTERNode("ddaughter"), // for wrapping substitution
    XMLANCHORNode("anchor"), // anchor node
    XMLSISADJFOOTNode("star"); // root of a sister adjunction tree

    private final String Stringvalue;

    XMLRRGTag(String s) {
        this.Stringvalue = s;
    }

    public String StringVal() {
        return Stringvalue;
    }
}
