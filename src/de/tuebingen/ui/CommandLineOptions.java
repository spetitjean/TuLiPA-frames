/*
 *  File CommandLineOptions.java
 *
 *  Authors:
 *     Yannick Parmentier  <parmenti@sfs.uni-tuebingen.de>
 *     
 *  Copyright:
 *     Yannick Parmentier, 2007
 *
 *  Last modified:
 *     Di 16. Okt 09:29:49 CEST 2007
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
package de.tuebingen.ui;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.regex.*;

/**
 * Class for command line options processing
 * freely inpired by : Dr. Matthias Laux
 * (http://www.javaworld.com/javaworld/jw-08-2004/jw-0816-command.html)
 * 
 * @author parmenti
 *
 */
public class CommandLineOptions {
	
	// We enumerate the different components of a command line
	// 1. A prefix (default is -)
	public enum Prefix {
		DASH('-'),
		SLASH('/');
		private char c;
		private Prefix(char c) {
			this.c = c;
		}
		char getName() {
			return c;
		}
	}
	// 2. A separator (default is blank)
	public enum Separator {
		BLANK(' '),
		COLON(':'),
		EQUALS('='),
		NONE('D');
		private char c;
		private Separator(char c) {
		    this.c = c;
		}
		char getName() {
		    return c;
		}
	}
		
	// Then we define an option
	class Option {

		private Prefix prefix;
		private String key;
		private Separator sep;
		private boolean needsVal;
		
		Option(Prefix prefix, String key, Separator separator, boolean value){
			this.prefix = prefix;
			this.key = key;
			this.sep = separator;
			this.needsVal = value;
		}

		Prefix getPrefix() {
			return prefix;
		}

		void setPrefix(Prefix prefix) {
			this.prefix = prefix;
		}

		String getKey() {
			return key;
		}

		void setKey(String key) {
			this.key = key;
		}

		Separator getSep() {
			return sep;
		}

		void setSep(Separator sep) {
			this.sep = sep;
		}

		boolean getNeedsVal() {
			return needsVal;
		}

		void setNeedsVal(boolean needsVal) {
			this.needsVal = needsVal;
		}
	
	}
		
	private List<Option> options;
	private Hashtable<String,Pattern> patterns;
	private Hashtable<String,String> values;
	
	public CommandLineOptions() {
		options  = new LinkedList<Option>();
		patterns = new Hashtable<String,Pattern>();
		values   = new Hashtable<String,String>();
	}
	
	public void merge(CommandLineOptions localOps) {
		Iterator<String> it = localOps.getValues().keySet().iterator();
		while (it.hasNext()) {
			String localKey = it.next();
			setOurVal(localKey, localOps.getVal(localKey));
		}
	}

	public void add(Prefix prefix, String key, Separator separator, boolean value){
		Option o = new Option(prefix, key, separator, value);
		options.add(o);
	}
	
	public void prepare() {
		/**
		 * Prepare the parsers for each possible option
		 */
		String accents = "\u00e9\u00e8\u00ea\u00f9\u00fb\u00fc\u00f4\u00ee\u00ef\u00e2\u00c0\u00f6\u00e4"; //éèêùûüôîïâàöä
		
		for (int i=0 ; i < options.size() ; i++) {
			Option o = options.get(i);
			Prefix prefix     = o.getPrefix();
			String key        = o.getKey();
			Separator sep     = o.getSep();
			boolean needsVal  = o.getNeedsVal();
			Pattern p ;
			if (needsVal) {
				//p = java.util.regex.Pattern.compile("\""+ prefix.getName() + key + "\"" + sep.getName() + "([\\p{Punct}\"a-zA-Z"+accents+"0-9\\.\\@_\\"+File.separator+"~-]+)");
				p = java.util.regex.Pattern.compile("\""+ prefix.getName() + key + "\"" + sep.getName() + "([^"+sep.getName()+"]+)");
			}
			else {
				p = java.util.regex.Pattern.compile("\""+ prefix.getName() + key + "\"()");
			}
			patterns.put(key, p);	
		}
	}
	
	public void parse(String line) {
		/**
		 * Process the command line to find the options
		 */
		Set<String> keys = patterns.keySet();
		Iterator<String> i = keys.iterator();
		while (i.hasNext()) {
			String k = (String) i.next();			
			Pattern p = patterns.get(k);
			// try this option on the command line
			try{
				Matcher m = p.matcher(line);						
				boolean a = m.find();
				if (a) {
					//System.err.println("-- "+k+": "+m.group(1));
					values.put(k, m.group(1));
				} 
				/*
				else {
					System.err.println("Line : "+line);
					System.err.println("Pattern not found : "+p.pattern());
				}
				*/
			}catch(PatternSyntaxException pse){
				System.err.println(pse.getDescription());
			}catch(IllegalStateException ise){
				System.err.println(ise.toString());
			}
			// next option
		}
	}
	
	public Enumeration<String> getKeys() {
		return values.keys();
	}

	public Hashtable<String, String> getValues(){
		return values;
	}
	
	public String getVal(String key) {
		if (values.containsKey(key)) {
			String res = values.get(key);
			/*
			String res = "";
			try {
				res = new String(values.get(key).getBytes(), "UTF-8");
			} catch (Exception e){
				e.printStackTrace();
			}
			System.out.println(key + " " + res);
			 */
			if (res.length() > 0) {
				res = res.replace("---", " ");
				// we remove the ""
				return res.substring(1, (res.length() - 1));
			} else
				return res;
		}
		else {
			return null;
		}
	}

	public void removeAll() {
		values.clear();
	}
	
	public void removeVal(String key) {
		values.remove(key);
	}
	
	public void setOurVal(String key, String value) {
		values.put(key, "\"" + value + "\"");
	}
	
	public void setVal(String key, String value) {
		values.put(key, value);
	}
	
	public boolean check(String key) {
		return (values.containsKey(key));
	}
	
	public String toString(){
		String res = "";
		Set<String> keys = values.keySet();
		Iterator<String> it = keys.iterator();
		while(it.hasNext()) {
			String k = it.next();
			if (values.get(k).equals("")) {
				res += "option " + k + " used \n";
			} else {
				res += "option " + k + " used, value: " + values.get(k) + "\n";
			}
		}
		return res;
	}
}
