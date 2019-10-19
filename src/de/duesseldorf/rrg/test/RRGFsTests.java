package de.duesseldorf.rrg.test;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.stream.StreamResult;

import de.duesseldorf.frames.Situation;
import de.duesseldorf.rrg.RRG;
import de.duesseldorf.rrg.RRGParseResult;
import de.duesseldorf.rrg.RRGParseTree;
import de.duesseldorf.rrg.io.BracketedRRGFromStringsReader;
import de.duesseldorf.rrg.io.RRGXMLBuilder;
import de.duesseldorf.rrg.parser.RRGParser;

public class RRGFsTests {

    private static List<List<String>> trees = new LinkedList<List<String>>();
    private static List<List<String>> sentences = new LinkedList<List<String>>();

    private static void fillTrees() {
        List<String> thePianoHasBeenDrinkingWithoutFeatures = new LinkedList<String>();
        thePianoHasBeenDrinkingWithoutFeatures.add("The\t(NP* (DEF-OP <>))");
        thePianoHasBeenDrinkingWithoutFeatures.add("piano\t(NP (N <>))");
        thePianoHasBeenDrinkingWithoutFeatures.add("has\t(CO* (TNS-OP <>))");
        thePianoHasBeenDrinkingWithoutFeatures.add("been\t(NUC* (ASP-OP <>))");
        thePianoHasBeenDrinkingWithoutFeatures
                .add("drinking\t(CL (CO (NP )(NUC (V <>))))");
        trees.add(thePianoHasBeenDrinkingWithoutFeatures);
        sentences.add(Arrays.asList("The piano has been drinking".split(" ")));

        List<String> thePianoHasBeenDrinkingWithNodeFeatures = new LinkedList<String>();
        thePianoHasBeenDrinkingWithNodeFeatures
                .add("The\t(NP*[DEF=+] (DEF-OP[OP=NP] <>))");
        thePianoHasBeenDrinkingWithNodeFeatures.add("piano\t(NP (N <>))");
        thePianoHasBeenDrinkingWithNodeFeatures
                .add("has\t(CO*[TENSE=pres] (TNS-OP[OP=CL] <>))");
        thePianoHasBeenDrinkingWithNodeFeatures
                .add("been\t(NUC*[ASP=perf] (ASP-OP[OP=NUC] <>))");
        thePianoHasBeenDrinkingWithNodeFeatures
                .add("drinking\t(CL (CO (NP )(NUC[TNS=-] (V <>))))");
        trees.add(thePianoHasBeenDrinkingWithNodeFeatures);
        sentences.add(Arrays.asList("The piano has been drinking".split(" ")));

        List<String> thePianoHasBeenDrinkingWithLRFeatures = new LinkedList<String>();
        thePianoHasBeenDrinkingWithLRFeatures.add(
                "The\t(NP* (DEF-OP[l=[DEF=+,OPS=[NP=+]],r=[DEF=-,OPS=[NP=-]]] <>))");
        thePianoHasBeenDrinkingWithLRFeatures.add("piano\t(NP (N <>))");
        thePianoHasBeenDrinkingWithLRFeatures
                .add("has\t(CO* (TNS-OP[l=[TNS=+,OPS=[CL=+]],r=[TNS=-]] <>))");
        thePianoHasBeenDrinkingWithLRFeatures.add(
                "been\t(NUC* (ASP-OP[l=[OPS=[NUC=+]],r=[OPS=[CL=-,CO=-]]] <>))");
        thePianoHasBeenDrinkingWithLRFeatures.add(
                "drinking\t(CL (CO[l=[TNS=+]] (NP )(NUC (V[l=[OPS=[CL=-,CO=-,NUC=-]],r=[OPS=[CL=-,CO=-]]] <>))))");

        trees.add(thePianoHasBeenDrinkingWithLRFeatures);
        sentences.add(Arrays.asList("The piano has been drinking".split(" ")));

        List<String> thePianoHasBeenDrinkingWithNodeAndLRFeatures = new LinkedList<String>();
        thePianoHasBeenDrinkingWithNodeAndLRFeatures.add(
                "The\t(NP*[DEF=+] (DEF-OP[OP=NP,l=[DEF=+,OPS=[NP=+]],r=[DEF=-,OPS=[NP=-]]] <>))");
        thePianoHasBeenDrinkingWithNodeAndLRFeatures.add("piano\t(NP (N <>))");
        thePianoHasBeenDrinkingWithNodeAndLRFeatures.add(
                "has\t(CO*[TENSE=pres] (TNS-OP[OP=CL,l=[TNS=+,OPS=[CL=+]],r=[TNS=-]] <>))");
        thePianoHasBeenDrinkingWithNodeAndLRFeatures.add(
                "been\t(NUC*[ASP=perf] (ASP-OP[OP=NUC,l=[OPS=[NUC=+]],r=[OPS=[CL=-,CO=-]]] <>))");
        thePianoHasBeenDrinkingWithNodeAndLRFeatures.add(
                "drinking\t(CL (CO[l=[TNS=+]] (NP )(NUC[TNS=-] (V[l=[OPS=[CL=-,CO=-,NUC=-]],r=[OPS=[CL=-,CO=-]]] <>))))");
        trees.add(thePianoHasBeenDrinkingWithNodeAndLRFeatures);
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
            RRGParseResult parseResult = new RRGParser("")
                    .parseSentence(sentences.get(i));
            System.out.println("parsed sentence: " + sentences.get(i));
            for (RRGParseTree parseTree : parseResult.getSuccessfulParses()) {
                System.out.println(parseTree);
            }
            StreamResult resultStream = new StreamResult(
                    "testout_" + i + ".xml");
            try {
                new RRGXMLBuilder(resultStream, parseResult, false)
                        .buildAndWrite();
            } catch (ParserConfigurationException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }

    }

}