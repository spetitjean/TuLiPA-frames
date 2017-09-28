/*
 *  File Encode.java
 *
 *  Authors:
 *     Yannick Parmentier <parmenti@sfs.uni-tuebingen.de>
 *     Wolfgang Maier <wo.maier@uni-tuebingen.de>
 *     
 *  Copyright:
 *     Yannick Parmentier, 2007
 *     Wolfgang Maier, 2007
 *
 *  Last modified:
 *     Di 16. Okt 10:01:19 CEST 2007
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
package de.tuebingen.parser;

import java.util.*;

import de.tuebingen.rcg.ArgContent;
import de.tuebingen.rcg.Argument;
import de.tuebingen.rcg.RCGInstantiationException;

/**
 * 
 * @author parmenti, wmaier
 *
 * Class performing encoding / decoding of bindings 
 * in terms of a CSP (solved by GecodeJ)
 */
public class Encode {

	private List<Argument>    source;   // the input string
	private List<Argument>      dest;	// the arguments to instantiate
	private Map<String, Integer> cat;   // the category constraints (for range bindings)
	
	public Encode(List<Argument> la1, List<Argument> la2, Map<String, Integer> cats){
		source = la1;
		dest   = la2;
		cat    = cats;
	}
	
	public List<Binding> buildAllBindings(boolean verbose) throws RCGInstantiationException {
		// combines the bindings for each argument of the of the input
		// list with those of the list of arguments to match
		List<Binding> res = new LinkedList<Binding>();
		
		if (source.size() != dest.size()){
			throw new RCGInstantiationException("Can't find bindings for argument lists of different size: " + dest.toString() + "," + source.toString());
		} else {
			// for all arguments we merge the bindings
			for (int i = 0 ; i < source.size() ; i++) {
				if (verbose) {System.err.println("Computing "+source.get(i).toString()+" <=> "+dest.get(i).toString()+ " (" + (i+1) + "/" +source.size() + ")");}
				List<Binding> lb = buildBindings(verbose, source.get(i), dest.get(i));
				if (lb.size() == 0) { // if there is an argument for which we have no bindings, we raise an error
					throw new RCGInstantiationException("Can't find bindings for arguments: " + dest.toString() + "," + source.toString());
				}				
				res = Binding.listMerge(res, lb);
			}
		}	
		//System.err.println(res.toString());
		return res;
	}
	
	public List<Binding> buildBindings(boolean verbose, Argument inArg, Argument toMatchArg) {
		
		LinkedList<Binding> lblist = new LinkedList<Binding>();
		
		// We need to unfold inArg if it is of type list!
		int realCol = 0;
		List<ArgContent> realArgs = new LinkedList<ArgContent>();
		
		for(int i = 0 ; i < inArg.size() ; i++) {
			ArgContent ac = inArg.get(i);
			if (ac.getType() == ArgContent.LIST) {
				realCol += ac.getList().size();
				realArgs.addAll(ac.getList());
			} else {
				realCol++;
				realArgs.add(ac);
			}
		} 
		
		//System.err.println("   -> "+ realArgs.toString());
		
		// We then compute the bindings
		// NB: we must check that a word do not occur several times in the real arguments 
		//     if this is the case, we must build one constraints matrix for each of these occurrences
		
		Map<String, Integer> occurences = new Hashtable<String, Integer>();
		List<int[][]> csts = new LinkedList<int[][]>();
		boolean preBindingsNeeded = false;
		int maxLength = 0;

		// before computing the constraints matrix, we check the value of the 1st args
		// if they mismatch, there is no need to compute bindings (there will not be any solution)
		ArgContent acTM0 = toMatchArg.get(0);
		ArgContent real0 = realArgs.get(0);
		if (acTM0.getType() == ArgContent.TERM && real0.getType() == ArgContent.TERM 
			&& !(real0.getName().equals(acTM0.getName()))) {
			// we return the empty list of bindings (i.e. no solution)
			return lblist;
		}
		
		// 1st, we build the constraints matrix
		// it contains at least the basic solution (1st occurrence of a constant)
		int[][] cst1 = buildCst(toMatchArg, realArgs, realCol, new Hashtable<String, Integer>());
		if(!(csts.contains(cst1))) {
			csts.add(cst1);
		}
		// we look for duplicated word
		// if there are some, we store the number of their occurrences
		for (int i = 0 ; i < realCol ; i++) {
			ArgContent acI = realArgs.get(i);
			if (acI.getType() == ArgContent.TERM) {
				if (occurences.containsKey(acI.getName())) {
					int j = occurences.get(acI.getName()) + 1;
					occurences.put(acI.getName(), j);
					//System.out.println("=== ambiguity found for " + acI.getName());
					preBindingsNeeded = true;
					if (j > maxLength) { maxLength = j; }
				} else {
					occurences.put(acI.getName(), 0);
				}
			}
		}
		// if we have found some duplicates, we compute the extra-constraints needed
		// if (preBindingsNeeded) {
		// 	//System.err.println("predBindings needed " + realArgs.toString());
		// 	// We compute the prebindings constraints on the argument to match!
		// 	Map<String, Integer> toMatchOcc = new Hashtable<String, Integer>();
		// 	for(int i = 0 ; i < toMatchArg.size() ; i++){
		// 		ArgContent acTM = toMatchArg.get(i);
		// 		if (acTM.getType() == ArgContent.TERM && occurences.containsKey(acTM.getName())) {
		// 			toMatchOcc.put(acTM.getName(), occurences.get(acTM.getName()));
		// 		}
		// 	}
		// 	// we compute the prebindings for these constraints
		// 	List<Map<String, Integer>> prebindings = PreBindings.computePreBindings(toMatchOcc, maxLength+1);
		// 	for(int i = 0 ; i < prebindings.size() ; i++) {
		// 		int[][] cst2 = buildCst(toMatchArg, realArgs, realCol, prebindings.get(i));
		// 		if(!(csts.contains(cst2))) {
		// 			csts.add(cst2);
		// 		}
		// 	}
		// }
		
		// 2nd, we ask for all the solutions
		// for(int i = 0 ; i < csts.size() ; i++) {
		// 	int[][] cst = csts.get(i);

		// 	List<int[][]> lres = BindingsCSP.computeBindings(cst);
		// 	//System.err.println("******* size " + lres.size());
		// 	for (int k = 0 ; k < lres.size() ; k++) {
		// 		int[][] sol = lres.get(k);
		// 		// the decoded solution is added to the list of bindings
		// 		try {
		// 			Binding bsol = decode(verbose, sol, new Argument(realArgs), toMatchArg);
		// 			if (!(lblist.contains(bsol))) {
		// 				//System.err.println("******* sol added " + bsol.toString());
		// 				lblist.add(bsol);
		// 			}
		// 		} catch (RCGInstantiationException ex) {
		// 			if (verbose) {
		// 				// we just intercept the message, but we do not raise
		// 				// the error, since other bindings may be found
		// 				System.err.println(ex);
		// 			}
		// 		}
		// 	}
		// }
		
		return lblist;
	}
	
	
	
	public int[][] buildCst(Argument toMatchArg, List<ArgContent> realArgs, int realCol, Map<String, Integer> occurrences){
		int[][] cst = new int[(toMatchArg.size() + 1)][(realCol + 1)];
		for(int j = 0 ; j < cst.length -1 ; j++) {
			//for each row
			ArgContent acTM = toMatchArg.get(j);
			if (acTM.getType() == ArgContent.TERM) {
				if (j==0) { // leading constant
					cst[j][0]   = 1;
					cst[j+1][1] = 1;
				}
				else {
					// we look for the "next" occurrence of acTM!
					int which = occurrences.containsKey(acTM.getName()) ? occurrences.get(acTM.getName()) : 0;
					
					for(int i = 0 ; i < cst[0].length -1 ; i++) {
						//	for each column
					
						ArgContent acI = realArgs.get(i);
						// if there is a constraint on the constant position, we mark it
						if (acI.getType() == ArgContent.TERM && acI.getName().equals(acTM.getName())) {
							if (which==0) {
								cst[j][i]     = 1;
								cst[j+1][i+1] = 1;
							}
							which--;
						}
					}
				}
			}
			//we check if this is a range related to a substitution node, 
			//if this is the case, the matrix gets the value 2 meaning f(xi) != f(xi+1) 
			//(substitution range cannot be bound to Epsilon)
			if (acTM.getType() == ArgContent.VAR && (acTM.getNode() == ArgContent.SUBST_RANGE || acTM.getNode() == ArgContent.MADJ_RANGE)) {
				cst[j][0] = 2; // only the leading element of the row is set to 2
			}
			
			//we check if this is an adjunction node for which there is no modifier in the subgrammar
			//if this is the case, the matrix gets the value 3 meaning f(xi) == f(xi+1)
			if (acTM.getType() == ArgContent.VAR && acTM.getNode() != ArgContent.SUBST_RANGE) {
				if (acTM.getCat() != null && !(cat.containsKey(acTM.getCat())))
					cst[j][0] = 3; // only the leading element of the row is set to 3
			}
		}
		return cst;
	}
	
	public Binding decode(boolean verbose, int[][] sol, Argument inArg, Argument toMatchArg) throws RCGInstantiationException {
		
		// To print the matrix of the solution 
		//BindingsCSP.printCst(sol, "Solution:");
		
		Binding b = new Binding();
		int rows     = 0;
		int columns  = 0;
		while (rows < sol.length-1) {
			if (sol[rows+1][columns] == sol[rows][columns]) {
				b.bind(verbose, toMatchArg.get(rows), new ArgContent(ArgContent.EPSILON, "Eps"));
			} else {
				LinkedList<ArgContent> al = new LinkedList<ArgContent>();
				
				while (columns < sol[0].length && sol[rows+1][columns] != 1) {
					al.add(inArg.get(columns));
					columns++;
				}	
				if (sol[rows+1][columns] == 1) {
					b.bind(verbose, toMatchArg.get(rows), new ArgContent(al));
				}
			}
			rows++;
		}
		//System.err.println("Computed binding: " + b.toString());
		return b;
	}

	public List<Argument> getSource() {
		return source;
	}

	public void setSource(List<Argument> source) {
		this.source = source;
	}

	public List<Argument> getDest() {
		return dest;
	}

	public void setDest(List<Argument> dest) {
		this.dest = dest;
	}

	public Map<String, Integer> getCat() {
		return cat;
	}

	public void setCat(Map<String, Integer> cat) {
		this.cat = cat;
	}
	
}
