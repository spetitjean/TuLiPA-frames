/*
 *  File DerivedTree.java
 *
 *  Authors:
 *     Johannes Dellert  <johannes.dellert@sfs.uni-tuebingen.de>
 *     David Arps <david.arps@hhu.de>
 *     Simon Petitjean <petitjean@phil.hhu.de>
 *     
 *  Copyright:
 *     Johannes Dellert, 2007
 *     David Arps, 2017
 *     Simon Petitjean, 2017
 *
 *  Last modified:
 *     2017
 *
 *  This file is part of the TuLiPA-frames system
 *     https://github.com/spetitjean/TuLiPA-frames
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
package de.tuebingen.derive;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;


import org.w3c.dom.Node;

import de.duesseldorf.frames.Frame;
import de.duesseldorf.frames.Fs;
import de.duesseldorf.frames.FsTools;
import de.duesseldorf.frames.UnifyException;
import de.duesseldorf.frames.Value;
import de.tuebingen.tag.Environment;
import de.tuebingen.tag.SemLit;
import de.tuebingen.anchoring.NameFactory;


public class DerivedTree {
    public Node root;
    public HashMap<Node, Fs> topFeatures;
    public HashMap<Node, Fs> bottomFeatures;
    public HashMap<Node, Fs> features;

    public List<SemLit> semantics;

    public Environment env;

    // information on parse success
    public boolean success = true;
    // count the terminals to filter out incomplete trees
    public int numTerminals;
    // public List<Fs> frames;
    private Frame frameSem = new Frame();

    public static boolean verbose = false;

    public DerivedTree(ElementaryTree iniTree) {
        // if elementary tree contains one anchor, increase number of terminals
        numTerminals = 0;
        if (iniTree.anchor != null)
            numTerminals++;

        root = iniTree.root;
        features = new HashMap<Node, Fs>();
        topFeatures = new HashMap<Node, Fs>();
        bottomFeatures = new HashMap<Node, Fs>();
        semantics = iniTree.semantics;
        // frames = iniTree.frames;
        // Here, copies are need, otherwise semantics of elementary trees are
        // displayed wrong
        if (iniTree.getFrameSem() != null
                && iniTree.getFrameSem().getFeatureStructures() != null)
            frameSem = new Frame(
                    new LinkedList<Fs>(
                            iniTree.getFrameSem().getFeatureStructures()),
                    new HashSet(iniTree.getFrameSem().getRelations()));
        else
            frameSem = new Frame();
        env = new Environment(0);
        addMissingBottomFeatures(iniTree.bottomFeatures);
        addMissingTopFeatures(iniTree.topFeatures);
    }

    public List<SemLit> getSemantics() {
        return semantics;
    }

    public Frame getFrameSem() {
        return frameSem;
    }

    public void setFrameSem(Frame frameSem) {
        this.frameSem = frameSem;
    }

    public void showAllFeaturesWithMarkedFailures(Node n, String feat1,
            String feat2) {
        Fs topFs = topFeatures.get(n);
        Fs botFs = bottomFeatures.get(n);
        Hashtable<String, Value> jointFs = new Hashtable<String, Value>();
        for (String key : topFs.getAVlist().keySet()) {
            if (botFs.getAVlist().get(key) != null) {
                String topValue = topFs.getAVlist().get(key).toString();
                String botValue = botFs.getAVlist().get(key).toString();
                if ((topValue.equals(feat1) && botValue.equals(feat2))
                        || (topValue.equals(feat2) && botValue.equals(feat1))) {
                    jointFs.put("top:" + key,
                            new Value(Value.Kind.VAL, "#" + topValue));
                    jointFs.put("bot:" + key,
                            new Value(Value.Kind.VAL, "#" + botValue));
                } else {
                    jointFs.put("top:" + key, topFs.getAVlist().get(key));
                    jointFs.put("bot:" + key, botFs.getAVlist().get(key));
                }
            } else {
                jointFs.put("top:" + key, topFs.getAVlist().get(key));
            }
        }
        Fs displayFs = new Fs(jointFs);
        features.put(n, displayFs);
    }

    // call with merge = true to compute the top-down merge at each node at the
    // end of the derivation
    // call with merge = false to update top and bot features according to the
    // environment for derivation step display
    public void updateTopDownFeatures(Node n, boolean merge,
            boolean finalUpdate) throws UnifyException {
        // update vars by environment
        Fs topFs = topFeatures.get(n);
        // System.out.println("Top features: "+topFs);
        if (topFs != null) {
            topFs = Fs.updateFS(topFs, env, finalUpdate);
            if (!merge)
                topFeatures.put(n, topFs);
        }
        Fs botFs = bottomFeatures.get(n);
        // System.out.println("Bot features: "+botFs);
        if (botFs != null) {
            botFs = Fs.updateFS(botFs, env, finalUpdate);
            if (!merge)
                bottomFeatures.put(n, botFs);
        }
        // merge top and bottom features
        if (merge) {
            if (topFs != null) {
                if (botFs != null) {
                    try {
                        features.put(n, FsTools.unify(topFeatures.get(n),
                                bottomFeatures.get(n), env));
                    } catch (UnifyException e) {
                        String feat1 = e.getFeat1();
                        String feat2 = e.getFeat2();
                        showAllFeaturesWithMarkedFailures(n, feat1, feat2);
                        throw new UnifyException(e.getMessage());
                    }
                } else {
                    features.put(n, topFeatures.get(n));
                }
                if (verbose)
                    topFeatures.get(n).setFeatWithoutReplace("hash", new Value(
                            Value.Kind.VAL, Integer.toString(n.hashCode())));
            } else if (botFs != null) {
                features.put(n, bottomFeatures.get(n));
            }
        }
        // update child node features
        for (int i = 0; i < n.getChildNodes().getLength(); i++) {
            updateTopDownFeatures(n.getChildNodes().item(i), merge,
                    finalUpdate);
        }
        // System.out.println("\nFinal features: "+ features);
    }


    public void updateFeatures(Node n, Environment eEnv, NameFactory nf, boolean finalUpdate)
            throws UnifyException {
        // update vars by environment
        Fs fs = features.get(n);
        if (fs != null) {
            fs = Fs.updateFS(fs, eEnv, finalUpdate);
            features.put(n, fs);
        }
        // update child node features
        for (int i = 0; i < n.getChildNodes().getLength(); i++) {
            updateFeatures(n.getChildNodes().item(i), eEnv, nf, finalUpdate);
        }

    }

    public boolean postUpdateFeatures(Node n, Environment eEnv, NameFactory nf, boolean finalUpdate)
    {
        // update vars by environment
        Fs fs = features.get(n);
        if (fs != null) {
            if (fs.collect_corefs(eEnv, nf, new HashSet<Value>()) == false) {
                return false;
            }
            features.put(n, fs);
        }
        // update child node features
        for (int i = 0; i < n.getChildNodes().getLength(); i++) {
            if(!postUpdateFeatures(n.getChildNodes().item(i), eEnv, nf, finalUpdate))
		return false;
        }
	return true;
    }

    public Boolean postPostUpdateFeatures(Node n, Environment eEnv, NameFactory nf, boolean finalUpdate)
	throws UnifyException
    {
        // update vars by environment
        Fs fs = features.get(n);
        if (fs != null) {
	    Fs newFs=fs.update_corefs(eEnv, new HashSet<Value>());
	    if(newFs==null){
		return false;}
	    features.put(n, fs);
        }
        // update child node features
        for (int i = 0; i < n.getChildNodes().getLength(); i++) {
            if(!postPostUpdateFeatures(n.getChildNodes().item(i), eEnv, nf, finalUpdate))
		return false;
        }
	return true;
	
    }
    
    public void addMissingBottomFeatures(HashMap<Node, Fs> botFs) {
        for (Node n : botFs.keySet()) {
            if (bottomFeatures.get(n) == null && botFs.get(n) != null) {
                bottomFeatures.put(n, botFs.get(n));
            }
        }
    }

    public void addMissingTopFeatures(HashMap<Node, Fs> topFs) {
        for (Node n : topFs.keySet()) {
            if (topFeatures.get(n) == null && topFs.get(n) != null) {
                topFeatures.put(n, topFs.get(n));
            }
        }
    }
}
