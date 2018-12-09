package de.duesseldorf.rrg.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import de.duesseldorf.rrg.RRG;
import de.duesseldorf.rrg.RRGTree;

public class BracketedRRGReader {

    File grammar = null;

    public BracketedRRGReader(File grammar) {
        this.grammar = grammar;
    }

    public RRG parseRRG() {
        Set<RRGTree> resultingTrees = new HashSet<RRGTree>();
        try {
            BufferedReader tsvFileReader = new BufferedReader(
                    new FileReader(grammar));
            String nextLine = tsvFileReader.readLine();
            while (nextLine != null) {
                if (nextLine != "") {
                    // sysout.
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return new RRG(resultingTrees);
    }

}
