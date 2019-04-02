package de.duesseldorf.io;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import de.duesseldorf.frames.Fs;
import de.duesseldorf.frames.Type;
import de.duesseldorf.frames.Value;
import de.tuebingen.anchoring.NameFactory;
import de.tuebingen.io.XMLTTMCTAGReader;

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
                        res = XMLGrammarReadingTools.getFeats(el,
                                XMLTTMCTAGReader.NOFS, toAdd, nf);
                    }
                }
            }
            if (from == XMLTTMCTAGReader.FROM_NODE && toAdd.size() > 0) {
                // we post-process the features to add
                Value top = res.getFeat("top");
                if (top == null) {
                    top = new Value(new Fs(5));
                    res.setFeatWithoutReplace("top", top);
                }
                Value bot = res.getFeat("bot");
                if (bot == null) {
                    bot = new Value(new Fs(5));
                    res.setFeatWithoutReplace("bot", bot);
                }

                Set<String> keys = toAdd.keySet();
                Iterator<String> it = keys.iterator();
                while (it.hasNext()) {
                    String f = it.next();
                    if (!(top.getAvmVal().hasFeat(f))) {
                        top.getAvmVal().setFeatWithoutReplace(f, toAdd.get(f));
                    }
                    if (!(bot.getAvmVal().hasFeat(f))) {
                        bot.getAvmVal().setFeatWithoutReplace(f, toAdd.get(f));
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return res;
    }

    /**
     * Process a fs XML tag to extract a TagNode label (class Fs)
     * 
     * @param e
     *            the DOM Element corresponding to the fs feature structure
     * 
     */
    public static Fs getFeats(Element e, int type,
            Hashtable<String, Value> toAdd, NameFactory nf) {
        // NB: an fs XML element has f element as children
        Fs res = null;
        String coref = e.getAttribute("coref");
        Value corefval = new Value(Value.Kind.VAR, nf.getName(coref));

        NodeList etypes = null;
        NodeList l = e.getChildNodes();

        // NodeList etypes = e.getElementsByTagName("type");
        for (int i = 0; i < l.getLength(); i++) {
            Node n = l.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                Element el = (Element) n;
                if (el.getTagName().equals("ctype")) {
                    etypes = el.getElementsByTagName("type");
                }
            }
        }

        Set<String> types = new HashSet<String>();
        if (etypes != null) {
            for (int i = 0; i < etypes.getLength(); i++) {
                Node n = etypes.item(i);
                Element el = (Element) n;
                // System.out.println("Type " + el.getAttribute("val"));
                types.add(el.getAttribute("val"));
            }
        }
        Type frame_type = new Type(types);
        // System.out.println("Found a type: "+frame_type);

        res = new Fs(l.getLength(), frame_type, corefval);

        for (int i = 0; i < l.getLength(); i++) {
            Node n = l.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                Element el = (Element) n;
                if (el.getTagName().equals("f")) {
                    String key = el.getAttribute("name");
                    Value val = null;
                    if (type == XMLTTMCTAGReader.TOP || key.equals("top")) {
                        val = XMLTTMCTAGReader.getVal(el, XMLTTMCTAGReader.TOP,
                                toAdd, key, nf);
                    } else if (type == XMLTTMCTAGReader.BOT
                            || key.equals("bot")) {
                        val = XMLTTMCTAGReader.getVal(el, XMLTTMCTAGReader.BOT,
                                toAdd, key, nf);
                    } else {
                        val = XMLTTMCTAGReader.getVal(el, XMLTTMCTAGReader.NOFS,
                                toAdd, key, nf);
                    }
                    res.setFeatWithoutReplace(key, val);
                }
            }
        }
        return res;
    }

}
