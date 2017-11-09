/*
 *  File ParsingInterface.java
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.w3c.dom.Document;

import de.duesseldorf.frames.Situation;
import de.duesseldorf.parser.SlimTAGParser;
import de.saar.chorus.dtool.DTool;
import de.tuebingen.anchoring.NameFactory;
import de.tuebingen.anchoring.TreeSelector;
import de.tuebingen.converter.GrammarConvertor;
import de.tuebingen.dependency.DependencyDOMbuilder;
import de.tuebingen.dependency.DependencyExtractor;
import de.tuebingen.disambiguate.ComputeSubGrammar;
import de.tuebingen.disambiguate.PolarityAutomaton;
import de.tuebingen.disambiguate.PolarizedToken;
import de.tuebingen.expander.DOMderivationBuilder;
import de.tuebingen.forest.ProduceDOM;
import de.tuebingen.forest.Rule;
import de.tuebingen.forest.Tidentifier;
import de.tuebingen.gui.DerivedTreeViewer;
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
import de.tuebingen.tag.TagNode;
import de.tuebingen.tag.TagTree;
import de.tuebingen.tag.Tuple;
import de.tuebingen.tagger.ExternalTagger;
import de.tuebingen.tagger.TaggerException;
import de.tuebingen.tokenizer.Tokenizer;
import de.tuebingen.tokenizer.TokenizerException;
import de.tuebingen.tokenizer.Word;
import de.tuebingen.tree.Grammar;
import de.tuebingen.ui.CommandLineOptions;
import de.tuebingen.util.CollectionUtilities;
import de.tuebingen.util.XMLUtilities;

public class ParsingInterface {

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

    /**
     * 
     * A TAG is converted into a RCG and then parsed.
     * 
     * @param op
     * @param sit
     *            The Situation - constisting of the grammar, possibly frames
     *            and a type hierarchy
     * @param sentence
     * @return
     * @throws Exception
     */
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
        Tokenizer tok = WorkbenchLoader.loadTokenizer(op);
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
                // System.out.println("LexNodes: " + ts.getLexNodes());
                PolarityAutomaton pa = new PolarityAutomaton(toksentence, lptk,
                        axiom, verbose, ts.getLexNodes(), ts.getCoancNodes());
                List<List<String>> tupleSets = pa.getPossibleTupleSets();
                subgrammars = ComputeSubGrammar.computeSubGrammar(verbose,
                        tupleSets, ts.getTupleHash(), ts.getTreeHash());

                System.err.println(
                        "\t@@##Tree combinations before classical polarity filtering: "
                                + ts.getambig());
                System.err.println(
                        "\t@@##Tree combinations after classical polarity filtering: "
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
            // if (false) { // i.e. we used lexical disambiguation
            // System.out.println("Doing subgrammars: " + subgrammars);
            rcggrammar = new RCG();
            for (int sI = 0; sI < subgrammars.size(); sI++) {
                List<Tuple> ltuples = subgrammars.get(sI);
                System.err.println("Converting sub-grammar " + sI + "...");
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
            CommandLineProcesses.error("No parse found.", op);
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

            System.err.println("\nForest extraction time for sentence \""
                    + sentence + "\": " + (estfTime) / (Math.pow(10, 9))
                    + " sec.");

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
                        DerivedTreeViewer.getViewTreesFromDOM(fdoc, sit,
                                grammarDict, false, false, false,
                                needsAnchoring, slabels, noUtool),
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
                DerivedTreeViewer.displayTreesfromDOM(sentence, fdoc, sit,
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

    public static boolean parseTAG(CommandLineOptions op, TTMCTAG g,
            String sentence) throws Exception {
        Situation sit = new Situation(g, null, null);
        return parseTAG(op, sit, sentence);
    }

    // BY TS
    public static boolean parseTAG(CommandLineOptions op, Situation sit,
            String sentence) throws Exception {

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
        Tokenizer tok = WorkbenchLoader.loadTokenizer(op);
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
            try {
                ts.retrieve(sit, slabels);
                // ts.retrieve(g.getMorphEntries(), g.getLemmas(),
                // g.getGrammar(),
                // slabels);
            } catch (Exception e) {
                e.printStackTrace();
            }
            // System.err.println(ts.toString());
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

        // to be sure that we are as efficient as possible, we do a separate
        // filtering stage here
        {
            Set<String> words = new HashSet<String>();
            for (int k = 0; k < tokens.size(); k++)
                words.add(tokens.get(k).getWord());
            // explicitly add empty word
            words.add(null);
            words.add("");

            List<String> to_remove = new LinkedList<String>();

            Iterator<String> its = grammarDict.keySet().iterator();
            while (its.hasNext()) {

                String key = its.next();
                TagTree tree = grammarDict.get(key);

                List<TagNode> nodes = new LinkedList<TagNode>();
                ((TagNode) tree.getRoot()).getAllNodesChildrenFirst(nodes);

                for (TagNode n : nodes) {

                    if (n.getType() == TagNode.LEX
                            && !words.contains(n.getCategory())) {

                        System.err.println("########Removing tree due to word "
                                + n.getCategory());

                        to_remove.add(key);
                        break;
                    }
                }
            }

            for (String r : to_remove)
                grammarDict.remove(r);
        }

        // DEBUG
        // For debugging:
        // Iterator<String> its = grammarDict.keySet().iterator();
        // File save_grammar = new File("save_grammar.txt");
        // FileWriter fw = new FileWriter(save_grammar);
        // while (its.hasNext()) {
        // String k = its.next();
        // TagTree tagt = grammarDict.get(k);
        // System.err.println("printing tree with index "+k+"\n");
        // System.err.println(tagt.toString(""));
        // fw.write("x x\t X\t" + tagt.print() + "\n");
        // }
        // fw.close();
        // END_DEBUG

        long sTime = System.nanoTime();
        // parse
        long parseTime = System.nanoTime();
        // TAGParser parser = new TAGParser(grammarDict);
        SlimTAGParser parser = new SlimTAGParser(grammarDict);
        Map<Tidentifier, List<Rule>> forest_rules = new HashMap<Tidentifier, List<Rule>>();
        List<Tidentifier> forest_roots = parser.parse(tokens, forest_rules,
                axiom);
        long parsingTime = System.nanoTime() - parseTime;
        System.err.println("Total time for parsing and tree extraction: "
                + (parsingTime) / (Math.pow(10, 9)) + " sec.");
        res = (forest_roots.size() > 0);

        if (res) {
            // visualize parses
            Document fdoc = ProduceDOM.buildDOMForest(forest_rules,
                    forest_roots, tok.getSentence(), op.getVal("g"),
                    new NameFactory(), null);

            // DEBUG(by TS)
            // XMLUtilities.writeXML(fdoc,"fdoc.xml","tulipa-forest3.dtd,xml",
            // true);
            // END_DEBUG

            if (op.check("f")) {
                Document fdoc2 = ProduceDOM.buildDOMForest(forest_rules,
                        forest_roots, tok.getSentence(), op.getVal("g"),
                        new NameFactory(), grammarDict);
                XMLUtilities.writeXML(fdoc2, op.getVal("f"),
                        "tulipa-forest3.dtd,xml", true);
            }

            if (op.check("x")) { // XML output of the derivations!
                long xmlTime = System.nanoTime();
                Document dparses = DOMderivationBuilder.buildDOMderivation(
                        DerivedTreeViewer.getViewTreesFromDOM(fdoc, sit,
                                grammarDict, false, false, false,
                                needsAnchoring, slabels, noUtool),
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
                DerivedTreeViewer.displayTreesfromDOM(sentence, fdoc, sit,
                        grammarDict, true, op.check("w"), op.check("w"),
                        needsAnchoring, slabels, noUtool);
                long dDTime = System.nanoTime() - estDTime;
                System.err.println("Derivation trees extraction time: "
                        + (dDTime) / (Math.pow(10, 9)) + " sec.");
                totalTime += dDTime;
            }

            if (op.check("d")) { // dependency output
                // cannot set file and path to dependency output from the jar
                // String deppath = op.getVal("d") + File.separator;
                String deppath = System.getProperty("user.dir")
                        + File.separator;
                String depfile = "dependencies.xml";
                String pdffile = "structure-*.pdf";
                // get dependencies
                DependencyExtractor de = new DependencyExtractor(tokens, fdoc,
                        grammarDict, needsAnchoring);
                de.processAll();
                // System.err.println(de.toString());
                Document ddd = DependencyDOMbuilder.buildAllDep(tokens,
                        sentence, de.getDependences());
                XMLUtilities.writeXML(ddd, deppath + depfile,
                        "http://w3.msi.vxu.se/~nivre/research/MALTXML.dtd,xml",
                        false);
                System.err.println("XML dependency structures available in "
                        + deppath + File.separator + depfile + ".");
                DTool.toPDF(System.getProperty("user.dir") + File.separator
                        + depfile);
                System.err.println("PDF dependency structures available in "
                        + deppath + File.separator + pdffile + ".");
            }

        } else {
            long noTime = System.nanoTime() - sTime;
            totalTime += noTime;
            System.err.println("No derivation forest available.");
        }

        return res;
    }
    // END_BY_TS

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

        Tokenizer tok = WorkbenchLoader.loadTokenizer(op);

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
}
