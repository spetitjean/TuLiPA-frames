/*
 *  File Interface.java
 *
 *  Authors:
 *     Wolfgang Maier  <wo.maier@uni-tuebingen.de>
 *     Yannick Parmentier <parmenti@sfs.uni-tuebingen.de>
 *     David Arps <david.arps@hhu.de>
 *         
 *  Copyright:
 *     Wolfgang Maier, 2007
 *     Yannick Parmentier, 2007
 *     David Arps, 2017
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

import java.util.Scanner;

import de.tuebingen.ui.CommandLineOptions;
import de.tuebingen.ui.History;

public class CommandLineProcesses {

    private static String charsetName = "UTF-8";
    private static Scanner scanner = new Scanner(System.in, charsetName);

    public static CommandLineOptions processCommandLine(String[] cmdline) {
        // Command line processing
        CommandLineOptions op = new CommandLineOptions();
        // we declare the optional rrg option to use the RRG parser
        op.add(CommandLineOptions.Prefix.DASH, "rrg",
                CommandLineOptions.Separator.BLANK, false);
        // we declare the optional cyktag option to use the SlimTAG parser
        // (which is the default parser for TAG)
        op.add(CommandLineOptions.Prefix.DASH, "cyktag",
                CommandLineOptions.Separator.BLANK, false);
        // we declare the optional tag2rcg option to parse TAGs with rcg
        // conversion
        op.add(CommandLineOptions.Prefix.DASH, "tag2rcg",
                CommandLineOptions.Separator.BLANK, false);
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
        // we declare the OPTIONAL x option (XML output in TuLiPA 2 format)
        op.add(CommandLineOptions.Prefix.DASH, "x",
                CommandLineOptions.Separator.BLANK, false);
        // we declare the OPTIONAL xg option (XML output in XMG Grammar format)
        op.add(CommandLineOptions.Prefix.DASH, "xg",
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
        op.add(CommandLineOptions.Prefix.DASH, "f",
                CommandLineOptions.Separator.BLANK, true);
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
        // input trees in bracketed format
        op.add(CommandLineOptions.Prefix.DASH, "rrgbrin",
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

    /**
     * 
     * @return
     */
    public static String printHelp() {
        String res = "";
        res += "TuLiPA-frames is a parsing architecture for various .\n";
        res += "It is mainly used for parsing Tree Adjoining Grammars and related grammar formalisms.\n";
        res += "More information at https://github.com/spetitjean/TuLiPA-frames \n\n";
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
        res += "-i      (interactive mode, loop waiting for the next sentence to parse)\n\t";
        res += "-b      (batch processing, takes a corpus as input, and creates one XML file per sentence)\n\t";
        res += "-s \"sentence\" (single parse mode)\n\n\t";
        res += "NB: default mode is graphical interface \n\n";

        res += "where OPTIONS are:\n\t";
        res += "for functionalities:\n\t";
        res += "-rrg      parser for RRG as of Kallmeyer and Osswald (2017), "
                + "default parsing algorithm for rrg\n\t";
        res += "-rrgbrin     input as bracketed elementary trees with the lexical element in front, separated by a tab\n\t";
        res += "-cyktag      use CYK parser for TAG, default parsing algorithm\n\t";
        res += "-tag2rcg     (use TAG to RCG conversion)\n\t";
        res += "-nofiltering cancels polarity filtering\n\t";
        res += "-r      (rcg parser, default is TAG/TT-MCTAG)\n\t";
        res += "-k N    (limits the size of the list of pending arguments to N)\n\t";
        res += "-v      (verbose mode, for debugging purposes, information dumped in stderr)\n\t";
        res += "-w      (when used with the graphical interface, displays \n\t";
        res += "         the derivation steps and incomplete derivations \n\t";
        res += "-x      (output the parses in XML TuLiPA 2 format either in stdout or \n\t";
        res += "         in a file if the -o option has been used)\n\t";
        res += "-xg     (output the parses in XML format of XMG grammars either in stdout or \n\t";
        res += "         in a file if the -o option has been used)\n\t";
        res += "-d      (activate the computation of dependency structures in pdf format, \n\t";
        res += "         these Pdf files are named structure-xx.pdf and stored in the working directory)\n\t";
        res += "-h      (prints help)\n\t";

        res += "for inputs:\n\t";
        res += "-g <path to the XML grammar>\n\t";
        res += "-f <path to the XML frame file, if the frames are given separately from the trees>\n\t";
        res += "-th <path to the XML type hierarchy>\n\t";
        res += "-l <path to the XML lemmas>\n\t";
        res += "-m <path to the XML morphs>\n\t";
        res += "-a <axiom> \n\t";

        res += "for output: (default is graphical output)\n\t";
        res += "-o <path to the output XML file>\n\n";
        return res;
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
