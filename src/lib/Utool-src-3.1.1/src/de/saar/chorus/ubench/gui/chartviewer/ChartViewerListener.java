package de.saar.chorus.ubench.gui.chartviewer;

import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import de.saar.chorus.domgraph.chart.Chart;
import de.saar.chorus.domgraph.chart.SolvedFormIterator;
import de.saar.chorus.domgraph.chart.Split;
import de.saar.chorus.domgraph.equivalence.EquationSystem;
import de.saar.chorus.domgraph.graph.DomGraph;
import de.saar.chorus.ubench.DomGraphTConverter;
import de.saar.chorus.ubench.JDomGraph;
import de.saar.chorus.ubench.gui.JSolvedFormTab;
import de.saar.chorus.ubench.gui.Ubench;

/**
 * This <code>ActionListener</code> processes all actions
 * of one (and only one) <code>ChartViewer</code>. 
 * 
 * @see de.saar.chorus.ubench.gui.chartviewer.ChartViewer
 * @author Michaela Regneri
 *
 */
public class ChartViewerListener implements ActionListener {

	// the chart viewer
	private ChartViewer viewer;

	
	/**
	 * A new <code>ChartViewerListener</code>
	 * initalised with its <code>ChartViewer</code>.
	 * 
	 * @param cv the chart viewer
	 */
	ChartViewerListener(ChartViewer cv) {
		viewer = cv;
	}
	
	/**
	 * This processes all events occuring within
	 * the <code>ChartViewer</code>.
	 */
	public void actionPerformed(ActionEvent e) {
		String command = e.getActionCommand();
		
		// redundancy elimination 
		if( command.equals("elredglobal") || command.equals("elred") ) {
			EquationSystem eqs = null;
			String name = null;
			
			// if the graph is not normal, abort with error message
			if( ! viewer.getDg().isNormal() ) {
				JOptionPane.showMessageDialog(viewer,
						"This is the chart of a graph which is not normal," + 
						System.getProperty("line.separator") + 
						"so Utool cannot eliminate redundancies.",
						"Server Error",
						JOptionPane.ERROR_MESSAGE);
				
				return;
			}
			
			// if the graph is not hnc, abort with error message
			if( ! viewer.getDg().isHypernormallyConnected()) {
				JOptionPane.showMessageDialog(viewer,
						"This is the chart of a graph which is not hypernormally" + 
						System.getProperty("line.separator") + 
						"connected, so Utool cannot eliminate redundancies.",
						"Server Error",
						JOptionPane.ERROR_MESSAGE);
				
				return;
			}
			
			// obtain the equation system and its name
			if( command.equals("elredglobal")) {
				// if the command was "reduce with global eq. system", get them from
				// Ubench
				eqs = Ubench.getInstance().getEquationSystem();
				name = Ubench.getInstance().getEqsname();
			} else {
				// otherwise, display a dialog that prompts for a filename and load
				// it from there
				eqs = new EquationSystem();
				name = loadEquationSystem(false, eqs);
			}
			
			// finally, reduce the chart and refresh the display
			if( (eqs != null) && (name != null)) {
				viewer.reduceChart(eqs, name);
				viewer.refreshChartWindow();
			}
			
			
		} else if( command.equals("delSplit") ) {
			// a Split was deleted
			
			Split selectedSplit = viewer.getSelectedSplit();
			if( selectedSplit != null ) {
				try {
					// remove the split from the chart itself
					Chart chart = viewer.getChart();
					Set<String> subgraph = viewer.getSubgraphForMarkedSplit();
					List<Split> splits = new ArrayList<Split>(chart.getSplitsFor(subgraph));
					
					splits.remove(selectedSplit);
					chart.setSplitsForSubgraph(subgraph, splits);
					viewer.refreshChartWindow();
					
				} catch(UnsupportedOperationException ex) {
					// a split which may not been removed
					JOptionPane.showMessageDialog(viewer,
							"You cannot delete the selected Split." + 
							System.getProperty("line.separator") + 
							"There has to be at least one Split left for each subgraph.",
							"Split not deleted",
							JOptionPane.ERROR_MESSAGE);
				}
				
			}
	
				
		} else if( command.equals("solvechart")) {
			// display the first solved form of the chart
			
			Chart chart = viewer.getChart();
			DomGraph firstForm = (DomGraph) viewer.getDg().clone();
			SolvedFormIterator sfi = new SolvedFormIterator(chart,firstForm);
			firstForm = firstForm.withDominanceEdges(sfi.next());
			
			DomGraphTConverter conv = new DomGraphTConverter(firstForm, viewer.getLabels());
			JDomGraph domSolvedForm = conv.getJDomGraph();
			
			JSolvedFormTab sFTab = new JSolvedFormTab(domSolvedForm, 
					viewer.getTitle()  + "  SF #1", 
					sfi, firstForm,
					1, chart.countSolvedForms().longValue(), 
					viewer.getTitle(), 
					Ubench.getInstance().getListener(), 
					viewer.getLabels());
			
			Ubench.getInstance().addTab(sFTab, true);
		} else if ( command.equals("resetchart") ) {
			// display the original chart again
			
			viewer.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			viewer.resetChart();
			viewer.setCursor(Cursor.getDefaultCursor());
		} else if ( command.equals("chartinfo") ) {
			// display information on the chart
			
			viewer.showInfoPane();
		} else if ( command.equals("closechart") ) {
			// close the window
			
			viewer.setVisible(false);
		}

	}
	
	/**
	 * This loads a xml file and reads the content
	 * to a xml file.
	 * 
	 * @param preliminary indicates whether or not to show the info message
	 * @param eqs the equation system to fill
	 * @return the (file) name of the equation system loaded
	 */
	private String loadEquationSystem(boolean preliminary, EquationSystem eqs) {
		String toReturn = null;
		
		/** TODO Why would we ever want to display this warning dialog?? - AK **/
		if(preliminary) {
			JOptionPane.showMessageDialog(viewer,
					"You have to specify a xml file that contains your equation system" + 
					System.getProperty("line.separator") + 
					" before Utool can eliminate equivalences.",
					"Please load an equation system",
					JOptionPane.INFORMATION_MESSAGE);
		}
		
		
		JFileChooser fc = new JFileChooser();

		fc.setDialogTitle("Select the equation system");
		fc.setFileFilter(Ubench.getInstance().getListener().new XMLFilter());
		fc.setCurrentDirectory(Ubench.getInstance().getLastPath());
		int fcVal = fc.showOpenDialog(viewer);	
		
		if(fcVal == JFileChooser.APPROVE_OPTION){
			
			viewer.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			
			File file = fc.getSelectedFile();
			
			try {
				eqs.read(new FileReader(file));
				toReturn = file.getName();
			} catch( Exception ex ) {
				JOptionPane.showMessageDialog(viewer,
						"The equation system cannot be parsed." + 
						System.getProperty("line.separator") + 
						"Either the input file is not readable, or it contains syntax errors.",
						"Error while loading equation system",
						JOptionPane.ERROR_MESSAGE);
			}


			Ubench.getInstance().setLastPath(file.getParentFile());
			viewer.setCursor(Cursor.getDefaultCursor());
			viewer.refreshTitleAndStatus();
		}
		
		return toReturn;
	}



}
