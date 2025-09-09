function createNavigation(data) {
	var html = buildNavigation(data, true);
	document.getElementById("navigation").innerHTML = html;
	
	// add functions:
	var toggler = document.getElementsByClassName("caret");
	var i;
	
	for (i = 0; i < toggler.length; i++) {
	  toggler[i].addEventListener("click", function() {
	    this.parentElement.querySelector(".nested").classList.toggle("active");
	    this.classList.toggle("caret-down");
	    var tid = this.getAttribute("value");
	    _expandeNode[tid] = this.getAttribute("class").includes("caret-down");
	  });
	}
	if (_selection) {
		renderSelectedNavItem(_selection.id);
	}
}

function buildNavigation(data, toplevel) {
	var html = "";
	var expanded;
	if (toplevel) {
		html += "<ul id=\"navUL\">";
		expanded = true;
	} else {
		expanded = _expandeNode[data.id];
	}
	var attributes = data["attributes"];
	var children = data["children"];
	children.sort(function(a, b){return a.order - b.order});
	
	var name = Object.values(attributes)[0];
	html += "<li>";
	if (children.length>0) {
		html += "<span value=\""+data.id+"\" class=\"caret" + (expanded ? " caret-down" : "") + "\"></span>";
	} else {
		html += "<span value=\""+data.id+"\" class=\"caretEmpty\"></span>";
	}
//	var selected = (_selection && data.id==_selection.id ? "navSelected" : "navUnselected");
//	console.log("sel: " + data.id + ": " + selected);
	html += "<a id='nav_"+data.id+"' href=\"javascript:selectElement(" + data["id"] + ",'Attributes')\" class='navUnselected'>" + name  + /* "(" + data.order + ")" + */ "</a>";
	
	//
	// For top-level element, add folders for child types:
	//
	if (data.parentId==data.id) {
		html += "<ul class=\"nested" + (expanded ? " active" : "") + "\">";
		
		// for all types
		var cTypes = [... new Set(children.map(a => a.type))];
		cTypes.sort();
		for (var t=0; t<cTypes.length; t++){
			html += "<li>";
			var category = cTypes[t];
			expanded = _expandeNode[category];
			html += "<span value=\""+category+"\" class=\"caret" + (expanded ? " caret-down" : "") + "\"></span>";
			html += "<a id='nav_"+category+"' href=\"javascript:selectCategory('" + category + "', '" + children.filter((el,index) => (el.type==category)).length + "')\" class='navUnselected'>" + category + "</a>";
			html += "<ul class=\"nested" + (expanded ? " active" : "") + "\">";
			for (var i=0; i<children.length; i++) {
				if (children[i].type==category) {
					var childHtml = buildNavigation(children[i], false);
					html += childHtml;
				}
			}
			
			html += "</ul>";
			html += "</li>";
		}
		html += "</ul>";
	} else {
		
		//
		// handle children
		//
		if (children.length>0) {
			html += "<ul class=\"nested" + (expanded ? " active" : "") + "\">";
			for (var i=0; i<children.length; i++) {
				var childHtml = buildNavigation(children[i], false);
				html += childHtml;
			}
			html += "</ul>";
		}	
		html += "</li>";
		
	}
	
	if (toplevel) {
		html += "</ul>";
	}
	return html;
}
