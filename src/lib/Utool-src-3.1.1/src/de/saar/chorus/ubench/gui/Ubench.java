/*
 * Created on 27.07.2004
 *
 */
package de.saar.chorus.ubench.gui;

import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Rectangle;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;

import de.saar.chorus.domgraph.ExampleManager;
import de.saar.chorus.domgraph.GlobalDomgraphProperties;
import de.saar.chorus.domgraph.codec.CodecManager;
import de.saar.chorus.domgraph.codec.InputCodec;
import de.saar.chorus.domgraph.codec.MalformedDomgraphException;
import de.saar.chorus.domgraph.codec.ParserException;
import de.saar.chorus.domgraph.equivalence.EquationSystem;
import de.saar.chorus.domgraph.graph.DomGraph;
import de.saar.chorus.domgraph.graph.NodeLabels;
import de.saar.chorus.domgraph.utool.ExitCodes;
import de.saar.chorus.ubench.DomGraphTConverter;
import de.saar.chorus.ubench.JDomGraph;
import de.saar.getopt.ConvenientGetopt;

/**
 * The main class of Ubench.
 * This implements the "singleton pattern", so this class
 * provides one (and only one) instance of Ubench. 
 * 
 * 
 * @author Alexander Koller
 * @author Michaela Regneri
 * 
 */
public class Ubench {
    private static Ubench instance = null;
    

    
    private File lastPath;
    // the tabs in their order of insertion
    private ArrayList<JGraphTab> tabs = new ArrayList<JGraphTab>();
    
    // the number of times that tabs with this name were created so far
    private Map<String,Integer> numTabsWithThisName = new HashMap<String,Integer>();
    
    // the pane containing the tabs
    private JDomTabbedPane tabbedPane;
    
    // the status bar
    private JDomGraphStatusBar statusBar;
    
    // the manager for tooltips (needed to shorten the stanard delay)
    private ToolTipManager ttm = ToolTipManager.sharedInstance();
    
    // the main window
    private JFrame window;
    
    // the main listener for menus and buttons
    private CommandListener listener;
    
    // the menu bar
    private JDomGraphMenu menuBar;
    
    // the codec manager
    private CodecManager codecManager;
    
    private ExampleManager exampleManager;
    
    // if true, the next tab addition will resize the main jframe
    // to fit the preferred size of this tab
    private boolean useNextTabToResizeFrame;
    
    private EquationSystem eqs;
    private String eqsname;
    
    private JDomGraphPreferencePane settings;
    
    public boolean reduceAutomatically;
    
    /**
     * Setting up a new Ubench object. 
     *
     */
    private Ubench() {
    	// register codecs
        codecManager = new CodecManager();
        codecManager.setAllowExperimentalCodecs(GlobalDomgraphProperties.allowExperimentalCodecs());
        registerAllCodecs(codecManager);
        
        lastPath = new File(System.getProperty("user.home"));
        try {
            exampleManager = new ExampleManager();
            exampleManager.addAllExamples("examples");
            exampleManager.addAllExamples("projects/Domgraph/examples");
         
        } catch (de.saar.chorus.domgraph.ExampleManager.ParserException e) {
            JOptionPane.showMessageDialog(window,
            		"A parsing error occurred " +
            		"while reading an examples declaration." + 
            		System.getProperty("line.separator") + 
            		e + " (cause: " + e.getCause() + ")");
        	
            System.exit(ExitCodes.EXAMPLE_PARSING_ERROR);
        }

        // Set look and feel. Currently we are only setting the Windows L&F, as
        // the GTK L&F (for Linux) looks ugly even on Java 6.0, and on MacOS even
        // the standard L&F looks good.
        try {
			UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
		} catch (Exception e) {
		}
		

        
        eqs = null;
        eqsname = null;
        reduceAutomatically = false;
        settings = new JDomGraphPreferencePane();
    }
    
    /**
     * Aligning the slider with the currently shown graph. (if there is one).
     */
    public void resetSlider() {
        if (getVisibleGraph() != null) {
            getVisibleTab().resetSlider();
        }
    }
    
    public void setPreferenceDialogVisible(boolean visible) {
    	settings.setVisible(visible);
    }
    
    /**
     * Showing the bar for the solving process.
     */
    public void showProgressBar() {
        statusBar.showProgressBar();
    }
    
    /**
     * @return the main window itself
     */
    public JFrame getWindow() {
        return window;
    }
    
    /**
     * Closing all tabs.
     */
    public void closeAllTabs() {
        tabbedPane.removeAll();
        tabs.clear();
        refresh();
    }
    
    /**
     * Closing Ubench.
     */
    public void quit() {
        System.exit(0);
    }
    
    /**
     * Returning the tab height (considering the possible minimization of the
     * window).
     * 
     * @return the height
     */
    public double getTabHeight() {
        int index = tabbedPane.getSelectedIndex();
        if (index > -1) {
            Rectangle tabRect = tabbedPane.getBoundsAt(tabbedPane
                    .getSelectedIndex());
            return tabbedPane.getHeight() - tabRect.height;
        }
        
        return window.getHeight() - 100;
    }
    
    /**
     * Returning the tab width (considering the possible minimization of the
     * window).
     * 
     * @return the width
     */
    public double getTabWidth() {
       
        return tabbedPane.getWidth();
    }
    
    /**
     * Refreshes the menu, the slider and the status bar.
     */
    public void refresh() {
        if (getVisibleTab() != null) {
            getVisibleTab().repaintIfNecessary();
            if(getVisibleTab().getClass() == JSolvedFormTab.class ) {
                setSolvingEnabled(false);
                menuBar.setSaveAllEnabled(false);
            } else {
                if( ((JDomGraphTab) getVisibleTab()).isSolvable()) {
                    setSolvingEnabled(true);
                    menuBar.setSaveAllEnabled(true);
                } else {
                    setSolvingEnabled(false);
                    menuBar.setSaveAllEnabled(false);
                }
                menuBar.setPlusMinusEnabled(false,false);
            }
        } else {
            menuBar.setGraphSpecificItemsEnabled(false);
        }
        resetSlider();
        statusBar.refresh();
    }
    
    /**
     * Closes the currently shown tab (if there is one).
     */
    public void closeCurrentTab() {
        int index = tabbedPane.getSelectedIndex();
        
        if (index > -1) {
            tabbedPane.remove(index);
            tabs.remove(index);
            
        }
        refresh();
    }
    
    /**
     * @return the currently shown tab or null if there is none
     */
    public JGraphTab getVisibleTab() {
        int index = tabbedPane.getSelectedIndex();
        
        if (index > -1)
            return (tabs.get(tabbedPane.getSelectedIndex()));
        else
            return null;
    }
    
    /**
     * 
     * @return the index of the currently shown tab
     */
    public int getVisibleTabIndex() {
        return tabbedPane.getSelectedIndex();
    }
    
    /**
     * Adding a complete tab to the window
     * 
     * @param tab
     *            the tab to ad
     * @param showNow
     *            if set to true, the tab will be displayed at once
     */
    public void addTab(JGraphTab tab, boolean showNow) {
        
        addTab(tab, showNow, tabbedPane.getTabCount());
        
    }
    
    /**
     * Adding a complete tab to the window
     * 
     * @param tab
     *            the tab to ad
     * @param showNow
     *            if set to true, the tab will be displayed at once
     */
    public void addTab(JGraphTab tab, boolean showNow, int ind) {
    	
    	
    	if(tab != null && (! tab.isEmpty())) {
    		int index;
    		tabs.add(ind, tab);
    		// registering the tab
    		if (ind < (tabs.size() - 1)) {
    			index = ind;
    			
    			tabbedPane.insertTab(tab.getDefaultName(), null, 
    					tab, tab.getDefaultName(), index);
    			
    		} else {
    			tabbedPane.addTab(tab.getDefaultName(), tab);
    			//tabs.add(tab);
    			index = tabs.size() - 1;
    		}
    		
    		
    		ttm.registerComponent(tab.getGraph());
    		
    		// if it's the first tab added, the graph menus get enabled
    		if (tabbedPane.getTabCount() == 1) {
    			menuBar.setGraphSpecificItemsEnabled(true);
    		}
    		
    		// if the tab shall be shown, the selected index is
    		// the last one
    		if (showNow) {
    			tabbedPane.setSelectedIndex(index);
    		}
    		
    		
    		
    		tabbedPane.validate();
    		
    		// aligning with preferences...
    		
    		// fitting?
    		if (Preferences.isFitToWindow())
    			tab.fitGraph();
    		
    		// solving?
    		if (Preferences.isAutoCount()) {
    			
    			statusBar.showProgressBar();
    			
    		}
    		
    		refresh();
    		
    		if( useNextTabToResizeFrame  ) {
    			// This is the first tab we're opening; use its size to resize the Ubench window.
    			// We try to make the window just big enough to fit the graph, but no bigger
    			// than the screen size.
    			
    			GraphicsEnvironment env =
    	        	GraphicsEnvironment.getLocalGraphicsEnvironment();
    			Rectangle bounds = env.getMaximumWindowBounds();
    			
    			window.pack();
    			
    			Dimension graphsize = window.getSize();
    			Dimension windowsize = new Dimension(Math.min(bounds.width, graphsize.width), Math.min(bounds.height, graphsize.height));
    			
    			window.setSize(windowsize);
    			window.validate();
    			
    			useNextTabToResizeFrame = false;
    		}
    		
    	}
    	
    }
    
    /**
     * (De-)Activates the menu-items only available for
     * solvable dominance graphs (as opposed to unsolvable
     * graphs and solved forms). 
     * 
     * @param b if false, solving becomes disabled. 
     */
    public void setSolvingEnabled(boolean b) {
        menuBar.setSolvingEnabled(b);
    }
    
    /**
     * Adding a new tab to the window displaying the given
     * <code>JDomGraph</Code>
     * 
     * @param graph the graph to display
     * @param label the name for the tab
     * @param paintNow if set to true, the graph is layoutet at once
     * @param showNow if set to true, the tab will be shown after creating
     * @return the tab or null if an error occured while setting up the tab
     */
    public JDomGraphTab addNewTab(JDomGraph graph, String label,
            DomGraph origin, boolean paintNow, boolean showNow,
            NodeLabels labels) {
        
        String normalisedLabel = normaliseTabLabel(label);
        
        // the new tab
        JDomGraphTab tab = new JDomGraphTab(graph, origin, normalisedLabel, paintNow,
                listener, labels);
        if (tab.getGraph() != null) {
            
            // tab sucessfully created
            addTab(tab, showNow);
            return tab;
        } else {
            // something went wrong (the tab contains no graph)
            return null;
        }
        
    }
    
    /**
     * 
     * @param label
     * @return
     */
    private String normaliseTabLabel(String label) {
        String stripSlashes = new File(label).getName();
        
        if( numTabsWithThisName.containsKey(stripSlashes)) {
            int next = numTabsWithThisName.get(stripSlashes) + 1;
            String ret = stripSlashes + "/" + next;
            numTabsWithThisName.put(stripSlashes, next);
            
            return ret;
        } else {
            numTabsWithThisName.put(stripSlashes,1);
            return stripSlashes;
        }
    }
    
    /**
     * Allowes to set up a new Tab by submitting the 
     * <code>DomGraph</code> to display and a <code>NodeLabels</code>
     * object along with the graph's name.
     * 
     * @param label name of the graph (resp. the tab)
     * @param graph the <code>DomGraph</code> to display
     * @param labels the storage for the node labels
     * @return true if the <code>DomGraph</code> was sucessfully 
     * 		   translated into a <code>JDomGraph</code>
     */
    public boolean addNewTab(String label, DomGraph graph, NodeLabels labels) {
        
        DomGraphTConverter conv = new DomGraphTConverter(graph, labels);
        JDomGraph jDomGraph = conv.getJDomGraph();
        if(jDomGraph == null)
            return false;
        
        JDomGraphTab tab = new JDomGraphTab(jDomGraph, graph, normaliseTabLabel(label),
                true, listener, labels);
        addTab(tab, true);
        return true;
    }
    
    /**
     * Adding a new tab to the window displaying the given
     * <code>JDomGraph</Code>, given the index indicating
     * where to insert it in the tab.
     * 
     * @param graph the graph to display
     * @param label the name of the tab
     * @param paintNow if set to true the graph is layoutet at once
     * @param showNow if set to true the graph is shown immidiately
     * @param index indicating on which place of the tab the new tab shall be inserted
     * @return the new tab or null if anything fails
     */
    public JDomGraphTab addNewTab(JDomGraph graph, String label,
            DomGraph origin, boolean paintNow, boolean showNow, int index,
            NodeLabels labels) {
        
        // the new tab
        JDomGraphTab tab = new JDomGraphTab(graph, origin, normaliseTabLabel(label),
                paintNow, listener, labels);
        if (tab.getGraph() != null) {
            
            // tab sucessfully created
            addTab(tab, showNow, index);
            return tab;
        } else {
            // something went wrong (the tab contains no graph)
            return null;
        }
        
    }
    
    /**
     * @return the <code>JDomGraph</code> currently displayed
     */
    private JDomGraph getVisibleGraph() {
        int index = tabbedPane.getSelectedIndex();
        
        if (index > -1)
            return (tabs.get(tabbedPane.getSelectedIndex()).getGraph());
        else
            return null;
    }

    /**
     * Loads a labelled dominance graph from a reader.
     * 
     * @param reader the Reader from which the graph is read
     * @param codec the name of the input codec that should be used
     * to decode the graph 
     * @param graph a <code>DomGraph</code> which this method sets
     * to the dominance graph part of the labelled graph
     * @param nl a <code>NodeLabels</code> object which this method
     * fills with the node labelling part of the labelled graph
     * @return a new <code>JDomGraph</code> representation for the
     * labelled graph
     */
    public JDomGraph genericLoadGraph(Reader reader, String codec, 
    		DomGraph graph, NodeLabels nl, Map<String, String> options) {
    	InputCodec inputCodec;
    	if(options != null ) {
    		inputCodec = 
            codecManager.getInputCodecForName(codec, options);
    	} else {
    		inputCodec = codecManager.getInputCodecForName(codec, "");
    	}
        if(inputCodec != null ) {
            try {
                inputCodec.decode(reader, graph, nl);
                
            } catch (IOException e) {
                JOptionPane
                .showMessageDialog(
                        window,
                        "The specified file doesn't exist or cannot be opened.",
                        "Error during import", JOptionPane.ERROR_MESSAGE);
                //e.printStackTrace();
                return null;
            } catch (ParserException pe) {
                JOptionPane
                .showMessageDialog(
                        window,
                        "A parsing error occurred while reading the input file:\n"
                        + pe,
                        "Error during import", JOptionPane.ERROR_MESSAGE);
                
                return null;
            } catch (MalformedDomgraphException me) {
                JOptionPane
                .showMessageDialog(
                        window,
                        "A semantic error occurred while decoding the graph:\n" +
                        me,
                        "Error during import", JOptionPane.ERROR_MESSAGE);
                
                return null;
            }
        } else {
            JOptionPane
            .showMessageDialog(
                    window,
                    "The filename extension of this file is not associated with any known input codec.",
                    "Error during import", JOptionPane.ERROR_MESSAGE);
            
            return null;
        }
        
        DomGraphTConverter conv = new DomGraphTConverter(graph, nl);
        return conv.getJDomGraph();
    }
    

    
    /**
     * Loads a labelled dominance graph from a file.
     * 
     * @param filename the file name
     * @param graph a <code>DomGraph</code> which this method sets
     * to the dominance graph part of the labelled graph
     * @param nl a <code>NodeLabels</code> object which this method
     * fills with the node labelling part of the labelled graph
     * @return a new <code>JDomGraph</code> representation for the
     * labelled graph
     */
    public JDomGraph genericLoadGraph(String filename, DomGraph graph, 
    		NodeLabels nl, Map<String,String> options) {
        try {
            return genericLoadGraph(new InputStreamReader(new FileInputStream(filename)),
                    codecManager.getInputCodecNameForFilename(filename),
                    graph, nl, options);
        } catch (IOException e) {
            JOptionPane
            .showMessageDialog(
                    window,
                    "The specified file doesn't exist or cannot be opened.",
                    "Error during import", JOptionPane.ERROR_MESSAGE);
            return null;
        }
    }
    
    /**
     * Starting Ubench, optionally with files to open on command line
     * 
     * @param args
     *            command line arfuments
     */
    public static void main(String[] args) {
        
        
        
        
        // parse command-line arguments
        ConvenientGetopt getopt = new ConvenientGetopt("Ubench",
                "java -jar Ubench.jar [options] [filename]",
                "If Ubench doesn't run in server mode, specify a filename on the command line"
                + "\nto display the graph.");
        
        getopt.parse(args);
        
        // extract arguments
        
        // load files that were specified on the command line
        for (String file : getopt.getRemaining()) {
            DomGraph anotherGraph = new DomGraph();
            NodeLabels labels = new NodeLabels();
            JDomGraph graph = getInstance().genericLoadGraph(file, anotherGraph, labels, null);
            if (graph != null) {
                // DomGraphTConverter conv = new DomGraphTConverter(graph);
                JDomGraphTab firstTab = getInstance().addNewTab(graph, (new File(file))
                        .getName(), anotherGraph, true, false, labels);
                
            }
        }
        
    }
    
   
    /**
     * Create a new JFrame window. The application will be terminated once this
     * window is closed.
     * 
     * @return the new window
     */
    private JFrame makeWindow() {
        JFrame f = new JFrame("JGraph Test");
        
        f.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });
        
        GraphicsEnvironment env =
        	GraphicsEnvironment.getLocalGraphicsEnvironment();
		Rectangle bounds = env.getMaximumWindowBounds();
		f.setMaximizedBounds(bounds);
		
        return f;
    }
    
    JDomGraphMenu getMenuBar() {
        return menuBar;
    }
    
    JDomGraphStatusBar getStatusBar() {
        return statusBar;
    }
    
    /**
     * @return Returns the listener.
     */
    public CommandListener getListener() {
        return listener;
    }
    
    /**
     * @param listener
     *            The listener to set.
     */
    public void setListener(CommandListener listener) {
        this.listener = listener;
    }
    
    /**
     * Registers all utool-codecs to the given
     * <code>CodecManager</code>. 
     * 
     * @param codecManager
     */
    private void registerAllCodecs(CodecManager codecManager) {
        try {
            codecManager.registerAllDeclaredCodecs();
        } catch(Exception e) {
        	JOptionPane.showMessageDialog(window,
        			"An error occurred trying to register a codec." +
        			System.getProperty("line.separator") + 
        			e + " (cause: " + e.getCause() + ")",
        			"Codec error",
        			JOptionPane.ERROR_MESSAGE
        			);
           
            System.exit(ExitCodes.CODEC_REGISTRATION_ERROR);
        }
        
    }
    
    /**
     * 
     * @return the codecManager
     */
    public CodecManager getCodecManager() {
        return codecManager;
    }
    
    /**
     * 
     * @return the exampleManager
     */
    public ExampleManager getExampleManager() {
        return exampleManager;
    }
    
   
	
	/**
     * Sets up a new window after having created 
     * a ubench instance.
     *
     */
    private void initialise() {
//      set up the window
    	
    	GridBagLayout layout = new GridBagLayout();
        window = makeWindow();
        window.setLayout(layout);
        
        listener = new CommandListener();
        tabbedPane = new JDomTabbedPane(listener);
        menuBar = new JDomGraphMenu(listener);
        window.setJMenuBar(menuBar);
        statusBar = new JDomGraphStatusBar(listener);
        
        
        // ttm.registerComponent(slider);
        ttm.registerComponent(statusBar);
        ttm.setInitialDelay(100);
        ttm.setLightWeightPopupEnabled(false);
        
        
        GridBagConstraints tpConstraints = new GridBagConstraints();
       // tpConstraints.anchor = GridBagConstraints.CENTER;
        tpConstraints.fill = GridBagConstraints.BOTH;
        tpConstraints.weighty = 5.0;
        tpConstraints.weightx = 1.0;
        
        GridBagConstraints sBConstraints = new GridBagConstraints();
        sBConstraints.gridy = GridBagConstraints.RELATIVE;
       
        sBConstraints.gridx = 0;
        sBConstraints.fill = GridBagConstraints.BOTH;
        sBConstraints.weighty = 0;
        sBConstraints.weightx = 0;
       
        
        layout.setConstraints(tabbedPane, tpConstraints);
        layout.setConstraints(statusBar, sBConstraints);
       
        
        window.add(tabbedPane);
        window.add(statusBar);
        
        // tabbedPane.copyShortcuts(slider);
        tabbedPane.copyShortcuts(statusBar);
        
        menuBar.setGraphSpecificItemsEnabled(false);
        
        window.setTitle("Underspecification Workbench");
        
        window.doLayout();
        window.setFocusable(true);
        window.setVisible(true);
        
        useNextTabToResizeFrame = true;
        window.pack();
        
        // start the window at a default window size that's not totally empty
        // (it will be resized once the first tab is opened)
        window.setSize(300,200);
        
        window.validate();
    }
    
    
    
    /**
     * Returns the (only) instance of the <code>Ubench</code>
     * class. Creates a new <code>Ubench</code> object if there
     * was none created yet, returns the already created instance
     * otherwise.
     * 
     * @return the single <code>Ubench</code> instance
     */
    public static Ubench getInstance() {
        if( instance == null ) {
            instance = new Ubench();
            instance.initialise();
        }
        
        return instance;
    }

	/**
	 * @return Returns the eqs.
	 */
	public EquationSystem getEquationSystem() {
		return eqs;
	}

	/**
	 * @param eqs The eqs to set.
	 */
	public void setEquationSystem(EquationSystem eqs, String name) {
		eqsname = name;
		this.eqs = eqs;
		if(this.eqs != null) {
			menuBar.setReduceAutomaticallyEnabled(true);
		}
	}
	
	
	
	/**
	 * @return Returns the eqsname.
	 */
	public String getEqsname() {
		return eqsname;
	}

	
	public boolean isEquationSystemLoaded() {
		return eqs != null;
	}

	/**
	 * @return Returns the lastPath.
	 */
	public File getLastPath() {
		return lastPath;
	}

	/**
	 * @param lastPath The lastPath to set.
	 */
	public void setLastPath(File lastPath) {
		this.lastPath = lastPath;
	}

	/**
	 * 
	 * @return a list of all the tabs loaded
	 */
	List<JGraphTab> getTabs() {
		return tabs;
	}
	

	
}

/**
 * 
 * test inputs to send over the socket:
 * 
 * 
 * 
 * <gxl xmlns:xlink="http://www.w3.org/1999/xlink"><graph id="chain2"
 * edgeids="true" hypergraph="false" edgemode="directed"><!-- OF 1 --><node
 * id="X"><type xlink:href="root" /><attr name="label"><string>f</string></attr><popup
 * id="foo" label="Foo Foo" /><popup id="bar" label="Bar Bar" /><popup
 * id="baz" label="Baz Baz" /></node><node id="X1"><type xlink:href="hole" /><popup
 * id="foo" label="Foo Foo" /><popup id="bar" label="Bar Bar" /><popup
 * id="baz" label="Baz Baz" /></node><edge from="X" to="X1" id="gridy-x1"><type
 * xlink:href="solid" /></edge><!-- OF 2 --><node id="Y"><type
 * xlink:href="root" /><attr name="label"><string>g</string></attr><popup
 * id="foo" label="Foo Foo" /><popup id="bar" label="Bar Bar" /><popup
 * id="baz" label="Baz Baz" /></node><node id="Y1"><type xlink:href="hole" /><popup
 * id="foo" label="Foo Foo" /><popup id="bar" label="Bar Bar" /><popup
 * id="baz" label="Baz Baz" /></node><edge from="Y" to="Y1" id="Y-Y1"><type
 * xlink:href="solid" /></edge><!-- UF 1 --><node id="Z"><type
 * xlink:href="root" /><attr name="label"><string>a</string></attr><popup
 * id="foo" label="Foo Foo" /><popup id="bar" label="Bar Bar" /><popup
 * id="baz" label="Baz Baz" /></node><!-- dominances --><edge from="X1"
 * to="Z" id="x1-z"><type xlink:href="dominance" /></edge><edge from="Y1"
 * to="Z" id="y1-z"><type xlink:href="dominance" /></edge></graph></gxl>
 * <gxl xmlns:xlink="http://www.w3.org/1999/xlink"><graph id="chain2b"
 * edgeids="true" hypergraph="false" edgemode="directed"><!-- OF 1 --><node
 * id="X"><type xlink:href="root" /><attr name="label"><string>g</string></attr><popup
 * id="foo" label="Foo Foo" /><popup id="bar" label="Bar Bar" /><popup
 * id="baz" label="Baz Baz" /></node><node id="X1"><type xlink:href="hole" /><popup
 * id="foo" label="Foo Foo" /><popup id="bar" label="Bar Bar" /><popup
 * id="baz" label="Baz Baz" /></node><edge from="X" to="X1" id="gridy-x1"><type
 * xlink:href="solid" /></edge><!-- OF 2 --><node id="Y"><type
 * xlink:href="root" /><attr name="label"><string>h</string></attr></node><node
 * id="Y1"><type xlink:href="hole" /><popup id="foo" label="Foo Foo" /><popup
 * id="bar" label="Bar Bar" /><popup id="baz" label="Baz Baz" /></node><edge
 * from="Y" to="Y1" id="Y-Y1"><type xlink:href="solid" /></edge><!-- UF 1 --><node
 * id="Z"><type xlink:href="root" /><attr name="label"><string>b</string></attr><popup
 * id="foo" label="Foo Foo" /><popup id="bar" label="Bar Bar" /><popup
 * id="baz" label="Baz Baz" /></node><!-- dominances --><edge from="X1"
 * to="Z" id="x1-z"><type xlink:href="dominance" /></edge><edge from="Y1"
 * to="Z" id="y1-z"><type xlink:href="dominance" /></edge></graph></gxl>
 * 
 * 
 */

