package de.duesseldorf.rrg.parser;

import de.duesseldorf.rrg.RRGNode;

import java.util.Collection;
import java.util.concurrent.ConcurrentSkipListSet;

public class Agenda {

    private ConcurrentSkipListSet<RRGParseItem> WITHIN;
    private ConcurrentSkipListSet<RRGParseItem> AUXROOT;
    private ConcurrentSkipListSet<RRGParseItem> DDAUGHTER;
    private ConcurrentSkipListSet<RRGParseItem> INITROOT;

    public Agenda() {
        this.WITHIN = new ConcurrentSkipListSet<RRGParseItem>();
        this.AUXROOT = new ConcurrentSkipListSet<RRGParseItem>();
        this.DDAUGHTER = new ConcurrentSkipListSet<RRGParseItem>();
        this.INITROOT = new ConcurrentSkipListSet<RRGParseItem>();
    }

    public void add(RRGParseItem item) {
        if (item.getwsflag()) {
            this.DDAUGHTER.add(item);
        } else if (item.getEqClass().type.equals(RRGNode.RRGNodeType.STAR) && item.getNodePos() == RRGParseItem.NodePos.TOP) {
            this.AUXROOT.add(item);
        } else if (item.getEqClass().isTopClass() && item.getEqClass().isRoot()) {
            this.INITROOT.add(item);
        } else {
            this.WITHIN.add(item);
        }
    }

    public RRGParseItem getNext() {
        return !this.WITHIN.isEmpty() ? this.WITHIN.pollFirst() :
                !this.AUXROOT.isEmpty() ? this.AUXROOT.pollFirst() :
                        !this.DDAUGHTER.isEmpty() ? this.DDAUGHTER.pollFirst() :
                                !this.INITROOT.isEmpty() ? this.INITROOT.pollFirst() : null;
    }

    public Collection<RRGParseItem> getAllItems() {
        Collection<RRGParseItem> result = new ConcurrentSkipListSet<>();
        result.addAll(WITHIN);
        result.addAll(AUXROOT);
        result.addAll(DDAUGHTER);
        result.addAll(INITROOT);
        return result;
    }

    public int size() {
        return WITHIN.size() + AUXROOT.size() + DDAUGHTER.size() + INITROOT.size();
    }

    public boolean isEmpty() {
        return WITHIN.isEmpty() && AUXROOT.isEmpty() && DDAUGHTER.isEmpty() && INITROOT.isEmpty();
    }
}
