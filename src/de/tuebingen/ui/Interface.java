/*
 *  File Interface.java
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
package de.tuebingen.ui;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import javax.swing.SwingUtilities;

import de.duesseldorf.frames.Situation;
import de.duesseldorf.rrg.RRG;
import de.duesseldorf.ui.CommandLineProcesses;
import de.duesseldorf.ui.ParsingInterface;
import de.duesseldorf.ui.WorkbenchLoader;
import de.tuebingen.tag.TTMCTAG;
import de.tuebingen.tokenizer.TokenizerException;
import de.tuebingen.tree.Grammar;

// for debugging outside the eclipse console:
//import java.io.PrintStream;

public class Interface {

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

        CommandLineOptions op = CommandLineProcesses.processCommandLine(args);
        boolean gui = false;

        if (op.check("v")) {
            System.err.println("User options:");
            System.err.println(op.toString());
        }

        // options checking
        if (op.check("h")) {
            System.out.println(CommandLineProcesses.printHelp());
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
            System.err.println(CommandLineProcesses.printUsage());
            System.exit(1);
        }
        // initialization
        String gram = op.check("g") ? op.getVal("g") : "";
        String fram = op.check("f") ? op.getVal("f") : "";
        String lem = op.check("l") ? op.getVal("l") : "";
        String mo = op.check("m") ? op.getVal("m") : "";
        String th = op.check("th") ? op.getVal("th") : "";
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
        if (op.check("s") || op.check("i")
                || (op.check("b") && !op.check("tag"))) { // NB: batch mode may
                                                          // require grammar
                                                          // reloading if option
                                                          // tag is used
            try {
                WorkbenchLoader.loadSituation(op, gram, fram, lem, mo, th);
                g = Situation.getGrammar();
                frameG = Situation.getFrameGrammar();

            } catch (Exception e) {
                e.printStackTrace();
                CommandLineProcesses.error(
                        "Error while loading grammar: check your grammar file",
                        op);
            }
            // System.err.println(g.toString());
        }

        if (op.check("s")) { // single parse mode
            String sentence = op.getVal("s");
            try {
                if (g instanceof TTMCTAG) {
                    // Select the CYK parser if no parsing mode was specified
                    // when startingthe parser without GUI and providing a
                    // sentence.
                    if (!op.check("cyktag") && !op.check("tag2rcg")) {
                        op.setVal("cyktag", "");
                    }
                    ParsingInterface.parseTAG(op, (TTMCTAG) g, sentence);
                } else if (g instanceof RRG) {
                    ParsingInterface.parseRRG(op, sentence);
                } else {
                    // RCG/CFG/simple RCG parse
                    ParsingInterface.parseNonTAG(op, g, sentence);
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
                sentence = CommandLineProcesses.printPrompt(history);
                if (sentence != null) {
                    if (sentence.startsWith("-1")) {
                        quit = true;
                    } else {
                        try {
                            // // tag2rcg conversion
                            // if (op.check("tag2rcg")) {
                            // ParsingInterface.parseSentence(op, g, sentence);}
                            if (g instanceof TTMCTAG) {
                                ParsingInterface.parseTAG(op, (TTMCTAG) g,
                                        sentence);
                            } else if (g instanceof RRG) {
                                ParsingInterface.parseRRG(op, sentence);
                            } else {
                                // RCG/CFG/simple RCG parse
                                ParsingInterface.parseNonTAG(op, g, sentence);
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
            if (!(op.check("x") || op.check("xg")))
                op.setVal("x", "true"); // to deactivate graphical output
                                        // interface
            // parse the input
            while ((is = r.readLine()) != null) {
                try {
                    if (op.check("r") || op.check("c") || op.check("lcfrs")) {// RCG
                                                                              // parsing
                        op.setOurVal("o", out + i + ".xml");
                        // RCG parse
                        ParsingInterface.parseNonTAG(op, g, is);
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
                                WorkbenchLoader.loadSituation(op, gram, lem,
                                        mo);
                                g = Situation.getGrammar();
                                frameG = Situation.getFrameGrammar();
                                op.removeVal("s"); // reinit once the filtering
                                // is done
                            } catch (Exception e) {
                                e.printStackTrace();
                                CommandLineProcesses.error(
                                        "Error while loading grammar: please check your command line options",
                                        op);
                            }
                            // we remove the "simple" lexical disambiguation,
                            // since the extended one has been performed:
                            op.setVal("nofiltering", "true");
                        }
                        ParsingInterface.parseTAG(op,
                                (TTMCTAG) Situation.getGrammar(), is);
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
        System.out.println("Done parsing. Goodbye!");
        System.exit(0);
    }

}
