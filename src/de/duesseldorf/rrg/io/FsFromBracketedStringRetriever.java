package de.duesseldorf.rrg.io;

import de.duesseldorf.frames.Fs;
import de.duesseldorf.frames.Value;

public class FsFromBracketedStringRetriever {
    public SystemLogger log;
    private String fsString;

    public FsFromBracketedStringRetriever(String fsString) {
        this.fsString = fsString;
    }

    /**
     * format: Core*[OP=CLAUSE,OTHER=[SOMEATTR=SOMEVAL]]
     * This doess not capture fs of a "depth" > 1 at the momemnt
     * possibly going recursive, though in practice the fs will be rather small
     * 
     * @param fsString
     * @return
     */
    public Fs createFsFromString() {
        Fs result = new Fs(1);
        return createFsFromString(result);
    }

    private Fs createFsFromString(Fs result) {
        // System.out.println("fsStr at start: " + fsString);
        if (fsString.startsWith("[") || fsString.startsWith(",")) {
            fsString = fsString.substring(1);
            int eqIndex = fsString.indexOf("=");
            if (eqIndex == -1) {
                // no eq in the string
                return result;
            }
            String attr = fsString.substring(0, eqIndex);
            // extract the value. The +1 because fsString starts with = at that
            // point
            fsString = fsString.substring(eqIndex + 1);

            Value val;
            if (fsString.startsWith("[")) {
                // extract a Fs value
                FsFromBracketedStringRetriever valRetriever = new FsFromBracketedStringRetriever(
                        fsString);
                val = new Value(valRetriever.createFsFromString());
                fsString = valRetriever.fsString;
            } else {
                // extract a simple value
                int endOfSimpleValIndex = fsString.contains(",")
                        && (fsString.indexOf(",") < fsString.indexOf("]"))
                                ? fsString.indexOf(",") : fsString.indexOf("]");
                String simpleValStr = fsString.substring(0,
                        endOfSimpleValIndex);
                val = new Value(Value.Kind.VAL, simpleValStr);
                fsString = fsString.substring(endOfSimpleValIndex);
            }
            result.setFeatWithoutReplace(attr, val);
        }
        if (fsString.length() < 3 || fsString.startsWith("]")) {
            // remove leading "]"
            if (fsString.startsWith("]")) {
                fsString = fsString.substring(1);
            }
            return result;
        }
        return createFsFromString(result);
    }
}