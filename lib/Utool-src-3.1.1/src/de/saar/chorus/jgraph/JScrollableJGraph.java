/*
 * @(#)JScrollableJGraph.java created 04.03.2006
 * 
 * Copyright (c) 2006 Alexander Koller
 *  
 */

package de.saar.chorus.jgraph;

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.jgraph.JGraph;

public class JScrollableJGraph extends JPanel {
  
	private static final long serialVersionUID = -4459883694973861984L;
	private JGraphSlider slider;
    protected JScrollPane scrollpane;
    
    public JScrollableJGraph(JGraph graph) {
        super(new BorderLayout());
        
        slider = new JGraphSlider(graph);
        scrollpane = new JScrollPane(graph);
       
        add(scrollpane, BorderLayout.CENTER);
        add(slider, BorderLayout.EAST);
        validate();
    }
    
    /**
     * Aligning the slider with the currently shown graph.
     * (if there is one).
     */
	public void resetSlider() {
		slider.resetSlider();
	}
}
