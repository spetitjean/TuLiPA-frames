package de.tuebingen.semantics;

import java.util.*;

public class ConnectednessGraph 
{
	HashMap<String, HashSet<String>> matrix;
	
	public ConnectednessGraph()
	{
		matrix = new HashMap<String, HashSet<String>>();
	}
	
	public void addEdge(String var1, String var2)
	{
		if (matrix.get(var1) == null)
		{
			matrix.put(var1, new HashSet<String>());
		}
		matrix.get(var1).add(var2);
	}
	
	public HashSet<String> getRoots()
	{
		HashSet<String> dominated = new HashSet<String>();
		HashSet<String> roots = new HashSet<String>();
		for (String s1 : matrix.keySet())
		{
			if (!dominated.contains(s1))
			{
				roots.add(s1);
			}
			HashSet<String> children = matrix.get(s1);
			for (String s2 : children)
			{
				if (roots.contains(s2))
				{
					roots.remove(s2);
				}
				dominated.add(s2);
			}
		}
		return roots;
	}
	
	public ArrayList<String> getRoots(Set<String> subgraph)
	{
		HashSet<String> dominated = new HashSet<String>();
		ArrayList<String> roots = new ArrayList<String>();
		for (String s1 : subgraph)
		{
			if (!dominated.contains(s1) && !roots.contains(s1))
			{
				roots.add(s1);
			}
			HashSet<String> children = matrix.get(s1);
			if (children != null)
			{
				for (String s2 : children)
				{
					if (roots.contains(s2))
					{
						roots.remove(s2);
					}
					dominated.add(s2);
				}
			}
		}
		return roots;
	}
	
	public ArrayList<HashSet<String>> graphPartitions()
	{
		int largestSetID = 0;
		HashMap<Integer, HashSet<String>> parts = new HashMap<Integer, HashSet<String>>();
		HashMap<String, Integer> partByVertex = new HashMap<String, Integer>();
		for (String s1 : matrix.keySet())
		{
			HashSet<String> children = matrix.get(s1);
			for (String s2 : children)
			{
				Integer s1set = partByVertex.get(s1);
				Integer s2set = partByVertex.get(s2);
				if (s1set == null && s2set == null)
				{
					HashSet<String> set = new HashSet<String>();
					set.add(s1);
					set.add(s2);
					partByVertex.put(s1, largestSetID);
					partByVertex.put(s2, largestSetID);
					parts.put(largestSetID++, set);
				}
				else if (s1set == null)
				{
					HashSet<String> set = parts.get(s2set);
                    if (set == null)
                    {
                        set = new HashSet<String>();
                        parts.put(s2set,set);
                    }
					set.add(s1);
					partByVertex.put(s1, s2set);				
				}
				else if (s2set == null)
				{
					HashSet<String> set = parts.get(s1set);
                    if (set == null)
                    {
                        set = new HashSet<String>();
                        parts.put(s1set,set);
                    }
					set.add(s2);
					partByVertex.put(s2, s1set);
				}
				else
				{
					HashSet<String> set1 = parts.get(s1set);
					HashSet<String> set2 = parts.get(s2set);
                    if (set1 == null)
                    {
                        set1 = new HashSet<String>();
                        parts.put(s1set,set1);
                    }
                    if (set2 == null)
                    {
                        set2 = new HashSet<String>();
                        parts.put(s2set,set2);
                    }
                    if (set1 != set2)
                    {
						set1.addAll(set2);
						for (String s : set2)
						{
							partByVertex.put(s, s1set);
						}
						parts.remove(s2set);		
                    }
				}
			}
		}
		ArrayList<HashSet<String>> readyParts = new ArrayList<HashSet<String>>();
		readyParts.addAll(parts.values());
		return readyParts;
	}
	
	public String toString()
	{
		String output = "";
		for (String s1 : matrix.keySet())
		{
			output += s1 + " --> ";
			HashSet<String> children = matrix.get(s1);
			for (String s2 : children)
			{
				output += s2 + " ";
			}
			output += "\n";
		}
		return output;
	}
}
