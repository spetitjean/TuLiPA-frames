package de.saar.chorus.ubench.gui;

import java.awt.BorderLayout;
import java.awt.CardLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

/**
 * The status bar of the workbench window, a
 * <code>JPanel</code> containing the possible layouts 
 * (for dominance graphs, solved forms, the solving process 
 * and an empty window) managed with a <code>CardLayout</code>.
 * 
 * @author Alexander Koller
 * @author Michaela Regneri
 *
 */
public class JDomGraphStatusBar extends JPanel {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 357657081130185650L;



	// several panels
	private JPanel progressPanel,	// showed while solving
				   emptyPanel;  	// showed if there is no graph.
	
	
	
	private JLabel emptyLabel;
	
	// layout of the "root" JPanel
	private CardLayout layout;

	
	// progress bar for solving process
	private JProgressBar progressBar;
	
    // counter to generate unique names in CardLayout
    private long label;
	
    /**
     * Initializing the status bar with the given listener
     * and setting up the different layouts.
     * 
     * @param listener the listener for the status bar
     */
	public JDomGraphStatusBar(CommandListener listener) {
		super();
        
		label = 0;
		layout = new CardLayout();
		setLayout(layout);
        
		emptyLabel = new JLabel("There is no graph to show.");
		emptyPanel = new JPanel();
		emptyPanel.add(emptyLabel, BorderLayout.CENTER);
		layout.addLayoutComponent(emptyPanel,"empty");
		add(emptyPanel,"empty");
		
		// progress bar for solving
		// TODO make that smaller...
		progressPanel = new JPanel();
		progressBar = new JProgressBar(0, 1);
		progressBar.setStringPainted(true); 
		
		
		progressBar.setString("Loading...");
		
		progressBar.setIndeterminate(true);
		progressPanel.add(progressBar);
		
		layout.addLayoutComponent(progressPanel,"progress");
		add(progressPanel,"progress");
		
		showEmptybar();
	}
	
	
	
	/**
	 * Makes the layout show the bar for
	 * empty windows.
	 */
	public void showEmptybar() {

		layout.show(this,"empty");
		validate();
	}
	
	
	/**
	 * Refreshes this. Depending on which kind of graph
	 * is shown (or not shown), a layout is chosen. Additionally,
	 * the classify-symbols are refreshed if necessary.
	 *
	 */
	public void refresh() {
		
		// empty window
		if(Ubench.getInstance().getVisibleTab() == null) {
			showEmptybar();
		} else {
			showBar(Ubench.getInstance().getVisibleTab().getBarCode());
		}
	}
    
	
    /**
     * Makes the layout show the bar indicatin that
     * the solving process is running.
     */
    public void showProgressBar() {
    	layout.show(this, "progress");
    }

   
     /**
      * 
      * @param newBar
      * @return
      */
     String insertBar(JPanel newBar) {
    	 
    	 String layoutLabel = String.valueOf(label);
    	 label++;
    	 
    	 layout.addLayoutComponent(newBar, layoutLabel);
    	 add(newBar, layoutLabel);
    	 
    	 return layoutLabel;
     }
     
     /**
      * 
      * @param lab
      */
     void showBar(String lab) {
    	 layout.show(this, lab);
    	 validate();
     }
     
     /**
      * 
      * @param toRemove
      */
     void removeBar(JPanel toRemove) {
    	 layout.removeLayoutComponent(toRemove);
    	 remove(toRemove);
     }
     
}
