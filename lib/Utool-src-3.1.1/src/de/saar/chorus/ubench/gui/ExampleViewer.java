package de.saar.chorus.ubench.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.ComponentOrientation;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import de.saar.chorus.domgraph.ExampleManager;
import de.saar.chorus.domgraph.graph.DomGraph;
import de.saar.chorus.domgraph.graph.NodeLabels;
import de.saar.chorus.ubench.JDomGraph;

/**
 * A window showing the examples registered in the
 * <code>ExampleManager</code> of the running Utool
 * instance. 
 * A description of each example and the codec
 * belonging to it is shown.
 * 
 * @author Alexander Koller
 * @author Michaela Regneri
 *
 * @see de.saar.chorus.domgraph.ExampleManager
 */
public class ExampleViewer extends JFrame implements 
		ListSelectionListener, ActionListener {
	
	
	private static final long serialVersionUID = 8997638614124891904L;

	// layout of the main window
	private BorderLayout layout = new BorderLayout();

	// the split pane with the file list and the
	// descriptions
	private JSplitPane listContents; 
	
//	 the text component with the descriptions
	private JTextPane desc;	
	
	// the font used in this component
	private String fontname;
	
	// the list of example files
	private JList files;
	
	// the array underlying the file list
	private String[] exampleNames;
	
	private JButton load,	// button for loading
					cancel; // button for cancelling
	
	// scrollbars
	private JScrollPane listPane;
	private JScrollPane descriptionPane;
	
	// the ExampleManager responsible for
	// files and descriptions loaded
	private ExampleManager manager;
	
	/**
	 * This creates a new example viewer.
	 * If there are no examples, an IOException is thrown
	 * containing the message that there are no 
	 * examples in the utool path.
	 * 
	 * @throws IOException when there are no examples registered
	 */
	public ExampleViewer() throws IOException {
		super("Open Example");
		setLayout(layout);
		setAlwaysOnTop(true);
		
		// the manager used throughout the gui
		manager = Ubench.getInstance().getExampleManager();
		exampleNames = manager.getExampleNames().toArray(new String[] { });
		
		
		
		// initalising the list
		if( exampleNames.length > 0 ) {
			files = new JList(exampleNames);
			files.addListSelectionListener(this);
			files.addMouseListener(new DoubleClickAdapter());
			files.addKeyListener(new EnterAdapter());
			listPane = new JScrollPane(files);
			listPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
			
			
			JPanel preview = new JPanel();
			preview.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);

			
			load = new JButton("Load!");
			load.addActionListener(this);
			load.setActionCommand("loEx");
			load.setEnabled(false);
			preview.add(load);
			
			cancel = new JButton ("Cancel");
			cancel.addActionListener(this);
			cancel.setActionCommand("cancel");
			preview.add(cancel);
			
			desc = new JTextPane();
			
			desc.setText("No example selected.");
			
			desc.setEditable(false);
			desc.setBackground(Color.LIGHT_GRAY);
			desc.setOpaque(false);
			descriptionPane = new JScrollPane(desc);
			descriptionPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
			
			listContents = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, listPane, descriptionPane);
			listContents.setOneTouchExpandable(true);
			listContents.setDividerLocation(150);

			desc.setFont(files.getFont());
			fontname = files.getFont().getFamily();
			listPane.setMinimumSize(new Dimension(100, 50));
			descriptionPane.setPreferredSize(new Dimension(
					((int) (listPane.getPreferredSize().width * 2)), 
					descriptionPane.getPreferredSize().height ));
			descriptionPane.setMinimumSize(listPane.getPreferredSize());
			add(listContents,BorderLayout.CENTER);
			add(preview, BorderLayout.SOUTH);
		
			pack();
			validate();
		} else {
			throw new IOException("Utool couldn't find any examples.");
			
		}
		
	}
	
	/**
	 * Helper method that removes unnecessary whitespaces
	 * from the example descriptions (which contain
	 * very many of them).
	 * @param str
	 * @return
	 */
	private String killWhitespaces(String str) {
		return str.replaceAll("\\s+", " ");
	}
	
	
	/**
	 * Processes the event in which the user selects
	 * a new example by showing the appropriate 
	 * description.
	 */
	public void valueChanged(ListSelectionEvent e) {
		
		
		String selected = exampleNames[files.getSelectedIndex()];
		load.setEnabled(true);
		
		desc.setContentType("text/html");
		desc.setText("<html><div style='font-family:" + fontname + "; font-size:12pt'><b>Example " 
				 + selected + "</b><br>(Codec: " + 
				Ubench.getInstance().getCodecManager().
				getInputCodecNameForFilename(selected) +
				")<br><br>"+ 
				killWhitespaces(
						manager.getDescriptionForExample(selected)
						)
						+ "</div></html>");
			
		
		validate();
	}
	

	/**
	 * processes the events triggered by the buttons
	 * (loading an example or cancelling the dialog).
	 */
	public void actionPerformed(ActionEvent e) {
		
		if(e.getActionCommand().equals("loEx")) {
			// load the selected example
			final String selected = exampleNames[files.getSelectedIndex()];
			setVisible(false);
			Ubench.getInstance().getStatusBar().showProgressBar();
			new Thread(){
				public void run() {
					
					// loading the graph and converting it to a 
					// JDomGraph
					DomGraph theDomGraph = new DomGraph();
					NodeLabels labels = new NodeLabels();
                    
                    Ubench u = Ubench.getInstance();
                    
					JDomGraph graph = 
                        u.genericLoadGraph(u.getExampleManager().getExampleReader(selected), 
                                u.getCodecManager().getInputCodecNameForFilename(selected),
                                theDomGraph, labels, null);
					
					
					if( graph != null ) {
						
						//	DomGraphTConverter conv = new DomGraphTConverter(graph);
						
						// setting up a new graph tab.
						// the graph is painted and shown at once.
						Ubench.getInstance().addNewTab(graph, selected, theDomGraph, true, true, labels);
					}
				}
			}.start();
		} else if (e.getActionCommand().equals("cancel")) {
			setVisible(false);
			dispose();
		}
		
	}
	
	/**
	 * This provides the possibility to double click on an
	 * example (instead of pressing the "load" button).
	 * 
	 * @author Michaela Regneri
	 *
	 */
	private class DoubleClickAdapter extends MouseAdapter {
		
		@Override
		public void mouseClicked(MouseEvent e) {
			if(e.getClickCount() == 2 &&
					(files.getSelectedIndex() > -1)) {
				actionPerformed(new ActionEvent
						(load, ActionEvent.ACTION_PERFORMED, "loEx"));
			}
		}
		
	}
	
	/**
	 * This provides the possibility to press
	 * "enter" to load an example (instead of pressing
	 * the "load" button).
	 * @author Michaela Regneri
	 *
	 */
	private class EnterAdapter extends KeyAdapter {
		public void keyReleased(KeyEvent ke) {
			if( ke.getKeyCode() == KeyEvent.VK_ENTER
					&& (files.getSelectedIndex() > -1)) {
				actionPerformed(new ActionEvent
						(load, ActionEvent.ACTION_PERFORMED, "loEx"));
			}
			
			
		}
		
	}
	
}



