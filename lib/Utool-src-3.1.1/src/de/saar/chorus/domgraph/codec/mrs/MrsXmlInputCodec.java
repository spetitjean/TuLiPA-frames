package de.saar.chorus.domgraph.codec.mrs;

import java.io.IOException;
import java.io.Reader;
import java.util.Map;
import java.util.Stack;
import java.util.TreeMap;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import de.saar.chorus.domgraph.codec.CodecMetadata;
import de.saar.chorus.domgraph.codec.CodecConstructor;
import de.saar.chorus.domgraph.codec.CodecOption;
import de.saar.chorus.domgraph.codec.InputCodec;
import de.saar.chorus.domgraph.codec.MalformedDomgraphException;
import de.saar.chorus.domgraph.codec.ParserException;
import de.saar.chorus.domgraph.graph.DomGraph;
import de.saar.chorus.domgraph.graph.NodeLabels;

@CodecMetadata(name="mrs-xml", extension=".mrs.xml")
public class MrsXmlInputCodec extends InputCodec {
	
	private MrsCodec codec;
	private Normalisation normalisation;

	@CodecConstructor
	public MrsXmlInputCodec(
		@CodecOption(name="normalisation", defaultValue="nets") Normalisation normalisation)
	{
		super();
		this.normalisation = normalisation;
	}
	
	
	private class XmlParser extends DefaultHandler {
		
		private StringBuilder chars;
		private Stack<String> parents;
		private Map<String,String> attrs;
		private String attr;
		private String top;
		private String label;
		private String value;
		private String hi;
		private String lo;
		private String handle;
		
		public XmlParser()
		{
			super();
			this.chars = new StringBuilder();
			this.parents = new Stack<String>();
			this.attrs = new TreeMap<String,String>();
			this.top = "";
			this.handle = "";
			this.label = "";
			this.value = "";
			this.hi = "";
			this.lo = "";
		}
		
		public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException
		{
			if (qName.equals("var")) {
				String vid = attributes.getValue("vid");
				String parent = parents.peek();
				
				if (vid.startsWith("x")) {
					try {
						codec.tellVariable(vid); 
					} catch (MalformedDomgraphException e) {
						throw new SAXException(e);
					}
				} else if (vid.startsWith("h")) {
					try {
						codec.tellHandle(vid); 
					} catch (MalformedDomgraphException e) {
						throw new SAXException(e);
					} 
				} 
				
				if (parent.equals("mrs")) {
					this.top = vid;
				} else if (parent.equals("ep")) {
					this.handle = vid;
				} else if (parent.equals("fvpair")) {
					this.value = vid;
				} else if (parent.equals("hi")) {
					this.hi = vid;
				} else if (parent.equals("lo")) {
					this.lo = vid;
				}
			} else if (qName.equals("ep")) {
				attrs = new TreeMap<String,String>();
			}
			chars = new StringBuilder();
			parents.push(qName);
		}
		
		public void endElement (String uri, String name, String qName) throws SAXException
		{
			if (qName.equals("mrs")) {
				try {
					codec.setTopHandleAndFinish(top);	
				} catch (MalformedDomgraphException e) {
					throw new SAXException(e);
				}
			} else if (qName.equals("ep")) {
				try {
					codec.addRelation(handle, label, attrs);
				} catch (MalformedDomgraphException e) {
					throw new SAXException(e);
				}
			} else if (qName.equals("hcons")) {
				codec.addDomEdge(hi, lo);  			
			} else if (qName.equals("fvpair")) {
				attrs.put(attr, value);
			} else if (qName.equals("pred")) {
				label = chars.toString();
			} else if (qName.equals("rargname")) {
				attr = chars.toString();
			} else if (qName.equals("constant")) {
				value = chars.toString();
			}
			
			parents.pop();
		}
		
		public void characters (char ch[], int start, int length)
		{		
			chars.append(ch, start, length);
		}
	}
	
	public void decode(Reader inputStream, DomGraph graph, NodeLabels labels) throws MalformedDomgraphException, IOException, ParserException
	{
		codec = new MrsCodec(graph, labels, normalisation);
		
		try {
			SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
			parser.parse(new InputSource(inputStream), new XmlParser());		
		} catch(IOException e) {
			throw e;
		} catch(SAXException e) {
			if( (e.getException() != null) && (e.getException() instanceof MalformedDomgraphException) ) {
				throw (MalformedDomgraphException) e.getException();
			} else {			
				throw new ParserException(e);
			}
		} catch(ParserConfigurationException e) {
			throw new ParserException(e);
		}
	}
	
}
