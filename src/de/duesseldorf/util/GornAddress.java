package de.duesseldorf.util;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/*
 *  File GornAddress.java
 *
 *  Authors:
 *     David Arps <david.arps@hhu.de
 *
 *  Copyright:
 *     David Arps, 2018
 *
 *  This file is part of the TuLiPA system
 *     https://github.com/spetitjean/TuLiPA-frames
 *
 *  TuLiPA is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  TuLiPA is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
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

    public GornAddress(List<Integer> list) {
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
     * @return a new GornAddress that is the address of the left sister of this
     * one. Return {@code null} if this one is a root or a leftmost
     * sister.
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
     * @return a new GornAddress that is the address of the right sister of this
     * one. Return {@code null} if this one is a root.
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
     * @return A new {@code GornAddress} that is the i-th daughter of this
     * GornAddress.
     */
    public GornAddress ithDaughter(int i) {
        List<Integer> newaddress = new LinkedList<Integer>(address);
        newaddress.add(i);
        return new GornAddress(newaddress);
    }

    /**
     * @return A new {@code GornAddress} that is the mother of this one. Return
     * null if {@code this} is a root.
     */
    public GornAddress mother() {
        if (address.isEmpty()) {
            return null;
        }
        return new GornAddress(address.subList(0, address.size() - 1));
    }

    /**
     * @return an integer i such that this GA is the ith daughter of some node.
     * Return -1 if this is a root.
     */
    public int isIthDaughter() {
        if (address.size() == 0) {
            return -1;
        }
        return this.address.get(address.size() - 1);
    }

    public List<Integer> getAddress() {
        return address;
    }

    /**
     * Another GA is bigger iff it is a GA right to and/or below this GA.
     * <p>
     * A
     * |\
     * B C
     * | %
     * C
     * All Addresses C and all nodes in the % field are bigger than B.
     *
     * @param other
     * @return n < 0 iff this is < other
     */
    public int compareTo(GornAddress other) {
        if (other.equals(this)) {
            return 0;
        }
        // one of them is a root
        if (this.address.size() == 0) {
            return -1;
        }
        if (other.address.size() == 0) {
            return 1;
        }

        // see if one address is smaller than another one
        for (int i = 0; i < address.size(); i++) {
            try {
                if (address.get(i) < other.address.get(i)) {
                    return -1;
                }
                // if you cant access the other address, theres an out of bounds
                // exception
            } catch (Exception e) {
                return 1;
            }

        }
        return -1;
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
