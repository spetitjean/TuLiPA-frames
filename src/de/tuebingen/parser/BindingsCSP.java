// /*
//  *  File BindingCSP.java
//  *
//  *  Authors:
//  *     Yannick Parmentier  <parmenti@sfs.uni-tuebingen.de>
//  *     
//  *  Copyright:
//  *     Yannick Parmentier, 2007
//  *
//  *  Last modified:
//  *     Di 16. Okt 09:58:58 CEST 2007
//  *
//  *  This file is part of the TuLiPA system
//  *     http://www.sfb441.uni-tuebingen.de/emmy-noether-kallmeyer/tulipa
//  *
//  *  TuLiPA is free software; you can redistribute it and/or modify
//  *  it under the terms of the GNU General Public License as published by
//  *  the Free Software Foundation; either version 3 of the License, or
//  *  (at your option) any later version.
//  *
//  *  TuLiPA is distributed in the hope that it will be useful,
//  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
//  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//  *  GNU General Public License for more details.
//  *
//  *  You should have received a copy of the GNU General Public License
//  *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
//  *
//  */
// package de.tuebingen.parser;

// import static org.gecode.Gecode.*;
// import static org.gecode.GecodeEnumConstants.*;

// import java.util.LinkedList;
// import java.util.List;
	 
// import org.gecode.*;

// //To inpect the solutions with the GUI, uncomment below:
// //import org.gecode.gist.Gist;

// @SuppressWarnings("restriction")
// public class BindingsCSP extends org.gecode.Space {
// 	public int rows;                                // number of rows
// 	public int columns;                             // number of columns
// 	public VarArray<IntVar> table;                  // the table
// 	public int a_d = (int)Gecode.getDefault_a_d();  // needed by Gecode, cf doc
// 	public int c_d = (int)Gecode.getDefault_c_d();  // needed by Gecode, cf doc

// 	public BindingsCSP(int r, int c) {
// 		super("BindingCSP");
// 		// creates the table of Integer Variables
// 		table = new VarArray<IntVar>(this, r, IntVar.class, 0, c-1);
// 		// sets the size of the table
// 		this.rows    = r;
// 		this.columns = c;
// 	}
	 
// 	public BindingsCSP(Boolean share, BindingsCSP bcsp) {
// 		// "sharing" constructor needed by Gecode, cf documentation
// 		super(share, bcsp);
// 		rows = bcsp.rows;
// 		columns = bcsp.columns;
// 		table = new VarArray<IntVar>(this, share, bcsp.table);
// 	}

// 	public void postCst(int[][] c){
// 		for(int i = 0; i < c.length ; i++) {
// 			// In all cases
// 			// there is a precedence order between row variables
// 			// i.e. value for variable row i is less or equal to 
// 			// value for variable row i+1
// 		     if (i < (c.length - 1)) {
// 		    	 rel(this, table.get(i), IRT_LQ, table.get(i+1));
// 		     }
// 	    	 // for ranges related to substitution nodes:
// 	    	 if (c[i][0] == 2) {
// 	    		 rel(this, table.get(i), IRT_NQ, table.get(i+1));
// 	    	 }
// 	    	 // for ranges related to null-adjunction nodes:
// 	    	 if (c[i][0] == 3) {
// 	    		 rel(this, table.get(i), IRT_EQ, table.get(i+1));
// 	    	 }
// 	    	 // specific constraints:
// 		     for(int j = 0 ; j < c[0].length ; j++) {
// 		    	 // if the value of the row is known (e.g. verspricht)
// 		    	 // we set it
// 		    	 if (c[i][j] == 1) { 
// 		    		 rel(this, table.get(i), IRT_EQ, j);
// 		    	 }
// 		     }
// 		}
// 		// we ensure that the boundaries are set
// 		rel(this, table.get(0), IRT_EQ, 0);
// 		rel(this, table.get(c.length-1), IRT_EQ, c[0].length-1);
		
// 		// branching instruction needed by Gecode, cf doc
// 		branch(this, table, INT_VAR_SIZE_MIN, INT_VAL_MIN);
// 	}
	 
// 	public void doSearch(BindingsCSP s, List<int[][]> sols){
// 		// searches for solutions in a deep-first search (DFS)
// 		// XXX: Watch out, this seems to cause a stack overflow
// 		DFSIterator<BindingsCSP> search = new DFSIterator<BindingsCSP>(s);
			
// 		//To inspect the solutions with the GUI, uncomment below:
// 		//Gist gist = new Gist(s, false);
// 		//gist.exploreAll();

// 		// for counting the number of solutions
// 		int nbsol = 0;
		
// 		while (search.hasNext()) {
// 			Space sol = (Space)search.next();
// 			//System.err.println(sol.toString());
// 			sols.add(((BindingsCSP) sol).storeSol());
// 			nbsol++;
// 		}
// 		System.err.println("There are "+nbsol+" solutions.");
// 	}
	     
// 	public int[][] storeSol(){
// 		int[][] res = new int[rows][columns];
// 		for (int i = 0; i < rows ; ++i) {
// 			for (int j = 0 ; j < columns ; ++j) { 
				
// 				if (table.get(i).assigned()) { // a value has been computed
// 					res[i][table.get(i).val()] = 1;
// 				}
// 			}
// 		}
// 		return res;
// 	}
	
// 	public String toString() {
// 		// prints the table solutions in sdtout
// 		// in real we should decode it to extract the bindings
// 		// computed by gecode
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
	 
// 	public static List<int[][]> computeBindings(int[][] constraints) {
// 		/*
// 		   This program is an example of the computing of the bindings between
// 		   [Jens][verspricht][Emma] and [L1][L2][L3][verspricht][R3][R2]
	
// 		   (version 2)

// 		   We will think in terms of range integers, ie we will consider as 
// 		   input arguments (called respectively I1 and I2):
		   
// 		     0[Jens]1[verspricht]2[Emma]3

// 		   and

// 	 	     A1 [L1] A2 [L2] A3 [L3] A4 [verspricht] A5 [R3] A6 [R2] A7

// 		   What we are looking for is the list of value assignations to 
// 		   all Lij and Rkl such that the following constraints (C) hold:
		   
// 		     Ai <= Aj  for all (i,j) in [1..3]X[1..3] such that i <= j

// 		     A4 = 1 and A5 = 2 (cf input I1)

// 		   These constraints can be represented graphically as follows (go ASCII art:)

// 		       | 0 | 1 | 2 | 3 | <-(range integer constants in I1)
// 		   ---------------------
// 		   A1  |   |   |   |   |
// 		   ---------------------
// 		   A2  |   |   |   |   |
// 		   ---------------------
// 		   A3  |   |   |   |   |
// 		   ---------------------
// 		   A4  |   | 1 |   |   |
// 		   ---------------------
// 		   A5  |   |   | 1 |   |
// 		   ---------------------
// 		   A6  |   |   |   |   |
// 		   ---------------------
// 		   A7  |   |   |   |   |
// 		   ---------------------
// 		   ^
// 		   |
// 		   (range integer variables in I2)

// 		   Note that the matrix we consider is sized :  (I2.size()+1) X (I1.size()+1)

// 		   The boxes left empty in the table have to be filled with 0 or 1 (1 representing 
// 		   the assignation of a value between a row variable and a column constant).

// 		   We want all solutions that match (C).

// 		   Gecode will compute these, from a table of constraints (including the boxes whose value 
// 		   is fixed as the matrix above with verspricht ranges), and will produce a list of tables of 
// 		   assignations (that will be decoded to retrieve the bindings), example of such a table:

// 		       | 0 | 1 | 2 | 3 |
// 		   ---------------------
// 		   A1  | 1 |   |   |   |      => [L1] = [Jens] (cf A1 = 0 and A2 = 1)
// 		   ---------------------
// 		   A2  |   | 1 |   |   |      => [L2] = [ ]    (cf A2 = 1 and A3 = 1)
// 		   ---------------------
// 		   A3  |   | 1 |   |   |      => [L3] = [ ]    (cf A3 = 1 and A4 = 1)
// 		   ---------------------
// 		   A4  |   | 1 |   |   |      => [R3] = [ ]    (cf A5 = 2 and A6 = 2)
// 		   ---------------------
// 		   A5  |   |   | 1 |   |      => [R2] = [Emma] (cf A6 = 2 and A7 = 3)
// 		   ---------------------
// 		   A6  |   |   | 1 |   |
// 		   ---------------------
// 		   A7  |   |   |   | 1 |
// 		   ---------------------

// 		   NB: for this assignation, there are 6 solutions.
// 		 */
		
// 		LinkedList<int[][]> solutions = new LinkedList<int[][]>();
		
// 		BindingsCSP bcsp = new BindingsCSP(constraints.length,constraints[0].length);
// 		// print the constraints
// 		//printCst(constraints, "Constraints matrix:");
// 		// post the constraints
// 		bcsp.postCst(constraints);
// 		bcsp.doSearch(bcsp, solutions);
// 		return solutions;
// 	}
	
// 	public static void printCst(int[][] constraints, String s) {
// 		System.err.println(s);
// 		for (int i = 0 ; i < constraints.length ; i++) {
// 			System.err.print("[");
// 			for (int j = 0 ; j < constraints[0].length ; j++) {
// 				System.err.print(constraints[i][j]+" ");
// 			}
// 			System.err.print("]\n");
// 		}		
// 	}
// }
