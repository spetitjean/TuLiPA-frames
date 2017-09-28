/*
 *  File Tagger.java
 *
 *  Authors:
 *     Wolfgang Maier  <wo.maier@uni-tuebingen.de>
 *     Yannick Parmentier <parmenti@sfs.uni-tuebingen.de>	
 *     
 *  Copyright:
 *     Wolfgang Maier, 2007
 *     Yannick Parmentier, 2007
 *
 *  Last modified:
 *     Mi 12. Dez 11:58:40 CET 2007
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
package de.tuebingen.tagger;

import java.io.IOException;
import java.util.List;

import de.tuebingen.tokenizer.Word;

/**
 * An interface which subsumes all POS/Hypertaggers, also classes
 * which call external taggers
 * 
 * @author wmaier, parmenti
 *
 */
public interface Tagger {

	public void doTagging(List<Word> words) throws TaggerException, IOException, InterruptedException;
	// tagging consists of extending the Word objects by adding the POS-tag
}
