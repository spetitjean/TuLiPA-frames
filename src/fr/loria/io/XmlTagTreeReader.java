/*
 *  File XmlTagTreeReader.java
 *
 *  Authors:
 *     Yannick Parmentier <parmenti@loria.fr>
 *     
 *  Copyright:
 *     Yannick Parmentier, 2008
 *
 *  Last modified:
 *     Fri May 16 16:03:14 CEST 2008
 *
 *  This file is part of the Polarity Filter
 *
 *  The Polarity Filter is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  The Polarity Filter is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package fr.loria.io;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import fr.loria.disambiguation.Polarities;
import fr.loria.filter.TreeFilter;
import de.tuebingen.tag.TagTree;
import de.tuebingen.lexicon.Lemma;
import de.tuebingen.lexicon.MorphEntry;
import de.tuebingen.util.MyEntityResolver;

public class XmlTagTreeReader {
	
	public static final int POL     = 0;
	public static final int GRAMMAR = 1;
	public static final int LEMMA   = 2;
	public static final int MORPH   = 3;
	
	private Map<String, Polarities> polarities;
	private List<String> lexNodes;
	private Map<String, TagTree> trees;
	private Map<String, List<MorphEntry>> morphs;
	private Map<String, List<Lemma>> lemmas;
	private Map<String, List<String>> families;
	private Map<String, List<String>> coanchors;
	private int inGrammarSize = 0;
	
	public XmlTagTreeReader() {}
	
	public XmlTagTreeReader(int which, String uri, boolean verbose) throws SAXException, IOException {
		XMLReader saxReader = XMLReaderFactory.createXMLReader("org.apache.xerces.parsers.SAXParser");
		saxReader.setEntityResolver(new MyEntityResolver());
		//System.err.println("@@@@@@@@@@@@@@ File " + uri);
		switch (which) {
		case POL:
	        saxReader.setContentHandler(new TagContentHandler(verbose));
	        saxReader.parse(uri);
	        polarities = ((TagContentHandler) saxReader.getContentHandler()).getPolarities();
	        lexNodes   = ((TagContentHandler) saxReader.getContentHandler()).getLexNodes();
	        families   = ((TagContentHandler) saxReader.getContentHandler()).getFamilies();
	        inGrammarSize=((TagContentHandler) saxReader.getContentHandler()).getInGrammarSize();
			break;
		case GRAMMAR:
	        saxReader.setContentHandler(new TagTreeContentHandler(verbose));
	        saxReader.parse(uri);
	        trees = ((TagTreeContentHandler) saxReader.getContentHandler()).getSchema();			
			break;
		case LEMMA:
			saxReader.setContentHandler(new LemmaContentHandler(verbose));
	        saxReader.parse(uri);
	        lemmas = ((LemmaContentHandler) saxReader.getContentHandler()).getLemma();
	        coanchors = ((LemmaContentHandler) saxReader.getContentHandler()).getCoanchors();
			break;
		case MORPH:
	        saxReader.setContentHandler(new MorphContentHandler(verbose));
	        saxReader.parse(uri);
	        morphs = ((MorphContentHandler) saxReader.getContentHandler()).getMorphs();
			break;
		}
	}
	
	public void filter(List<String> f, String uri, boolean verbose) throws SAXException, IOException {
		XMLReader saxReader = XMLReaderFactory.createXMLReader("org.apache.xerces.parsers.SAXParser");
		TreeFilter tf = new TreeFilter(f);
		tf.setParent(saxReader);
		tf.setEntityResolver(new MyEntityResolver());
		tf.setContentHandler(new TagTreeContentHandler(verbose));
		tf.parse(uri);
		trees = ((TagTreeContentHandler) tf.getContentHandler()).getSchema();
	}
	
	public Map<String, Polarities> getPolarities() {
		return polarities;
	}

	public List<String> getLexNodes() {
		return lexNodes;
	}

	public Map<String, TagTree> getTrees() {
		return trees;
	}

	public Map<String, List<MorphEntry>> getMorphs() {
		return morphs;
	}

	public Map<String, List<Lemma>> getLemmas() {
		return lemmas;
	}

	public Map<String, List<String>> getFamilies() {
		return families;
	}

	public Map<String, List<String>> getCoanchors() {
		return coanchors;
	}

	public int getInGrammarSize() {
		return inGrammarSize;
	}
}
