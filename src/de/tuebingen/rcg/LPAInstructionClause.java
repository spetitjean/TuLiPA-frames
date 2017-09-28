package de.tuebingen.rcg;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import de.tuebingen.tag.TagTree;

public class LPAInstructionClause extends Clause
{
	//additional information that comes with the RCG grammars enhanced by LPA metavariables
	
	//remove one specific tree from LHS LPA and add it to RHS LPA
	public static final int REMOVE_OPERATION = -1;
	//do not make changes to the LPAs
	public static final int NO_OPERATION = 0;
	//add all trees in LHS LPA to one specific RHS LPA
	public static final int TRANSFER_OPERATION = 1;
	//LHS LPA must be empty
	public static final int EMPTY_LPA = 2;
	
	//has one of the operation type values defined above
	public int operationType;
	
	//used for remove operation
	public String removedTree = "";
	//used for transfer operation
	public int targetRHSPred = -1;
	
	public LPAInstructionClause() 
	{
		super(null, new ArrayList<Predicate>());
		removedTree = "";
		targetRHSPred = -1;
	}
	
	public LPAInstructionClause(Predicate p, List<Predicate> lp) 
	{
		super(p,lp,-1);
		removedTree = "";
		targetRHSPred = -1;
	}
	
	public LPAInstructionClause(Predicate p, List<Predicate> lp, float prob) 
	{
		setLhs(p);
		setRhs(lp);
		this.operationType = NO_OPERATION;
		this.removedTree = "";
		this.targetRHSPred = -1;
	}
	
	// for pretty printing
	public String toString(Map<String, TagTree> dict) 
	{
		String res = super.toString(dict);
		String opStr = "";
		switch (operationType)
		{
			case REMOVE_OPERATION: opStr = "rem"; break;
			case TRANSFER_OPERATION: opStr = "trans"; break;
			case EMPTY_LPA: opStr = "empty"; break;
		}
		TagTree tree = dict.get(removedTree);
		String detailStr = "";
		if (tree != null) detailStr = tree.getOriginalId();
		if (targetRHSPred != -1) detailStr += targetRHSPred;
		res += " " + opStr + " " + detailStr;
		return  res;
	}
}
