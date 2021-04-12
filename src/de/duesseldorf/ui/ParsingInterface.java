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

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.stream.StreamResult;

import de.duesseldorf.ui.webgui.RRGLocalWebGUI;
import org.w3c.dom.Document;

import de.duesseldorf.frames.Situation;
import de.duesseldorf.io.SentenceListFromFileCreator;
import de.duesseldorf.parser.SlimTAGParser;
import de.duesseldorf.rrg.RRG;
import de.duesseldorf.rrg.RRGParseResult;
import de.duesseldorf.rrg.RRGParseTree;
import de.duesseldorf.rrg.RRGTools;
import de.duesseldorf.rrg.RRGTree;
import de.duesseldorf.rrg.RRGTreeTools;
import de.duesseldorf.rrg.anchoring.RRGAnchorMan;
import de.duesseldorf.rrg.io.RRGXMLBuilder;
import de.duesseldorf.rrg.parser.RRGParser;
import de.tuebingen.anchoring.NameFactory;
import de.tuebingen.anchoring.TreeSelector;
import de.tuebingen.converter.GrammarConvertor;
import de.tuebingen.disambiguate.ComputeSubGrammar;
import de.tuebingen.disambiguate.PolarityAutomaton;
import de.tuebingen.disambiguate.PolarizedToken;
import de.tuebingen.expander.DOMderivationBuilder;
import de.tuebingen.forest.ProduceDOM;
import de.tuebingen.forest.Rule;
import de.tuebingen.forest.Tidentifier;
import de.tuebingen.gui.DerivedTreeViewer;
import de.tuebingen.gui.ParseTreeCollection;
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
import de.tuebingen.tag.Environment;

public class ParsingInterface {

    public static boolean omitPrinting = true;

    public static boolean parseTAG(CommandLineOptions op, TTMCTAG g,
                                   String sentence) throws Exception {
        // you might want to check if everything is in order here
        try {
            // System.out.println(sit.getGrammar().toString());
            // System.out.println(sit.getFrameGrammar().toString());
            // System.out.println(sit.getTypeHierarchy().toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
        boolean res = false;
        long totalTime = 0;
        boolean verbose = op.check("v");
        boolean noUtool = op.check("n");
        boolean needsAnchoring = Situation.getGrammar().needsAnchoring();
        Document fdoc = null;

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

        // Tokenizing
        Tokenizer tok = WorkbenchLoader.loadTokenizer(op);
        List<Word> tokens = null;
        tok.setSentence(sentence);
        tokens = tok.tokenize();
        if (verbose) {
            System.err.println("Tokenized sentence: " + tokens.toString());
        }

        List<String> toksentence = Tokenizer.tok2string(tokens);
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
        List<Word> cleantokens = tokens;

        // if (op.check("nofiltering")||op.check("cyktag")) {
        // cleantokens = clean_tokens(tokens);
        // }
        TreeSelector ts = new TreeSelector(cleantokens, verbose);
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
                ts.retrieve(slabels);
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
            if (!(op.check("nofiltering") || op.check("cyktag"))) {
                // --------------------------------------------------------
                // before RCG conversion, we apply lexical disambiguation:
                // --------------------------------------------------------
                List<PolarizedToken> lptk = ts.getPtokens();
                if (verbose) {
                    for (PolarizedToken ptk : lptk) {
                        System.err.println(ptk.toString());
                    }
                }
                System.err.println("########Starting Polarity Automaton ");
                PolarityAutomaton pa = new PolarityAutomaton(toksentence, lptk,
                        axiom, verbose, ts.getLexNodes(), ts.getCoancNodes());
                System.err.println("########Done Polarity Automaton ");
                List<List<String>> tupleSets = pa.getPossibleTupleSets();
                System.err.println("########Got possible tuple sets ");
                subgrammars = ComputeSubGrammar.computeSubGrammar(verbose,
                        tupleSets, ts.getTupleHash(), ts.getTreeHash());
                System.err.println("########Computed sub grammar ");

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
            ts.store(Situation.getGrammar().getGrammar());
        }
        // Tree Selection results stored in specific variables to avoid
        // keeping a pointer to the ts variable (and wasting memory)
        Map<String, TagTree> grammarDict = ts.getTreeHash();
        if (op.check("tag2rcg")) {
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
                    // System.err.println("Converting sub-grammar " + sI +
                    // "...");
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
                GrammarConvertor gc = new GrammarConvertor(anchoredTuples,
                        verbose, toksentence, grammarDict, !needsAnchoring,
                        k_limit, limit);
                gc.buildAllClauses(axiom);
                // OptimizedGrammarConverter ogc = new
                // OptimizedGrammarConverter(anchoredTuples, verbose,
                // toksentence,
                // grammarDict, !needsAnchoring);
                // ogc.buildAllClauses(axiom);
                rcggrammar = new RCG();
                rcggrammar.addGrammar(gc.getRcggrammar(), grammarDict);
                // rcggrammar.addGrammar(ogc.getRcggrammar(), grammarDict);
            }
            long estimatedTime = System.nanoTime() - startTime;
            totalTime += estimatedTime;
            if (rcggrammar == null
                    || rcggrammar.getStartPredicateLabel() == null) {
                // System.err.println("Grammar conversion failed. \nPlease check
                // the
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
             * grammarDict.keySet().iterator(); while(itt.hasNext()) { String
             * ttree
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
                XMLUtilities.writeXML(rcgd, op.getVal("e"), "rcg.dtd,xml",
                        true);
            }

            // 8. RCG parsing
            RCG rcgg = rcggrammar;

            RCGParser rcgparser = null;
            if (op.check("q2") || op.check("earley")) {
                if (verbose)
                    System.err.println("Using CSP earley parser");
                // full parser from Kallmeyer&Maier&Parmentier(2009), using
                // constraint programming
                rcgparser = new RCGParserConstraintEarley(rcgg);
                int verb = (verbose) ? 0 : -2;
                ((RCGParserConstraintEarley) rcgparser).setVerbose(verb);
                ((RCGParserConstraintEarley) rcgparser).setGDict(grammarDict);
            } else if (op.check("full")) {
                if (verbose)
                    System.err.println(
                            "Using Top-Down parser from Boullier (2000)");
                rcgparser = new RCGParserBoullier2(verbose, rcgg, grammarDict,
                        rcgg.getCategories());
            }
            // simple RCG parser is the default
            if (rcgparser == null) {
                if (verbose)
                    System.err.println("Using simple RCG parser");
                rcgparser = new SimpleRCGParserEarley(rcgg);
            }

            // for printing the categories modifiable within the subgrammar:
            // System.out.println(rcgg.getCategories());

            long sTime = System.nanoTime();
            // the first parameter of parseSentence defines the verbosity
            boolean parseres = false;
            try {
                parseres = rcgparser.parseSentence(verbose, tokens);
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
                System.err.println(
                        "Sentence \"" + tok.getSentence() + "\" parsed.");
                // for printing the RCG derivation forest (also printed by the
                // parser in verbose mode)
                // System.err.println(parser.printForest());

                // 9. Forest extraction
                ForestExtractor extractor = ForestExtractorFactory
                        .getForestExtractor(rcgparser);
                extractor.init(verbose, rcgg,
                        rcgparser.getForestExtractorInitializer());
                long fTime = System.nanoTime();
                extractor.extract();
                long estfTime = System.nanoTime() - fTime;

                System.err.println("\nForest extraction time for sentence \""
                        + sentence + "\": " + (estfTime) / (Math.pow(10, 9))
                        + " sec.");

                if (verbose)
                    System.err.println("**" + extractor.printForest());
                fdoc = ProduceDOM.buildDOMForest(extractor.getForest(),
                        extractor.getStart(), tok.getSentence(), op.getVal("g"),
                        new NameFactory(), null);
                // DerivedTreeViewer.displayTreesfromDOM(sentence, fdoc,
                // grammarDict, true, op.check("w"), op.check("w"),
                // needsAnchoring,
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
                res = parseres;
            }
        } else if (op.check("cyktag")) {
            // to be sure that we are as efficient as possible, we do a separate
            // filtering stage here
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
                // System.err.println("########Starting removing words ");
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

            // parse
            long parseTime = System.nanoTime();
            // TAGParser parser = new TAGParser(grammarDict);
            SlimTAGParser parser = new SlimTAGParser(grammarDict);
            Map<Tidentifier, List<Rule>> forest_rules = new HashMap<Tidentifier, List<Rule>>();
            List<Tidentifier> forest_roots = parser.parse(tokens, forest_rules,
                    axiom);
            System.err.println("Parsed");

            // Debug by Simon: some derivations are identical (when
            // several adjunctions happen for instance), we take care
            // of it here waiting for a better fix

            // Map<Tidentifier, List<Rule>> new_forest_rules = new
            // HashMap<Tidentifier, List<Rule>>();
            // List<Tidentifier> new_forest_roots = new
            // LinkedList<Tidentifier>();
            // List<String> new_forest_strings = new LinkedList<String>();
            // Tidentifier defaultTI = forest_roots.get(0);
            // for(Tidentifier ti: forest_roots){
            // //System.out.println("Tidenfifier: "+ti);
            // //System.out.println("Rules:");
            // //List<Rule> new_tree_rules= new LinkedList<Rule>();
            // String new_tree_string = "";
            // for(Rule ru: forest_rules.get(ti)){
            // //System.out.println(ru.getRhs());
            // // we build the same rule removing all identifiers
            // // so that we can compare it

            // // first build the RHS
            // Combination comb = new Combination();
            // for(TreeOp to: ru.getRhs()){
            // Tidentifier ito = new Tidentifier(to.getId());
            // ito.setClauseId(0);
            // comb.addOp(new TreeOp(ito,to.getType()));
            // }

            // Rule r = new Rule(defaultTI);
            // r.setRhs(comb);

            // //new_tree_rules.add(r);
            // new_tree_string=new_tree_string+r.toString();
            // }
            // //new_forest_rules.add(new_tree_rules);
            // if(new_forest_strings.contains(new_tree_string)){
            // System.out.println("Found duplicate!: "+new_tree_string);
            // }
            // else{
            // new_forest_strings.add(new_tree_string);
            // new_forest_rules.put(ti,forest_rules.get(ti));
            // new_forest_roots.add(ti);
            // }
            // }
            // Debug by Simon: some derivations are identical (when
            // several adjunctions happen for instance)
            // We just need to check which trees were used:
            // When a same tree is used twice in a derivation, it's wrong
            // When the same set of trees are used in two derivations, it's
            // wrong

            // TODO: It's through forest rules than one needs to loop, not
            // forest roots!

            // Map<Tidentifier, List<Rule>> new_forest_rules = new
            // HashMap<Tidentifier, List<Rule>>();
            // List<Tidentifier> new_forest_roots = new
            // LinkedList<Tidentifier>();
            // //List<String> new_forest_strings = new LinkedList<String>();
            // //Tidentifier defaultTI = forest_roots.get(0);
            // Set<Set<String>> setUsedTrees = new HashSet<Set<String>>();
            // for(Tidentifier ti: forest_roots){
            // System.out.println("Tidenfifier: "+ti);
            // System.out.println("Rules:");
            // for(Rule ru: forest_rules.get(ti)){
            // System.out.println("One rule: "+ru);
            // }
            // List<Rule> new_tree_rules= new LinkedList<Rule>();
            // String new_tree_string = "";
            // // for(Rule ru: forest_rules.get(ti)){
            // // Set<String> usedTrees = new HashSet<String>();
            // // System.out.println(ru.getRhs());
            // // // we build the same rule removing all identifiers
            // // // so that we can compare it

            // // // first build the RHS
            // // // Combination comb = new Combination();
            // // for(TreeOp to: ru.getRhs()){
            // // Tidentifier ito = new Tidentifier(to.getId());
            // // String treeId= ito.getTreeId();
            // // System.out.println("Tree identifier: "+treeId);
            // // //ito.setClauseId(0);
            // // //comb.addOp(new TreeOp(ito,to.getType()));
            // // if(!usedTrees.contains(treeId)){
            // // usedTrees.add(treeId);
            // // }
            // // else{
            // // System.out.println("Tree found twice in derivation: "+treeId);
            // // }
            // // }
            // // if(!setUsedTrees.contains(usedTrees)){
            // // setUsedTrees.add(usedTrees);
            // // new_tree_rules.add(ru);
            // // }
            // // else{
            // // new_tree_rules.add(ru);
            // // System.out.println("Same derivation found twice: "+usedTrees);
            // // }
            // // //Rule r = new Rule(defaultTI);
            // // //r.setRhs(comb);

            // // //new_tree_rules.add(r);
            // // //new_tree_string=new_tree_string+r.toString();
            // // }
            // //new_forest_rules.add(new_tree_rules);
            // // if(new_forest_strings.contains(new_tree_string)){
            // // System.out.println("Found duplicate!: "+new_tree_string);
            // // }
            // if(false){}
            // else{
            // //new_forest_strings.add(new_tree_string);
            // new_forest_rules.put(ti,forest_rules.get(ti));
            // new_forest_roots.add(ti);
            // }
            // System.out.println("New forest rules:");
            // for(Rule ru: new_forest_rules.get(ti)){
            // System.out.println("One rule: "+ru);
            // }
            // }
            // //forest_roots=new_forest_roots;

            // forest_rules=new_forest_rules;
            // System.out.println("Done with duplicates ");

            long parsingTime = System.nanoTime() - parseTime;
            System.err.println("Total time for parsing and tree extraction: "
                    + (parsingTime) / (Math.pow(10, 9)) + " sec.");
            res = (forest_roots.size() > 0);
            if (res) {
                // visualize parses
                fdoc = ProduceDOM.buildDOMForest(forest_rules, forest_roots,
                        tok.getSentence(), op.getVal("g"), new NameFactory(),
                        null);

                // DEBUG(by TS)
                // XMLUtilities.writeXML(fdoc,"fdoc.xml","tulipa-forest3.dtd,xml",
                // true);
                // END_DEBUG

                // Deactivated, because it crashes with the separate
                // framegrammar file
                // if (op.check("f")) {
                // Document fdoc2 = ProduceDOM.buildDOMForest(forest_rules,
                // forest_roots, tok.getSentence(), op.getVal("g"),
                // new NameFactory(), grammarDict);
                // XMLUtilities.writeXML(fdoc2, op.getVal("f"),
                // "tulipa-forest3.dtd,xml", true);
                // }
            }
        } else {
            System.err.println("Select a parsing mode");
        }
        if (res) {
            // ArrayList<ParseTreeCollection> viewTreesFromD = DerivedTreeViewer
            // .getViewTreesFromDOM(fdoc, sit, grammarDict, false, false,
            // false, needsAnchoring, slabels, noUtool);
            if (op.check("x") || op.check("xg")) { // XML output of the
                // derivations!
                long xmlTime = System.nanoTime();

                ArrayList<ParseTreeCollection> viewTreesFromDOM = DerivedTreeViewer
                        .getViewTreesFromDOM(fdoc, grammarDict, false, false,
                                false, needsAnchoring, slabels, noUtool);
                if (op.check("x")) {
                    DOMderivationBuilder standardDerivationBuilder = new DOMderivationBuilder(
                            sentence);
                    Document dparses = standardDerivationBuilder
                            .buildDOMderivation(viewTreesFromDOM);
                    XMLUtilities.writeXML(dparses, outputfile,
                            "tulipa-parses.dtd,xml", true);
                } else if (op.check("xg")) {
                    DOMderivationBuilder webguiDerivationBuilder = new DOMderivationBuilder(
                            sentence);
                    Document dwebguiparses = webguiDerivationBuilder
                            .buildDOMderivationGrammar(viewTreesFromDOM);
                    XMLUtilities.writeXML(dwebguiparses, outputfile,
                            "tulipa-parses.dtd,xml", true);
                }
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
                // XMLUtilities.writeXML(fdoc, "stdout",
                // "tulipa-forest3.dtd,xml",
                // true);
                long dDTime = System.nanoTime() - estDTime;
                System.err.println("Derivation trees extraction time: "
                        + (dDTime) / (Math.pow(10, 9)) + " sec.");
                totalTime += dDTime;
            }

            // if (op.check("d")) { // dependency output
            // // cannot set file and path to dependency output from the
            // // jar
            // // String deppath = op.getVal("d") + File.separator;
            // String deppath = System.getProperty("user.dir")
            // + File.separator;
            // String depfile = "dependencies.xml";
            // String pdffile = "structure-*.pdf";
            // // get dependencies
            // DependencyExtractor de = new DependencyExtractor(tokens,
            // fdoc, grammarDict, needsAnchoring);
            // de.processAll();
            // // System.err.println(de.toString());
            // Document ddd = DependencyDOMbuilder.buildAllDep(tokens,
            // sentence, de.getDependences());
            // XMLUtilities.writeXML(ddd, deppath + depfile,
            // "http://w3.msi.vxu.se/~nivre/research/MALTXML.dtd,xml",
            // false);
            // System.err.println("XML dependency structures available in "
            // + deppath + File.separator + depfile + ".");
            // DTool.toPDF(System.getProperty("user.dir") + File.separator
            // + depfile);
            // System.err.println("PDF dependency structures available in "
            // + deppath + File.separator + pdffile + ".");
            // }

        } else {
            System.err.println("No derivation forest available.");
        }
        // total = loading + anchoring + conversion + parsing + forest + XML
        // /
        // derivation trees
        System.err.println("\nTotal parsing time for sentence \"" + sentence
                + "\": " + totalTime / (Math.pow(10, 9)) + " sec.");

        return res;
    }
    // END_BY_TS

    public static List<Word> clean_tokens(List<Word> tokens) {
        List<Word> clean = new LinkedList<Word>();
        Set<String> mem = new HashSet<String>();
        for (Word word : tokens) {
            if (!mem.contains(word.getWord())) {
                clean.add(word);
                mem.add(word.getWord());
            }
        }
        return clean;
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

    public static boolean parseRRG(CommandLineOptions op, String sent)
            throws Exception {
        //// 0: estup
        omitPrinting = op.check("omitPrint");
        boolean returnValue = false;
        boolean verbose = op.check("v");

        // store number of results so we don't need to store actual output
        List<Integer> batchparsingResultSizes = new LinkedList<>();

        List<String> sentences = new LinkedList<String>();
        if (op.check("b") && !(op.getVal("o").matches(".*[0-9].*xml"))) {
            sentences.addAll(new SentenceListFromFileCreator(op.getVal("b"))
                    .getListRepresentation());
        } else {
            sentences.add(sent);
        }
	System.err.println(sentences);
        //// 1 parse sentences
        long startParsingTime = System.nanoTime();
        Integer sentenceCounter = 0;
        for (String sentence : sentences) {

	    // it seems like the grammar trees and their environments get modified during parsing
	    // this should probably not happen
	    ((RRG) Situation.getGrammar()).resetAnchoredTrees();
	    for (RRGTree rrgtree : ((RRG) Situation.getGrammar()).getTrees()){
	    	rrgtree.setEnv(new Environment(5));
		
	    }

	    List<String> toksentence = Arrays.asList(sentence.split("\\s+"));
            RRGParseResult result = new RRGParseResult.Builder().build();
            System.out.println("RRG grammar needs anchoring: "
                    + ((Boolean) Situation.getGrammar().needsAnchoring())
                    .toString());
            RRGAnchorMan anchorman = new RRGAnchorMan(toksentence);
            Set<RRGTree> treesInvolvedInParsing = anchorman.anchor();
	    
            if (!op.check("brack2XML")) {
                ExecutorService executor = Executors.newCachedThreadPool();
                Callable<RRGParseResult> task = new Callable<RRGParseResult>() {
                    public RRGParseResult call() {
                        RRGParser rrgparser = new RRGParser(op.getVal("a"),
                                treesInvolvedInParsing);
                        return rrgparser.parseSentence(toksentence);
                    }
                };
                Future<RRGParseResult> future = executor.submit(task);
                try {
                    result = future.get(500, TimeUnit.SECONDS);
                } catch (Exception e) {
                    System.out.println("parsing failed due to exception: " + e);
                    e.printStackTrace();
                    System.exit(1);
                } finally {
                    future.cancel(true);
                }
            } else { // hack for converting .tsv grammar to xml grammar
                Set<RRGParseTree> elementaryTreeSet = RRGTools.convertTreeSet(
                        (((RRG) Situation.getGrammar()).getTrees()));
                result = new RRGParseResult.Builder()
                        .successfulParses(elementaryTreeSet).build();
            }
            returnValue = !result.getSuccessfulParses().isEmpty();
            if (op.check("b")) {
                batchparsingResultSizes
                        .add(result.getSuccessfulParses().size());
            } else if (!omitPrinting) {
                for (RRGParseTree rrgParseTree : result.getSuccessfulParses()) {
                    // System.out.println("Extraction steps for " +
                    // SrrgParseTree.getId());
                    // System.out.println(rrgParseTree.extractionstepsPrinted());
                    System.out.println("result for " + rrgParseTree.getId());
                    System.out.println(RRGTreeTools
                            .asStringWithNodeLabelsAndNodeType(rrgParseTree));
                    // System.out.println(
                    // "Environment: " + rrgParseTree.getEnv().toString());
                }
                for (RRGParseTree tree : result
                        .getTreesWithEdgeFeatureMismatches()) {
                    System.out.println(
                            "tree with feature mismatch " + tree.getId());
                    System.out.println(RRGTreeTools
                            .asStringWithNodeLabelsAndNodeType(tree));
                }
                System.out.println(
                        "result:\n" + result.getSuccessfulParses().size()
                                + "\tgood trees.");
                System.out.println(
                        result.getTreesWithEdgeFeatureMismatches().size()
                                + "\ttrees with edge feature mismatches");
            }

            // XML Output
            if (op.check("xg") || op.check("b")) {
                StreamResult resultStream;
                if (op.check("o")) {
                    String fileName = op.getVal("o");
                    if (op.check("b") && !(op.getVal("o").matches(".*[0-9].*xml"))) {
                        if (fileName.endsWith(".xml")) {
                            fileName = fileName.substring(0,
                                    fileName.length() - 4);
                            fileName = fileName + "_" + sentenceCounter;
                            fileName += ".xml";
                        } else {
                            fileName = "_" + sentenceCounter + fileName;
                        }
                    }
                    resultStream = new StreamResult(fileName);
                } else {
                    resultStream = new StreamResult(System.out);
                }
                try {
                    RRGXMLBuilder rrgXMLBuilder = new RRGXMLBuilder(result, op.check("edgemismatch"));
                    rrgXMLBuilder.build();
                    rrgXMLBuilder.write(resultStream);
                } catch (ParserConfigurationException e) {
                    System.err.println(
                            "could not build parse results due to ParserConfigurationException");
                }
            } else {
                System.out.println("no output file specified with option -o");
            }

            // call the GUI
            if (!op.check("no-gui") && !op.check("b") && !op.check("xg") && !op.check("x")) {
                RRGXMLBuilder rrgxmlBuilder = new RRGXMLBuilder(result, op.check("edgemismatch"));
                int possiblePort = RRGLocalWebGUI.defaultPort;
                if (op.check("gui")) {
                    possiblePort += RRGLocalWebGUIs.numberOfRunningGUIs();
                }
                int port = op.check("port") ? Integer.parseInt(op.getVal("port")) : possiblePort;
                RRGLocalWebGUI webGUI = new RRGLocalWebGUI(port);
                RRGLocalWebGUIs.addLocalWebGUI(webGUI);
                webGUI.displayParseResults(sentence, rrgxmlBuilder);
            }
            // next round
            sentenceCounter = sentenceCounter + 1;

        }
        if (op.check("b")) {
            System.out.println(
                    "Batch parsing: sentence and number of resulting parse trees");
            for (int i = 0; i < batchparsingResultSizes.size(); i++) {
                System.out.println(i + "\t" + batchparsingResultSizes.get(i));
            }
        }
        long parsingTime = System.nanoTime() - startParsingTime;
        System.err.println(
                "Total time : " + (parsingTime) / (Math.pow(10, 9)) + " sec.");
        return returnValue;
    }

    /**
     * @param op
     * @param sentence
     * @param verbose
     * @throws TokenizerException
     * @throws IOException
     */
    private static List<String> tokenize(CommandLineOptions op, String sentence,
                                         boolean verbose) throws TokenizerException, IOException {
        Tokenizer tok = WorkbenchLoader.loadTokenizer(op);
        List<Word> tokens = null;
        tok.setSentence(sentence);
        tokens = tok.tokenize();
        if (verbose) {
            System.err.println("Tokenized sentence: " + tokens.toString());
        }
        List<String> toksentence = Tokenizer.tok2string(tokens);
        return toksentence;
    }
}
