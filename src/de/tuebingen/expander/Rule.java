/*
 *  File Rule.java
 *
 *  Authors:
 *     Johannes Dellert  <johannes.dellert@sfs.uni-tuebingen.de>
 *     
 *  Copyright:
 *     Johannes Dellert, 2008
 *
 *  Last modified:
 *     Wed Jan 30 11:49:58 CET 2008
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
package de.tuebingen.expander;

import java.util.*;

import org.w3c.dom.*;

public class Rule
{
    String id;
    String treeName;
    
    ArrayList<RHS> rhs;
    
    public Rule(String id, String treeName)
    {
        this.id = id;
        this.treeName = treeName;
        rhs = new ArrayList<RHS>();
    }
    
    public Rule(Node n)
    {
        this.id = n.getAttributes().getNamedItem("id").getNodeValue();
        this.treeName = n.getAttributes().getNamedItem("tree_name").getNodeValue();
        rhs = new ArrayList<RHS>();       
        NodeList children = n.getChildNodes();
        for (int i = 0; i < children.getLength(); i++)
        {
            Node child = children.item(i);
            if (child instanceof Element)
            {
                rhs.add(new RHS(child));
            }
        }
    }
    
    public ArrayList<Element> apply(Element n, Document D, HashMap<String, Rule> rules, int recDepth, boolean verbose)
    {
        ArrayList<Element> allPossibleTrees = new ArrayList<Element>();
        for (RHS r : rhs)
        {
        	if (verbose) System.err.println((produceSpaces(recDepth)) + "rhs(" + r.ops.size() + ")\n" + (produceSpaces(recDepth)) + "{");
            ArrayList<Element> possibleTrees = new ArrayList<Element>();
            possibleTrees.add((Element) n.cloneNode(true));
            for (OpDisjunction disj : r.ops)
            {    	
                ArrayList<Element> possibleSubTrees = new ArrayList<Element>();
                ArrayList<Element> possibleSubTreeOperations = new ArrayList<Element>();
                if (verbose) System.err.println((produceSpaces(recDepth + 2)) + "disj(" + disj + ")\n" + (produceSpaces(recDepth + 2)) + "{");
                for (Operation op : disj.ops)
                {
                	if (verbose) System.err.println(produceSpaces(recDepth + 4) + "op(" + op + ")\n" + (produceSpaces(recDepth + 4)) + "{");
                    Rule nextRule = rules.get(op.id);
                    Element nextNode = D.createElement("tree");
                    nextNode.setAttribute("id", nextRule.treeName);
                    possibleSubTrees.addAll(nextRule.apply(nextNode, D, rules, recDepth + 6, verbose));
                    //insert the op nodes in between for compatibility
                    int subTreeNumber = possibleSubTrees.size();
                    if (verbose) System.err.println((produceSpaces(recDepth + 4)) + "} subtrees: " + subTreeNumber);
                    for (int i = 0; i < subTreeNumber; i++)
                    {
                        Element subNode = possibleSubTrees.remove(0);
                        Element opNode = D.createElement(op.type);
                        opNode.setAttribute("node", op.node);
                        opNode.setAttribute("id", op.opId);
                        opNode.appendChild(subNode);
                        possibleSubTreeOperations.add(opNode);
                    }
                }
                if (verbose) System.err.println((produceSpaces(recDepth + 2)) + "}");
                //go through all the possible trees and extend them to all the combinatorical variants
                int oldSize = possibleTrees.size();
                for (int i = 0; i < oldSize; i++)
                {
                    Element superNode = possibleTrees.remove(0);
                    for (Element subNode : possibleSubTreeOperations)
                    {
                        Element superCopy = (Element) superNode.cloneNode(true);
                        Element subNodeCopy = (Element) subNode.cloneNode(true);
                        superCopy.appendChild(subNodeCopy);
                        possibleTrees.add(superCopy);
                    }
                }
            }
            allPossibleTrees.addAll(possibleTrees);
            if (verbose) System.err.println((produceSpaces(recDepth)) + "}");
        }
        return allPossibleTrees;
    }
    
    public String produceSpaces(int number)
    {
    	String spaces = "";
    	for (int i = 0; i < number; i++)
    	{
    		spaces += "  ";
    	}
    	return spaces;
    }
}
