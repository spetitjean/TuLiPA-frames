/*
 *  File PreBindings.java
 *
 *  Authors:
 *     Yannick Parmentier  <parmenti@sfs.uni-tuebingen.de>
 *
 *  Copyright:
 *     Yannick Parmentier, 2007
 *
 *  Last modified:
 *     Di 16. Okt 10:02:43 CEST 2007
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
// package de.tuebingen.parser;

// import static org.gecode.Gecode.branch;
// import static org.gecode.Gecode.rel;
// import static org.gecode.GecodeEnumConstants.INT_VAL_MIN;
// import static org.gecode.GecodeEnumConstants.INT_VAR_SIZE_MIN;
// import static org.gecode.GecodeEnumConstants.IRT_LQ;

// import java.util.HashMap;
// import java.util.Hashtable;
// import java.util.Iterator;
// import java.util.LinkedList;
// import java.util.List;
// import java.util.Map;
// import java.util.Set;

// import org.gecode.DFSIterator;
// import org.gecode.Gecode;
// import org.gecode.IntVar;
// import org.gecode.IntVarRanges;
// import org.gecode.Range;
// import org.gecode.Space;
// import org.gecode.VarArray;

// /**
//  * Class used to compute the constraints on bindings in case a constant appear more than once
//  *   in the input argument
//  *   
//  * This class merely distributes values over a range (in case of several ranges, this computes a cartesian product)
//  * 
//  * @author parmenti
//  *
//  */
// @SuppressWarnings("restriction")
// public class PreBindings extends Space {
//      public int rows;                                // number of rows
//      public int columns;                             // number of columns
//      public VarArray<IntVar> table;                  // the table
//      public int a_d = (int)Gecode.getDefault_a_d();  // needed by Gecode, cf doc
//      public int c_d = (int)Gecode.getDefault_c_d();  // needed by Gecode, cf doc

//      public PreBindings(int r, int c) {
//     	 super("PreBindings");
//     	 // creates the table of Integer Variables
//     	 table = new VarArray<IntVar>(this, r, IntVar.class, 0, c-1);
//     	 // sets the size of the table
//     	 this.rows    = r;
//     	 this.columns = c;
//      }

//      public PreBindings(Boolean share, PreBindings bcsp) {
//     	 // "sharing" constructor needed by Gecode, cf documentation
//     	 super(share, bcsp);
//     	 rows = bcsp.rows;
//     	 columns = bcsp.columns;
//     	 table = new VarArray<IntVar>(this, share, bcsp.table);
//      }

//      public void postCst(Map<Integer, Integer> c){
//     	 for(int i = 0; i < c.size() ; i++) {
//     		 // for each constant in the predicate's argument 
//     		 // we now the maximal value it can take
//     		 int max = c.get(i);
//     		 rel(this, table.get(i), IRT_LQ, max);
//     	 }
//     	 // branching instruction needed by Gecode, cf doc
//     	 branch(this, table, INT_VAR_SIZE_MIN, INT_VAL_MIN);
//      }

//      public void doSearch(PreBindings s, List<Map<String, Integer>> sols, Map<Integer, String> id2constant){
//     	 // searches for solutions in a depth-first search (DFS)
//     	 DFSIterator<PreBindings> search = new DFSIterator<PreBindings>(s);

//     	 // for counting the number of solutions
//     	 int nbsol = 0;

//     	 while (search.hasNext()) {
//     		 Space sol = (Space)search.next();
//     		 //System.err.println(sol.toString());
//     		 sols.add(((PreBindings) sol).decode(id2constant));
//     		 nbsol++;
//     	 }
//     	 System.out.println("There are "+nbsol+" solutions.");
//      }

//      public Map<String, Integer> decode(Map<Integer, String> id2constant) {
//     	 Map<String, Integer> res = new HashMap<String, Integer>();

//     	 for (int i = 0; i < rows ; ++i) {
//     		 String constant = id2constant.get(i);
//     		 for (int j = 0 ; j < columns ; ++j) {     		 
//     			 if (table.get(i).assigned()) { // a value has been computed
//     				 res.put(constant, table.get(i).val());
//     			 } 
//     		 }
//     	 }
//     	 //printCst(res);
//     	 return res;
//      }

//      public String toString() {
//     	 // prints the table solutions in sdtout
//     	 // in real we should decode it to extract the bindings
//     	 // computed by gecode
//     	 String res = "";

//     	 for (int i = 0; i < rows ; ++i) {
//     		 char[] l = new char[columns];
//     		 for (int j = 0 ; j < columns ; ++j) 
//     			 l[j] = '\u00B7';

//     		 if (table.get(i).assigned()) { // a value has been computed
//     			 l[table.get(i).val()] = '1';
//     		 } else { // the search space has been narrowed
//     			 // but we do not have a single value yet 
//     			 // (should not happen in our case)
//     			 for (Range r : new IntVarRanges(table.get(i)))
//     				 for (int j : r)
//     					 l[j] = 'q';
//     		 }
//     		 res += new String(l);
//     		 res += "\n";
//     	 }
//     	 return res;
//      }

//      public static List<Map<String, Integer>> computePreBindings(Map<String, Integer> occurences, int maxLength) {

//     	 List<Map<String, Integer>>  sols = new LinkedList<Map<String, Integer>>();
//     	 Map<Integer, String> id2constant = new Hashtable<Integer, String>();
//     	 Map<Integer, Integer> constraints= new Hashtable<Integer,Integer>();

//     	 Set<String>    keys = occurences.keySet();
//     	 Iterator<String> it = keys.iterator();
//     	 int id = 0;
//     	 while (it.hasNext()) {
//     		 String constant = it.next();
//     		 id2constant.put(id, constant);
//     		 constraints.put(id, occurences.get(constant));
//     		 id++;
//     	 }

//     	 PreBindings bcsp = new PreBindings(constraints.size(), maxLength);
//     	 // post the constraints
//     	 bcsp.postCst(constraints);
//     	 bcsp.doSearch(bcsp, sols, id2constant);

//     	 return sols;
//      }

//      public static void printCst(Map<String, Integer> occurences){
//     	 String res = "Constraints \n";
//     	 Set<String>    keys = occurences.keySet();
//     	 Iterator<String> it = keys.iterator();
//     	 while(it.hasNext()) {
//     		String c = it.next();
//     		res += c + " " + occurences.get(c) + "\n";
//     	 }
//     	 System.err.println(res);
//      }
//  }
