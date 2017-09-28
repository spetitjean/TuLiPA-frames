/*
 *  File SectionAreaExtension.java
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
package de.tuebingen.gui.tree.view;

import java.awt.Color;
import java.awt.Graphics2D;

public class SelectionAreaExtension extends TreeViewExtension
{
	public void paintOnTreePanel(TreeViewPanel panel, Graphics2D canvas)
	{	
		canvas.setColor(Color.RED);
		int width = panel.t.getTotalTreeWidth();
		int height = panel.t.getTotalTreeHeight();
		for (int i = 0; i < width; i += 5)
		{
			for (int j = 0; j < height; j += 5)
			{
				System.err.println(i + "," + j);
				int nodeID = panel.t.getNodeAtCoordinates(i,j);
				if (nodeID != -1)
				{
					canvas.drawLine(i, j, panel.t.treeNodes.get(nodeID).x, panel.t.treeNodes.get(nodeID).y);
				}
			}
		}
	}
}
