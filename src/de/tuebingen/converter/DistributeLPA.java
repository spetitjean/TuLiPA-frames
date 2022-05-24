/*
 *  File DistributeLPA.java
 *
 *  Authors:
 *     Yannick Parmentier  <parmenti@sfs.uni-tuebingen.de>
 *     
 *  Copyright:
 *     Yannick Parmentier, 2007
 *
 *  Last modified:
 *     Di 16. Okt 11:10:14 CEST 2007
 *
 *  This file is part of the TuLiPA system
 *     http://www.sfb441.uni-tuebingen.de/emmy-noether-kallmeyer/tulipa
 *
 *  TuLiPA is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 3 of the License, or
 *  (at your option) any later version.
 *
//  *  TuLiPA is distributed in the hope that it will be useful,
//  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
//  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//  *  GNU General Public License for more details.
//  *
//  *  You should have received a copy of the GNU General Public License
//  *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
//  *
//  */
// package de.tuebingen.converter;

// import static org.gecode.Gecode.*;
// import static org.gecode.GecodeEnumConstants.*;

// import java.util.LinkedList;

// import org.gecode.*;

// @SuppressWarnings("restriction")
// public class DistributeLPA extends Space {
// 	/**
// 	 * Class whose role is to distribute n elements within k slots
// 	 * More precisely, it creates a partition
// 	 */
//     public int trees;  // rows
//     public int nodes;  // columns
//     public VarArray<IntVar> csp;
//     public int a_d = (int)Gecode.getDefault_a_d();
//     public int c_d = (int)Gecode.getDefault_c_d();

//     public DistributeLPA(int rows, int columns, int [][] cc) {
//     	super("DistributeLPA");
//     	csp = new VarArray<IntVar>(this, rows, IntVar.class, 0, columns-1);
//     	this.trees = rows;
//     	this.nodes = columns;
//     	postCstr(cc);
//     }

//     public DistributeLPA(Boolean share, DistributeLPA dist) {
//     	super(share, dist);
//     	nodes = dist.nodes;
//     	trees = dist.trees;
//     	csp = new VarArray<IntVar>(this, share, dist.csp);
//     }

//     public void postCstr(int[][] constr) {
//     	for (int i = 0 ; i < trees ; i++) {
//     		for (int j = 0 ; j < nodes ; j++) {
//     			if (constr[i][j] == 0) {
//     				// we exclude the value j from valid adjunction nodes
//     				// for the tree i
//     				rel(this, csp.get(i), IRT_NQ, j);
//     			}
//     		}
//     	}
//     	branch(this, csp, INT_VAR_SIZE_MIN, INT_VAL_MIN);
//     }

//     public void doSearch(DistributeLPA s, LinkedList<Object> result){
//     	DFSIterator<DistributeLPA> search = new DFSIterator<DistributeLPA>(s);

//     	while (search.hasNext()) {
//     		Space sol = (Space)search.next();
//     		//System.out.println(sol.toString());
//     		result.add(((DistributeLPA) sol).store());
//     	}
//     }

//     public int[][] store(){
//     	int[][] res = new int[trees][nodes];
//     	for (int i = 0; i < trees ; ++i) {
//     		for (int j = 0 ; j < nodes ; ++j) 
//     			res[i][j] = 0;

//     		if (csp.get(i).assigned()) {
//     			res[i][csp.get(i).val()] = 1;
//     		}
//     	}
//     	return res;
//     }

//     public String toString() {
//     	String res = ""+trees+" X "+nodes+"\n";

//     	for (int i = 0; i < trees ; i++) {
//     		char[] l = new char[nodes];
//     		for (int j = 0 ; j < nodes ; j++) 
//     			l[j] = '0';

//     		if (csp.get(i).assigned()) {
//     			l[csp.get(i).val()] = '1';
//     		} 
//     		res += new String(l);
//     		res += "\n";
//     	}
//     	return res;
//     }

//     public static LinkedList<Object> solve(int[][] constr) {
//     	LinkedList<Object> solutions = new LinkedList<Object>();
//     	//System.out.println("input => "+constr.length+" X "+constr[0].length);
//     	DistributeLPA dist = new DistributeLPA(constr.length,constr[0].length, constr);
//     	dist.doSearch(dist, solutions);
//     	return solutions;
//     }    

// }
