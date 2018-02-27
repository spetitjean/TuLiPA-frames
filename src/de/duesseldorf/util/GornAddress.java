package de.duesseldorf.util;

import java.util.LinkedList;
import java.util.List;

/**
 * A Gorn address, start counting at 1
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

    private GornAddress(List<Integer> list) {
        address = list;
    }

    List<Integer> getListRep() {
        return this.address;
    }

    /**
     * 
     * @return A new {@codeGornAddress} that is the i-th daughter of this
     *         GornAddress. Start counting at 1!
     */
    public GornAddress ithDaughter(int i) {
        List<Integer> newaddress = new LinkedList<Integer>(address);
        newaddress.add(i);
        return new GornAddress(newaddress);
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        for (Integer integer : address) {
            sb.append(integer);
            sb.append(".");
        }
        String stringrep = sb.toString();
        if (stringrep.endsWith(".")) {
            stringrep = stringrep.substring(0, stringrep.length() - 1);
        } else if (address.isEmpty()) {
            stringrep = "eps";
        }
        return stringrep;
    }

}
