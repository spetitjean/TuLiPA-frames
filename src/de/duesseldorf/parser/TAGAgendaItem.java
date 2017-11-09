/*
 *  File TAGParser.java
 *
 *  Authors:
 *     Thomas Schoenemann  <tosch@phil.uni-duesseldorf.de>
 *     
 *  Copyright:
 *     Thomas Schoenemann, 2012
 *
 *  Last modified:
 *     Mar 01 2012
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
package de.duesseldorf.parser;
import java.util.*;
//import de.tuebingen.tree.Grammar;
import de.tuebingen.tag.*;
import de.tuebingen.tokenizer.*;
import de.tuebingen.forest.*;
import de.tuebingen.tree.*;
import de.tuebingen.rcg.PredComplexLabel;

/**
 * @author tosch
 *
 */

class TAGAgendaItem {

    public TagNode node;
    public int i1;
    public int i2;
    public int j1;
    public int j2;
    public int pos;

    public TAGAgendaItem(TagNode n, int _i1, int _i2, int _j1, int _j2, int _pos) {

	node = n;
	i1 = _i1;
	i2 = _i2;
	j1 = _j1;
	j2  =_j2;
	pos = _pos;
    }

};
