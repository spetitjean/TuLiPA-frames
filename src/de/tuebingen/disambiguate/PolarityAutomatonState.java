/*
 *  File PolarityAutomatonState.java
 *
 *  Authors:
 *     Johannes Dellert  <jdellert@sfs.uni-tuebingen.de>
 *     
 *  Copyright:
 *     Johannes Dellert, 2008
 *
 *  Last modified:
 *     Thu Feb 28 15:43:52 CET 2008
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

public class PolarityAutomatonState
{
	int stateId;
	String tupleId;
    Polarities polarities;
    //automata are deterministic: edge labels are assigned the IDs of the states they lead to
    HashMap<String, Integer> edges;
    
    public PolarityAutomatonState(Polarities polarities, int i, String tid)
    {
    	this.tupleId = tid;
    	this.stateId = i;
        this.polarities = polarities;
        this.edges = new HashMap<String, Integer>();
    }
    
    public int getStateId() 
    {
		return stateId;
	}

	public void setStateId(int stateId) 
	{
		this.stateId = stateId;
	}

	public String getTupleId() 
	{
		return tupleId;
	}

	public void setTupleId(String tupleId) 
	{
		this.tupleId = tupleId;
	}

	public String toString() 
	{
    	String res = "";
    	res += "State " + stateId + " (" + tupleId + ")\n";
    	res += polarities.toString() + "\n";
    	for(String s : edges.keySet()) 
    	{
    		res += "edge to " + s + ", " + edges.get(s) + "\n";
    	}
    	res += "\n";
    	return res;
    }
}
