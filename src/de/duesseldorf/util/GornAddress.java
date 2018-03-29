package de.duesseldorf.util;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * A Gorn address, start counting at 0
 * 
 * @author david
 *
 */
public class GornAddress {

    // represented as a list of Integers
    private List<Integer> address;

    private int STARTCOUNTINGAT = 0;

    public GornAddress() {
        address = new LinkedList<Integer>();
    }

    private GornAddress(List<Integer> list) {
        address = list;
    }

    public Iterator<Integer> addressIterator() {
        return this.address.iterator();
    }

    public boolean hasLeftSister() {
        if (address.isEmpty()) {
            return false;
        }
        return !this.address.get(address.size() - 1).equals(STARTCOUNTINGAT);
    }

    /**
     * 
     * @return a new GornAddress that is the address of the left sister of this
     *         one. Return {@code null} if this one is a root or a leftmost
     *         sister.
     */
    public GornAddress leftSister() {
        List<Integer> newGAlist = new LinkedList<Integer>(address);
        GornAddress newGA = null;
        if (!newGAlist.isEmpty() && hasLeftSister()) {
            int newGAlistLastIndex = newGAlist.size() - 1;
            newGAlist.set(newGAlistLastIndex,
                    newGAlist.get(newGAlistLastIndex) - 1);
            newGA = new GornAddress(newGAlist);
        }
        return newGA;
    }

    /**
     * 
     * @return a new GornAddress that is the address of the right sister of this
     *         one. Return {@code null} if this one is a root.
     */
    public GornAddress rightSister() {
        List<Integer> newGAlist = new LinkedList<Integer>(address);
        GornAddress newGA = null;
        if (!newGAlist.isEmpty()) {
            int newGAlistLastIndex = newGAlist.size() - 1;
            newGAlist.set(newGAlistLastIndex,
                    newGAlist.get(newGAlistLastIndex) + 1);
            newGA = new GornAddress(newGAlist);
        }
        return newGA;
    }

    /**
     * 
     * @return A new {@code GornAddress} that is the i-th daughter of this
     *         GornAddress.
     */
    public GornAddress ithDaughter(int i) {
        List<Integer> newaddress = new LinkedList<Integer>(address);
        newaddress.add(i);
        return new GornAddress(newaddress);
    }

    /**
     * 
     * @return A new {@code GornAddress} that is the mother of this one. Return
     *         null if {@code this} is a root.
     */
    public GornAddress mother() {
        if (address.isEmpty()) {
            return null;
        }
        return new GornAddress(address.subList(0, address.size() - 1));
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
