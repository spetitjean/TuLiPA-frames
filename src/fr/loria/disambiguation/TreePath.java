/*
 *  File TreePath.java
 *
 *  Authors:
 *     Yannick Parmentier <parmenti@loria.fr>
 *     
 *  Copyright:
 *     Yannick Parmentier, 2008
 *
 *  Last modified:
 *     Fri May 16 16:03:14 CEST 2008
 *
 *  This file is part of the Polarity Filter
 *
 *  The Polarity Filter is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  The Polarity Filter is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package fr.loria.disambiguation;

import java.util.*;

public class TreePath {
	
	private List<List<String>> trees;  // tree collected along the path
	private int state;                 // state reached
	
	public TreePath() {
		trees = new LinkedList<List<String>>();
	}
	
	public TreePath(int i) {
		this();
		state = i;
	}
	
	public void add(List<String> others) {
		trees.add(others);
	}
	
	public void copyTrees(List<List<String>> others) {
		for (List<String> ls : others) {
			this.add(ls);
		}
	}

	public List<List<String>> getTrees() {
		return trees;
	}

	public void setTrees(List<List<String>> trees) {
		this.trees = trees;
	}

	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}
	
}
