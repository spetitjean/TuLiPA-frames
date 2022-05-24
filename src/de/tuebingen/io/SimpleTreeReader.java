/*
 *  File SimpleTreeReader.java
 *
 *  Authors:
 *     Wolfgang Maier  <wo.maier@.uni-tuebingen.de>
 *
 *  Copyright:
 *     Wolfgang Maier, 2007
 *
 *  Last modified:
 *     Di 16. Okt 10:45:42 CEST 2007
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
package de.tuebingen.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import de.tuebingen.tree.Node;
import de.tuebingen.tree.SimpleNode;
import de.tuebingen.tree.SimpleTree;
import de.tuebingen.tree.Label;

/**
 * @author wmaier
 */
public class SimpleTreeReader {

    public final static int MAX_LINELENGTH = 1000;

    /*
     * constants concerning certain treebank formats
     */
    public final static String VROOT_LABEL = "VROOT";

    /*
     * important character constants
     */
    protected final static char OPENPAR = '(';
    protected final static char CLOSEPAR = ')';
    protected final static char OPENBRACK = '[';
    protected final static char CLOSEBRACK = ']';
    protected final static char SPACE = ' ';

    private BufferedReader r;
    private String format;

    public SimpleTreeReader(File f) throws FileNotFoundException, UnsupportedEncodingException {
        this(f, "");
    }

    public SimpleTreeReader(File f, String mode) throws FileNotFoundException, UnsupportedEncodingException {
        r = new BufferedReader(new InputStreamReader(new FileInputStream(f), "ISO-8859-1"));
        this.format = mode;
    }

    public void close() {
        try {
            r.close();
        } catch (IOException e) {
            // do nothing if close() fails
        }
    }

    public String getMode() {
        return format;
    }

    public void setMode(String mode) {
        this.format = mode;
    }


    /*
     * Interpreter methods for different formats
     */

    private static String eliminateWhitespace(String s) {
        String ret = "";
        for (int i = 0; i < s.length(); ++i) {
            int j = i;
            // skip the following whitespaces
            for (; j < s.length() && Character.isSpaceChar(s.charAt(j)); ++j) ;
            // do not add the whitespace if the following character after
            // the sequence of whitespaces is a paren
            if (!(j > i && j < s.length()
                    && (s.charAt(j) == OPENPAR || s.charAt(j) == CLOSEPAR))) {
                ret += s.charAt(i);
            }
        }
        return ret;
    }

    /*
     * Bracketed format, one tree per line. Preterminals assumed to be non-branching.
     * Whitespace is required between preterminals and terminals, and only allowed
     * there. Example for a single tree:
     * (S(NP(N John))(VP(V reads)(NP(DET a)(N book))))
     */
    private int termno = 0;  // for continuous terminal numbering

    private SimpleNode parensonelineToSimpleNode(String ps) throws Exception {
        SimpleNode ret = null;
        if (ps.charAt(0) != OPENPAR || ps.charAt(ps.length() - 1) != CLOSEPAR) {
            throw new Exception("Could not read tree [" + ps + "]: No surrounding parens.");
        }
        // cut off the surrounding parens
        String s = ps.substring(1, ps.length() - 1);
        String label = "";
        int parenind = s.indexOf(OPENPAR);   // next open paren
        int terminalind = s.indexOf(SPACE);  // next space
        if (parenind == -1 && terminalind > 0) {

            // we are a preterminal
            label = s.substring(0, terminalind);
            String rest = s.substring(terminalind + 1);
            if (rest.indexOf(CLOSEPAR) > -1) {
                throw new Exception("Could not read tree [" + s + "]: Unbalanced parenthesis?");
            }
            if (rest.indexOf(SPACE) > -1 || rest.length() == 0) {
                throw new Exception("Could not read tree [" + s + "]: Empty terminal or terminal contains space?");
            }

            // create the preterminal
            ret = new SimpleNode(label);
            ret.setLabel(Label.parseLabel(label));

            // create the terminal below
            SimpleNode terminal = new SimpleNode(rest);
            terminal.setLabel(Label.parseLabel(rest));

            // set the right indices on both nodes
            terminal.setLeft(termno);
            ret.setLeft(termno);
            termno++;
            terminal.setRight(termno);
            ret.setRight(termno);

            // set the terminal sequence on both nodes
            ArrayList<Node> terminalarr = new ArrayList<Node>();
            terminalarr.add(terminal);
            terminal.setTerminals(new ArrayList<Node>(terminalarr));
            ret.setTerminals(terminalarr);

            // add the terminal as child of the preterminal
            ret.addChild(terminal);

        } else if (parenind > 0) {
            // we are a regular nonterminal
            label = s.substring(0, parenind);
            ret = new SimpleNode(label);
            ret.setLabel(Label.parseLabel(label));
            int level = 0;
            int childrenstart = parenind;
            int childrenend = childrenstart + 1;
            while (childrenend + 1 < s.length() && s.charAt(childrenstart) != CLOSEPAR) {
                ++childrenend;
                if (s.charAt(childrenend) == CLOSEPAR && level == 0) {
                    SimpleNode child = parensonelineToSimpleNode(s.substring(childrenstart, childrenend + 1));
                    if (child == null) {
                        throw new Exception("Could not read tree [" + s + "] A child could not be recognized.");
                    }
                    // set link to current node on child (parent for the child)
                    child.setParent(ret);
                    // add the child to current node
                    ret.addChild(child);
                    childrenstart = childrenend + 1;
                    childrenend = childrenstart + 1;
                    continue;
                }
                // look for next child
                if (s.charAt(childrenend) == OPENPAR) {
                    ++level;
                } else if (s.charAt(childrenend) == CLOSEPAR) {
                    --level;
                }
            }
            if (level > 0 || childrenstart < s.length()) {
                throw new Exception("Could not read tree [" + s + "] Unbalanced parenthesis?");
            }
            // set indices of span
            List<Node> children = ret.getChildren();
            SimpleNode leftmostChild = (SimpleNode) children.get(0);
            SimpleNode rightmostChild = (SimpleNode) children.get(children.size() - 1);
            ret.setLeft(leftmostChild.getLeft());
            ret.setRight(rightmostChild.getRight());
            // fill terminal array
            for (int i = 0; i < children.size(); ++i) {
                SimpleNode child = (SimpleNode) children.get(i);
                //System.err.println(child.toString());
                ret.addAllTerminals(child.getTerminals());
            }
        } else {
            throw new Exception("Could not read tree [" + s + "]: Label empty?");
        }
        return ret;
    }

    private SimpleTree parensonelineToSimpleTree(String s) throws Exception {
        SimpleTree ret = new SimpleTree();
        ret.setId(s);
        SimpleNode n = null;
        try {
            termno = 0;
            n = parensonelineToSimpleNode(s);
        } catch (Exception e) {
            throw new Exception("[" + s + "]: " + e.getMessage());
        }
        ret.setRoot(n);
        ret.setTerminals(n.getTerminals());
        return ret;
    }

    /**
     * Read a complete tree from the file. The expected format depends on the
     * format field.
     *
     * @return - a SimpleTree or null if no tree can be read.
     */
    public SimpleTree readTree() throws IOException, Exception {
        SimpleTree ret = null;
        if ("INP_PARENS_ONELINE".equals(format)) {
            String line = r.readLine();
            ret = parensonelineToSimpleTree(line);
        } else if ("INP_PENN".equals(format)) {
            String sentence = "";
            String line = r.readLine();
            if (line != null) {
                sentence += line.trim();
                line = r.readLine();
                while (line != null && line.charAt(0) != OPENPAR) {
                    sentence += line.trim();
                    r.mark(MAX_LINELENGTH);
                    line = r.readLine();
                }
                r.reset();
            }
            if (sentence.length() > 5) {
                sentence = eliminateWhitespace(sentence);
                if (sentence.charAt(0) != OPENPAR || sentence.charAt(sentence.length() - 1) != CLOSEPAR) {
                    throw new Exception("Could not read tree [" + sentence + "]: No surrounding parens.");
                }
                sentence = sentence.substring(1);
                sentence = "(" + VROOT_LABEL + sentence;
                ret = parensonelineToSimpleTree(sentence);
            }
        } else {
            throw new IOException("Unknown tree format.");
        }
        return ret;
    }


    /*
     * just for testing
     */
    public static void main(String[] args) throws Exception {
        SimpleTreeReader r = new SimpleTreeReader(new File("/home/wmaier/workspace/TuebingenParser/test/penntest"));
        r.setMode("INP_PENN");
        SimpleTree t = null;
        while ((t = r.readTree()) != null) {
            System.err.println(t.toString());
        }
        //String s = "(VROOT(S(NP-SBJ(NP(NNP Pierre)(NNP Vinken))(, ,)(ADJP(NP(CD 61)(NNS years))(JJ old))(, ,))(VP(MD will)(VP(VB join)(NP(DT the)(NN board))(PP-CLR(IN as)(NP(DT a)(JJ nonexecutive)(NN director)))(NP-TMP(NNP Nov.)(CD 29))))(. .)))";
        //String s = "(S(NP(N John))(VP(V reads)(NP(DET a)(N book))))";
        //String s = "(NP (NNP Pierre) (NNP Vinken) )";
        //String s = "(S (NP-SBJ(NP (NNP Pierre) (NNP Vinken) )(, ,)(ADJP(NP (CD 61) (NNS years) )(JJ old) )(, ,) )(VP (MD will)(VP (VB join)(NP (DT the) (NN board) )(PP-CLR (IN as)(NP (DT a) (JJ nonexecutive) (NN director) ))(NP-TMP (NNP Nov.) (CD 29) )))(. .) )";
        //s = eliminateWhitespace(s);
        //System.err.println(s);
        //System.err.println(parensonelineToSimpleTree(s));
    }

}
