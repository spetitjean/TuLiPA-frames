package de.duesseldorf.frames;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import de.tuebingen.anchoring.NameFactory;

public class FrameUpdater {

    private Frame frame;
    private NameFactory nf;

    public FrameUpdater(Frame frame, NameFactory nf) {
        this.frame = frame;
        this.nf = nf;
    }

    /**
     * assigns new names to the varaibles in the frame, based on the NameFactory
     *
     * @return
     */
    public Frame rename() {
        List<Fs> oldFSs = frame.getFeatureStructures();

        List<Fs> resultingFeatureStructures = new LinkedList<Fs>();
        for (Fs oldFs : oldFSs) {
            Fs newFs = new Fs(oldFs, nf);
            resultingFeatureStructures.add(newFs);
        }
        Set<Relation> oldRels = frame.getRelations();
        Set<Relation> newRels = new HashSet<Relation>();
        if (!oldRels.isEmpty()) {
            // System.out.println(
            // "FrameUpdater.rename does not handle relations yet!");#
            for (Relation oldRel : oldRels) {
                newRels.add(oldRel.rename(nf));
            }
        }
        Frame result = new Frame(resultingFeatureStructures, newRels);
        return result;
    }

}
