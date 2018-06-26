package de.duesseldorf.rrg.extractor;

import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentSkipListMap;

import de.duesseldorf.util.GornAddress;

public class GAShiftHandler {
    // for every node somewhere right a GA (the key), how many positions is it
    // shifted to the right? (value)
    private Map<GornAddress, Integer> shifts;

    public GAShiftHandler() {
        this.shifts = new ConcurrentSkipListMap<GornAddress, Integer>();
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
        return newAddress;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        for (Entry<GornAddress, Integer> s : shifts.entrySet()) {
            sb.append(s.getKey());
            sb.append("\t");
            sb.append(s.getValue());
            sb.append("\n");
        }

        return sb.toString();
    }

}
