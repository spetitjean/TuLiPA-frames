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
public class GornAddress implements Comparable<GornAddress> {

    // represented as a list of Integers
    private List<Integer> address;

    private int STARTCOUNTINGAT = 0;

    public GornAddress() {
        address = new LinkedList<Integer>();
    }

    public GornAddress(GornAddress other) {
        this.address = new LinkedList<Integer>();
        for (Integer i : other.address) {
            this.address.add(i);
        }
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

    /**
     * 
     * @return an integer ii such that this GA is the ith daughter of some node.
     */
    public int isIthDaughter() {
        return this.address.get(address.size() - 1);
    }

    List<Integer> getAddress() {
        return address;
    }

    /**
     * Another GA is bigger iff it is a GA right to and/or below this GA.
     * 
     * @param other
     * @return
     */
    public int compareTo(GornAddress other) {
        if (other.equals(this)) {
            return 0;
        }
        if (address.size() < other.getAddress().size()
                || isIthDaughter() < other.isIthDaughter()) {
            return -1;
        } else {
            return 1;
        }
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
