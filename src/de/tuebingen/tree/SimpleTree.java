/*
 *  File SimpleTree.java
 *
 *  Authors:
 *     Wolfgang Maier <wo.maier@uni-tuebingen.de>
 *     
 *  Copyright:
 *     Wolfgang Maier, 2007
 *
 *  Last modified:
 *     Di 16. Okt 09:35:57 CEST 2007
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
package de.tuebingen.tree;

import java.util.List;

/**
 * Implements a simple tree with string-labeled nodes
 * @author wmaier
 *
 */
public class SimpleTree implements Tree {

	private String id;
	private Node root;
	private List<Node> terminals;
	
	public SimpleTree() {
		this("", null);
	}
	
	public SimpleTree(Node root) {
		this("", root);
	}
	
	public SimpleTree(String id, Node root) {
		this.id = id;
		this.root = root;
	}
	
	public Node getRoot() {
		return root;
	}

	public void setRoot(Node root) {
		this.root = root;
	}

	public void setTerminals(List<Node> terminals) {
		this.terminals = terminals;		
	}

	public List<Node> getTerminals() {
		return terminals;
	}
	
	public String getId() {
		return id;
	}
	
	public void setId(String id) {
		this.id = id;
	}

	public int numNodes(Node n) {
		int ret = 1;
		if (n.getChildren() != null) {
			List<Node> l = n.getChildren();
			for (int i=0 ; i < l.size() ; i++){
				ret += numNodes(l.get(i));
			}
		}
		return ret;
	}

	public String toString() {
		if (root != null) {
			return root.toString();
		} else {
			return "";
		}
	}

}
