package de.duesseldorf.rrg.test;

import java.io.File;

class TestCase {

    String index;
    File inputGrammar;
    File targetGrammar;
    String sentence;

    public TestCase(String index, File inputGrammar, File targetGrammar,
            String sentence) {
        this.index = index;
        this.inputGrammar = inputGrammar;
        this.targetGrammar = targetGrammar;
        this.sentence = sentence;
    }

    public static TestCase buildNewTestCase(String lineFromTestSuite) {
        String[] lineSplit = lineFromTestSuite.split("\t");
        if (lineSplit.length != 3) {
            System.err.println("testsuite not properly configured");
            return null;
        }
        File inputGrammar = new File(lineSplit[1]);
        File targetGrammar = new File(lineSplit[2]);
        return new TestCase(lineSplit[0], inputGrammar, targetGrammar,
                lineSplit[1]);
    }

}