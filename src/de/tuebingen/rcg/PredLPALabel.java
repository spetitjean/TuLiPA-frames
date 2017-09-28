package de.tuebingen.rcg;

import java.util.ArrayList;
import java.util.List;

public class PredLPALabel implements PredLabel
{
	private String label;
	List<Object> lpa;

	public PredLPALabel(String label) 
	{
		this.label = label;
		this.lpa = new ArrayList<Object>();
	}
	
	public PredLPALabel(String label, List<Object> lpa) 
	{
		this.label = label;
		this.lpa = lpa;
	}
	
	
	public PredLPALabel(PredLPALabel l) 
	{
		this.label = new String(l.label);
		this.lpa = new ArrayList<Object>();
		this.lpa.addAll(l.lpa);
	}

	public String getName() 
	{
		return label;
	}

	public void setName(String name) 
	{
		this.label = name;
	}
	
	public List<Object> getLPACopy()
	{
		List<Object> cloneLPA = new ArrayList<Object>();
		cloneLPA.addAll(lpa);
		return cloneLPA;
	}

	public int hashCode() 
	{
	    return label.hashCode() + lpa.toString().hashCode();
	}

	public boolean equals(Object o) 
	{
		return o.hashCode() == this.hashCode();
	}
	
	public String toString() 
	{
		return label + lpaToString();
	}
	
	public String lpaToString()
	{
		String s = "{";
		for (Object o : lpa)
		{
			s += o + ", ";
		}
		if (s.length() > 1) s = s.substring(s.length() - 3);
		s += "}";
		return s;
	}

	@Override
	public Object clone() 
	{
		return new PredLPALabel(new String(label), getLPACopy());
	}
}
