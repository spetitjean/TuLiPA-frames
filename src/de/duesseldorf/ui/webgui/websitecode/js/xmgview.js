// xmgview.js

// Timestamp: 2017-09-29 Fri

// Draws beautiful, interactive SVG trees and feature structures based on XMG's boring XML output.
// Written by Timm Lichte (lichte@phil.hhu.de), 2017.
// Some extensions and maintenance by Simon Petitjean (petitjean@phil.hhu.de), 2018--. 

// TODO: revisit padding variables
var offsetxLabel = 3;
var offsetxFS = 0;
var offsetxFSval = 10;
var offsetxFSfeat = 5;
var offsetxNode = 40;
var offsetyNode = 50;

// global variables 
var svgRoot,										// root element of the svg tree
		entryName;									// name of the grammar entry 

// draw tree (standalone) 
function standaloneTree(){
    makeTree(document.getElementsByTagName("svg")[0],document.getElementsByTagName("entry")[0])
}

// draw frame (standalone)
function standaloneFrame() {
    makeFrame(document.getElementsByTagName("svg")[0],document.getElementsByTagName("entry")[0]);
    
}


// makeTree is used in the webgui
function makeTree(target,entry) {
    entryName = entry.getAttribute("name");
    svgRoot = target;
    var syntree;
    var tree=entry.getElementsByTagName("tree")[0];
    
    // if there is no tree element, create an empty one
    if (tree == null) {
	console.log("No tree description");
	var empty_tree = document.createElementNS("http://www.w3.org/2000/svg","svg");
	empty_tree.setAttribute("type","tree");
	empty_tree.setAttribute("id","synTreeSVG");
	svgRoot.appendChild(empty_tree);
    }

    // in case there is no tree, look for a morph description
    if(tree==null){
	if(entry.getElementsByTagName("morph")[0]!=null){
	    processMorph(entry.getElementsByTagName("morph")[0],empty_tree);
	}
    }
    else{
	// turn tree elements into daughters of the SVG root
	transformTree(tree,svgRoot);  // TODO: remove second argument?
    }
    
    for (var i = 0; i < svgRoot.children.length; i++) {
	if (svgRoot.children[i].getAttribute("type") == "tree") {
	    syntree = svgRoot.children[i];
	    syntree.setAttribute("id","synTreeSVG");
	}
    }	
    
    processTree(svgRoot);
    addTreeButtons(svgRoot);
}


// display a morph description: a string and a sequence of "fields" paired with frames
function processMorph(morph,target){
    var textsvg = document.createElementNS("http://www.w3.org/2000/svg","svg");
    target.appendChild(textsvg);

    // display the string
    var text = document.createElementNS("http://www.w3.org/2000/svg","text");
    textsvg.appendChild(text);
    text.setAttribute("font-size",25);
    text.setAttribute("text-anchor","start");
    text.setAttribute("y",40);
    text.setAttribute("x",0);
    text.innerHTML=morph.getElementsByTagName("string")[0].getAttribute("value");
    
    //text.setAttribute("x",pos+5);
    //text.setAttribute("y",y+"em");
    
    function makeSVGText(label,svgtexttarget,pos){
	var textsvg = document.createElementNS("http://www.w3.org/2000/svg","svg");
	svgtexttarget.appendChild(textsvg);
	var text = document.createElementNS("http://www.w3.org/2000/svg","text");
	textsvg.appendChild(text);
	text.setAttribute("font-size",25);
	text.setAttribute("text-anchor","start");
	text.setAttribute("x",pos+5);
	text.setAttribute("y",90);
	text.innerHTML=label;
	return pos+text.getBBox().width+2;
    }

    // display the fields
    var fields=morph.getElementsByTagName("fields")[0];
    var pos=0;
    for(var i=0; i < fields.children.length; i++){
	// separator
	if(i>0){
	    pos=makeSVGText("-",textsvg,pos);
	}
	pre_pos=pos;
	// string part
	pos=makeSVGText(fields.children[i].getElementsByTagName("string")[0].getAttribute("value"),textsvg,pos);

	// frame part
	var new_frame = document.createElementNS("http://www.w3.org/2000/svg","svg");
	new_frame.setAttribute("type","frame");
	textsvg.appendChild(new_frame);
	
	var feats=fields.children[i].getElementsByTagName("feats")[0];
	for(var j=0; j<feats.children.length; j++){
	    transformFS(feats.children[j],new_frame);
	}
	y_all=0;
	for (var j = 0; j < new_frame.children.length; j++) {
	    var one_new_frame=new_frame.children[j];
	    if (one_new_frame.getAttribute("type")=="fs") {
		processFS(one_new_frame);
		one_new_frame.setAttribute("y",110+y_all); 
		one_new_frame.setAttribute("x",pre_pos);
		pos=pos+one_new_frame.getBBox().width+2;
		y_all=y_all+one_new_frame.getBBox().height+5;
	    }
	}
    }
    textsvg.setAttribute("id","synTreeSVG");
}


// makeFrame is used in the webgui
function makeFrame(target,entry) {
    //console.log("Here make Frame");
    entryName = entry.getAttribute("name");
    svgRoot = target;
    
    // add new svg element that contains the frame 
    var new_frame = document.createElementNS("http://www.w3.org/2000/svg","svg");
    new_frame.setAttribute("type","frame");
    new_frame.setAttribute("id","semFrameSVG");
    // if there is no frame element, stop here
    if (entry.getElementsByTagName("frame")[0] == null) {
	console.log("No frame to display, but maybe pred");
	// this should be done in script.js, but I somehow cannot make changes in it
	if(entry.getElementsByTagName("semantics")[0] != null)
	    makePred(target,entry);
	return;
    }	
    if (entry.getElementsByTagName("frame")[0].children.length==0) {
	console.log("No frame to display (frame dimension is here but empty)");
	return;
	
    }	
    svgRoot.appendChild(new_frame);
    var frame = entry.getElementsByTagName("frame")[0];
    var ypoint = 3;
    
    // frame descriptions may consist of separate components
    for (var i = 0; i < frame.children.length; i++) {
	transformFS(frame.children[i],new_frame);  // TODO: remove second argument?			
    }
    for (var i = 0; i < new_frame.children.length; i++) {
	if (new_frame.children[i].getAttribute("type") == "fs") {
	    var fs = new_frame.children[i];
	    processFS(fs);
	    fs.setAttribute("y",ypoint);  // paddin
	    var fsHeight = fs.getBBox().height;
	    // plot label of overall frame 
	    if (fs.hasAttribute("label")) {
		addLabel(fs.getAttribute("label"),new_frame);
		var label = new_frame.lastElementChild;
		var labelSize = label.getBBox();
		fs.setAttribute("x",labelSize.width + 5);  // padding
		new_frame.lastElementChild.setAttribute("y",fsHeight > labelSize.height ? fsHeight/2 - labelSize.height/2 + ypoint : ypoint);
	    }
	    ypoint += fsHeight + 20;  // padding
	}
    }
    num=0;
    for (var i = 0; i < frame.children.length; i++) {
	
	num=printRelation(frame.children[i],new_frame,ypoint,num);  // TODO: remove second argument?			
    }
    
    addFrameButtons(svgRoot);
    console.log(svgRoot);
}



function makePred(target,entry){

    function makeSVGBox(label,svgboxtarget,pos){
	var textsvg = document.createElementNS("http://www.w3.org/2000/svg","svg");
	textsvg.setAttribute("type","label");
	svgboxtarget.appendChild(textsvg);
	
	var text = document.createElementNS("http://www.w3.org/2000/svg","text");
	textsvg.appendChild(text);
	text.setAttribute("font-size",25);
	text.setAttribute("text-anchor","start");
	text.setAttribute("x",pos+5);
	text.setAttribute("y",y+"em");
	text.innerHTML = label;
	
	var box = document.createElementNS("http://www.w3.org/2000/svg","rect");
	textsvg.appendChild(box);
	box.setAttribute("x", text.getBBox().x - 1.2);
	box.setAttribute("y", text.getBBox().y - 1.2);
	box.setAttribute("width", text.getBBox().width + 2.4);
	box.setAttribute("height", text.getBBox().height +2.4);
	box.setAttribute("name",label);
	box.setAttribute("cursor","pointer");
	box.setAttribute("onclick","highlightLabel(evt)");
	box.setAttribute("style", "stroke:black; fill:transparent;");

	return pos+text.getBBox().width+2;
	
    }

    function makeSVGText(label,svgtexttarget,pos){
	var textsvg = document.createElementNS("http://www.w3.org/2000/svg","svg");
	svgtexttarget.appendChild(textsvg);
	var text = document.createElementNS("http://www.w3.org/2000/svg","text");
	textsvg.appendChild(text);
	text.setAttribute("font-size",25);
	text.setAttribute("text-anchor","start");
	text.setAttribute("x",pos+5);
	text.setAttribute("y",y+"em");
	text.innerHTML=label;
	return pos+text.getBBox().width+2;
    }

    
    //console.log("Here make Pred");
    entryName = entry.getAttribute("name");
    svgRoot = target;
    var new_pred = document.createElementNS("http://www.w3.org/2000/svg","svg");
    new_pred.setAttribute("type","pred");
    new_pred.setAttribute("id","sempredSVG");
    svgRoot.appendChild(new_pred);
    var pred = entry.getElementsByTagName("semantics")[0];
    var ypoint = 3;


    
    // semantics is a list of literals and semdominances
    for (var i = 0; i < pred.children.length; i++){
	var nodename = pred.children[i].nodeName;
	var y=1.5*(i+1);

	if(nodename=="semdominance"){
	    var pos=0;
	    var domsvg = document.createElementNS("http://www.w3.org/2000/svg","svg");
	    new_pred.appendChild(domsvg);
	    var semdom = pred.children[i];
	    if (semdom.children[0].hasAttribute("varname")) {		
		var left = semdom.children[0].getAttribute("varname").replace("@","");
	    }
	    else{
		var left = semdom.children[0].getAttribute("value");
	    }

	    if (semdom.children[1].hasAttribute("varname")) {
		var right = semdom.children[1].getAttribute("varname").replace("@","");
	    }
	    else{
		var right = semdom.children[1].getAttribute("value");
	    }


	    pos=makeSVGBox(left,domsvg,pos);
	    pos=makeSVGText(">>",domsvg,pos);
	    pos=makeSVGBox(right,domsvg,pos);

	}
	
	if(nodename=="literal"){
	    var pos = 0;
	    var litsvg = document.createElementNS("http://www.w3.org/2000/svg","svg");
	    new_pred.appendChild(litsvg);
	    
	    var literal = pred.children[i];
	    // negation is here:
	    // literal.getAttribute("negated")
	    // label is literal.children[0]
	    // predicate is literal.children[1]
	    // args are literal.children[2...]
	    
	    if (literal.children[1].children[0].hasAttribute("varname")) {
		var predicate = literal.children[1].children[0].getAttribute("varname").replace("@","");
	    }
	    else{
		var predicate = literal.children[1].children[0].getAttribute("value");
	    }
	    
	    if (literal.children[0].children[0].hasAttribute("varname")){
		var label = literal.children[0].children[0].getAttribute("varname").replace("@","");
	    }
	    else{
		// for some reason values can be variables in labels
		var label = literal.children[0].children[0].getAttribute("value").replace("@","");
	    }
	    
	    if (literal.children[2].children[0].hasAttribute("varname")){
		var arg0 = literal.children[2].children[0].getAttribute("varname").replace("@","");
	    }
	    else{
		var arg0 = literal.children[2].children[0].getAttribute("value");
	    }
	    
	    
	    // so it is LABEL : PREDICATE ( ARG1 , ... , ARGN )

	    pos=makeSVGBox(label,litsvg,pos);

	    pos=makeSVGText(":",litsvg,pos);
	    
	    pos=makeSVGBox(predicate,litsvg,pos);
	    
	    pos=makeSVGText("(",litsvg,pos);

	    
	    // add other args
	    for (var j = 2; j< literal.children.length; j++){
		if(j>2){
		    pos=makeSVGText(",",litsvg,pos);
		}
	    	if (literal.children[j].children[0].hasAttribute("varname")){
	    	    var arg= literal.children[j].children[0].getAttribute("varname").replace("@","");
	    	}
	    	else{
	    	    var arg= literal.children[j].children[0].getAttribute("value");
	    	}

		pos=makeSVGBox(arg,litsvg,pos);
		
	    }

	    pos=makeSVGText(")",litsvg,pos);


	}
    }

    addFrameButtons(svgRoot); 
}

function makeTrace(target,entry){
    svgRoot = target;
    // add new svg element that contains the interface 
    var new_trace = document.createElementNS("http://www.w3.org/2000/svg","svg");
    new_trace.setAttribute("type","trace");
    new_trace.setAttribute("id","TraceSVG");
    // if there is no trace element, stop here
    if (entry.getElementsByTagName("trace")[0] == null) {
		    console.log("No trace to display");
		    return;
		}	
    svgRoot.appendChild(new_trace);
    var trace = entry.getElementsByTagName("trace")[0];
    for (var i = 0; i < trace.children.length; i++){
	var y=1.5*(i+1);
	var classname = trace.children[i].childNodes[0].nodeValue;

	var textsvg = document.createElementNS("http://www.w3.org/2000/svg","svg");
	new_trace.appendChild(textsvg);
	var text = document.createElementNS("http://www.w3.org/2000/svg","text");
	textsvg.appendChild(text);
	text.setAttribute("font-size",15);
	text.setAttribute("text-anchor","start");
	text.setAttribute("y",y+"em");
	text.innerHTML=classname;
	
    }
    
}

function makeInterface(target,entry) {
    entryName = entry.getAttribute("name");
    svgRoot = target;
    // add new svg element that contains the interface 
    var new_interface = document.createElementNS("http://www.w3.org/2000/svg","svg");
    new_interface.setAttribute("type","interface");
    new_interface.setAttribute("id","InterfaceSVG");
    // if there is no interface element, stop here
                if (entry.getElementsByTagName("interface")[0] == null) {
		    console.log("No interface to display");
		    return;
		}	
		svgRoot.appendChild(new_interface);
		var interface = entry.getElementsByTagName("interface")[0];
		var ypoint = 3;
		
		// interface descriptions may consist of separate components
                for (var i = 0; i < interface.children.length; i++) {
				transformFS(interface.children[i],new_interface);  // TODO: remove second argument?			
		}
		for (var i = 0; i < new_interface.children.length; i++) {
				if (new_interface.children[i].getAttribute("type") == "fs") {
						var fs = new_interface.children[i];
						processFS(fs);
						fs.setAttribute("y",ypoint);  // paddin
						var fsHeight = fs.getBBox().height;
						// plot label of overall interface 
						if (fs.hasAttribute("label")) {
								addLabel(fs.getAttribute("label"),new_interface);
								var label = new_interface.lastElementChild;
								var labelSize = label.getBBox();
								fs.setAttribute("x",labelSize.width + 5);  // padding
								new_interface.lastElementChild.setAttribute("y",fsHeight > labelSize.height ? fsHeight/2 - labelSize.height/2 + ypoint : ypoint);
						}
						ypoint += fsHeight + 20;  // padding
				}
		}
    num=0;
    for (var i = 0; i < interface.children.length; i++) {
	
	num=printRelation(interface.children[i],new_interface,ypoint,num);  // TODO: remove second argument?			
    }
		
		//addInterfaceButtons(svgRoot); 
}


// turn inTree into an svg element and make it daughter of outParent
function transformTree (inTree,outParent) {
    var daughters;
    if (inTree.children.length > 1) {
	daughters = document.createElementNS("http://www.w3.org/2000/svg","svg");
	daughters.setAttribute("type","children");
	outParent.appendChild(daughters);
    }
    else { daughters = outParent; }
    for (var i = 0; i < inTree.children.length; i++){
	var child = inTree.children.item(i);
	var new_outParent = document.createElementNS("http://www.w3.org/2000/svg","svg");
	if (child.tagName=="node") {
	    new_outParent.setAttribute("type","tree");
	    daughters.appendChild(new_outParent);
	    transformTree(child,new_outParent);
	}
	if (child.tagName=="narg") {
	    new_outParent.setAttribute("type","node");
	    if (child.parentNode.getAttribute("type") != "std"){ 
		new_outParent.setAttribute("mark",child.parentNode.getAttribute("type"));
	    }
	    if (!/XMGVAR_/.test(child.parentNode.getAttribute("name")) && child.parentNode.getAttribute("name")!=null){ 
		new_outParent.setAttribute("name",child.parentNode.getAttribute("name"));
	    }
	    outParent.appendChild(new_outParent);
	    outParent.insertBefore(new_outParent,outParent.children[0]); // root node element comes first			
	    transformFS(child.children[0],new_outParent);
	    reorderFS(new_outParent);
	}
    }
}

// ypoint is the coordinate where the relation should be print
// if more than one relation, ypoint should be updated (+1em?)
// num is the number of the relation
function printRelation(rel,out,ypoint,num){
    
    if(rel.nodeName!="relation"){
	
	return num;}

    function makeSVGBox(label,svgboxtarget,pos){
	var textsvg = document.createElementNS("http://www.w3.org/2000/svg","svg");
	textsvg.setAttribute("type","label");
	svgboxtarget.appendChild(textsvg);
	
	var text = document.createElementNS("http://www.w3.org/2000/svg","text");
	text.setAttribute("font-size",15);
	text.setAttribute("text-anchor","start");
	text.setAttribute("x",pos);
	text.setAttribute("y",ypoint);
	text.innerHTML = label;
	textsvg.appendChild(text);
	
	var box = document.createElementNS("http://www.w3.org/2000/svg","rect");
	box.setAttribute("x", text.getBBox().x - 1.2);
	box.setAttribute("y", text.getBBox().y - 1.2);
	box.setAttribute("width", text.getBBox().width + 2.4);
	box.setAttribute("height", text.getBBox().height +2.4);
	box.setAttribute("name",label);
	box.setAttribute("cursor","pointer");
	box.setAttribute("onclick","highlightLabel(evt)");
	box.setAttribute("style", "stroke:black; fill:transparent;");
	textsvg.appendChild(box);

	return pos+text.getBBox().width+5;

    }

    function makeSVGText(label,svgtexttarget,pos){
	var textsvg = document.createElementNS("http://www.w3.org/2000/svg","svg");
	svgtexttarget.appendChild(textsvg);
	var text = document.createElementNS("http://www.w3.org/2000/svg","text");
	textsvg.appendChild(text);
	text.setAttribute("font-size",15);
	text.setAttribute("text-anchor","start");
	text.setAttribute("x",pos);
	text.setAttribute("y",ypoint);
	text.innerHTML=label;

	return pos+text.getBBox().width+5;
    }

    
    
    ypoint=ypoint+10+num*40;
    var new_rel = document.createElementNS("http://www.w3.org/2000/svg","svg");
    new_rel.setAttribute("type","pred");
    new_rel.setAttribute("type","feature");
    new_rel.setAttribute("id","semrelSVG");
    out.appendChild(new_rel);
    var pos=0;

    var name = rel.getAttribute("name");

    pos=makeSVGText(name,new_rel,pos);
    
    pos=makeSVGText("(",new_rel,pos);
    
    for (var i = 0; i < rel.children.length; i++){

	if(i>0){
	    pos=makeSVGText(",",new_rel,pos);
	}
	
	var textsvg = document.createElementNS("http://www.w3.org/2000/svg","svg");
	// If this is done after, the coordinates of the textsvg or the text objects are not set
	new_rel.appendChild(textsvg);
    	// var text = document.createElementNS("http://www.w3.org/2000/svg","text");
    	// text.setAttribute("font-size",15);
    	// text.setAttribute("text-anchor","start");
    	// text.setAttribute("x",new_rel.getBBox().width);
    	// text.setAttribute("y",ypoint);
    	// text.innerHTML = "";
	if (rel.children[i].hasAttribute("varname")) { 
	    pos=makeSVGBox(rel.children[i].getAttribute("varname").replace("@",""),new_rel,pos);
	    
    	}
    	else{
	    pos=makeSVGText(rel.children[i].getAttribute("value"),new_rel,pos);

    	}
    }

    pos=makeSVGText(")",new_rel,pos);
    
    return num +1;
    
}

// turn inFS into an svg element and make it a daughter of outParent
function transformFS(inFS,outParent) {
    if(inFS.nodeName=="relation"){
	return;
    }
		var new_fs = document.createElementNS("http://www.w3.org/2000/svg","svg");
		new_fs.setAttribute("type","fs");
		if (inFS.hasAttribute("coref")) {
				new_fs.setAttribute("label",inFS.getAttribute("coref").replace("@",""));
		}

		// probably deprecated
		if (inFS.hasAttribute("type")) {
				new_fs.setAttribute("fstype",inFS.getAttribute("type").replace("[","").replace("]","").toLowerCase());
		}
		// following the new DTD which has a ctype element
		if (inFS.children.length > 0 && inFS.children[0].tagName == "ctype") {
				var ctype = inFS.children[0];
				var ctypeArray = [];
				for (var i = 0; i < ctype.children.length; i++){
						// ctypeArray.push(ctype.children[i].innerHTML);
						ctypeArray.push(ctype.children[i].getAttribute("val"));
				}
				new_fs.setAttribute("fstype",ctypeArray.toString());
				//inFS.removeChild(ctype); // to make things easier when dealing with feature-value pairs afterwards
		}
		
		outParent.appendChild(new_fs);

    
		// feature-value pairs
		for (var i = 0; i < inFS.children.length; i++){
				var child = inFS.children.item(i);
				if(child.getAttribute("name")==null)
					continue;
				var new_feature = document.createElementNS("http://www.w3.org/2000/svg","svg");
				new_feature.setAttribute("type","feature");
				new_fs.appendChild(new_feature);

				var new_featureName = document.createElementNS("http://www.w3.org/2000/svg","text");
				new_featureName.innerHTML = child.getAttribute("name");
				new_feature.appendChild(new_featureName);

				if (child.children[0].tagName == "sym") {
						var new_value = document.createElementNS("http://www.w3.org/2000/svg","svg");
						new_value.setAttribute("type","value")
						if (child.children[0].hasAttribute("varname")) {
								new_value.setAttribute("label",child.children[0].getAttribute("varname").replace("@",""));
						}
						var	new_valueText = document.createElementNS("http://www.w3.org/2000/svg","text");
						new_valueText.innerHTML = child.children[0].getAttribute("value");	
						new_value.appendChild(new_valueText);
						new_feature.appendChild(new_value);
				}

				if (child.children[0].tagName == "fs") {
						var new_value = document.createElementNS("http://www.w3.org/2000/svg","svg");
						new_value.setAttribute("type","value");
						transformFS(child.children[0],new_value);
						new_feature.appendChild(new_value);
				}

				if (child.children[0].tagName == "vAlt") {
						var new_value = document.createElementNS("http://www.w3.org/2000/svg","svg");
						new_value.setAttribute("type","value");
						var	new_valueText = document.createElementNS("http://www.w3.org/2000/svg","text");
						new_valueText.innerHTML="@{"; 
						for(var ia = 0; ia<child.children[0].children.length;ia++){
							if(ia>0){
								new_valueText.innerHTML=new_valueText.innerHTML+",";
							}
						new_valueText.innerHTML=new_valueText.innerHTML+child.children[0].children[ia].getAttribute("value");
						}
						new_valueText.innerHTML=new_valueText.innerHTML+"}";	
						new_value.appendChild(new_valueText);
						new_feature.appendChild(new_value);

				}
		}
}

// first top, then bot
function reorderFS(node) {
		var top = -1, 
				bot = -1,
				fs = node.children[0];
		if (fs.getAttribute("type") != "fs") {return;}
		for (var i = 0; i < fs.children.length; i++){
				var featureName = fs.children[i].children[0].innerHTML;
				if (featureName == "bot") { bot = i; }
				if (featureName == "top") { top = i; }
				if (featureName == "cat") { 
					node.setAttribute("cat",fs.children[i].children[1].children[0].innerHTML); 
				}
				if (featureName == "phon") { 
						node.setAttribute("phon",fs.children[i].children[1].children[0].innerHTML); 
				}
		}
		if (top > -1) {
				fs.insertBefore(fs.children[top],fs.children[0]);
		}
		if ( bot > -1) {
				fs.insertBefore(fs.lastElementChild,fs.children[bot]);
		}
}

function processTree (tree) {
    //console.log("Processing tree ");
    //console.log(tree);
		for (var i = 0; i < tree.children.length; i++){
				var child = tree.children.item(i);
				if (child.getAttribute("type")=="tree") {
						processTree(child);
						drawTree(child);
				}
				if (child.getAttribute("type")=="children") {
						for (var j = 0; j < child.children.length; j++) {
								processTree(child.children[j]);
								drawTree(child.children[j]);
						}
				}
				if (child.getAttribute("type")=="node") {
						processNode(child);
				}
		}
        //console.log("Done processing tree ");
}

function processNode (node) {
    //console.log("Processing node");
    //console.log(node);
		var fs = node.children[0];
                processFS(fs,node.getAttribute("name"));
		var fsHeight = fs.getBBox().height;
		var fsWidth = fs.getBBox().width;
		var markWidth = 0;
		var markHeight = 0;

		// process the mark attribute
    if (node.hasAttribute("mark")) {
	var markSVG = document.createElementNS("http://www.w3.org/2000/svg","svg");
	var mark = document.createElementNS("http://www.w3.org/2000/svg","path");
	if (node.getAttribute("mark")=="anchor") {
	    mark.setAttribute("d","M 10 0 L 0 10 L 10 20 L 20 10 Z");
	}
	if (node.getAttribute("mark")=="subst") {
	    mark.setAttribute("d","M 5 0 L 5 20 L 0 10 M 5 20 L 10 10");
	}
	if (node.getAttribute("mark")=="foot") {
	    mark.setAttribute("d","M 10 10 L 0 10 M 10 10 L 10 0 M 10 10 L 2 2 M 10 10 L 18 18 M 10 10 L 20 10 M 10 10 L 10 20 M 10 10 L 2 18 M 10 10 L 18 2");
	}
	if (node.getAttribute("mark")=="star") {
	    mark.setAttribute("d","M 10 10 L 0 10 M 10 10 L 10 0 M 10 10 L 2 2 M 10 10 L 18 18 M 10 10 L 20 10 M 10 10 L 10 20 M 10 10 L 2 18 M 10 10 L 18 2");
	}
	if (node.getAttribute("mark")=="op-bound") {
	    mark.setAttribute("d","M 0 5 L 20 5 M 0 15 L 20 15 M 20 0");
	    //mark.setAttribute("d","M 10 10 L 30 10 M 0 10 L 0 30");
	}
	// if (node.getAttribute("mark")=="ddaughter") {
	// 		mark.setAttribute("d","M 20 20 C 0 10 0 40 20 30 L 20 0");
	// }
	mark.setAttribute("style", "stroke:green; fill:none;");
	mark.setAttribute("transform", "scale (.7)");
	markSVG.appendChild(mark);
	markSVG.setAttribute("x",parseInt(fs.getAttribute("x")) + fsWidth + 10);
	markSVG.setAttribute("type","mark"); 
	node.appendChild(markSVG);
	
	markHeight = markSVG.getBBox().height;
	markWidth = markSVG.getBBox().width;
    }
    
    
    // append cat label or phon label
    if (node.hasAttribute("phon")) {
	addPhon(node.getAttribute("phon"),node);
    }
    else {
	var category = node.getAttribute("cat");
	if(node.hasAttribute("name")){
	    category=category+"<tspan font-weight=\"bold\" fill=\"red\"> ("+node.getAttribute("name")+") </tspan>";
	}
	addCategory(category,node);
    }
    
    //	collapse and expand node
    if (node.getAttribute("type") == "node"){
	var collapseExpandSVG = document.createElementNS("http://www.w3.org/2000/svg","svg");
	collapseExpandSVG.setAttribute("type","ce-switch");
	collapseExpandSVG.setAttribute("ce-status","expanded");
	var collapseSymbol = document.createElementNS("http://www.w3.org/2000/svg","path");
	var expandSymbol = document.createElementNS("http://www.w3.org/2000/svg","path");
	var collapseExpandRect = document.createElementNS("http://www.w3.org/2000/svg","rect");
	collapseSymbol.setAttribute("d","M 10 0 L 0 10 L 10 20 M 15 0 L 5 10 L 15 20");
	collapseSymbol.setAttribute("style", "stroke:gray; fill:none;");
	collapseSymbol.setAttribute("transform", "scale (.7)");
	collapseSymbol.setAttribute("display", "initial");;
	collapseExpandSVG.appendChild(collapseSymbol);
	expandSymbol.setAttribute("d","M 0 0 L 10 10 L 0 20 M 5 0 L 15 10 L 5 20");
	expandSymbol.setAttribute("style", "stroke:gray; fill:none;");
	expandSymbol.setAttribute("display", "none");
	expandSymbol.setAttribute("transform", "scale (.7)");
	collapseExpandSVG.appendChild(expandSymbol);
	collapseExpandRect.setAttribute("y",0);
	collapseExpandRect.setAttribute("x",0);
	collapseExpandRect.setAttribute("width",15);
	collapseExpandRect.setAttribute("height",20);
	collapseExpandRect.setAttribute("style", "fill:transparent;");
	collapseExpandRect.setAttribute("transform", "scale (.7)");
	collapseExpandRect.setAttribute("onclick","collapseExpandNodeEvent(evt)");
	collapseExpandRect.setAttribute("cursor","pointer");
	collapseExpandSVG.appendChild(collapseExpandRect);
	if (markHeight*2 > fsHeight) {
	    collapseExpandSVG.setAttribute("x",parseInt(fs.getAttribute("x")) + fsWidth + markWidth + 10); //padding
	}
	else {
	    collapseExpandSVG.setAttribute("x",parseInt(fs.getAttribute("x")) + fsWidth + 10); //padding
	}
	collapseExpandSVG.setAttribute("y",fsHeight/2 );
	node.appendChild(collapseExpandSVG);
	node.setAttribute("x",0);
    }
    //console.log("Finished processing node");
}

function addLabel(labeltext,target) {
    var label = document.createElementNS("http://www.w3.org/2000/svg","svg");
    label.setAttribute("type","label");
    target.appendChild(label);
    
    var text = document.createElementNS("http://www.w3.org/2000/svg","text");
    text.setAttribute("font-size",11);
    text.setAttribute("text-anchor","start");
    text.setAttribute("x",2);
    text.setAttribute("y","1.2em");
    text.innerHTML = labeltext;
    label.appendChild(text);
    
    var box = document.createElementNS("http://www.w3.org/2000/svg","rect");
    box.setAttribute("x", 1);
    box.setAttribute("y", text.getBBox().y-1.2);
    box.setAttribute("width", text.getBBox().width+2);
    box.setAttribute("height", text.getBBox().height+1.2);
    box.setAttribute("name",labeltext);
    box.setAttribute("cursor","pointer");
    box.setAttribute("onclick","highlightLabel(evt)");
    box.setAttribute("style", "stroke:black; fill:transparent;");
    label.appendChild(box);
}

function addCategory(cat,node) {
    var catlabel = document.createElementNS("http://www.w3.org/2000/svg","text");
    catlabel.setAttribute("x",0);
    catlabel.setAttribute("y",25);
    catlabel.setAttribute("font-size",25);
    catlabel.setAttribute("text-anchor","start");
    catlabel.setAttribute("style","text-transform: uppercase;");
    catlabel.setAttribute("display","none");
    catlabel.setAttribute("type","catlabel");
    catlabel.innerHTML = cat;
    node.appendChild(catlabel);
}

function addPhon(phon,node) {
    var catlabel = document.createElementNS("http://www.w3.org/2000/svg","text");
    catlabel.setAttribute("x",0);
    catlabel.setAttribute("y",25);
    catlabel.setAttribute("font-size",25);
    catlabel.setAttribute("style", "font-style: italic;");
    catlabel.setAttribute("text-anchor","start");
    catlabel.setAttribute("display","none");
    catlabel.setAttribute("type","catlabel");
    catlabel.innerHTML = phon;
    node.appendChild(catlabel);
}


function processFS(fs, nodename=null) {
    //console.log("Processing FS");
    //console.log(fs);
    fs.setAttribute("x",0); // needed for node marks
    var hasType = false;
    var ypoint = 3;
    
    // process type of feature structure
    if (fs.hasAttribute("fstype")) {
	var hasType = true;
	var type = document.createElementNS("http://www.w3.org/2000/svg","svg");
	type.setAttribute("type","type");
	fs.insertBefore(type,fs.firstChild);
	
	var text = document.createElementNS("http://www.w3.org/2000/svg","text");
	text.innerHTML = fs.getAttribute("fstype");
	text.setAttribute("y", "0.9em");
	text.setAttribute("font-size",15);
	text.setAttribute("style", "font-style: italic;");
	type.appendChild(text);
	
	// position type of feature structure
	type.setAttribute("x", 5);  //padding
	type.setAttribute("y", ypoint);			
	ypoint += type.getBBox().height + 5; //padding 
    }
    
    if (fs.children.length < 1) {
	return;
    }
    
    // assumption: if there is a type, it is the first child of the feature structure
    var firstFeatureChild = 0;
    if (hasType) { firstFeatureChild = 1;}
    
    // compute maxlength of feature names
    var maxlengthFeatures = 0;
    for (var i = firstFeatureChild; i < fs.children.length; i++){
	var feature = fs.children[i];
	if(feature.children[0]!=null){
	    var featureName = feature.children[0];
	    
	    featureName.setAttribute("font-size","13");
	    featureName.setAttribute("y","1em");
	    featureName.setAttribute("font-variant","normal");
	    featureName.innerHTML = featureName.innerHTML.toUpperCase();
	    if (featureName.getBBox().width > maxlengthFeatures) {
		maxlengthFeatures = featureName.getBBox().width;
	    }
	}
    }
    
    //	process feature and value
    for (var i = firstFeatureChild; i < fs.children.length; i++){
	
	var feature = fs.children[i];
	var featureName = feature.children[0];
	var value = feature.children[1];
	var labelWidth = 0;
	
	if (featureName.innerHTML == "CAT"){
	    //console.log("HERE FOUND CAT");
	    if (nodename != null){
		//console.log("HERE ADDING NAME");
		//console.log(value.children[0]);
		value.children[0].innerHTML = value.children[0].innerHTML + "<tspan font-weight=\"bold\" fill=\"red\"> ("+ nodename+") </tspan>";
		//value.innerHTML = "TEST";
	    }
	}
	
	
	featureName.setAttribute("x", 5); //padding
	value.setAttribute("x", maxlengthFeatures + 10); //padding 
	
	// value is label
	if (value.hasAttribute("label")) {
	    addLabel(value.getAttribute("label"),value);
	    labelWidth = value.lastElementChild.getBBox().width;
	}
	
	// value is text element
	if (value.children[0].tagName =="text") {
	    value.children[0].setAttribute("y", "0.86em");
	    value.children[0].setAttribute("font-size", "15");
	    value.children[0].setAttribute("x", labelWidth);
	}
	
	// value is fs
	if (value.children[0].getAttribute("type")=="fs") {
	    var getlabel = value.children[0].getAttribute("label");
	    var oldvalue = value;
	    //console.log("REC FS, nodename="+nodename);
	    processFS(value.children[0], nodename);
	    if (getlabel != null) {
		addLabel(getlabel,oldvalue);
		var label = value.lastElementChild;
		var labelSize = label.getBBox();
		labelWidth = labelSize.width;
		labelHeight = labelSize.height;
		
		var oldlabel = oldvalue.lastElementChild;
		var oldlabelSize = oldlabel.getBBox();
		oldlabelWidth = oldlabelSize.width;
		oldlabelHeight = oldlabelSize.height;
		
		// place fs after label
		oldvalue.children[0].setAttribute("x",oldlabelWidth + 5);
		// center label vertically
		//label.setAttribute("y", value.children[0].getBBox().height/2 > labelSize.height/2 ? value.children[0].getBBox().height/2 - labelSize.height/2 - 2 : -2); // padding 
		// here should be centered
		oldlabel.setAttribute("y", oldvalue.getBBox().height/2 - oldlabelHeight/2); // padding 
	    } 
	}	
	
	// center feature name vertically
	featureName.setAttribute("y", value.getBBox().height/2-featureName.getBBox().height/2 + 12); // padding
	
	// set and increment y attribute
	feature.setAttribute("y", ypoint);
	ypoint += feature.getBBox().height + 5; //padding 
	
    }
    
    // draw squared brackets around fs	
    var left_x = 2;
    var right_x = fs.getBBox().width+9;
    var top_y = 2;
    var bot_y = fs.getBBox().height+7;
    var brackettip = 7; 	
    
    var FSrb = document.createElementNS("http://www.w3.org/2000/svg","path");
    FSrb.setAttribute("d","M "+left_x+" "+top_y+" H "+brackettip+" M "+left_x+" "+top_y+" L "+left_x+" "+bot_y+ " H "+brackettip );
    FSrb.setAttribute("style", "stroke:black; fill:none;");
    fs.appendChild(FSrb);
    
    var FSlb = document.createElementNS("http://www.w3.org/2000/svg","path");
    FSlb.setAttribute("d","M "+right_x+" "+top_y+" H "+(right_x-brackettip)+" M "+right_x+" "+top_y+" L "+right_x+" "+bot_y+" H "+(right_x-brackettip));//M "+ fs.getBBox().width +" 2 L "+ fs.getBBox().width +" " + fs.getBBox().height + "H 7" );
    FSlb.setAttribute("style", "stroke:black; fill:none;");
    fs.appendChild(FSlb);
    //console.log("Finished processing fs");
}


function drawTree (tree) {
    //console.log("Drawing tree");
    var root;
    var rootHeight;
    var rootWidth;
    var daughters;
    var daughtersXpoint = 0;

    // tree.children should contain
    // [0]: a node, the root
    // [1]: the daughters
    for (var i = 0; i < tree.children.length; i++) {
	var child = tree.children[i];
	if (child.getAttribute("type")=="node") {
	    root = child;
	    rootHeight = root.getBBox().height;
	    rootWidth = root.getBBox().width;
	}
	if (child.getAttribute("type")=="children") {
	    daughters = child;
	    daughters.setAttribute("x",0);
	}	
    }
    
    if (tree.children.length > 2) {
	console.log("Unexpected: tree has more than 2 children")
    }
    
    // position children element
    if (daughters) {
	daughters.setAttribute ("y", rootHeight + offsetyNode);
	
	//	position children next to each other
	for (var i = 0; i < daughters.children.length; i++){
	    var child = daughters.children.item(i);
	    child.setAttribute ("x", daughtersXpoint);
	    daughtersXpoint += child.getBBox().width + offsetxNode; 
	}
    }
    //console.log("Still drawing tree");
    
    // center root node
    if (daughters) {
	if (daughters.getBBox().width > rootWidth) { //padding
	    
	    newPosition= (
		// the center of the right-most daughter
		(parseInt(daughters.lastElementChild.getAttribute("x")) +
		 parseInt(daughters.lastElementChild.firstElementChild.getAttribute("x")) +
		 // the center of the left-most daughter
		 (parseInt(daughters.firstElementChild.getAttribute("x")) +
		  parseInt(daughters.firstElementChild.firstElementChild.getAttribute("x"))
		 )))/2 +
		daughters.lastElementChild.firstElementChild.getBBox().width/2 -rootWidth/2;
	    
	    root.setAttribute ("x", newPosition);
	}
	else{
	    root.setAttribute ("x", 0);
	    daughters.setAttribute ("x", rootWidth/2 - daughters.getBBox().width/2);
	}
    }
    
    //console.log("Drawing edges");
    // draw edges
    if (daughters) {
	for (var i = 0; i < daughters.children.length; i++){
	    var child = daughters.children.item(i);
	    if(child.children[0].getAttribute("mark")=="ddaughter"){
		var edge = document.createElementNS("http://www.w3.org/2000/svg","line");
		edge.setAttribute("x1", parseInt(root.getAttribute("x")) + root.getBBox().width/2);
		edge.setAttribute("x2", parseInt(daughters.getAttribute("x")) + parseInt(child.getAttribute("x")) + parseInt(child.firstElementChild.getAttribute("x")) + child.firstElementChild.getBBox().width/2);
		edge.setAttribute("y1", rootHeight + 3);
		edge.setAttribute("y2", rootHeight + offsetyNode + 2);
		edge.setAttribute("style", "stroke:black; fill:none;");
		edge.setAttribute("type","edge");
		edge.setAttribute("stroke-dasharray",5.5);
		tree.appendChild(edge);
	    }
	    else{
		var edge = document.createElementNS("http://www.w3.org/2000/svg","line");
		
		edge.setAttribute("x1", parseInt(root.getAttribute("x")) + root.getBBox().width/2);
		edge.setAttribute("x2", parseInt(daughters.getAttribute("x")) + parseInt(child.getAttribute("x")) + parseInt(child.firstElementChild.getAttribute("x")) + child.firstElementChild.getBBox().width/2);
		edge.setAttribute("y1", rootHeight + 3);
		edge.setAttribute("y2", rootHeight + offsetyNode + 2);
		edge.setAttribute("style", "stroke:black; fill:none;");
		edge.setAttribute("type","edge");
		tree.appendChild(edge);
	    }
	}
    }
    //console.log("Finished drawing tree");
}


function redrawNode (node) {
		var content, mark, ceswitch;
		var markWidth = 0;
		var markHeight = 0;
		var contentHeight = 0;
		var contentWidth = 0;
		var ceswitchHeight = 0;
		var ceswitchWidth = 0;

		for (var i = 0; i < node.children.length; i++){
				if (node.children[i].getAttribute("display") == "initial") {
						content = node.children[i];
						contentHeight = content.getBBox().height;
						contentWidth = content.getBBox().width;
				}
				if (node.children[i].getAttribute("type") == "mark") {
						mark = node.children[i];
						markWidth = mark.getBBox().width;
						markHeight = mark.getBBox().height;
				}
				if (node.children[i].getAttribute("type") == "ce-switch") {
						ceswitch = node.children[i];
						ceswitchWidth = ceswitch.getBBox().width;
						ceswitchHeight = ceswitch.getBBox().height;						
				}
		}

		if (!content) {return;}  // TODO: add error message
		if (content.getAttribute("type") == "catlabel") {
				content.setAttribute("x",ceswitchWidth);
		}
		if (mark) {
				if (content.getAttribute("type") == "catlabel") {
						mark.setAttribute("x",parseInt(content.getAttribute("x")) + contentWidth + 5 ); //padding
				}
				else {
						mark.setAttribute("x",parseInt(content.getAttribute("x")) + contentWidth + 7); //padding
				}
		}
		if (ceswitch) {
				if (content.getAttribute("type") == "catlabel") {
						ceswitch.setAttribute("x",parseInt(content.getAttribute("x")) + contentWidth + markWidth + 10); //padding
						ceswitch.setAttribute("y",contentHeight/2 - ceswitchHeight/2);
				}
				else {
						ceswitch.setAttribute("y",contentHeight/2 );
						if (markHeight*2 > contentHeight) {
								ceswitch.setAttribute("x",parseInt(content.getAttribute("x")) + contentWidth + markWidth + 10); //padding
						}
						else {
								ceswitch.setAttribute("x",parseInt(content.getAttribute("x")) + contentWidth + 10); //padding
						}
				}
		}
}

function redrawTree (tree) {
		
		// remove all edges
		for (var i = tree.children.length-1; i > 0; i--) {    // children have to be traversed backwards, because removeChild influences the index of the other children
				if (tree.children[i].getAttribute("type") == "edge") {
						tree.removeChild(tree.children[i]);			
				}
		}

		// draw tree again
		drawTree(tree);

		// check whether tree.parentNode.parentNode really exists
		if (tree.parentNode.parentNode.tagName &&	tree.parentNode.parentNode.hasAttribute("type") && tree.parentNode.parentNode.getAttribute("type") == "tree") {
				redrawTree(tree.parentNode.parentNode);
		}
}


function highlightLabel (evt) {
		var evtlabel = evt.target;
		var evtlabelName = evtlabel.getAttribute("name");

		// case A: label is already red
		// make label transparent again and return
		if (evtlabel.getAttribute("style") =="stroke:black; fill:red;") {
				var evtlabels = document.getElementsByName(evtlabelName);
				for (var i = 0; i < evtlabels.length; i++){
						var label = evtlabels.item(i);
						label.setAttribute("style", "stroke:black; fill:transparent;");
				}
				return;
		}

		// case B: label is not yet red
		// first remove any marks that might be left from last click
		var labels = document.getElementsByTagName("rect");
		for (var i = 0; i < labels.length; i++){
				var label = labels.item(i);
				if (label.parentNode.getAttribute("type")=="label") {
						label.setAttribute("style", "stroke:black; fill:transparent;");;
						//console.log(evtlabelName);
				}
		}

		// then draw the mark
		var evtlabels = document.getElementsByName(evtlabelName);
		for (var i = 0; i < evtlabels.length; i++){
				var label = evtlabels.item(i);
				label.setAttribute("style", "stroke:black; fill:red;");
		}
}


function collapseExpandNodeEvent (evt) {
		// just a wrapper for collapeExpandnode()
		var ceswitch = evt.target.parentNode;
		collapseExpandNode(ceswitch);

		var object = document.getElementById("synTreeSVG");

		// adapt latex export if necessary
		if (object.parentNode.getElementById("latexExport")) {
				// remove element latexExport
				var element = object.parentNode.getElementById("latexExport");
				element.parentNode.removeChild(element);
				// draw new latexExport
				exportLatex(object);
		}
}  

function collapseExpandNode (ceswitch) {
		// change ce-symbol
		for (var i = 0; i < ceswitch.children.length; i++){
				var child = ceswitch.children[i];
				if (ceswitch.getAttribute("ce-status") == "expanded") { ceswitch.setAttribute("ce-status","collapsed") }
				else { ceswitch.setAttribute("ce-status","expanded")}
				if (child.hasAttribute ("display")) {  
						if (child.getAttribute("display")!="initial") {
								child.setAttribute("display","initial");
						}
						else { child.setAttribute("display","none"); }
				}	
		}

		//	change node display
		var node = ceswitch.parentNode;
		for (var i = 0; i < node.children.length; i++) {
				if (node.children[i].getAttribute("type")=="fs") {
						var fs = node.children[i]; 
						if (fs.hasAttribute("display") && fs.getAttribute("display")!="initial") {
								fs.setAttribute("display","initial");
						}
						else {fs.setAttribute("display","none"); }
				}
				if (node.children[i].getAttribute("type")=="catlabel") {
						var catlabel = node.children[i];
						if (catlabel.hasAttribute("display") && catlabel.getAttribute("display")!="initial") {
								catlabel.setAttribute("display","initial");
						}
						else {catlabel.setAttribute("display","none"); }
				}			
		}
		redrawNode(node); 	//drawTree(node);
		redrawTree(node.parentNode);  // remove all edges; draw new ones 

} 
	
function addTreeButtons (target) {
		var tree = target.getElementsByTagName("svg")[0];
		var xpos;

		var buttonExportSVG = generateButton("SVG","pressedButtonExportSVG(evt)",target);
		buttonExportSVG.setAttribute("x", 0);
		xpos = buttonExportSVG.getBBox().width+5;  // padding

		var buttonExportLatex = generateButton("LaTeX","pressedButtonExportLatex(evt)",target);
		buttonExportLatex.setAttribute("x", xpos);
		buttonExportLatex.setAttribute("id","buttonLatex");
		xpos += buttonExportLatex.getBBox().width+5; // padding

		var buttonCollapseAll = generateButton("collapse","pressedButtonCollapseAll(evt)",target);
		buttonCollapseAll.setAttribute("x", xpos);
                xpos += buttonCollapseAll.getBBox().width+5; // padding

		var buttonExpandAll = generateButton("expand","pressedButtonExpandAll(evt)",target);
		buttonExpandAll.setAttribute("x", xpos);

		if(tree!=null)
		tree.setAttribute("y", buttonExportSVG.getBBox().height+10); // padding
}

function addFrameButtons (target) {
    var frame = target.getElementsByTagName("svg")[0];
    var xpos;
    
    var buttonExportSVG = generateButton("SVG","pressedButtonExportSVG(evt)",target);
    buttonExportSVG.setAttribute("x", 0);
    xpos = buttonExportSVG.getBBox().width+5;  // padding
    
    var buttonExportLatex = generateButton("LaTeX","pressedButtonExportLatex(evt)",target);
    buttonExportLatex.setAttribute("x", xpos);
    buttonExportLatex.setAttribute("id","buttonLatex");
    xpos += buttonExportLatex.getBBox().width+5; // padding

    var buttonGraph = generateButton("Graph","pressedButtonGraph(evt)",target);
    buttonGraph.setAttribute("x", xpos);
    buttonGraph.setAttribute("id","buttonGraph");
    xpos += buttonGraph.getBBox().width+5; // padding
    
    frame.setAttribute("y", buttonExportSVG.getBBox().height+10); // padding
}


function generateButton(label,action,target) {
		var button = document.createElementNS("http://www.w3.org/2000/svg","svg");
		target.appendChild(button);
		button.setAttribute("type","button"); 
		
		var text = document.createElementNS("http://www.w3.org/2000/svg","text");
		text.setAttribute("font-size",15);
		text.setAttribute("text-anchor","start");
		text.setAttribute("x",2);
		text.setAttribute("y","1.2em");
		text.setAttribute("style", "font-family: sans-serif; font-weight: bold;");
		text.setAttribute("type","buttonText");
		text.innerHTML = label;
		button.appendChild(text);

		var background = document.createElementNS("http://www.w3.org/2000/svg","rect");
		background.setAttribute("x", 1);
		background.setAttribute("y", text.getBBox().y-1.2);
		background.setAttribute("width", text.getBBox().width+2);
		background.setAttribute("height", text.getBBox().height+1.2);
		background.setAttribute("style", "fill:lightgray;");
		background.setAttribute("type", "buttonInnerRect");
		button.appendChild(background);
		
		button.insertBefore(background,text);
		
		var box = document.createElementNS("http://www.w3.org/2000/svg","rect");
		box.setAttribute("x", 1);
		box.setAttribute("y", text.getBBox().y-1.2);
		box.setAttribute("width", text.getBBox().width+2);
		box.setAttribute("height", text.getBBox().height+1.2);
		box.setAttribute("cursor","pointer");
		box.setAttribute("onclick",action);
		box.setAttribute("style", "stroke:gray; fill:transparent;");
		button.appendChild(box);

		return(button);
}

function pressedButtonExportSVG (evt) {
		var object;
		var siblings = evt.target.parentNode.parentNode.children;
		for (var i = 0;  i< siblings.length; i++) {
				if (siblings[i].getAttribute("type")=="tree" || siblings[i].getAttribute("type")=="frame") {
						object = siblings[i];
				}
		}
		exportSVG(object)
}


function exportSVG (object) {
    var clone = object.cloneNode(true);
    //console.log(clone);
    // clean-up SVG: remove ce-switch and onclick-stuff
    var svgElements = clone.getElementsByTagName("svg");
    for (var i = 0; i<svgElements.length; i++) {
	if (svgElements[i].getAttribute("type") == "ce-switch") {
	    svgElements[i].parentNode.removeChild(svgElements[i]);
	}
    }
    var rectElements = clone.getElementsByTagName("rect");
    for (var i = 0; i< rectElements.length; i++) {
	if (rectElements[i].hasAttribute("onclick")) {
	    rectElements[i].removeAttribute("onclick");
	}
	if (rectElements[i].hasAttribute("cursor")) {
	    rectElements[i].removeAttribute("cursor");
				}
    }
    
    var serializer = new XMLSerializer();
    var blob = new Blob([serializer.serializeToString(clone)],{type:"image/svg+xml"});
    saveAs(blob, entryName+".svg");
}

function pressedButtonCollapseAll (evt) {
		var object;
		var siblings = evt.target.parentNode.parentNode.children;
		for (var i = 0;  i< siblings.length; i++) {
				if (siblings[i].getAttribute("type")=="tree") {
						object = siblings[i];
				}
		}
		var all = object.getElementsByTagName("*");
		for (var i=0, max=all.length; i < max; i++) {
				
				if (all[i].hasAttribute("type") && all[i].getAttribute("type") == "ce-switch" && all[i].getAttribute("ce-status") == "expanded") {
						for (var j=0; j < all[i].children.length; j++ ) {
								if (all[i].children[j].tagName == "rect") {
										collapseExpandNode(all[i].children[j].parentNode);
								}
						}
						all[i].setAttribute("ce-status","collapsed");	
				}
		}
                var svgTree=evt.target.parentNode.parentNode;
    		svgTree.setAttribute("height",svgTree.getBBox().height+10);
		svgTree.setAttribute("width",svgTree.getBBox().width+10);


		// adapt latex export if necessary
		if (object.parentNode.getElementById("latexExport")) {
				// remove element latexExport
				var element = object.parentNode.getElementById("latexExport");
				element.parentNode.removeChild(element);
				// draw new latexExport
				exportLatex(object);
		}
}


function pressedButtonExpandAll (evt) {
		var object;
		var siblings = evt.target.parentNode.parentNode.children;
		for (var i = 0;  i< siblings.length; i++) {
				if (siblings[i].getAttribute("type")=="tree" || siblings[i].getAttribute("type")=="frame") {
						object = siblings[i];
				}
		}
		
		var all = object.getElementsByTagName("*");
		for (var i=0, max=all.length; i < max; i++) {
											
				if (all[i].hasAttribute("type") && all[i].getAttribute("type") == "ce-switch" && all[i].getAttribute("ce-status") == "collapsed") {
						for (var j=0; j < all[i].children.length; j++ ) {
								if (all[i].children[j].tagName == "rect") {
										collapseExpandNode(all[i].children[j].parentNode);
								}
						}
						all[i].setAttribute("ce-status","expanded");	
				}
		}

                var svgTree=evt.target.parentNode.parentNode;
    		svgTree.setAttribute("height",svgTree.getBBox().height+10);
		svgTree.setAttribute("width",svgTree.getBBox().width+10);
    
		// adapt latex export if necessary
		if (object.parentNode.getElementById("latexExport")) {
				// remove element latexExport
				var element = object.parentNode.getElementById("latexExport");
				element.parentNode.removeChild(element);
				// draw new latexExport
				exportLatex(object);
		}
}


function toggleButtonDisplay (button) {
		if (button.getAttribute("status") == "pressed" ) {
				for (var i = 0; i < button.children.length; i++ ) {
						if (button.children[i].getAttribute("type") == "buttonText") {
								button.children[i].setAttribute("style", "font-family: sans-serif; font-weight: bold;");
						}
						if (button.children[i].getAttribute("type") == "buttonInnerRect") {
								button.children[i].setAttribute("style","fill: lightgray;");
						}
				}
				button.setAttribute("status","unpressed");
		}
		else {
				for (var i = 0; i < button.children.length; i++ ) {
						if (button.children[i].getAttribute("type") == "buttonText") {
								button.children[i].setAttribute("style","font-family: sans-serif; stroke: white; fill: white;");
						}
						if (button.children[i].getAttribute("type") == "buttonInnerRect") {
								button.children[i].setAttribute("style","fill: blue;");
						}
				}
				button.setAttribute("status","pressed");
		}
		
}


function pressedButtonExportLatex (evt) {
    var object;
    var siblings = evt.target.parentNode.parentNode.children;
    for (var i = 0;  i< siblings.length; i++) {
	if (siblings[i].getAttribute("type")=="tree" || siblings[i].getAttribute("type")=="frame") {
	    object = siblings[i];
	}
    }
    
    var latexButton = object.parentNode.getElementById("buttonLatex");
    toggleButtonDisplay(latexButton);
    exportLatex(object);    
}

function pressedButtonGraph (evt) {
    var object;
    var siblings = evt.target.parentNode.parentNode.children;
    for (var i = 0;  i< siblings.length; i++) {
	if (siblings[i].getAttribute("type")=="frame") {
	    object = siblings[i];
	}
    }
   
    
    var graphButton = object.parentNode.getElementById("buttonGraph");
    toggleButtonDisplay(graphButton);
    //displayGraph(object);
    var StringIndent = "\t";
    
    if (graphButton.getAttribute("status") == "pressed" ) {
	$.ajax({
	    method: "POST",
	    url: "GRAPHVIZ.svg",
	    //data: { text: graphFrame(object,StringIndent) }
	    data:  graphFrame(object,StringIndent) 
	})
	    .done(function( response ) {
		//console.log(response);
		// document.getElementsByTagName("svg")[0].innerHTML = response;
		// console.log(document.querySelector("[id=semFrameSVG]"));
		semFrameSVG = document.querySelector("[id=semFrameSVG]");
		semFrameSVG.innerHTMLFrame = semFrameSVG.innerHTML;
		semFrameSVG.innerHTML = response;

		allNodes = semFrameSVG.getElementsByClassName("node");
		// console.log(allNodes);
		for (var i = 0; i < allNodes.length; i++){
		    // console.log(allNodes[i]);
		    // console.log(allNodes[i].childNodes);
		    // console.log(allNodes[i].childNodes[3]);
		    //addLabel(allNodes[i].childNodes[5].lastChild.data,allNodes[i].childNodes[5]);

		    // remove the square brackets

		    if (allNodes[i].childNodes[1].lastChild.data.startsWith('[')){
			addLabel(allNodes[i].childNodes[1].lastChild.data.replace('[','').replace(']',''),allNodes[i]);
		    
			//addLabel("V3",allNodes[i]);
			label = allNodes[i].lastChild; 
			label.setAttribute("y",allNodes[i].childNodes[3].getAttribute('cy'));
			label.setAttribute("x",allNodes[i].childNodes[3].getAttribute('cx')-0.5*label.getBBox().width);
			label.lastChild.setAttribute("stroke-width","0");
		    }
		}		

		
		semFrameSVG.parentNode.setAttribute("width",semFrameSVG.getBBox().width+10); // TODO: this should be more dynamic
		semFrameSVG.parentNode.setAttribute("height",semFrameSVG.getBBox().height+50);
		semFrameSVG.parentNode.setAttribute("max-height","100%");
		semFrameSVG.parentNode.setAttribute("max-width","100%");
		// console.log("graph:");
		// console.log(semFrameSVG);

		// console.log(document.getElementsByTagName("svg"));
		// var parser = new DOMParser();
		// var doc_graph = parser.parseFromString(response, "image/svg+xml");
		// var serializer = new XMLSerializer();
		// var blob = new Blob([serializer.serializeToString(doc_graph)],{type:"image/svg+xml"});
		// saveAs(blob, entryName+".svg");
	    });

    }
    else{
	semFrameSVG = document.querySelector("[id=semFrameSVG]");
	document.querySelector("[id=semFrameSVG]").innerHTML = semFrameSVG.innerHTMLFrame;	
	semFrameSVG.parentNode.setAttribute("width",semFrameSVG.getBBox().width+10); // TODO: this should be more dynamic
	semFrameSVG.parentNode.setAttribute("height",semFrameSVG.getBBox().height+50);
	semFrameSVG.parentNode.setAttribute("max-height","100%");
	semFrameSVG.parentNode.setAttribute("max-width","100%");
    }
}

function displayGraph (object) {
    var StringIndent = "\t";
    
    // delete latexExport element if it already exists
    if (object.parentNode.getElementById("graph")) {
	// remove element latexExport
	var element = object.parentNode.getElementById("graph");
	element.parentNode.removeChild(element);
	return;
    }
    
    // create latexExport element
    var foreignObject;
    foreignObject = document.createElementNS("http://www.w3.org/2000/svg","foreignObject");
    foreignObject.setAttribute("id","graph");
    foreignObject.setAttribute("width","100%");
    foreignObject.setAttribute("height","100%");
    object.parentNode.appendChild(foreignObject);
    
    var bodyElement = document.createElementNS("http://www.w3.org/1999/xhtml","body");
    foreignObject.appendChild(bodyElement);
    
    var textArea = document.createElementNS("http://www.w3.org/1999/xhtml","textarea");
    bodyElement.appendChild(textArea);
    textArea.setAttribute("disabled","false");
    textArea.setAttribute("rows","25");
    textArea.setAttribute("cols","50");
    
    if (object.getAttribute("type") == "frame") {
    	textArea.innerHTML = graphFrame(object,StringIndent) + "\n";
    }

    foreignObject.setAttribute("y", object.getAttribute("y"));
    if(object.hasAttribute("x"))
	foreignObject.setAttribute("x",  object.getAttribute("x"));
    //foreignObject.setAttribute("height","100%");
    //foreignObject.setAttribute("width","100%");
    //foreignObject.parentNode.setAttribute("height","100%");
    //foreignObject.parentNode.setAttribute("width","100%");

}


function exportLatex (object) {
    var texStringIndent = "\t";
    
    // delete latexExport element if it already exists
    if (object.parentNode.getElementById("latexExport")) {
	// remove element latexExport
	var element = object.parentNode.getElementById("latexExport");
	element.parentNode.removeChild(element);
	return;
    }
    
    // create latexExport element
    var foreignObject;
    foreignObject = document.createElementNS("http://www.w3.org/2000/svg","foreignObject");
    foreignObject.setAttribute("id","latexExport");
    foreignObject.setAttribute("width","100%");
    foreignObject.setAttribute("height","100%");
    object.parentNode.appendChild(foreignObject);
    //console.log(foreignObject.getBBox().height);
    //var bodyElement = document.createElementNS("http://www.w3.org/1999/xhtml","body");
    //foreignObject.appendChild(bodyElement);
    
    var textArea = document.createElementNS("http://www.w3.org/1999/xhtml","textarea");
    foreignObject.appendChild(textArea);
    textArea.setAttribute("disabled","false");
    textArea.setAttribute("rows","25");
    textArea.setAttribute("cols","50");
    
    if (object.getAttribute("type") == "tree") {
	textArea.innerHTML = "\\Forest{\n\t" + texifyTree(object,texStringIndent) + "\n}\n";
    }
    if (object.getAttribute("type") == "frame") {
	textArea.innerHTML = texifyFrame(object,texStringIndent) + "\n";
    }

    foreignObject.setAttribute("y", object.getAttribute("y"));
    if(object.hasAttribute("x"))
    	foreignObject.setAttribute("x",  object.getAttribute("x"));
    foreignObject.setAttribute("height","100%");
    foreignObject.setAttribute("width","100%");
    //console.log(foreignObject.parentNode);
    //console.log(foreignObject.getBBox().height);
    // foreignObject.parentNode.setAttribute("height",foreignObject.getBBox().height+50);
    // foreignObject.parentNode.setAttribute("width",foreignObject.getBBox().height+50);
    // foreignObject.parentNode.setAttribute("height","100%");
    // foreignObject.parentNode.setAttribute("width","100%");
    //foreignObject.parentNode.setAttribute("max-height","100%");
    //foreignObject.parentNode.setAttribute("max-width","100%");

}


function texifyTree (tree,texStringIndent) {
		var texString = "\[";

		// process children of tree
		for (var i = 0; i < tree.children.length; i++) {
				if (tree.children[i].getAttribute("type") == "node") {
						texString +=  texifyNode(tree.children[i]);
				}
				if (tree.children[i].getAttribute("type") == "children") {
						texStringIndent += "\t";
						for (var j = 0; j < tree.children[i].children.length; j++) {
								texString += "\n" + texStringIndent + texifyTree(tree.children[i].children[j],texStringIndent);
						}
				}
		}

		texString += "\]";
		return(texString);
}

function graphFrame (frame,StringIndent) {
    var String = "";
    var CurrentNode = "";
    for (var i = 0; i < frame.children.length; i++) {
	if (frame.children[i].getAttribute("type")=="fs") {
	    if(frame.children[i].hasAttribute("label")){
		//CurrentNode+= "["+frame.children[i].getAttribute("label")+"]";
	    }
	    String +=
		"digraph{\nrankdir=\"TB\";\n" +
		//CurrentNode +
		graphFS(frame.children[i],StringIndent) +
		"\n}";
	}
	else{
	    if (frame.children[i].getAttribute("id")=="semrelSVG") {
		// the predicate
		String +=
		    "digraph\n";
		String += frame.children[i].children[0].getElementsByTagName("text")[0].innerHTML;
		// the arguments
		for(var j =1; j < frame.children[i].children.length; j++){
		    if (frame.children[i].children[j].getAttribute("type") == "label") {
			String += "[" +  frame.children[i].children[j].getElementsByTagName("text")[0].innerHTML +"] ";
		    }
		    else{
			var current=frame.children[i].children[j].getElementsByTagName("text");
			if(current.length>0)
			    String += current[0].innerHTML;
		    }
		}
		String +=
		    "\n";
	    }
	}
    }
    // add some empty lines for debugging
    for(i=0;i<10;i++){String+="\n";}
    //console.log(String);
    return(String);
}


function texifyFrame (frame,texStringIndent) {
    var texString = "";
    for (var i = 0; i < frame.children.length; i++) {
	if (frame.children[i].getAttribute("type")=="fs") {
	    texString +=
		"\\begin{avm}\n" +
		(frame.children[i].hasAttribute("label") ? "\\@{"+frame.children[i].getAttribute("label")+"}" : "") +
		texifyFS(frame.children[i],texStringIndent) +
		"\n\\end{avm}\n\n";
	}
	else{
	    if (frame.children[i].getAttribute("id")=="semrelSVG") {
		// the predicate
		texString +=
		    "\\begin{avm}\n";
		texString += frame.children[i].children[0].getElementsByTagName("text")[0].innerHTML;
		// the arguments
		for(var j =1; j < frame.children[i].children.length; j++){
		    if (frame.children[i].children[j].getAttribute("type") == "label") {
			texString += "\\@{" +  frame.children[i].children[j].getElementsByTagName("text")[0].innerHTML +"} ";
		    }
		    else{
			var current=frame.children[i].children[j].getElementsByTagName("text");
			if(current.length>0)
			    texString += current[0].innerHTML;
		    }
		}
		texString +=
		    "\n\\end{avm}\n\n";
	    }
	}
    }
    return(texString);
}


function texifyNode (node) {
		var texString = "{";
		for (var i = 0; i < node.children.length; i++) {
				if (node.children[i].getAttribute("type") == "fs" && node.children[i].getAttribute("display") != "none") {
						texString += "\\begin{avm}";
						texString += texifyFS(node.children[i]);
						texString += "\\end{avm}";
				}
				if (node.children[i].getAttribute("type") == "fs" && node.children[i].getAttribute("display") == "none") {
						if (node.hasAttribute("phon")) {
								texString += "\\textit{" + node.getAttribute("phon") + "}";
						}
						else {
								texString += node.getAttribute("cat").toUpperCase();
						}
				}
		}
		if (node.hasAttribute("mark")) {
		    var mark = node.getAttribute("mark");
		    //console.log(mark);
				if (mark == "subst") {texString += "$\\downarrow$";}
				if (mark == "anchor") {texString += "$\\diamond$";}
				if (mark == "foot") {texString += "*";}
				if (mark == "star") {texString += "*";}
				if (mark == "op-bound") {texString += "$=\\joinrel=$";}
				//if (mark == "ddaughter") {texString += "$_d$";}
		}
		texString += "}";
		return(texString);
}


function texifyFS (fs){
    var texString = "\\[";
    if (fs.hasAttribute("fstype")) {
	//console.log(fs.getAttribute("fstype"));
	// /_/gi is a regex which we use for escaping underscores (maybe needed at some other places)
	texString += "\\asort{" + fs.getAttribute("fstype").replace(/_/gi,"\\_") + "}";
    }
    for (var i = 0; i < fs.children.length; i++) {
	var feature = fs.children[i];
	if (feature.getAttribute("type") == "feature") {
	    texString += feature.getElementsByTagName("text")[0].innerHTML.toLowerCase();
	    texString += " &amp; ";
	    if (feature.getElementsByTagName("svg")) { // value
		var value = feature.getElementsByTagName("svg")[0];
		var labelString = "";
		var valueString = "";
		for (var j = 0; j < value.children.length; j++) {
		    if (value.children[j].tagName == "text" ){
			valueString = value.children[j].innerHTML;
		    }
		    if (value.children[j].getAttribute("type") == "label") {
			labelString = "\\@{" +  value.children[j].getElementsByTagName("text")[0].innerHTML +"} "; 
		    }
		    if (value.children[j].getAttribute("type") == "fs") {
			valueString += texifyFS(value.children[j]);
		    }
		}
		texString += labelString + valueString + " \\\\ ";
	    }
	}
    }
    texString += "\\]";
    return(texString);
}

function graphFS (fs, indent){
    var String = "";
    var CurrentNode= "["+fs.getAttribute("label")+"]";
    var CurrentType = "";
    var Attribute = "";
    if (fs.hasAttribute("fstype")) {
	//console.log(fs.getAttribute("fstype"));
	// /_/gi is a regex which we use for escaping underscores (maybe needed at some other places)
	CurrentType = fs.getAttribute("fstype").replace(/_/gi,"\\_");
    }
    var TypeString="";
    if(CurrentType!=""){
	//TypeString= "[label=\""+ CurrentType +"\"]";
	TypeString= "[label=\"" + CurrentType + "\", class=\"clickable_node\", id=\"V3\"]";
    }
	
    String += "\""+CurrentNode+ "\"" +TypeString +";\n";
    for (var i = 0; i < fs.children.length; i++) {
	var feature = fs.children[i];
	if (feature.getAttribute("type") == "feature") {
	    Attribute = feature.getElementsByTagName("text")[0].innerHTML.toLowerCase();
	    //CurrentNode += " &amp; ";
	    if (feature.getElementsByTagName("svg")) { // value
		var value = feature.getElementsByTagName("svg")[0];
		var labelString = "";
		var valueString = "";
		for (var j = 0; j < value.children.length; j++) {
		    if (value.children[j].tagName == "text" ){
			valueString = value.children[j].innerHTML;
		    }
		    if (value.children[j].getAttribute("type") == "label") {
			labelString = "\""+ "[" +  value.children[j].getElementsByTagName("text")[0].innerHTML +"]" + "\""; 
		    }
		    if (value.children[j].getAttribute("type") == "fs") {
			String += graphFS(value.children[j], indent);
		    }
		}
		String += "\""+CurrentNode+ "\"" + "->" +  labelString + valueString + " [ label= \" " + Attribute + "\"] ;\n";
	    }
	}
    }
    //String += "\n}";
    return(String);
}
