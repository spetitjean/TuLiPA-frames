/*
 *  File Label.java
 *
 *  Authors:
 *     Wolfgang Maier  <wo.maier@uni-tuebingen.de>
 *
 *  Copyright:
 *     Wolfgang Maier, 2007
 *
 *  Last modified:
 *     Di 16. Okt 09:34:21 CEST 2007
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
package de.tuebingen.tree;

/**
 * @author wmaier
 */
public class Label {

    public static final char SEP_EDGE = '-';

    private String label;
    private String edge;

    public Label() {
        this("");
    }

    public Label(String label) {
        this(label, "");
    }

    public Label(String label, String edge) {
        this.label = label;
        this.edge = edge;
    }

    public String getEdge() {
        return edge;
    }

    public void setEdge(String edge) {
        this.edge = edge;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public static Label parseLabel(String label) {
        Label ret = new Label(label);
        int edgeind = label.lastIndexOf(Label.SEP_EDGE);
        if (edgeind > -1) {
            ret.setLabel(label.substring(0, edgeind));
            ret.setEdge(label.substring(edgeind + 1));
        }
        return ret;
    }

}
