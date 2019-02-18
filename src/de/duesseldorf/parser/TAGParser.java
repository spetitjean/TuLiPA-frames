/*
 *  File TAGParser.java
 *
 *  Authors:
 *     Thomas Schoenemann  <tosch@phil.uni-duesseldorf.de>
 *     
 *  Copyright:
 *     Thomas Schoenemann, 2012
 *
 *  Last modified:
 *     Mar 01 2012
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
package de.duesseldorf.parser;
import java.util.*;
import java.lang.Math;
//import de.tuebingen.tree.Grammar;
import de.tuebingen.tag.*;
import de.tuebingen.tokenizer.*;
import de.duesseldorf.frames.Value;
import de.duesseldorf.frames.ValueTools;
import de.tuebingen.forest.*;
import de.tuebingen.tree.*;
import de.tuebingen.rcg.PredComplexLabel;

/**
 * @author tosch
 *
 */
public class TAGParser {
    
    private Map<String, TagTree> grammarDict;
    private Map<TagTree, String> revGrammarDict;
    
    private Map<TagNode,double[][][]> closed_map;
    private Map<TagNode,int[][][]> closed_state_trace;
    private Map<TagNode,TagTree[][][]> closed_tree_trace;
    
    private Map<TagNode,double[][][][][]> gap_map;
    private Map<TagNode,int[][][][][]> gap_state_trace;
    private Map<TagNode,TagTree[][][][][]> gap_tree_trace;
    
    List<TagNode> all_nodes; //all nodes except lexical nodes and foot nodes,
                             //in an order so that children are listed before their parents

    private Map<String, List<TagTree> > initial_trees;
    private Map<String, List<TagTree> > auxiliary_trees;

    private int nTokens;

    private Map<TagNode, String> adr_map;

    //only used in the agenda-routine
    private Vector<Queue<TAGAgendaItem> > agenda;
    private Map<TagNode,boolean[][][]> closed_in_queue;
    private Map<TagNode,boolean[][][][][]> gap_in_queue;
    List<TagNode> foot_nodes;
    Vector<Vector<List<TagNode> > > foot_nodes_ij;
    //end of only used in the agenda-routine
    
    public TAGParser(Map<String, TagTree> dict) {

	adr_map = new HashMap<TagNode, String>();
	
	//grammarDict = dict;

	//internally, all trees have to be binary
	grammarDict = new HashMap<String, TagTree>();
	revGrammarDict = new HashMap<TagTree, String>();

	Iterator<String> its = dict.keySet().iterator();

	while (its.hasNext()) {

	    String cur_key = its.next();
	    TagTree cur_tree = dict.get(cur_key);

	    List<TagNode> nodes = new LinkedList<TagNode>();

	    //this time we need the anchestors before the children
	    ((TagNode) cur_tree.getRoot()).getAllNodesParentFirst(nodes);

	    boolean is_binary = true;

	    for (TagNode n : nodes) {

		if (n.getChildren() != null && n.getChildren().size() > 2) {
		    is_binary = false;
		    break;
		}
	    }

	    if (is_binary) {
		grammarDict.put(cur_key,cur_tree);

		for (TagNode n : nodes)
		    adr_map.put(n,n.getAddress());

		revGrammarDict.put(cur_tree,cur_key);
	    }
	    else {

		//System.err.println("ERROR: handling of non-binary trees is TODO");
		System.err.println("Internal conversion to binary tree");

		//System.err.println("tree: " + cur_tree.getRoot());


		//NOTE: right now we do not copy all attributes of the tree:
		//  the tree is used only internally, all _relevant_ attributes should be copied.
		TagTree bin_tree = new TagTree(cur_tree.getId());
		bin_tree.setOriginalId(cur_tree.getOriginalId());
		
		
		grammarDict.put(cur_key,bin_tree);
		revGrammarDict.put(bin_tree,cur_key);

		//maps nodes in the old tree (cur_tree) to nodes in the new tree (bin_tree)
		Map<TagNode,TagNode> node_map = new HashMap<TagNode,TagNode>();

		//this won't copy children, as desired
		TagNode root = new TagNode((TagNode) cur_tree.getRoot());
		root.setAddress("0");
		if (root.getChildren() != null) {
		    System.err.println("Error: copying the root node gave a non-null list of children");
		}

		bin_tree.setRoot(root);

		node_map.put((TagNode) cur_tree.getRoot(),root);
		//rev_node_map.put(root,(TagNode) cur_tree.getRoot());
		
		for (TagNode org_node : nodes) {

		    //System.err.println("at node " + org_node);

		    TagNode map_node = node_map.get(org_node);

		    String map_gorn = map_node.getAddress();

		    if (org_node.getChildren() != null) {

			if (org_node.getChildren().size() <= 2) {

			    for (int k=0; k < org_node.getChildren().size(); k++) {

				TagNode cur_child = (TagNode) org_node.getChildren().get(k);
				
				TagNode new_node = new TagNode(cur_child);
				if (new_node.getLabel() == null) {
				    //apparently this happens for lex. nodes obtained by anchoring
				    new_node.setLabel(org_node.getLabel());
				}

				//System.err.println("new node has label: " + new_node.getLabel());

				if (map_gorn == "0") //parent is root node
				    new_node.setAddress(  String.valueOf(k+1)  );
				else
				    new_node.setAddress(map_gorn + "." + String.valueOf(k+1) );

				adr_map.put(new_node, cur_child.getAddress());

				map_node.add2children(new_node);

				node_map.put(cur_child , new_node );
			    }
			}
			else {

			    TagNode inter_parent = map_node;
			    
			    for (int k=0; k < org_node.getChildren().size(); k++) {

				String inter_gorn = inter_parent.getAddress();

				TagNode cur_child = (TagNode) org_node.getChildren().get(k);
				
				TagNode new_node = new TagNode(cur_child);
				if (new_node.getLabel() == null) {
				    //apparently this happens for lex. nodes obtained by anchoring
				    new_node.setLabel(org_node.getLabel());
				}

				//System.err.println("new node has label: " + new_node.getLabel());

				int adr_suffix = 1;
				if (k+1 == org_node.getChildren().size())
				    adr_suffix = 2;

				if (inter_gorn == "0") //parent is root node
				    new_node.setAddress(  String.valueOf(adr_suffix)  );
				else
				    new_node.setAddress(inter_gorn + "." + adr_suffix );

				adr_map.put(new_node, cur_child.getAddress());

				inter_parent.add2children(new_node);

				node_map.put(cur_child , new_node );

				if (k+2 < org_node.getChildren().size()) {
				    TagNode inter_node = new TagNode();
				    inter_node.setAddress(inter_gorn + ".2" );

				    //the label should not matter much, but we have to set one
				    inter_node.setLabel(map_node.getLabel());

				    //to keep equivalence, adjunction cannot be allowed at intermediate nodes.
				    inter_node.setType(TagNode.NOADJ);

				    inter_parent.add2children(inter_node);

				    //NOTE: no entries in the node maps (there is no equivalent node)
				    
				    inter_parent = inter_node;
				}
				
			    }
			}
		    }
		}
		
		List<TagNode> new_nodes = new LinkedList<TagNode>();
		root.getAllNodesParentFirst(new_nodes);

		//this will fill all data structures of concerning the root and its descendants
		bin_tree.findMarks(root,"0");

		if (cur_tree.hasFoot()) {
		    bin_tree.setFoot(node_map.get(cur_tree.getFoot()));
		}
		if (cur_tree.hasAnchor()) {
		    bin_tree.setAnchor(node_map.get(cur_tree.getAnchor()));
		}
		if (cur_tree.getLexAnc() != null) {
		    bin_tree.setLexAnc(node_map.get(cur_tree.getLexAnc()));
		}
	    }
	}

	initial_trees = new HashMap<String,List<TagTree> >();
	auxiliary_trees = new HashMap<String,List<TagTree> >();

	all_nodes = new LinkedList<TagNode>();
	
	its = grammarDict.keySet().iterator();
	while (its.hasNext()) {
	    String cur_key =  its.next();
	    TagTree tag_tree = grammarDict.get(cur_key);

	    //now done above
	    //revGrammarDict.put(tag_tree,cur_key);
	    
	    String category = ((TagNode) tag_tree.getRoot()).getCategory();
	    	    
	    if (tag_tree.hasFoot()) {
		//auxiliary_trees.add(tag_tree);
		if (auxiliary_trees.get(category) == null) 
		    auxiliary_trees.put(category, new LinkedList<TagTree>());
		auxiliary_trees.get(category).add(tag_tree);
	    }
	    else {
		//initial_trees.add(tag_tree);
		if (initial_trees.get(category) == null) 
		    initial_trees.put(category, new LinkedList<TagTree>());
		initial_trees.get(category).add(tag_tree);
	    }

	    ((TagNode) tag_tree.getRoot()).getAllNodesChildrenFirst(all_nodes);	    
	}

	/*** remove all lexical nodes and foot nodes from the list all_nodes ***/
	List<TagNode> to_remove = new LinkedList<TagNode>();

	for (TagNode n : all_nodes) {

	    String category = n.getCategory();
	    
	    if (auxiliary_trees.get(category) == null) 
		auxiliary_trees.put(category, new LinkedList<TagTree>());
	    if (initial_trees.get(category) == null) 
		initial_trees.put(category, new LinkedList<TagTree>());

	    if (n.getType() == TagNode.LEX || n.getType() == TagNode.FOOT) {
		to_remove.add(n);
	    }
	}

	System.err.println("found " + to_remove.size() + " lexical and foot nodes");
	
	for (TagNode n : to_remove) {
	    //System.err.println("size before removal: " + all_nodes.size());
	    all_nodes.remove(n);
	    //System.err.println("size after removal: " + all_nodes.size());
	}
    }
    
    private boolean substitution_possible(TagNode node, TagTree tag_tree) {

	TagNode subst_root = (TagNode) tag_tree.getRoot();
	
	Value top1 = node.getLabel().getFeat("top");
	Value top2 = subst_root.getLabel().getFeat("top");
	
	boolean allowed = true;
	try {
	    
	    Value v = ValueTools.unify(top1,top2,new Environment(0));
	    if (v == null)
		allowed = false;
	    
	} catch (Exception e) {
	    allowed = false;
	}
	
	//TODO: CROSS-CHECK
	//check if the labels match
	return (node.getCategory().equals(subst_root.getCategory())); 
	//return (allowed);
    }

    private boolean adjunction_possible(TagNode node, TagTree tag_tree) {

	TagNode subst_root = (TagNode) tag_tree.getRoot();
	TagNode subst_foot = (TagNode) tag_tree.getFoot();
	
	Value top1 = node.getLabel().getFeat("top");
	Value top2 = subst_root.getLabel().getFeat("top");
	
	Value bottom1 = node.getLabel().getFeat("bot");
	Value bottom2 = subst_foot.getLabel().getFeat("bot");
	
	boolean allowed1 = true;
	try {
	    
	    Value v = ValueTools.unify(top1,top2,new Environment(0));
	    if (v == null)
		allowed1 = false;
	    
	} catch (Exception e) {
	    allowed1 = false;
	}
	
	boolean allowed2 = true;
	try {
	    
	    Value v = ValueTools.unify(bottom1,bottom2,new Environment(0));
	    if (v == null)
		allowed2 = false;
	    
	} catch (Exception e) {
	    allowed2 = false;
	}
	
	//TODO: CROSS-CHECK
	return (node.getCategory().equals(subst_root.getCategory()));
	//return (allowed1 && allowed2) ;
    }
    
    private void trace_all_no_gap(TagNode cur_node, int i, int j, int pos, Tidentifier cur_id,
				  Map<Tidentifier, List<Rule>> rules, Set<Integer> rule_idx_set) {

	//System.err.println("trace_all_no_gap("+i+","+j+","+pos + ") for " +cur_node.toString());
	//System.err.println(rule_idx_set.size() + " rules");

	if (cur_node.getType() == TagNode.LEX) {
	    return;
	}

	String category = cur_node.getCategory();

	List<Node> cur_children = cur_node.getChildren();
	int nChildren = (cur_children == null) ? 0 : cur_children.size();

	if (nChildren > 2) {
	    System.err.println("ERROR: nodes of degree 3 or more can presently not be handled.");
	    return;
	}

	boolean rule_set_used = false;


	List<Rule> saved_rules = new LinkedList<Rule>();

	for (Integer r : rule_idx_set) {
	    
	    Rule toCopy = rules.get(cur_id).get(r.intValue());
	    
	    saved_rules.add(new Rule(toCopy));
	}
	

	if (pos == 1) {
	    //no adjunction included here

	    //moves. Note that moves do not close gaps
	    if (nChildren > 0) {
		
		if (nChildren == 1) {
		    //here we need to be sure that the children have already been processed for this span
		    
		    TagNode child = (TagNode) cur_children.get(0);
		    
		    trace_all_no_gap(child, i,j,0,cur_id,rules,rule_idx_set);
		}
		else {
		    
		    //here we need to be sure that the children have already been processed for this span
		    // (there can be empty sub-spans)
		    
		    assert(nChildren == 2);
		    
		    TagNode child1 = (TagNode) cur_children.get(0);
		    TagNode child2 = (TagNode) cur_children.get(1);
		    
		    for (int split_point=i; split_point <= j; split_point++) {
						
			double hyp = closed_map.get(child1)[i][split_point][0] 
			    + closed_map.get(child2)[split_point][j][0];
			
			if (hyp < 1e300) {

			    Set<Integer> cur_rule_set = rule_idx_set;
			
			    if (rule_set_used) {
				
				Set<Integer> new_rule_set = new HashSet<Integer>();
				
				for (Rule r : saved_rules) {
				    
				    rules.get(cur_id).add(new Rule(r));
				    new_rule_set.add(rules.get(cur_id).size()-1);
				    rule_idx_set.add(rules.get(cur_id).size()-1);
				}
				
				cur_rule_set = new_rule_set;
			    }
			    
			    trace_all_no_gap(child1,i,split_point,0,cur_id,rules,cur_rule_set);
			    trace_all_no_gap(child2,split_point,j,0,cur_id,rules,cur_rule_set);

			    rule_set_used = true;
			}
		    }
		}
	    }
	    else {
		
		//leaf node
		
		//NOTE: we DON'T have to cover substitution here: after subst. adjunction is no longer possible
	    }
	    
	}
	else {
	    //adjunction included here
	    

	    //a) null-adjoin
	    //CROSS-CHECK: is this the correct check for mandatory adjunction?
	    if (cur_node.getAdjStatus() != TagNode.MADJ) {
		//adjunction is not obligatory
		double hyp_na = closed_map.get(cur_node)[i][j][1];
		if (hyp_na < 1e300) {
		    
		    trace_all_no_gap(cur_node,i,j,1,cur_id,rules,rule_idx_set);
		    rule_set_used = true;
		}
	    }
	    
				    
	    if (nChildren == 0) {
		
		//b) substitution
		
		for (TagTree subst_tag_tree : initial_trees.get(category)) {

		    if (!substitution_possible(cur_node, subst_tag_tree))
			continue;

		    TagNode subst_root = (TagNode) subst_tag_tree.getRoot();
		    
		    //NOTE: every time we integrate a tree into another tree, we add the weight
		    // of the tree. This way the system with the passes is less complicated.
		    // In the end we have to add the weight of the initial tree
		    double hyp = closed_map.get(subst_root)[i][j][0]
			+ 1.0; //this is the tree weight
		    
		    if (hyp < 1e300) {
			
			Set<Integer> cur_rule_set = rule_idx_set;
			
			if (rule_set_used) {
			    
			    Set<Integer> new_rule_set = new HashSet<Integer>();
			    
			    for (Rule r : saved_rules) {
				
				rules.get(cur_id).add(new Rule(r));
				new_rule_set.add(rules.get(cur_id).size()-1);
				rule_idx_set.add(rules.get(cur_id).size()-1);
			    }
			    
			    cur_rule_set = new_rule_set;
			}
			
			//now we report the tree, and a node address in a different tree. sounds a bit strange, so CROSS-CHECK!
			Tidentifier tid = new Tidentifier(rules.size(),revGrammarDict.get(subst_tag_tree),
							  subst_tag_tree.getId());
			//tid.setNodeId(cur_node.getAddress());
			tid.setNodeId(adr_map.get(cur_node));
			rules.put(tid,new LinkedList<Rule>());
			rules.get(tid).add(new Rule(tid));
			
			for (Integer r : cur_rule_set) {
			    
			    rules.get(cur_id).get(r.intValue()).getRhs().addOp(new TreeOp(tid,PredComplexLabel.SUB)); 
			}
			
			Set<Integer> sub_rule_set = new HashSet<Integer>();
			sub_rule_set.add(0);

			trace_all_no_gap(subst_root, i, j, 0, tid, rules, sub_rule_set);
			
			rule_set_used = true;
		    }
		}
	    }
	    else if (!cur_node.isNoadj()) {
		//c) adjoining
		
		for (TagTree subst_tag_tree : auxiliary_trees.get(category)) {

		    if (!adjunction_possible(cur_node, subst_tag_tree))
			continue;

		    TagNode subst_root = (TagNode) subst_tag_tree.getRoot();
		    
		    for (int i_inner = i; i_inner <= j; i_inner++) {
			for (int j_inner = i_inner; j_inner <= j; j_inner++) {
			    
			    //System.err.println("substroot: " + subst_root);
			    //System.err.println("substroot lookup: " + gap_map.get(subst_root));
			    
			    //NOTE: every time we integrate a tree into another tree, we add the weight
			    // of the tree. This way the system with the passes is less complicated.
			    // In the end we have to add the weight of the initial tree
			    double hyp = gap_map.get(subst_root)[i][i_inner][j_inner][j][0]
				+ closed_map.get(cur_node)[i_inner][j_inner][1]
				+ 1.0; //this is the tree weight
			    
			    if (hyp < 1e300) {
				
				Set<Integer> cur_rule_set = rule_idx_set;

				if (rule_set_used) {
				    
				    Set<Integer> new_rule_set = new HashSet<Integer>();
				    
				    for (Rule r : saved_rules) {
					
					rules.get(cur_id).add(new Rule(r));
					new_rule_set.add(rules.get(cur_id).size()-1);
					rule_idx_set.add(rules.get(cur_id).size()-1);
				    }
				    
				    cur_rule_set = new_rule_set;
				}
				
				//now we report the tree, and a node address in a different tree. sounds a bit strange, so CROSS-CHECK!
				Tidentifier tid = new Tidentifier(rules.size(),revGrammarDict.get(subst_tag_tree),
								  subst_tag_tree.getId());
				//tid.setNodeId(cur_node.getAddress());
				tid.setNodeId(adr_map.get(cur_node));
				rules.put(tid,new LinkedList<Rule>());
				rules.get(tid).add(new Rule(tid));
				
				for (Integer r : cur_rule_set) {
				    
				    rules.get(cur_id).get(r.intValue()).getRhs().addOp(new TreeOp(tid,PredComplexLabel.ADJ)); 
				}
				
				Set<Integer> sub_rule_set = new HashSet<Integer>();
				sub_rule_set.add(0);
				
				trace_all_with_gap(subst_root, i, i_inner, j_inner, j, 0, tid, rules, sub_rule_set);
				trace_all_no_gap(cur_node,i_inner,j_inner,1, cur_id, rules, cur_rule_set);
				
				rule_set_used = true;
			    }
			}
		    }
		    
		}
	    }
	}
    }

    private void trace_all_with_gap(TagNode cur_node, int i1, int i2, int j1, int j2, int pos, Tidentifier cur_id,
				    Map<Tidentifier, List<Rule>> rules, Set<Integer> rule_idx_set) {

	if (cur_node.getType() == TagNode.LEX) {
	    System.err.println("STRANGE: lexical node above a foot node");
	}
	if (cur_node.getType() == TagNode.FOOT) {
	    return;
	}

	List<Node> cur_children = cur_node.getChildren();
	int nChildren = (cur_children == null) ? 0 : cur_children.size();

	if (nChildren > 2) {
	    System.err.println("ERROR: nodes of degree 3 or more can presently not be handled.");
	    return;
	}

	String category = cur_node.getCategory();

	List<Rule> saved_rules = new LinkedList<Rule>();

	for (Integer r : rule_idx_set) {
	    
	    Rule toCopy = rules.get(cur_id).get(r.intValue());
	    
	    saved_rules.add(new Rule(toCopy));
	}

	boolean rule_set_used = false;

	if (pos == 1) {
	    //no adjunction included here
	    
	    if (nChildren > 0) {
		
		if (nChildren == 1) {
		    
		    TagNode child = (TagNode) cur_children.get(0);
		    
		    assert(gap_map.get(child) != null);

		    trace_all_with_gap(child,i1,i2,j1,j2,0,cur_id,rules, rule_idx_set);
		}
		else {
		    
		    assert(nChildren <= 2);
		    
		    TagNode child1 = (TagNode) cur_children.get(0);
		    TagNode child2 = (TagNode) cur_children.get(1);
		    
		    if (gap_map.get(child1) != null) {
			
			//the foot node is in the subtree of child1
			assert(gap_map.get(child2) == null);
			
			for (int split_point = j1; split_point <= j2; split_point++) {
			    
			    double hyp = gap_map.get(child1)[i1][i2][j1][split_point][0]
				+ closed_map.get(child2)[split_point][j2][0];
			    
			    if (hyp < 1e300) {

				Set<Integer> cur_rule_set = rule_idx_set;
			
				if (rule_set_used) {
				    
				    Set<Integer> new_rule_set = new HashSet<Integer>();
				    
				    for (Rule r : saved_rules) {
					
					rules.get(cur_id).add(new Rule(r));
					new_rule_set.add(rules.get(cur_id).size()-1);
					rule_idx_set.add(rules.get(cur_id).size()-1);
				    }
				    
				    cur_rule_set = new_rule_set;
				}
			    
				trace_all_with_gap(child1,i1,i2,j1,split_point,0,cur_id,rules,cur_rule_set);
				trace_all_no_gap(child2,split_point,j2,0,cur_id,rules,cur_rule_set);
				
				rule_set_used = true;
			    }
			}
		    }
		    else {
			
			//the foot node is in the subtree of child2
			assert(gap_map.get(child2) != null);
			
			for (int split_point = i1; split_point <= i2; split_point++) {
			    
			    double hyp = closed_map.get(child1)[i1][split_point][0]
				+ gap_map.get(child2)[split_point][i2][j1][j2][0];
			    
			    if (hyp < 1e300) {

				Set<Integer> cur_rule_set = rule_idx_set;
			
				if (rule_set_used) {
				    
				    Set<Integer> new_rule_set = new HashSet<Integer>();
				    
				    for (Rule r : saved_rules) {
					
					rules.get(cur_id).add(new Rule(r));
					new_rule_set.add(rules.get(cur_id).size()-1);
					rule_idx_set.add(rules.get(cur_id).size()-1);
				    }
				    
				    cur_rule_set = new_rule_set;
				}
			    
				trace_all_no_gap(child1,i1,split_point,0,cur_id,rules,cur_rule_set);
				trace_all_with_gap(child2,split_point,i2,j1,j2,0,cur_id,rules,cur_rule_set);
				
				rule_set_used = true;
			    }
			}
		    }
		}
	    }
	    else {
		
		//leaf node
		
		//NOTE: we DON'T have to cover substitution here: after subst. adjunction is no longer possible
	    }
	    
	}
	else {
	    //adjunction included here
	    
	    //a) null-adjoin
	    //CROSS-CHECK: is this the correct check for mandatory adjunction?
	    if (cur_node.getAdjStatus() != TagNode.MADJ) {
		//adjunction is not obligatory
		double hyp_na = gap_map.get(cur_node)[i1][i2][j1][j2][1];
		if (hyp_na < 1e300) {
		    
		    trace_all_with_gap(cur_node,i1,i2,j1,j2,1,cur_id,rules,rule_idx_set);

		    rule_set_used = true;
		}
	    }

	    
	    //b) true adjunction
	    //CROSS-CHECK: is this the correct check for forbidden adjunction?
	    if (nChildren >  0 && !cur_node.isNoadj()) {
		
		//NOTE: one could also have a lookup for the respective category here
		for (TagTree subst_tag_tree : auxiliary_trees.get(category)) {
		    
		    if (adjunction_possible(cur_node,subst_tag_tree)) {
			
			TagNode subst_root = (TagNode) subst_tag_tree.getRoot();
			
			for (int i_inter=i1; i_inter <= i2; i_inter++) {
			    for (int j_inter = j1; j_inter <= j2; j_inter++) {
				
				//NOTE: every time we integrate a tree into another tree, we add the weight
				// of the tree. This way the system with the passes is less complicated.
				// In the end we have to add the weight of the initial tree
				
				double hyp = gap_map.get(cur_node)[i_inter][i2][j1][j_inter][1]
				    + gap_map.get(subst_root)[i1][i_inter][j_inter][j2][0]
				    + 1.0; //this is the tree weight
				
				if (hyp < 1e300) {
				
				    Set<Integer> cur_rule_set = rule_idx_set;

				    if (rule_set_used) {
					
					Set<Integer> new_rule_set = new HashSet<Integer>();
				    
					for (Rule r : saved_rules) {
					    
					    rules.get(cur_id).add(new Rule(r));
					    new_rule_set.add(rules.get(cur_id).size()-1);
					    rule_idx_set.add(rules.get(cur_id).size()-1);
					}
					
					cur_rule_set = new_rule_set;
				    }
				    
				    //now we report the tree, and a node address in a different tree. sounds a bit strange, so CROSS-CHECK!
				    Tidentifier tid = new Tidentifier(rules.size(),revGrammarDict.get(subst_tag_tree),
								      subst_tag_tree.getId());
				    //tid.setNodeId(cur_node.getAddress());
				    tid.setNodeId(adr_map.get(cur_node));
				    rules.put(tid,new LinkedList<Rule>());
				    rules.get(tid).add(new Rule(tid));
				    
				    for (Integer r : cur_rule_set) {
				    
					rules.get(cur_id).get(r.intValue()).getRhs().addOp(new TreeOp(tid,PredComplexLabel.ADJ)); 
				    }
				
				    Set<Integer> sub_rule_set = new HashSet<Integer>();
				    sub_rule_set.add(0);
				    
				    trace_all_with_gap(cur_node, i_inter,i2,j1,j_inter, 1, cur_id, rules, cur_rule_set);
				    trace_all_with_gap(subst_root, i1,i_inter,j_inter,j2, 0, tid, rules, sub_rule_set);
				    
				    rule_set_used = true;
				}
			    }
			}
		    }
		}
	    }	    
	}	
    }
    
    private void single_trace_no_gap(TagNode cur_node, int i, int j, int pos, Tidentifier cur_id,
				     Map<Tidentifier, List<Rule>> rules) {

	System.err.println("******* single_trace_no_gap "+i+","+j+","+pos + " for " + cur_node.toString());

	if (cur_node.getType() == TagNode.LEX) {
	    System.err.println("----- leaving single_trace_no_gap");
	    return;
	}

	int state = closed_state_trace.get(cur_node)[i][j][pos];

	List<Node> children = cur_node.getChildren();

	//String category = cur_node.getCategory();

	boolean is_leaf = (children == null || children.size() == 0);

	int op = state % 3;
	state /= 3;

	if (op == 0) {
	    //move
	    assert(pos == 1);

	    if (children.size() == 1) {
		//unary move

 		single_trace_no_gap((TagNode) children.get(0),i,j,0,cur_id,rules);
	    }
	    else {
		assert(children.size() == 2);

		single_trace_no_gap((TagNode) children.get(0),i,state,0,cur_id,rules);
		single_trace_no_gap((TagNode) children.get(1),state,j,0,cur_id,rules);
	    }
	}
	else if (op == 1) {
	    //null-adjoin
	    assert(pos == 0);

	    single_trace_no_gap(cur_node,i,j,1,cur_id,rules);
	}
	else {
	    //substitute (at leafs) or adjoin (at internal nodes)

	    System.err.println("node type: " + cur_node.typeToString());

	    TagTree sub_tree = closed_tree_trace.get(cur_node)[i][j][pos];
	    assert(sub_tree != null);
	    assert(revGrammarDict.get(sub_tree) != null);
	    System.err.println("sub tree: " + sub_tree + ", is leaf: " + is_leaf);

	    //CROSS-CHECK: inquire what the first entry should look like
	    Tidentifier sub_tree_id = new Tidentifier(rules.size(),revGrammarDict.get(sub_tree),sub_tree.getId());
	    sub_tree_id.setNodeId(adr_map.get(cur_node));

	    rules.put(sub_tree_id,new LinkedList<Rule>());
	    rules.get(sub_tree_id).add(new Rule(sub_tree_id));

	    if (is_leaf) {
		//the rules are applied to the current tree, not to the subtree

		//now we report the tree, and a node address in a different tree. sounds a bit strange, so CROSS-CHECK!
		//Tidentifier tid = new Tidentifier(sub_tree_id);
		//tid.setNodeId(cur_node.getAddress());
		Tidentifier tid = sub_tree_id;
		rules.get(cur_id).get(0).getRhs().addOp(new TreeOp(tid,PredComplexLabel.SUB)); 

		single_trace_no_gap((TagNode) sub_tree.getRoot(),i,j,0,sub_tree_id,rules);
	    }
	    else {
		
		//Tidentifier tid = new Tidentifier(sub_tree_id);
		//tid.setNodeId(cur_node.getAddress());
		Tidentifier tid = sub_tree_id;
		rules.get(cur_id).get(0).getRhs().addOp(new TreeOp(tid,PredComplexLabel.ADJ)); 

		
		//trace the branches

		int i_inner = state / (nTokens+1);
		int j_inner = state % (nTokens+1);

		//double hyp = gap_map.get(subst_root)[i_outer][i_inner][j_inner][j_outer][0]
		//  + closed_map.get(cur_node)[i_inner][j_inner][1]
		
		single_trace_with_gap((TagNode) sub_tree.getRoot(),i,i_inner,j_inner,j,0,sub_tree_id,rules);
		single_trace_no_gap(cur_node,i_inner,j_inner,1,cur_id,rules);
	    }
	}

	System.err.println("----- leaving single_trace_no_gap");
    }

    private void single_trace_with_gap(TagNode cur_node, int i1, int i2, int j1, int j2, int pos, Tidentifier cur_id,
				       Map<Tidentifier, List<Rule>> rules) {

	System.err.println("******* single_trace_with_gap "+i1+","+i2+","+j1+","+j2+","+pos + " for " +cur_node.toString());

	if (cur_node.getType() == TagNode.LEX) {
	    System.err.println("STRANGE: lexical node above a foot node");
	}
	if (cur_node.getType() == TagNode.FOOT) {
	    return;
	}

	int state = gap_state_trace.get(cur_node)[i1][i2][j1][j2][pos];

	List<Node> children = cur_node.getChildren();

	boolean is_leaf = (children == null || children.size() == 0);
	assert(!is_leaf);

	System.err.println("state: "+state);

	int op = state % 3;
	state /= 3;

	if (op == 0) {
	    //move

	    assert(pos == 1);
	    
	    if (children.size() == 1) {
		//unary move
		
 		single_trace_with_gap((TagNode) children.get(0),i1,i2,j1,j2,0,cur_id,rules);
	    }
	    else {
		assert(children.size() == 2);

		if (state <= i2) {

		    single_trace_no_gap((TagNode) children.get(0),i1,state,0,cur_id,rules);
		    single_trace_with_gap((TagNode) children.get(1),state,i2,j1,j2,0,cur_id,rules);
		}
		else {

		    single_trace_with_gap((TagNode) children.get(0),i1,i2,j1,state,0,cur_id,rules);
		    single_trace_no_gap((TagNode) children.get(1),state,j2,0,cur_id,rules);
		}
	    }

	}
	else if (op == 1) {
	    //null-adjoin
	    single_trace_with_gap(cur_node,i1,i2,j1,j2,1,cur_id,rules);
	}
	else {
	    //adjoin (for internal nodes) or substitution (for leaf nodes)

	    TagTree sub_tree = gap_tree_trace.get(cur_node)[i1][i2][j1][j2][pos];
	    assert(sub_tree != null);


	    assert(revGrammarDict.get(sub_tree) != null);
	    System.err.println("sub tree: " + sub_tree + ", is leaf: " + is_leaf);

	    //CROSS-CHECK: inquire what the first entry should look like
	    Tidentifier sub_tree_id = new Tidentifier(rules.size(),revGrammarDict.get(sub_tree),sub_tree.getId());
	    sub_tree_id.setNodeId(adr_map.get(cur_node));

	    rules.put(sub_tree_id,new LinkedList<Rule>());
	    rules.get(sub_tree_id).add(new Rule(sub_tree_id));

	    if (is_leaf) {

		System.err.println("STRANGE: leaf with a gap");
	    }
	    else {
		
		//Tidentifier tid = new Tidentifier(sub_tree_id);
		//tid.setNodeId(cur_node.getAddress());
		Tidentifier tid = sub_tree_id;
		rules.get(cur_id).get(0).getRhs().addOp(new TreeOp(tid,PredComplexLabel.ADJ)); 

		
		//trace the branches

		int i_inter = state / (nTokens+1);
		int j_inter = state % (nTokens+1);

		//gap_map.get(cur_node)[i_inter][i_inner][j_inner][j_inter][1]
		//    + gap_map.get(subst_root)[i_outer][i_inter][j_inter][j_outer][0]

		
		single_trace_with_gap((TagNode) sub_tree.getRoot(),i1,i_inter,j_inter,j2,0,sub_tree_id,rules);
		single_trace_with_gap(cur_node,i_inter,i2,j1,j_inter,1,cur_id,rules);
	    }
	}
	
    }


    public List<Tidentifier> parse(List<Word> tokens, Map<Tidentifier, List<Rule>> rules, String axiom) {

	//boolean success = build_chart(tokens);
	boolean success = build_chart_via_agenda(tokens);

	if (!success)
	    return new LinkedList<Tidentifier>();

	//return extract_best(rules, axiom);
	return extract_all(rules, axiom);
    }

    /****************************************************************/
    public boolean build_chart(List<Word> tokens) {

	long chartStartTime = System.nanoTime();

	nTokens = tokens.size();

	//trace code: 0 = move, 1 = null-adjoin, 2 = subst/adjoin

	//each map is indexed as [start of span-1][end of span][adjunction possible]
	//where for the last index a 0 means that adjunction is no longer possible, a 1 that it is still possible
	closed_map = new HashMap<TagNode,double[][][]>();

	closed_state_trace = new HashMap<TagNode,int[][][]>();
	closed_tree_trace = new HashMap<TagNode,TagTree[][][]>();

	//map for nodes where adjunction is still possible
	//this implies that the node must be the ancestor of a foot node
	//each map is indexed as [start of span1-1][end of span1][start of span2-1][end of span2][adjunction possible]
	gap_map = new HashMap<TagNode,double[][][][][]>(); 

	gap_state_trace = new HashMap<TagNode,int[][][][][]>(); 
	gap_tree_trace = new HashMap<TagNode,TagTree[][][][][]>(); 

	Iterator<String> its = grammarDict.keySet().iterator();
	while (its.hasNext()) {
	    String cur_key =  its.next();
	    TagTree tag_tree = grammarDict.get(cur_key);

	    //add entries to the above maps

	    TagNode tree_root = (TagNode) tag_tree.getRoot();

	    List<TagNode> tree_nodes = new LinkedList<TagNode>();
	    tree_root.getAllNodesChildrenFirst(tree_nodes);

	    for (TagNode cur_node : tree_nodes) {
		//we currently only implement binary trees

		List<Node> cur_children = cur_node.getChildren();


		if (cur_children != null && cur_children.size() > 2) {
		    System.err.println("ERROR: elementary trees need to be binary (at most). Please transform your grammar.");
		    return false;
		}

		assert(cur_node.getChildren().size() <= 2);
	    }

	    if (!tag_tree.hasFoot()) {

		for (TagNode cur_node : tree_nodes) {

		    closed_map.put(cur_node,new double[nTokens+1][nTokens+1][2]);
		    closed_state_trace.put(cur_node,new int[nTokens+1][nTokens+1][2]);
		    closed_tree_trace.put(cur_node,new TagTree[nTokens+1][nTokens+1][2]);

		    for (int k=0; k <= nTokens; k++) {
			for (int l=0; l <= nTokens; l++) {
			    for (int p=0; p < 2; p++) {
				closed_map.get(cur_node)[k][l][p] = 1e300; 
				closed_state_trace.get(cur_node)[k][l][p] = -1;
				closed_tree_trace.get(cur_node)[k][l][p] = null;
			    }
			}
		    }
		}
	    }
	    else {

		TagNode tree_foot = (TagNode) tag_tree.getFoot();

		//we will use this to determine whether a node is an anchestor of the foot node
		String foot_gorn = tree_foot.getAddress();

		for (TagNode cur_node : tree_nodes) {


		    //check if the node is above the foot node (or if it is the foot node itself)
		    String cur_gorn = cur_node.getAddress();
		    if (cur_gorn == "0")
			cur_gorn = "";

		    boolean above_foot = false;


		    //WARNING: this only works if all nodes are of degree 9 or less 
		    // (otherwise would have to tokenize the Gorn addresses by splitting at '.')
		    // however, due to the conversion in the constructor we always have binary trees here
		    if (cur_gorn.length() <= foot_gorn.length()) {
			
			above_foot = true;
			for (int k=0; k < cur_gorn.length(); k++) {
			    if (cur_gorn.charAt(k) != foot_gorn.charAt(k)) {
				above_foot = false;
			    }
			}
		    }

		    //System.err.println("checking " + cur_node + ", above foot: "  + above_foot + ", cur_gorn: " + cur_gorn 
		    //		       + ", foot_gorn: " + foot_gorn);


		    if (above_foot) {

			gap_map.put(cur_node,new double[nTokens+1][nTokens+1][nTokens+1][nTokens+1][2]);
			gap_state_trace.put(cur_node,new int[nTokens+1][nTokens+1][nTokens+1][nTokens+1][2]);
			gap_tree_trace.put(cur_node,new TagTree[nTokens+1][nTokens+1][nTokens+1][nTokens+1][2]);

			for (int k1=0; k1 <= nTokens; k1++) {
			    for (int l1=0; l1 <= nTokens; l1++) {
				for (int k2=0; k2 <= nTokens; k2++) {
				    for (int l2=0; l2 <= nTokens; l2++) {
					for (int p=0; p < 2; p++) {
					    gap_map.get(cur_node)[k1][l1][k2][l2][p] = 1e300;
					    gap_state_trace.get(cur_node)[k1][l1][k2][l2][p] = -1;
					    gap_tree_trace.get(cur_node)[k1][l1][k2][l2][p] = null;
					}
				    }
				}
			    }
			}
			
		    }
		    else {

			closed_map.put(cur_node,new double[nTokens+1][nTokens+1][2]);
			closed_state_trace.put(cur_node,new int[nTokens+1][nTokens+1][2]);
			closed_tree_trace.put(cur_node,new TagTree[nTokens+1][nTokens+1][2]);

			for (int k=0; k <= nTokens; k++) {
			    for (int l=0; l <= nTokens; l++) {
				for (int p=0; p < 2; p++) {
				    closed_map.get(cur_node)[k][l][p] = 1e300;
				    closed_state_trace.get(cur_node)[k][l][p] = 0;
				    closed_tree_trace.get(cur_node)[k][l][p] = null;
				}
			    }
			}
		    }
		}
	    }
	}

	/******* Parsing ********/

	//NOTE: the algorithm is efficient if, whenever you assign a leaf a span by substitution,
	//  then when you reach the root node you have covered a _larger_ span.
	//  If this assumption is not fulfilled, there can be more than two passes.
	//  If the grammar has nonsense rules like NP -> NP  it could cycle indefinitely, but
	//    not if all trees have strictly positive cost.
	

	//a) base cases 
	its = grammarDict.keySet().iterator();
	while (its.hasNext()) {

	    String cur_key =  its.next();
	    TagTree tag_tree = grammarDict.get(cur_key);

	    TagNode tree_root = (TagNode) tag_tree.getRoot();

	    List<TagNode> tree_nodes = new LinkedList<TagNode>();
	    tree_root.getAllNodesChildrenFirst(tree_nodes);

	    for (TagNode cur_node : tree_nodes) {
		
		if (cur_node.getType() == TagNode.LEX) {

		    //Lexcical base case

		    //if the lex. node is not an anchor, getWord() will return null. That's not what we want
		    //Word word = cur_node.getWord();
		    String word = cur_node.getCategory();

		    //a lexical node can never be above a foot node
		    assert(closed_map.get(cur_node) != null);

		    //adjunction is never possible in base cases

		    //if (word == null || word.getWord() == null || word.getWord().length() == 0) {
		    if (word == null || word.length() == 0) {
			//epsilon scan

			for (int i=0; i <= nTokens; i++)
			    closed_map.get(cur_node)[i][i][0] = 0.0;
		    }
		    else {
			//true word match

			for (int i=0; i < nTokens; i++) {
			    if (tokens.get(i).getWord() == word) {
				closed_map.get(cur_node)[i][i+1][0] = 0.0;
				//System.err.println("word match for pos" + i);
			    }
			}
		    }
		}
		else if (cur_node == tag_tree.getFoot()) {

		    assert(gap_map.get(cur_node) != null);

		    //Foot Predict
		    for (int i=0; i <= nTokens; i++) {
			for (int j=i; j <= nTokens; j++) {
			    gap_map.get(cur_node)[i][i][j][j][0] = 0.0;
			}
		    }
		}
	    }
	}

	long chartInitTime = System.nanoTime() - chartStartTime;
	System.err.println("Time for chart initialization: " + (chartInitTime)/(Math.pow(10, 9))+" sec.");

	//b) proceed with the chart (derived cases)
	//NOTE: we actually must start at 0, for in case of unary moves we can have non-basic cases with span-size 0
	for (int span = 0; span <= nTokens; span++) {

	    for (int i_outer = 0; i_outer <= nTokens-span; i_outer++) {

		boolean next_pass_needed = true;

		for (int pass = 0; next_pass_needed; pass++) {
		    
		    //NOTE: in pass 0 we need to execute everything
		    //   starting from pass one we would only need to cover cases where one of the antecedents covers the entire span
		    //NOTE2: the loop over passes could be moved into the loop over i_outer
		    
		    if (pass >= 3) {
			//TODO: add information here
			System.err.println("WARNING: more than three passes needed. This should usually not happen.");
		    }

		    next_pass_needed = false;

		    int j_outer = i_outer+span;

		    for (TagNode cur_node : all_nodes) {

			// if (cur_node.getType() == TagNode.LEX) 
			//     continue;
			// if (cur_node.getType()  == TagNode.FOOT)
			//     continue;

			String category = cur_node.getCategory();


			List<Node> cur_children = cur_node.getChildren();
			int nChildren = (cur_children == null) ? 0 : cur_children.size();
			
			assert(nChildren <= 2);
			
			if (gap_map.get(cur_node) != null) {
			    
			    //the node is above a foot node => handle gaps
			    
			    for (int i_inner = i_outer; i_inner <= j_outer; i_inner++) {

				for (int j_inner = i_inner; j_inner <= j_outer; j_inner++) {
				    
				    
				    //1.) score when adjunction is still possible
				    // for simplicity of the weighted deduction rules, we compute this score
				    // even if the node does not allow adjunction
				    
				    {					
					double initial_score = gap_map.get(cur_node)[i_outer][i_inner][j_inner][j_outer][1];
					
					double best_adjpos_score = initial_score;
					int best_split_point = -1;					
					
					//moves. Note that moves do not close gaps
					if (nChildren > 0) {
					    
					    if (nChildren == 1) {
						
						TagNode child = (TagNode) cur_children.get(0);
						
						assert(gap_map.get(child) != null);
						
						double hyp = gap_map.get(child)[i_outer][i_inner][j_inner][j_outer][0];
						
						if (hyp < best_adjpos_score) {
						    
						    best_adjpos_score = hyp;
						}
						
					    }
					    else {
						
						assert(nChildren <= 2);
						
						TagNode child1 = (TagNode) cur_children.get(0);
						TagNode child2 = (TagNode) cur_children.get(1);
						
						if (gap_map.get(child1) != null) {
						    
						    //the foot node is in the subtree of child1
						    assert(gap_map.get(child2) == null);

						    double[][][][] gap_map1_i_outer = gap_map.get(child1)[i_outer];
						    double[][][] closed_map2 = closed_map.get(child2);
						    
						    for (int split_point = j_inner; split_point <= j_outer; split_point++) {

							if (pass > 0 && (split_point != j_inner && split_point != j_outer))
							    continue;
							
							// double hyp = gap_map.get(child1)[i_outer][i_inner][j_inner][split_point][0]
							// 	+ closed_map.get(child2)[split_point][j_outer][0];
							double hyp = gap_map1_i_outer[i_inner][j_inner][split_point][0]
							    + closed_map2[split_point][j_outer][0];
							
							if (hyp < best_adjpos_score) {
							    
							    best_adjpos_score = hyp;
							    best_split_point = split_point;
							}
						    }
						}
						else {
						    
						    //the foot node is in the subtree of child2
						    assert(gap_map.get(child2) != null);							
						    
						    double[][] closed_map1_i_outer = closed_map.get(child1)[i_outer];
						    double[][][][][] gap_map2 = gap_map.get(child2);
						    
						    for (int split_point = i_outer; split_point <= i_inner; split_point++) {
							
							if (pass > 0 && (split_point != i_outer && split_point != i_inner))
							    continue;

							// double hyp = closed_map.get(child1)[i_outer][split_point][0]
							// 	+ gap_map.get(child2)[split_point][i_inner][j_inner][j_outer][0];
							double hyp = closed_map1_i_outer[split_point][0]
							    + gap_map2[split_point][i_inner][j_inner][j_outer][0];
							
							if (hyp < best_adjpos_score) {
							    
							    best_adjpos_score = hyp;
							    best_split_point = split_point;
							}
						    }
						}
						
					    }
					}
					else {

					    //leaf node
					    
					    //NOTE: we DON'T have to cover substitution here: after subst. adjunction is no longer possible
					}
					
					if (best_adjpos_score < initial_score) {
					    gap_map.get(cur_node)[i_outer][i_inner][j_inner][j_outer][1] = best_adjpos_score;
					    
					    next_pass_needed = true;
					    
					    
					    //store traceback information
					    int trace = 0;
					    if (best_split_point >= 0)
						trace += 3*best_split_point;
					    
					    gap_state_trace.get(cur_node)[i_outer][i_inner][j_inner][j_outer][1] = trace;
					}
				    }


				    //2.) score when adjunction is no longer possible
				    {

					double[][][][][] cur_gap_map = gap_map.get(cur_node);
					//double initial_score = gap_map.get(cur_node)[i_outer][i_inner][j_inner][j_outer][0];
					double initial_score = cur_gap_map[i_outer][i_inner][j_inner][j_outer][0];
					
					double best_adjdone_score = initial_score;
					
					boolean best_is_null_adjoin = false;
					boolean best_is_substitution = false;
					boolean best_is_adjoining = false;
					
					int best_i_inter = -1;
					int best_j_inter = -1;
					
					TagTree best_tree = null; //for either adjoining or substitution
					
					//a) null-adjoin
					//CROSS-CHECK: is this the correct check for mandatory adjunction?
					if (cur_node.getAdjStatus() != TagNode.MADJ) {
					    //adjunction is not obligatory
					    double hyp_na = gap_map.get(cur_node)[i_outer][i_inner][j_inner][j_outer][1];
					    if (hyp_na < best_adjdone_score) {
						
						best_adjdone_score = hyp_na;
						best_is_null_adjoin = true;
					    }
					}
					
					//NOTE: substitution is not possible, we are above or at a foot node

					//b) true adjoining
					//CROSS-CHECK: is this the correct check for forbidden adjunction?
					if (nChildren >  0 && !cur_node.isNoadj()) {
					    
					    //NOTE: one could also have a lookup for the respective category here
					    for (TagTree subst_tag_tree : auxiliary_trees.get(category)) {
						
						if (adjunction_possible(cur_node,subst_tag_tree)) {
						    
						    TagNode subst_root = (TagNode) subst_tag_tree.getRoot();
						    
						    double [][][][] subst_gap_map_i_outer = gap_map.get(subst_root)[i_outer];
						    
						    for (int i_inter=i_outer; i_inter <= i_inner; i_inter++) {

							for (int j_inter = j_inner; j_inter <= j_outer; j_inter++) {
							    
							    //NOTE: every time we integrate a tree into another tree, we add the weight
							    // of the tree. This way the system with the passes is less complicated.
							    // In the end we have to add the weight of the initial tree
								
							    // double hyp = gap_map.get(cur_node)[i_inter][i_inner][j_inner][j_inter][1]
							    //     + gap_map.get(subst_root)[i_outer][i_inter][j_inter][j_outer][0]
							    //     + 1.0; //this is the tree weight
							    double hyp = cur_gap_map[i_inter][i_inner][j_inner][j_inter][1]
								+ subst_gap_map_i_outer[i_inter][j_inter][j_outer][0]
								+ 1.0; //this is the tree weight
							    
							    if (hyp < best_adjdone_score) {
								
								best_adjdone_score = hyp;
								best_i_inter = i_inter;
								best_j_inter = j_inter;
								
								best_is_adjoining = true;
								best_tree = subst_tag_tree;
							    }
							}
						    }
						}
					    }
					}
						
					    
					if (best_adjdone_score < initial_score) {
					    gap_map.get(cur_node)[i_outer][i_inner][j_inner][j_outer][0] = best_adjdone_score;
						    
					    next_pass_needed = true;
						
					    
					    //store traceback information
					    int trace = 0;
					    if (best_is_null_adjoin) {
						trace = 1;
						gap_state_trace.get(cur_node)[i_outer][i_inner][j_inner][j_outer][0] = trace;
					    }
					    else {
						trace = 2;
						
						if (best_is_adjoining)
						    trace += 3*(best_i_inter * (nTokens+1) + best_j_inter);
						
						gap_state_trace.get(cur_node)[i_outer][i_inner][j_inner][j_outer][0] = trace;
						gap_tree_trace.get(cur_node)[i_outer][i_inner][j_inner][j_outer][0] = best_tree;
					    }
					}					    
				    }	    
				}
			    }    
			}
			else {
				
			    //the node is not above a foot node => no gaps

			    
			    //1.) score when adjunction is still possible
			    // for simplicity of the weighted deduction rules, we compute this score
			    // even if the node does not allow adjunction
			    {

				double initial_score = closed_map.get(cur_node)[i_outer][j_outer][1];
				    
				double best_adjpos_score = initial_score;
				    
				int best_split_point = -1;
				
				//a) moves
				if (nChildren > 0) {
				    
				    if (nChildren == 1) {
					    
					//here we need to be sure that the children have already been processed for this span
					
					TagNode child = (TagNode) cur_children.get(0);
					
					double hyp = closed_map.get(child)[i_outer][j_outer][0];

					if (hyp < best_adjpos_score) {
					    best_adjpos_score = hyp;
					}
				    }
				    else {
					
					//here we need to be sure that the children have already been processed for this span
					// (there can be empty sub-spans)
					    
					assert(nChildren == 2);

					TagNode child1 = (TagNode) cur_children.get(0);
					TagNode child2 = (TagNode) cur_children.get(1);
					
					double[][] closed_map1_i_outer = closed_map.get(child1)[i_outer];
					double[][][] closed_map2 = closed_map.get(child2);
					    
					for (int split_point=i_outer; split_point <= j_outer; split_point++) {
						
					    if (pass > 0 && (split_point != i_outer && split_point != j_outer))
						continue;

					    // double hyp = closed_map.get(child1)[i_outer][split_point][0] 
					    //     + closed_map.get(child2)[split_point][j_outer][0];
					    double hyp = closed_map1_i_outer[split_point][0] 
						+ closed_map2[split_point][j_outer][0];
						
					    if (hyp < best_adjpos_score) {
						
						best_adjpos_score = hyp;
						best_split_point = split_point;
					    }
					}
				    }
				}
				else {
				    //leaf node
				    
				    //NOTE: we DON'T have to cover substitution here: after subst. adjunction is no longer possible
				}

				//NOTE:  adjoining is not possible here, since we process the score were adjoining is not included
			    
				if (best_adjpos_score < initial_score) {
				    closed_map.get(cur_node)[i_outer][j_outer][1] = best_adjpos_score;
				    
				    next_pass_needed = true;
				    
				    //store traceback information
				    int trace = 0;
				    if (best_split_point >= 0)
					trace += 3*best_split_point;
				    
				    closed_state_trace.get(cur_node)[i_outer][j_outer][1] = trace;
				}
			    }
			    
				
			    //2.) score when adjunction is no longer possible
			    {
				    
				double[][][] cur_closed_map = closed_map.get(cur_node);
				//double initial_score = closed_map.get(cur_node)[i_outer][j_outer][0];
				double initial_score = cur_closed_map[i_outer][j_outer][0];
				
				double best_adjdone_score = initial_score;
				
				boolean best_is_null_adjoin = false;
				boolean best_is_substitution = false;
				boolean best_is_adjoining = false;
				
				int best_i_inner = -1;
				int best_j_inner = -1;
				
				TagTree best_tree = null; //for either adjoining or substitution
				
				//a) null-adjoin
				//CROSS-CHECK: is this the correct check for mandatory adjunction?
				if (cur_node.getAdjStatus() != TagNode.MADJ) {
				    //adjunction is not obligatory
				    double hyp_na = closed_map.get(cur_node)[i_outer][j_outer][1];
				    if (hyp_na < best_adjdone_score) {
					
					best_adjdone_score = hyp_na;
					best_is_null_adjoin = true;
				    }
				}
				    
				if (nChildren == 0) {
					
				    //b) substitution

				    for (TagTree subst_tag_tree : initial_trees.get(category)) {

					if (substitution_possible(cur_node,subst_tag_tree)) {

					    TagNode subst_root = (TagNode) subst_tag_tree.getRoot();

					    //NOTE: every time we integrate a tree into another tree, we add the weight
					    // of the tree. This way the system with the passes is less complicated.
					    // In the end we have to add the weight of the initial tree
					    double hyp = closed_map.get(subst_root)[i_outer][j_outer][0]
						+ 1.0; //this is the tree weight
					    
					    if (hyp < best_adjdone_score) {
						    
						//System.err.println("substitution score: "+hyp);

						best_adjdone_score = hyp;
						best_is_substitution = true;
						best_tree = subst_tag_tree;
					    }
					}
				    }
				}
				else if (!cur_node.isNoadj()) {
				    //c) adjoining

				    //NOTE: it would be sensible to have a map of the categories to the corresponding auxiliary trees
				    
				    for (TagTree subst_tag_tree : auxiliary_trees.get(category)) {
					
					if (adjunction_possible(cur_node,subst_tag_tree)) {
					    
					    TagNode subst_root = (TagNode) subst_tag_tree.getRoot();
					    
					    double [][][][] cur_gap_map_i_outer =  gap_map.get(subst_root)[i_outer];

					    for (int i_inner = i_outer; i_inner <= j_outer; i_inner++) {

						//NOTE: cannot exclude anything in the second or higher pass:
						//  we are accessing gap_map.get(subst_root)[i_outer][..][..][j_outer], which can have changed

						for (int j_inner = i_inner; j_inner <= j_outer; j_inner++) {
						    
						    //System.err.println("substroot: " + subst_root);
						    //System.err.println("substroot lookup: " + gap_map.get(subst_root));

						    //NOTE: every time we integrate a tree into another tree, we add the weight
						    // of the tree. This way the system with the passes is less complicated.
						    // In the end we have to add the weight of the initial tree
						    // double hyp = gap_map.get(subst_root)[i_outer][i_inner][j_inner][j_outer][0]
						    //     + closed_map.get(cur_node)[i_inner][j_inner][1]
						    //     + 1.0; //this is the tree weight
						    double hyp = cur_gap_map_i_outer[i_inner][j_inner][j_outer][0]
							+ cur_closed_map[i_inner][j_inner][1]
							+ 1.0; //this is the tree weight
							
						    if (hyp < best_adjdone_score) {
							    
							best_adjdone_score = hyp;
							best_is_adjoining = true;
							best_tree = subst_tag_tree;							
							best_i_inner = i_inner;
							best_j_inner = j_inner;
						    }
						}
					    }
					}
					
				    }
				}


				if (best_adjdone_score < initial_score) {

				    closed_map.get(cur_node)[i_outer][j_outer][0] = best_adjdone_score;
					
				    next_pass_needed = true;
					
				    //store traceback information
				    int trace = 0;
				    if (best_is_null_adjoin) {
					trace = 1;
					closed_state_trace.get(cur_node)[i_outer][j_outer][0] = trace;
				    }
				    else {
					trace = 2;
					
					if (best_is_adjoining)
					    trace += 3*(best_i_inner * (nTokens+1) + best_j_inner);
					
					closed_state_trace.get(cur_node)[i_outer][j_outer][0] = trace;
					closed_tree_trace.get(cur_node)[i_outer][j_outer][0] = best_tree;
				    }
				    
				}
			    }
			}
			    
		    } //end of loop over nodes (in all trees)
		} //end of loop over passes
	    } //end of loop over i_outer 
	} //end of loop over spans

	long chartTime = System.nanoTime() - chartStartTime;
	System.err.println("Total time for init & chart construction: " + (chartTime)/(Math.pow(10, 9))+" sec.");


	return true;
    } 

    private void add_if_not_in_queue(TagNode node, int i, int j, int pos) {

	if (! closed_in_queue.get(node)[i][j][pos]) {

	    agenda.get(j-i).offer(new TAGAgendaItem(node,i,-1,-1,j,pos));
	    
	    closed_in_queue.get(node)[i][j][pos] = true;

	    //add foot items
	    for (TagNode foot_node : foot_nodes_ij.get(i).get(j)) {

		double[][][][][] cur_map = gap_map.get(foot_node);
		if (cur_map[i][i][j][j][0] != 0.0) {
		    cur_map[i][i][j][j][0] = 0.0;
		    add_if_not_in_queue(foot_node,i,i,j,j,0);
		}
	    }
	}
    }

    private void add_if_not_in_queue(TagNode node, int i1, int i2, int j1, int j2, int pos) {

	if (! gap_in_queue.get(node)[i1][i2][j1][j2][pos]) {

	    agenda.get(j2-i1).offer(new TAGAgendaItem(node,i1,i2,j1,j2,pos));
	    
	    gap_in_queue.get(node)[i1][i2][j1][j2][pos] = true;


	    //add foot items
	    for (TagNode foot_node : foot_nodes_ij.get(i1).get(j2)) {

		double[][][][][] cur_map = gap_map.get(foot_node);
		if (cur_map[i1][i1][j2][j2][0] != 0.0) {
		    cur_map[i1][i1][j2][j2][0] = 0.0;
		    add_if_not_in_queue(foot_node,i1,i1,j2,j2,0);
		}
	    }
	}
    }
    

    /****************************************************************/
    public boolean build_chart_via_agenda(List<Word> tokens) {


	long chartStartTime = System.nanoTime();

	nTokens = tokens.size();

	//trace code: 0 = move, 1 = null-adjoin, 2 = subst/adjoin

	//each map is indexed as [start of span-1][end of span][adjunction possible]
	//where for the last index a 0 means that adjunction is no longer possible, a 1 that it is still possible
	closed_map = new HashMap<TagNode,double[][][]>();

	closed_in_queue = new HashMap<TagNode,boolean[][][]>();

	closed_state_trace = new HashMap<TagNode,int[][][]>();
	closed_tree_trace = new HashMap<TagNode,TagTree[][][]>();

	//foot_nodes = new LinkedList<TagNode>();

	//map for nodes where adjunction is still possible
	//this implies that the node must be the ancestor of a foot node
	//each map is indexed as [start of span1-1][end of span1][start of span2-1][end of span2][adjunction possible]
	gap_map = new HashMap<TagNode,double[][][][][]>(); 

	gap_in_queue = new HashMap<TagNode,boolean[][][][][]>();

	gap_state_trace = new HashMap<TagNode,int[][][][][]>(); 
	gap_tree_trace = new HashMap<TagNode,TagTree[][][][][]>(); 

	//List<TagNode> substitution_nodes = new LinkedList<TagNode>();
	List<TagNode> adjunction_nodes = new LinkedList<TagNode>();

	Map<TagNode,TagNode> parent_map = new HashMap<TagNode,TagNode>();

	Map<TagNode,TagTree> tree_map = new HashMap<TagNode,TagTree>();

	Map<String,List<Integer> > word_positions = new HashMap<String,List<Integer> >();
	for (int k=0; k < nTokens; k++) {

	    Word cur_token = tokens.get(k);

	    if (word_positions.get(cur_token.getWord()) == null)
		word_positions.put(cur_token.getWord(), new LinkedList<Integer>());

	    word_positions.get(cur_token.getWord()).add(k+1);
	}


	Iterator<String> its = grammarDict.keySet().iterator();
	while (its.hasNext()) {
	    String cur_key =  its.next();
	    TagTree tag_tree = grammarDict.get(cur_key);

	    //add entries to the above maps

	    TagNode tree_root = (TagNode) tag_tree.getRoot();

	    tree_map.put(tree_root,tag_tree);
	    
	    List<TagNode> tree_nodes = new LinkedList<TagNode>();
	    tree_root.getAllNodesChildrenFirst(tree_nodes);


	    for (TagNode cur_node : tree_nodes) {
		//we currently only implement binary trees

		List<Node> cur_children = cur_node.getChildren();

		if (cur_children != null) {

		    for (Node c : cur_children) {

			parent_map.put((TagNode) c, cur_node);
		    }
		    
		    if (cur_children.size() > 2) {
			System.err.println("ERROR: elementary trees need to be binary (at most). Please transform your grammar.");
			return false;
		    }
		    
		    if (cur_children.size() == 0) {
		    	// if (cur_node.getType() != TagNode.LEX && cur_node.getType() != TagNode.FOOT)
		    	//     substitution_nodes.add(cur_node);
		    }
		    else if (!cur_node.isNoadj()) {
		    	adjunction_nodes.add(cur_node);
		    }

		    assert(cur_node.getChildren().size() <= 2);
		}
		else {
		    // if (cur_node.getType() != TagNode.LEX && cur_node.getType() != TagNode.FOOT)
		    // 	substitution_nodes.add(cur_node);
		}
	    }

	    if (!tag_tree.hasFoot()) {

		for (TagNode cur_node : tree_nodes) {

		    closed_map.put(cur_node,new double[nTokens+1][nTokens+1][2]);
		    closed_in_queue.put(cur_node,new boolean[nTokens+1][nTokens+1][2]);

		    closed_state_trace.put(cur_node,new int[nTokens+1][nTokens+1][2]);
		    closed_tree_trace.put(cur_node,new TagTree[nTokens+1][nTokens+1][2]);

		    for (int k=0; k <= nTokens; k++) {
			for (int l=0; l <= nTokens; l++) {
			    for (int p=0; p < 2; p++) {
				closed_map.get(cur_node)[k][l][p] = 1e300; 
				closed_in_queue.get(cur_node)[k][l][p] = false;

				closed_state_trace.get(cur_node)[k][l][p] = -1;
				closed_tree_trace.get(cur_node)[k][l][p] = null;
			    }
			}
		    }
		}
	    }
	    else {

		TagNode tree_foot = (TagNode) tag_tree.getFoot();

		//foot_nodes.add(tree_foot);

		//we will use this to determine whether a node is an anchestor of the foot node
		String foot_gorn = tree_foot.getAddress();

		for (TagNode cur_node : tree_nodes) {

		    //check if the node is above the foot node (or if it is the foot node itself)
		    String cur_gorn = cur_node.getAddress();
		    if (cur_gorn == "0")
			cur_gorn = "";

		    boolean above_foot = false;


		    //WARNING: this only works if all nodes are of degree 9 or less 
		    // (otherwise would have to tokenize the Gorn addresses by splitting at '.')
		    // however, due to the conversion in the constructor we always have binary trees here
		    if (cur_gorn.length() <= foot_gorn.length()) {
			
			above_foot = true;
			for (int k=0; k < cur_gorn.length(); k++) {
			    if (cur_gorn.charAt(k) != foot_gorn.charAt(k)) {
				above_foot = false;
			    }
			}
		    }

		    //System.err.println("checking " + cur_node + ", above foot: "  + above_foot + ", cur_gorn: " + cur_gorn 
		    //		       + ", foot_gorn: " + foot_gorn);


		    if (above_foot) {

			gap_map.put(cur_node,new double[nTokens+1][nTokens+1][nTokens+1][nTokens+1][2]);
			gap_in_queue.put(cur_node,new boolean[nTokens+1][nTokens+1][nTokens+1][nTokens+1][2]);

			gap_state_trace.put(cur_node,new int[nTokens+1][nTokens+1][nTokens+1][nTokens+1][2]);
			gap_tree_trace.put(cur_node,new TagTree[nTokens+1][nTokens+1][nTokens+1][nTokens+1][2]);

			for (int k1=0; k1 <= nTokens; k1++) {
			    for (int l1=0; l1 <= nTokens; l1++) {
				for (int k2=0; k2 <= nTokens; k2++) {
				    for (int l2=0; l2 <= nTokens; l2++) {
					for (int p=0; p < 2; p++) {
					    gap_map.get(cur_node)[k1][l1][k2][l2][p] = 1e300;
					    gap_in_queue.get(cur_node)[k1][l1][k2][l2][p] = false;

					    gap_state_trace.get(cur_node)[k1][l1][k2][l2][p] = -1;
					    gap_tree_trace.get(cur_node)[k1][l1][k2][l2][p] = null;					    
					}
				    }
				}
			    }
			}
			
		    }
		    else {

			closed_map.put(cur_node,new double[nTokens+1][nTokens+1][2]);
			closed_in_queue.put(cur_node,new boolean[nTokens+1][nTokens+1][2]);

			closed_state_trace.put(cur_node,new int[nTokens+1][nTokens+1][2]);
			closed_tree_trace.put(cur_node,new TagTree[nTokens+1][nTokens+1][2]);

			for (int k=0; k <= nTokens; k++) {
			    for (int l=0; l <= nTokens; l++) {
				for (int p=0; p < 2; p++) {
				    closed_map.get(cur_node)[k][l][p] = 1e300;
				    closed_in_queue.get(cur_node)[k][l][p] = false;

				    closed_state_trace.get(cur_node)[k][l][p] = 0;
				    closed_tree_trace.get(cur_node)[k][l][p] = null;
				}
			    }
			}
		    }
		}
	    }
	}

	/******* Parsing ********/

	//NOTE: the algorithm is efficient if, whenever you assign a leaf a span by substitution,
	//  then when you reach the root node you have covered a _larger_ span.
	//  If this assumption is not fulfilled, there can be more than two passes.
	//  If the grammar has nonsense rules like NP -> NP  it could cycle indefinitely, but
	//    not if all trees have strictly positive cost.
	

	Vector<Vector<List<TagNode> > > subst_nodes_ij = new Vector<Vector<List<TagNode> > >();
	foot_nodes_ij = new Vector<Vector<List<TagNode> > >();

	agenda = new Vector<Queue<TAGAgendaItem> >();

	for (int k=0; k <= nTokens; k++) {
	    agenda.add(new LinkedList<TAGAgendaItem>());
	    subst_nodes_ij.add(new Vector<List<TagNode> >() );
	    foot_nodes_ij.add(new Vector<List<TagNode> >() );
	    for (int k2 = 0; k2 <= nTokens; k2++) {
		if (k2 < k) {
		    subst_nodes_ij.get(k).add(null);
		    foot_nodes_ij.get(k).add(null);
		}
		else {
		    subst_nodes_ij.get(k).add(new LinkedList<TagNode>() );
		    foot_nodes_ij.get(k).add(new LinkedList<TagNode>() );
		}
	    }
	}

	//a) base cases 
	its = grammarDict.keySet().iterator();
	while (its.hasNext()) {

	    String cur_key =  its.next();
	    TagTree tag_tree = grammarDict.get(cur_key);

	    TagNode tree_root = (TagNode) tag_tree.getRoot();

	    List<TagNode> tree_nodes = new LinkedList<TagNode>();
	    tree_root.getAllNodesChildrenFirst(tree_nodes);

	    String lex_gorn = null;
	    Word lex_word = null;
	    
	    if (tag_tree.hasLex()) {

		lex_gorn = ((TagNode) tag_tree.getLexAnc()).getAddress();
		lex_word = ((TagNode) tag_tree.getLexAnc()).getWord();
	    }

	    for (TagNode cur_node : tree_nodes) {
		
		if (cur_node.getType() == TagNode.LEX) {

		    //Lexical base case

		    //Word word = cur_node.getWord();

		    //if the lexical word is not an anchor, getWord() will return null. That's not what we want.
		    String word = cur_node.getCategory();

		    //a lexical node can never be above a foot node
		    assert(closed_map.get(cur_node) != null);

		    //adjunction is never possible in base cases

		    //System.err.println("base case, node: " + cur_node);
		    //System.err.println("word: " + word);

		    //if (word == null || word.getWord() == null || word.getWord().length() == 0) {
		    if (word == null || word.length() == 0) {
			//epsilon scan

			if (!tag_tree.hasLex()) {
			    for (int i=0; i <= nTokens; i++) {
				closed_map.get(cur_node)[i][i][0] = 0.0;
				
				add_if_not_in_queue(cur_node,i,i,0);
			    }
			}
			else  {
			    
			    //here, too, we assume that each node does not have more than 9 children.
			    // however, due to the conversion in the constructor we always have binary trees here
			    String cur_gorn = cur_node.getAddress();

			    //find first difference in the gorn addresses
			    int k;
			    for (k=0; k < cur_gorn.length() && k < lex_gorn.length() && cur_gorn.charAt(k) == lex_gorn.charAt(k); k++)
				;

			    if (k >= cur_gorn.length() || k >= lex_gorn.length()) {
				System.out.println("STRANGE: either the lexical node or the epsilon node are not a leaf");
			    }

			    for (int i=0; i <= nTokens; i++) {			    

				boolean possible = false;

				if (cur_gorn.charAt(k) < lex_gorn.charAt(k)) {
				    //epsilon is left of the lexical node

				    for (Integer wi : word_positions.get(lex_word.getWord())) {
					
					if (wi.intValue() > i) {
					    possible = true;
					    break;
					} 
				    }
				}
				else {
				    //epsilon is right of the lexical node

				    for (Integer wi : word_positions.get(lex_word.getWord())) {
					
					if (wi.intValue() <= i) {
					    possible = true;
					    break;
					} 
				    }
				}

				if (possible) {

				    closed_map.get(cur_node)[i][i][0] = 0.0;
				
				    add_if_not_in_queue(cur_node,i,i,0);
				}
			    }
			}
		    }
		    else {
			//true word match

			for (int i=0; i < nTokens; i++) {
			    if (tokens.get(i).getWord() == word) {
				closed_map.get(cur_node)[i][i+1][0] = 0.0;
				//System.err.println("word match for pos" + i);

				add_if_not_in_queue(cur_node,i,i+1,0);
			    }
			}
		    }
		}
		else if (cur_node == tag_tree.getFoot()) {

		    //here, too, we assume that each node does not have more than 9 children.
		    // however, due to the conversion in the constructor we always have binary trees here
		    String cur_gorn = cur_node.getAddress();

		    if (!tag_tree.hasLex()) {

			for (int i1= 0; i1 <= nTokens; i1++) 
			    for (int i2 = i1; i2 <= nTokens; i2++)
				foot_nodes_ij.get(i1).get(i2).add(cur_node);
		    }
		    else {

			//find first difference in the gorn addresses
			int k;
			for (k=0; k < cur_gorn.length() && k < lex_gorn.length() && cur_gorn.charAt(k) == lex_gorn.charAt(k); k++)
			    ;
			
			if (k >= cur_gorn.length() || k >= lex_gorn.length()) {
			    System.out.println("STRANGE: either the lexical node or the leaf node are not a leaf");
			}
			
			if (cur_gorn.charAt(k) < lex_gorn.charAt(k)) {
			    //leaf node is left of the lexical node
			    
			    int i_max = -1;

			    for (Integer wi : word_positions.get(lex_word.getWord())) {
				
				if (wi.intValue() > i_max) {
				    i_max = wi.intValue();
				} 
			    }
			    
			    for (int i1=0; i1 < i_max; i1++) 
				for (int i2 = i1; i2 < i_max; i2++)
				    foot_nodes_ij.get(i1).get(i2).add(cur_node);
			    
			}
			else {
			    //leaf node is right of the lexical node
			    
			    int i_min = nTokens+1;
			    
			    for (Integer wi : word_positions.get(lex_word.getWord())) {
				
				if (wi.intValue() < i_min) {
				    i_min = wi.intValue();
				} 
			    }
			    
			    for (int i1= i_min; i1 <= nTokens; i1++) 
				for (int i2 = i1; i2 <= nTokens; i2++)
				    foot_nodes_ij.get(i1).get(i2).add(cur_node);
			    
			}
		    }


		    //foot predict is now handled later on in the agenda

		    // assert(gap_map.get(cur_node) != null);

		    // //Foot Predict
		    // for (int i=0; i <= nTokens; i++) {
		    // 	for (int j=i; j <= nTokens; j++) {
		    // 	    gap_map.get(cur_node)[i][i][j][j][0] = 0.0;

		    // 	    add_if_not_in_queue(cur_node,i,i,j,j,0);
		    // 	}
		    // }
		}
		else if (cur_node.getChildren() == null || cur_node.getChildren().size() == 0) {
		    //substitution node

		    if (!tag_tree.hasLex()) {

			for (int i1 = 0; i1 <= nTokens; i1++) 
			    for (int i2 = i1; i2 <= nTokens; i2++)
				subst_nodes_ij.get(i1).get(i2).add(cur_node);
		    }
		    else {

			//here, too, we assume that each node does not have more than 9 children.
			// however, due to the conversion in the constructor we always have binary trees here
			String cur_gorn = cur_node.getAddress();

			//find first difference in the gorn addresses
			int k;
			for (k=0; k < cur_gorn.length() && k < lex_gorn.length() && cur_gorn.charAt(k) == lex_gorn.charAt(k); k++)
			    ;
			
			if (k >= cur_gorn.length() || k >= lex_gorn.length()) {
			    System.out.println("STRANGE: either the lexical node or the leaf node are not a leaf");
			}
			
			if (cur_gorn.charAt(k) < lex_gorn.charAt(k)) {
			    //leaf node is left of the lexical node
			    
			    int i_max = -1;
			    
			    for (Integer wi : word_positions.get(lex_word.getWord())) {
				
				if (wi.intValue() > i_max) {
				    i_max = wi.intValue();
				} 
			    }
			    
			    for (int i1=0; i1 < i_max; i1++) 
				for (int i2 = i1; i2 < i_max; i2++)
				    subst_nodes_ij.get(i1).get(i2).add(cur_node);
			    
			}
			else {
			    //leaf node is right of the lexical node
			    
			    int i_min = nTokens+1;
			    
			    for (Integer wi : word_positions.get(lex_word.getWord())) {
					
				if (wi.intValue() < i_min) {
				    i_min = wi.intValue();
				} 
			    }
			    
			    for (int i1= i_min; i1 <= nTokens; i1++) 
				for (int i2 = i1; i2 <= nTokens; i2++)
				    subst_nodes_ij.get(i1).get(i2).add(cur_node);
			}
		    }

		}
	    }
	}

	long chartInitTime = System.nanoTime() - chartStartTime;
	System.err.println("Time for chart initialization: " + (chartInitTime)/(Math.pow(10, 9))+" sec.");

	//b) proceed with the chart (derived cases)
	

	int active_list = 0;

	while (active_list <= nTokens) {
	
	    while (active_list <= nTokens && agenda.get(active_list).size() == 0) {
		active_list++;
	    }
	    if (active_list > nTokens)
		break;


	    TAGAgendaItem cur_item = agenda.get(active_list).poll();

	    TagNode cur_node = cur_item.node;
	    
	    List<Node> cur_children = cur_node.getChildren();
	    int nChildren = (cur_children == null) ? 0 : cur_children.size();
	    
	    assert(nChildren <= 2);

	    String category = cur_node.getCategory();

	    if (cur_item.i2 == -1) {
		//current node is NOT above a foot node
		

		int i = cur_item.i1;
		int j = cur_item.j2;
		int pos = cur_item.pos;

		closed_in_queue.get(cur_node)[i][j][pos] = false;

		double[][][] cur_map = closed_map.get(cur_node);

		double base_score = cur_map[i][j][pos]; 

		if (pos == 1) {

		    //null-adjoin
		    //CROSS-CHECK: is this the correct check for mandatory adjunction?
		    if (cur_node.getAdjStatus() != TagNode.MADJ) {

			if (base_score < cur_map[i][j][0]) {

			    cur_map[i][j][0] = base_score;
			    //TODO:set trace information
			    
			    add_if_not_in_queue(cur_node,i,j,0);
			}
		    }

		    //true adjoin
		    //CROSS-CHECK: is this the correct check for forbidden adjunction?
		    if (nChildren >  0 && !cur_node.isNoadj()) {

			//NOTE: could keep track of the maximal span derived so far => reduce the spans of the loops

			// we need only combine with items that have already been dequeued (all others will trigger a separate execution later)

			for (TagTree subst_tag_tree : auxiliary_trees.get(category)) {

			    if (!adjunction_possible(cur_node,subst_tag_tree)) 
				continue;
			    
			    // for (int i_outer = 0; i_outer <= i; i_outer++) {
			    // 	for (int j_outer = j; j_outer <= nTokens; j_outer++) {

			    int i_outer = i;
			    int j_outer = j;
			    
				
			    double prev_score = cur_map[i_outer][j_outer][0];
			    double best_score = prev_score;
			    
			    TagNode subst_root = (TagNode) subst_tag_tree.getRoot();
			    
			    double hyp_score = base_score + gap_map.get(subst_root)[i_outer][i][j][j_outer][0]
				+ 1.0; //this is the tree weight
			    
			    if (hyp_score < best_score) {
				
				best_score = hyp_score;
				//TODO: set trace information
			    }
			    
			    if (best_score < prev_score) {
				
				cur_map[i_outer][j_outer][0] = best_score;
				//TODO:set trace information
				
				add_if_not_in_queue(cur_node,i_outer,j_outer,0);
			    }
			}
		    }
		}
		else {
		    //pos == 0

		    //a) moves
		    TagNode parent = parent_map.get(cur_node);
		    if (parent != null) {
			
			List<Node> siblings = parent.getChildren();

			if (siblings.size() == 1) {
			    //unary move

			    if (base_score < closed_map.get(parent)[i][j][1]) {

				closed_map.get(parent)[i][j][1] = base_score;

				//TODO: set trace info

				add_if_not_in_queue(parent,i,j,1);
			    }
			}
			else {
			    //binary move

			    if (siblings.get(0) == cur_node) {
				//current node is left child
			
				TagNode sibling = (TagNode) siblings.get(1);
	
				if (closed_map.get(sibling) != null) {
				    //sibling is not above a foot node

				    // we need only combine with items that have already been dequeued (all others will trigger a separate execution later)

				    //int limit = nTokens;
				    int limit = Math.min(nTokens, j+active_list);
				    
				    for (int j_outer = j;  j_outer <= limit; j_outer++) {

					double hyp = base_score + closed_map.get(sibling)[j][j_outer][0];
					
					if (hyp < closed_map.get(parent)[i][j_outer][1]) {
					    
					    closed_map.get(parent)[i][j_outer][1] = hyp;
					    //TODO: set trace information
					    
					    add_if_not_in_queue(parent,i,j_outer,1);
					}
				    }
				}
				else {
				    //sibling is above a foot node

				    // we need only combine with items that have already been dequeued (all others will trigger a separate execution later)

				    //int limit = nTokens;
				    int limit = Math.min(nTokens, j+active_list);

				    for (int i_inner = j; i_inner <= limit; i_inner ++) {
					for (int j_inner = i_inner; j_inner <= limit; j_inner ++) {

					    for (int j_outer = j_inner; j_outer <= limit; j_outer++) {

						double hyp = base_score + gap_map.get(sibling)[j][i_inner][j_inner][j_outer][0];
						
						if (hyp < gap_map.get(parent)[i][i_inner][j_inner][j_outer][1]) {

						    gap_map.get(parent)[i][i_inner][j_inner][j_outer][1] = hyp;
						    //TODO: set trace info

						    add_if_not_in_queue(parent,i,i_inner,j_inner,j_outer,1);
						}
					    }
					}
				    }
				}
			    }
			    else {
				//current node is right child

				TagNode sibling = (TagNode) siblings.get(0);
				
				if (closed_map.get(sibling) != null) {
				    //sibling is not above a foot node

				    // we need only combine with items that have already been dequeued (all others will trigger a separate execution later)

				    //int limit = 0;
				    int limit = Math.max(0,i-active_list);

				    for (int i_outer = limit; i_outer <= i; i_outer++) {

					double hyp = base_score + closed_map.get(sibling)[i_outer][i][0];
					
					if (hyp < closed_map.get(parent)[i_outer][j][1]) {
					    
					    closed_map.get(parent)[i_outer][j][1] = hyp;
					    //TODO: set trace information
					    
					    add_if_not_in_queue(parent,i_outer,j,1);
					}
				    }
				}
				else {
				    //sibling is above a foot node

				    final double[][][][][] sib_map = gap_map.get(sibling);
				    double[][][][][] par_map = gap_map.get(parent);

				    // we need only combine with items that have already been dequeued (all others will trigger a separate execution later)

				    //int limit = 0;
				    int limit = Math.max(0,i-active_list);

				    for (int i_outer = limit; i_outer <= i; i_outer++) {
					for (int i_inner = i_outer; i_inner <=i; i_inner++) {
					    
					    for (int j_inner = i_inner; j_inner <= i; j_inner++) {

						double hyp = base_score + sib_map[i_outer][i_inner][j_inner][i][0];

						if (hyp < par_map[i_outer][i_inner][j_inner][j][1]) {

						    par_map[i_outer][i_inner][j_inner][j][1] = hyp;
						    add_if_not_in_queue(parent,i_outer,i_inner,j_inner,j,1);
						}
					    }
					}
				    }
				}
			    }
			}
		    }
		    else {
		    
			//b) root node => substitution

			TagTree tag_tree = tree_map.get(cur_node);
			
			//System.err.println("tag tree:"+ tag_tree);
			
			double hyp_score = base_score + 1.0; //add the tree weight

			//for (TagNode subst_node : substitution_nodes) {
			for (TagNode subst_node : subst_nodes_ij.get(i).get(j)) {
			    
			    //System.err.println("subst node: "+ subst_node);

			    if (substitution_possible(subst_node,tag_tree)) {
				
				if (hyp_score < closed_map.get(subst_node)[i][j][0] ) {

				    closed_map.get(subst_node)[i][j][0] = hyp_score;
				    //TODO: set trace information
				    
				    add_if_not_in_queue(subst_node,i,j,0);
				}
			    }
			}
		    }

		}
	    }
	    else {
		//current node IS above above a foot node

		int i1 = cur_item.i1;
		int i2 = cur_item.i2;
		int j1 = cur_item.j1;
		int j2 = cur_item.j2;
		int pos = cur_item.pos;

		double[][][][][] cur_map = gap_map.get(cur_node);

		gap_in_queue.get(cur_node)[i1][i2][j1][j2][pos] = false;

		double base_score = cur_map[i1][i2][j1][j2][pos];

		if (pos == 1) {


		    //null-adjoin
		    //CROSS-CHECK: is this the correct check for mandatory adjunction?
		    if (cur_node.getAdjStatus() != TagNode.MADJ) {

			if (base_score < cur_map[i1][i2][j1][j2][0]) {

			    cur_map[i1][i2][j1][j2][0] = base_score;
			    //TODO:set trace information
			    
			    add_if_not_in_queue(cur_node,i1,i2,j1,j2,0);
			}
		    }

		    //true adjoin
		    //CROSS-CHECK: is this the correct check for forbidden adjunction?
		    if (nChildren >  0 && !cur_node.isNoadj()) {
			
			for (TagTree subst_tag_tree : auxiliary_trees.get(category)) {
				    
			    if (!adjunction_possible(cur_node,subst_tag_tree)) 
				continue;

			    // we need only combine with items that have already been dequeued (all others will trigger a separate execution later)

			    //for (int i_outer = 0; i_outer <= i1; i_outer++) {
			    //   for (int j_outer = j2; j_outer <= nTokens; j_outer++) {

			    int i_outer = i1;
			    int j_outer = j2;
				    
			    double prev_score = cur_map[i_outer][i2][j1][j_outer][0];
			    double best_score = prev_score;
				    
			    TagNode subst_root = (TagNode) subst_tag_tree.getRoot();
			    
			    double hyp_score = base_score + gap_map.get(subst_root)[i_outer][i1][j2][j_outer][0]
				+ 1.0; //this is the tree weight
			    
			    if (hyp_score < best_score) {
				
				best_score = hyp_score;
				//TODO: set trace information
			    }

			    if (best_score < prev_score) {
				
				cur_map[i_outer][i2][j1][j_outer][0] = best_score;
				//TODO: set trace information
					
				add_if_not_in_queue(cur_node,i_outer,i2,j1,j_outer,0);
			    }
			}
			
		    }
		}
		else {
		    //pos == 0

		    TagNode parent = parent_map.get(cur_node);
		    if (parent != null) {
			
			//a) moves

			List<Node> siblings = parent.getChildren();

			if (siblings.size() == 1) {
			    //unary move

			    if (base_score < gap_map.get(parent)[i1][i2][j1][j2][1] ) {

				gap_map.get(parent)[i1][i2][j1][j2][1] = base_score;
				//TODO: set trace info

				add_if_not_in_queue(parent,i1,i2,j1,j2,1);
			    }
			}
			else {
			    //binary moves


			    if (siblings.get(0) == cur_node) {
				//current node is left child
			
				TagNode sibling = (TagNode) siblings.get(1);
				
				// we need only combine with items that have already been dequeued (all others will trigger a separate execution later)

				//int limit = nTokens;
				int limit = Math.min(nTokens,j2+active_list);

				for (int j_outer = j2; j_outer <= limit; j_outer++) {

				    double hyp = base_score + closed_map.get(sibling)[j2][j_outer][0];
				    
				    if (hyp < gap_map.get(parent)[i1][i2][j1][j_outer][1]) {

					gap_map.get(parent)[i1][i2][j1][j_outer][1] = hyp;
					//TODO: set trace info

					add_if_not_in_queue(parent,i1,i2,j1,j_outer,1);
				    }
				}
			    }
			    else {

				//current node is right child

				TagNode sibling = (TagNode) siblings.get(0);

				// we need only combine with items that have already been dequeued (all others will trigger a separate execution later)

				//int limit = 0;
				int limit = Math.max(0,i1-active_list);

				for (int i_outer = limit; i_outer <= i1; i_outer++) {

				    double hyp = base_score + closed_map.get(sibling)[i_outer][i1][0];
				    
				    if (hyp < gap_map.get(parent)[i_outer][i2][j1][j2][1]) {

					gap_map.get(parent)[i_outer][i2][j1][j2][1] = hyp;
					//TODO: set trace info
					
					add_if_not_in_queue(parent,i_outer,i2,j1,j2,1);
				    }
				}
			    }
			}

		    }
		    else {
			// b) root node => adjunction

			TagTree tag_tree = tree_map.get(cur_node);

			//here we only rely on smaller spans => nothing to improve

			for (TagNode adj_node : adjunction_nodes) {

			    if (adjunction_possible(adj_node,tag_tree)) {

				if (closed_map.get(adj_node) != null) {

				    double hyp = base_score + closed_map.get(adj_node)[i2][j1][1]
					+ 1.0; //tree weight
				    
				    if (hyp < closed_map.get(adj_node)[i1][j2][0]) {
					
					closed_map.get(adj_node)[i1][j2][0] = hyp;
					//TODO: set trace information
					
					add_if_not_in_queue(adj_node,i1,j2,0);
				    }
				    
				}
				else {
				    
				    double[][][][][] adj_map = gap_map.get(adj_node);

				    for (int i_inner = i2; i_inner <= j1; i_inner++) {
					for (int j_inner = i_inner; j_inner <= j1; j_inner ++) {
					    
					    double hyp = base_score + gap_map.get(adj_node)[i2][i_inner][j_inner][j1][1]
						+ 1.0; //tree weight
					    
					    if (hyp < adj_map[i1][i_inner][j_inner][j2][0]) {

						adj_map[i1][i_inner][j_inner][j2][0] = hyp;
						//TODO: set trace information
						
						add_if_not_in_queue(adj_node,i1,i_inner,j_inner,j2,0);
					    }
					}
				    }
				}
			    }
			}
		    }


		}

	    }
	}

	long chartTime = System.nanoTime() - chartStartTime;
	System.err.println("Total time for init & chart construction: " + (chartTime)/(Math.pow(10, 9))+" sec.");


	return true;
    }


    public List<Tidentifier> extract_best(Map<Tidentifier, List<Rule>> rules, String axiom) {
	
	//extract best parse (if any)
	rules.clear();


	System.err.println("Chart filled. Now traversing the trees to find the best parse.");

	TagTree best_start_tree = null;
	double best_parse_score = 1e300;
	
	//find best start tree
	for (TagTree tag_tree : initial_trees.get(axiom)) {

	    TagNode tree_root = (TagNode) tag_tree.getRoot();

	    System.err.println("tree root category: \"" + tree_root.getCategory() + "\", looking for \"" + axiom + "\".");

	    if (tree_root.getCategory().equals(axiom)) {

		double score = closed_map.get(tree_root)[0][nTokens][0]
		    + 1.0; //this is the tree weight

		if (score < best_parse_score) {

		    best_parse_score = score;
		    best_start_tree = tag_tree;
		}

		System.err.println("parse score: " + score);
		//System.err.println("unfinished score: " + closed_map.get(tree_root)[0][nTokens][1]);
	    }

	    //DEBUG
	    {
		
		List<TagNode> tree_nodes = new LinkedList<TagNode>();
		tree_root.getAllNodesChildrenFirst(tree_nodes);
		//List<TagNode> tree_nodes = tree_node_lookup.get(tag_tree);		

		if (tree_nodes.size() <= 9) {

		    //System.err.println("scores for root of tree " + tag_tree.getId() + "\n" + tag_tree.getRoot().toString() );
		    System.err.println("scores for closed nodes of tree " + tag_tree.getId() + " (" + tag_tree.getOriginalId() + ")"
				       + "\n" + tag_tree.getRoot().toString() );
		    
		    for (TagNode cur_node : tree_nodes) {

			System.err.println("####node " + cur_node.toString());

			for (int i=0; i <= nTokens; i++) {
			    for (int j=i; j <= nTokens; j++) {
				System.err.println("["+i+","+j+"]: " + closed_map.get(cur_node)[i][j][0] 
						   + ", " + closed_map.get(cur_node)[i][j][1]);
			    }
			}
		    }
		}
	    }
	    //END_DEBUG
	}
	
	List<Tidentifier> root_trees = new LinkedList<Tidentifier>();

	if (best_start_tree == null) {

	    System.err.println("No parse found.");
	}
	else {

	    //actual traceback
	    Tidentifier best_root_tree = new Tidentifier(0,revGrammarDict.get(best_start_tree),best_start_tree.getId());
	    
	    root_trees.add(best_root_tree);
	    
	    rules.put(best_root_tree,new LinkedList<Rule>());
	    rules.get(best_root_tree).add(new Rule(best_root_tree));

	    this.single_trace_no_gap((TagNode) best_start_tree.getRoot(), 0, nTokens, 0, best_root_tree, rules);
	}


	return root_trees;
    }
  

    /********************************************************/
    public List<Tidentifier> extract_all(Map<Tidentifier, List<Rule>> rules, String axiom) {

	//extract best parse (if any)
	rules.clear();

	List<Tidentifier> root_trees = new LinkedList<Tidentifier>();

	//DEBUG
	// Iterator<String> its = grammarDict.keySet().iterator();
	// while (its.hasNext()) {
	//     String cur_key =  its.next();
	//     TagTree tag_tree = grammarDict.get(cur_key);

	//     TagNode tree_root = (TagNode) tag_tree.getRoot();

	//     System.err.println("############ scores for closed and gap nodes of tree " + tag_tree.getId() + " (" + tag_tree.getOriginalId() + ")"
	// 		       + "\n" + tag_tree.getRoot().toString() );

	    
	//     List<TagNode> tree_nodes = new LinkedList<TagNode>();
	//     tree_root.getAllNodesChildrenFirst(tree_nodes);

	//     for (TagNode node : tree_nodes) {

	// 	System.err.println("entries for node " + node);

		
	// 	if (closed_map.get(node) != null) {
		    
	// 	    for (int i=0; i <= nTokens; i++) {
	// 		for (int j=i; j <= nTokens; j++) {
	// 		    System.err.println("["+i+","+j+"]: " + closed_map.get(node)[i][j][0] 
	// 				       + ", " + closed_map.get(node)[i][j][1]);
	// 		}
	// 	    }
	// 	}
	// 	else {
		    
	// 	    for (int i1=0; i1 <= nTokens; i1++) {
	// 		for (int i2=i1; i2 <= nTokens; i2++) {
	// 		    for (int j1=i2; j1 <= nTokens; j1++) {
	// 			for (int j2=j1; j2 <= nTokens; j2++) {
	// 			    System.err.println("["+i1+","+i2+"]["+j1+","+j2+"]: " + gap_map.get(node)[i1][i2][j1][j2][0] 
	// 					       + ", " + gap_map.get(node)[i1][i2][j1][j2][1]);
	// 			}
	// 		    }
	// 		}
	// 	    }
	// 	}
	//     }
	// }
	//END_DEBUG


	if (initial_trees.get(axiom) == null) {
	    return root_trees;
	}

	for (TagTree tag_tree : initial_trees.get(axiom)) {

	    TagNode tree_root = (TagNode) tag_tree.getRoot();

	    System.err.println("tree root category: \"" + tree_root.getCategory() + "\", looking for \"" + axiom + "\".");

	    if (tree_root.getCategory().equals(axiom)) {

		double score = closed_map.get(tree_root)[0][nTokens][0]
		    + 1.0; //this is the tree weight

		if (score < 1e300) {

		    Tidentifier tree_id = new Tidentifier(0,revGrammarDict.get(tag_tree),tag_tree.getId());
	    
		    root_trees.add(tree_id);
	    
		    rules.put(tree_id,new LinkedList<Rule>());
		    rules.get(tree_id).add(new Rule(tree_id));


		    Set<Integer> rule_idx_set = new HashSet<Integer>();
		    rule_idx_set.add(new Integer(0));
		    
		    this.trace_all_no_gap((TagNode) tag_tree.getRoot(), 0, nTokens, 0, tree_id, rules, rule_idx_set);
		}
	    }
	}

	return root_trees;
    }

};
