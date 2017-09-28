package de.saar.chorus.domgraph.chart;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import de.saar.chorus.domgraph.graph.DomEdge;
import de.saar.chorus.domgraph.graph.DomGraph;

/**
 * An iterator over the different solved forms represented by
 * a {@link Chart}. The chart is passed to the constructor of
 * an object of this class. Then you can iterate over the solved
 * forms of this chart using <code>hasNext()</code> and <code>next()</code>
 * as usual.<p>
 * 
 * Each successful call to <code>next()</code> will return an object
 * of class <code>List<{@link DomEdge}></code>, i.e. a list of
 * dominance edge representations. This list can e.g. be passed
 * to the <code>encode</code> method of {@link de.saar.chorus.domgraph.codec.OutputCodec} or
 * one of its subclasses.<p>
 * 
 * This class implements a transition system for states consisting
 * of an agenda of subgraphs that must currently be resolved, and
 * a stack of splits that still need to be processed. This algorithm
 * is dramatically faster than a naive algorithm which simply computes
 * the sets of solved forms of a graph by computing the Cartesian
 * product of the sets of solved forms of its subgraphs, but quite
 * a bit more complicated.
 * 
 * @author Alexander Koller
 * @author Michaela Regneri
 *
 */
public class SolvedFormIterator implements Iterator<List<DomEdge>> {
	private Chart chart;
	private Agenda agenda;
	private Stack<EnumerationStackEntry> stack;

    //private Map<Set<String>, String> fragmentTable;
    private Set<String> roots;
    private String rootForThisFragset;
	
    // the solved form which will be returned by the next call
    // to next()
	private List<DomEdge> nextSolvedForm;
	
    // a cached list of solved forms for get(int)
	private List< List<DomEdge> > solvedForms;
    // the iterator used for computing the solved forms
    private SolvedFormIterator iteratorForGet;
    
	
    // I need the graph in order to determine the fragments: I need to
    // know the roots of singleton fragsets to create the dom edge.
	public SolvedFormIterator(Chart ch, DomGraph graph) {
        this(ch,graph,true);
	}
    
    private SolvedFormIterator(Chart ch, DomGraph graph, boolean makeIteratorForGet) {
        chart = ch;
        agenda = new Agenda();
        stack = new Stack<EnumerationStackEntry>();
        solvedForms = new ArrayList< List<DomEdge> >();
        
        if( makeIteratorForGet ) {
            iteratorForGet = new SolvedFormIterator(ch, graph, false);
        } else {
            iteratorForGet = null;
        }
        
        //fragmentTable = graph.getFragments();
        roots = graph.getAllRoots();
        
        
        for( Set<String> fragset : chart.getToplevelSubgraphs() ) {
            if( fragset.size() > 0 ) {
                agenda.add(new AgendaEntry(null, fragset));
            }
        }
        
        //Null-Element on Stack
        stack.push( new EnumerationStackEntry(null, new ArrayList<Split>(), null));
        
        updateNextSolvedForm();
    }
    
	

    private void updateNextSolvedForm() {
		if( isFinished() ) {
			nextSolvedForm = null;
		} else {
			findNextSolvedForm();
			
			if( representsSolvedForm() ) {
				nextSolvedForm = extractDomEdges();
			} else {
				nextSolvedForm = null;
			}
		}
	}
	
	private boolean representsSolvedForm(){
		return (agenda.isEmpty() && stack.size() > 0 );
	}
	
	
	private boolean isFinished(){	
		return (agenda.isEmpty() && stack.isEmpty() );
	}
	
	
	private List<DomEdge> extractDomEdges() {
		List<DomEdge> toReturn = new ArrayList<DomEdge>();
		
		for( EnumerationStackEntry ese : stack) {
			toReturn.addAll(ese.getEdgeAccu());
		}
		
		return toReturn;
	}
	
	
	private void findNextSolvedForm() {
		if( !isFinished() ) {
			do {
				//System.err.println("step()");
				step();
			} while(!agenda.isEmpty());
			
			if( isFinished() ) {
				agenda.clear();
			} else {
//				System.err.println("===== SOLVED FORM ===="); //Debug
			}
		} 
	}
	
	
	private void addSplitToAgendaAndAccu(EnumerationStackEntry ese) {
		Split split = ese.getCurrentSplit();
		
		// iterate over all dominators
			for( String node : split.getAllDominators() ) {
				List<Set<String>> wccs = split.getWccs(node);
				for( int i = 0; i < wccs.size(); i++ ) {
					Set<String> wcc = wccs.get(i);
					addFragsetToAgendaAndAccu(wcc, node, ese);
				}
			}
		
	}
	
	private void addFragsetToAgendaAndAccu(Set<String> fragSet, String dominator, EnumerationStackEntry ese) {
        if( isSingleton(fragSet) ) {
            // singleton fragsets: add directly to ese's domedge list
            DomEdge newEdge = new DomEdge(dominator, getSingletonRoot(fragSet));
            ese.addDomEdge(newEdge);
            //System.err.println("Singleton DomEdge : " + newEdge);
        } else {
            // larger fragsets: add to agenda
            AgendaEntry newEntry = new AgendaEntry(dominator,fragSet);
            agenda.add(newEntry);
        }
    }


    private String getSingletonRoot(@SuppressWarnings("unused") Set<String> fragSet) {
        return rootForThisFragset;
        //return fragSet.iterator().next();
        //return fragmentTable.get(fragSet);
    }

    private boolean isSingleton(Set<String> fragSet) {
        int numRoots = 0;
        
        for( String node : fragSet ) {
            if( roots.contains(node) ) {
                numRoots++;
                rootForThisFragset = node;
            }
        }
        
        return numRoots == 1;
    }

    private void step() {
		EnumerationStackEntry top = stack.peek();
		AgendaEntry agTop;
		Set<String> topFragset;
		String topNode;
		
		// 1. Apply (Up) as long as possible
		if ( agenda.isEmpty() ) {
			while( top.isAtLastSplit() ) {
				//System.err.println("(Up)"); //debug
				stack.pop();
				if (stack.isEmpty() )
					return;
				else 
					top = stack.peek();
			}
		}
        
        //System.err.println("agenda: " + agenda);
		
		// 2. Apply (Step) or (Down) as appropriate.
		// Singleton fragments are put directly into the accu, rather
		// than the agenda (i.e. simulation of Singleton).
		if( agenda.isEmpty() ) {
			// (Step)
			//System.err.println("(Step)");
			top.clearAccu();
			top.nextSplit();
			
			if ( top.getDominator() != null ) {
                DomEdge newEdge = 
                    new DomEdge(top.getDominator(), top.getCurrentSplit().getRootFragment());
				top.addDomEdge(newEdge);
				
				//System.err.println("new DomEdge: " + newEdge);
			}
			
			if(! top.getAgendaCopy().isEmpty() ) {
				//System.err.println("Retrieve agenda from stored stack entry."); //debug
				agenda.addAll(top.getAgendaCopy());
			}
			
			addSplitToAgendaAndAccu( top );
		} else {
            // (Down)
            //System.err.println("(Down)");

			agTop = agenda.pop();
            //System.err.println("agTop = " + agTop);
			topNode = agTop.getDominator();
			topFragset = agTop.getFragmentSet();
            
			if (topFragset.size() > 1 ) {
                List<Split> sv = chart.getSplitsFor(topFragset);
                
                EnumerationStackEntry newTop = 
                    new EnumerationStackEntry(topNode, sv, agenda);
                
                //System.err.println("new ese: " + newTop);

				if( topNode != null ) {
                    DomEdge newEdge = 
                        new DomEdge(topNode, newTop.getCurrentSplit().getRootFragment());
					newTop.addDomEdge( newEdge );
				
					//System.err.println("new DomEdge: " + newEdge);
				}
				
                //System.err.println("push: " + newTop);
				stack.push(newTop);
				addSplitToAgendaAndAccu( newTop );
			}
		}
	}
    
    
    
    
    /**** convenience methods for implementing Iterator ****/
    
    public boolean hasNext() {
    	return nextSolvedForm != null;
    }

    public List<DomEdge> next() {
    	List<DomEdge> ret = nextSolvedForm;
    	
    	if( ret != null ) {
    		updateNextSolvedForm();
    		return ret;
    	} else {
    		return null;
    	}
    }

    /**
     * This returns a solved form represented by a List of <code>DomEdge</code>
     * objects. The form is accessed via its index.
     * Forms with indices exceeding the range of <code>int</code> have to be
     * extracted manually by calling <code>next()</code> as often as necessary.
     * 
     * @param sf index of the solved form to extract
     * @return the solved form
     */
    public List<DomEdge> getSolvedForm(int sf) {
        for( int i = solvedForms.size(); i <= sf; i++ ) {
            if( !iteratorForGet.hasNext() ) {
                return null;
            } else {
                solvedForms.add(iteratorForGet.next());
            }
        }
        
        return solvedForms.get(sf);
    }
    
   
    

    public void remove() {
        throw new UnsupportedOperationException();
    }
    
    public Chart getChart() {
    	return chart;
    }
    
    
    /**** classes for the agenda and the enumeration stack ****/
    
    private static class EnumerationStackEntry {
        private String dominator;
        private List<DomEdge> edgeAccu;
        private Split currentSplit;
        private List<Split> splits; // points into chart
        private Split lastElement;
        private Agenda agendaCopy;
        private Iterator<Split> splitIterator;
        
        EnumerationStackEntry(String dom, List<Split> spl, Agenda agenda) {
            dominator = dom;
            splits = spl;
            agendaCopy = new Agenda();
            
            if( agenda != null ) {
                agendaCopy.addAll(agenda);
            }
            
            if( (spl != null) && (! spl.isEmpty() )  ) {
                
                splitIterator = splits.iterator();
                currentSplit = splitIterator.next();
                
                lastElement = splits.get(splits.size() - 1);
                
            } 
            
            edgeAccu = new ArrayList<DomEdge>();
        }
            
        public void nextSplit() {
            currentSplit = splitIterator.next();
        }
        
        public boolean isAtLastSplit() {
            if( splitIterator == null ) {
                return true;
            }
            return ( (! splitIterator.hasNext()) || currentSplit.equals(lastElement));
        }
        
        public void addDomEdge(DomEdge edge) {
            edgeAccu.add(edge);
        }
        
        public void clearAccu() {
            edgeAccu.clear();
        }
        
       
        /**
         * @return Returns the dominator.
         */
        public String getDominator() {
            return dominator;
        }

        /**
         * @return Returns the edgeAccu.
         */
        public List<DomEdge> getEdgeAccu() {
            return edgeAccu;
        }


        /**
         * @return Returns the currentSplit.
         */
        public Split getCurrentSplit() {
            return currentSplit;
        }


        /**
         * @return Returns the agendaCopy.
         */
        public Agenda getAgendaCopy() {
            return agendaCopy;
        }

        
        public String toString() {
            StringBuilder ret = new StringBuilder();
            
            ret.append("<ESE dom=" + dominator + ", accu=" + edgeAccu);
            ret.append(", agendacopy=" + agendaCopy + ", splits=");
            for( Split split : splits ) {
                if( split == currentSplit ) {
                    ret.append(split.toString().toUpperCase());
                } else {
                    ret.append(split);
                }
                ret.append(",");
            }
            
            return ret.toString();
        }
    }

    
    
    

    private static class AgendaEntry { 
        String dominator;
        Set<String> fragmentSet;
        
        AgendaEntry(String source, Set<String> target) {
            dominator = source;
            fragmentSet = target;
        }
        
        
        public String getDominator() {
            return dominator;
        }

        public Set<String> getFragmentSet() {
            return fragmentSet;
        }

        public String toString() {
            return "<Ag dom="+dominator+", fs=" + fragmentSet + ">";
        }
    }

    

    private static class Agenda extends Stack<AgendaEntry> {

		/**
		 * 
		 */
		private static final long serialVersionUID = 7426236767126350134L;
    }

}
