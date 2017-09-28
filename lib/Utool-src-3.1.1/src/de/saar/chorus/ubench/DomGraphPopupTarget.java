/*
 * Created on 03.08.2004
 *
 */
package de.saar.chorus.ubench;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

/**
 * Objects of this class represent nodes, edges, or fragments which can
 * display a popup menu when the user right-clicks on them.
 * 
 * When a DomGraphPopupTarget object is created, it needs to know the
 * JDomGraph object to which it belongs. The JDomGraph object is responsible
 * for displaying the popup menu and for notifying listeners that wait for
 * popup selection events. Typically, this popup menu will contain one menu
 * entry for each popup target (node, fragment, etc.) under the mouse cursor.
 * 
 * The actual menu of the popup target can be populated by calls to the 
 * addMenuItem method. Each menu item gets an ID and a label: The label is
 * displayed in the popup menu itself, but the ID is sent to listeners
 * when a user clicks on the menu item.
 * 
 * If the popup target's menu is used as a submenu
 * in e.g. a popup menu (as we expect), its menu label in the supermenu is 
 * taken from the getMenuLabel() method, which concrete subclasses must override.
 * 
 * @author Alexander Koller
 *
 */
abstract class DomGraphPopupTarget implements ActionListener {
    private JMenu menu;
    private JDomGraph parent;
    
    private Map<Object,String> itemsToIDs;  // menu item -> ID of this menu item

    
    /**
     * Create a new popup target.
     * 
     * @param parent the JDomGraph object to which the popup target belongs.
     */
    public DomGraphPopupTarget(JDomGraph parent) {
        this.parent = parent;
        menu = null;
        itemsToIDs = new HashMap<Object,String>();
    }


    /**
     * Return the JMenu object that represents this popup target's submenu.
     * 
     * @return the JMenu object.
     */
    public JMenuItem getMenu() {
        return menu;
    }


    /**
     * Adds a menu item to the popup target's menu. The item has an ID and a label.
     * The label specifies what is displayed when the menu item is drawn.
     * The ID is sent to the popup listeners when the user clicks on the item.
     * 
     * @param id an identification for this menu item
     * @param label the label that will be displayed for this menu item.
     */
    public void addMenuItem(String id, String label) {
        if( menu == null ) {
            menu = new JMenu(getMenuLabel());
        }
    
        JMenuItem item = new JMenuItem(label); 
        menu.add(item);
        itemsToIDs.put(item, id);
        item.addActionListener(this);
    }
    
    /* (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent e) {
        parent.notifyPopupListeners(itemsToIDs.get(e.getSource()));
    }


    /**
     * Returns the label of the menu item for this popup target in
     * a superordinate menu.
     * 
     * @return the menu item label.
     */
    public abstract String getMenuLabel();
    
	/**
	 * @return Returns the parent.
	 */
	public JDomGraph getParent() {
		return parent;
	}
}
