package de.duesseldorf.rrg.parser;

/*
 *  File Operation.java
 *
 *  Authors:
 *     David Arps <david.arps@hhu.de
 *
 *  Copyright:
 *     David Arps, 2018
 *
 *  This file is part of the TuLiPA system
 *     https://github.com/spetitjean/TuLiPA-frames
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
public enum Operation {
    SUBSTITUTE, // perform a substitution
    LEFTADJOIN, // perform a sister adjunction to the left
    RIGHTADJOIN, // perform a sister adjunction to the right
    MOVEUP, // move up
    COMBINESIS, // combine sisters
    NLS, // no left sister
    SCAN, // scan
    PREDICTWRAPPING, COMPLETEWRAPPING, GENCW, GENCWJUMPBACK;
}
