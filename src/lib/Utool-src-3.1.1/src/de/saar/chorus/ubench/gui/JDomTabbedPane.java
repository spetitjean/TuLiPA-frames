package de.saar.chorus.ubench.gui;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;


/**
 * A <code>JTabbedPane</code> and its <code>ChangeListener</code> 
 * to display several instances of <code>JDomGraphTab</code>.
 * 
 * @author Alexander Koller
 * @author Michaela Regneri
 *
 */

public class JDomTabbedPane extends JTabbedPane implements ChangeListener {
    
	/**
	 * 
	 */
	private static final long serialVersionUID = 1123628090923260178L;
	Action browse, browseBack;
	
	/**
	 * Setting up the pane by adding its listener and
	 * creating a popup (for the tabs).
	 *
	 * @param listener a listener for the popup dialog
	 */
	public JDomTabbedPane(CommandListener listener) {
		super();
		addChangeListener(this);
		setComponentPopupMenu(new JDomTabPopup(listener));
		
		/**
		 * TODO find out why this doesn't work at all...
		 */
		browse = new AbstractAction("browse") {
			/**
			 * 
			 */
			private static final long serialVersionUID = -9044721476670631615L;

			public void actionPerformed(ActionEvent e) {
				int i = getSelectedIndex();
				if(i < (getComponentCount() - 1)) {
					setSelectedIndex(i + 1);
				}
				else {
					setSelectedIndex(0);
				}
				Ubench.getInstance().refresh();
			}
		};
		getActionMap().getParent().put(browse.getValue(Action.NAME), browse);
		getInputMap().getParent().put(KeyStroke.getKeyStroke("ctrl LESS"), browse.getValue(Action.NAME));
		
		browseBack = new AbstractAction("back") {
			/**
			 * 
			 */
			private static final long serialVersionUID = -8001425669225741039L;

			public void actionPerformed(ActionEvent e) {
				int i = getSelectedIndex();
				if(i > 0) {
					setSelectedIndex(i - 1);
				}
				else {
					setSelectedIndex(getComponentCount() - 1);
				}
			        Ubench.getInstance().refresh();
			}
		};
		getActionMap().getParent().put(browseBack.getValue(Action.NAME), browseBack);
		getInputMap().getParent().put(KeyStroke.getKeyStroke("ctrl shift LESS"), browseBack.getValue(Action.NAME));
	}
    
	/**
	 * Copies all its shortcuts to a given component.
	 * This should provide the possibility to create
	 * "global" shortcuts, independent of the current selected
	 * component.
	 * 
	 * @param daughter the Component to copy the shortcuts to
	 */
	public void copyShortcuts(JComponent daughter) {
		InputMap iMap = daughter.getInputMap();
		ActionMap aMap = daughter.getActionMap();
		
	
		aMap.put(browse.getValue(Action.NAME), browse);
		iMap.put(KeyStroke.getKeyStroke("ctrl LESS"), browse.getValue(Action.NAME));

		aMap.put(browseBack.getValue(Action.NAME), browseBack);
		iMap.put(KeyStroke.getKeyStroke("ctrl shift LESS"), browseBack.getValue(Action.NAME));

	}
	
	/**
	 * Overwrites the <code>stateChanged</code> method of 
	 * <code>ChangeListener</code>.
	 * Just causes refreshing all visible GUI components.
	 */
	public void stateChanged(ChangeEvent e) {
		
		JGraphTab visibleTab = Ubench.getInstance().getVisibleTab();
		
      if ( ! (visibleTab == null) ) {
    	  Ubench.getInstance().refresh();
		  if(visibleTab.hasVisibleChartViewer()) {
			  visibleTab.focusChart();
		  }
		  validate();
      }
   }
}
