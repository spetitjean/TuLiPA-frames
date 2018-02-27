package de.duesseldorf.io;

/**
 * Everything used for parsing the input RRG is listed here
 * 
 * @author david
 *
 */
public enum XMLRRGTag {
    // tags
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
    SYM("sym"), // and its value

    // attributes
    NAME("name"), // of an entry or node
    ID("id"), // of a tree
    TYPE("type"), // of a node, e.g. substitution nodes or anchors (see below)
    COREF("coref"), // coreference of a FS
    VALUE("value"), // in a FS
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