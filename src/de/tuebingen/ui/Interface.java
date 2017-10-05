/*
 *  File Interface.java
 *
 *  Authors:
 *     Wolfgang Maier  <wo.maier@uni-tuebingen.de>
 *     Yannick Parmentier <parmenti@sfs.uni-tuebingen.de>
 *     
 *  Copyright:
 *     Wolfgang Maier, 2007
 *     Yannick Parmentier, 2007
 *
 *  Last modified:
 *     Di 16. Okt 09:32:10 CEST 2007
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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import javax.swing.SwingUtilities;

import org.w3c.dom.Document;

import de.duesseldorf.frames.Situation;
import de.duesseldorf.frames.TypeHierarchy;
import de.duesseldorf.io.XMLTypeHierarchyReader;
import de.tuebingen.anchoring.InstantiatedTagTree;
import de.tuebingen.anchoring.LexicalSelection;
//import de.saar.chorus.dtool.DTool;
import de.tuebingen.anchoring.NameFactory;
import de.tuebingen.anchoring.TreeSelector;
import de.tuebingen.converter.GrammarConvertor;
import de.tuebingen.disambiguate.ComputeSubGrammar;
import de.tuebingen.disambiguate.PolarityAutomaton;
import de.tuebingen.disambiguate.PolarizedToken;
import de.tuebingen.expander.DOMderivationBuilder;
import de.tuebingen.forest.ProduceDOM;
import de.tuebingen.gui.DerivedTreeViewer;
import de.tuebingen.io.RCGReader;
import de.tuebingen.io.TextCFGReader;
import de.tuebingen.io.TextRCGReader;
import de.tuebingen.io.XMLLemmaReader;
import de.tuebingen.io.XMLMorphReader;
import de.tuebingen.io.XMLRCGReader;
import de.tuebingen.io.XMLTTMCTAGReader;
import de.tuebingen.lexicon.Lemma;
import de.tuebingen.lexicon.MorphEntry;
import de.tuebingen.parser.ForestExtractor;
import de.tuebingen.parser.ForestExtractorFactory;
import de.tuebingen.parser.RCGParser;
import de.tuebingen.parser.RCGParserBoullier2;
import de.tuebingen.parser.simple.SimpleRCGParserEarley;
import de.tuebingen.parserconstraints.RCGParserConstraintEarley;
import de.tuebingen.rcg.RCG;
import de.tuebingen.rcg.RCGDOMbuilder;
import de.tuebingen.rcg.RCGparseOutput;
import de.tuebingen.tag.TTMCTAG;
import de.tuebingen.tag.TagTree;
import de.tuebingen.tag.Tuple;
import de.tuebingen.tagger.ExternalTagger;
import de.tuebingen.tagger.TaggerException;
import de.tuebingen.tokenizer.BuiltinTokenizer;
import de.tuebingen.tokenizer.FileTokenizer;
import de.tuebingen.tokenizer.Tokenizer;
import de.tuebingen.tokenizer.TokenizerException;
import de.tuebingen.tokenizer.Word;
import de.tuebingen.tree.Grammar;
import de.tuebingen.util.CollectionUtilities;
import de.tuebingen.util.XMLUtilities;
import fr.loria.disambiguation.Polarities;
import fr.loria.filter.XMLcopy;
import fr.loria.io.TransformPolarity;
import fr.loria.io.XmlTagTreeReader;

// for debugging outside the eclipse console:
//import java.io.PrintStream;

public class Interface {

    private static String charsetName = "UTF-8";
    private static Scanner scanner = new Scanner(System.in, charsetName);

    /**
     * Interface to the Tuebingen Parser.
     * 
     * This is entry point for the application.
     * 
     * @param args
     */
    public static void main(String[] args)
            throws IOException, TokenizerException, Exception {

        System.err.println("Welcome - thanks for using TuLiPA "
                + InputGUI.VERSION + ".\n");

        // for debugging outside the eclipse console:
        // System.setErr(new PrintStream("/tmp/err.txt"));

        CommandLineOptions op = processCommandLine(args);
        boolean gui = false;

        if (op.check("v")) {
            System.err.println("User options:");
            System.err.println(op.toString());
        }

        // options checking
        if (op.check("h")) {
            System.out.println(printHelp());
            System.exit(0);
        }

        gui = !(op.check("s")) || (op.check("i")) || (op.check("b"));
        if (gui)
            op.setVal("gui", "");

        if (((op.check("s")) || (op.check("i")) || (op.check("b")))
                && !(op.check("g"))) {
            System.err.println("Argument missing - exit");
            System.err.println(
                    "When the graphical mode is not used, the option g is mandatory.\n");
            System.err.println(printUsage());
            System.exit(1);
        }
        // initialization
        String gram = op.check("g") ? op.getVal("g") : "";
        String lem = op.check("l") ? op.getVal("l") : "";
        String mo = op.check("m") ? op.getVal("m") : "";
        Grammar g = null;
        Grammar frameG = null;

        // axiom's default value is "v"
        String a = op.check("a") ? op.getVal("a") : "v";
        // axiom printed to stderr only in non-graphical mode:
        if (op.check("s") || op.check("b") || op.check("i")) {
            System.err.print("Axiom: ");
            System.err.println(a);
        }

        // if we are not in graphical mode
        // we load the grammar (and potentially lexicons)
        Situation sit = null;
        if (op.check("s") || op.check("i")
                || (op.check("b") && !op.check("tag"))) { // NB: batch mode may
                                                          // require grammar
                                                          // reloading if option
                                                          // tag is used
            try {
                sit = loadGrammar(op, gram, lem, mo);
                g = sit.getGrammar();
                frameG = sit.getFrameGrammar();

            } catch (Exception e) {
                Interface.error(
                        "Error while loading grammar: check your grammar file",
                        op);
                e.printStackTrace();
            }
            // System.err.println(g.toString());
        }

        if (op.check("s")) { // single parse mode
            String sentence = op.getVal("s");
            try {
                if (g instanceof TTMCTAG) {
                    parseSentence(op, sit, sentence);
                    parseSentence(op, g, sentence);
                } else {
                    // RCG/CFG/simple RCG parse
                    parseNonTAG(op, g, sentence);
                }
            } catch (Exception e) {
                System.err.println(e);
                e.printStackTrace();
            }
        } else if (op.check("i")) { // interactive mode
            boolean quit = false;
            String sentence = "";
            History history = new History();

            while (!quit) {
                sentence = printPrompt(history);
                if (sentence != null) {
                    if (sentence.startsWith("-1")) {
                        quit = true;
                    } else {
                        try {
                            if (g instanceof TTMCTAG) {
                                parseSentence(op, g, sentence);
                            } else {
                                // RCG/CFG/simple RCG parse
                                parseNonTAG(op, g, sentence);
                            }
                        } catch (Exception e) {
                            System.err.println(e);
                            e.printStackTrace();
                        }
                    }
                }
            }
        } else if (op.check("b")) { // batch processing
            // Load the input from a file
            File input = new File(op.getVal("b"));
            BufferedReader r = new BufferedReader(new FileReader(input));
            String is = "";
            int i = 0;
            String out = op.check("o") ? op.getVal("o") : "a.out"; // for RCG
                                                                   // output
            if (!op.check("x"))
                op.setVal("x", "true"); // to deactivate graphical output
                                        // interface
            // parse the input
            while ((is = r.readLine()) != null) {
                try {
                    if (op.check("r") || op.check("c") || op.check("lcfrs")) {// RCG
                                                                              // parsing
                        op.setOurVal("o", out + i + ".xml");
                        // RCG parse
                        parseNonTAG(op, g, is);
                    } else {
                        // in batch mode, there is an xml output, with the file
                        // name defined as follows.
                        op.setOurVal("o", out + i + ".xml");
                        // if TAG (left context) polarity filtering is
                        // activated, grammar reloading is necessary
                        if (op.check("tag")) {
                            try {
                                op.setVal("s", "\"" + is + "\""); // we need to
                                                                  // define the
                                                                  // sentence to
                                                                  // parse for
                                                                  // the grammar
                                                                  // filtering
                                sit = loadGrammar(op, gram, lem, mo);
                                g = sit.getGrammar();
                                frameG = sit.getFrameGrammar();

                                op.removeVal("s"); // reinit once the filtering
                                // is done
                            } catch (Exception e) {
                                e.printStackTrace();
                                Interface.error(
                                        "Error while loading grammar: please check your command line options",
                                        op);
                            }
                            // we remove the "simple" lexical disambiguation,
                            // since the extended one has been performed:
                            op.setVal("nofiltering", "true");
                        }
                        parseSentence(op, g, is);
                    }
                } catch (Exception e) {
                    System.err.println(
                            "Parse of sentence \"" + is + "\" failed.");
                }
                i++;
            }
            r.close();
        } else { // graphical mode
            // arguments needs to be final
            final CommandLineOptions ops = op;
            // Input GUI
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    new InputGUI(ops);
                }
            });
        }
    }

    public static CommandLineOptions processCommandLine(String[] cmdline) {
        // Command line processing
        CommandLineOptions op = new CommandLineOptions();
        // we declare the optional th option for the type hierarchy
        op.add(CommandLineOptions.Prefix.DASH, "th",
                CommandLineOptions.Separator.BLANK, true);
        // we declare the g option (for the grammar file)
        op.add(CommandLineOptions.Prefix.DASH, "g",
                CommandLineOptions.Separator.BLANK, true);
        // we declare the l option (for the lemma file)
        op.add(CommandLineOptions.Prefix.DASH, "l",
                CommandLineOptions.Separator.BLANK, true);
        // we declare the m option (for the morph file)
        op.add(CommandLineOptions.Prefix.DASH, "m",
                CommandLineOptions.Separator.BLANK, true);
        // we declare the OPTIONAL b option (for batch processing on a corpus))
        op.add(CommandLineOptions.Prefix.DASH, "b",
                CommandLineOptions.Separator.BLANK, true);
        // we declare the OPTIONAL s option (sentence to parse)
        op.add(CommandLineOptions.Prefix.DASH, "s",
                CommandLineOptions.Separator.BLANK, true);
        // we declare the OPTIONAL o option (output file (XML forest))
        op.add(CommandLineOptions.Prefix.DASH, "o",
                CommandLineOptions.Separator.BLANK, true);
        // we declare the OPTIONAL v option (verbose -- for debugging)
        op.add(CommandLineOptions.Prefix.DASH, "v",
                CommandLineOptions.Separator.BLANK, false);
        // we declare the OPTIONAL w option (with derivation steps -- for
        // grammar debugging)
        op.add(CommandLineOptions.Prefix.DASH, "w",
                CommandLineOptions.Separator.BLANK, false);
        // we declare the OPTIONAL h option (prints help)
        op.add(CommandLineOptions.Prefix.DASH, "h",
                CommandLineOptions.Separator.BLANK, false);
        // we declare the OPTIONAL d option (computing dependencies)
        op.add(CommandLineOptions.Prefix.DASH, "d",
                CommandLineOptions.Separator.BLANK, false);
        // we declare the OPTIONAL a option (axiom (syntactic category))
        op.add(CommandLineOptions.Prefix.DASH, "a",
                CommandLineOptions.Separator.BLANK, true);
        // we declare the OPTIONAL k option (size of the LPA during TT-MCTAG to
        // RCG conversion)
        op.add(CommandLineOptions.Prefix.DASH, "k",
                CommandLineOptions.Separator.BLANK, true);
        // we declare the OPTIONAL i option (interactive mode)
        op.add(CommandLineOptions.Prefix.DASH, "i",
                CommandLineOptions.Separator.BLANK, false);
        // we declare the OPTIONAL x option (XML output)
        op.add(CommandLineOptions.Prefix.DASH, "x",
                CommandLineOptions.Separator.BLANK, false);
        // we declare the OPTIONAL z option (restricted tree to RCG conversion)
        op.add(CommandLineOptions.Prefix.DASH, "z",
                CommandLineOptions.Separator.BLANK, true);
        // we declare the OPTIONAL t option (tagger to use)
        op.add(CommandLineOptions.Prefix.DASH, "t",
                CommandLineOptions.Separator.BLANK, true);
        // we declare the OPTIONAL e option (export RCG)
        op.add(CommandLineOptions.Prefix.DASH, "e",
                CommandLineOptions.Separator.BLANK, true);
        // we declare the OPTIONAL f option (export XML forest)
        // op.add(CommandLineOptions.Prefix.DASH, "f",
        // CommandLineOptions.Separator.BLANK, true);
        // we declare the OPTIONAL u option (utool deactivation)
        op.add(CommandLineOptions.Prefix.DASH, "n",
                CommandLineOptions.Separator.BLANK, false);
        // we declare the OPTIONAL builtinTokenizer option (load builtin
        // tokenizer, mode expected)
        op.add(CommandLineOptions.Prefix.DASH, "builtinTokenizer",
                CommandLineOptions.Separator.BLANK, true);
        // we declare the OPTIONAL fileTokenizer option (load custom tokenizer
        // from file)
        op.add(CommandLineOptions.Prefix.DASH, "fileTokenizer",
                CommandLineOptions.Separator.BLANK, true);
        // we declare the OPTIONAL lcfrs option (LCFRS/simple RCG mode)
        op.add(CommandLineOptions.Prefix.DASH, "lcfrs",
                CommandLineOptions.Separator.BLANK, false);
        // we declare the OPTIONAL r option (RCG mode)
        op.add(CommandLineOptions.Prefix.DASH, "r",
                CommandLineOptions.Separator.BLANK, false);
        // we declare the OPTIONAL c option (CFG mode)
        op.add(CommandLineOptions.Prefix.DASH, "c",
                CommandLineOptions.Separator.BLANK, false);
        // we declare the OPTIONAL q2 option (use earley algorithm with CSP)
        op.add(CommandLineOptions.Prefix.DASH, "q2",
                CommandLineOptions.Separator.BLANK, false);
        op.add(CommandLineOptions.Prefix.DASH, "earley",
                CommandLineOptions.Separator.BLANK, false);
        // we declare the OPTIONAL full option (force old Boullier algorithm for
        // TAG parsing)
        op.add(CommandLineOptions.Prefix.DASH, "full",
                CommandLineOptions.Separator.BLANK, false);
        // we declare the OPTIONAL tag option (TAG parsing with extended lexical
        // disambiguation)
        op.add(CommandLineOptions.Prefix.DASH, "tag",
                CommandLineOptions.Separator.BLANK, false);
        // we declare the OPTIONAL nofiltering option (no "simple" lexical
        // disambiguation)
        op.add(CommandLineOptions.Prefix.DASH, "nofiltering",
                CommandLineOptions.Separator.BLANK, false);
        // we declare the OPTIONAL gui option (gui is used)
        op.add(CommandLineOptions.Prefix.DASH, "gui",
                CommandLineOptions.Separator.BLANK, false);
        // we declare the OPTIONAL global option (no left context lexical
        // selection)
        op.add(CommandLineOptions.Prefix.DASH, "global",
                CommandLineOptions.Separator.BLANK, false);

        // we compile the patterns for parsing the command line
        op.prepare();
        // we concatenate the command line
        String line = "";
        for (int i = 0; i < cmdline.length; i++) {
            // System.out.println(cmdline[i]);
            String tmp = cmdline[i];
            tmp = tmp.replace(" ", "---");
            // System.out.println(tmp);
            line += "\"" + tmp + "\" ";
        }
        // we parse the command line
        op.parse(line);
        return op;
    }

    /**
     * Loads a grammar without separate file for frames
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
    public static Situation loadGrammar(CommandLineOptions op, String gram,
            String lem, String mo) throws Exception {
        return loadGrammar(op, gram, null, lem, mo, null);
    }

    /**
     * Loads a grammar with a separate file for frames
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
    public static Situation loadGrammar(CommandLineOptions op, String gram,
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
        } else { // TAG/TT-MCTAG parsing
            // if the tag option is enabled (SAX-based loading with extended
            // lexical disambiguation)
            if (op.check("tag")) {
                // first we add pre-computed polarities to the input TAG grammar
                String res = buildPolarities(op);
                if (res == null) {
                    Interface.error("Error while building polarities", op);
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
                    Interface.error(
                            "Anchoring needed, lexicon files not found.", op);
                } else if (op.check("l") && !(op.getVal("l").equals(""))
                        && !(lem.equals(""))) {
                    lemmas = new File(lem);
                    xlr = new XMLLemmaReader(lemmas);
                    g.setLemmas(xlr.getLemmas());
                }
                // 3. Morphs processing
                if (needsAnchoring && !(op.check("m")) && !(mo.equals(""))) {
                    Interface.error(
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

    /**
     * A TAG is converted into a RCG and then parsed.
     * 
     * @param op
     * @param g
     * @param sentence
     * @return true if the sentence is parsed successfully
     * @throws Exception
     */
    public static boolean parseSentence(CommandLineOptions op, Grammar g,
            String sentence) throws Exception {
        Situation sit = new Situation(g, null, null);
        return parseSentence(op, sit, sentence);

    }

    public static boolean parseSentence(CommandLineOptions op, Situation sit,
            String sentence) throws Exception {

        // you might want to check if everything is in order here
        // try {
        // // System.out.println(sit.getGrammar().toString());
        // // System.out.println(sit.getFrameGrammar().toString());
        // // System.out.println(sit.getTypeHierarchy().toString());
        // } catch (Exception e) {
        // System.err.println("sth is null in Interface.622");
        // e.printStackTrace();
        // }
        boolean res = false;
        long totalTime = 0;
        boolean verbose = op.check("v");
        boolean noUtool = op.check("n");
        boolean needsAnchoring = sit.getGrammar().needsAnchoring();

        String outputfile = "";
        if (op.check("o")) {
            outputfile = op.getVal("o");
        } else {
            outputfile = "stdout";
        }
        String axiom = "v"; // default axiom's value is v
        if (op.check("a")) {
            axiom = op.getVal("a");
        }

        List<String> slabels = new LinkedList<String>();

        // 4. Load the tokenizer
        Tokenizer tok = loadTokenizer(op);
        List<Word> tokens = null;
        tok.setSentence(sentence);
        tokens = tok.tokenize();
        if (verbose) {
            System.err.println("Tokenized sentence: " + tokens.toString());
        }
        List<String> toksentence = Tokenizer.tok2string(tokens);
        // System.err.println("\t@@ Length " + toksentence.size());

        /* ******** external POS tagging ************/
        ExternalTagger tagger = new ExternalTagger();
        File taggerExec = op.check("t") ? new File(op.getVal("t")) : null;
        tagger.setExec(taggerExec);
        tagger.setParams("");
        try {
            tagger.doTagging(tokens);
        } catch (TaggerException e) {
            System.err.println(" ********** Tagging Exception *********");
            System.err.println(e.toString());
        }
        // ExternalTagger.printPosToken(tokens);
        /* ******************************************/

        // 5. Lexical selection and Anchoring
        TreeSelector ts = new TreeSelector(tokens, verbose);
        List<List<Tuple>> subgrammars = null;

        if (needsAnchoring) {
            long ancTime = System.nanoTime();
            // 5-a. According to the tokens, we retrieve the pertinent morph
            // entries
            // 5-b. According to the morph entries, we instantiate the pertinent
            // lemmas
            // 5-c. According to the instantiated lemmas, we instantiate the
            // pertinent tuples
            // 6. Tree anchoring
            ts.retrieve(sit, slabels);
            // ts.retrieve(g.getMorphEntries(), g.getLemmas(), g.getGrammar(),
            // slabels);
            // System.out.println(ts.toString());
            // System.err.println(ts.getTupleHash());

            long anchoredTime = System.nanoTime() - ancTime;
            System.err.println("Grammar anchoring time: "
                    + (anchoredTime) / (Math.pow(10, 9)) + " sec.");
            if (verbose)
                System.err.println("Anchoring results:\n" + ts.toString());
            totalTime += anchoredTime;
            if (!op.check("nofiltering")) {
                // --------------------------------------------------------
                // before RCG conversion, we apply lexical disambiguation:
                // --------------------------------------------------------
                List<PolarizedToken> lptk = ts.getPtokens();
                if (verbose) {
                    for (PolarizedToken ptk : lptk) {
                        System.err.println(ptk.toString());
                    }
                }
                PolarityAutomaton pa = new PolarityAutomaton(toksentence, lptk,
                        axiom, verbose, ts.getLexNodes(), ts.getCoancNodes());
                List<List<String>> tupleSets = pa.getPossibleTupleSets();
                subgrammars = ComputeSubGrammar.computeSubGrammar(verbose,
                        tupleSets, ts.getTupleHash(), ts.getTreeHash());

                System.err.println(
                        "\t@@##Tree combinations before classical polarity filtering   : "
                                + ts.getambig());
                System.err.println(
                        "\t@@##Tree combinations after classical polarity filtering   : "
                                + CollectionUtilities.computeAmbig(tupleSets,
                                        toksentence)
                                + "\n");

                if (verbose) {
                    System.err.println("Valid tuple sets:\n" + tupleSets);
                    // System.err.println("\nCorresponding sub-grammars:\n" +
                    // subgrammars);
                }
                // --------------------------------------------------------
            } else {
                System.err.println(
                        "\n\t@@##Tree combinations after left polarity filtering   : "
                                + ts.getambig() + "\n");
            }
        } else {
            ts.store(sit.getGrammar().getGrammar());
        }
        // Tree Selection results stored in specific variables to avoid
        // keeping a pointer to the ts variable (and wasting memory)
        Map<String, TagTree> grammarDict = ts.getTreeHash();
        List<Tuple> anchoredTuples = ts.getAnctuples();
        // For debugging:
        /*
         * Iterator<String> its = grammarDict.keySet().iterator(); while
         * (its.hasNext()) { String k = its.next(); TagTree tagt =
         * grammarDict.get(k); System.err.println(tagt.toString("")); }
         * for(Tuple t : ts.getAnctuples()) { System.err.println(t); }
         */

        // 7. RCG conversion
        int limit = op.check("z") ? Integer.parseInt(op.getVal("z")) : -1;
        int k_limit = op.check("k") ? Integer.parseInt(op.getVal("k")) : -1;

        RCG rcggrammar = null;
        long startTime = System.nanoTime();
        if (subgrammars != null) { // i.e. we used lexical disambiguation
            rcggrammar = new RCG();
            for (int sI = 0; sI < subgrammars.size(); sI++) {
                List<Tuple> ltuples = subgrammars.get(sI);
                // System.err.println("Converting sub-grammar " + sI + "...");
                GrammarConvertor gc = new GrammarConvertor(ltuples, verbose,
                        toksentence, grammarDict, !needsAnchoring, k_limit,
                        limit);
                gc.buildAllClauses(axiom);
                rcggrammar.addGrammar(gc.getRcggrammar(), grammarDict);
                // OptimizedGrammarConverter ogc = new
                // OptimizedGrammarConverter(anchoredTuples, verbose,
                // toksentence, grammarDict, !needsAnchoring);
                // ogc.buildAllClauses(axiom);
                // rcggrammar.addGrammar(ogc.getRcggrammar(), grammarDict);
            }
        } else {
            GrammarConvertor gc = new GrammarConvertor(anchoredTuples, verbose,
                    toksentence, grammarDict, !needsAnchoring, k_limit, limit);
            gc.buildAllClauses(axiom);
            // OptimizedGrammarConverter ogc = new
            // OptimizedGrammarConverter(anchoredTuples, verbose, toksentence,
            // grammarDict, !needsAnchoring);
            // ogc.buildAllClauses(axiom);
            rcggrammar = new RCG();
            rcggrammar.addGrammar(gc.getRcggrammar(), grammarDict);
            // rcggrammar.addGrammar(ogc.getRcggrammar(), grammarDict);
        }
        long estimatedTime = System.nanoTime() - startTime;
        totalTime += estimatedTime;
        if (rcggrammar == null || rcggrammar.getStartPredicateLabel() == null) {
            // System.err.println("Grammar conversion failed. \nPlease check the
            // value of the axiom.");
            throw new Exception(
                    "Polarity filtering / grammar conversion failed. \nPlease check the value of the axiom and the lexicon.");
        } else
            System.err.println("Grammar conversion time: "
                    + (estimatedTime) / (Math.pow(10, 9)) + " sec.");
        // for printing the RCG grammar computed
        // either pretty printed:
        // System.out.println(rcggrammar.toString(ts.getTreeHash()) + "\n");
        // --------------------------------------------------------------
        // or not (i.e. the real grammar + id interpretations):
        /*
         * //if (true) { if (verbose) { Iterator<String> itt =
         * grammarDict.keySet().iterator(); while(itt.hasNext()) { String ttree
         * = itt.next(); System.err.println("Tree " + ttree + "\t := " +
         * grammarDict.get(ttree).getOriginalId() + " " +
         * grammarDict.get(ttree).toString("")); }
         * //System.err.println(rcggrammar.toString()); }
         */

        // 7'. RCG XML export (for DyALog)
        if (op.check("e")) {
            System.err.println(rcggrammar.toString(grammarDict));
            Document rcgd = RCGDOMbuilder.exportGrammar(rcggrammar,
                    grammarDict);
            XMLUtilities.writeXML(rcgd, op.getVal("e"), "rcg.dtd,xml", true);
        }

        // 8. RCG parsing
        RCG rcgg = rcggrammar;

        RCGParser parser = null;
        if (op.check("q2") || op.check("earley")) {
            if (verbose)
                System.err.println("Using CSP earley parser");
            // full parser from Kallmeyer&Maier&Parmentier(2009), using
            // constraint programming
            parser = new RCGParserConstraintEarley(rcgg);
            int verb = (verbose) ? 0 : -2;
            ((RCGParserConstraintEarley) parser).setVerbose(verb);
            ((RCGParserConstraintEarley) parser).setGDict(grammarDict);
        } else if (op.check("full")) {
            if (verbose)
                System.err
                        .println("Using Top-Down parser from Boullier (2000)");
            parser = new RCGParserBoullier2(verbose, rcgg, grammarDict,
                    rcgg.getCategories());
        }
        // simple RCG parser is the default
        if (parser == null) {
            if (verbose)
                System.err.println("Using simple RCG parser");
            parser = new SimpleRCGParserEarley(rcgg);
        }

        // for printing the categories modifiable within the subgrammar:
        // System.out.println(rcgg.getCategories());

        long sTime = System.nanoTime();
        // the first parameter of parseSentence defines the verbosity
        boolean parseres = false;
        try {
            parseres = parser.parseSentence(verbose, tokens);
        } catch (Exception e) {
            // to get the stats anyway (even if there is no parse)
            long estTime = System.nanoTime() - sTime;
            System.err.println(
                    "Total parsing time (no parse found) for sentence \""
                            + sentence + "\": "
                            + (totalTime + estTime) / (Math.pow(10, 9))
                            + " sec.");
            // ----------------------------------------------------
            Interface.error("No parse found.", op);
        }
        long estTime = System.nanoTime() - sTime;
        System.err.println(
                "Parsing time: " + (estTime) / (Math.pow(10, 9)) + " sec.");
        if (parseres) {
            System.err
                    .println("Sentence \"" + tok.getSentence() + "\" parsed.");
            // for printing the RCG derivation forest (also printed by the
            // parser in verbose mode)
            // System.err.println(parser.printForest());

            // 9. Forest extraction
            ForestExtractor extractor = ForestExtractorFactory
                    .getForestExtractor(parser);
            extractor.init(verbose, rcgg,
                    parser.getForestExtractorInitializer());
            long fTime = System.nanoTime();
            extractor.extract();
            long estfTime = System.nanoTime() - fTime;

            if (verbose)
                System.err.println("**" + extractor.printForest());

            Document fdoc = ProduceDOM.buildDOMForest(extractor.getForest(),
                    extractor.getStart(), tok.getSentence(), op.getVal("g"),
                    new NameFactory(), null);
            // DerivedTreeViewer.displayTreesfromDOM(sentence, fdoc,
            // grammarDict, true, op.check("w"), op.check("w"), needsAnchoring,
            // slabels, noUtool);
            // 9'. forest XML export
            // if (op.check("f")) {
            // Document fdoc2 = ProduceDOM.buildDOMForest(
            // extractor.getForest(), extractor.getStart(),
            // tok.getSentence(), op.getVal("g"), new NameFactory(),
            // grammarDict);
            // XMLUtilities.writeXML(fdoc2, op.getVal("f"),
            // "tulipa-forest3.dtd,xml", true);
            // }
            // System.err.println("Forest extraction time: "
            // + (estfTime) / (Math.pow(10, 9)) + " sec.");
            // // update the time counter
            // totalTime += estTime + estfTime;

            // 10. GUI or XML output of the parses (and dependencies if needed)
            if (op.check("x")) { // XML output of the derivations!
                long xmlTime = System.nanoTime();
                Document dparses = DOMderivationBuilder.buildDOMderivation(
                        DerivedTreeViewer.getViewTreesFromDOM(fdoc, grammarDict,
                                false, false, false, needsAnchoring, slabels,
                                noUtool),
                        sentence);
                XMLUtilities.writeXML(dparses, outputfile,
                        "tulipa-parses.dtd,xml", true);
                long estXMLTime = System.nanoTime();
                System.err.println(
                        "Parses available (in XML) in " + outputfile + ".");
                estXMLTime = System.nanoTime() - xmlTime;

                System.err.println("XML production time: "
                        + (estXMLTime) / (Math.pow(10, 9)) + " sec.");
                totalTime += estXMLTime;
            } else { // graphical output (default)
                long estDTime = System.nanoTime();
                DerivedTreeViewer.displayTreesfromDOM(sentence, fdoc,
                        grammarDict, true, op.check("w"), op.check("w"),
                        needsAnchoring, slabels, noUtool);
                long dDTime = System.nanoTime() - estDTime;
                System.err.println("Derivation trees extraction time: "
                        + (dDTime) / (Math.pow(10, 9)) + " sec.");
                totalTime += dDTime;
            }

            // if (op.check("d")) { // dependency output
            // // cannot set file and path to dependency output from the jar
            // //String deppath = op.getVal("d") + File.separator;
            // String deppath = System.getProperty("user.dir") + File.separator;
            // String depfile = "dependencies.xml";
            // String pdffile = "structure-*.pdf";
            // // get dependencies
            // DependencyExtractor de = new DependencyExtractor(tokens, fdoc,
            // grammarDict, needsAnchoring);
            // de.processAll();
            // //System.err.println(de.toString());
            // Document ddd = DependencyDOMbuilder.buildAllDep(tokens, sentence,
            // de.getDependences());
            // XMLUtilities.writeXML(ddd, deppath + depfile,
            // "http://w3.msi.vxu.se/~nivre/research/MALTXML.dtd,xml", false);
            // System.err.println("XML dependency structures available in " +
            // deppath + File.separator + depfile + ".");
            // DTool.toPDF(System.getProperty("user.dir") + File.separator +
            // depfile);
            // System.err.println("PDF dependency structures available in " +
            // deppath + File.separator + pdffile + ".");
            // }

            res = true;

        } else {
            long noTime = System.nanoTime() - sTime;
            totalTime += noTime;
            System.err.println("No derivation forest available.");
            res = false;
        }

        // total = loading + anchoring + conversion + parsing + forest + XML /
        // derivation trees
        System.err.println("\nTotal parsing time for sentence \"" + sentence
                + "\": " + (totalTime) / (Math.pow(10, 9)) + " sec.");

        return res;
    }

    public static boolean parseNonTAG(CommandLineOptions op, Grammar g,
            String sentence) throws TokenizerException, IOException {
        boolean res = false;

        RCGParser p = null;

        if (op.check("q2") || op.check("earley")) {
            // full parser from Kallmeyer&Maier&Parmentier(2009), using
            // constraint programming
            p = new RCGParserConstraintEarley(g);
        } else if (op.check("lcfrs") || op.check("c")) {
            p = new SimpleRCGParserEarley(g);
        }
        if (p == null) {
            p = new RCGParserBoullier2(op.check("v"), g, null,
                    new Hashtable<String, Integer>());
        }

        Tokenizer tok = loadTokenizer(op);

        tok.setSentence(sentence);
        List<Word> l = tok.tokenize();

        long startTime = System.nanoTime();
        res = p.parseSentence(op.check("v"), l);
        long estimatedTime = System.nanoTime() - startTime;
        if (res) {
            System.err.println("Sentence \"" + sentence + "\" parsed.");

            // forest stuff for new algorithms not yet implemented
            if (!(p instanceof RCGParserBoullier2)) {
                System.err.println("Parsing time : "
                        + (estimatedTime) / (Math.pow(10, 9)) + " sec.");
            }

            Document doc = RCGparseOutput.produceDOMparse(op.check("v"),
                    (RCG) g, p.getAnswers(), p.getEmptyRHS(), p.getParse());
            String txtForest = RCGparseOutput.printForest(p.getParse());
            System.err.println(txtForest);
            if (op.check("o")) {
                XMLUtilities.writeXML(doc, op.getVal("o"), "rcg-forest.dtd,xml",
                        true);
                System.err.println(
                        "XML parse printed in file : " + op.getVal("o"));
            } else {
                XMLUtilities.writeXML(doc, "stdout", "rcg-forest.dtd,xml",
                        true);
                System.err.println("XML parse printed in stdout");
            }
        } else {
            System.err.println("Sentence \"" + sentence + "\" not parsed.");
        }
        System.err.println("Parsing time : "
                + (estimatedTime) / (Math.pow(10, 9)) + " sec.");
        return res;
    }

    public static String printPrompt(History history) {
        String res = "";
        System.err.println(
                "Please enter a sentence to parse: (-1 to quit, + or - followed by enter for history)");
        res = scanner.nextLine();
        if (res.equals("+")) {
            res = history.getNext();
            System.err.println(res);
        } else if (res.equals("-")) {
            res = history.getPrevious();
            System.err.println(res);
        } else {
            history.add(res);
            history.setLast();
        }
        return res;
    }

    public static String printHelp() {
        String res = "";
        res += "TuLiPA is a parsing architecture based on Range Concatenation Grammars.\n";
        res += "It is mainly used for parsing TT-MCTAGs.\n";
        res += "More information at http://www.sfb441.uni-tuebingen.de/emmy/tulipa/index.html \n\n";
        res += printUsage();
        return res;
    }

    /**
     * 
     * @return returns user information about TuLiPa
     */
    public static String printUsage() {
        String res = "";
        res += "Usage: java -jar tulipa-xx.jar MODE OPTIONS \n\n";

        res += "where MODE is one of:\n\t";
        res += "-i 		(interactive mode, loop waiting for the next sentence to parse)\n\t";
        res += "-b		(batch processing, takes a corpus as input, and creates one XML file per sentence)\n\t";
        res += "-s \"sentence\"	(single parse mode)\n\n\t";
        res += "NB: default mode is graphical interface \n\n";

        res += "where OPTIONS are:\n\t";
        res += "for functionalities:\n\t";
        res += "-r      (rcg parser, default is TAG/TT-MCTAG)\n\t";
        res += "-k N    (limits the size of the list of pending arguments to N)\n\t";
        res += "-v      (verbose mode, for debugging purposes, information dumped in stderr)\n\t";
        res += "-w      (when used with the graphical interface, displays \n\t";
        res += "         the derivation steps and uncomplete derivations";
        res += "-x      (output the parses in XML format either in stdout or \n\t";
        res += "         in a file if the -o option has been used)\n\t";
        res += "-d      (activate the computation of dependency structures in pdf format, \n\t";
        res += "         these Pdf files are named structure-xx.pdf and stored in the working directory)\n\t";
        res += "-h      (prints help)\n\t";

        res += "for inputs:\n\t";
        res += "-g <path to the XML grammar>\n\t";
        res += "-f <path to the XML frame file>\n\t";
        res += "-th <path to the XML type hierarchy>\n\t";
        res += "-l <path to the XML lemmas>\n\t";
        res += "-m <path to the XML morphs>\n\t";
        res += "-a <axiom> \n\t";

        res += "for output: (default is graphical output)\n\t";
        res += "-o <path to the output XML file>\n\n";
        return res;
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
            Interface.error("Tokenizing error.", op);
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
            Interface.error(
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
                Interface.error("Grammar not found.", op);
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
            Interface.error(
                    "Error while reading polarities. Please check your grammar file and your <grammar>-pol.xml file.",
                    op);
        }
        return zesubgrammar;
    }

    public static void error(String msg, CommandLineOptions op)
            throws Exception {
        if (!op.check("gui")) {
            System.err.println(msg);
            System.exit(1);
        } else
            throw new Exception(msg);
    }

}
