/*
 *  File WorkbenchLoader.java
 *
 *  Authors:
 *     Wolfgang Maier  <wo.maier@uni-tuebingen.de>
 *     Yannick Parmentier <parmenti@sfs.uni-tuebingen.de>
 *     David Arps <david.arps@hhu.de>
 *     Simon Petitjean <petitjean@phil.hhu.de>
 *         
 *  Copyright:
 *     Wolfgang Maier, 2007
 *     Yannick Parmentier, 2007
 *     David Arps, 2017
 *     Simon Petitjean, 2017
 *
 * Last modified:
 *     2017
 *
 *  This file is part of the TuLiPA-frames system
 *     https://github.com/spetitjean/TuLiPA-frames
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
package de.duesseldorf.ui;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import de.duesseldorf.frames.Situation;
import de.duesseldorf.frames.TypeHierarchy;
import de.duesseldorf.io.BracketedRRGReader;
import de.duesseldorf.io.XMLRRGReader;
import de.duesseldorf.io.XMLTypeHierarchyReader;
import de.tuebingen.anchoring.InstantiatedTagTree;
import de.tuebingen.anchoring.LexicalSelection;
import de.tuebingen.io.RCGReader;
import de.tuebingen.io.TextCFGReader;
import de.tuebingen.io.TextRCGReader;
import de.tuebingen.io.XMLLemmaReader;
import de.tuebingen.io.XMLMorphReader;
import de.tuebingen.io.XMLRCGReader;
import de.tuebingen.io.XMLTTMCTAGReader;
import de.tuebingen.lexicon.Lemma;
import de.tuebingen.lexicon.MorphEntry;
import de.tuebingen.tag.TTMCTAG;
import de.tuebingen.tag.TagTree;
import de.tuebingen.tag.Tuple;
import de.tuebingen.tokenizer.BuiltinTokenizer;
import de.tuebingen.tokenizer.FileTokenizer;
import de.tuebingen.tokenizer.Tokenizer;
import de.tuebingen.tokenizer.TokenizerException;
import de.tuebingen.tokenizer.Word;
import de.tuebingen.tree.Grammar;
import de.tuebingen.ui.CommandLineOptions;
import de.tuebingen.ui.PolarityFilterOutput;
import de.tuebingen.util.CollectionUtilities;
import fr.loria.disambiguation.Polarities;
import fr.loria.filter.XMLcopy;
import fr.loria.io.TransformPolarity;
import fr.loria.io.XmlTagTreeReader;

public class WorkbenchLoader {
    /**
     * Loads a grammar without separate file for frames and type hierarchy
     * 
     * @param op
     *            The CommandlineOptions to be considered
     * @param gram
     *            Grammar
     * @param lem
     *            Lemma
     * @param mo
     *            Morphological Entrys
     * @return Ideally A ready to go Grammar
     * @throws Exception
     */
    public static Situation loadSituation(CommandLineOptions op, String gram,
            String lem, String mo) throws Exception {
        return loadSituation(op, gram, null, lem, mo, null);
    }

    /**
     * Loads a grammar with a separate file for frames and type hiearchy
     * 
     * @param op
     *            The CommandlineOptions to be considered
     * @param gram
     *            Grammar
     * @param fram
     *            The separate frames not stored in the original grammar file
     * @param lem
     *            Lemma
     * @param mo
     *            Morphological Entrys
     * @param tyHi
     *            Type hierarchy
     * @return Ideally A ready to go Grammar
     * @throws Exception
     */
    public static Situation loadSituation(CommandLineOptions op, String gram,
            String fram, String lem, String mo, String tyHi) throws Exception {
        Grammar g;
        Grammar frameG = null;
        TypeHierarchy tHi = null;
        File grammar = null;
        File lemmas = null;
        File morphs = null;
        File frame = null;
        File typeHierarchy = null;

        // System.out.println(op.toString());

        if (op.check("g")) {
            grammar = new File(gram);
            if (!grammar.exists()) {
                System.err.println("File " + gram + " not found.");
                throw new Exception("File " + gram + " not found.");
            }
        }
        // operator for loding the frame grammar
        if (op.check("f")) {
            frame = new File(fram);
        }
        if (op.check("th")) {
            typeHierarchy = new File(tyHi);
            // System.out.println( "Type hierarchy file loaded in interface: " +
            // tyHi);
        }

        if (op.check("r") || op.check("c") || op.check("lcfrs")) {// RCG parsing
            long loadTime = System.nanoTime();
            String gext = grammar.getName();
            if (gext.length() > 4) {
                gext = gext.substring(gext.length() - 3);
            }
            RCGReader rcggr;
            if (op.check("r")) {
                if (gext.equals("xml")) {
                    rcggr = new XMLRCGReader(grammar);
                } else {
                    rcggr = new TextRCGReader(grammar);
                }
            } else {
                rcggr = new TextCFGReader(grammar);
            }
            g = rcggr.getRCG();
            rcggr.close();
            long loadedTime = System.nanoTime() - loadTime;
            System.err.println("RCG grammar loading time: "
                    + (loadedTime) / (Math.pow(10, 9)) + " sec.");
        } else if (op.check("rrg")) { // RRG-parsing

            if (op.check("rrgbrin")) {
                BracketedRRGReader brReader = new BracketedRRGReader(grammar);
                g = brReader.parseRRG();
            } else {
                XMLRRGReader rrgreader = new XMLRRGReader(grammar);
                g = rrgreader.retrieveRRG();
                rrgreader.close();
            }

        } else { // TAG/TT-MCTAG parsing
            // if the tag option is enabled (SAX-based loading with extended
            // lexical disambiguation)
            if (op.check("tag")) {
                // first we add pre-computed polarities to the input TAG grammar
                String res = buildPolarities(op);
                if (res == null) {
                    CommandLineProcesses
                            .error("Error while building polarities", op);
                }
                // then, we compute a subgrammar according to the sentence to
                // parse (includes anchoring)
                g = computeValidSets(op);
                // we remove the "simple" lexical disambiguation, since the
                // extended one has been performed:
                op.setVal("nofiltering", "true");
            } else {
                // else classical grammar loading (DOM-based)
                XMLLemmaReader xlr = null;
                XMLMorphReader xmr = null;
                boolean needsAnchoring = false;

                long loadTime = System.nanoTime();
                // 1. Grammar processing
                XMLTTMCTAGReader xgr = new XMLTTMCTAGReader(grammar);
                g = new TTMCTAG(xgr.getTuples());
                needsAnchoring = xgr.needsAnchoring();
                g.setNeedsAnchoring(needsAnchoring);
                xgr.close();
                // 1a Semantic processing
                // frame part,
                if (frame != null) {
                    XMLTTMCTAGReader fxgr = new XMLTTMCTAGReader(frame);
                    // TODO probably, this needs to be modified
                    frameG = new TTMCTAG(fxgr.getFrames());
                    needsAnchoring = fxgr.needsAnchoring();
                    frameG.setNeedsAnchoring(needsAnchoring);
                    fxgr.close();
                    // for (String s : frameG.getLemmas().keySet()) {
                    // System.out.print(s + "\t" + frameG.getLemmas().get(s));
                    // }
                }

                // 1b Type hierarchy processing
                if (typeHierarchy != null) {
                    XMLTypeHierarchyReader thr = new XMLTypeHierarchyReader(
                            typeHierarchy);
                    tHi = thr.getTypeHierarchy();

                    thr.close();
                }

                // 2. Lemmas processing
                if (needsAnchoring && !(op.check("l"))) {
                    CommandLineProcesses.error(
                            "Anchoring needed, lexicon files not found.", op);
                } else if (op.check("l") && !(op.getVal("l").equals(""))
                        && !(lem.equals(""))) {
                    lemmas = new File(lem);
                    xlr = new XMLLemmaReader(lemmas);
                    g.setLemmas(xlr.getLemmas());
                }
                // 3. Morphs processing
                if (needsAnchoring && !(op.check("m")) && !(mo.equals(""))) {
                    CommandLineProcesses.error(
                            "Anchoring needed, lexicon files not found.", op);
                } else if (op.check("m")) {
                    morphs = new File(mo);
                    xmr = new XMLMorphReader(morphs);
                    g.setMorphEntries(xmr.getMorphs());
                }
                long loadedTime = System.nanoTime() - loadTime;
                System.err.println("Grammar and lexicons loading time: "
                        + (loadedTime) / (Math.pow(10, 9)) + " sec.");
            }
            // System.err.println(g.toString());
        }
        return new Situation(g, frameG, tHi);
    }

    public static String buildPolarities(CommandLineOptions op) {
        String polarityFile = null;
        boolean ok = true;
        long loadTime = 0;
        long loadedTime = 0;
        String grammar = op.check("g") ? op.getVal("g") : null;
        if (grammar != null) {
            String input = grammar;
            String output = input.substring(0, input.lastIndexOf('.'))
                    + "-pol.xml";
            String[] files = { input, output };
            // Polarity file to be computed only if it does not already exist:
            File out = new File(output);
            File in = new File(input);
            if (!out.exists() || (out.lastModified() < in.lastModified())) {
                try {
                    TransformPolarity tp = new TransformPolarity();
                    loadTime = System.nanoTime();
                    tp.xsltprocess(files);
                    loadedTime = System.nanoTime() - loadTime;
                } catch (Exception e) {
                    System.err.println(e.getMessage());
                    ok = false;
                }
                if (ok) {
                    System.err.println(
                            "Polarity file created in " + output + ".");
                    System.err.println("Polarity computation time: "
                            + (loadedTime) / (Math.pow(10, 9)) + " sec.\n");
                    polarityFile = output;
                    op.setVal("p", " " + out.getAbsolutePath() + " ");
                }
            } else {
                polarityFile = output;
                op.setVal("p", " " + out.getAbsolutePath() + " ");
            }
        } else {
            System.err.println("Please specify a grammar using -g");
        }
        return polarityFile;
    }

    public static Grammar computeValidSets(CommandLineOptions op)
            throws IOException, Exception {

        PolarityFilterOutput result = new PolarityFilterOutput();
        String res = "";
        TTMCTAG zesubgrammar = new TTMCTAG();
        zesubgrammar.setNeedsAnchoring(true);

        boolean verbose = op.check("v");
        String sentence = op.check("s") ? op.getVal("s") : null;
        String axiom = op.check("a") ? op.getVal("a") : "s"; // default axiom is
                                                             // s
        String gram = op.check("g") ? op.getVal("g") : null;
        String outFile = op.check("o") ? op.getVal("x") : "";
        boolean withLeftContext = !op.check("global");
        List<String> toksentence = null;

        // 2. We tokenize the input sentence
        List<Word> tokens = null;
        try {
            Tokenizer tok = loadTokenizer(op);
            tok.setSentence(sentence);
            tokens = tok.tokenize();
            if (verbose)
                System.err.println("Tokenized sentence: " + tokens.toString());
            toksentence = Tokenizer.tok2string(tokens);
        } catch (TokenizerException e) {
            System.err.println("***** Tokenizer error ******");
            e.printStackTrace();
            CommandLineProcesses.error("Tokenizing error.", op);
        }
        if (verbose)
            System.err.println("Tokens: " + toksentence);

        // 3. We read the XML files (polarized grammar + lemma)
        String uri = op.getVal("p");
        String morph = op.getVal("m");
        String lemma = op.getVal("l");

        /*
         * // for debugging System.err.println("@@@@@@@@@@@@@ File g" + uri);
         * System.err.println("@@@@@@@@@@@@@ File l" + lemma);
         * System.err.println("@@@@@@@@@@@@@ File m" + morph);
         */
        if (uri == null || morph == null || lemma == null) {
            CommandLineProcesses.error(
                    "Missing argument on the command line (gram/lemma/morph)",
                    op);
        }

        int inputSize = 0;
        long totalTime = 0;

        try {
            long loadTime = System.nanoTime();
            XmlTagTreeReader xmlreader = new XmlTagTreeReader(
                    XmlTagTreeReader.POL, uri, verbose);
            long loadedTime = System.nanoTime() - loadTime;
            totalTime += loadedTime;
            System.err.println("Polarities loading time            : "
                    + (loadedTime) / (Math.pow(10, 9)) + " sec.");

            Map<String, Polarities> polarities = xmlreader.getPolarities();
            List<String> lexNodes = xmlreader.getLexNodes();
            /*
             * System.out.println("\nLex nodes: "); for (String s : lexNodes) {
             * System.out.print(s + " "); }
             */
            Map<String, List<String>> families = xmlreader.getFamilies();
            inputSize = xmlreader.getInGrammarSize();
            /*
             * Iterator<String> trees = polarities.keySet().iterator();
             * while(trees.hasNext()) { String tree = trees.next();
             * System.out.println("Tree " + tree); Polarities pol =
             * polarities.get(tree); System.out.println(pol.toString()); } // we
             * check the families Iterator<String> otherit =
             * families.keySet().iterator(); while (otherit.hasNext()) { String
             * f = otherit.next(); System.out.println("Family " + f); for(String
             * s : families.get(f)) { System.out.print(s + " "); }
             * System.out.println(); }
             */

            loadTime = System.nanoTime();
            xmlreader = new XmlTagTreeReader(XmlTagTreeReader.MORPH, morph,
                    verbose);
            loadedTime = System.nanoTime() - loadTime;
            totalTime += loadedTime;
            System.err.println("Morph lexicon loading time         : "
                    + (loadedTime) / (Math.pow(10, 9)) + " sec.");

            Map<String, List<MorphEntry>> morphs = xmlreader.getMorphs();
            /*
             * Iterator<String> mit = morphs.keySet().iterator(); while
             * (mit.hasNext()) { String t = mit.next();
             * System.out.println("Morph " + t); MorphEntry tt = morphs.get(t);
             * System.out.println(tt.toString()); }
             */

            loadTime = System.nanoTime();
            xmlreader = new XmlTagTreeReader(XmlTagTreeReader.LEMMA, lemma,
                    verbose);
            loadedTime = System.nanoTime() - loadTime;
            totalTime += loadedTime;
            System.err.println("Lemma lexicon loading time         : "
                    + (loadedTime) / (Math.pow(10, 9)) + " sec.");

            Map<String, List<Lemma>> lemmas = xmlreader.getLemmas();
            Map<String, List<String>> coanchors = xmlreader.getCoanchors();
            /*
             * Iterator<String> lit = lemmas.keySet().iterator(); while
             * (lit.hasNext()) { String t = lit.next();
             * System.out.println("Lemma " + t); Lemma tt = lemmas.get(t);
             * System.out.println(tt.toString()); }
             */
            // ---------------------------------------
            // We attach the lexicons to the grammar:
            zesubgrammar.setLemmas(lemmas);
            zesubgrammar.setMorphEntries(morphs);
            // ---------------------------------------

            loadTime = System.nanoTime();
            LexicalSelection ls = new LexicalSelection(polarities, tokens,
                    verbose);
            ls.retrieve(morphs, lemmas, families);
            Map<String, List<InstantiatedTagTree>> selected = ls.getSelected();
            loadedTime = System.nanoTime() - loadTime;
            totalTime += loadedTime;
            System.err.println("Unrestricted lexical selection time: "
                    + (loadedTime) / (Math.pow(10, 9)) + " sec.");

            loadTime = System.nanoTime();
            fr.loria.disambiguation.PolarityAutomaton pa = new fr.loria.disambiguation.PolarityAutomaton(
                    toksentence, selected, axiom, verbose, lexNodes, coanchors,
                    withLeftContext);
            loadedTime = System.nanoTime() - loadTime;
            totalTime += loadedTime;
            System.err.println("Polarity automaton building time   : "
                    + (loadedTime) / (Math.pow(10, 9)) + " sec.");

            loadTime = System.nanoTime();
            List<List<List<String>>> sets = pa.getPossibleSets();
            loadedTime = System.nanoTime() - loadTime;
            totalTime += loadedTime;
            System.err.println("Polarity automaton exploration time: "
                    + (loadedTime) / (Math.pow(10, 9)) + " sec.\n");

            if (verbose)
                System.err.println("Valid sets:");
            int num = 0;
            for (List<List<String>> listofsets : sets) {
                // res += listofsets.toString();
                res += "Sol. #" + num + ":\n";
                num += 1;
                int which = 0;
                if (toksentence == null) {
                    System.err.println(
                            "Tokenization error: please make sure the vocabulary is in the lexicon.");
                } else {
                    for (String s : toksentence) {
                        res += s + ": ";
                        if (which < listofsets.size()) {
                            res += listofsets.get(which);
                            which += 1;
                        } else {
                            res += " no known associated tree";
                        }
                        res += "\n";
                    }
                }
                if (verbose)
                    System.err.println(listofsets);
            }
            System.err.println("Total polarity filtering time      : "
                    + (totalTime) / (Math.pow(10, 9)) + "sec.\n");
            // --------------------------------------------------------

            System.err.println("Statistics:");
            System.err.println("\tAutomaton size: " + pa.getTotalStates());
            System.err.println("\t#Removed states - left constraint: "
                    + pa.getLeftRemoved());
            System.err.println("\t#Removed states - minimization   : "
                    + pa.getMergedRemoved() + "\n");

            System.err.println("\t@@##Tree combinations prior to filtering: "
                    + ls.getambig() + "\n");
            // System.err.println("\t#Tree combinations prior to filtering: " +
            // CollectionUtilities.computeCartesianCard(selected, toksentence) +
            // "\n"); // WRONG
            // System.err.println("\t#Tree combinations after left filtering: "
            // + CollectionUtilities.computeCartesianCard(sets, toksentence) +
            // "\n"); //WRONG

            List<String> subgrammar = CollectionUtilities
                    .computeSubGrammar(sets);

            System.err.println("\tInput grammar size    : " + inputSize);
            System.err.println(
                    "\tOutput subgrammar size: " + subgrammar.size() + "\n");

            if (subgrammar.size() == 0)
                System.err.println(
                        "******************\nPolarity-based lexical selection failed. There may be an unknow word. Use -v to check.\n******************\n");

            result.setPrettyRes(res);

            xmlreader = new XmlTagTreeReader();
            if (gram != null) {
                loadTime = System.nanoTime();
                xmlreader.filter(subgrammar, gram, verbose);
                loadedTime = System.nanoTime() - loadTime;
                System.err.println("XML filtering time                 : "
                        + (loadedTime) / (Math.pow(10, 9)) + " sec.\n");
            } else {
                CommandLineProcesses.error("Grammar not found.", op);
            }

            Map<String, TagTree> grammar = xmlreader.getTrees(); // the actual
                                                                 // filtering
                                                                 // operation
            /*
             * Iterator<String> git = grammar.keySet().iterator(); while
             * (git.hasNext()) { String t = git.next();
             * System.out.println("** Tree " + t); TagTree tt = grammar.get(t);
             * System.out.println(tt.toString("  ")); }
             */
            result.setSubgrammar(grammar);

            // --------------------------------------------------------------------
            // Encapsulation of the subgrammar into a TTMCTAG (for
            // compatibility):
            Iterator<String> git2 = grammar.keySet().iterator();
            while (git2.hasNext()) {
                String t = git2.next();
                TagTree tt = grammar.get(t);
                Tuple tuple = new Tuple();
                tuple.setHead(tt);
                List<TagTree> tup = new LinkedList<TagTree>();
                tup.add(tt);
                tuple.setFamily(tt.getFamily());
                tuple.setId("tuple-" + tt.getId());
                zesubgrammar.add2family(tt.getFamily(), tuple);
            }
            // --------------------------------------------------------------------

            if (!outFile.equals("")) {
                System.err.println("Writing XML file ... " + outFile);
                loadTime = System.nanoTime();
                XMLcopy.produceOutXML(subgrammar, gram, verbose, outFile);
                loadedTime = System.nanoTime() - loadTime;
                System.err.println("XML writing time                   : "
                        + (loadedTime) / (Math.pow(10, 9)) + " sec.\n");
            }

        } catch (Throwable t) {
            if (verbose)
                t.printStackTrace();
            CommandLineProcesses.error(
                    "Error while reading polarities. Please check your grammar file and your <grammar>-pol.xml file.",
                    op);
        }
        return zesubgrammar;
    }

    public static Tokenizer loadTokenizer(CommandLineOptions op)
            throws TokenizerException, IOException {
        Tokenizer tok = null;
        if (op.check("fileTokenizer")) {
            tok = new FileTokenizer(op.getVal("fileTokenizer"));
        } else {
            String tokenizerMode = "";
            if (op.check("builtinTokenizer")) {
                tokenizerMode = op.getVal("builtinTokenizer");
            } else {
                tokenizerMode = BuiltinTokenizer.GERMAN;
            }
            tok = new BuiltinTokenizer(tokenizerMode);
        }
        return tok;
    }
}
