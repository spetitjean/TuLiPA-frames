// scripts for the viewer page

window.onload = function () {
		
		//$('body').layout({ applyDefaultStyles: true });
    var margin = {
        top: 20,
        right: 120,
        bottom: 20,
        left: 120
    },
				width = 1000,
        height = 800;

    //file Uploader

		if (typeof uploaded_file === 'undefined') {
				var uploaded_file = $('#file_display').val();
				$( '#ajaxLoadAni' ).fadeOut( 1000);
		}

		//console.log(uploaded_file);
		var request = new XMLHttpRequest();
    request.open("GET", uploaded_file, false);
    request.send();
		var xml = request.responseXML;
    var entries = xml.getElementsByTagName("entry");
		// $('#numentry').val(entries.length);
    var entrytags = "";

		for (i = 0; i < entries.length; i++) {
        entrytags = entrytags + '<span class="btn btn-primary btn-block" id=' + i + '>' + entries[i].attributes.name.value + '</span>';
    }

		
		// We use d3 to connect single models of the grammar file with the viewer
    d3.select('#entries').append('div').html(entrytags);
    $('#numentry').text(entries.length);
    d3.selectAll("span")
        .on("click", function (d) {
            // active Button
            $(this).addClass('active').siblings().removeClass('active');
            var entry = entries[d3.select(this)[0][0].attributes.id.value];

	    if (document.getElementById("semFrame") != null) {
		var semFrame = document.getElementById("semFrame");
		
		// remove all svg children
		while (semFrame.getElementsByTagName("svg")[0]){
		    semFrame.removeChild(semFrame.getElementsByTagName("svg")[0]);
		}
		
		// add a fresh svg root
		var svgRootSemFrame = document.createElementNS("http://www.w3.org/2000/svg","svg");
		semFrame.appendChild(svgRootSemFrame);
		
		// draw semanticsFrame
		makeFrame(svgRootSemFrame,entry); // makeTree is part of DrawTree.js
		svgRootSemFrame.setAttribute("width",svgRootSemFrame.getBBox().width+10); // TODO: this should be more dynamic
		svgRootSemFrame.setAttribute("height",svgRootSemFrame.getBBox().height+10);
		svgRootSemFrame.setAttribute("max-height","100%");
		svgRootSemFrame.setAttribute("max-width","100%");
		
		// if SVG element is empty, remove dimension
		if (document.getElementById("SemFrameSVG")!=null) 
		    if (document.getElementById("semFrameSVG").children.length < 1) {
			semFrame.remove();
		    }
	    }

	    // render syntax tree
	    if (document.getElementById("synTree") != null) {
		
		var synTree = document.getElementById("synTree");
		
		// remove all svg children
		while (synTree.getElementsByTagName("svg")[0]){
		    synTree.removeChild(synTree.getElementsByTagName("svg")[0]);
		}
		
		// add a fresh svg root
		var svgRoot = document.createElementNS("http://www.w3.org/2000/svg","svg");
		synTree.appendChild(svgRoot);
		
	    // display trace
	    if (document.getElementById("Trace") != null) {
		var Trace = document.getElementById("Trace");
		
		// remove all svg children
		while (Trace.getElementsByTagName("svg")[0]){
		    Trace.removeChild(Trace.getElementsByTagName("svg")[0]);
		}
		
		// add a fresh svg root
		var svgRootTrace = document.createElementNS("http://www.w3.org/2000/svg","svg");
		Trace.appendChild(svgRootTrace);
		
		// draw trace
		makeTrace(svgRootTrace,entry); // makeTrace is part of xmgview.js
		svgRootTrace.setAttribute("width",svgRootTrace.getBBox().width+10); // TODO: this should be more dynamic
		svgRootTrace.setAttribute("height",svgRootTrace.getBBox().height+10);
		svgRootTrace.setAttribute("max-height","100%");
		svgRootTrace.setAttribute("max-width","100%");
		
		// if SVG element is empty, remove dimension
		if (document.getElementById("TraceSVG")!=null) 
		    if (document.getElementById("TraceSVG").children.length < 1) {
			Trace.remove();
		    }
	    }


	    // display interface
	    if (document.getElementById("Interface") != null) {
		var Interface = document.getElementById("Interface");
		
		// remove all svg children
		while (Interface.getElementsByTagName("svg")[0]){
		    Interface.removeChild(Interface.getElementsByTagName("svg")[0]);
		}
		
		// add a fresh svg root
		var svgRootInterface = document.createElementNS("http://www.w3.org/2000/svg","svg");
		Interface.appendChild(svgRootInterface);
		
		// draw interface
		makeInterface(svgRootInterface,entry); // makeInterface is part of xmgview.js
		svgRootInterface.setAttribute("width",svgRootInterface.getBBox().width+10); // TODO: this should be more dynamic
		svgRootInterface.setAttribute("height",svgRootInterface.getBBox().height+10);
		svgRootInterface.setAttribute("max-height","100%");
		svgRootInterface.setAttribute("max-width","100%");
		
		// if SVG element is empty, remove dimension
		if (document.getElementById("InterfaceSVG")!=null) 
		    if (document.getElementById("InterfaceSVG").children.length < 1) {
			Interface.remove();
		    }
	    }
							
		
		// draw syntactic tree 
		makeTree(svgRoot,entry); // makeTree is part of xmgview.js
		svgRoot.setAttribute("width",svgRoot.getBBox().width+10); // TODO: this should be more dynamic
		svgRoot.setAttribute("height",svgRoot.getBBox().height+10);
		svgRoot.setAttribute("max-width","100%"); 
		svgRoot.setAttribute("max-height","100%");
		
		
		// if SVG element is empty, remove dimension
		if (document.getElementById("synTreeSVG").children.length < 1) {
		    synTree.remove();
		}
	    }
	    
	    // change bootstrap size, if one of the dimensions is empty
	    if (document.getElementById("synTree") != null && document.getElementById("semFrame") == null) {
		document.getElementById("synTree").setAttribute("class","col-sm-10");
	    }
	    if (document.getElementById("synTree") == null && document.getElementById("semFrame") != null) {
		document.getElementById("semFrame").setAttribute("class","col-sm-10");
	    }
	    
						
        });

		// filter entries
		// FIXME: also hides the "Filter" label; update entry counter
    (function ($) {
        $('#filter').keyup(function () {
            var rex = new RegExp($(this).val(), 'i');
            $('span').hide();
            $('svg').hide();
            $('span').filter(function () {
                return rex.test($(this).text());
            }).show();
        })
  }(jQuery));

}

