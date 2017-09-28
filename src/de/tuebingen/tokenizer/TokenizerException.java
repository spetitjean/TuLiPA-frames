/*
 *  File TokenizerException.java
 *
 *  Authors:
 *     Wolfgang Maier  <wo.maier@uni-tuebingen.de>
 *     
 *  Copyright:
 *     Wolfgang Maier, 2007
 *
 *  Last modified:
 *     Di 16. Okt 09:42:20 CEST 2007
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

/**
 * Indicates a problem within the tokenizer.
 * 
 * @author wmaier
 *
 */
public class TokenizerException extends Exception {

	private static final long serialVersionUID = 2305202443769388502L;

	/**
	 *  An exception within the tokenizer has occured 
	 */
	public TokenizerException() {
	}

	/**
	 *  An exception within the tokenizer has occured 
	 * @param arg0 a suitable message
	 */
	public TokenizerException(String arg0) {
		super(arg0);
	}

	/**
	 *  An exception within the tokenizer has occured 
	 * @param arg0
	 */
	public TokenizerException(Throwable arg0) {
		super(arg0);
	}

	/**
	 *  An exception within the tokenizer has occured 
	 * @param arg0
	 * @param arg1
	 */
	public TokenizerException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

}
