/*
 *  File TreeViewNode.java
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

import java.util.*;
import java.awt.*;

import de.tuebingen.gui.tree.model.*;

public class TreeViewNode 
{
	TreeModelNode modelNode;
    public int id;
    private int parent;
    public ArrayList<Integer> children;
    public String tag;
    public Color color;
    Color edgeTagColor;
    public int x;
    public int y;
    public int subTreeWidth;
	protected String edgeTag;
	protected String edgeDir;

	String edgeType;
    
    public TreeViewNode(TreeModelNode modelNode, int parent, ArrayList<Integer> children, int x, int y)
    {
        this.modelNode = modelNode;
        this.id = modelNode.id;
        this.setParent(parent);
        //this.children = children;
        this.children = new ArrayList<Integer>();
        this.tag = modelNode.content;
        this.color = null;
        this.edgeTagColor = null;
        this.x = x;
        this.y = y;
        this.subTreeWidth = 1;
        this.edgeTag = "";
        this.edgeDir = "";
        this.edgeType = "";
        //star at beginning of tag symbolizes dotted edge to parent
        if (tag.startsWith("*"))
        {
            tag = tag.substring(1);
            edgeType = "dotted";
        }
    }
    
    public TreeViewNode(int id, int parent, ArrayList<Integer> children, String tag, int x, int y)
    {
        this.modelNode = null;
        this.id = id;
        this.setParent(parent);
        this.children = children;
	this.children = new ArrayList<Integer>();
        this.tag = tag;
        this.color = null;
        this.edgeTagColor = null;
        this.x = x;
        this.y = y;
        this.subTreeWidth = 1;
        this.edgeTag = "";
        this.edgeDir = "";
        this.edgeType = "";
    }
    
    public int getSubTreeWidth()
	{
		return subTreeWidth;
	}
    
    public String getEdgeTag()
	{
		return edgeTag;
	}

	public void setEdgeTag(String edgeTag)
	{
		this.edgeTag = edgeTag;
	}
	
	public String getEdgeDir()
	{
		return edgeDir;
	}

	public void setEdgeDir(String edgeDir)
	{
		this.edgeDir = edgeDir;
	}

	public void setParent(int parent)
	{
		this.parent = parent;
	}

	public int getParent()
	{
		return parent;
	}
}
