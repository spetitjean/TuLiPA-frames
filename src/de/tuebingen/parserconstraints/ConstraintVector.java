/*
 *  File ConstraintVector.java
 *
 *  Authors:
 *     Yannick Parmentier <parmenti@loria.fr>
 *     
 *  Copyright:
 *     Yannick Parmentier, 2008
 *
 *  Last modified:
 *     Thu Dec 18 11:42:01 CET 2008
 *
 *  This file is part of the TuLiPA system
 *     http://sourcesup.cru.fr/tulipa
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
package de.tuebingen.parserconstraints;

import java.util.*;

import de.tuebingen.parser.Binding;
import de.tuebingen.rcg.ArgContent;
import de.tuebingen.rcg.RCGInstantiationException;
import de.tuebingen.tokenizer.Word;
import de.tuebingen.util.Pair;

public class ConstraintVector {

	private int                nbOfRanges;  // needed to prepare the structure from which the hash will be computed
	private CVectorKey                cvk;  // to compute a hash
	
	private Map<String, String>  mapNames;  // mapping newName -> original name (clause definition)
	private List<String>       boundaries;  // ranges that are boundaries of the LHS
	private Map<String,ArgContent> ranges;  // all ranges
	private List<Constraint>  constraints;  // constraints on ranges

	private int           sentence_length;  // for initializing the constraints
	private Map<String, Integer>    known;  // for storing the known range boundaries
	                                        // (this field is not used in toStringRenamed() c.f. equals in ConstraintItem)
	
	private boolean            consistent;  // true at first, set to false once we find a range that does not match the input string
	private List<Word>              input;  // we maintain a reference to the tokenized input string for checking consistency
	private Pair                   bounds;  // pair of maps giving integer id to string boundary variable
	
	public ConstraintVector(){
		mapNames     = new HashMap<String, String>();
		boundaries   = new LinkedList<String>();
		ranges       = new HashMap<String,ArgContent>();
		constraints  = new LinkedList<Constraint>();
		known        = new HashMap<String, Integer>();
		consistent   = true;
	}
	
	public ConstraintVector(int n) {
		this();
		//System.err.println(n+" range variables");
		nbOfRanges   = n;
		cvk          = new CVectorKey(n);
	}
	
	@SuppressWarnings("unchecked")
	public ConstraintVector(ConstraintVector cv) {
		this(cv.getNbOfRanges());
		//System.err.println("Copy const: " + cv.print());
		for (String s : cv.getMapNames().keySet()) {
			mapNames.put(s, cv.getMapNames().get(s));
		}
		for (String ac : cv.getBoundaries()) {
			boundaries.add(new String(ac));
		}
		for (String s : cv.getRanges().keySet()) {
			ranges.put(s, new ArgContent(cv.getRanges().get(s)));
		}
		for (Constraint c : cv.getConstraints()) {
			this.addConstraint(new Constraint(c));
		}
		for (String s: cv.getKnown().keySet()) {
			known.put(s, cv.getKnown().get(s));
		}
		sentence_length = cv.getSentence_length();
		consistent = cv.isConsistent();
		input      = cv.getInput(); // no copy needed for this field
		Pair p = cv.getBounds();
		Map<Object, Object> map1 = new HashMap<Object, Object>();
		Map<Object, Object> map2 = new HashMap<Object, Object>();
		Map<Object, Object> cvmap1 = ((Map<Object, Object>) p.getKey());
		Map<Object, Object> cvmap2 = ((Map<Object, Object>) p.getValue());
		for (Object o : cvmap1.keySet()) {
			map1.put(o, cvmap1.get(o));
		}
		for (Object o : cvmap2.keySet()) {
			map2.put(o, cvmap2.get(o));
		}
		Pair pp = new Pair();
		pp.setKey(map1);
		pp.setValue(map2);
		bounds = pp;
	}
	
	public void addAllConstraints(List<Constraint> co) {
		for (Constraint c : co)
			this.addConstraint(c);
	}
	
	public int addConstraint(Constraint c) {
		int addStatus = 0; 
		/*
		addStatus = cvk.addConst(c);
		if (addStatus != -1)
			this.constraints.add(c);
		*/
		if (!this.constraints.contains(c)) {
			this.constraints.add(c);
		}
		
		return addStatus;
	}
	
	@SuppressWarnings("unchecked")
	public List<Constraint> renameConstraints(Pair dictionary) {
		List<Constraint> lc = new LinkedList<Constraint>();
		List<String> olDs = (List<String>) dictionary.getKey();
		List<String> neWs = (List<String>) dictionary.getValue();
			
		for (Constraint c : this.getConstraints()) {
			Constraint c2 = c.update(olDs, neWs);
			if (c2 != null)
				lc.add(c2);
		}
		
		return lc;
	}
	
	public void updateMapNames(String neW, String olD) {
		mapNames.put(neW, olD);
		mapNames.put(neW+Constraint.LEFT, olD+Constraint.LEFT);
		mapNames.put(neW+Constraint.RIGHT, olD+Constraint.RIGHT);
	}
	
	public void putVal(String key, Integer value) {
		// method used to maintain the map of known range boundaries
		known.put(key, value);
	}
	
	public void checkConsistency(boolean verbose, Binding bd) {
		// this method set the consistent flag to false once
		// we know about ranges that do not match the input string
		for (String s : this.getRanges().keySet()) {
			int sType = this.getRanges().get(s).getType(); // VAR or TERM ?
			
			if (known.containsKey(s+Constraint.LEFT) && known.containsKey(s+Constraint.RIGHT)) {
				int left = known.get(s+Constraint.LEFT);
				int right= known.get(s+Constraint.RIGHT);
				
				if (input != null) { 
					// two options must be considered
					// a. we know a range covering a terminal of the clause 
					//    -> consistency check
					// b. we know a range covering a variable of the clause (maybe it covers several terminals of the string)
					//    -> variable binding
					switch (sType) {
					case ArgContent.EPSILON:
						// this case should never happen for the constraints have been defined
						// forcing left to be different from right for such ranges
						if (left != right) {
							consistent=false;
						}
						break;
					case ArgContent.TERM:
						if (left == right) {
							// terminals cannot cover empty ranges
							consistent=false;
							if (verbose)
								System.err.println("Range mismatch: " + mapNames.get(s) + " cannot cover <" + left + ", " + right + ">");
						} else {
							String inDaClause = mapNames.get(s);
							String inDaInput  = input.get(left).getWord();
							if (!inDaClause.equals(inDaInput)) {
								consistent=false;
								if (verbose)
									System.err.println("Range mismatch: " + inDaClause + " \\= " + inDaInput);
							}
						}
						break;
					case ArgContent.VAR:
						LinkedList<ArgContent> acl = new LinkedList<ArgContent>();
						if (left == right) {
							// NB: epsilon ranges are always consistent
							ArgContent ac1 = new ArgContent(ArgContent.EPSILON, "Eps");
							acl.add(ac1);
						} else {
							for (int i  = left ; i < right ; i++) {
								ArgContent ac = new ArgContent(ArgContent.TERM, input.get(i).getWord());
								acl.add(ac);
							}
						}
						ArgContent range = this.getRanges().get(s);
						try {
							bd.bind(verbose, range, new ArgContent(acl));
						} catch (RCGInstantiationException e) {
							if (verbose)
								System.err.println("Range instantiation exception "+e);
							consistent=false;
						}
						break;
					default: //skip
					}
				}
			}
		}
	}
	
	public List<Constraint> select(List<String> lefts, List<String> rights, Map<String, String> newNames) {
		List<Constraint> lc = new LinkedList<Constraint>();
		for (String left : lefts) {
			for (String right : rights) {
				for (Constraint c : this.constraints) {
					Constraint c2 = c.select(left, right, newNames);
					if (c2 != null) {
						if (!lc.contains(c2))
							lc.add(c2);
					}
				}
			}
		}
		return lc;
	}
	
	public List<String> boundariesConstraints() {
		List<String> ls = new ArrayList<String>();
		for (int i = 0 ; i < this.boundaries.size() ; i++) {
			// we gather the constraints associated to argument's boundary
			String what = this.boundaries.get(i);
			for (Constraint c : this.constraints) {
				// getConst returns null if c does not concern "what"
				// and a copy of the constraint otherwise
				Integer c2 = c.getConst(what);
				if (c2 != null)
					ls.add(String.valueOf(c2));
			}
		}
		Collections.sort(ls);
		return ls;
	}
	
	public int getLastBoundValue() {
		int res = -1;
		String name = this.boundaries.get(this.boundaries.size() - 1);
		int i = 0;
		while (i < this.constraints.size()) {
			Integer j = this.constraints.get(i).getConst(name);
			if (j != null) {
				res = j;
				// to exit the loop
				i = this.constraints.size();
			}
			i++;
		}		
		return res;
	}
	
	public int getFirstBoundValue() {
		int res = -1;
		String name = this.boundaries.get(0);
		int i = 0;
		while (i < this.constraints.size()) {
			Integer j = this.constraints.get(i).getConst(name);
			if (j != null) {
				res = j;
				// to exit the loop
				i = this.constraints.size();
			}
			i++;
		}
		return res;
	}

	public List<String> constraints2strings() {
		// this methods sorts the pretty printed constraints
		// Recall that these are used to detect duplicated sets of constraints
		// and also inclusion between sets of constraints
		List<String> ls = new ArrayList<String>();
		for (Constraint c : this.constraints) {
			ls.add(c.toStringRenamed(this.mapNames));
		}
		Collections.sort(ls);
		return ls;
	}
	
	public List<String> constraints2shortStrings() {
		// this methods sorts the pretty printed constraints
		// Recall that these are used to detect duplicated sets of constraints
		// and also inclusion between sets of constraints
		List<String> ls = new ArrayList<String>();
		for (Constraint c : this.constraints) {
			ls.add(c.toShortString());
		}
		Collections.sort(ls);
		return ls;
	}
	
	public boolean isMoreGeneral(ConstraintVector other) {
		// check whether the current constraint vector is more general than
		// its argument, i.e. the constraint vector other
		// -> this means, all constraints in the current vector are in other
		List<String> l1 =  this.constraints2shortStrings();//this.constraints2strings();
		List<String> l2 = other.constraints2shortStrings();//other.constraints2strings();
		return l2.containsAll(l1);
	}
	
	public int getNbOfCstBounds() {
		int res = 0;
		for (String n : mapNames.keySet()) {
			if (n.endsWith(Constraint.LEFT) || n.endsWith(Constraint.RIGHT))
				res++;
		}
		return res;
	}
	
	public Pair getBounds() {
		// compute the bounding maps _only once_
		Pair res = new Pair();
		if (bounds != null) {
			res = bounds;
		} else {
			Map<String, Integer> map1 = new HashMap<String, Integer>();
			Map<Integer, String> map2 = new HashMap<Integer, String>();
			int i = 0;
			for (String n : mapNames.keySet()) {
				if (n.endsWith(Constraint.LEFT) || n.endsWith(Constraint.RIGHT)) {
					if (!map1.containsKey(n)) {
						map1.put(n, new Integer(i));
						map2.put(new Integer(i), n);
						i++;
					}
				}
			}
			res.setKey(map1);
			res.setValue(map2);
			bounds = res;
		}
		return res;
	}
	
	public Map<String, String> getMapNames() {
		return mapNames;
	}

	public void setMapNames(Map<String, String> mapNames) {
		this.mapNames = mapNames;
	}

	public List<String> getBoundaries() {
		return boundaries;
	}

	public void setBoundaries(List<String> boundaries) {
		this.boundaries = boundaries;
	}

	public Map<String,ArgContent> getRanges() {
		return ranges;
	}

	public void setRanges(Map<String,ArgContent> ranges) {
		this.ranges = ranges;
	}

	public List<Constraint> getConstraints() {
		return constraints;
	}

	public void setConstraints(List<Constraint> constraints) {
		this.constraints = constraints;
	}
	
	public String print() {
		StringBuffer res = new StringBuffer();
		res.append("(");
		for (int i = 0 ; i < this.boundaries.size() ; i++) {
			if (i%2 == 0) {
				res.append("Left: ");
				res.append(this.boundaries.get(i));
			} else {
				res.append("-Right: ");
				res.append(this.boundaries.get(i));
				if (i < this.boundaries.size() -1)
					res.append(" ; ");
			}
		}
		res.append(") ");
		for (Constraint c : this.constraints) {
			res.append(" ; ");
			res.append(c.toString());
		}
		return res.toString();
	}
	
	public String toShortString() {
		StringBuffer res = new StringBuffer();
		res.append("(");
		for (int i = 0 ; i < this.boundaries.size() ; i++) {
			String what = this.boundaries.get(i);

			if (i%2 == 0) {
				res.append("L: ");
				res.append(what);
			} else {
				res.append("-R: ");
				res.append(what);
				if (i < this.boundaries.size() -1)
					res.append(" ");
			}
		}
		res.append(") ");
		// we need to sort the constraints for the String representation
		// of the vector is used for uniqueness in the agenda
		List<String> ls = this.constraints2shortStrings();
		for (String s : ls) {
			res.append("_");
			res.append(s);
		}
		return res.toString();		
	}
	
	public String toStringRenamed() {
		StringBuffer res = new StringBuffer();
		res.append("(");
		for (int i = 0 ; i < this.boundaries.size() ; i++) {
			String what = this.boundaries.get(i);
			if (this.mapNames.containsKey(what))
				what = this.mapNames.get(what);
			if (i%2 == 0) {
				res.append("Left: ");
				res.append(what);
			} else {
				res.append("-Right: ");
				res.append(what);
				if (i < this.boundaries.size() -1)
					res.append(" ; ");
			}
		}
		res.append(") ");
		// we need to sort the constraints for the String representation
		// of the vector is used for uniqueness in the agenda
		List<String> ls = this.constraints2strings();
		for (String s : ls) {
			res.append(" ; ");
			res.append(s);
		}
		return res.toString();
	}

	public String toString() {
		StringBuffer res = new StringBuffer();
		res.append("Correspondances between names: \n");
		for (String n : this.mapNames.keySet()) {
			res.append(n + " := " + this.mapNames.get(n) + "\n");
		}
		res.append("\nPassive ranges: \n");
		for (int i = 0 ; i < (this.boundaries.size() - 1); i=i+2) {
			res.append("left: "  + this.boundaries.get(i) + "\n");
			res.append("right: " + this.boundaries.get(i+1) + "\n -- \n");
		}
		res.append("\nActive ranges: \n");
		for (String ac : this.ranges.keySet()) {
			ArgContent acc = this.ranges.get(ac);
			res.append("** " + acc.getName() + "\n");
		}
		res.append("\nConstraints: \n");
		for (Constraint c : this.constraints) {
			res.append(c.toString() + "\n");
		}
		return res.toString();
	}

	public int getSentence_length() {
		return sentence_length;
	}

	public void setSentence_length(int sentence_length) {
		this.sentence_length = sentence_length;
	}

	public Map<String, Integer> getKnown() {
		return known;
	}

	public void setKnown(Map<String, Integer> known) {
		this.known = known;
	}

	public boolean isConsistent() {
		return consistent;
	}

	public void setConsistent(boolean consistent) {
		this.consistent = consistent;
	}

	public List<Word> getInput() {
		return input;
	}

	public void setInput(List<Word> input) {
		this.input = input;
	}

	public void setBounds(Pair bounds) {
		this.bounds = bounds;
	}

	public int getNbOfRanges() {
		return nbOfRanges;
	}

	public void setNbOfRanges(int nbOfRanges) {
		this.nbOfRanges = nbOfRanges;
	}

	public CVectorKey getCvk() {
		return cvk;
	}

}

