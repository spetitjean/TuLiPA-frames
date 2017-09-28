/*
 *  File IncrementalEarleyItem.java
 *
 *  Authors:
 *     Johannes Dellert
 *     
 *  Copyright:
 *     Johannes Dellert, 2009
 *
 *  Last modified:
 *     Do 16. Apr 09:55:36 CEST 2009
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
package de.tuebingen.parser.simple;

import de.tuebingen.rcg.*;

import java.util.*;

public class IncrementalEarleyItem
{
	static ArrayList<IncrementalEarleyItem> items = new ArrayList<IncrementalEarleyItem>();
	
	public static String itemsAsGraph()
	{
		String graStr = "";
		for (IncrementalEarleyItem it : items)
		{
			graStr += it.toGraph() + "\n";
		}
		return graStr;
	}
	
	int id;
	
    Clause cl;
    int pos;
    int i;
    int j;
    int el;
    //range table for the variables; second dimension encodes range start and end
    int[][] range;
    
    //epsilon handling: store epsilon positions
    int currentEpsilon;
    
    //store pointers to parent elements
    ArrayList<ItemOrigin> origins;
    
    //derived information on following argument element; must be updated by computeFollowingArgProperties()
    int currentElementType;
    int currentRHSPredicate;
    int currentRHSArgument;
    int currentRHSElement;
    PredLabel currentRHSPredLabel;  
    
    public IncrementalEarleyItem(Clause cl)
    {
    	this.id = items.size();
    	items.add(this);
    	
        this.cl = cl;
        pos = 0;
        i = 0;
        j = 0;
        el = 0;
        origins = new ArrayList<ItemOrigin>();
        range = new int[determineElementNumber(cl)][2];
        
        currentEpsilon = 0;
    }
    
    /*
     * set all ranges to -1 to express that nothing is known about them yet
     */
    public void resetAllRanges()
    {
        for (int i = 0; i < range.length; i++)
        {
            range[i][0] = -1;
            range[i][1] = -1;         
        }
    }
    
    /*
     * simple way to determine the number of variables in a simple ordered RCG clause
     */
    public int determineElementNumber(Clause cl)
    {
        int varNumber = 0;
        for (Argument arg : cl.getLhs().getArgs())
        {
            varNumber += arg.getContent().size();
        }
        return varNumber;
    }
    
    /*
     * simple way to determine the position of a variable on the LHS of a clause
     */
    public int determineVariablePosition(ArgContent var)
    {
        int pos = 0;
        int foundEpsilon = 0;
        for (Argument arg : cl.getLhs().getArgs())
        {
            for (ArgContent ac : arg)
            {
            	if (ac.getType() == ArgContent.EPSILON)
            	{
            		if (foundEpsilon == currentEpsilon)
            		{
            			currentEpsilon++;
            			return pos;
            		}
            		foundEpsilon++;
            	}
            	else
            	{
            		if (ac.equals(var)) return pos;
            	}
                pos++;
            }
        }
        return -1;
    }
    
    /*
     * get the argument content immediately before the dot
     */
    public ArgContent getPrecedingArgContent()
    {
        List<ArgContent> l = cl.getLhs().getArgs().get(i).getContent();
        if (j < 1) return null;
        else return l.get(j - 1);
    }
    
    /*
     * get the argument content immediately after the dot
     */
    public ArgContent getFollowingArgContent()
    {
        List<ArgContent> l = cl.getLhs().getArgs().get(i).getContent();
        if (j >= l.size()) return null;
        else return l.get(j);
    }
    
    /*
     * check whether item's predicate has an epsilon RHS
     */
    public boolean hasEpsilonRHS()
    {
    	return (cl.getRhs().isEmpty());
    }
    
    /*
     * compute and store all the properties of the argument content immediately after the dot
     */
    public void computeFollowingArgProperties()
    {
        currentElementType = -2;
        currentRHSPredicate = -1;
        currentRHSArgument = -1;
        currentRHSPredLabel = null;
        ArgContent fcont = getFollowingArgContent();
        ArgContent pcont = getPrecedingArgContent();
        if (fcont != null)
        {
            currentElementType = fcont.getType();
        }
        for (int pNumber = 0, pmax = cl.getRhs().size(); pNumber < pmax; pNumber++)
        {
            Predicate pred = cl.getRhs().get(pNumber);
            for (int aNumber = 0, amax = pred.getArgs().size(); aNumber < amax; aNumber++)
            {
                Argument arg = pred.getArgs().get(aNumber);
                for (int cNumber = 0, cmax = arg.getContent().size(); cNumber < cmax; cNumber++)
                {
                    ArgContent rcont = arg.getContent().get(cNumber);
                    if ((fcont != null && rcont.equals(fcont)))
                    {
                        currentRHSPredicate = pNumber;
                        currentRHSArgument = aNumber;
                        currentRHSElement = cNumber;
                        currentRHSPredLabel = pred.getLabel();
                    }
                    else if ((fcont == null) && rcont.equals(pcont))
                    {
                        currentRHSPredicate = pNumber;
                        currentRHSArgument = aNumber;
                        currentRHSElement = -1;
                        currentRHSPredLabel = pred.getLabel();
                    }
                }
            }
        }
    }
    
    public void addItemOrigin(String label, IncrementalEarleyItem parent1, IncrementalEarleyItem parent2)
    {
    	ItemOrigin orig = new ItemOrigin();
    	orig.label = label;
    	orig.parent1 = parent1;
    	orig.parent2 = parent2;
    	origins.add(orig);
    }
    
    public int hashCode()
    {
        return mainString().hashCode();
    }
    
    public boolean equals(Object obj)
    {
        if (obj != null && obj.getClass().equals(this.getClass()))
        {
            IncrementalEarleyItem o = (IncrementalEarleyItem) obj;
            if (!o.cl.equals(cl)) return false;
            if (o.el != el) return false; 
            if (o.pos != pos) return false;
            for (int i = 0; i < range.length; i++)
            {
                if (o.range[i][0] != range[i][0]) return false;
                if (o.range[i][1] != range[i][1]) return false;
            }
            return true;
        }
        return false;
    }
    
	public int getHash()
	{
		//might recieve a different implementation in the future
		return hashCode();
	}
    
    public String mainString()
    { 
        return "[" + cl.toString() + "," + pos + ",<" + i + "," + j + ">=" + el + "," + rangeString() + "]"; 
    }
    
    public String toString()
    {
        return mainString()+ " cET: " + currentElementType + " cRPL: " + currentRHSPredLabel + " cRP: " + currentRHSPredicate + " cRA: " + currentRHSArgument + " cRE: " + currentRHSElement + " cRET: " + currentElementType; 
    }
    
    public String rangeString()
    {
    	 String rangeStr = "[";
         for (int i = 0; i < range.length; i++)
         {
             rangeStr += "[";
             for (int j = 0; j < range[i].length; j++)
             {
                 rangeStr += range[i][j];
                 if (j < range[i].length - 1) rangeStr += ",";
             }
             rangeStr += "]";
             if (i < range.length - 1) rangeStr += ",";
         }
         rangeStr += "]";
         return rangeStr;
    }
    
    public String argumentRangeString()
    {
    	Predicate p = cl.getLhs();		
    	
		//FIXME: very slow because of determineVariablePosition calls!
    	//FIXME: very problematic handling of epsilons (determineVariablePosition fails here!)
		String ranges = "[";	
		currentEpsilon = 0;
		for (Argument arg : p.getArgs())
		{
			int lpos = determineVariablePosition(arg.get(0));
			int rpos = lpos;
			if (arg.get(0).getType() != ArgContent.EPSILON)
			{
				rpos = determineVariablePosition(arg.get(arg.size() - 1));
			}		
			ranges += "[" + range[lpos][0] + "," + range[rpos][1] + "]";
			ranges += ",";
		}
		if (ranges.length() > 1)
		{
			ranges = ranges.substring(0, ranges.length() - 1);
		}
		ranges += "]";
		currentEpsilon = 0;
		return ranges;
    }
    
    public String toXML()
    {
    	String xmlString = "<" ;//+ origin + " ";
    	xmlString += "cl=\"" + cl.toString().replaceAll(">", "") +"\" ";
    	xmlString += "pos=\"" + pos +"\" ";
    	xmlString += "i_j=\"" + i + "_" + j + "\" ";
    	xmlString += "ranges=\"" + rangeString() + "\" ";
    	xmlString += ">";
    	//if (parent1 != null) xmlString += parent1.toXML();
    	//if (parent2 != null) xmlString += parent2.toXML();   	
    	//xmlString += "</" + origin + ">";
    	return xmlString;
    }
    
    public String toGraph()
    {
    	String graphString = id + " " + id + mainString().replaceAll(" ", "");
    	for (ItemOrigin origin : origins)
    	{
    		if (origin.parent1 != null) graphString += " (" + origin.label + "," + origin.parent1.id + ")";
    		if (origin.parent2 != null) graphString += " (" + origin.label + "," + origin.parent2.id + ")";
    	}
    	return graphString;
    }
}
