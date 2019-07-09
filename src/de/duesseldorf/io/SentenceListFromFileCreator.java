package de.duesseldorf.io;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import de.duesseldorf.rrg.io.SystemLogger;

public class SentenceListFromFileCreator {

    private SystemLogger log;
    private String filename;

    public SentenceListFromFileCreator(String filename) {
        this.filename = filename;
        this.log = new SystemLogger(System.err, true);
    }

    public List<String> getListRepresentation() {
        List<String> result = new LinkedList<String>();

        BufferedReader fileReader;
        try {
            fileReader = new BufferedReader(new FileReader(filename));
        } catch (FileNotFoundException e) {
            log.info("could not read grammar file " + filename);
            return null;
        }
        String nextLine = "";
        try {
            nextLine = fileReader.readLine();
        } catch (Exception e) {
            log.info("could not read first line of file" + filename);
            return null;
        }
        while (nextLine != null) {
            result.add(nextLine);
            try {
                nextLine = fileReader.readLine();
            } catch (IOException e) {
                System.err.println(
                        "error while reading line at or after: " + nextLine);
            }
        }
        try {
            fileReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        log.info(
                "batch processing: retrieved " + result.size() + " sentences.");
        return result;
    }

}
