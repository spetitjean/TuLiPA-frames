/*
 *  File Word.java
 *
 *  Authors:
 *     Wolfgang Maier <wo.maier@uni-tuebingen.de>
 *     
 *  Copyright:
 *     Wolfgang Maier, 2007
 *
 *  Last modified:
 *     Di 16. Okt 09:42:52 CEST 2007
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
package de.tuebingen.tokenizer;

import de.tuebingen.tagger.PosTag;
import de.tuebingen.tagger.SimplePosTag;

import java.util.*;

/**
 * Represents a single word of the input sentence.
 * @author wmaier
 */

public class Word {
	
	private int start;
	private int end;
	private String word;
	private List<PosTag> postag;
	private boolean hasStrangeToken;
	
	public Word() {
		this(null);
	}

	public Word(String word) {
		this(word, 0, 0);
	}
	
	public Word(String word, int start, int end) {
		this(word, start, end, new LinkedList<PosTag>());
	}
	
	public Word(String word, int start, int end, List<PosTag> postags) {
		this.word = word;
		this.start = start;
		this.end = end;
		this.postag = postags;
	}
	
	public int getStart() {
		return start;
	}

	public void setStart(int start) {
		this.start = start;
	}

	public int getEnd() {
		return end;
	}

	public void setEnd(int end) {
		this.end = end;
	}

	public String getWord() {
		return word;
	}

	public void setWord(String word) {
		this.word = word;
	}

	public List<PosTag> getTag() {
		return postag;
	}
	
	public Map<String, String> getTagAsAMap() {
		// returns the mapping lemma <-> cat for each POS-tag
		Map<String, String> tags = new HashMap<String, String>();
		for(int i = 0 ; i < postag.size() ; i++){
			SimplePosTag ptag = (SimplePosTag) postag.get(i); 
			tags.put(ptag.getLemma(), ptag.getTag());
		}
		if (postag.size() == 0) {tags = null;}
		return tags;
	}

	public void setTag(List<PosTag> posTag) {
		this.postag = posTag;
	}
	
	public void setATag(PosTag posTag) {
		List<PosTag> ptag = this.getTag();
		ptag.add(posTag);
	}

	public void setStrangeToken(boolean b) {
		this.hasStrangeToken = b;
	}
	
	public boolean hasStrangeToken() {
		return hasStrangeToken;
	}
	
	public String posToString(){
		String res = "";
		for(int i = 0 ; postag != null && i < postag.size() ; i++){
			res += postag.get(i).toString() + " ";
		}
		return res;
	}

	public String toString() {
		String ret = "'" + word + "'" + this.posToString() + "[" + start + "," + end + "]";
		if (hasStrangeToken) {
			ret += "#";
		}
		return ret;
	}
	
}
