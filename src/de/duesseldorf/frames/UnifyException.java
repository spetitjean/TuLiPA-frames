/*
 *  File UnifyException.java
 *
 *  Authors:
 *     Yannick Parmentier  <parmenti@sfs.uni-tuebingen.de>
 *     
 *  Copyright:
 *     Yannick Parmentier, 2007
 *
 *  Last modified:
 *     Di 16. Okt 09:50:14 CEST 2007
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
package de.duesseldorf.frames;

/**
 * 
 * @author parmenti
 *
 */
public class UnifyException extends Exception {
	
	/**
	 * Class used to handle feature unification exceptions 
	 */
	private static final long serialVersionUID = 1L;
    
    private String feat1;
    private String feat2;

	public UnifyException(){
		super();
	}
    
    public UnifyException(String feat1, String feat2)
    {
        super("Unification failure between " + feat1 + " and " + feat2);
        this.feat1 = feat1;
        this.feat2 = feat2;
    }
	
	public UnifyException(String message){
		super(message);
	}
	
	public UnifyException(Throwable cause){
		super(cause);
	}
	
	public UnifyException(String message, Throwable cause){
		super(message, cause);
	}

    public String getFeat1()
    {
        return feat1;
    }

    public String getFeat2()
    {
        return feat2;
    }
}
