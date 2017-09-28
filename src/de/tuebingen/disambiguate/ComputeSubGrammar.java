/*
 *  File ComputeSubGrammar.java
 *
 *  Authors:
 *     Yannick Parmentier  <parmenti@sfs.uni-tuebingen.de>
 *     
 *  Copyright:
 *     Yannick Parmentier, 2008
 *
 *  Last modified:
 *     Wed Feb 27 15:53:52 CET 2008
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
package de.tuebingen.disambiguate;

import java.util.*;
import de.tuebingen.tag.*; 

/**
 * Class used to compute a tree grammar, given a set of compatible tuples
 * @author parmenti
 *
 */
public class ComputeSubGrammar {
	
	public static List<List<Tuple>> computeSubGrammar(boolean verbose, List<List<String>> compatibleTuples, Map<String, List<String>> tupleMap, Map<String, TagTree> dict) {
		List<List<Tuple>> res = new LinkedList<List<Tuple>>();
		for(int i = 0 ; i < compatibleTuples.size() ; i++) {
			List<String> tuples = compatibleTuples.get(i);
			if (verbose)
				System.err.println("\n*****\nSelecting sub-grammar " + i + " ...");
			List<Tuple> res2 = new LinkedList<Tuple>();
			for(int j = 0 ; j < tuples.size() ; j++) {
				String tuple = tuples.get(j);
				List<String> trees = tupleMap.get(tuple);
				if (trees != null) { //trees is null when the tuple name is "" (lexical item or coanchor)
					Tuple newTuple = new Tuple(tuple);
					List<TagTree> arguments = new LinkedList<TagTree>();
					for(int k = 0 ; k < trees.size() ; k++) {
						String tree = trees.get(k);
						TagTree ttree = dict.get(tree);
						if (ttree.getIsHead()) {
							newTuple.setHead(ttree);
							newTuple.setOriginalId(ttree.getOriginalTupleId());
							newTuple.setFamily(ttree.getFamily());
						} else {
							arguments.add(ttree);
						}
					}
					newTuple.setArguments(arguments);
					if (verbose)
						System.err.println("Adding tuple " + newTuple.getId() + "(" + newTuple.getOriginalId() + ")");
					res2.add(newTuple);
				}
			}
			res.add(res2);
		}
		return res;
	}
}
