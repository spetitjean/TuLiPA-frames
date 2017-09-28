/*
 * @(#)ChartPresenter.java created 21.04.2006
 * 
 * Copyright (c) 2006 Alexander Koller
 *  
 */

package de.saar.chorus.domgraph.chart;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.saar.chorus.domgraph.graph.DomGraph;

public class ChartPresenter {
    public static String chartOnlyRoots(Chart ch, DomGraph g) {
        StringBuffer ret = new StringBuffer();
        Set<String> roots = g.getAllRoots();
        Set<Set<String>> visited = new HashSet<Set<String>>();

        for( Set<String> fragset : ch.getToplevelSubgraphs() ) {
            ret.append(corSubgraph(fragset, ch, roots, visited));
        }
        
        return ret.toString();
    }
    


    private static String corSubgraph(Set<String> subgraph, Chart ch, Set<String> roots, Set<Set<String>> visited) {
        Set<String> s = new HashSet<String>(subgraph);
        StringBuffer ret = new StringBuffer();
        boolean first = true;
        String whitespace = "                                                                                                                                                                                  ";
        Set<Set<String>> toVisit = new HashSet<Set<String>>();
        
        if( !visited.contains(subgraph )) {
            visited.add(subgraph);
            
            s.retainAll(roots);
            String sgs = s.toString();
            
            
            if( ch.getSplitsFor(subgraph) != null ) {
                ret.append("\n" + sgs + " -> ");
                for( Split split : ch.getSplitsFor(subgraph)) {
                    if( first ) {
                        first = false;
                    } else {
                        ret.append(whitespace.substring(0, sgs.length() + 4));
                    }
                    
                    ret.append(corSplit(split, roots) + "\n");
                    toVisit.addAll(split.getAllSubgraphs());
                }
                
                for( Set<String> sub : toVisit ) {
                    ret.append(corSubgraph(sub, ch, roots, visited));
                }
            }
                
            return ret.toString();
        }
        else {
            return "";
        }
    }



    private static String corSplit(Split split, Set<String> roots) {
        StringBuffer ret = new StringBuffer("<" + split.getRootFragment());
        Map<String,List<Set<String>>> map = new HashMap<String,List<Set<String>>> (); 
        
        for( String hole : split.getAllDominators() ) {
            List<Set<String>> x = new ArrayList<Set<String>>();
            map.put(hole, x);

            for( Set<String> wcc : split.getWccs(hole) ) {
                Set<String> copy = new HashSet<String>(wcc);
                copy.retainAll(roots);
                x.add(copy);
            }
        }
        
        
        ret.append(" " + map);
        ret.append(">");
        return ret.toString();
    }
}
