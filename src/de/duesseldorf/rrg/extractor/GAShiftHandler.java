package de.duesseldorf.rrg.extractor;

import java.util.Collections;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.TreeMap;

import de.duesseldorf.util.GornAddress;

/*
 *  File GAShiftHandler.java
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
public class GAShiftHandler {
    // for every node somewhere right a GA (the key), how many positions is it
    // shifted to the right? (value)
    private TreeMap<GornAddress, Integer> shifts;

    public GAShiftHandler() {
        this.shifts = new TreeMap<GornAddress, Integer>();
    }

    public void addShift(GornAddress address, int rightMovement) {
        this.shifts.put(address, rightMovement);

    }

    public GornAddress computeShiftedAddress(GornAddress oldAddress) {
        GornAddress newAddress = new GornAddress(oldAddress);
        // oldAddress.

        // walk through all GAs a from small to big. If newAddress > a, shift
        // the right position of newAddress (i.e. shift the level where the
        // actual change happens.)

        for (Entry<GornAddress, Integer> entry : shifts.entrySet()) {
            if (newAddress.compareTo(entry.getKey()) > 0) {
                System.out.println("found something! " + newAddress + " > "
                        + entry.getKey());
            }
        }

        return newAddress;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        for (Entry<GornAddress, Integer> s : shifts.descendingMap()
                .entrySet()) {
            sb.append(s.getKey());
            sb.append("\t");
            sb.append(s.getValue());
            sb.append("\n");
        }

        LinkedList<GornAddress> sortedList = new LinkedList<GornAddress>(
                shifts.keySet());
        Collections.sort(sortedList);
        System.out.println(sortedList);
        sb.append(shifts.navigableKeySet().toString());
        return sb.toString();
    }

}
