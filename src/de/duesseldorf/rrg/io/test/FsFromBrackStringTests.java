package de.duesseldorf.rrg.io.test;

import java.util.LinkedList;
import java.util.List;

import de.duesseldorf.frames.Fs;
import de.duesseldorf.frames.Value;
import de.duesseldorf.frames.Value.Kind;
import de.duesseldorf.rrg.io.FsFromBracketedStringRetriever;

public class FsFromBrackStringTests {
    public static void main(String[] args) {
        List<String> brackStrings = new LinkedList<String>();
        List<Fs> featureStructures = new LinkedList<Fs>();

        // empty
        brackStrings.add("[]");
        featureStructures.add(new Fs());

        // one simple Stringfeat
        brackStrings.add("[first=BOOM]");
        Fs tmpFs = new Fs();
        tmpFs.setFeatWithoutReplace("first", new Value(Kind.VAL, "BOOM"));
        featureStructures.add(tmpFs);

        // 2 simple Stringfeats with !
        brackStrings.add("[first=BOOM,sec=ouch!]");
        Fs tmpFs2 = new Fs();
        tmpFs2.setFeatWithoutReplace("first", new Value(Kind.VAL, "BOOM"));
        tmpFs2.setFeatWithoutReplace("sec", new Value(Kind.VAL, "ouch!"));
        featureStructures.add(tmpFs2);

        // 3 simple rec
        brackStrings.add("[first=[feat=sun]]");
        Fs tmpFs3 = new Fs();
        Fs tmpInternalFs = new Fs();
        tmpInternalFs.setFeatWithoutReplace("feat", new Value(Kind.VAL, "sun"));
        tmpFs3.setFeatWithoutReplace("first", new Value(tmpInternalFs));
        featureStructures.add(tmpFs3);

        // 4 a little more
        brackStrings.add("[first=[feat=sun],sec=[feat=sun]]");
        Fs tmpFs4 = new Fs(tmpFs3);
        Fs tmpInternalFs2 = new Fs(tmpInternalFs);
        // tmpFs4.setFeat("first", new Value(tmpInternalFs2));
        tmpFs4.setFeatWithoutReplace("sec", new Value(tmpInternalFs2));
        System.out.println("tmpfs4: " + tmpFs4);
        featureStructures.add(tmpFs4);

        for (int i = 4; i < brackStrings.size(); i++) {
            String ithString = brackStrings.get(i);
            Fs retrieverResult = new FsFromBracketedStringRetriever(ithString)
                    .createFsFromString();
            Fs realResult = featureStructures.get(i);
            if (realResult.equals(retrieverResult)) {
                System.out.println("successfully retrieved: " + ithString);
                System.out
                        .println("retrieval result is:    " + retrieverResult);
            } else {
                System.out.println("ERROR: tried to retrieve " + ithString);
                System.out.println("Got: " + retrieverResult);
                System.out.println("should be: " + realResult);
            }
        }
    }
}
