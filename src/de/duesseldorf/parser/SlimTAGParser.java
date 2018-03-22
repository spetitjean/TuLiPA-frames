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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.Vector;

import de.duesseldorf.frames.Situation;

import de.tuebingen.forest.Rule;
import de.tuebingen.forest.Tidentifier;
import de.tuebingen.forest.TreeOp;
import de.tuebingen.rcg.PredComplexLabel;
//import de.tuebingen.tree.Grammar;
import de.tuebingen.tag.Environment;
import de.tuebingen.tag.TagNode;
import de.tuebingen.tag.TagTree;
import de.tuebingen.tag.Value;
import de.tuebingen.tokenizer.Word;
import de.tuebingen.tree.Node;

/**
 * @author tosch
 *
 */
public class SlimTAGParser {

    private Map<String, TagTree> grammarDict;
    private Map<TagTree, String> revGrammarDict;

    private Map<TagNode, Map<Integer, Map<Integer, double[]>>> closed_map;

    private Map<TagNode, Map<Integer, Map<Integer, Map<Integer, Map<Integer, double[]>>>>> gap_map;

    List<TagNode> all_nodes; // all nodes except lexical nodes and foot nodes,
                             // in an order so that children are listed before
                             // their parents

    private Map<String, List<TagTree>> initial_trees;
    private Map<String, List<TagTree>> auxiliary_trees;

    // only used in the agenda-routine
    private Vector<Queue<TAGAgendaItem>> agenda;
    List<TagNode> foot_nodes;
    private Map<String, Vector<Vector<List<TagNode>>>> foot_nodes_ij;
    private Map<TagNode, Map<Integer, Map<Integer, boolean[]>>> closed_in_queue;
    private Map<TagNode, Map<Integer, Map<Integer, Map<Integer, Map<Integer, boolean[]>>>>> gap_in_queue;
    // end of only used in the agenda-routine

    private Map<TagNode, String> adr_map;

    private int nTokens;

    public SlimTAGParser(Map<String, TagTree> dict) {
        grammarDict = new HashMap<String, TagTree>();
        revGrammarDict = new HashMap<TagTree, String>();

        adr_map = new HashMap<TagNode, String>();
	
        Iterator<String> its = dict.keySet().iterator();

        while (its.hasNext()) {
	    
            String cur_key = its.next();
            TagTree cur_tree = dict.get(cur_key);

            List<TagNode> nodes = new LinkedList<TagNode>();
            // this time we need the anchestors before the children
            ((TagNode) cur_tree.getRoot()).getAllNodesParentFirst(nodes);

            boolean is_binary = true;

            for (TagNode n : nodes) {

                if (n.getChildren() != null && n.getChildren().size() > 2) {
                    is_binary = false;
                    break;
                }
            }

            if (is_binary) {
                grammarDict.put(cur_key, cur_tree);

                for (TagNode n : nodes){
		    //System.out.println("adr_map.put with address[0]: "+n.getAddress());
                    adr_map.put(n, n.getAddress());
		}

                revGrammarDict.put(cur_tree, cur_key);
            } else {

                // System.err.println("Internal conversion to binary tree");
                // System.err.println("tree: " + cur_tree.getRoot());

                // NOTE: right now we do not copy all attributes of the tree:
                // the tree is used only internally, all _relevant_ attributes
                // should be copied.
                TagTree bin_tree = new TagTree(cur_tree.getId());
                bin_tree.setOriginalId(cur_tree.getOriginalId());

                grammarDict.put(cur_key, bin_tree);
                revGrammarDict.put(bin_tree, cur_key);

                // maps nodes in the old tree (cur_tree) to nodes in the new
                // tree (bin_tree)
                Map<TagNode, TagNode> node_map = new HashMap<TagNode, TagNode>();

                // this won't copy children, as desired
                TagNode root = new TagNode((TagNode) cur_tree.getRoot());
                root.setAddress("0");
                if (root.getChildren() != null) {
                    System.err.println(
                            "Error: copying the root node gave a non-null list of children");
                }

                bin_tree.setRoot(root);

                node_map.put((TagNode) cur_tree.getRoot(), root);

                for (TagNode org_node : nodes) {

                    //System.err.println("at node " + org_node);

                    TagNode map_node = node_map.get(org_node);

                    String map_gorn = map_node.getAddress();

                    if (org_node.getChildren() != null) {

                        if (org_node.getChildren().size() <= 2) {

                            for (int k = 0; k < org_node.getChildren()
                                    .size(); k++) {

                                TagNode cur_child = (TagNode) org_node
                                        .getChildren().get(k);

                                TagNode new_node = new TagNode(cur_child);
                                if (new_node.getLabel() == null) {
                                    // apparently this happens for lex. nodes
                                    // obtained by anchoring
                                    new_node.setLabel(org_node.getLabel());
                                }

                                // System.err.println("new node has label: " +
                                // new_node.getLabel());

                                if (map_gorn == "0") // parent is root node
                                    new_node.setAddress(String.valueOf(k + 1));
                                else
                                    new_node.setAddress(map_gorn + "."
                                            + String.valueOf(k + 1));

				//System.out.println("adr_map.put with address [1]: "+cur_child.getAddress());
                                adr_map.put(new_node, cur_child.getAddress());

                                map_node.add2children(new_node);

                                node_map.put(cur_child, new_node);
                            }
                        } else {

                            TagNode inter_parent = map_node;

                            for (int k = 0; k < org_node.getChildren()
                                    .size(); k++) {

                                String inter_gorn = inter_parent.getAddress();

                                TagNode cur_child = (TagNode) org_node
                                        .getChildren().get(k);

                                TagNode new_node = new TagNode(cur_child);
                                if (new_node.getLabel() == null) {
                                    // apparently this happens for lex. nodes
                                    // obtained by anchoring
                                    new_node.setLabel(org_node.getLabel());
                                }

                                // System.err.println("new node has label: " +
                                // new_node.getLabel());

                                int adr_suffix = 1;
                                if (k + 1 == org_node.getChildren().size())
                                    adr_suffix = 2;

                                if (inter_gorn == "0") // parent is root node
                                    new_node.setAddress(
                                            String.valueOf(adr_suffix));
                                else
                                    new_node.setAddress(
                                            inter_gorn + "." + adr_suffix);

				//System.out.println("adr_map.put with address [2]: "+cur_child.getAddress());
                                adr_map.put(new_node, cur_child.getAddress());

                                inter_parent.add2children(new_node);

                                node_map.put(cur_child, new_node);

                                if (k + 2 < org_node.getChildren().size()) {
                                    TagNode inter_node = new TagNode();
                                    inter_node.setAddress(inter_gorn + ".2");

                                    // the label should not matter much, but we
                                    // have to set one
                                    inter_node.setLabel(map_node.getLabel());

                                    // to keep equivalence, adjunction cannot be
                                    // allowed at intermediate nodes.
                                    inter_node.setType(TagNode.NOADJ);

                                    inter_parent.add2children(inter_node);

                                    // NOTE: no entries in the node maps (there
                                    // is no equivalent node)

                                    inter_parent = inter_node;
                                }

                            }
                        }
                    }
                }

                List<TagNode> new_nodes = new LinkedList<TagNode>();
                root.getAllNodesParentFirst(new_nodes);

                // this will fill all data structures of concerning the root and
                // its descendants
                bin_tree.findMarks(root, "0");

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

        initial_trees = new HashMap<String, List<TagTree>>();
        auxiliary_trees = new HashMap<String, List<TagTree>>();
        all_nodes = new LinkedList<TagNode>();

        its = grammarDict.keySet().iterator();
        while (its.hasNext()) {
            String cur_key = its.next();
            TagTree tag_tree = grammarDict.get(cur_key);

            String category = ((TagNode) tag_tree.getRoot()).getCategory();

            if (tag_tree.hasFoot()) {
                if (auxiliary_trees.get(category) == null)
                    auxiliary_trees.put(category, new LinkedList<TagTree>());
                auxiliary_trees.get(category).add(tag_tree);
            } else {
                if (initial_trees.get(category) == null)
                    initial_trees.put(category, new LinkedList<TagTree>());
                initial_trees.get(category).add(tag_tree);
            }

            ((TagNode) tag_tree.getRoot()).getAllNodesChildrenFirst(all_nodes);
        }

        /***
         * remove all lexical nodes and foot nodes from the list all_nodes
         ***/
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

        for (TagNode n : to_remove) {
            // System.err.println("Removing "+n.getCategory());
            all_nodes.remove(n);
        }

        // System.err.println("Remaining nodes: ");
        // for (TagNode n : all_nodes) {
        // if(n.getCategory()!=null){
        // System.err.println(n.getCategory());
        // }
        // else{
        // System.err.println("Got null category: "+n.getType()+"
        // "+n.getName());
        // }
        // }
    }

    private boolean substitution_possible(TagNode node, TagTree tag_tree) {

        TagNode subst_root = (TagNode) tag_tree.getRoot();

        Value top1 = node.getLabel().getFeat("top");
        Value top2 = subst_root.getLabel().getFeat("top");

        boolean allowed = true;
        try {

            Value v = Value.unify(top1, top2, new Environment(0));
            if (v == null)
                allowed = false;

        } catch (Exception e) {
            allowed = false;
        }

        // TODO: CROSS-CHECK
        // check if the labels match
        return (node.getCategory().equals(subst_root.getCategory()));
        // return (allowed);
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

            Value v = Value.unify(top1, top2, new Environment(0));
            if (v == null)
                allowed1 = false;

        } catch (Exception e) {
            allowed1 = false;
        }

        boolean allowed2 = true;
        try {

            Value v = Value.unify(bottom1, bottom2, new Environment(0));
            if (v == null)
                allowed2 = false;

        } catch (Exception e) {
            allowed2 = false;
        }

        // TODO: CROSS-CHECK
        return (node.getCategory().equals(subst_root.getCategory()));
        // return (allowed1 && allowed2) ;
    }

    public List<Tidentifier> parse(List<Word> tokens,
				   Map<Tidentifier, List<Rule>> rules, String axiom) {

        // boolean success = build_chart(tokens);
        boolean success = build_chart_via_agenda(tokens);
	
        if (!success)
            return new LinkedList<Tidentifier>();

        return extract_all(rules, axiom);
    }

    private void trace_all_no_gap(TagNode cur_node, int i, int j, int pos,
            Tidentifier cur_id, Map<Tidentifier, List<Rule>> rules,
            Set<Integer> rule_idx_set) {

	//System.err.println("trace_all_no_gap("+i+","+j+","+pos + ") for "
        // +cur_node.toString());
        // System.err.println(rule_idx_set.size() + " rules");

        if (cur_node.getType() == TagNode.LEX) {
            //System.err.println("Lexical node: "+cur_node.getCategory());
            return;
        }

        String category = cur_node.getCategory();
        List<Node> cur_children = cur_node.getChildren();
        int nChildren = (cur_children == null) ? 0 : cur_children.size();

        if (nChildren > 2) {
            System.err.println(
                    "ERROR: nodes of degree 3 or more can presently not be handled.");
            return;
        }
	// System.err.println("In trace all no gaps [0]");
        boolean rule_set_used = false;

        List<Rule> saved_rules = new LinkedList<Rule>();

        for (Integer r : rule_idx_set) {

            Rule toCopy = rules.get(cur_id).get(r.intValue());

            saved_rules.add(new Rule(toCopy));
        }
	// System.err.println("In trace all no gaps [1]");
        if (pos == 1) {
            // no adjunction included here
	    // System.err.println("In trace all no gaps [pos 1]");

            // moves. Note that moves do not close gaps
            if (nChildren > 0) {

                if (nChildren == 1) {
                    // here we need to be sure that the children have already
                    // been processed for this span
		    // System.err.println("In trace all no gaps [1 child]");
                    TagNode child = (TagNode) cur_children.get(0);

                    trace_all_no_gap(child, i, j, 0, cur_id, rules,
                            rule_idx_set);
		    // System.err.println("Done trace all no gaps [2]");

                }
		else {
		    
                    // here we need to be sure that the children have already
                    // been processed for this span
                    // (there can be empty sub-spans)
		    // System.err.println("In trace all no gaps [2 children]");

                    assert (nChildren == 2);

                    TagNode child1 = (TagNode) cur_children.get(0);
                    TagNode child2 = (TagNode) cur_children.get(1);
		    // System.err.println("In trace all no gaps [2 children, got the children]");

		    // Added for debugging
		    Iterator<Integer> it;
		    if(closed_map.get(child1).get(i)==null){
			//System.err.println("Strange thing[2]");
			it = new LinkedList<Integer>().iterator();
		    }
		    else{
			it = closed_map.get(child1).get(i)
                            .keySet().iterator();
		    // System.err.println("In trace all no gaps [2 children, starting while loop]");
		    }
			
                    while (it.hasNext()) {
                        int split_point = it.next().intValue();
			// System.err.println("Building hyp");
			// System.err.println("First thing: "+ closed_map.get(child1).get(i)
			// 		   .get(split_point)[0]);
			// System.err.println("Child2: "+ closed_map.get(child2));
			// System.err.println("Split point: "+split_point);
			// System.err.println("Second thing: "+ closed_map.get(child2).get(split_point)
			//		   .get(j)[0]);
			double hyp=0;
			// System.err.println("Child2 split point: "+ closed_map.get(child2).get(split_point));
			// System.err.println("j: "+ j);
			// System.err.println("Child1 get(i): "+ closed_map.get(child1).get(i));
			// System.err.println("split point: "+ split_point);

			if(closed_map.get(child2).get(split_point)!=null){
		   
			    hyp = closed_map.get(child1).get(i)
                                .get(split_point)[0]
                                + closed_map.get(child2).get(split_point)
				.get(j)[0];
			}
			// Added for debugging:
			else{
			    hyp = closed_map.get(child1).get(i)
                                .get(split_point)[0]
                                + closed_map.get(child1).get(i)
				.get(split_point)[0];
			    hyp = 1e300;
			    //System.err.println("Strange thing[3]");
			    //return;
			}
			// System.err.println("hyp: "+ hyp);   
                        if (hyp < 1e300) {

                            Set<Integer> cur_rule_set = rule_idx_set;

                            if (rule_set_used) {

                                Set<Integer> new_rule_set = new HashSet<Integer>();

                                for (Rule r : saved_rules) {
                                    rules.get(cur_id).add(new Rule(r));
                                    new_rule_set
                                            .add(rules.get(cur_id).size() - 1);
                                    rule_idx_set
                                            .add(rules.get(cur_id).size() - 1);
                                }

                                cur_rule_set = new_rule_set;
                            }
                            trace_all_no_gap(child1, i, split_point, 0, cur_id,
                                    rules, cur_rule_set);
			    // System.err.println("Done trace all no gaps [3]");
                            trace_all_no_gap(child2, split_point, j, 0, cur_id,
                                    rules, cur_rule_set);
			    // System.err.println("Done trace all no gaps [4]");

                            rule_set_used = true;
                        }
                    }
                }
            } else {

                // leaf node

                // NOTE: we DON'T have to cover substitution here: after subst.
                // adjunction is no longer possible
            }

        } else {
	    // System.err.println("In trace all no gaps [not pos 1]");
            // adjunction included here

            // a) null-adjoin
	    //System.err.println("Null-adjoin[1]");
            // CROSS-CHECK: is this the correct check for mandatory adjunction?
            if (cur_node.getAdjStatus() != TagNode.MADJ) {
                // adjunction is not obligatory
		// System.err.println("Null adjoin");
		// System.err.println("Cur node: "+ closed_map.get(cur_node));
		// System.err.println("Get i: "+ closed_map.get(cur_node).get(i));
		//// System.err.println("Get j: "+ closed_map.get(cur_node).get(i).get(j));
		double hyp_na=0;
		// Added for debugging
		if(closed_map.get(cur_node).get(i)==null){
		    //hyp_na = closed_map.get(cur_node).get(i).get(j)[1];
		    //System.err.println("Strange thing[1]");
		    hyp_na = 1e300;
		    //return;
		}
		else{
		    hyp_na = closed_map.get(cur_node).get(i).get(j)[1];
		}
                if (hyp_na < 1e300) {
                    trace_all_no_gap(cur_node, i, j, 1, cur_id, rules,
                            rule_idx_set);
		    // System.err.println("Done trace all no gaps [5]");
                    rule_set_used = true;
                }
            }

            if (nChildren == 0) {

                // b) substitution
		//System.err.println("Substitution");

                for (TagTree subst_tag_tree : initial_trees.get(category)) {

                    // this is currently superfluous, we already ensured
                    // matching categories
                    // if (!substitution_possible(cur_node, subst_tag_tree))
                    // continue;

                    TagNode subst_root = (TagNode) subst_tag_tree.getRoot();

                    // NOTE: every time we integrate a tree into another tree,
                    // we add the weight
                    // of the tree. This way the system with the passes is less
                    // complicated.
                    // In the end we have to add the weight of the initial tree

                    double hyp = 1e300;
                    if (closed_map.get(subst_root).get(i) != null
                            && closed_map.get(subst_root).get(i).get(j) != null)
                        hyp = closed_map.get(subst_root).get(i).get(j)[0] + 1.0; // this
                                                                                 // is
                                                                                 // the
                                                                                 // tree
                                                                                 // weight

                    if (hyp < 1e300) {

                        Set<Integer> cur_rule_set = rule_idx_set;

                        if (rule_set_used) {

                            Set<Integer> new_rule_set = new HashSet<Integer>();

                            for (Rule r : saved_rules) {

                                rules.get(cur_id).add(new Rule(r));
                                new_rule_set.add(rules.get(cur_id).size() - 1);
                                rule_idx_set.add(rules.get(cur_id).size() - 1);
                            }

                            cur_rule_set = new_rule_set;
                        }

                        // now we report the tree, and a node address in a
                        // different tree. sounds a bit strange, so CROSS-CHECK!
                        Tidentifier tid = new Tidentifier(rules.size(),
                                revGrammarDict.get(subst_tag_tree),
                                subst_tag_tree.getId());
			//System.out.println("Setting Node ID in SlimTAGParser [0]: "+adr_map.get(cur_node));
                        tid.setNodeId(adr_map.get(cur_node));
                        rules.put(tid, new LinkedList<Rule>());
                        rules.get(tid).add(new Rule(tid));
		    
                        for (Integer r : cur_rule_set) {

                            rules.get(cur_id).get(r.intValue()).getRhs().addOp(
                                    new TreeOp(tid, PredComplexLabel.SUB));
                        }

                        Set<Integer> sub_rule_set = new HashSet<Integer>();
                        sub_rule_set.add(0);

                        trace_all_no_gap(subst_root, i, j, 0, tid, rules,
                                sub_rule_set);
			// System.err.println("Done trace all no gaps [6]");
                        rule_set_used = true;
                    }
                }
            } else if (!cur_node.isNoadj()) {
                // c) adjoining
		//System.err.println("Adjunction[1] ");
					
		
                for (TagTree subst_tag_tree : auxiliary_trees.get(category)) {
		    //System.err.println("[00]");
                    // this is currently superfluous, we already ensured
                    // matching categories
                    // if (!adjunction_possible(cur_node, subst_tag_tree))
                    // continue;

                    TagNode subst_root = (TagNode) subst_tag_tree.getRoot();
                    if (gap_map.get(subst_root).get(i) == null){
			//System.err.println("Strange thing[11]");
			continue;
		    }

                    Iterator<Integer> it_i = closed_map.get(cur_node).keySet()
                            .iterator();

                    while (it_i.hasNext()) {
			//System.err.println("[0]");
			
                        int i_inner = it_i.next().intValue();

                        if (i_inner < i)
                            continue;
                        if (i_inner > j)
                            break;

                        if (gap_map.get(subst_root).get(i).get(i_inner) == null){
			    //System.err.println("Strange thing[12]");
                            continue;
			}

                        Iterator<Integer> it_j = closed_map.get(cur_node).get(i)
                                .keySet().iterator();

                        while (it_j.hasNext()) {
			    //System.err.println("[1]");

                            int j_inner = it_j.next().intValue();

                            if (j_inner < i_inner)
                                continue;
                            if (j_inner > j)
                                break;

		
			    // Added for debugging:
			    double hyp=0;
			    if (gap_map.get(subst_root).get(i)
				.get(i_inner).get(j_inner)== null){
				//System.err.println("Strange thing[4]");
				hyp = 1e300;
				//continue;
			    }
			    else{
				if (gap_map.get(subst_root).get(i).get(i_inner)
                                    .get(j_inner) == null
                                    || gap_map.get(subst_root).get(i)
				    .get(i_inner).get(j_inner)
				    .get(j) == null){
				    //System.err.println("Strange thing[5]");
				    hyp = 1e300;
				    //continue;
				}
				else{
				    
				    //System.err.println("Here[ElseElse]");
				    
				    // NOTE: every time we integrate a tree into another
				    // tree, we add the weight
				    // of the tree. This way the system with the passes
				    // is less complicated.
				    // In the end we have to add the weight of the
				    // initial tree
			 
				    hyp = gap_map.get(subst_root).get(i)
					.get(i_inner).get(j_inner).get(j)[0]
					+ closed_map.get(cur_node).get(i_inner)
					.get(j_inner)[1]
					+ 1.0; // this is the tree weight
				}
			    }
				    
			    //System.err.println("[22]");
                            if (hyp < 1e300) {
				// System.err.println("[222]");
                                Set<Integer> cur_rule_set = rule_idx_set;

                                if (rule_set_used) {

                                    Set<Integer> new_rule_set = new HashSet<Integer>();

                                    for (Rule r : saved_rules) {
					// System.err.println("[3]");

                                        rules.get(cur_id).add(new Rule(r));
                                        new_rule_set.add(
                                                rules.get(cur_id).size() - 1);
                                        rule_idx_set.add(
                                                rules.get(cur_id).size() - 1);
                                    }

                                    cur_rule_set = new_rule_set;
                                }

                                // now we report the tree, and a node address in
                                // a different tree. sounds a bit strange, so
                                // CROSS-CHECK!
                                Tidentifier tid = new Tidentifier(rules.size(),
                                        revGrammarDict.get(subst_tag_tree),
                                        subst_tag_tree.getId());
				// System.out.println("Setting Node ID in SlimTAGParser [2]: "+adr_map.get(cur_node));
				// Simon: if the node has no address (should not happen) we give it 0
				if(adr_map.get(cur_node)!=null){
				    //System.err.println("Strange thing[no address]");
				    tid.setNodeId(adr_map.get(cur_node));
				    //System.err.println("New address: "+adr_map.get(cur_node));
				}
				// else{
				//     tid.setNodeId("0");
				// }
                                rules.put(tid, new LinkedList<Rule>());
                                rules.get(tid).add(new Rule(tid));
				// System.err.println("[4]");

                                for (Integer r : cur_rule_set) {

                                    rules.get(cur_id).get(r.intValue()).getRhs()
                                            .addOp(new TreeOp(tid,
                                                    PredComplexLabel.ADJ));
                                }

                                Set<Integer> sub_rule_set = new HashSet<Integer>();
                                sub_rule_set.add(0);

                                trace_all_with_gap(subst_root, i, i_inner,
                                        j_inner, j, 0, tid, rules,
                                        sub_rule_set);
                                trace_all_no_gap(cur_node, i_inner, j_inner, 1,
                                        cur_id, rules, cur_rule_set);
				// System.err.println("Done trace all no gaps [7]");
                                rule_set_used = true;
                            }
                        }
                    }
                }
            }
        }
    }

    private void trace_all_with_gap(TagNode cur_node, int i1, int i2, int j1,
            int j2, int pos, Tidentifier cur_id,
            Map<Tidentifier, List<Rule>> rules, Set<Integer> rule_idx_set) {

        // System.err.println("trace_all_with_gap("+i1+","+i2+","+j1+","+j2+","+pos
        // + ") for " +cur_node.toString());

        if (cur_node.getType() == TagNode.LEX) {
            System.err.println("STRANGE: lexical node above a foot node");
        }
        if (cur_node.getType() == TagNode.FOOT) {
            return;
        }

        String category = cur_node.getCategory();

        List<Node> cur_children = cur_node.getChildren();
        int nChildren = (cur_children == null) ? 0 : cur_children.size();

        if (nChildren > 2) {
            System.err.println(
                    "ERROR: nodes of degree 3 or more can presently not be handled.");
            return;
        }

        List<Rule> saved_rules = new LinkedList<Rule>();

        for (Integer r : rule_idx_set) {

            Rule toCopy = rules.get(cur_id).get(r.intValue());

            saved_rules.add(new Rule(toCopy));
        }

        boolean rule_set_used = false;

        if (pos == 1) {
            // no adjunction included here

            if (nChildren > 0) {

                if (nChildren == 1) {

                    TagNode child = (TagNode) cur_children.get(0);

                    assert (gap_map.get(child) != null);

                    trace_all_with_gap(child, i1, i2, j1, j2, 0, cur_id, rules,
                            rule_idx_set);
                } else {

                    assert (nChildren <= 2);

                    TagNode child1 = (TagNode) cur_children.get(0);
                    TagNode child2 = (TagNode) cur_children.get(1);

                    if (gap_map.get(child1) != null) {

                        // the foot node is in the subtree of child1
                        assert (gap_map.get(child2) == null);

                        if (gap_map.get(child1).get(i1) != null
                                && gap_map.get(child1).get(i1).get(i2) != null
                                && gap_map.get(child1).get(i1).get(i2)
                                        .get(j1) != null) {

                            Iterator<Integer> it = gap_map.get(child1).get(i1)
                                    .get(i2).get(j1).keySet().iterator();

                            while (it.hasNext()) {

                                int split_point = it.next().intValue();
                                if (split_point > j2)
                                    break;

                                double hyp = 1e300;

                                if (closed_map.get(child2)
                                        .get(split_point) != null
                                        && closed_map.get(child2)
                                                .get(split_point)
                                                .get(j2) != null)
                                    hyp = gap_map.get(child1).get(i1).get(i2)
                                            .get(j1).get(split_point)[0]
                                            + closed_map.get(child2)
                                                    .get(split_point)
                                                    .get(j2)[0];

                                if (hyp < 1e300) {

                                    Set<Integer> cur_rule_set = rule_idx_set;

                                    if (rule_set_used) {

                                        Set<Integer> new_rule_set = new HashSet<Integer>();

                                        for (Rule r : saved_rules) {

                                            rules.get(cur_id).add(new Rule(r));
                                            new_rule_set.add(
                                                    rules.get(cur_id).size()
                                                            - 1);
                                            rule_idx_set.add(
                                                    rules.get(cur_id).size()
                                                            - 1);
                                        }

                                        cur_rule_set = new_rule_set;
                                    }

                                    trace_all_with_gap(child1, i1, i2, j1,
                                            split_point, 0, cur_id, rules,
                                            cur_rule_set);
                                    trace_all_no_gap(child2, split_point, j2, 0,
                                            cur_id, rules, cur_rule_set);
				// System.err.println("Done trace all no gaps [8]");

                                    rule_set_used = true;
                                }
                            }
                        }
                    } else {

                        // the foot node is in the subtree of child2
                        assert (gap_map.get(child2) != null);

                        if (closed_map.get(child1).get(i1) == null){
                            System.err.println("Very STRANGE");
			    // Simon: added for debugging
			    return;
			}

                        Iterator<Integer> it = closed_map.get(child1).get(i1)
                                .keySet().iterator();

                        while (it.hasNext()) {

                            int split_point = it.next().intValue();

                            if (split_point > i2)
                                break;

                            double hyp = 1e300;

                            if (gap_map.get(child2).get(split_point) != null
                                    && gap_map.get(child2).get(split_point)
                                            .get(i2) != null
                                    && gap_map.get(child2).get(split_point)
                                            .get(i2).get(j1) != null
                                    && gap_map.get(child2).get(split_point)
                                            .get(i2).get(j1).get(j2) != null)
                                hyp = closed_map.get(child1).get(i1)
                                        .get(split_point)[0]
                                        + gap_map.get(child2).get(split_point)
                                                .get(i2).get(j1).get(j2)[0];

                            if (hyp < 1e300) {

                                Set<Integer> cur_rule_set = rule_idx_set;

                                if (rule_set_used) {

                                    Set<Integer> new_rule_set = new HashSet<Integer>();

                                    for (Rule r : saved_rules) {

                                        rules.get(cur_id).add(new Rule(r));
                                        new_rule_set.add(
                                                rules.get(cur_id).size() - 1);
                                        rule_idx_set.add(
                                                rules.get(cur_id).size() - 1);
                                    }

                                    cur_rule_set = new_rule_set;
                                }

                                trace_all_no_gap(child1, i1, split_point, 0,
                                        cur_id, rules, cur_rule_set);
				// System.err.println("Done trace all no gaps [0]");
                                trace_all_with_gap(child2, split_point, i2, j1,
                                        j2, 0, cur_id, rules, cur_rule_set);

                                rule_set_used = true;
                            }
                        }
                    }
                }
            } else {

                // leaf node

                // NOTE: we DON'T have to cover substitution here: after subst.
                // adjunction is no longer possible
            }
        } else {

            // adjunction included here

            // a) null-adjoin
	    // System.err.println("Null-adjoin[2]");
            // CROSS-CHECK: is this the correct check for mandatory adjunction?
            if (cur_node.getAdjStatus() != TagNode.MADJ) {
                // adjunction is not obligatory
                //double hyp_na = 0;
		double hyp_na = gap_map.get(cur_node).get(i1).get(i2).get(j1)
		      .get(j2)[1];
                if (hyp_na < 1e300) {

                    trace_all_with_gap(cur_node, i1, i2, j1, j2, 1, cur_id,
                            rules, rule_idx_set);

                    rule_set_used = true;
                }
            }

            // b) true adjoining
	    //System.err.println("True adjoining");
            // CROSS-CHECK: is this the correct check for forbidden adjunction?
            if (nChildren > 0 && !cur_node.isNoadj()) {

                // NOTE: one could also have a lookup for the respective
                // category here
                for (TagTree subst_tag_tree : auxiliary_trees.get(category)) {

                    // if (adjunction_possible(cur_node,subst_tag_tree)) {
                    if (true) {

                        TagNode subst_root = (TagNode) subst_tag_tree.getRoot();


			Iterator<Integer> it_i; 
                        if (gap_map.get(subst_root).get(i1) == null){
			    //System.err.println("Strange thing[6]");
                            //continue;
			    it_i= new LinkedList<Integer>().iterator();
			}
			else{
			    it_i = gap_map.get(subst_root).get(i1)
                                .keySet().iterator();
			}
                        while (it_i.hasNext()) {

                            int i_inter = it_i.next().intValue();
                            if (i_inter > i2){
                                break;
			    }

                            if (gap_map.get(cur_node).get(i_inter) == null
                                    || gap_map.get(cur_node).get(i_inter)
                                            .get(i2) == null
                                    || gap_map.get(cur_node).get(i_inter)
				.get(i2).get(j1) == null){
				//System.err.println("Strange thing[7]");
				continue;
			    }

                            Iterator<Integer> it_j = gap_map.get(subst_root)
                                    .get(i1).get(i_inter).keySet().iterator();

                            while (it_j.hasNext()) {

                                int j_inter = it_j.next().intValue();

                                if (j_inter > j2){
				    //System.err.println("Strange thing[10]");
                                    break;
				}

                                if (gap_map.get(subst_root).get(i1).get(i_inter)
				    .get(j_inter).get(j2) == null){
				    //System.err.println("Strange thing[8]");
                                    continue;
				}
                                // NOTE: every time we integrate a tree into
                                // another tree, we add the weight
                                // of the tree. This way the system with the
                                // passes is less complicated.
                                // In the end we have to add the weight of the
                                // initial tree

                                double hyp = 1e300;
                                if (gap_map.get(cur_node).get(i_inter).get(i2)
                                        .get(j1).get(j_inter) != null)
                                    hyp = gap_map.get(cur_node).get(i_inter)
                                            .get(i2).get(j1).get(j_inter)[1]
                                            + gap_map.get(subst_root).get(i1)
                                                    .get(i_inter).get(j_inter)
                                                    .get(j2)[0]
                                            + 1.0; // this is the tree weight

                                if (hyp < 1e300) {

                                    Set<Integer> cur_rule_set = rule_idx_set;

                                    if (rule_set_used) {

                                        Set<Integer> new_rule_set = new HashSet<Integer>();

                                        for (Rule r : saved_rules) {

                                            rules.get(cur_id).add(new Rule(r));
                                            new_rule_set.add(
                                                    rules.get(cur_id).size()
                                                            - 1);
                                            rule_idx_set.add(
                                                    rules.get(cur_id).size()
                                                            - 1);
                                        }

                                        cur_rule_set = new_rule_set;
                                    }

                                    // now we report the tree, and a node
                                    // address in a different tree. sounds a bit
                                    // strange, so CROSS-CHECK!
                                    Tidentifier tid = new Tidentifier(
                                            rules.size(),
                                            revGrammarDict.get(subst_tag_tree),
                                            subst_tag_tree.getId());
				    //System.out.println("Setting Node ID in SlimTAGParser [3]: "+adr_map.get(cur_node));
                                    tid.setNodeId(adr_map.get(cur_node));
                                    rules.put(tid, new LinkedList<Rule>());
                                    rules.get(tid).add(new Rule(tid));

                                    for (Integer r : cur_rule_set) {

                                        rules.get(cur_id).get(r.intValue())
                                                .getRhs().addOp(new TreeOp(tid,
                                                        PredComplexLabel.ADJ));
                                    }

                                    Set<Integer> sub_rule_set = new HashSet<Integer>();
                                    sub_rule_set.add(0);

                                    trace_all_with_gap(cur_node, i_inter, i2,
                                            j1, j_inter, 1, cur_id, rules,
                                            cur_rule_set);
                                    trace_all_with_gap(subst_root, i1, i_inter,
                                            j_inter, j2, 0, tid, rules,
                                            sub_rule_set);

                                    rule_set_used = true;
                                }
                            }
                        }
                    }
                }
            }

        }
    }


    private void add_if_not_in_queue(TagNode node, int i, int j, int pos) {

        if (closed_in_queue.get(node).get(i) == null) {
            closed_in_queue.get(node).put(i, new HashMap<Integer, boolean[]>());
        }
        if (closed_in_queue.get(node).get(i).get(j) == null) {
            closed_in_queue.get(node).get(i).put(j, new boolean[2]);
            closed_in_queue.get(node).get(i).get(j)[0] = false;
            closed_in_queue.get(node).get(i).get(j)[1] = false;
        }

        if (!closed_in_queue.get(node).get(i).get(j)[pos]) {

            agenda.get(j - i).offer(new TAGAgendaItem(node, i, -1, -1, j, pos));

            closed_in_queue.get(node).get(i).get(j)[pos] = true;

            String label = node.getCategory();

            // add foot items
            for (TagNode foot_node : foot_nodes_ij.get(label).get(i).get(j)) {

                if (gap_in_queue.get(foot_node).get(i) == null) {
                    gap_in_queue.get(foot_node).put(i,
                            new HashMap<Integer, Map<Integer, Map<Integer, boolean[]>>>());
                    gap_map.get(foot_node).put(i,
                            new HashMap<Integer, Map<Integer, Map<Integer, double[]>>>());
                }
                if (gap_in_queue.get(foot_node).get(i).get(i) == null) {
                    gap_in_queue.get(foot_node).get(i).put(i,
                            new HashMap<Integer, Map<Integer, boolean[]>>());
                    gap_map.get(foot_node).get(i).put(i,
                            new HashMap<Integer, Map<Integer, double[]>>());
                }
                if (gap_in_queue.get(foot_node).get(i).get(i).get(j) == null) {
                    gap_in_queue.get(foot_node).get(i).get(i).put(j,
                            new HashMap<Integer, boolean[]>());
                    gap_map.get(foot_node).get(i).get(i).put(j,
                            new HashMap<Integer, double[]>());
                }

                if (gap_in_queue.get(foot_node).get(i).get(i).get(j)
                        .get(j) == null) {

                    gap_in_queue.get(foot_node).get(i).get(i).get(j).put(j,
                            new boolean[2]);
                    gap_map.get(foot_node).get(i).get(i).get(j).put(j,
                            new double[2]);

                    gap_map.get(foot_node).get(i).get(i).get(j).get(j)[0] = 0.0;
                    gap_map.get(foot_node).get(i).get(i).get(j)
                            .get(j)[1] = 1e300;

                    add_if_not_in_queue(foot_node, i, i, j, j, 0);
                }
            }

        }
    }

    private void add_if_not_in_queue(TagNode node, int i1, int i2, int j1,
            int j2, int pos) {

        // System.err.println("+++++ evaluating for queue: span ["+i1 + "," + i2
        // + "][" + j1 + "," + j2 + "]");
        // System.err.println("+++ node " + node);

        if (gap_in_queue.get(node).get(i1) == null) {
            gap_in_queue.get(node).put(i1,
                    new HashMap<Integer, Map<Integer, Map<Integer, boolean[]>>>());
        }
        if (gap_in_queue.get(node).get(i1).get(i2) == null) {
            gap_in_queue.get(node).get(i1).put(i2,
                    new HashMap<Integer, Map<Integer, boolean[]>>());
        }
        if (gap_in_queue.get(node).get(i1).get(i2).get(j1) == null) {
            gap_in_queue.get(node).get(i1).get(i2).put(j1,
                    new HashMap<Integer, boolean[]>());
        }
        if (gap_in_queue.get(node).get(i1).get(i2).get(j1).get(j2) == null) {
            gap_in_queue.get(node).get(i1).get(i2).get(j1).put(j2,
                    new boolean[2]);
            gap_in_queue.get(node).get(i1).get(i2).get(j1).get(j2)[0] = false;
            gap_in_queue.get(node).get(i1).get(i2).get(j1).get(j2)[1] = false;
        }

        if (!gap_in_queue.get(node).get(i1).get(i2).get(j1).get(j2)[pos]) {

            agenda.get(j2 - i1)
                    .offer(new TAGAgendaItem(node, i1, i2, j1, j2, pos));

            gap_in_queue.get(node).get(i1).get(i2).get(j1).get(j2)[pos] = true;

            String label = node.getCategory();

            // add foot items
            for (TagNode foot_node : foot_nodes_ij.get(label).get(i1).get(j2)) {

                if (gap_in_queue.get(foot_node).get(i1) == null) {
                    gap_in_queue.get(foot_node).put(i1,
                            new HashMap<Integer, Map<Integer, Map<Integer, boolean[]>>>());
                    gap_map.get(foot_node).put(i1,
                            new HashMap<Integer, Map<Integer, Map<Integer, double[]>>>());
                }
                if (gap_in_queue.get(foot_node).get(i1).get(i1) == null) {
                    gap_in_queue.get(foot_node).get(i1).put(i1,
                            new HashMap<Integer, Map<Integer, boolean[]>>());
                    gap_map.get(foot_node).get(i1).put(i1,
                            new HashMap<Integer, Map<Integer, double[]>>());
                }
                if (gap_in_queue.get(foot_node).get(i1).get(i1)
                        .get(j2) == null) {
                    gap_in_queue.get(foot_node).get(i1).get(i1).put(j2,
                            new HashMap<Integer, boolean[]>());
                    gap_map.get(foot_node).get(i1).get(i1).put(j2,
                            new HashMap<Integer, double[]>());
                }

                if (gap_in_queue.get(foot_node).get(i1).get(i1).get(j2)
                        .get(j2) == null) {

                    gap_in_queue.get(foot_node).get(i1).get(i1).get(j2).put(j2,
                            new boolean[2]);
                    gap_map.get(foot_node).get(i1).get(i1).get(j2).put(j2,
                            new double[2]);

                    gap_map.get(foot_node).get(i1).get(i1).get(j2)
                            .get(j2)[0] = 0.0;
                    gap_map.get(foot_node).get(i1).get(i1).get(j2)
                            .get(j2)[1] = 1e300;

                    add_if_not_in_queue(foot_node, i1, i1, j2, j2, 0);
                }
            }
        }
    }

    /****************************************************************/
    public boolean build_chart_via_agenda(List<Word> tokens) {

        /***
         * This is a bottom-up parsing routine based on the deduction rules from
         *** [Kallmeyer, Parsing beyond context free grammars, Springer (2010),
         * pages 78-82].
         *** At the same time, this is an agenda-driven routine where impossible
         * items are never visited.
         ***/

	
        nTokens = tokens.size();

        long chartStartTime = System.nanoTime();

        Map<String, List<TagNode>> adjunction_nodes = new HashMap<String, List<TagNode>>();

        foot_nodes_ij = new HashMap<String, Vector<Vector<List<TagNode>>>>();
        Map<String, Vector<Vector<List<TagNode>>>> subst_nodes_ij = new HashMap<String, Vector<Vector<List<TagNode>>>>();

        Map<TagNode, TagNode> parent_map = new HashMap<TagNode, TagNode>();

        Map<TagNode, TagTree> tree_map = new HashMap<TagNode, TagTree>();

        Map<String, List<Integer>> word_positions = new HashMap<String, List<Integer>>();
        for (int k = 0; k < nTokens; k++) {

            String cur_token = tokens.get(k).getWord();

            if (word_positions.get(cur_token) == null)
                word_positions.put(cur_token, new LinkedList<Integer>());

            //System.err.println("inserting " + cur_token);

            word_positions.get(cur_token).add(k + 1);
        }

        // trace code: 0 = move, 1 = null-adjoin, 2 = subst/adjoin

        // each map is indexed as [start of span-1][end of span][adjunction
        // possible]
        // where for the last index a 0 means that adjunction is no longer
        // possible, a 1 that it is still possible
        closed_map = new HashMap<TagNode, Map<Integer, Map<Integer, double[]>>>();
        closed_in_queue = new HashMap<TagNode, Map<Integer, Map<Integer, boolean[]>>>();

        // map for nodes where adjunction is still possible
        // this implies that the node must be the ancestor of a foot node
        // each map is indexed as [start of span1-1][end of span1][start of
        // span2-1][end of span2][adjunction performed]
        gap_map = new HashMap<TagNode, Map<Integer, Map<Integer, Map<Integer, Map<Integer, double[]>>>>>();
        gap_in_queue = new HashMap<TagNode, Map<Integer, Map<Integer, Map<Integer, Map<Integer, boolean[]>>>>>();

        Iterator<String> its = grammarDict.keySet().iterator();
        while (its.hasNext()) {
            String cur_key = its.next();
            TagTree tag_tree = grammarDict.get(cur_key);

            // add entries to the above maps

            TagNode tree_root = (TagNode) tag_tree.getRoot();

            tree_map.put(tree_root, tag_tree);

            List<TagNode> tree_nodes = new LinkedList<TagNode>();
            tree_root.getAllNodesChildrenFirst(tree_nodes);

            for (TagNode cur_node : tree_nodes) {
                // we currently only implement binary trees

                List<Node> cur_children = cur_node.getChildren();

                String label = cur_node.getCategory();
                if (adjunction_nodes.get(label) == null)
                    adjunction_nodes.put(label, new LinkedList<TagNode>());
                if (foot_nodes_ij.get(label) == null)
                    foot_nodes_ij.put(label,
                            new Vector<Vector<List<TagNode>>>());
                if (subst_nodes_ij.get(label) == null)
                    subst_nodes_ij.put(label,
                            new Vector<Vector<List<TagNode>>>());

                if (cur_children != null) {

                    for (Node c : cur_children) {

                        parent_map.put((TagNode) c, cur_node);
                    }

                    if (cur_children.size() > 2) {
                        System.err.println(
                                "ERROR: elementary trees need to be binary (at most). Please transform your grammar.");
                        return false;
                    }

                    if (cur_children.size() == 0) {
                        // if (cur_node.getType() != TagNode.LEX &&
                        // cur_node.getType() != TagNode.FOOT)
                        // substitution_nodes.add(cur_node);
                    } else if (!cur_node.isNoadj()) {

                        adjunction_nodes.get(label).add(cur_node);
                    }

                    assert (cur_node.getChildren().size() <= 2);
                }
            }

            if (!tag_tree.hasFoot()) {

                for (TagNode cur_node : tree_nodes) {

                    closed_map.put(cur_node,
                            new HashMap<Integer, Map<Integer, double[]>>());
                    closed_in_queue.put(cur_node,
                            new HashMap<Integer, Map<Integer, boolean[]>>());
                }
            } else {

                TagNode tree_foot = (TagNode) tag_tree.getFoot();

                // we will use this to determine whether a node is an anchestor
                // of the foot node
                String foot_gorn = tree_foot.getAddress();

                for (TagNode cur_node : tree_nodes) {

                    // check if the node is above the foot node (or if it is the
                    // foot node itself)
                    String cur_gorn = cur_node.getAddress();
                    if (cur_gorn == "0")
                        cur_gorn = "";

                    boolean above_foot = false;

                    // WARNING: this only works if all nodes are of degree 9 or
                    // less
                    // (otherwise would have to tokenize the Gorn addresses by
                    // splitting at '.')
                    // however, due to the conversion in the constructor we
                    // always have binary trees here
                    if (cur_gorn.length() <= foot_gorn.length()) {

                        above_foot = true;
                        for (int k = 0; k < cur_gorn.length(); k++) {
                            if (cur_gorn.charAt(k) != foot_gorn.charAt(k)) {
                                above_foot = false;
                            }
                        }
                    }

                    // System.err.println("checking " + cur_node + ", above
                    // foot: " + above_foot + ", cur_gorn: " + cur_gorn
                    // + ", foot_gorn: " + foot_gorn);

                    if (above_foot) {

                        gap_map.put(cur_node,
                                new HashMap<Integer, Map<Integer, Map<Integer, Map<Integer, double[]>>>>());
                        gap_in_queue.put(cur_node,
                                new HashMap<Integer, Map<Integer, Map<Integer, Map<Integer, boolean[]>>>>());

                    } else {

                        closed_map.put(cur_node,
                                new HashMap<Integer, Map<Integer, double[]>>());
                        closed_in_queue.put(cur_node,
                                new HashMap<Integer, Map<Integer, boolean[]>>());
                    }
                }
            }
        }

        /******* Parsing ********/

        // NOTE: the algorithm is efficient if, whenever you assign a leaf a
        // span by substitution,
        // then when you reach the root node you have covered a _larger_ span.
        // If this assumption is not fulfilled, there can be more than two
        // passes.
        // If the grammar has nonsense rules like NP -> NP it could cycle
        // indefinitely, but
        // not if all trees have strictly positive cost.

        agenda = new Vector<Queue<TAGAgendaItem>>();

        for (int k = 0; k <= nTokens; k++) {
            agenda.add(new LinkedList<TAGAgendaItem>());

            Iterator<String> label_it;

            label_it = subst_nodes_ij.keySet().iterator();
            while (label_it.hasNext())
                subst_nodes_ij.get(label_it.next())
                        .add(new Vector<List<TagNode>>());

            label_it = foot_nodes_ij.keySet().iterator();
            while (label_it.hasNext())
                foot_nodes_ij.get(label_it.next())
                        .add(new Vector<List<TagNode>>());

            for (int k2 = 0; k2 <= nTokens; k2++) {
                if (k2 < k) {
                    label_it = subst_nodes_ij.keySet().iterator();
                    while (label_it.hasNext())
                        subst_nodes_ij.get(label_it.next()).get(k).add(null);

                    label_it = foot_nodes_ij.keySet().iterator();
                    while (label_it.hasNext())
                        foot_nodes_ij.get(label_it.next()).get(k).add(null);
                } else {
                    label_it = subst_nodes_ij.keySet().iterator();
                    while (label_it.hasNext())
                        subst_nodes_ij.get(label_it.next()).get(k)
                                .add(new LinkedList<TagNode>());

                    label_it = foot_nodes_ij.keySet().iterator();
                    while (label_it.hasNext())
                        foot_nodes_ij.get(label_it.next()).get(k)
                                .add(new LinkedList<TagNode>());
                }
            }
        }

        // a) base cases
        its = grammarDict.keySet().iterator();
        while (its.hasNext()) {

            String cur_key = its.next();
            TagTree tag_tree = grammarDict.get(cur_key);

            TagNode tree_root = (TagNode) tag_tree.getRoot();

	    //System.out.println("Tag tree: "+tree_root);
	    
            List<TagNode> tree_nodes = new LinkedList<TagNode>();
            tree_root.getAllNodesChildrenFirst(tree_nodes);

            String lex_gorn = null;
            Word lex_word = null;

            if (tag_tree.hasLex()) {
                // System.err.println("Get lexical anchor: "+((TagNode)
                // tag_tree.getLexAnc()).getAddress()+", "+((TagNode)
                // tag_tree.getLexAnc()).getWord());
                lex_gorn = ((TagNode) tag_tree.getLexAnc()).getAddress();
                lex_word = ((TagNode) tag_tree.getLexAnc()).getWord();
            }

            for (TagNode cur_node : tree_nodes) {
            //for (int tag_index=0; tag_index<tree_nodes.size(); tag_index++) {
	    //TagNode cur_node = tree_nodes.get(tag_index);
                if (cur_node.getType() == TagNode.LEX) {

                    // Lexcical base case
		    
                    // if the lex. node is not an anchor, getWord() will return
                    // null. That's not what we want
                    // Word word = cur_node.getWord();

                    //System.err.println("Node: "+cur_node);
                    //System.err.println("Gorn Address: "+lex_gorn);
                    // System.err.println("Word: "+cur_node.getWord());
                    // System.err.println("Category: "+cur_node.getCategory());

                    String word = cur_node.getCategory();
		    
		    // These are the features, they should be null in
		    // the cases of co-anchor or lex node
		    // System.out.println("Current label: "+cur_node.getLabel());

		    // Borrowed this from TreeSelector
		    // we try to do a bit more than before for co-anchors/lex nodes
		    // we look into the lexicon to find a real inflected form
		    // and get its features
		    // if not found, we just cary on like before (only a cat)

		    // only if the node was not anchored in a normal
		    // way (co-anchor or lex node)
		    // if(cur_node.getLabel()==null){
		    // 	//System.out.println("Current FS: "+cur_node);
		    // 	if (lm.containsKey(word)) {
		    // 	    List<MorphEntry> lme = lm.get(word);
		    // 	    for (int j = 0; j < lme.size(); j++) {
		    // 		// ToDo
		    // 		// Here we brutally hope there will be one entry, and only one lemmaref for this entry
		    // 		//System.out.println(lme.get(j));
		    // 		cur_node.setLabel(lme.get(j).getLemmarefs().get(0).getFeatures());
		    // 		//cur_node.setLabel(null);
		    // 		//System.out.println("FS set to : "+lme.get(j).getLemmarefs().get(0).getFeatures());
				
		    // 	    }
		    // 	}
		    // }


		    //System.err.println("New node: "+cur_node);
		    // This does not work, how can we update it?
		    // tree_nodes.set(tag_index,cur_node);
		    // tag_tree.updateNode(cur_node,lex_gorn);
		    // grammarDict.put(cur_key,tag_tree);
		    // revGrammarDict.put(tag_tree,cur_key);
		    // System.out.println(tag_tree.getRoot());


                    // a lexical node can never be above a foot node
                    assert (closed_map.get(cur_node) != null);

                    // adjunction is never possible in base cases

                    if (word == null || word.length() == 0) {
                        // epsilon scan

                        if (!tag_tree.hasLex()) {

                            for (int i = 0; i <= nTokens; i++) {
                                // closed_map.get(cur_node)[i][i][0] = 0.0;

                                closed_map.get(cur_node).put(i,
                                        new HashMap<Integer, double[]>());
                                closed_map.get(cur_node).get(i).put(i,
                                        new double[2]);
                                closed_map.get(cur_node).get(i).get(i)[0] = 0.0;
                                closed_map.get(cur_node).get(i)
                                        .get(i)[1] = 1e300;

                                add_if_not_in_queue(cur_node, i, i, 0);
                            }
                        } else {

                            // here, too, we assume that each node does not have
                            // more than 9 children.
                            // however, due to the conversion in the constructor
                            // we always have binary trees here
                            String cur_gorn = cur_node.getAddress();

                            // find first difference in the gorn addresses
                            int k;
                            for (k = 0; k < cur_gorn.length()
                                    && k < lex_gorn.length()
                                    && cur_gorn.charAt(k) == lex_gorn
                                            .charAt(k); k++)
                                ;

                            if (k >= cur_gorn.length()
                                    || k >= lex_gorn.length()) {
                                System.out.println(
                                        "STRANGE: either the lexical node or the epsilon node are not a leaf");
                            }

                            for (int i = 0; i <= nTokens; i++) {

                                boolean possible = false;

                                if (cur_gorn.charAt(k) < lex_gorn.charAt(k)) {
                                    // epsilon is left of the lexical node

                                    for (Integer wi : word_positions
                                            .get(lex_word.getWord())) {

                                        if (wi.intValue() > i) {
                                            possible = true;
                                            break;
                                        }
                                    }
                                } else {
                                    // epsilon is right of the lexical node

                                    for (Integer wi : word_positions
                                            .get(lex_word.getWord())) {

                                        if (wi.intValue() <= i) {
                                            possible = true;
                                            break;
                                        }
                                    }
                                }

                                if (possible) {

                                    closed_map.get(cur_node).put(i,
                                            new HashMap<Integer, double[]>());
                                    closed_map.get(cur_node).get(i).put(i,
                                            new double[2]);
                                    closed_map.get(cur_node).get(i)
                                            .get(i)[0] = 0.0;
                                    closed_map.get(cur_node).get(i)
                                            .get(i)[1] = 1e300;

                                    add_if_not_in_queue(cur_node, i, i, 0);
                                }
                            }

                        }
                    } else {
                        // true word match

                        // closed_map.get(cur_node)[i][i+1][0] = 0.0;
                        // System.err.println("word match for pos" + i);
                        // System.err.println("Matching word [1]: "+word+" of
                        // type "+word.getClass());

                        for (int i = 0; i < nTokens; i++) {
                            // System.err.println("Trying true word match [1]:
                            // "+tokens.get(i).getWord()+" of type
                            // "+word.getClass());
                            if (tokens.get(i).getWord().equals(word)) {
                                // System.out.println("Got word match [1]");
                                // System.out.println("word match for pos " + i + ", "+tokens.get(i).getWord()+", "+cur_node.getAddress());
                                // System.out.println("Node is: "+cur_key+", "+tag_tree.getOriginalId()+", "+tag_tree.getPosition()+", "+cur_node.isAncLex());
				// Simon: Before adding an initial item, we check if the tree
				// is supposed to be anchored here (in case of identical lexical items in the input)
				// this is for avoiding duplicates
				// This does not apply for co-anchors (hence the  || !cur_node.isAncLex())
				if(tag_tree.getPosition()==i+1 || !cur_node.isAncLex()){
				    closed_map.get(cur_node).put(i,
								 new HashMap<Integer, double[]>());
				    closed_map.get(cur_node).get(i).put(i + 1,
									new double[2]);
				    closed_map.get(cur_node).get(i)
                                        .get(i + 1)[0] = 0.0;
				    closed_map.get(cur_node).get(i)
                                        .get(i + 1)[1] = 1e300;
				    
				    add_if_not_in_queue(cur_node, i, i + 1, 0);
				}
                            }
                        }
                    }
                } else if (cur_node == tag_tree.getFoot()) {

                    assert (gap_map.get(cur_node) != null);

                    // Foot Predict

                    String cur_gorn = cur_node.getAddress();

                    String cur_label = cur_node.getCategory();

                    if (!tag_tree.hasLex()) {

                        for (int i1 = 0; i1 <= nTokens; i1++)
                            for (int i2 = i1; i2 <= nTokens; i2++)
                                foot_nodes_ij.get(cur_label).get(i1).get(i2)
                                        .add(cur_node);
                    } else {

                        // find first difference in the gorn addresses
                        int k;
                        for (k = 0; k < cur_gorn.length()
                                && k < lex_gorn.length() && cur_gorn
                                        .charAt(k) == lex_gorn.charAt(k); k++)
                            ;

                        if (k >= cur_gorn.length() || k >= lex_gorn.length()) {
                            System.out.println(
                                    "STRANGE: either the lexical node or the leaf node are not a leaf");
                        }

                        if (cur_gorn.charAt(k) < lex_gorn.charAt(k)) {
                            // leaf node is left of the lexical node

                            int i_max = -1;

                            for (Integer wi : word_positions
                                    .get(lex_word.getWord())) {

                                if (wi.intValue() > i_max) {
                                    i_max = wi.intValue();
                                }
                            }

                            for (int i1 = 0; i1 < i_max; i1++)
                                for (int i2 = i1; i2 < i_max; i2++)
                                    foot_nodes_ij.get(cur_label).get(i1).get(i2)
                                            .add(cur_node);

                        } else {
                            // leaf node is right of the lexical node

                            int i_min = nTokens + 1;

                            for (Integer wi : word_positions
                                    .get(lex_word.getWord())) {

                                if (wi.intValue() < i_min) {
                                    i_min = wi.intValue();
                                }
                            }

                            for (int i1 = i_min; i1 <= nTokens; i1++)
                                for (int i2 = i1; i2 <= nTokens; i2++)
                                    foot_nodes_ij.get(cur_label).get(i1).get(i2)
                                            .add(cur_node);

                        }
                    }

                    // foot predicts are no longer added from the start, they
                    // are now handled via the agenda
                    // for (int i=0; i <= nTokens; i++) {

                    // gap_map.get(cur_node).put(i,new HashMap<Integer,
                    // Map<Integer, Map<Integer, double[] > > >());
                    // gap_map.get(cur_node).get(i).put(i,new HashMap<Integer,
                    // Map<Integer, double[] > >());

                    // for (int j=i; j <= nTokens; j++) {
                    // //gap_map.get(cur_node)[i][i][j][j][0] = 0.0;

                    // gap_map.get(cur_node).get(i).get(i).put(j,new
                    // HashMap<Integer, double[] >());
                    // gap_map.get(cur_node).get(i).get(i).get(j).put(j,new
                    // double[2]);
                    // gap_map.get(cur_node).get(i).get(i).get(j).get(j)[0] =
                    // 0.0;
                    // gap_map.get(cur_node).get(i).get(i).get(j).get(j)[1] =
                    // 1e300;
                    // }
                    // }
                } else if (cur_node.getChildren() == null
                        || cur_node.getChildren().size() == 0) {
                    // substitution node

                    if (!tag_tree.hasLex()) {

                        for (int i1 = 0; i1 <= nTokens; i1++)
                            for (int i2 = i1; i2 <= nTokens; i2++)
                                subst_nodes_ij.get(cur_node.getCategory())
                                        .get(i1).get(i2).add(cur_node);
                    } else {

                        // here, too, we assume that each node does not have
                        // more than 9 children.
                        // however, due to the conversion in the constructor we
                        // always have binary trees here
                        String cur_gorn = cur_node.getAddress();

                        // find first difference in the gorn addresses
                        int k;
                        for (k = 0; k < cur_gorn.length()
                                && k < lex_gorn.length() && cur_gorn
                                        .charAt(k) == lex_gorn.charAt(k); k++)
                            ;

                        if (k >= cur_gorn.length() || k >= lex_gorn.length()) {
                            System.out.println(
                                    "STRANGE: either the lexical node or the leaf node are not a leaf");
                        }

                        if (cur_gorn.charAt(k) < lex_gorn.charAt(k)) {
                            // leaf node is left of the lexical node

                            int i_max = -1;

                            // System.err.println("lex word: " + lex_word);

                            for (Integer wi : word_positions
                                    .get(lex_word.getWord())) {

                                if (wi.intValue() > i_max) {
                                    i_max = wi.intValue();
                                }
                            }

                            for (int i1 = 0; i1 < i_max; i1++)
                                for (int i2 = i1; i2 < i_max; i2++)
                                    subst_nodes_ij.get(cur_node.getCategory())
                                            .get(i1).get(i2).add(cur_node);

                        } else {
                            // leaf node is right of the lexical node

                            int i_min = nTokens + 1;

                            for (Integer wi : word_positions
                                    .get(lex_word.getWord())) {

                                if (wi.intValue() < i_min) {
                                    i_min = wi.intValue();
                                }
                            }

                            for (int i1 = i_min; i1 <= nTokens; i1++)
                                for (int i2 = i1; i2 <= nTokens; i2++)
                                    subst_nodes_ij.get(cur_node.getCategory())
                                            .get(i1).get(i2).add(cur_node);
                        }
                    }
                }
            }
        }

        long chartInitTime = System.nanoTime() - chartStartTime;
        System.err.println("Total time for init & chart initialization: "
                + (chartInitTime) / (Math.pow(10, 9)) + " sec.");

        // b) proceed with the chart (derived cases)
        int active_list = 0;

        while (active_list <= nTokens) {

            while (active_list <= nTokens
                    && agenda.get(active_list).size() == 0) {
                active_list++;
            }
            if (active_list > nTokens)
                break;

            TAGAgendaItem cur_item = agenda.get(active_list).poll();
            TagNode cur_node = cur_item.node;
            List<Node> cur_children = cur_node.getChildren();
            int nChildren = (cur_children == null) ? 0 : cur_children.size();

            assert (nChildren <= 2);

            String category = cur_node.getCategory();

            // if (cur_item.i2 != -1) {
            // System.err.println("******* printing cur item: span
            // ["+cur_item.i1 + "," + cur_item.i2 + "][" + cur_item.j1 + "," +
            // cur_item.j2 + "]");
            // System.err.println("*** node " + cur_node);
            // }

            if (cur_item.i2 == -1) {
                // current node is NOT above a foot node

                int i = cur_item.i1;
                int j = cur_item.j2;
                int pos = cur_item.pos;

                closed_in_queue.get(cur_node).get(i).get(j)[pos] = false;

                Map<Integer, Map<Integer, double[]>> cur_map = closed_map
                        .get(cur_node);

                double base_score = cur_map.get(i).get(j)[pos];

                if (pos == 1) {

                    // null-adjoin
                    // CROSS-CHECK: is this the correct check for mandatory
                    // adjunction?
                    if (cur_node.getAdjStatus() != TagNode.MADJ) {

                        if (base_score < cur_map.get(i).get(j)[0]) {

                            cur_map.get(i).get(j)[0] = base_score;

                            add_if_not_in_queue(cur_node, i, j, 0);
                        }
                    }

                    // true adjoin
                    // CROSS-CHECK: is this the correct check for forbidden
                    // adjunction?
                    if (nChildren > 0 && !cur_node.isNoadj()) {

                        // NOTE: could keep track of the maximal span derived so
                        // far => reduce the spans of the loops

                        // we need only combine with items that have already
                        // been dequeued (all others will trigger a separate
                        // execution later)

                        int i_outer = i;
                        int j_outer = j;

                        // since i_outer and j_outer are really i and j, we
                        // don't have to check for existence here
                        double prev_score = cur_map.get(i_outer)
                                .get(j_outer)[0];
                        double best_score = prev_score;

                        for (TagTree subst_tag_tree : auxiliary_trees
                                .get(category)) {

                            // this is currently superfluous, we already ensured
                            // matching categories
                            // if
                            // (!adjunction_possible(cur_node,subst_tag_tree))
                            // continue;

                            TagNode subst_root = (TagNode) subst_tag_tree
                                    .getRoot();

                            if (gap_map.get(subst_root).get(i_outer) == null
                                    || gap_map.get(subst_root).get(i_outer)
                                            .get(i) == null
                                    || gap_map.get(subst_root).get(i_outer)
                                            .get(i).get(j) == null
                                    || gap_map.get(subst_root).get(i_outer)
                                            .get(i).get(j).get(j_outer) == null)
                                continue;

                            double hyp_score = base_score
                                    + gap_map.get(subst_root).get(i_outer)
                                            .get(i).get(j).get(j_outer)[0]
                                    + 1.0; // this is the tree weight

                            if (hyp_score < best_score) {

                                best_score = hyp_score;
                            }
                        }

                        if (best_score < prev_score) {

                            cur_map.get(i_outer).get(j_outer)[0] = best_score;

                            add_if_not_in_queue(cur_node, i_outer, j_outer, 0);
                        }
                    }
                } else {
                    // pos == 0

                    // a) moves
                    TagNode parent = parent_map.get(cur_node);

                    if (parent != null) {

                        List<Node> siblings = parent.getChildren();

                        if (siblings.size() == 1) {
                            // unary move

                            Map<Integer, Map<Integer, double[]>> par_map = closed_map
                                    .get(parent);

                            if (par_map.get(i) == null) {
                                par_map.put(i,
                                        new HashMap<Integer, double[]>());
                            }
                            if (par_map.get(i).get(j) == null) {
                                par_map.get(i).put(j, new double[2]);
                                par_map.get(i).get(j)[0] = 1e300;
                                par_map.get(i).get(j)[1] = 1e300;
                            }

                            if (base_score < par_map.get(i).get(j)[1]) {

                                par_map.get(i).get(j)[1] = base_score;

                                add_if_not_in_queue(parent, i, j, 1);
                            }
                        } else {
                            // binary move

                            if (siblings.get(0) == cur_node) {
                                // current node is left child

                                TagNode sibling = (TagNode) siblings.get(1);

                                if (closed_map.get(sibling) != null) {
                                    // sibling is not above a foot node

                                    Map<Integer, Map<Integer, double[]>> par_map = closed_map
                                            .get(parent);

                                    // we need only combine with items that have
                                    // already been dequeued (all others will
                                    // trigger a separate execution later)

                                    int limit = Math.min(nTokens,
                                            j + active_list);

                                    if (closed_map.get(sibling)
                                            .get(j) != null) {

                                        Iterator<Integer> it = closed_map
                                                .get(sibling).get(j).keySet()
                                                .iterator();

                                        while (it.hasNext()) {

                                            int j_outer = it.next().intValue();

                                            // this should be guaranteed:
                                            // if (j_outer < j)
                                            // continue;
                                            if (j_outer > limit)
                                                break;

                                            double hyp = base_score + closed_map
                                                    .get(sibling).get(j)
                                                    .get(j_outer)[0];

                                            if (par_map.get(i) == null)
                                                par_map.put(i,
                                                        new HashMap<Integer, double[]>());
                                            if (par_map.get(i)
                                                    .get(j_outer) == null) {
                                                par_map.get(i).put(j_outer,
                                                        new double[2]);
                                                par_map.get(i).get(
                                                        j_outer)[0] = 1e300;
                                                par_map.get(i).get(
                                                        j_outer)[1] = 1e300;
                                            }

                                            if (hyp < par_map.get(i)
                                                    .get(j_outer)[1]) {

                                                par_map.get(i)
                                                        .get(j_outer)[1] = hyp;

                                                add_if_not_in_queue(parent, i,
                                                        j_outer, 1);
                                            }
                                        }
                                    }
                                } else {
                                    // sibling is above a foot node

                                    // we need only combine with items that have
                                    // already been dequeued (all others will
                                    // trigger a separate execution later)

				    //System.err.println("Sibling above a foot node[1]");
				    
                                    int limit = Math.min(nTokens,
                                            j + active_list);

                                    if (gap_map.get(sibling).get(j) == null)
                                        continue;

                                    Map<Integer, Map<Integer, Map<Integer, Map<Integer, double[]>>>> par_map = gap_map
                                            .get(parent);

                                    Iterator<Integer> it_i = gap_map
                                            .get(sibling).get(j).keySet()
                                            .iterator();

                                    while (it_i.hasNext()) {

                                        int i_inner = it_i.next().intValue();

                                        if (i_inner > limit)
                                            break;

                                        Iterator<Integer> it_j = gap_map
                                                .get(sibling).get(j)
                                                .get(i_inner).keySet()
                                                .iterator();

                                        while (it_j.hasNext()) {

                                            int j_inner = it_j.next()
                                                    .intValue();

                                            if (j_inner > limit)
                                                break;

                                            Iterator<Integer> it_j_outer = gap_map
                                                    .get(sibling).get(j)
                                                    .get(i_inner).get(j_inner)
                                                    .keySet().iterator();

                                            while (it_j_outer.hasNext()) {

                                                int j_outer = it_j_outer.next()
                                                        .intValue();

                                                if (j_outer > limit)
                                                    break;

                                                double hyp = base_score
                                                        + gap_map.get(sibling)
                                                                .get(j)
                                                                .get(i_inner)
                                                                .get(j_inner)
                                                                .get(j_outer)[0];

                                                if (par_map.get(i) == null)
                                                    par_map.put(i,
                                                            new HashMap<Integer, Map<Integer, Map<Integer, double[]>>>());
                                                if (par_map.get(i)
                                                        .get(i_inner) == null)
                                                    par_map.get(i).put(i_inner,
                                                            new HashMap<Integer, Map<Integer, double[]>>());
                                                if (par_map.get(i).get(i_inner)
                                                        .get(j_inner) == null)
                                                    par_map.get(i).get(i_inner)
                                                            .put(j_inner,
                                                                    new HashMap<Integer, double[]>());
                                                if (par_map.get(i).get(i_inner)
                                                        .get(j_inner)
                                                        .get(j_outer) == null) {
                                                    par_map.get(i).get(i_inner)
                                                            .get(j_inner)
                                                            .put(j_outer,
                                                                    new double[2]);
                                                    par_map.get(i).get(i_inner)
                                                            .get(j_inner)
                                                            .get(j_outer)[0] = 1e300;
                                                    par_map.get(i).get(i_inner)
                                                            .get(j_inner)
                                                            .get(j_outer)[1] = 1e300;
                                                }

                                                if (hyp < par_map.get(i)
                                                        .get(i_inner)
                                                        .get(j_inner)
                                                        .get(j_outer)[1]) {

                                                    par_map.get(i).get(i_inner)
                                                            .get(j_inner)
                                                            .get(j_outer)[1] = hyp;

                                                    add_if_not_in_queue(parent,
                                                            i, i_inner, j_inner,
                                                            j_outer, 1);
                                                }
                                            }
                                        }
                                    }
                                }
                            } else {
                                // current node is right child

                                TagNode sibling = (TagNode) siblings.get(0);

                                if (closed_map.get(sibling) != null) {
                                    // sibling is not above a foot node

                                    Map<Integer, Map<Integer, double[]>> par_map = closed_map
                                            .get(parent);

                                    // we need only combine with items that have
                                    // already been dequeued (all others will
                                    // trigger a separate execution later)

                                    int limit = Math.max(0, i - active_list);

                                    Iterator<Integer> it = closed_map
                                            .get(sibling).keySet().iterator();

                                    while (it.hasNext()) {

                                        int i_outer = it.next().intValue();

                                        if (i_outer > i)
                                            break;

                                        if (i_outer < limit)
                                            continue;

                                        if (closed_map.get(sibling).get(i_outer)
                                                .get(i) == null)
                                            continue;

                                        double hyp = base_score
                                                + closed_map.get(sibling)
                                                        .get(i_outer).get(i)[0];

                                        if (par_map.get(i_outer) == null)
                                            par_map.put(i_outer,
                                                    new HashMap<Integer, double[]>());

                                        if (par_map.get(i_outer)
                                                .get(j) == null) {
                                            par_map.get(i_outer).put(j,
                                                    new double[2]);
                                            par_map.get(i_outer)
                                                    .get(j)[0] = 1e300;
                                            par_map.get(i_outer)
                                                    .get(j)[1] = 1e300;
                                        }

                                        if (hyp < par_map.get(i_outer)
                                                .get(j)[1]) {

                                            par_map.get(i_outer)
                                                    .get(j)[1] = hyp;

                                            add_if_not_in_queue(parent, i_outer,
                                                    j, 1);
                                        }
                                    }
                                } else {
                                    // sibling is above a foot node

				    //System.err.println("Sibling above a foot node, parent: "+parent+", sibling: "+sibling);
				    
                                    Map<Integer, Map<Integer, Map<Integer, Map<Integer, double[]>>>> par_map = gap_map
                                            .get(parent);
                                    Map<Integer, Map<Integer, Map<Integer, Map<Integer, double[]>>>> sib_map = gap_map
                                            .get(sibling);

                                    int limit = Math.max(0, i - active_list);

                                    Iterator<Integer> it_i_outer = sib_map
                                            .keySet().iterator();

                                    while (it_i_outer.hasNext()) {

                                        int i_outer = it_i_outer.next()
                                                .intValue();

                                        if (i_outer > i)
                                            break;
                                        if (i_outer < limit)
                                            continue;

                                        Iterator<Integer> it_i = sib_map
                                                .get(i_outer).keySet()
                                                .iterator();

                                        while (it_i.hasNext()) {
					    //System.err.println("Getting next i: "+it_i);
                                            int i_inner = it_i.next()
                                                    .intValue();

                                            if (i_inner > i)
                                                break;

					    // Simon: changed this for
					    // debugging: the keySet
					    // must be copied because
					    // it might be modified
					    // (ConcurrentModificationException)
                                            Iterator<Integer> it_j = new HashSet<Integer>( sib_map
                                                    .get(i_outer).get(i_inner)
											   .keySet()).iterator();


                                            while (it_j.hasNext()) {
						//System.err.println("Getting next j: "+it_j);
                                                int j_inner = it_j.next()
                                                        .intValue();
						//System.err.println("Got next");

                                                if (j_inner > i)
                                                    break;

                                                if (sib_map.get(i_outer)
                                                        .get(i_inner)
                                                        .get(j_inner)
                                                        .get(i) == null)
                                                    continue;

                                                double hyp = base_score
                                                        + sib_map.get(i_outer)
                                                                .get(i_inner)
                                                                .get(j_inner)
                                                                .get(i)[0];

                                                if (par_map
                                                        .get(i_outer) == null)
                                                    par_map.put(i_outer,
                                                            new HashMap<Integer, Map<Integer, Map<Integer, double[]>>>());
                                                if (par_map.get(i_outer)
                                                        .get(i_inner) == null)
                                                    par_map.get(i_outer).put(
                                                            i_inner,
                                                            new HashMap<Integer, Map<Integer, double[]>>());
                                                if (par_map.get(i_outer)
                                                        .get(i_inner)
                                                        .get(j_inner) == null)
                                                    par_map.get(i_outer)
                                                            .get(i_inner)
                                                            .put(j_inner,
                                                                    new HashMap<Integer, double[]>());
                                                if (par_map.get(i_outer)
                                                        .get(i_inner)
                                                        .get(j_inner)
                                                        .get(j) == null) {
                                                    par_map.get(i_outer)
                                                            .get(i_inner)
                                                            .get(j_inner)
                                                            .put(j, new double[2]);
                                                    par_map.get(i_outer)
                                                            .get(i_inner)
                                                            .get(j_inner)
                                                            .get(j)[0] = 1e300;
                                                    par_map.get(i_outer)
                                                            .get(i_inner)
                                                            .get(j_inner)
                                                            .get(j)[1] = 1e300;
                                                }

                                                if (hyp < par_map.get(i_outer)
                                                        .get(i_inner)
                                                        .get(j_inner)
                                                        .get(j)[1]) {
						    //System.err.println("Here[1]");
                                                    par_map.get(i_outer)
                                                            .get(i_inner)
                                                            .get(j_inner)
                                                            .get(j)[1] = hyp;
                                                    add_if_not_in_queue(parent,
                                                            i_outer, i_inner,
                                                            j_inner, j, 1);
                                                }
                                            }
                                        }
                                    }

                                }
                            }
                        }
                    } else {

                        // b) root node => substitution

                        TagTree tag_tree = tree_map.get(cur_node);

                        // System.err.println("tag tree:"+ tag_tree);

                        double hyp_score = base_score + 1.0; // add the tree
                                                             // weight

                        String label = cur_node.getCategory();

                        // for (TagNode subst_node : substitution_nodes) {
                        for (TagNode subst_node : subst_nodes_ij.get(label)
                                .get(i).get(j)) {

                            // System.err.println("subst node: "+ subst_node);

                            // this is currently superfluous, we already ensured
                            // matching categories
                            // if (substitution_possible(subst_node,tag_tree)) {
                            if (true) {

                                Map<Integer, Map<Integer, double[]>> subst_map = closed_map
                                        .get(subst_node);

                                if (subst_map.get(i) == null)
                                    subst_map.put(i,
                                            new HashMap<Integer, double[]>());
                                if (subst_map.get(i).get(j) == null) {
                                    subst_map.get(i).put(j, new double[2]);
                                    subst_map.get(i).get(j)[0] = 1e300;
                                    subst_map.get(i).get(j)[1] = 1e300;
                                }

                                if (hyp_score < subst_map.get(i).get(j)[0]) {

                                    subst_map.get(i).get(j)[0] = hyp_score;

                                    add_if_not_in_queue(subst_node, i, j, 0);
                                }
                            }
                        }

                    }
                }
            } else {
                // current node IS above a foot node

                int i1 = cur_item.i1;
                int i2 = cur_item.i2;
                int j1 = cur_item.j1;
                int j2 = cur_item.j2;
                int pos = cur_item.pos;

                Map<Integer, Map<Integer, Map<Integer, Map<Integer, double[]>>>> cur_map = gap_map
                        .get(cur_node);

                gap_in_queue.get(cur_node).get(i1).get(i2).get(j1)
                        .get(j2)[pos] = false;

                double base_score = cur_map.get(i1).get(i2).get(j1)
                        .get(j2)[pos];

                if (pos == 1) {

                    // null-adjoin
                    // CROSS-CHECK: is this the correct check for mandatory
                    // adjunction?
                    if (cur_node.getAdjStatus() != TagNode.MADJ) {

                        if (base_score < cur_map.get(i1).get(i2).get(j1)
                                .get(j2)[0]) {

                            cur_map.get(i1).get(i2).get(j1)
                                    .get(j2)[0] = base_score;

                            add_if_not_in_queue(cur_node, i1, i2, j1, j2, 0);
                        }
                    }

                    // true adjoin
                    // CROSS-CHECK: is this the correct check for forbidden
                    // adjunction?
                    if (nChildren > 0 && !cur_node.isNoadj()) {

                        int i_outer = i1;
                        int j_outer = j2;

                        double prev_score = cur_map.get(i_outer).get(i2).get(j1)
                                .get(j_outer)[0];
                        double best_score = prev_score;

                        for (TagTree subst_tag_tree : auxiliary_trees
                                .get(category)) {

                            // this is currently superfluous, we already ensured
                            // matching categories
                            // if
                            // (!adjunction_possible(cur_node,subst_tag_tree))
                            // continue;

                            // we need only combine with items that have already
                            // been dequeued (all others will trigger a separate
                            // execution later)

                            TagNode subst_root = (TagNode) subst_tag_tree
                                    .getRoot();

                            Map<Integer, Map<Integer, Map<Integer, Map<Integer, double[]>>>> adj_map = gap_map
                                    .get(subst_root);

                            if (adj_map.get(i_outer) == null
                                    || adj_map.get(i_outer).get(i1) == null
                                    || adj_map.get(i_outer).get(i1)
                                            .get(j2) == null
                                    || adj_map.get(i_outer).get(i1).get(j2)
                                            .get(j_outer) == null)
                                continue;

                            // since i_outer and j_outer are really i1 and j2,
                            // we need not check for existence here

                            double hyp_score = base_score + adj_map.get(i_outer)
                                    .get(i1).get(j2).get(j_outer)[0] + 1.0; // this
                                                                            // is
                                                                            // the
                                                                            // tree
                                                                            // weight

                            if (hyp_score < best_score) {

                                best_score = hyp_score;
                            }
                        }

                        if (best_score < prev_score) {

                            cur_map.get(i_outer).get(i2).get(j1)
                                    .get(j_outer)[0] = best_score;

                            add_if_not_in_queue(cur_node, i_outer, i2, j1,
                                    j_outer, 0);
                        }

                    }
                } else {
                    // pos == 0

                    TagNode parent = parent_map.get(cur_node);
                    if (parent != null) {

                        // a) moves

                        List<Node> siblings = parent.getChildren();

                        Map<Integer, Map<Integer, Map<Integer, Map<Integer, double[]>>>> par_map = gap_map
                                .get(parent);

                        if (siblings.size() == 1) {
                            // unary move

                            if (par_map.get(i1) == null)
                                par_map.put(i1,
                                        new HashMap<Integer, Map<Integer, Map<Integer, double[]>>>());
                            if (par_map.get(i1).get(i2) == null)
                                par_map.get(i1).put(i2,
                                        new HashMap<Integer, Map<Integer, double[]>>());
                            if (par_map.get(i1).get(i2).get(j1) == null)
                                par_map.get(i1).get(i2).put(j1,
                                        new HashMap<Integer, double[]>());
                            if (par_map.get(i1).get(i2).get(j1)
                                    .get(j2) == null) {
                                par_map.get(i1).get(i2).get(j1).put(j2,
                                        new double[2]);
                                par_map.get(i1).get(i2).get(j1)
                                        .get(j2)[0] = 1e300;
                                par_map.get(i1).get(i2).get(j1)
                                        .get(j2)[1] = 1e300;
                            }

                            if (base_score < par_map.get(i1).get(i2).get(j1)
                                    .get(j2)[1]) {

                                par_map.get(i1).get(i2).get(j1)
                                        .get(j2)[1] = base_score;

                                add_if_not_in_queue(parent, i1, i2, j1, j2, 1);
                            }
                        } else {
                            // binary moves

                            if (siblings.get(0) == cur_node) {
                                // current node is left child

                                TagNode sibling = (TagNode) siblings.get(1);

                                // we need only combine with items that have
                                // already been dequeued (all others will
                                // trigger a separate execution later)

                                int limit = Math.min(nTokens, j2 + active_list);

                                if (closed_map.get(sibling).get(j2) != null) {

                                    Iterator<Integer> it = closed_map
                                            .get(sibling).get(j2).keySet()
                                            .iterator();

                                    while (it.hasNext()) {

                                        int j_outer = it.next().intValue();

                                        if (j_outer > limit)
                                            break;

                                        double hyp = base_score + closed_map
                                                .get(sibling).get(j2)
                                                .get(j_outer)[0];

                                        if (par_map.get(i1) == null)
                                            par_map.put(i1,
                                                    new HashMap<Integer, Map<Integer, Map<Integer, double[]>>>());
                                        if (par_map.get(i1).get(i2) == null)
                                            par_map.get(i1).put(i2,
                                                    new HashMap<Integer, Map<Integer, double[]>>());
                                        if (par_map.get(i1).get(i2)
                                                .get(j1) == null)
                                            par_map.get(i1).get(i2).put(j1,
                                                    new HashMap<Integer, double[]>());
                                        if (par_map.get(i1).get(i2).get(j1)
                                                .get(j_outer) == null) {
                                            par_map.get(i1).get(i2).get(j1).put(
                                                    j_outer, new double[2]);
                                            par_map.get(i1).get(i2).get(j1)
                                                    .get(j_outer)[0] = 1e300;
                                            par_map.get(i1).get(i2).get(j1)
                                                    .get(j_outer)[1] = 1e300;
                                        }

                                        if (hyp < par_map.get(i1).get(i2)
                                                .get(j1).get(j_outer)[1]) {

                                            par_map.get(i1).get(i2).get(j1)
                                                    .get(j_outer)[1] = hyp;

                                            add_if_not_in_queue(parent, i1, i2,
                                                    j1, j_outer, 1);
                                        }
                                    }
                                }
                            } else {

                                // current node is right child

                                TagNode sibling = (TagNode) siblings.get(0);

                                // we need only combine with items that have
                                // already been dequeued (all others will
                                // trigger a separate execution later)

                                int limit = Math.max(0, i1 - active_list);

                                Iterator<Integer> it = closed_map.get(sibling)
                                        .keySet().iterator();

                                while (it.hasNext()) {

                                    int i_outer = it.next().intValue();

                                    if (i_outer > i1)
                                        break;

                                    if (closed_map.get(sibling).get(i_outer)
                                            .get(i1) == null)
                                        continue;

                                    double hyp = base_score
                                            + closed_map.get(sibling)
                                                    .get(i_outer).get(i1)[0];

                                    if (par_map.get(i_outer) == null)
                                        par_map.put(i_outer,
                                                new HashMap<Integer, Map<Integer, Map<Integer, double[]>>>());
                                    if (par_map.get(i_outer).get(i2) == null)
                                        par_map.get(i_outer).put(i2,
                                                new HashMap<Integer, Map<Integer, double[]>>());
                                    if (par_map.get(i_outer).get(i2)
                                            .get(j1) == null)
                                        par_map.get(i_outer).get(i2).put(j1,
                                                new HashMap<Integer, double[]>());
                                    if (par_map.get(i_outer).get(i2).get(j1)
                                            .get(j2) == null) {
                                        par_map.get(i_outer).get(i2).get(j1)
                                                .put(j2, new double[2]);
                                        par_map.get(i_outer).get(i2).get(j1)
                                                .get(j2)[0] = 1e300;
                                        par_map.get(i_outer).get(i2).get(j1)
                                                .get(j2)[1] = 1e300;
                                    }

                                    if (hyp < par_map.get(i_outer).get(i2)
                                            .get(j1).get(j2)[1]) {

                                        par_map.get(i_outer).get(i2).get(j1)
                                                .get(j2)[1] = hyp;

                                        add_if_not_in_queue(parent, i_outer, i2,
                                                j1, j2, 1);
                                    }
                                }

                            }
                        }
                    } else {

                        // adjunction

                        TagTree tag_tree = tree_map.get(cur_node);

                        // here we only rely on smaller spans => nothing to
                        // improve

                        for (TagNode adj_node : adjunction_nodes
                                .get(cur_node.getCategory())) {

                            // System.err.println("*************** checking
                            // adjunction of node " + cur_node + " to node " +
                            // adj_node );
                            // System.err.println("spans:
                            // ["+i1+","+i2+"]["+j1+","+j2+"]");

                            // this is currently superfluous, we already ensured
                            // matching categories
                            // if (adjunction_possible(adj_node,tag_tree)) {
                            if (true) {

                                if (closed_map.get(adj_node) != null) {

                                    if (closed_map.get(adj_node).get(i2) != null
                                            && closed_map.get(adj_node).get(i2)
                                                    .get(j1) != null) {

                                        double hyp = base_score
                                                + closed_map.get(adj_node)
                                                        .get(i2).get(j1)[1]
                                                + 1.0; // tree weight

                                        if (closed_map.get(adj_node)
                                                .get(i1) == null)
                                            closed_map.get(adj_node).put(i1,
                                                    new HashMap<Integer, double[]>());
                                        if (closed_map.get(adj_node).get(i1)
                                                .get(j2) == null) {
                                            closed_map.get(adj_node).get(i1)
                                                    .put(j2, new double[2]);
                                            closed_map.get(adj_node).get(i1)
                                                    .get(j2)[0] = 1e300;
                                            closed_map.get(adj_node).get(i1)
                                                    .get(j2)[1] = 1e300;
                                        }

                                        if (hyp < closed_map.get(adj_node)
                                                .get(i1).get(j2)[0]) {

                                            closed_map.get(adj_node).get(i1)
                                                    .get(j2)[0] = hyp;

                                            add_if_not_in_queue(adj_node, i1,
                                                    j2, 0);
                                        }
                                    }

                                } else {

                                    Map<Integer, Map<Integer, Map<Integer, Map<Integer, double[]>>>> adj_map = gap_map
                                            .get(adj_node);

                                    if (adj_map.get(i2) == null)
                                        continue;

                                    Iterator<Integer> it_i = adj_map.get(i2)
                                            .keySet().iterator();

                                    while (it_i.hasNext()) {

                                        int i_inner = it_i.next().intValue();

                                        if (i_inner > j1)
                                            break;

                                        Iterator<Integer> it_j = adj_map.get(i2)
                                                .get(i_inner).keySet()
                                                .iterator();

                                        while (it_j.hasNext()) {

                                            int j_inner = it_j.next()
                                                    .intValue();

                                            if (j_inner > j1)
                                                break;

                                            if (adj_map.get(i2).get(i_inner)
                                                    .get(j_inner)
                                                    .get(j1) == null)
                                                continue;

                                            double hyp = base_score + adj_map
                                                    .get(i2).get(i_inner)
                                                    .get(j_inner).get(j1)[1]
                                                    + 1.0; // tree weight

                                            if (adj_map.get(i1) == null)
                                                adj_map.put(i1,
                                                        new HashMap<Integer, Map<Integer, Map<Integer, double[]>>>());
                                            if (adj_map.get(i1)
                                                    .get(i_inner) == null)
                                                adj_map.get(i1).put(i_inner,
                                                        new HashMap<Integer, Map<Integer, double[]>>());
                                            if (adj_map.get(i1).get(i_inner)
                                                    .get(j_inner) == null)
                                                adj_map.get(i1).get(i_inner)
                                                        .put(j_inner,
                                                                new HashMap<Integer, double[]>());
                                            if (adj_map.get(i1).get(i_inner)
                                                    .get(j_inner)
                                                    .get(j2) == null) {
                                                adj_map.get(i1).get(i_inner)
                                                        .get(j_inner)
                                                        .put(j2, new double[2]);
                                                adj_map.get(i1).get(i_inner)
                                                        .get(j_inner)
                                                        .get(j2)[0] = 1e300;
                                                adj_map.get(i1).get(i_inner)
                                                        .get(j_inner)
                                                        .get(j2)[1] = 1e300;
                                            }

                                            if (hyp < adj_map.get(i1)
                                                    .get(i_inner).get(j_inner)
                                                    .get(j2)[0]) {

                                                adj_map.get(i1).get(i_inner)
                                                        .get(j_inner)
                                                        .get(j2)[0] = hyp;

                                                add_if_not_in_queue(adj_node,
                                                        i1, i_inner, j_inner,
                                                        j2, 0);
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
        System.err.println("Total time for init & chart construction: "
                + (chartTime) / (Math.pow(10, 9)) + " sec.");

	
        return true;
    }

    /********************************************************/
    public List<Tidentifier> extract_all(Map<Tidentifier, List<Rule>> rules,
            String axiom) {

        // extract best parse (if any)
        rules.clear();

        List<Tidentifier> root_trees = new LinkedList<Tidentifier>();

        // DEBUG
        // Iterator<String> its = grammarDict.keySet().iterator();
        // while (its.hasNext()) {
        // String cur_key = its.next();
        // TagTree tag_tree = grammarDict.get(cur_key);

        // TagNode tree_root = (TagNode) tag_tree.getRoot();

        // System.err.println("############ scores for closed and gap nodes of
        // tree " + tag_tree.getId() + " (" + tag_tree.getOriginalId() + ")"
        // + "\n" + tag_tree.getRoot().toString() );

        // List<TagNode> tree_nodes = new LinkedList<TagNode>();
        // tree_root.getAllNodesChildrenFirst(tree_nodes);

        // for (TagNode node : tree_nodes) {

        // System.err.println("entries for node " + node);

        // if (closed_map.get(node) != null) {

        // Iterator<Integer> it1 = closed_map.get(node).keySet().iterator();

        // while (it1.hasNext()) {

        // int i = it1.next().intValue();

        // Iterator<Integer> it2 =
        // closed_map.get(node).get(i).keySet().iterator();

        // while (it2.hasNext()) {

        // int j = it2.next().intValue();

        // System.err.println("["+i+","+j+"]: " +
        // closed_map.get(node).get(i).get(j)[0] + ","
        // + closed_map.get(node).get(i).get(j)[1] + "");
        // }
        // }
        // }
        // else {

        // Iterator<Integer> it_i1 = gap_map.get(node).keySet().iterator();

        // while (it_i1.hasNext()) {

        // int i1 = it_i1.next().intValue();

        // Iterator<Integer> it_i2 =
        // gap_map.get(node).get(i1).keySet().iterator();

        // while (it_i2.hasNext()) {

        // int i2 = it_i2.next().intValue();

        // Iterator<Integer> it_j1 =
        // gap_map.get(node).get(i1).get(i2).keySet().iterator();

        // while (it_j1.hasNext()) {

        // int j1 = it_j1.next().intValue();

        // Iterator<Integer> it_j2 =
        // gap_map.get(node).get(i1).get(i2).get(j1).keySet().iterator();

        // while (it_j2.hasNext()) {

        // int j2 = it_j2.next().intValue();

        // System.err.println("["+i1+","+i2+"]["+j1+","+j2+"]: " +
        // gap_map.get(node).get(i1).get(i2).get(j1).get(j2)[0] + "," +
        // gap_map.get(node).get(i1).get(i2).get(j1).get(j2)[1]);
        // }
        // }
        // }
        // }
        // }
        // }

        // }
        // END_DEBUG

        if (initial_trees.get(axiom) == null)
            return root_trees;

        for (TagTree tag_tree : initial_trees.get(axiom)) {
	    
            TagNode tree_root = (TagNode) tag_tree.getRoot();

            // System.err.println("tree root category: \"" +
            // tree_root.getCategory() + "\", looking for \"" + axiom + "\".");

            if (tree_root.getCategory().equals(axiom)) {
			       
                double score = 1e300;

                if (closed_map.get(tree_root).get(0) != null && closed_map
                        .get(tree_root).get(0).get(nTokens) != null)
                    score = closed_map.get(tree_root).get(0).get(nTokens)[0]
                            + 1.0; // this is the tree weight

                // System.err.println("score: " + score);

                if (score < 1e300) {

                    Tidentifier tree_id = new Tidentifier(0,
                            revGrammarDict.get(tag_tree), tag_tree.getId());

                    root_trees.add(tree_id);
		    
                    rules.put(tree_id, new LinkedList<Rule>());
                    rules.get(tree_id).add(new Rule(tree_id));

                    Set<Integer> rule_idx_set = new HashSet<Integer>();
                    rule_idx_set.add(new Integer(0));

		    TagNode rootNode = (TagNode) tag_tree.getRoot();
		    adr_map.put(rootNode,rootNode.getAddress());
                    this.trace_all_no_gap(rootNode, 0,
                            nTokens, 0, tree_id, rules, rule_idx_set);
		    // System.err.println("Done trace all no gaps [1]");
                }
            }
        }
        return root_trees;
    }

};
