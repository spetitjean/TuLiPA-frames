package de.saar.chorus.ubench;

import org.jgraph.graph.DefaultEdge;
import org.jgraph.graph.DefaultGraphCell;

public class DummyPopupListener implements DomGraphPopupListener {

	/* (non-Javadoc)
	 * @see de.saar.chorus.ubench.DomGraphPopupListener#popupSelected(org.jgraph.graph.DefaultGraphCell, de.saar.chorus.ubench.Fragment, java.lang.String)
	 */
	public void popupSelected(DefaultGraphCell source, Fragment fragment, String menuItem) {


        if( source instanceof DefaultEdge ) {
            System.err.println("Click (edge): " + fragment.getParent().getEdgeData((DefaultEdge) source).getName()
                    	+ ", fragment = " + fragment.getMenuLabel() 
                    	+ ", menuId = " + menuItem);
        } else {
            System.err.println("Click (node): " +  fragment.getParent().getNodeData(source).getName()
                    + ", fragment = " + fragment.getMenuLabel()
                    + ", menuId = " + menuItem);
        }
		
	}

}
