package de.duesseldorf.frames;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import de.tuebingen.anchoring.NameFactory;


/**
 * @author david
 *         The frame serves as a holder for feature structures (Fs) and a set
 *         of relations
 */
public class Frame {

    private List<Fs> featureStructures = new LinkedList<Fs>();
    private Set<Relation> relations = new HashSet<Relation>();

    public Frame() {
	this.featureStructures = new LinkedList<Fs>();
	this.relations = new HashSet<Relation>();
    }

    public Frame(List<Fs> featureStructures, Set<Relation> relations) {
        this.featureStructures = featureStructures;
        this.relations = relations;
    }

    public Frame(Frame frame, NameFactory nf){
	List<Fs> new_featureStructures = new LinkedList<Fs>();
	for (Fs f: frame.getFeatureStructures()){
	    new_featureStructures.add(new Fs(f, nf));
	}
	Set<Relation> new_relations = new HashSet<Relation>();
	for (Relation rel: frame.getRelations()){
	    new_relations.add(new Relation(rel, nf));
	}
	this.featureStructures = new_featureStructures;
	this.relations = new_relations;
    }

    public List<Fs> getFeatureStructures() {
        return featureStructures;
    }

    public void setFeatureStructures(List <Fs> featureStructures) {
        this.featureStructures=featureStructures;
    }

    public void addToFeatureStructures(Fs fs) {
        featureStructures.add(fs);
    }

    public Set<Relation> getRelations() {
        return relations;
    }

    public void addOtherFrame(Frame other) {
	if(other!=null){
	    featureStructures.addAll(other.getFeatureStructures());
	    relations.addAll(other.getRelations());
	}
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
