package de.duesseldorf.rrg.extractor;

import java.util.LinkedList;
import java.util.List;

import de.duesseldorf.util.GornAddress;

/*
 *  File NaiveGAShiftHandler.java
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
public class NaiveGAShiftHandler {

    private class GAShiftStep {

        private GornAddress address;
        private Integer movement;

        public GAShiftStep(GornAddress address, Integer movement) {
            this.address = address;
            this.movement = movement;
        }

        public GornAddress getAddress() {
            return address;
        }

        public Integer getMovement() {
            return movement;
        }
    }

    private List<GAShiftStep> shifts;

    public NaiveGAShiftHandler() {
        this.shifts = new LinkedList<NaiveGAShiftHandler.GAShiftStep>();
    }

    public NaiveGAShiftHandler(NaiveGAShiftHandler other) {
        this.shifts = new LinkedList<NaiveGAShiftHandler.GAShiftStep>(
                other.shifts);
    }

    public GornAddress computeShift(GornAddress address) {
        GornAddress result = new GornAddress(address);
        // compare the input address to all addressees in the list in ascending
        // order.
        // if the address is bigger:
        // shift according to ithDaughter oof that address, and proceed withh
        // the result.
        // if it is smaller:
        // return

        for (GAShiftStep gaShiftStep : shifts) {
            // the result is smaller than the shifted address. We are done.
            if (result.compareTo(gaShiftStep.getAddress()) < 0) {
                System.out.println("return here for " + result + " < "
                        + gaShiftStep.address);
                return result;
            }
            // we need to shift the result at the right place.

            int shiftDepth = gaShiftStep.getAddress().getAddress().size() - 1;
            List<Integer> addressToShift = result.getAddress();
            addressToShift.set(shiftDepth,
                    addressToShift.get(shiftDepth) + gaShiftStep.getMovement());
            result = new GornAddress(addressToShift);
        }

        return result;
    }

    public void addShift(GornAddress address, int rightMovement) {
        GAShiftStep shift = new GAShiftStep(address, rightMovement);
        int depth = address.getAddress().size();
        if (shifts.size() == 0) {
            shifts.add(shift);
            return;
        }
        for (int i = 0; i < shifts.size(); i++) {
            if (depth < shifts.get(i).address.getAddress().size()) {
                shifts.add(i, shift);
                return;
            }
            // same length?
            if (depth == shifts.get(i).address.getAddress().size()) {
                // further right or left?
                if (shifts.get(i).address.getAddress().get(depth - 1) > address
                        .getAddress().get(depth - 1)) {
                    shifts.add(i, shift);
                    return;
                }
            }
        }
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("Address\tMovement to right:\n");
        for (GAShiftStep gaShiftStep : shifts) {
            sb.append(gaShiftStep.getAddress() + "\t"
                    + gaShiftStep.getMovement() + "\n");
        }
        return sb.toString();
    }
}
