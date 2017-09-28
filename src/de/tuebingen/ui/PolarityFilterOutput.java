package de.tuebingen.ui;

import java.util.Map;

import de.tuebingen.tag.TagTree;

public class PolarityFilterOutput {
	
	private String                prettyRes = null;
	private Map<String, TagTree> subgrammar = null;
	
	public PolarityFilterOutput() {}
	
	public PolarityFilterOutput (String r, Map<String, TagTree> map) {
		prettyRes = r;
		subgrammar= map; 
	}

	public String getPrettyRes() {
		return prettyRes;
	}

	public void setPrettyRes(String prettyRes) {
		this.prettyRes = prettyRes;
	}

	public Map<String, TagTree> getSubgrammar() {
		return subgrammar;
	}

	public void setSubgrammar(Map<String, TagTree> subgrammar) {
		this.subgrammar = subgrammar;
	}

}
