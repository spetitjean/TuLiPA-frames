/**
 * File Situation.java
 * 
 * Authors:
 * David Arps <david.arps@hhu.de>
 * Simon Petitjean <petitjean@phil.hhu.de>
 * 
 * Copyright
 * David Arps, 2017
 * Simon Petitjean, 2017
 * 
 * This file is part of the TuLiPA-frames system
 * https://github.com/spetitjean/TuLiPA-frames
 * 
 * 
 * TuLiPA is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * TuLiPA is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 * 
 */

package de.duesseldorf.frames;

import de.tuebingen.tree.Grammar;

/**
 * 
 * Stores a grammar, the corresponding frames and the type hierarchy to allow
 * easy access.
 * 
 * @author david
 *
 */
public class Situation {

    private static Grammar g;
    private static Grammar frameG;
    private static TypeHierarchy tyHi;

    public static void instantiate(Grammar grammar, Grammar frameGrammar,
            TypeHierarchy typeHierarchy) {
        g = grammar;
        frameG = frameGrammar;
        tyHi = typeHierarchy;
    }

    public static Grammar getGrammar() {
        return g;
    }

    public static Grammar getFrameGrammar() {
        return frameG;
    }

    public static TypeHierarchy getTypeHierarchy() {
        return tyHi;
    }
}
