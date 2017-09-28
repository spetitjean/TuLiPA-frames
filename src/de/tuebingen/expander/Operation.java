/*
 *  File Operation.java
 *
 *  Authors:
 *     Johannes Dellert  <johannes.dellert@sfs.uni-tuebingen.de>
 *     
 *  Copyright:
 *     Johannes Dellert, 2008
 *
 *  Last modified:
 *     Wed Jan 30 11:49:58 CET 2008
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
package de.tuebingen.expander;

import org.w3c.dom.*;

public class Operation
{
    String id;
    String opId;
    String node;
    String type;
    
    public Operation(Node n)
    {
        id = n.getAttributes().getNamedItem("id").getNodeValue();
        opId = id.substring(id.indexOf("_"));
        node = n.getAttributes().getNamedItem("node").getNodeValue();
        type = n.getAttributes().getNamedItem("type").getNodeValue();
        //for compatibility reasons
        if (type.equals("sub")) type = "subst";
    }
    
    public String toString()
    {
    	return opId;
    	//return "(" + id + "," + opId + "," + node + "," + type + ")";
    }
}
