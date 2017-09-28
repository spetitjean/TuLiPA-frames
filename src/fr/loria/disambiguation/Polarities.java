/*
 *  File Polarities.java
 *
 *  Authors:
 *     Yannick Parmentier  <parmenti@loria.fr>
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
package fr.loria.disambiguation;

import java.util.*;

public class Polarities {
	
	public static final int MINUS   = -1;
	public static final int NEUTRAL =  0;
	public static final int PLUS    =  1;
	
	private Map<String, Integer>           charges; // i.e. n: +1
	private Map<String, Integer>       leftCharges; // i.e. n: +1
	
	public Polarities() {
		charges        = new HashMap<String, Integer>();
		leftCharges    = new HashMap<String, Integer>();
	}
	
	public Polarities(String label, String v, int polarity) {
		this();
		setPol(label, v, polarity);
		setLeftPol(label, v, polarity);
	}
	
	public boolean isNotLeftPositive() {
		boolean res = false;
		Iterator<String> it = leftCharges.keySet().iterator();
		while(it.hasNext() && !res) {
			String key = it.next();
			Integer pol = leftCharges.containsKey(key) ? leftCharges.get(key) : 0;
			res = (pol < 0); 
			// NB: as soon as res is true, we exit the loop and return true
			//if (res) 
				//System.err.println("Left context is negative for polarity " + key);
		}
		return res;
	}
	
	public void definePol(String label, String v, int polarity, Map<String, Integer> typeCharges) {
		int sumPol    = typeCharges.containsKey(label) ? typeCharges.get(label) : 0;
		if ((sumPol+polarity) != 0) 
			typeCharges.put(label, sumPol + polarity);
		else
			typeCharges.remove(label);
	}
	
	public void setPol(String label, String v, int polarity) {
		if (polarity != 0)
			this.definePol(label, v, polarity, charges);
	}
	
	public void setLeftPol(String label, String v, int polarity) {
		if (polarity != 0)
			this.definePol(label, v, polarity, leftCharges);
	}
	
	public Map<String, Integer> getCharges() {
		return charges;
	}
	
	public Map<String, Integer> getLeftCharges() {
		return leftCharges;
	}

	public static Polarities add(Polarities p1, Polarities p2, boolean withLeftContext)
	{
		boolean problem = false;
	    Polarities addedPolarities = new Polarities();
	    for (String s : p1.charges.keySet())
	    {
	        addedPolarities.charges.put(s, p1.charges.get(s));
	    }
	    for (String s : p1.leftCharges.keySet())
	    {
	        addedPolarities.leftCharges.put(s, p1.leftCharges.get(s));
	    }
	    for (String s : p2.charges.keySet())
        {
            if (!(addedPolarities.charges.containsKey(s)))
            {
                addedPolarities.charges.put(s, p2.charges.get(s));
            }
            else
            {
            	if (addedPolarities.charges.get(s) + p2.charges.get(s) != 0)
            		addedPolarities.charges.put(s, addedPolarities.charges.get(s) + p2.charges.get(s));
            	else
            		addedPolarities.charges.remove(s);
            }
        }
	    for (String s : p2.leftCharges.keySet())
	    {
            if (!(addedPolarities.leftCharges.containsKey(s)))
            {
    	        addedPolarities.leftCharges.put(s, p2.leftCharges.get(s));
            }
            else
            {
            	if (addedPolarities.leftCharges.get(s) + p2.getLeftCharges().get(s) != 0)
            		addedPolarities.leftCharges.put(s, addedPolarities.leftCharges.get(s) + p2.getLeftCharges().get(s));
            	else 
            		addedPolarities.leftCharges.remove(s);
            }
            if (withLeftContext) {
            	// check: all left polarities must be positive
            	problem |= addedPolarities.isNotLeftPositive();
            }
        }
       if (problem)
    	   return null;
       else
    	   return addedPolarities;
	}
	
	public String toString(Map<String, Integer> typeCharges) {
		String res = "";
		Set<String> keys = typeCharges.keySet();
		Iterator<String> it = keys.iterator();
		while (it.hasNext()) {
			String label = it.next();
			res += "< " + label + ": ";
			Integer c = typeCharges.get(label);
			res += c;
			res += ">\n";			
		}
		return res;
	}


	public String toString () {
		String res = "";
		res += "Global: \n";
		res += this.toString(charges);
		res += "Left: \n";
		res += this.toString(leftCharges);		
		return res;
	}
	
	public int hashCode() {
		return this.toString().hashCode();
	}
	
	public boolean equals(Object other) {
		boolean res = true;
		for (String s : charges.keySet()) {
			res &= ((Polarities) other).charges.containsKey(s) && ((Polarities) other).charges.get(s) == charges.get(s);
			if (!res)
				return res;
		}
		for (String s : leftCharges.keySet()) {
			res &= ((Polarities) other).leftCharges.containsKey(s) && ((Polarities) other).leftCharges.get(s) == leftCharges.get(s);
			if (!res)
				return res;
		}
		return res;
	}

}

