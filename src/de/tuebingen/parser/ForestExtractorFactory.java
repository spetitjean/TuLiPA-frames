/*
 *  File ForestExtractorFactory.java
 *
 *  Authors:
 *     Wolfgang Maier <wo.maier@uni-tuebingen.de>
 *     
 *  Copyright:
 *     Wolfgang Maier, 2009
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
package de.tuebingen.parser;

import de.tuebingen.forest.ExtractForest;
import de.tuebingen.parser.simple.SimpleRCGParserEarley;
import de.tuebingen.parserconstraints.ExtractTreeForest;
import de.tuebingen.parserconstraints.RCGParserConstraintEarley;

public class ForestExtractorFactory {

	public static ForestExtractor getForestExtractor(RCGParser p) throws Exception {
		
		if (p instanceof RCGParserBoullier2) {
			return new ExtractForest();
		}
		
		if (p instanceof RCGParserConstraintEarley) {
			return new ExtractTreeForest();
		}
		
		if (p instanceof SimpleRCGParserEarley) {
			return new ExtractTreeForest();
		}
		
		throw new Exception("No forest extractor available for " + p.getClass().toString());
	}
	
}
