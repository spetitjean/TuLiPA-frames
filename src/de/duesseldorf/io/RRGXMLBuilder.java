package de.duesseldorf.io;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import de.duesseldorf.rrg.RRGNode;
import de.duesseldorf.rrg.RRGNode.RRGNodeType;
import de.duesseldorf.rrg.RRGParseTree;
import de.duesseldorf.rrg.io.XMLRRGTag;
import de.tuebingen.tree.Node;

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

    private Set<RRGParseTree> parseResult;
    private Document doc;
    private StreamResult resultStream;

    public RRGXMLBuilder(StreamResult resultStream,
            Set<RRGParseTree> parseResult) throws ParserConfigurationException {
        this.resultStream = resultStream;
        this.parseResult = parseResult;
        this.doc = DocumentBuilderFactory.newInstance().newDocumentBuilder()
                .newDocument();
    }

    public void buildAndWrite() {
        // create the root element
        Element rootGrammar = doc.createElement(XMLRRGTag.GRAMMAR.StringVal());
        for (RRGParseTree parse : parseResult) {
            Element entry = doc.createElement(XMLRRGTag.ENTRY.StringVal());
            entry.setAttribute(XMLRRGTag.NAME.StringVal(), parse.getId());
            Element tree = createTree(parse);
            entry.appendChild(tree);

            // At the moment, we don't have a frame. But the empty element is
            // needed to display on the WebGUI properly
            Element frame = doc.createElement(XMLRRGTag.FRAME.StringVal());
            entry.appendChild(frame);

            rootGrammar.appendChild(entry);
        }
        doc.appendChild(rootGrammar);
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
        resultnargNode.appendChild(fs);
        return resultnargNode;
    }
}