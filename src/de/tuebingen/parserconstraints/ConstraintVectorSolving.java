/*
 *  File ConstraintVectorSolving.java
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
// package de.tuebingen.parserconstraints;

// import static org.gecode.Gecode.*;
// import static org.gecode.GecodeEnumConstants.*;

// import java.util.LinkedList;
// import java.util.List;
// import java.util.Map;
		 
// import org.gecode.*;

// import de.tuebingen.util.Pair;

// //To inpect the solutions with the GUI, uncomment below:
// //import org.gecode.gist.Gist;

// @SuppressWarnings("restriction")
// public class ConstraintVectorSolving extends org.gecode.Space {
	
// 	public int rows;                                // number of rows
// 	public int columns;                             // number of columns
// 	public VarArray<IntVar> table;                  // the table
// 	public int a_d = (int)Gecode.getDefault_a_d();  // needed by Gecode, cf doc
// 	public int c_d = (int)Gecode.getDefault_c_d();  // needed by Gecode, cf doc

// 	public ConstraintVectorSolving(int r, int c) {
// 		super("Constraint Vector Solving");
// 		// creates the table of Integer Variables
// 		table = new VarArray<IntVar>(this, r, IntVar.class, 0, c-1);
// 		// sets the size of the table
// 		this.rows    = r;
// 		this.columns = c;
// 	}
	
// 	public ConstraintVectorSolving(Boolean share, ConstraintVectorSolving csp) {
// 		// sharing" constructor needed by Gecode, cf documentation
// 		super(share, csp);
// 		rows = csp.rows;
// 		columns = csp.columns;
// 		table = new VarArray<IntVar>(this, share, csp.table);
// 	}

// 	@SuppressWarnings("unchecked")
// 	public void postCst(ConstraintVector cv){
// 		List<Constraint> lc = cv.getConstraints();
// 		Pair bounds = cv.getBounds();
// 		Map<Object, Object> bound2int = (Map<Object, Object>) bounds.getKey();
		
// 		//System.err.println("## Vect: " + cv.print());
// 		//System.err.println("## Map: " + ConstraintVectorSolving.printMaps(bound2int));
		
// 		for (Constraint c : lc) {
			
// 			//System.err.println("## Posting c: " + c.toString());
			
// 			int type = c.getType();
// 			String arg1 = c.getArg1();
// 			String arg2 = c.getArg2();
// 			int index1 = arg1.equals(Constraint.VAL) ? -1 : (Integer) bound2int.get(arg1);
// 			int index2 = arg2.equals(Constraint.VAL) ? -1 : (Integer) bound2int.get(arg2);
// 			switch (type) {
// 			case Constraint.EQUALS:
// 				if (index1 == -1 && index2 == -1) {
// 					if (c.getModif1() != c.getModif2()) {
// 						// constraint: val1 = val2
// 						// but val1 != val2 : we force the CSP to have no solution!
// 						rel(this, table.get(0), IRT_EQ, c.getModif1());
// 						rel(this, table.get(0), IRT_EQ, c.getModif2());
// 					}
// 				} else if (index1 == -1) {
// 					rel(this, table.get(index2), IRT_EQ, c.getModif1());
// 				} else if (index2 == -1) {
// 					rel(this, table.get(index1), IRT_EQ, c.getModif2());
// 				} else {
// 					if (c.getModif1() == 0 && c.getModif2() == 0) {
// 						rel(this, table.get(index1), IRT_EQ, table.get(index2));
// 					} else if (c.getModif1() == 0) {
// 						post(this, new Expr(table.get(index2)).plus(c.getModif2()), IRT_EQ, table.get(index1));
// 					} else if (c.getModif2() == 0) {
// 						post(this, new Expr(table.get(index1)).plus(c.getModif1()), IRT_EQ, table.get(index2));
// 					} else {
// 						post(this, new Expr(table.get(index1)).plus(c.getModif1()), IRT_EQ, new Expr(table.get(index2)).plus(c.getModif2()));
// 					}
// 				}
// 				break;
// 			case Constraint.LE_EQ:
// 				if (index1 == -1 && index2 == -1) {
// 					if (c.getModif1() > c.getModif2()) {
// 						// constraint: val1 < val2
// 						// but val1 >= val2 : we force the CSP to have no solution!
// 						rel(this, table.get(0), IRT_EQ, c.getModif1());
// 						rel(this, table.get(0), IRT_EQ, c.getModif2());
// 					}
// 				} else if (index1 == -1) {
// 					rel(this, table.get(index2), IRT_GR, c.getModif1());
// 				} else if (index2 == -1) {
// 					rel(this, table.get(index1), IRT_LQ, c.getModif2());
// 				} else {
// 					if (c.getModif1() == 0 && c.getModif2() == 0) {
// 						rel(this, table.get(index1), IRT_LQ, table.get(index2));
// 					} else if (c.getModif1() == 0) {
// 						post(this, new Expr(table.get(index2)).plus(c.getModif2()), IRT_GR, table.get(index1));
// 					} else if (c.getModif2() == 0) {
// 						post(this, new Expr(table.get(index1)).plus(c.getModif1()), IRT_LQ, table.get(index2));
// 					} else {
// 						post(this, new Expr(table.get(index1)).plus(c.getModif1()), IRT_LQ, new Expr().plus(c.getModif1(), table.get(index1)));
// 					}
// 				}
// 				break;
// 			default: //skip	
// 			}
// 		}

// 		// branching instruction needed by Gecode, cf doc
// 		branch(this, table, INT_VAR_SIZE_MIN, INT_VAL_MIN);
// 	}
		 
// 	public void searchAll(ConstraintVectorSolving s, List<int[][]> sols) {
// 		// searches for solutions in a deep-first search (DFS)
// 		DFSIterator<ConstraintVectorSolving> search = new DFSIterator<ConstraintVectorSolving>(s);
				
// 		//To inspect the solutions with the GUI, uncomment below:
// 		//Gist gist = new Gist(s, false);
// 		//gist.exploreAll();

// 		// for counting the number of solutions
// 		int nbsol = 0;
			
// 		while (search.hasNext()) {
// 			Space sol = (Space)search.next();
// 			//System.err.println(sol.toString());
// 			sols.add(((ConstraintVectorSolving) sol).storeSol());
// 			nbsol++;
// 		}
// 		System.err.println("There are "+nbsol+" solutions.");
// 	}
	
// 	public int[][] storeSol(){
// 		int[][] res = new int[rows][columns];
// 		for (int i = 0; i < rows ; ++i) {
// 			for (int j = 0 ; j < columns ; ++j)
// 				res[i][j] = -1;
				
// 			if (table.get(i).assigned()) { // a value has been computed
// 				res[i][table.get(i).val()] = 1;
// 			} 
// 		}
// 		return res;
// 	}
	
// 	public String toString() {
// 		// prints the table solutions in sdtout
// 		String res = "";
		
// 		for (int i = 0; i < rows ; ++i) {
// 			char[] l = new char[columns];
// 			for (int j = 0 ; j < columns ; ++j) 
// 				l[j] = '\u00B7';
		     
// 			if (table.get(i).assigned()) { // a value has been computed
// 				l[table.get(i).val()] = '1';
// 		    } else { // the search space has been narrowed
// 		    	// but we do not have a single value yet 
// 		    	// (should not happen in our case)
// 		    	for (Range r : new IntVarRanges(table.get(i)))
// 		    		for (int j : r)
// 		    			l[j] = 'q';
// 		   	}
		     	
// 			res += new String(l);
// 			res += "\n";
// 		}
// 		return res;
// 	}
	
	
// 	public static boolean getPartial(boolean verbose, ConstraintVector cv, ConstraintVectorSolving s) {
// 		// method used to get partial results to a constraint vector
// 		// in practice, it searches for the root of the search tree
// 		// this root encodes pre-computed values of variables (the one that are known)
// 		// and leaves the others underspecified
// 		DFSIterator<ConstraintVectorSolving> search = new DFSIterator<ConstraintVectorSolving>(s);
		
// 		boolean res = false;
// 		// displays a string for now, will return the constraint vector cv updated
// 		if (search.hasNext()) { 
// 			//System.err.println("Underspecified solution:\n" + s.toString());
// 			int[][] sol = s.storeSol();
// 			//System.err.println(s.toString());
// 			extend(verbose, cv, sol, -1);
// 			res = true;
// 		}
// 		//else System.err.println("No solution");
// 		return res;
// 	}

	
// 	// NB: extend add constraints to the current vector while decode creates a new vector 
// 	// for each solution that has been computed!
// 	@SuppressWarnings("unchecked")
// 	public static void extend(boolean verbose, ConstraintVector cv, int[][] sol, int k) {
// 		String toShow = "Solution #" + k + "\n";
// 		Pair p = cv.getBounds();
// 		Map<Integer, String> int2bound = (Map<Integer, String>) p.getValue();
// 		for (int i = 0 ; i < sol.length ; i++) {
// 			for (int j = 0 ; j < sol[i].length ; j++) {
// 				if (sol[i][j] == 1) {
// 					toShow += int2bound.get(new Integer(i)) + " = " + j +"\n";
// 					Constraint c = new Constraint(Constraint.EQUALS, int2bound.get(new Integer(i)), Constraint.VAL, 0, j);
// 					cv.putVal(int2bound.get(new Integer(i)), new Integer(j));
// 					cv.addConstraint(c);
// 					if (k<0 && verbose) 
// 						System.err.println("### Adding: " + c.toString());
// 				}
// 			}
// 		}
// 		if (k>=0 && verbose) System.err.println(toShow);
// 	}

	
// 	@SuppressWarnings("unchecked")
// 	public static ConstraintVector decode(boolean verbose, ConstraintVector cv, int[][] sol, int k) {
// 		ConstraintVector res = new ConstraintVector(cv);
// 		String toShow = "Solution #" + k + "\n";
// 		Pair p = res.getBounds();
// 		Map<Object, Object> int2bound = (Map<Object, Object>) p.getValue();
// 		//System.err.println("##Vect: " + cv.print());
// 		//System.err.println("## Map: " + ConstraintVectorSolving.printMaps(int2bound));
// 		for (int i = 0 ; i < sol.length ; i++) {
// 			for (int j = 0 ; j < sol[i].length ; j++) {
// 				if (sol[i][j] == 1) {
// 					toShow += int2bound.get(new Integer(i)) + " = " + j +"\n";
// 					Constraint c = new Constraint(Constraint.EQUALS, (String) int2bound.get(new Integer(i)), Constraint.VAL, 0, j);
// 					res.putVal((String) int2bound.get(new Integer(i)), new Integer(j));
// 					res.addConstraint(c);
// 					if (k<0 && verbose) 
// 						System.err.println("### Adding: " + c.toString());
// 				}
// 			}
// 		}
// 		if (k>=0 && verbose) System.err.println(toShow);
// 		return res;
// 	}
	
// 	public boolean hasSolution(ConstraintVectorSolving s) {
// 		// searches for solutions in a deep-first search (DFS)
// 		DFSIterator<ConstraintVectorSolving> search = new DFSIterator<ConstraintVectorSolving>(s);
// 		return search.hasNext();
// 	}
		     
// 	public static boolean hasSol(boolean verbose, ConstraintVector cv) {
// 		ConstraintVectorSolving csp = new ConstraintVectorSolving(cv.getNbOfCstBounds(), cv.getSentence_length()+1);
// 		// post the constraints
// 		csp.postCst(cv);
// 		return getPartial(verbose, cv, csp);
// 	}
		 
// 	public static List<int[][]> computeSol(ConstraintVector cv) {
// 		// method computing all solutions to the CSP
// 		LinkedList<int[][]> solutions = new LinkedList<int[][]>();
// 		ConstraintVectorSolving csp = new ConstraintVectorSolving(cv.getNbOfCstBounds(), cv.getSentence_length()+1);
// 		// post the constraints
// 		csp.postCst(cv);
// 		csp.searchAll(csp, solutions);
// 		return solutions;
// 	}
	
// 	public static String printMaps(Map<Object, Object> map) {
// 		String res = "";
// 		for (Object s : map.keySet()) {
// 			res += s + " <-> " + map.get(s) + " | ";
// 		}
// 		return res;
// 	}
// }
