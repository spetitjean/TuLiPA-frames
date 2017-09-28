/*
 *  File GraphExplorer.java
 *
 *  Authors:
 *     Johannes Dellert
 *     
 *  Copyright:
 *     Johannes Dellert, 2009
 *
 *  Last modified:
 *     Do 16. Apr 09:55:36 CEST 2009
 *
 *  This file is part of the TuLiPA system
 *     http://www.sfb441.uni-tuebingen.de/emmy-noether-kallmeyer/tulipa
 *
 *  TuLiPA is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  TuLiPA is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package de.tuebingen.gui.graph;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;

public class GraphExplorer extends JFrame
{
	private static final long serialVersionUID = 7101664632564544762L;
	GraphModel g;
    public GraphDisplay p;
    JScrollPane pScrollPane;
    
    public GraphExplorer(GraphModel g)
    {
        this.g = g;
        p = new GraphDisplay(g);
        pScrollPane = new JScrollPane(p,JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        getContentPane().add(pScrollPane);
        
        setTitle("GraphExplorer");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
   
    public class GraphDisplay extends JPanel implements MouseListener, MouseMotionListener
    {
		private static final long serialVersionUID = 3991974017336466150L;
		GraphModel g;
        GraphModel mg;
        HashMap<Integer,HashMap<String,Integer>> coordinates;
        HashMap<String,Integer> accessTable; 
        int outerBoundX;
        int outerBoundY;
        int movedVertex;
        
        public GraphDisplay(GraphModel g)
        {
            this.mg = g;
            if (g.vertices.size() < 1000)
            {
                this.g = mg;
            }
            else
            {
                Iterator<Integer> i = g.vertices.keySet().iterator();
                this.g = mg.getClosure(i.next(), 5);
                while (this.g.vertices.size() < 100 && i.hasNext())
                {
                    this.g = mg.getClosure(i.next(), 5);
                }
            }
            outerBoundX = 0;
            outerBoundY = 0;
            movedVertex = -1;
            calculateCoordinates();
            calculateAccessTable();  
            this.addMouseListener(this);
            this.addMouseMotionListener(this);
            
            MouseListener popupListener = new MouseListener();
            this.addMouseListener(popupListener);
        }
        
        public void calculateCoordinates()
        {
            coordinates = new HashMap<Integer,HashMap<String,Integer>>();
            int fieldLength = (int) (Math.sqrt(g.vertices.size()) + 1);
            outerBoundX = fieldLength * 210;
            outerBoundY = outerBoundX;
            this.setPreferredSize(new Dimension(outerBoundX, outerBoundY));
            int x = 0;
            int y = 0;
            for (int i : g.vertices.keySet())
            {
                @SuppressWarnings("unused")
				Vertex v = g.vertices.get(i);
                HashMap<String,Integer> d = new HashMap<String,Integer>();
                d.put("x", x * 200);
                d.put("y", y * 200);
                coordinates.put(i,d);
                if (x < fieldLength - 1)
                {
                    x++;
                }
                else
                {
                    x = 0;
                    y++;
                }
            }
            for (int i : g.vertices.keySet())
            {
                Vertex v = g.vertices.get(i);
                HashMap<String,Integer> d = coordinates.get(i);
                int X = d.get("x");
                int Y = d.get("y");
                for (Edge e : v.edges)
                {
                    HashMap<String,Integer> o = coordinates.get(e.getGoal());
                    int oX = o.get("x");
                    int oY = o.get("y");
                    X = X + (int) (0.3 * (oX - X)); 
                    Y = Y + (int) (0.3 * (oY - Y));
                    oX = oX + (int) (0.3 * (X - oX));
                    oY = oY + (int) (0.3 * (Y - oY));
                    o.put("x", oX);
                    o.put("y", oY);
                }
                d.put("x", X);
                d.put("y", Y);
            }
        }
        
        public void calculateDescendentGraph(boolean balanced)
        {
        	coordinates = new HashMap<Integer,HashMap<String,Integer>>();
        	HashMap<Integer,Integer> mapToLevel = new HashMap<Integer,Integer>();
        	HashMap<Integer,ArrayList<Integer>> levels = new HashMap<Integer,ArrayList<Integer>>();
        	levels.put(0,new ArrayList<Integer>());
        	for (int i : mg.vertices.keySet())
        	{
        		HashMap<String,Integer> coord = new HashMap<String,Integer>();
        		coord.put("y", 50);
        		coord.put("x", 100);
        		coordinates.put(i, coord);
        		mapToLevel.put(i,0);
        		levels.get(0).add(i);
        	}
        	for (int i = 0 ; i < mg.vertices.size(); i++)
        	{
        		Vertex v = mg.vertices.get(i);
        		for (Edge e : v.edges)
        		{
        			//System.err.println("Deciding: " + i + "-->" + e.getGoal() + " " + mapToLevel.get(i) + " " + mapToLevel.get(e.getGoal()));
        			if (mapToLevel.get(i) <= mapToLevel.get(e.getGoal()))
        			{
        				ArrayList<Integer> level = levels.get(mapToLevel.get(i));
        				for (int j = 0; j < level.size(); j++)
        				{
        					if (level.get(j) == i)
        					{
        						level.remove(j);
        						break;
        					}
        				}
        				if (levels.get(mapToLevel.get(e.getGoal()) + 1) == null)
        				{
        					levels.put(mapToLevel.get(e.getGoal()) + 1,new ArrayList<Integer>());
        				}
        				levels.get(mapToLevel.get(e.getGoal()) + 1).add(i);
        				mapToLevel.put(i, mapToLevel.get(e.getGoal()) + 1);
        			}
        		}
        	}
        	int largestLevelSize = 0;
        	int j = 0;
        	for (ArrayList<Integer> level : levels.values())
        	{	
        		if (level.size() > largestLevelSize) largestLevelSize = level.size();
        		for (int i = 0; i < level.size(); i++)
        		{
        			coordinates.get(level.get(i)).put("y", 100 + j * 100);
        			coordinates.get(level.get(i)).put("x", 100 + i * 600);
        		}
        		j++;
        	}
            outerBoundX = largestLevelSize * 600 + 200;
            outerBoundY = levels.size() * 100 + 500;
        	if (balanced)
        	{
            	for (int k = levels.size() - 1; k >= 0; k--)
            	{	
            		ArrayList<Integer> level = levels.get(k);
            		for (int i = 0; i < level.size(); i++)
            		{
            			int minX = outerBoundX / 2;
            			int maxX = outerBoundX / 2;
            			for (Edge e : g.vertices.get(level.get(i)).edges)
            			{
            				int newX = coordinates.get(e.getGoal()).get("x");
            				if (newX < minX) minX = newX;
            				if (newX > maxX) maxX = newX;	
            			}
            			coordinates.get(level.get(i)).put("x", (minX + maxX) / 2);
            		}
            	}
        	}
        	g = mg;
            this.setPreferredSize(new Dimension(outerBoundX, outerBoundY));
        }
        
        public void calculateTreeCoordinates(int rootVertex)
        {
            calculateTreeCoordinates(rootVertex, mg.vertices.size());
        }
        
        public void calculateTreeCoordinates(int rootVertex, int depth)
        {
            coordinates = new HashMap<Integer,HashMap<String,Integer>>();
            ArrayList<ArrayList<Integer>> closures = new ArrayList<ArrayList<Integer>>();
            ArrayList<Integer> nullClosure = new ArrayList<Integer>();
            nullClosure.add(rootVertex);
            closures.add(nullClosure);
            int largestClosure = 1;
            for (int i = 1; i <= depth; i++)
            {
                ArrayList<Integer> previousClosure = closures.get(i-1);
                ArrayList<Integer> presentClosure = new ArrayList<Integer>();
                for (int v : previousClosure)
                {
                    HashMap<String,Integer> d = new HashMap<String,Integer>();
                    d.put("y", 50 + i * 100);
                    coordinates.put(v,d);
                    ArrayList<Integer> newClosureItems = mg.getClosure(v, 1).getVertexIDs();
                    //System.out.println("New closure items: " + newClosureItems);
                    for (Integer j : newClosureItems)
                    {
                        if (j != v && !presentClosure.contains(j))
                        {
                            presentClosure.add(j);
                        }
                    }
                }
                closures.add(presentClosure);
                //System.out.println("Closure " + i + " of size " + presentClosure.size() + ": " + presentClosure);
                if (presentClosure.size() > largestClosure)
                {
                    largestClosure = presentClosure.size();
                }
                if (presentClosure.size() == 0) break;
            }
            ArrayList<Integer> lastClosure = closures.get(closures.size() - 1);
            for (int v : lastClosure)
            {
                HashMap<String,Integer> d = new HashMap<String,Integer>();
                d.put("y", 50 + (closures.size()) * 100);
                coordinates.put(v,d);
            }
            outerBoundX = largestClosure * 200 + 200;
            outerBoundY = closures.size() * 100 + 200;
            this.setPreferredSize(new Dimension(outerBoundX, outerBoundY));
            for (ArrayList<Integer> closure : closures)
            {
                if (closure.size() != 0)
                {
                    for (int i = closure.size()/2, j = outerBoundX/2; i >= 0; i--, j -= 200)
                    {
                        coordinates.get(closure.get(i)).put("x", j);
                    }
                    for (int i = closure.size()/2, j = outerBoundX/2; i < closure.size(); i++, j += 200)
                    {
                        coordinates.get(closure.get(i)).put("x", j);
                    }
                }
            }
            g = new GraphModel();
            for (ArrayList<Integer> closure : closures)
            {
                for (int i : closure)
                {
                    g.vertices.put(i, mg.vertices.get(i));
                }
            }
        }
        
        public void calculateAccessTable()
        {
            accessTable = new HashMap<String,Integer>();
            for (int i : coordinates.keySet())
            {
                HashMap<String,Integer> d = coordinates.get(i);
                int x = d.get("x");
                int y = d.get("y");
                for (int k = 10 ; k < 15; k++)
                {
                    for(int l = 10; l < 15; l++)
                    {
                        accessTable.put((x + k) + "." + (y + l), i);
                    }
                }
            }
        }
        
        public void mousePressed(MouseEvent e)
        {
            int mousePressedX = e.getX(); 
            int mousePressedY = e.getY(); 
            Integer mv = accessTable.get(mousePressedX + "." + mousePressedY);
            if (mv == null) movedVertex = -1;
            else movedVertex = mv;
            System.out.println(mousePressedX + "/" + mousePressedY + ":" + movedVertex);
        }
        
        public void mouseReleased(MouseEvent e)
        {
            int mouseReleasedX = e.getX(); 
            int mouseReleasedY = e.getY(); 
            if (movedVertex != -1)
            {
                HashMap<String,Integer> d = coordinates.get(movedVertex);
                d.put("x", mouseReleasedX);
                d.put("y", mouseReleasedY);
            }
            movedVertex = -1;
            calculateAccessTable();
            repaint();
        }
        
        public void mouseMoved(MouseEvent e)
        {
            
        }
        
        public void mouseDragged(MouseEvent e)
        {
            if (movedVertex != -1)
            {
                int mouseX = e.getX(); 
                int mouseY = e.getY(); 
                if (movedVertex != -1)
                {
                    HashMap<String,Integer> d = coordinates.get(movedVertex);
                    d.put("x", mouseX);
                    d.put("y", mouseY);
                }
                repaint();
            }
        }
        
        public void mouseClicked(MouseEvent e)
        {
        }
        
        public void mouseEntered(MouseEvent e)
        {
            
        }
        
        public void mouseExited(MouseEvent e)
        {
            
        }
        
        public void paint(Graphics c)
        {
            c.setColor(Color.WHITE);
            c.fillRect(0,0, outerBoundX, outerBoundY);
            c.setColor(Color.BLACK);
            for (int i : coordinates.keySet())
            {
                Vertex v = g.vertices.get(i);
                HashMap<String,Integer> d = coordinates.get(i);
                int x = d.get("x");
                int y = d.get("y");
                c.drawOval(x + 10, y + 10, 5, 5);
                c.drawString(v.getCaption(), x + 20, y + 10);
                for (Edge e : v.edges)
                {
                    try
                    {
                        HashMap<String,Integer> o = coordinates.get(e.getGoal());
                        c.drawLine(x + 10, y + 10, o.get("x") + 10, o.get("y") + 10);
                        c.drawString(e.getLabel(), (x + 10 + o.get("x") + 10) / 2, (y + 10 + o.get("y") + 10) / 2);
                        double slope = (o.get("y") - y + 0.0)/(o.get("x") - x + 0.0);
                        double lowerSlope = (slope -1)/(1+slope);
                        double higherSlope = -(1/lowerSlope);
                        //System.out.println("Slope: " + slope + " lowerSlope: " + lowerSlope + " higherSlope: " + higherSlope);
                        if((o.get("x")  < x && slope < -1 )||(o.get("x")  > x && slope > -1 )) c.drawLine(o.get("x") + 10,o.get("y") + 10,o.get("x") + 10 - (int)(7/Math.sqrt(1+lowerSlope*lowerSlope)),o.get("y") + 10 -(int)((7/Math.sqrt(1+lowerSlope*lowerSlope))*lowerSlope));
                        if((o.get("x")  < x && slope > -1 )||(o.get("x")  > x && slope < -1 )) c.drawLine(o.get("x") + 10,o.get("y") + 10,o.get("x") + 10 + (int)(7/Math.sqrt(1+lowerSlope*lowerSlope)),o.get("y") + 10 +(int)((7/Math.sqrt(1+lowerSlope*lowerSlope))*lowerSlope));
                        if((o.get("x")  < x && slope > 1 )||(o.get("x")  > x && slope < 1 )) c.drawLine(o.get("x") + 10,o.get("y") + 10,o.get("x") + 10 - (int)(7/Math.sqrt(1+higherSlope*higherSlope)),o.get("y") + 10 -(int)((7/Math.sqrt(1+higherSlope*higherSlope))*higherSlope));
                        if((o.get("x")  < x && slope < 1 )||(o.get("x")  > x && slope > 1 )) c.drawLine(o.get("x") + 10,o.get("y") + 10,o.get("x") + 10 + (int)(7/Math.sqrt(1+higherSlope*higherSlope)),o.get("y") + 10 +(int)((7/Math.sqrt(1+higherSlope*higherSlope))*higherSlope));
                    }
                    catch (NullPointerException ex)
                    {
                        System.out.println("Unable to draw edge: " + e.toString());
                    }
                }
            }
        }
        
        class MouseListener extends MouseAdapter implements ActionListener
        {
            private JPopupMenu popup;
            int selectedVertex;

            MouseListener()
            {
                popup = new JPopupMenu();
                JMenu closure = new JMenu("closure");
                popup.add(closure);
                JMenuItem closure1 = new JMenuItem("1-closure");
                closure1.addActionListener(this);
                closure.add(closure1);
                JMenuItem closure2 = new JMenuItem("2-closure");
                closure2.addActionListener(this);
                closure.add(closure2);
                JMenuItem closure3 = new JMenuItem("3-closure");
                closure3.addActionListener(this);
                closure.add(closure3);
                JMenuItem closure4 = new JMenuItem("4-closure");
                closure4.addActionListener(this);
                closure.add(closure4);
                JMenuItem closure5 = new JMenuItem("5-closure");
                closure5.addActionListener(this);
                closure.add(closure5);
                JMenu tree = new JMenu("tree");
                popup.add(tree);
                JMenuItem tree1 = new JMenuItem("tree of depth 1");
                tree1.addActionListener(this);
                tree.add(tree1);
                JMenuItem tree2 = new JMenuItem("tree of depth 2");
                tree2.addActionListener(this);
                tree.add(tree2);
                JMenuItem tree3 = new JMenuItem("tree of depth 3");
                tree3.addActionListener(this);
                tree.add(tree3);
                JMenuItem tree4 = new JMenuItem("tree of depth 4");
                tree4.addActionListener(this);
                tree.add(tree4);
                JMenuItem tree5 = new JMenuItem("tree of depth 5");
                tree5.addActionListener(this);
                tree.add(tree5);
                JMenuItem deepTree = new JMenuItem("tree of maximal depth (Infinite Recursion!)");
                deepTree.addActionListener(this);
                tree.add(deepTree);
                
                JMenuItem graphItem = new JMenuItem("entire graph");
                graphItem.addActionListener(this);
                popup.add(graphItem);
                JMenuItem balGraphItem = new JMenuItem("entire graph (balanced)");
                balGraphItem.addActionListener(this);
                popup.add(balGraphItem);
                
                selectedVertex = -1;
            }

            public void mouseReleased(MouseEvent e)
            {
                if (e.isPopupTrigger())
                {
                	Integer sv = accessTable.get(e.getX() + "." + e.getY());
                	if (sv == null) selectedVertex = -1;
                	else  selectedVertex = sv;        
                    popup.show(e.getComponent(), e.getX(), e.getY());
                }
            }

            public void mousePressed(MouseEvent e)
            {
                if (e.isPopupTrigger())
                {
                	Integer sv = accessTable.get(e.getX() + "." + e.getY());
                	if (sv == null) selectedVertex = -1;
                	else  selectedVertex = sv; 
                    popup.show(e.getComponent(), e.getX(), e.getY()); 
                }
            }
            
            public void mouseClicked(MouseEvent e)
            {
                if (e.isPopupTrigger())
                {
                	Integer sv = accessTable.get(e.getX() + "." + e.getY());
                	if (sv == null) selectedVertex = -1;
                	else  selectedVertex = sv; 
                    popup.show(e.getComponent(), e.getX(), e.getY());
                }
            }
            
            public void actionPerformed(ActionEvent e)
            {
                String m = e.getActionCommand();
                if (m.equals("1-closure"))
                {
                    if (selectedVertex != -1)
                    {
                        g = mg.getClosure(selectedVertex, 1);
                        calculateCoordinates();
                        calculateAccessTable();
                        repaint();
                    }
                }
                if (m.equals("2-closure"))
                {
                    if (selectedVertex != -1)
                    {
                        g = mg.getClosure(selectedVertex, 2);
                        calculateCoordinates();
                        calculateAccessTable();
                        repaint();
                    }
                }
                if (m.equals("3-closure"))
                {
                    if (selectedVertex != -1)
                    {
                        g = mg.getClosure(selectedVertex, 3);
                        calculateCoordinates();
                        calculateAccessTable();
                        repaint();
                    }
                }
                if (m.equals("4-closure"))
                {
                    if (selectedVertex != -1)
                    {
                        g = mg.getClosure(selectedVertex, 4);
                        calculateCoordinates();
                        calculateAccessTable();
                        repaint();
                    }
                }
                if (m.equals("5-closure"))
                {
                    if (selectedVertex != -1)
                    {
                        g = mg.getClosure(selectedVertex, 5);
                        calculateCoordinates();
                        calculateAccessTable();
                        repaint();
                    }
                }
                if (m.equals("tree of depth 1"))
                {
                    if (selectedVertex != -1)
                    {
                        calculateTreeCoordinates(selectedVertex, 1);
                        calculateAccessTable();
                        repaint();
                    }
                }
                if (m.equals("tree of depth 2"))
                {
                    if (selectedVertex != -1)
                    {
                        calculateTreeCoordinates(selectedVertex, 2);
                        calculateAccessTable();
                        repaint();
                    }
                }
                if (m.equals("tree of depth 3"))
                {
                    if (selectedVertex != -1)
                    {
                        calculateTreeCoordinates(selectedVertex, 3);
                        calculateAccessTable();
                        repaint();
                    }
                }
                if (m.equals("tree of depth 4"))
                {
                    if (selectedVertex != -1)
                    {
                        calculateTreeCoordinates(selectedVertex, 4);
                        calculateAccessTable();
                        repaint();
                    }
                }
                if (m.equals("tree of depth 5"))
                {
                    if (selectedVertex != -1)
                    {
                        calculateTreeCoordinates(selectedVertex, 5);
                        calculateAccessTable();
                        repaint();
                    }
                }

                if (m.equals("tree of maximal depth (Infinite Recursion!)"))
                {
                    if (selectedVertex != -1)
                    {
                        calculateTreeCoordinates(selectedVertex);
                        calculateAccessTable();
                        repaint();
                    }
                }
                
                if (m.equals("entire graph"))
                {
                    calculateDescendentGraph(false);
                    calculateAccessTable();
                    repaint();
                }
                
                if (m.equals("entire graph (balanced)"))
                {
                    calculateDescendentGraph(true);
                    calculateAccessTable();
                    repaint();
                }
            }
        }

    }

    public static void main(String[] args)
    {
        GraphModel g;
        if (args[0].endsWith("gra"))
        {
            g = GraphModel.loadGraphFromFile(args[0]);
        }
        else
        {
            g = GraphModel.loadWordNetFromFile(args[0]);
        }
        GraphExplorer e = new GraphExplorer(g);
        e.setSize(1000, 750);
        e.setLocation(0, 0);
        e.setVisible(true);
    }
}
