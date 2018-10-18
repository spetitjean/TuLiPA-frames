/**
 * 
 */
package de.duesseldorf.frames;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import de.tuebingen.tag.Fs;

/**
 * @author david
 *         The frame serves as a holder for a feature structure (Fs) and a set
 *         of relations
 */
public class Frame {

    private List<Fs> featureStructures = new LinkedList<Fs>();
    private Set<Relation> relations = new HashSet<Relation>();

    public Frame() {
    }

    public Frame(List<Fs> featureStructures, Set<Relation> relations) {
        this.featureStructures = featureStructures;
        this.relations = relations;
    }

    public List<Fs> getFeatureStructures() {
        return featureStructures;
    }

    public void addToFeatureStructures(Fs fs) {
        featureStructures.add(fs);
    }

    public Set<Relation> getRelations() {
        return relations;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        if (!featureStructures.isEmpty()) {
            for (Fs fs : featureStructures) {
                sb.append(fs.toString());
                sb.append("\n");
            }
        }
        if (!relations.isEmpty()) {
            sb.append("\n");
            for (Relation rel : relations) {
                sb.append(rel.toString());
                sb.append("\n");
            }
        }
        return sb.toString();
    }
}