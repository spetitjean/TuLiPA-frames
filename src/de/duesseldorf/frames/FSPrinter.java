package de.duesseldorf.frames;

import java.util.Iterator;
import java.util.Set;

import de.tuebingen.tag.Fs;
import de.tuebingen.tag.Value;

public class FSPrinter {

    // public String toString() {
    // String res = "";
    //
    // if (isTyped()) {
    // res = "("+coref+")" + res + type + " - ";
    //
    // }
    //
    // Set<String> keys = AVlist.keySet();
    // Iterator<String> i = keys.iterator();
    // while (i.hasNext()) {
    // String k = (String) i.next();
    // res += k + " = " + AVlist.get(k).toString() + ", ";
    // }
    // if (res.length() > 2) {
    // // we remove the last ", "
    // res = res.substring(0, (res.length() - 2));
    // }
    // return res;
    // }

    /**
     * returns a naive string representation of a (probably recursive) typed or
     * untyped FS
     * 
     * @param fs
     * @return
     */
    public static String printFS(Fs fs) {
        String res = "<p>";
        res += printFS(fs, 0);
        res += "</p>";
        return res;
    }

    private static String printFS(Fs fs, int recursiondepth) {

        StringBuffer sb = new StringBuffer();

        if (fs.isTyped()) {
            sb.append(fs.getCoref());
            sb.append("<br>");
            sb.append(appRecDepth(recursiondepth));
            sb.append("type: ");
            sb.append(fs.getType().toString());
            sb.append("</br>");
        }

        Set<String> keys = fs.getAVlist().keySet();
        Iterator<String> i = keys.iterator();
        while (i.hasNext()) {
            String k = i.next();
            sb.append("<br>");
            sb.append(appRecDepth(recursiondepth));
            sb.append(k);
            sb.append(" = ");

            Value v = fs.getAVlist().get(k);

            if (v.is(Value.AVM)) {
                sb.append(printFS(v.getAvmVal(), recursiondepth + k.length()));
            } else if (v.is(Value.VAL)) {
                sb.append(v.getSVal());
                sb.append("</br>");
            } else if (v.is(Value.VAR)) {
                sb.append(v.getVarVal());
            } else {
                sb.append("minor FSPrinter fuckup: " + v.toString());
            }
        }
        return sb.toString();
    }

    private static String appRecDepth(int recursiondepth) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < recursiondepth; i++) {
            sb.append("&nbsp;&nbsp;&nbsp;");
        }
        return sb.toString();
    }
}
