package de.duesseldorf.rrg.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import de.duesseldorf.rrg.io.SystemLogger;

public class RRGTestUtils {

    private static SystemLogger log = new SystemLogger(System.err, true);

    public static List<TestCase> loadTestSuite() {
        List<String> treeStrings = new LinkedList<String>();
        BufferedReader tsvFileReader;
        try {
            File testSuite = new File("testcases.tsv");
            tsvFileReader = new BufferedReader(new FileReader(testSuite));
        } catch (FileNotFoundException e) {
            log.info("could not read test suite file");
            return null;
        }
        List<TestCase> testcases = new LinkedList<TestCase>();
        String nextLine;
        try {
            nextLine = tsvFileReader.readLine();
            while (nextLine != null) {
                if (!nextLine.startsWith("#") && nextLine.contains("\t")) {
                    testcases.add(TestCase.buildNewTestCase(nextLine));
                }
                nextLine = tsvFileReader.readLine();
            }
            tsvFileReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return testcases;
    }
}
