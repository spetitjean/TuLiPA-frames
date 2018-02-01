/*
 *  File ExtractTreeForest.java
 *
 *  Authors:
 *     Yannick Parmentier  <parmenti@loria.fr>
 *     
 *  Copyright:
 *     Yannick Parmentier, 2009
 *
 *  Last modified:
 *     Wed Jan 21 09:52:13 CET 2009
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
package de.tuebingen.parserconstraints;

import java.util.*;

import de.tuebingen.forest.Combination;
import de.tuebingen.forest.Rule;
import de.tuebingen.forest.Tidentifier;
import de.tuebingen.forest.TreeOp;
import de.tuebingen.parser.ClauseKey;
import de.tuebingen.parser.ForestExtractor;
import de.tuebingen.parser.ForestExtractorInitializer;
import de.tuebingen.rcg.PredComplexLabel;
import de.tuebingen.rcg.RCG;
import de.tuebingen.util.Pair;

public class ExtractTreeForest extends ForestExtractor
{

	private List<Integer> answers;
	private Map<Integer, List<int[]>> forest;
	private Map<Integer, List<ClauseKey>> links;
	private Map<Tidentifier, List<Rule>> tree_forest;
	private RCG grammar;
	private List<Tidentifier> start;
	private Map<Integer, TreeOp> done;
	private boolean verbose;
	
	private int indent;

	public ExtractTreeForest(boolean v, RCG g, List<Integer> li, Map<Integer, List<int[]>> and_or, Map<Integer, List<ClauseKey>> l)
	{
		verbose = v;
		grammar = g;
		answers = li;
		forest = and_or;
		links = l;
		tree_forest = new HashMap<Tidentifier, List<Rule>>();
		start = new LinkedList<Tidentifier>();
		done = new HashMap<Integer, TreeOp>();
		indent = 0;
	}

	public ExtractTreeForest()
	{
	}

	@SuppressWarnings("unchecked")
	@Override
	public void init(boolean verbose, RCG rcgg, ForestExtractorInitializer fi)
	{
		this.verbose = verbose;
		this.grammar = rcgg;
		this.answers = (List<Integer>) fi.poll();
		this.forest = (Map<Integer, List<int[]>>) fi.poll();
		this.links = (Map<Integer, List<ClauseKey>>) fi.poll();
		tree_forest = new HashMap<Tidentifier, List<Rule>>();
		start = new LinkedList<Tidentifier>();
		done = new HashMap<Integer, TreeOp>();
	}

	public Map<Integer, List<ClauseKey>> preprocessBindings(Map<Integer, List<ClauseKey>> l)
	{
		Map<Integer, List<ClauseKey>> l2 = new HashMap<Integer, List<ClauseKey>>();

		return l2;
	}

	public void extract()
	{
		for (int h : answers)
		{
			// h is a root of the derivation forest
			if (verbose)
				System.err.println("# Processing answer hash " + h);
			this.processHash(h, null);
		}
	}

	public TreeOp processHash(int hash, Pair opInfo)
	{
		TreeOp res = null;
		if (!done.containsKey(hash))
		{
			if (verbose)
			{
				indent++;
				System.err.println(produceSpaces(indent) + "H call " + hash);
			}
			TreeOp htop = null;
			TreeOp top = null;
			for (int i = 0; i < links.get(hash).size(); i++)
			{
				TreeOp op = this.processClause(hash, i, opInfo);
				if (top != null)
				{
					// System.err.println("***** top: " + top.getId() + " - op: " + op.getId());
					// we only add a *new* alternative (if there is only binding ambiguity, we do not overload the forest)
					//if (top.getId().getClauseId() != op.getId().getClauseId() || !(top.getId().getNodeId().equals(op.getId().getNodeId())) || top.getType() != op.getType()) {
					if (!top.getId().equals(op.getId())) {
						top.setOr(op);
					}
				}
				top = op;
				if (i == 0) // we keep a pointer to the 1st TreeOp structure!
					htop = top;
			}
			if (verbose)
				System.err.println(produceSpaces(indent) + "H store " + hash + " content: " + htop);
			done.put(hash, htop);
			res = htop;
		} 
		else
		{
			if (verbose)
			{
				indent++;
				System.err.println(produceSpaces(indent) + "H duplicate: " + hash);
			}
			res = done.get(hash);
		}
		if (verbose)
		{
			System.err.println(produceSpaces(indent) + "H exit " + hash + " op: " + res);
			indent--;
		}
		return res;
	}

	public TreeOp processClause(Integer hash, int pos, Pair opInfo)
	{
		TreeOp top = null;
		if (verbose)
		{
			indent++;
			System.err.println(produceSpaces(indent) + "C call " + hash + " pos: " + pos);
		}
		ClauseKey ck = links.get(hash).get(pos);
		PredComplexLabel lab = (PredComplexLabel) grammar.getClause(ck.getCindex()).getLhs().getLabel();
		if (verbose)
			System.err.println(produceSpaces(indent) + "C clause " +hash + " " + ck.toString(grammar));
		int type = lab.getType();
		switch (type)
		{
			case PredComplexLabel.TREE:
				top = this.processTreeClause(hash, pos, opInfo);
				break;
			case PredComplexLabel.ADJ:
				top = this.processBranchingClause(hash, pos);
				break;
			case PredComplexLabel.SUB:
				top = this.processBranchingClause(hash, pos);
				break;
			case PredComplexLabel.START:
				int[] f = forest.get(hash).get(pos);
				top = this.processHash(f[0], null);
				// f[0] for the rhs of a clause whose lhs is the start pred
				// always has a single predicate
				if (verbose)
					System.err.println(produceSpaces(indent) + "C add start " + hash + " " + top.toString());
				start.add(top.getId());
				TreeOp ttop = top.getOr();
				boolean go_on = (ttop != null);
				while (go_on)
				{
					start.add(ttop.getId());
					ttop = ttop.getOr();
					go_on = (ttop != null);
				}
				break;
			default: // skip
		}
		if (verbose)
		{
			System.err.println(produceSpaces(indent) + "C exit " + hash + " op: " + top);
			indent--;
		}
		return top;
	}

	public TreeOp processTreeClause(int hash, int pos, Pair opInfo)
	{
		if (verbose)
		{
			indent++;
			System.err.println(produceSpaces(indent) + "T call " + hash + " pos: " + pos);
		}
		// We get the tree information (thanks to the RCG clause)
		ClauseKey ck = links.get(hash).get(pos);
		PredComplexLabel lab = (PredComplexLabel) grammar.getClause(ck.getCindex()).getLhs().getLabel();
		String treename = lab.getTreeid();
		int num = ck.getCindex();
		if (verbose)
			System.err.println(produceSpaces(indent) + "T tree " + hash + " "+ ck.toString());

		// We initialize the corresponding entry in the tree forest
		Tidentifier id = new Tidentifier(num, treename, ck.getArgs());
		List<Rule> rules = tree_forest.get(id);
		if (!tree_forest.containsKey(id))
		{
			rules = new LinkedList<Rule>();
			tree_forest.put(id, rules);
		}

		TreeOp op = new TreeOp(id);

		if (opInfo != null)
		{ // if the caller is a branching clause
			op.setType((Integer) opInfo.getKey());
			//System.out.println("Setting Node ID in ExtractTreeForest: "+(String) opInfo.getValue());
			op.getId().setNodeId((String) opInfo.getValue());
		}

		// We get the RHS from the rcg forest
		int[] tab = forest.get(hash).get(pos);
		Rule rule = new Rule(id);
		Combination comb = processAlternative(tab);
		rule.setRhs(comb);
		if (!rules.contains(rule))
			rules.add(rule);
		if (verbose)
		{
			System.err.println(produceSpaces(indent) + "T exit " + hash + " op: " + op);
			indent--;
		}
		return op;
	}

	public Combination processAlternative(int[] tab)
	{
		if (verbose)
		{
			indent++;
			System.err.print(produceSpaces(indent) + "A call: ");
		    for (int i : tab) System.err.print(i + " ");
		    System.err.println();
		}
		Combination combi = new Combination();
		// Combination to be fed with the TreeOp of the RHS
		for (int i = 0; i < tab.length; i++)
		{
			Integer hash = tab[i];
			List<int[]> rhs = forest.get(hash);
			if (rhs != null && rhs.size() > 0 && rhs.get(0).length > 0)
			{
				// the empty-RHS branching rules are removed
				// (i.e. they are not considered) ; Example: <adj,0,t,{}>(Eps,Eps) -> Eps
				TreeOp top = this.processHash(hash, null);
				if (verbose) System.err.println(produceSpaces(indent) + "A add " + hash + " to combi: " + top.toString());
				//BUGFIX: need to keep different operations with similar IDs separate
				combi.addOp(new TreeOp(top));
			}
		}
		if (verbose)
		{
			System.err.println(produceSpaces(indent) + "A exit: " + combi);
			indent--;
		}
		return combi;
	}

	public TreeOp processBranchingClause(Integer hash, int pos)
	{
		// We get the branching info
		ClauseKey ck = links.get(hash).get(pos);
		PredComplexLabel lab = (PredComplexLabel) grammar.getClause(ck.getCindex()).getLhs().getLabel();
		int type = lab.getType();
		String node = lab.getNodeid();
		Pair opInfo = new Pair(type, node);
		if (verbose)
		{
			indent++;
			System.err.println(produceSpaces(indent) + "B branch " + hash + " key: " + ck.toString() + " type: " + type + " node: " + node);
		}

		int[] tab = forest.get(hash).get(pos);
		TreeOp op = this.processHash(tab[0], opInfo); // tab[0] for branching clause only have a single RHS pred

		if (verbose)
		{
			System.err.println(produceSpaces(indent) + "B exit " + hash + " op: " + op);
			indent--;
		}
		//BUGFIX: hand on correct operation information
		op.setType(type);
		//System.out.println("Setting Node ID in ExtractTreeForest: "+node);
		op.getId().setNodeId(node);
		return op;
	}

	public boolean isVerbose()
	{
		return verbose;
	}

	public void setVerbose(boolean verbose)
	{
		this.verbose = verbose;
	}

	public List<Tidentifier> getStart()
	{
		return start;
	}

	public Map<Tidentifier, List<Rule>> getForest()
	{
		return tree_forest;
	}

	public String printForest()
	{
		String res = "";
		res += "\n\nForest: \n(start ids: ";
		for (Tidentifier t : start)
		{
			res += t.getClauseId() + " ";
		}
		res += ")\n";
		Set<Tidentifier> keys = tree_forest.keySet();
		Iterator<Tidentifier> it = keys.iterator();
		while (it.hasNext())
		{
			Tidentifier tid = it.next();
			res += tid.toString() + "\t";
			List<Rule> l = tree_forest.get(tid);
			for (int i = 0; i < l.size(); i++)
			{
				if (tid.getClauseId() < 10)
					res += "0" + tid.getClauseId() + " ";
				else
					res += tid.getClauseId() + " ";
				res += "--> ";
				res += l.get(i).toString();
			}
		}
		return res;
	}

	public static String viewTab(int[] tab)
	{
		String res = "";
		for (int i : tab)
		{
			res += i;
			res += " ";
		}
		return res;
	}
	
    public String produceSpaces(int number)
    {
    	String spaces = "";
    	for (int i = 0; i < number; i++)
    	{
    		spaces += "  ";
    	}
    	return spaces;
    }

}
