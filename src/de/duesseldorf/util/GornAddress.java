package de.duesseldorf.util;

import java.util.LinkedList;
import java.util.List;

/**
 * A Gorn address is represented as a list of integers.
 * 
 * @author david
 *
 */
public class GornAddress {

    // represented as a list of Integers
    private List<Integer> address;

    public GornAddress() {
        address = new LinkedList<Integer>();
    }

    GornAddress(List<Integer> list) {
        address = list;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        for (Integer integer : address) {
            sb.append(integer);
            sb.append(" ");
        }
        String stringrep = sb.toString();
        if (stringrep.endsWith(" ")) {
            stringrep = stringrep.substring(0, stringrep.length() - 1);
        } else if (address.isEmpty()) {
            stringrep = "eps";
        }
        return stringrep;
    }

}
