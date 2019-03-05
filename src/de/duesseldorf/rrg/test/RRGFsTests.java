package de.duesseldorf.rrg.test;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import de.duesseldorf.frames.Situation;
import de.duesseldorf.rrg.RRG;
import de.duesseldorf.rrg.RRGParseTree;
import de.duesseldorf.rrg.io.BracketedRRGFromStringsReader;
import de.duesseldorf.rrg.parser.RRGParser;

public class RRGFsTests {

    private static List<List<String>> trees = new LinkedList<List<String>>();
    private static List<List<String>> sentences = new LinkedList<List<String>>();

    private static void fillTrees() {
        List<String> thePianoHasBeenDrinking = new LinkedList<String>();
        thePianoHasBeenDrinking.add("The\t(NP* (DEF-OP <>))");
        thePianoHasBeenDrinking.add("piano\t(NP (N <>))");
        thePianoHasBeenDrinking.add("has\t(CO* (TNS-OP <>))");
        thePianoHasBeenDrinking.add("been\t(NUC* (ASP-OP <>))");
        thePianoHasBeenDrinking.add("drinking\t(CL (CO (NP )(NUC (V <>))))");
        trees.add(thePianoHasBeenDrinking);
        sentences.add(Arrays.asList("The piano has been drinking".split(" ")));

    }

    public static void main(String[] args) {
        fillTrees();

        for (int i = 0; i < trees.size(); i++) {
            // create RRG
            RRG grammar = BracketedRRGFromStringsReader
                    .createRRGFromListOfBracketedStrings(trees.get(i));
            Situation.instantiate(grammar, null, null);
            // parse
            Set<RRGParseTree> parseResult = new RRGParser()
                    .parseSentence(sentences.get(i));
            System.out.println("parsed sentence: " + sentences.get(i));
            for (RRGParseTree parseTree : parseResult) {
                System.out.println(parseTree);
            }
        }

    }

}