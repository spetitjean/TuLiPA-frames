/*
 *  File TagNode.java
 *
 *  Authors:
 *     Wolfgang Maier  <wo.maier@uni-tuebingen.de>
 *     Yannick Parmentier <parmenti@sfs.uni-tuebingen.de>
 *     
 *  Copyright:
 *     Wolfgang Maier, 2007
 *     Yannick Parmentier, 2007
 *
 *  Last modified:
 *     Di 18. Sep 15:49:03 CEST 2007
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
package de.tuebingen.tag;

import java.util.LinkedList;
import java.util.List;

import de.tuebingen.anchoring.NameFactory;
import de.tuebingen.rcg.ArgContent;
import de.tuebingen.tokenizer.Word;
import de.tuebingen.tree.Node;

/**
 * @author wmaier, parmenti
 *
 */
public class TagNode implements Node {

	/*
	 * type 0: anchor
	 * type 1: foot
	 * type 2: subst
	 * type 3: no adjunction
	 * type 4: lex
	 * type 5: co-anchor
	 * type 6: std (standard)
	 */
	public static final int ANCHOR   = 0;
	public static final int FOOT     = 1;
	public static final int SUBST    = 2;
	public static final int NOADJ    = 3;
	public static final int LEX      = 4;
	public static final int COANCHOR = 5;
	public static final int STD      = 6;
	
	// for feature structures labeling the node
	public static final int TOP      = 0;
	public static final int BOT      = 1;
	
	// for status wrt adjunction
	public static final int NOCST    = ArgContent.STD_RANGE;   // no constraint
	public static final int NADJ     = ArgContent.NADJ_RANGE;  // null adjunction 
	public static final int MADJ     = ArgContent.MADJ_RANGE;  // mandatory adjunction  
	
	private int type;
	private boolean noadj;      // is true when adjunction is forbidden on the node
	private Fs label;
	private String name;
	private String address;	    // Gorn address of the node
	private String category;
	private List<Node> children;
	private ArgContent lRange;  // left range
	private ArgContent rRange;  // right range
	private ArgContent sRange;  // subst range
	private ArgContent cRange;  // const range (for lex nodes)
	private Word         word;  // words (for anchored lex nodes)
	private boolean  isAncLex;  // true for lex nodes containing the main anchor (for RCg conversion using positions of main anchors)
	private int     adjStatus;
	
	
	public TagNode() {
		type     = STD;   // by default a node is a std node
		noadj    = false; // by default the node can receive adjunctions
		category = null;
		name     = null;
		address  = null;
		children = null;
		label    = null;
		lRange   = null;
		rRange   = null;
		sRange   = null;
		cRange   = null;
		word     = null;
		isAncLex = false;
		adjStatus= NOCST;
	}
	
	public TagNode(TagNode n){
		// surface copy constructor (do not consider children nodes)
		type     = n.getType();
		category = n.getCategory();
		name     = n.getName();
		address  = n.getAddress();
		noadj    = n.isNoadj();
		if (n.getLabel() != null) {
			label= new Fs(n.getLabel());
		} else {
			label= null;
		}
		word     = n.getWord();
		isAncLex = n.isAncLex(); 
		adjStatus= n.getAdjStatus();

		//ADDED_BY_TS
		children = null;	
		//END_ADDED_BY_TS
	}
	
	public TagNode(TagNode n, NameFactory nf) {
		type     = n.getType();
		category = n.getCategory();
		name     = n.getName();
		address  = n.getAddress();
		noadj    = n.isNoadj();
		if (n.getLabel() != null) {
			label    = new Fs(n.getLabel(), nf);
		} else {
			label = null;
		}
		if (n.getChildren() != null) {
			children = new LinkedList<Node>();
			for (int i = 0 ; i < n.getChildren().size() ; i++) {
				children.add(new TagNode((TagNode) n.getChildren().get(i), nf));
			}
		} else {
			children = null;
		}
		word     = n.getWord();
		isAncLex = n.isAncLex();
		adjStatus= n.getAdjStatus();
	}
	
	public void add2children(TagNode n) {
		if (children == null)
			children = new LinkedList<Node>();
		children.add(n);
	}
	
	public String typeToString(){
		String res = "";
		switch (type) {
		case ANCHOR:   res = "anchor"; break;
		case FOOT:     res = "foot"; break;
		case SUBST:    res = "subst"; break;
		case NOADJ:    res = "no_adjunction"; break;
		case LEX:      res = "lex"; break;
		case COANCHOR: res = "co-anchor"; break;
		case STD:      res = "std"; break;
		}
		return res;
	}
	
	public List<ArgContent> giveArgs(){
		List<ArgContent> args = new LinkedList<ArgContent>();
		switch (type){
		case ANCHOR: //anchor
			if (!noadj) {
				args.add(this.getLRange());
				args.add(this.getRRange());
			} // else skip no-adj nodes
			break;
		case FOOT: //foot
			args.add(this.getCRange());
			break;
		case SUBST: //subst
			args.add(this.getSRange());
			break;
		case LEX: //lex
			args.add(this.getCRange());
			break;
		case COANCHOR: //co-anchor
			args.add(this.getLRange());
			args.add(this.getRRange());
			break;
		case STD: //std
			args.add(this.getLRange());
			args.add(this.getRRange());
			break;
		default: // skip no-adj nodes	
		}
		return args;
	}
	
	public void updateAdjStatus(){
		adjStatus = label.getAdjStatus();
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
		if (type == NOADJ) {noadj = true;}
		else if (type == ANCHOR) {isAncLex = true;}
	}

	public Fs getLabel() {
		return label;
	}

	public void setLabel(Fs label) {
		this.label = label;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public List<Node> getChildren() {
		return children;
	}

	public void setChildren(List<Node> children) {
		this.children = children;
	}

	public ArgContent getLRange() {
		return lRange;
	}

	public void setLRange(ArgContent range) {
		lRange = range;
	}

	public ArgContent getRRange() {
		return rRange;
	}

	public void setRRange(ArgContent range) {
		rRange = range;
	}

	public ArgContent getSRange() {
		return sRange;
	}

	public void setSRange(ArgContent range) {
		sRange = range;
	}

	public ArgContent getCRange() {
		return cRange;
	}

	public void setCRange(ArgContent range) {
		cRange = range;
	}

	public void findCategory(){
		// update the node state by looking up for the cat feature
		if (label != null) {
			category = label.getCategory();
			label.propagateCategory(category);
		}
	}
	
	public void addTopBot() {
		if (label != null) {
			
		}
	}
	
	public String getFeatVal(String f, int fs){
		return label.getFeatVal(f, fs);		
	}
	
	public String getAdjCategory(int fs){
		return label.getCategory(true, fs);		
	}
	
	public String getSubstCategory(){
		return label.getCategory(false, TOP);
	}
	
	public String toString(){
		return this.toString("  "); //default space value 
	}
	
	public String toString(String space) {
		String child = "";
		String lab = "";
		if (children != null) {
			for (int i=0; i < children.size(); i++){
				child+=((TagNode) children.get(i)).toString(space+"  ");
			}
		}
		if (label != null) {
			lab = label.toString();
		}
		return (space+"node"+" ("+name+" - " + address + ") "+category +" "+typeToString()+" "+" ["+lab+"]\n"+child);
	}

	public boolean isNoadj() {
		return noadj;
	}

	public void setNoadj(boolean noadj) {
		this.noadj = noadj;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public Word getWord() {
		return word;
	}

	public void setWord(Word word) {
		this.word = word;
	}

	public boolean isAncLex() {
		return isAncLex;
	}

	public void setAncLex(boolean isAncLex) {
		this.isAncLex = isAncLex;
	}

	public int getAdjStatus() {
		return adjStatus;
	}

    public void setAdjStatus(int adjStatus) {
	    this.adjStatus = adjStatus;
	}
    
    //ADDED_BY_TS
    public void getAllNodesChildrenFirst(List<TagNode> list) {

	assert(list != null);

	if (children != null) {
	    for (int i=0; i < children.size(); i++){
	    
		assert(children.get(i) != null);
		TagNode child = (TagNode)  children.get(i);
		assert(child != null);
		
		//System.err.println("child: " + child.toString());
		
		child.getAllNodesChildrenFirst(list);
	    }
	}
	    
	list.add(this);
    }


    public void getAllNodesParentFirst(List<TagNode> list) {

	assert(list != null);

	list.add(this);

	if (children != null) {
	    for (int i=0; i < children.size(); i++){
	    
		assert(children.get(i) != null);
		TagNode child = (TagNode)  children.get(i);
		assert(child != null);
		
		//System.err.println("child: " + child.toString());
		
		child.getAllNodesParentFirst(list);
	    }
	}
    }

    public String print() {

	String s = "(";
	if (category != "Eps")
	    s = s + category; 

	//note: we are currently ignoring the features

	if (type == FOOT)
	    s = s + "*";
	else if (type == LEX)
	    s = s + "<>";
	else if (children == null || children.size() == 0)
	    s = s + "!";

	if (children != null) {
	    for (Node n : children) {
		s = s + " " + ((TagNode) n).print();
	    }
	}

	return s + ")";
    }
    //END_ADDED_BY_TS
	
}
