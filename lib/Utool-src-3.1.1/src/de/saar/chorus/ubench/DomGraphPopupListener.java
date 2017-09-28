/*
 * Created on 03.08.2004
 *
 */
package de.saar.chorus.ubench;

import org.jgraph.graph.DefaultGraphCell;

/**
 * Listeners that implement this interface can be registered in a JDomGraph
 * object to be called every time the user selects an item from a popup menu.
 * 
 * @author Alexander Koller
 *
 */
public interface DomGraphPopupListener {
    /**
     * Process a popup-selection event. This method is called for each 
     * registered listener whenever the user clicks on a popup menu item.
     * 
     * @param source the node or edge to which the popup menu belonged.
     * @param fragment the fragment to which this node or edge belonged (or null).
     * @param menuItem the label of the menu item that the user selected.
     */
    void popupSelected(DefaultGraphCell source, Fragment fragment, String menuItem);
}
