/*
 *  File ConstraintItem.java
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

import java.util.List;

import de.tuebingen.rcg.Clause;
import de.tuebingen.rcg.PredLabel;
import de.tuebingen.util.TextUtilities;

public class ConstraintItem {

	private boolean active;
	private ConstraintVector vect;

	// active
	private Clause cl;
	private int dotpos;

	// passive
	private PredLabel pl;
	private boolean completed;

	public ConstraintItem() {
		active = false;
		completed = false;
		cl = null;
		dotpos = 0;
		pl = null;
		vect = null;
	}
	
	public ConstraintItem(ConstraintItem ci) {
		this();
		active    = ci.isActive();
		completed = ci.isCompleted();
		cl        = ci.getCl();
		dotpos    = ci.getDotpos();
		pl        = ci.getPl();
		// this is necessary for the top-down parser (I think) 
		if (ci.vect != null) 
			vect = new ConstraintVector(ci.getVect());
		else
			vect = null;
	}
	
	public ConstraintItem(ConstraintVector v) {
		this();
		vect = v;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public ConstraintVector getVect() {
		return vect;
	}

	public void setVect(ConstraintVector vect) {
		this.vect = vect;
	}

	public Clause getCl() {
		return cl;
	}

	public void setCl(Clause cl) {
		this.cl = cl;
	}

	public int getDotpos() {
		return dotpos;
	}

	public void setDotpos(int dotpos) {
		this.dotpos = dotpos;
	}

	public PredLabel getPl() {
		return pl;
	}

	public void setPl(PredLabel pl) {
		this.pl = pl;
	}

	public boolean isCompleted() {
		return completed;
	}

	public void setCompleted(boolean completed) {
		this.completed = completed;
	}
	
	public int getHash() {
		// this methods returns a hash value for a given 
		// _completed passive_ item, and -1 otherwise
		// NB: the hash value is computed using the predicate name
		//     and the values of the arguments' boundaries
		int res = 0;
		if (this.pl == null) {
			res = -1;
		} else {
			String pred = this.pl.toString();
			//System.err.println(" ### " + pred.toString());
			List<String> bounds = this.vect.boundariesConstraints();
			//System.err.println(" ### " + bounds.toString());
			String hash = TextUtilities.append(pred, TextUtilities.appendList(bounds));
			res = hash.hashCode();
			//System.err.println(" ### The hash is " + hash + " (" + res + ")");
		}
		return res;
	}
	
	public int getFirstBound() {
		return this.vect.getFirstBoundValue();
	}
	
	public int hashCode() {
		return toStringRenamed().hashCode();
		//return toShortString().hashCode();
	}
	
	public boolean equals(Object o) {
		return (o instanceof ConstraintItem) && this.hashCode() == o.hashCode();
	}
	
	public String toShortString() {
		StringBuffer res = new StringBuffer();
		res.append("[");
		if (active) {
			res.append(cl.toShortString());
			res.append(", ");
			res.append(dotpos);
			res.append(", ");
		}
		else {
			res.append(pl.toString());
			res.append(", ");
			res.append(completed);
			res.append(", ");
		}
		res.append(vect.toShortString());
		res.append("]");
		return res.toString();		
	}
	
	public String toStringRenamed() {
		StringBuffer res = new StringBuffer();
		res.append("[");
		if (active) {
			//res.append(cl.toStringRenamed(vect.getMapNames()));
			res.append(cl.toShortString());
			res.append(", ");
			res.append(dotpos);
			res.append(", ");
		}
		else {
			res.append(pl.toString());
			res.append(", ");
			res.append(completed);
			res.append(", ");
		}
		res.append(vect.toStringRenamed());
		res.append("]");
		return res.toString();
	}
	
	public String toString() {
		StringBuffer res = new StringBuffer();
		res.append("[");
		if (active) {
			res.append(cl.print());
			res.append(", ");
			res.append(dotpos);
			res.append(", ");
		}
		else {
			res.append(pl.toString());
			res.append(", ");
			res.append(completed);
			res.append(", ");
		}
		res.append(vect.print());
		res.append("]");
		return res.toString();
	}
}
