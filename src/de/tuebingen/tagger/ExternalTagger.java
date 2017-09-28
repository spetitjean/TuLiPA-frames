/*
 *  File ExternalTagger.java
 *
 *  Authors:
 *     Wolfgang Maier  <wo.maier@uni-tuebingen.de>	
 *     Yannick Parmentier  <parmenti@sfs.uni-tuebingen.de>   
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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.regex.*;

import de.tuebingen.tokenizer.Word;

/**
 * @author wmaier, parmenti
 *
 */
public class ExternalTagger implements Tagger {

	private File     exec;
	private String params;

	public ExternalTagger() {
		this(null);
	}

	public ExternalTagger(File exec) {
		this(exec, "");
	}
	
	public ExternalTagger(File exec, String params) {
		setExec(exec);
		setParams(params);
	}

	public boolean setExec(File exec) {
		if (exec != null && exec.exists() && exec.isFile()){// && exec.canExecute()) {
			this.exec = exec;
			return true;
		} else {
			return false;
		}
	}
	
	public File getExec() {
		return exec;
	}
	
	public void setParams(String params) {
		this.params = params;
	}
	
	public String getParams() {
		return params;
	}

	public void doTagging(List<Word> words) throws TaggerException, IOException, InterruptedException {
		// is there a tagger defined ?
		if (exec != null) {
			List<PosTag> tags = new LinkedList<PosTag>();
			Map<String, Word> tokens = new HashMap<String, Word>(); // for convenience
			// creation of an external process for launching the tagging
			// the tagger path is relative to the user directory when launching TuLiPA
			String wd = System.getProperty("user.dir");
			ProcessBuilder builder = new ProcessBuilder(exec.toString());
			builder.directory(new File(wd));
			// starts the tagger		
			Process p = builder.start();
			// Input/Output redirection
			// We feed the tagger's stdin with the tokenized sentence
			BufferedOutputStream stdin = new BufferedOutputStream(p.getOutputStream());
			for (int i = 0; i < words.size(); ++i) {
				String word = words.get(i).getWord();
				tokens.put(word, words.get(i));
				for (int j = 0; j < word.length(); ++j) {
					stdin.write(word.charAt(j));
				}
				stdin.write(' '); // word separator
			}
			stdin.close();
			// we wait for the tagger to finish its work
			p.waitFor();
			// we read the tagger's output
			BufferedInputStream stdout = new BufferedInputStream(p.getInputStream());
			int c = 0;
			String posline = "";
			while ((c = stdout.read()) != -1) {
				//System.out.print((char) c);
				switch ((char) c) {
				case '\n':
					tags.addAll(this.parsePosTag(posline));
					posline = "";
					break;
				default:
					posline += (char) c;
				}
			}
			stdout.close();
			if (p.exitValue() != 0) {
				//System.err.println("Error: tagger exited with value " + p.exitValue() + ":");
				BufferedInputStream bs = new BufferedInputStream(p.getErrorStream());
				c = 0;
				String msg = "";
				while ((c = bs.read()) != -1) {
					//System.err.print((char) c);
					msg += (char) c;
				}
				throw new TaggerException(msg);
			}
			// eventually, we update the tokens (and duplicate them for ambiguities):
			for(int i = 0 ; i < tags.size() ; i++) {
				SimplePosTag postag = (SimplePosTag) tags.get(i);
				Word tok = tokens.get(postag.getFlex());
				tok.setATag(postag);
			}
			//ExternalTagger.printPosTags(tags);
		}
	}
	
	public List<PosTag> parsePosTag(String pos){
		// method splitting the POS tagger's output
		List<PosTag> tag = null;
		try{
			Pattern p = Pattern.compile("(\\S+)\\s+(\\S+)\\s+(\\S+)");
			Matcher m = p.matcher(pos);
			boolean a = m.find();
			if (a) {
				tag = this.getAllTags(m.group(1), m.group(2), m.group(3));
			} 
			else {
				System.err.println("Line : "+pos);
				System.err.println("Pattern not found : "+p.pattern());
			}
		}catch(PatternSyntaxException pse){
			System.err.println(pse.getDescription());
		}catch(IllegalStateException ise){
			System.err.println(ise.toString());
		}
		if (tag == null) {tag = new LinkedList<PosTag>();} 
		return tag;
	}
	
	public List<PosTag> getAllTags(String word, String cat, String lemmas) {
		// this method is used to deal with lexical ambiguities
		// e.g. POS-lemmas such as feig|feige -> 2 pos tags are created
		List<PosTag> tag = new LinkedList<PosTag>();
		String[] lems = lemmas.split("\\|");
		for(int i = 0 ; i < lems.length ; i++) {
			SimplePosTag stag = new SimplePosTag(word, cat, lems[i]);
			tag.add(stag);
		}
		return tag;
	}
	
	public static void printPosTags(List<PosTag> tagged) {
		for(int i = 0 ; tagged != null && i < tagged.size() ; i++){
			System.out.println(tagged.get(i).toString());
		}
	}

	public static void printPosToken(List<Word> tagged) {
		for(int i = 0 ; tagged != null && i < tagged.size() ; i++){
			System.out.println(tagged.get(i).toString());
		}
	}
}
