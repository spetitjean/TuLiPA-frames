/*
 *  File AttributeDisplayExtension.java
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
package de.tuebingen.gui;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;

import de.tuebingen.gui.tree.view.*;

public class AttributeDisplayExtension extends TreeViewExtension
{
	public void paintOnTreePanel(TreeViewPanel panel, Graphics2D canvas)
	{
		XMLViewTree t = (XMLViewTree) panel.t;
        //print information on current tree
        for (int i = 0; i < t.getNodeLevels().size(); i++)
        {
            ArrayList<Integer> nodes = t.getNodeLevels().get(i);
            for (int j = 0; j < nodes.size(); j++)
            {
				//print attributes of node
		        if (!t.collapsedAttributes.contains(nodes.get(j)))
		        {
		            int attrYPos = t.treeNodes.get(nodes.get(j)).y;
		            int leftBorder = t.treeNodes.get(nodes.get(j)).x;
		            

		            // reversed traversal of the features for distinguishing top and bot
		            ArrayList<XMLViewTreeAttribute> feats = t.getAttrs(nodes.get(j));
		            
		            for (int k = feats.size() - 1 ; k >= 0 ; k--)
		            {
		            	XMLViewTreeAttribute attr = feats.get(k);
		                int leftFringe = t.treeNodes.get(nodes.get(j)).x - (attr.name.length() + attr.value.length()) * 4;
		                if (leftFringe < leftBorder)
		                {
		                    leftBorder = leftFringe;
		                }
		            }
		            
		            canvas.setColor(Color.WHITE);
		            canvas.fillRect(leftBorder, t.treeNodes.get(nodes.get(j)).y + 2, (t.treeNodes.get(nodes.get(j)).x - leftBorder) * 2, 15 * t.getAttrs(nodes.get(j)).size());
		            canvas.setColor(Color.BLACK);
		            canvas.drawRect(leftBorder, t.treeNodes.get(nodes.get(j)).y + 2, (t.treeNodes.get(nodes.get(j)).x - leftBorder) * 2, 15 * t.getAttrs(nodes.get(j)).size());             
		            
		            for (int k = feats.size() - 1 ; k >= 0 ; k--)
		            {
		            	XMLViewTreeAttribute attr = feats.get(k);
		                attrYPos += 15;
		                if (attr.value.startsWith("#"))
		                {
		                    canvas.setColor(Color.RED);
		                    String attrString = attr.name + ": " + attr.value.substring(1);
		                    canvas.drawString(attrString,leftBorder,attrYPos);
		                }
		                else
		                {
		                    String attrString = attr.name + ": " + attr.value;
		                    canvas.drawString(attrString,leftBorder,attrYPos);
		                }
		                canvas.setColor(Color.BLACK);
		            }
		        }
            }
        }
	}
}
