/*
 *  File GraphModel.java
 *
 *  Authors:
 *     Johannes Dellert
 *
 *  Copyright:
 *     Johannes Dellert, 2009
 *
 *  Last modified:
 *     Do 16. Apr 09:55:36 CEST 2009
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
package de.tuebingen.gui.graph;

import java.util.*;
import java.util.regex.*;
import java.io.*;

public class GraphModel {
    public HashMap<Integer, Vertex> vertices;
    public HashMap<String, Integer> dict;

    public GraphModel() {
        this.vertices = new HashMap<Integer, Vertex>();
        dict = null;
    }

    public static GraphModel loadGraphFromFile(String fileName) {
        File f = new File(fileName);
        //System.err.print("Loading graph ");
        try {
            Scanner s = new Scanner(f);
            GraphModel g = new GraphModel();
            while (s.hasNextLine()) {
                extendGraph(g, s.nextLine());
                //System.err.print(".");
            }
            //System.err.println("done");
            return g;
        } catch (FileNotFoundException e) {
            System.out.println("Couldn't read graph file " + fileName);
            System.exit(1);
        }
        return null;
    }

    public static GraphModel loadGraph(String graphString) {
        //System.out.print("Loading graph ");
        Scanner s = new Scanner(graphString);
        GraphModel g = new GraphModel();
        while (s.hasNextLine()) {
            extendGraph(g, s.nextLine());
            //System.out.print(".");
        }
        //System.out.println("done");
        return g;
    }

    public static GraphModel loadWordNetFromFile(String fileName) {
        File f = new File(fileName);
        //System.out.print("Loading wordnet ");
        try {
            Scanner s = new Scanner(f);
            GraphModel g = new GraphModel();
            int l = 0;
            while (s.hasNextLine()) {
                l++;
                extendWordNet(g, s.nextLine());
                if (l % 100 == 0) System.out.print(".");
            }
            //System.out.println("done");
            return g;
        } catch (FileNotFoundException e) {
            System.out.println("Couldn't read wordnet file " + fileName);
            System.exit(1);
        }
        return null;
    }

    public static void extendGraph(GraphModel g, String line) {
        Scanner s = new Scanner(line);
        try {
            int id = Integer.parseInt(s.next());
            String c = s.next();
            g.addVertex(id, c);
            while (s.hasNext()) {
                String e = s.next();
                String l = e.substring(1, e.indexOf(","));
                int to = Integer.parseInt(e.substring(e.indexOf(",") + 1, e.length() - 1));
                g.addDirectedEdge(id, to, l);
            }
        } catch (NoSuchElementException e) {
            System.out.println("Unable to process line: " + line);
        }
    }

    public static void extendWordNet(GraphModel g, String line) {
        Scanner s = new Scanner(line);
        try {
            int id = Integer.parseInt(s.next());
            s.next();
            s.next();
            s.next();
            String c = s.next();
            g.addVertex(id, c);
            Pattern p = Pattern.compile("[<!\\+&=]");
            while (!s.hasNext(p)) {
                s.next();
                //System.out.println("Skipping: " + s.next());
            }
            while (!s.hasNext("|")) {
                String l = s.next();
                int to = Integer.parseInt(s.next());
                s.next();
                s.next();
                g.addDirectedEdge(id, to, l);
                //System.out.println("Building edge: " + id + "_" + l + "_" + to);
            }
        } catch (NoSuchElementException e) {
            //System.out.println("Unable to process line: " + line);
        } catch (NumberFormatException e) {
            //System.out.println("Unable to process line: " + line);
        }
    }

    public void addVertex(int id, String caption) {
        vertices.put(id, new Vertex(caption));
    }

    public void addDirectedEdge(int from, int to, String label) {
        Vertex v = vertices.get(from);
        if (v != null) {
            v.addEdge(new Edge(label, to));
        }
        if (vertices.get(to) == null) {
            vertices.put(to, new Vertex("?"));
        }
    }

    public void activateFastAccessByCaption() {
        dict = new HashMap<String, Integer>();
        for (int i : vertices.keySet()) {
            dict.put(vertices.get(i).getCaption(), i);
        }
    }

    public void deactivateFastAccessByCaption() {
        dict = null;
    }

    public GraphModel getClosureByCaption(String caption, int degree) {
        if (dict != null && dict.get(caption) != null) {
            return getClosure(dict.get(caption), degree);
        }
        return new GraphModel();
    }

    public GraphModel getClosure(int id, int degree) {
        GraphModel g2 = new GraphModel();
        //System.out.println(id);
        Vertex startVertex = vertices.get(id);
        ArrayList<Integer> toProcess = new ArrayList<Integer>();
        if (startVertex != null) {
            g2.addVertex(id, startVertex.getCaption());
            toProcess.add(id);
            for (int i = 0; i < degree; i++) {
                ArrayList<Integer> nextToProcess = new ArrayList<Integer>();
                for (int v : toProcess) {
                    for (Edge e : vertices.get(v).edges) {
                        Vertex oV = vertices.get(e.getGoal());
                        if (oV != null) {
                            if (g2.vertices.get(e.getGoal()) == null || g2.vertices.get(e.getGoal()).getCaption().equals("?")) {
                                nextToProcess.add(e.getGoal());
                                g2.addVertex(e.getGoal(), oV.getCaption());
                            }
                            g2.addDirectedEdge(v, e.getGoal(), e.getLabel());
                        }
                    }
                }
                toProcess = nextToProcess;
                //System.out.println(i + ": " + toProcess);
            }
        }
        //System.out.println(g2.vertices.size());
        return g2;
    }

    public ArrayList<String> getVertexList() {
        ArrayList<String> list = new ArrayList<String>();
        for (int i : vertices.keySet()) {
            list.add(vertices.get(i).getCaption());
        }
        return list;
    }

    public ArrayList<Integer> getVertexIDs() {
        ArrayList<Integer> list = new ArrayList<Integer>();
        for (int i : vertices.keySet()) {
            list.add(i);
        }
        return list;
    }
}
