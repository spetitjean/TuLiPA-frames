package de.duesseldorf.io;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import de.tuebingen.anchoring.NameFactory;
import de.tuebingen.io.XMLTTMCTAGReader;
import de.tuebingen.tag.Fs;
import de.tuebingen.tag.Value;

public class XMLGrammarReadingTools {

    /**
     * Process a narg XML tag to extract a TagNode label
     * 
     * @param e
     *            the DOM Element corresponding to the feature structure
     * 
     */
    public static Fs getNarg(Element e, int from, NameFactory nf) {
        Fs res = null;
        try {
            NodeList l = e.getChildNodes();
    
            // we declare the hash that will be used for features
            // that have to be added to both top and bot
            Hashtable<String, Value> toAdd = new Hashtable<String, Value>();
            for (int i = 0; i < l.getLength(); i++) {
                Node n = l.item(i);
                if (n.getNodeType() == Node.ELEMENT_NODE) {
                    Element el = (Element) n;
                    if (el.getTagName().equals("fs")) {
                        res = XMLTTMCTAGReader.getFeats(el, XMLTTMCTAGReader.NOFS, toAdd, nf);
                    }
                }
            }
            if (from == XMLTTMCTAGReader.FROM_NODE && toAdd.size() > 0) {
                // we post-process the features to add
                Value top = res.getFeat("top");
                if (top == null) {
                    top = new Value(new Fs(5));
                    res.setFeat("top", top);
                }
                Value bot = res.getFeat("bot");
                if (bot == null) {
                    bot = new Value(new Fs(5));
                    res.setFeat("bot", bot);
                }
    
                Set<String> keys = toAdd.keySet();
                Iterator<String> it = keys.iterator();
                while (it.hasNext()) {
                    String f = it.next();
                    if (!(top.getAvmVal().hasFeat(f))) {
                        top.getAvmVal().setFeat(f, toAdd.get(f));
                    }
                    if (!(bot.getAvmVal().hasFeat(f))) {
                        bot.getAvmVal().setFeat(f, toAdd.get(f));
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return res;
    }

}
