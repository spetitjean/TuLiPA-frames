/*
 *  File Tidentifier.java
 *
 *  Authors:
 *     Yannick Parmentier  <parmenti@sfs.uni-tuebingen.de>
 *
 *  Copyright:
 *     Yannick Parmentier, 2008
 *
 *  Last modified:
 *     Fr 18. Jan 19:44:44 CET 2008
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
package de.tuebingen.forest;

public class Tidentifier implements Comparable<Tidentifier> {

    private int clauseId;
    private String treeId;
    private String nodeId; // gorn address of the node receiving the operation
    private String binding; //bindings may differ between 2 instances of the same clauseId!

    public Tidentifier(int i, String tname, String bd) {
        clauseId = i;
        treeId = tname;
        binding = bd;
        nodeId = "0"; // default value, added by YP 2013/06/13
    }

    public Tidentifier(Tidentifier tid) {
        clauseId = tid.getClauseId();
        treeId = tid.getTreeId();
        nodeId = tid.getNodeId();
        binding = tid.getBinding();
    }

    public int getClauseId() {
        return clauseId;
    }

    public void setClauseId(int number) {
        this.clauseId = number;
    }

    public String getTreeId() {
        return treeId;
    }

    public void setTreeId(String treeId) {
        this.treeId = treeId;
    }

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public String getBinding() {
        return binding;
    }

    public void setBinding(String binding) {
        this.binding = binding;
    }

    public int hashCode() {
        return toString().hashCode();
    }

    public boolean equals(Object o) {
        return this.hashCode() == o.hashCode();
    }

    public String toString() {
        String res = "";
        res += clauseId;
        res += " [" + treeId + "] ";
        res += binding + " ";
        return res;
    }

    public int compareTo(Tidentifier arg0) { // for sorting Tidentifier according to clause ids
        return ((Integer) this.clauseId).compareTo(arg0.getClauseId());
    }

}
