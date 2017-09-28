/*
 *  File TextCFGReader.java
 *
 *  Authors:
 *     Wolfgang Maier  <wo.maier.uni-tuebingen.de>
 *     
 *  Copyright:
 *     Wolfgang Maier, 2009
 *
 *  Last modified:
 *     Di 16. Jan 10:45:12 CEST 2009
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
package de.tuebingen.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import de.tuebingen.rcg.ArgContent;
import de.tuebingen.rcg.Argument;
import de.tuebingen.rcg.Clause;
import de.tuebingen.rcg.PredStringLabel;
import de.tuebingen.rcg.Predicate;
import de.tuebingen.rcg.RCG;

/**
 * A reader for context-free grammars in the following format:
 *
 * # start S
 * # nonterm N V 
 * S -> N V 
 * N -> Peter
 * V -> schläft
 * 
 * @author wmaier
 *
 */
public class TextCFGReader extends BufferedReader implements RCGReader {

	public final static String LRSEP = "->";
	
	public List<String> nonterm;
	
	public TextCFGReader(File f) throws FileNotFoundException {
		super(new FileReader(f));
		nonterm = new ArrayList<String>();
	}
	
	public Clause stringToClause(String line) throws Exception {
		Clause ret = null;
		int sepind = line.indexOf(LRSEP);
		if (sepind > 0) {
			String lhs = line.substring(0, sepind - 1).trim();
			int rhsstart = sepind + LRSEP.length() + 1;
			String rhs = "";
			if (rhsstart < line.length())
				rhs = line.substring(rhsstart).trim();
			System.err.println("lhs: " + lhs + ", rhs: " + rhs);
			
			if (nonterm.indexOf(lhs) > -1) {
				ret = new Clause();
				Predicate lhsp = new Predicate(new PredStringLabel(lhs));
				String[] rhssp = rhs.split(" "); 
				Argument lhsarg = new Argument();
				if (rhssp.length > 0) {
					Predicate rhspred = null;
					for (String s : rhssp) {
						if (nonterm.contains(s)) {
							ArgContent ac = new ArgContent(ArgContent.VAR,s);
							lhsarg.addArg(ac);
							rhspred = new Predicate(new PredStringLabel(s));
							rhspred.addArg(new Argument(ac));
							ret.addToRhs(rhspred);
						} else {
							lhsarg.addArg(new ArgContent(ArgContent.TERM,s));
						}
					}
				} else {
					lhsarg.addArg(new ArgContent(ArgContent.EPSILON, ""));
				}
				lhsp.addArg(lhsarg);
				ret.setLhs(lhsp);
			} else {
				throw new Exception("Could not parse " + line + ": Lhs is not marked as a nonterminal!");
			}
		} else {
			throw new Exception("Could not parse " + line + ": Separator '" + LRSEP + "' not found.");
		}
		return ret;
	}
	

	public RCG getRCG() throws Exception {
		RCG ret = new RCG();
		String line = "";
		while ((line = super.readLine()) != null) {
			if (line.length() < 2) {
				continue;
			}
			line = line.trim();
			if (line.charAt(0) == '#') {
				line = line.substring(1).trim();
				if (line.indexOf("start") == 0) {
					line = line.substring(5).trim();
					if (line.length() > 0) 
						ret.setStartPredicate(new PredStringLabel(line));
				} else if (line.indexOf("nonterm") == 0) {
					line = line.substring(7).trim();
					if (line.length() > 0) {
						for (String s : line.split(" ")) 
							nonterm.add(s);
					}
				}
			} else if (line.charAt(0) != '%') {
				if (nonterm.size() > 0) {
					Clause c = stringToClause(line);
					if (c != null) ret.addClause(c, null);
				} else {
					throw new Exception("Cannot read CFG: Must specify nonterminals before specifying the grammar.");
				}
			}
		}
		if (!ret.startPredicateDefined()) {
			ret.setStartPredicate(new PredStringLabel("S"));
		}
		if (ret.getClausesForLabel(ret.getStartPredicateLabel()) == null) {
			throw new Exception("Cannot read CFG: No predicate with start predicate label found.");
		}
		return ret;		
	}
	
	
	/*
	 * just for testing
	 */
	public static void main(String[] args) throws Exception {
		TextCFGReader r = new TextCFGReader(new File("/home/wmaier/workspace/tulipa/trunk/test/cfg/example.cfg"));
		RCG g = r.getRCG();
		System.out.println(g.toString());
		r.close();
	}
	

}
