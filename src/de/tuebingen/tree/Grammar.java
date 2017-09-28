/*
 *  File Grammar.java
 *
 *  Authors:
 *     Yannick Parmentier  <parmenti@sfs.uni-tuebingen.de>
 *     
 *  Copyright:
 *     Yannick Parmentier, 2007
 *
 *  Last modified:
 *     Di 16. Okt 09:33:31 CEST 2007
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

import java.util.List;
import java.util.Map;

import de.tuebingen.lexicon.Lemma;
import de.tuebingen.lexicon.MorphEntry;
import de.tuebingen.tag.Tuple;

public interface Grammar {

    public abstract boolean needsAnchoring();

    public abstract void setNeedsAnchoring(boolean b);

    public abstract void setMorphEntries(
            Map<String, List<MorphEntry>> morphEntries);

    public abstract void setLemmas(Map<String, List<Lemma>> lemmas);

    public abstract Map<String, List<Lemma>> getLemmas();

    public abstract Map<String, List<MorphEntry>> getMorphEntries();

    public abstract Map<String, List<Tuple>> getGrammar();

}
