package de.duesseldorf.rrg.io;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import de.duesseldorf.frames.Fs;
import de.duesseldorf.frames.Relation;
import de.duesseldorf.frames.Value;
import de.duesseldorf.rrg.RRGNode;
import de.duesseldorf.rrg.RRGNode.RRGNodeType;
import de.duesseldorf.rrg.RRGParseResult;
import de.duesseldorf.rrg.RRGParseTree;
import de.tuebingen.tree.Node;

import java.util.LinkedList;

public class RRGXMLBuilder {

    static Map<RRGNode.RRGNodeType, XMLRRGTag> nodeTypesToXMLTags = new HashMap<RRGNode.RRGNodeType, XMLRRGTag>();
    static {
        nodeTypesToXMLTags.put(RRGNodeType.ANCHOR, XMLRRGTag.XMLANCHORNode);
        nodeTypesToXMLTags.put(RRGNodeType.DDAUGHTER,
                XMLRRGTag.XMLDDAUGHTERNode);
        nodeTypesToXMLTags.put(RRGNodeType.LEX, XMLRRGTag.XMLLEXNode);
        nodeTypesToXMLTags.put(RRGNodeType.STAR, XMLRRGTag.XMLSISADJFOOTNode);
        nodeTypesToXMLTags.put(RRGNodeType.STD, XMLRRGTag.XMLSTDNode);
        nodeTypesToXMLTags.put(RRGNodeType.SUBST, XMLRRGTag.XMLSUBSTNode);
    }

    private final RRGParseResult parseResult;
    private Document doc;
    private boolean printEdgeMismatches;

    public RRGXMLBuilder(RRGParseResult parseResult,
            boolean printEdgeMismatches) throws ParserConfigurationException {
        this.parseResult = parseResult;
        this.printEdgeMismatches = printEdgeMismatches;
        this.doc = DocumentBuilderFactory.newInstance().newDocumentBuilder()
                .newDocument();
    }

    /** create a Document representation of the parse result
     *
     * @return
     */
    public Document build() {
        // create the root element
        Element rootGrammar = doc.createElement(XMLRRGTag.GRAMMAR.StringVal());
        for (RRGParseTree parse : parseResult.getSuccessfulParses()) {
            Element entry = createEntryElement(parse);
            rootGrammar.appendChild(entry);
        }
        if (printEdgeMismatches) {
            for (RRGParseTree parse : parseResult
                    .getTreesWithEdgeFeatureMismatches()) {
                parse.setId("mismatch_" + parse.getId());
                Element entry = createEntryElement(parse);
                rootGrammar.appendChild(entry);
            }
        }
        doc.appendChild(rootGrammar);
        return doc;
    }

    /**
     * always execute the build method before!
     * wrte the document to a StreamResult
     */
    public void write(StreamResult resultStream) {
        try {
            Transformer transformer = TransformerFactory.newInstance()
                    .newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(
                    "{http://xml.apache.org/xslt}indent-amount", "2");

            DOMSource source = new DOMSource(doc);
            transformer.transform(source, resultStream);
        } catch (Exception e) {
            System.err.println(
                    "Something went wrong during output transformation");
            e.printStackTrace();
        }
    }



    /**
     * @param parse
     * @return
     */
    private Element createEntryElement(RRGParseTree parse) {
        Element entry = doc.createElement(XMLRRGTag.ENTRY.StringVal());
        entry.setAttribute(XMLRRGTag.NAME.StringVal(), parse.getId());
        Element tree = createTree(parse);
        entry.appendChild(tree);

        Element frame = createFrame(parse);
        entry.appendChild(frame);

        // used elementary trees:
        Element trace = createTrace(parse);
        entry.appendChild(trace);
        return entry;
    }

    private Element createTrace(RRGParseTree parse) {
        Element trace = doc.createElement(XMLRRGTag.TRACE.StringVal());
        for (String elemId : parse.getIds()) {
            Element classElem = doc.createElement(XMLRRGTag.CLASS.StringVal());
            classElem.setTextContent(elemId);
            trace.appendChild(classElem);
        }
        return trace;
    }

    private Element createFrame(RRGParseTree parse) {
        Element frame = doc.createElement(XMLRRGTag.FRAME.StringVal());
        for (Fs fs : parse.getFrameSem().getFeatureStructures()) {
            Element fsElem = doc.createElement(XMLRRGTag.FEATURESTRUCTURE.StringVal());
            //classElem.setTextContent(elemId);
	    fsElem = createFsElement(fsElem, fs);
            frame.appendChild(fsElem);
        }
	for (Relation rel : parse.getFrameSem().getRelations()) {
            Element relElem = doc.createElement(XMLRRGTag.RELATION.StringVal());
            //classElem.setTextContent(elemId);
	    relElem = createRelElement(relElem, rel);
            frame.appendChild(relElem);
        }
	
        return frame;
    }
    

    private Element createTree(RRGParseTree parse) {
        Element result = doc.createElement(XMLRRGTag.TREE.StringVal());
        result.setAttribute(XMLRRGTag.ID.StringVal(), parse.getId());
        Element treeRoot = createTreeRec((RRGNode) parse.getRoot());
        result.appendChild(treeRoot);
        return result;
    }

    private Element createTreeRec(RRGNode root) {
        Element result = doc.createElement(XMLRRGTag.NODE.StringVal());
        // build the node itself
        String nodeTypeString = nodeTypesToXMLTags.get(root.getType())
                .StringVal();
        result.setAttribute(XMLRRGTag.TYPE.StringVal(), nodeTypeString);
        Element narg = createnarg(root);
        result.appendChild(narg);

        for (Node child : root.getChildren()) {
            // append the children
            result.appendChild(createTreeRec((RRGNode) child));
        }
        return result;
    }

    private Element createnarg(RRGNode root) {
        Element resultnargNode = doc.createElement(XMLRRGTag.NARG.StringVal());
        Element fs = doc.createElement(XMLRRGTag.FEATURESTRUCTURE.StringVal());
        Element f = doc.createElement(XMLRRGTag.FEATURE.StringVal());
        f.setAttribute(XMLRRGTag.NAME.StringVal(), XMLRRGTag.CAT.StringVal());
        Element sym = doc.createElement(XMLRRGTag.SYM.StringVal());
        sym.setAttribute(XMLRRGTag.VALUE.StringVal(), root.getCategory());

        f.appendChild(sym);
        fs.appendChild(f);

        fs = createFsElement(fs, root.getNodeFs());

        resultnargNode.appendChild(fs);
        return resultnargNode;
    }

    private Element createRelElement(Element relElement, Relation rel){
	relElement.setAttribute(XMLRRGTag.NAME.StringVal(),rel.getName());
	for (Value v: rel.getArguments()){
	    Element sym = doc.createElement(XMLRRGTag.SYM.StringVal());
	    sym.setAttribute(XMLRRGTag.VALUE.StringVal(), v.getVarVal());
	    relElement.appendChild(sym);
	}
	return relElement;
    }

    private Element createVAltElement(Element vAltElement, Value vAlt){
	// do we really need the coref? Ignoring it for now
	LinkedList<Value> adisj = vAlt.getAdisj(); 
	// we remove the first element of the list because it is apparently the coref
	adisj.remove();

	for (Value v: adisj){
	    Element sym = doc.createElement(XMLRRGTag.SYM.StringVal());
	    sym.setAttribute(XMLRRGTag.VALUE.StringVal(), v.getSVal());
	    vAltElement.appendChild(sym);
	}
	return vAltElement;
    }

    
    private Element createFsElement(Element fsElement, Fs realfs) {
	if (realfs.isTyped()){
	    Element ctype = doc.createElement(XMLRRGTag.CTYPE.StringVal());
	    for(String etype: realfs.getType().getElementaryTypes()){
		Element sym = doc.createElement(XMLRRGTag.TYPE.StringVal());
		sym.setAttribute(XMLRRGTag.VAL.StringVal(),etype);
		ctype.appendChild(sym);
	    }
	    fsElement.appendChild(ctype);
	}
        for (Entry<String, Value> avpair : realfs.getAVlist().entrySet()) {
            Element f = doc.createElement(XMLRRGTag.FEATURE.StringVal());
            f.setAttribute(XMLRRGTag.NAME.StringVal(), avpair.getKey());

            if (avpair.getValue().is(Value.Kind.VAL)) {
                // System.out.println("first case with " + avpair.getKey() +
                // "->"
                // + avpair.getValue());
                Element sym = doc.createElement(XMLRRGTag.SYM.StringVal());
                sym.setAttribute(XMLRRGTag.VALUE.StringVal(),
                        avpair.getValue().getSVal());
                f.appendChild(sym);
            } else if (avpair.getValue().is(Value.Kind.AVM)) {
                // System.out.println("snd case with " + avpair.getKey() + "->"
                // + avpair.getValue());
                Element fsval = createFsElement(
                        doc.createElement(
                                XMLRRGTag.FEATURESTRUCTURE.StringVal()),
                        avpair.getValue().getAvmVal());
                f.appendChild(fsval);
            } else if (avpair.getValue().is(Value.Kind.VAR)) {
                Element sym = doc.createElement(XMLRRGTag.SYM.StringVal());
                sym.setAttribute("varname", avpair.getValue().getVarVal());
                f.appendChild(sym);
            } else if (avpair.getValue().is(Value.Kind.ADISJ)) {
                Element valt = doc.createElement(XMLRRGTag.VALT.StringVal());
		valt = createVAltElement(valt, avpair.getValue());
		f.appendChild(valt);
            }
	    else {
                System.err.println("ERROR during XML writing!!!"
                        + avpair.getValue().getType());
            }

            fsElement.appendChild(f);
        }
        String corefString;
        try {
            corefString = realfs.getCoref().getVarVal();
        } catch (NullPointerException e) {
            corefString = "";
        }
        // System.out.println("realfs.getCoref()" +
        // realfs.getCoref().getVarVal());
        if (corefString != "") {
            fsElement.setAttribute("coref", corefString);
        }
        return fsElement;
    }

    public RRGParseResult getParseResult() {
        return parseResult;
    }
}
