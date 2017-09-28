/*
 *  File Predicate.java
 *
 *  Authors:
 *     Wolfgang Maier  <wo.maier@uni-tuebingen.de>
 *     
 *  Copyright:
 *     Wolfgang Maier, 2007
 *
 *  Last modified:
 *     Di 16. Okt 09:54:40 CEST 2007
 *
 *  This file is part of the TuLiPA system
 *     http://www.sfb441.uni-tuebingen.de/emmy-noether-kallmeyer/tulipa
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
package de.tuebingen.rcg;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import de.tuebingen.parser.Binding;
import de.tuebingen.tag.TagTree;

/**
 * Represents a predicate (for RCGs). 
 * 
 * @author wmaier
 *
 */
public class Predicate {

	private PredLabel label;
	private List<Argument> args;
	private ArgContent first;
	
	public Predicate() {
		this.label = null;
		first = null;
		args = new ArrayList<Argument>();
	}
	
	public Predicate(PredLabel label) {
		this.label = label;
		first = null; 
		args = new ArrayList<Argument>();
	}
	
	public Predicate(PredLabel label, Argument arg){
		this(label);
		addArg(arg);
	}
	
	public Predicate(Predicate p) {
		if (p.label instanceof PredStringLabel) {
			this.label = new PredStringLabel((PredStringLabel)p.label);
		} else if (p.label instanceof PredComplexLabel) {
			this.label = new PredComplexLabel((PredComplexLabel)p.label);
		}
		this.args = new ArrayList<Argument>();
		for (int i = 0 ; i < p.getArgs().size() ; i++) {
			args.add(new Argument(p.getArgs().get(i)));
		}
	}
	
	public int getNbOfRanges () {
		int res = 0;
		for (Argument arg : this.args) {
			for (ArgContent argc : arg) {
				res += argc.getSize();
			}
		}
		return res;
	}

	public ArgContent getFirst() {
		return first;
	}

	public PredLabel getLabel() {
		return label;
	}
	
	public void setLabel(PredLabel label) {
		this.label = label;
	}
	
	public List<Argument> getArgs() {
		return args;
	}
	
	public void setArgs(List<Argument> args) {
		this.args = args;
	}
	
	public void addArg(Argument r) {
		if (getFirst() == null && r != null && r.size() > 0) {
			first = r.get(0);
		}
		args.add(r);
	}
	
	public int getArity() {
		return args.size();
	}

	/**
	 * Instantiate the predicate with a given binding, means replace every 
	 * variable present in the predicate with a suitable value from the binding
	 * @param p - the predicate
	 * @param b - the binding
	 * @return the instantiated predicate
	 * @throws RCGInstantiationException - if the predicate couldn't be instantiated
	 */
	public static List<Argument> instantiate(Predicate p, Binding b) throws RCGInstantiationException {
		List<Argument> ret = new ArrayList<Argument>();
		List<Argument> uninstrhs = p.getArgs();
		for (int i = 0; i < uninstrhs.size(); ++i) {
			Argument currarg = uninstrhs.get(i);
			ArrayList<ArgContent> argl = new ArrayList<ArgContent>();
			for (int j = 0 ; j < currarg.size() ; j++) {
				ArgContent instarg = currarg.get(j);
				if (instarg.getType() == ArgContent.VAR) { // we only dereference variables
					instarg = b.deref(currarg.get(j));
				}
				if (instarg == null) {
					throw new RCGInstantiationException("Could not instantiate predicate " + p.toString() + ".\n Current table status: \n" + b.toString());
				}
				argl.add(instarg);
			}
			ret.add(new Argument(argl));
		}
		return ret;
	}
	
	/**
	 * A String representation
	 */
	public String toString() {
		String ret = label.toString() + "(";
		Iterator<Argument> it = args.iterator();
		while (it.hasNext()) {
			Argument rl = (Argument)it.next();
			// separate rangelists with a comma
			ret += rl.toString() + ",";  
		}
		// last comma is too much
		if (ret.length() > 0 && ret.lastIndexOf(',') == ret.length() - 1) {
			ret = ret.substring(0, ret.length() - 1);
		}
		return ret + ")";
	}

	// pretty printing:
	public String toString(Map<String, TagTree> dict) {
		String ret = "";
		if (label instanceof PredStringLabel)
			ret += label.toString() + "(";
		else if (label instanceof PredComplexLabel)
			ret += ((PredComplexLabel) label).toString(dict) + "(";
		Iterator<Argument> it = args.iterator();
		while (it.hasNext()) {
			Argument rl = (Argument)it.next();
			// separate rangelists with a comma
			ret += rl.toString() + ",";  
		}
		// last comma is too much
		if (ret.length() > 0 && ret.lastIndexOf(',') == ret.length() - 1) {
			ret = ret.substring(0, ret.length() - 1);
		}
		return ret + ")";
	}
	
	public String toStringRenamed(Map<String,String> names) {
		String ret = "";
		ret += label.toString() + "(";
		Iterator<Argument> it = args.iterator();
		while (it.hasNext()) {
			Argument rl = (Argument)it.next();
			// separate rangelists with a comma
			ret += rl.toStringRenamed(names) + ",";  
		}
		// last comma is too much
		if (ret.length() > 0 && ret.lastIndexOf(',') == ret.length() - 1) {
			ret = ret.substring(0, ret.length() - 1);
		}
		return ret + ")";		
	}
	
	public boolean hasEpsArgs() {
		boolean res = true;
		for(Argument arg : args) {
			res &= arg.isEpsilon();
		}
		return res;
	}
}
