function makeTableInputField(element, attributeName, editPermission) {
	var code = "";
	var id = element.id;
	var label = element.label;
	var value = element.attributes[attributeName];
	var meta = _model[element.type]["attributes"][attributeName];
	if (meta) {
		// Attribute exists (it may not exist e.g. "codeList" for "Text"
		var type = meta.type;
		var attributeType = meta["class"];
		var dependencies = meta["dependencies"];
		var depResolved = hasDependenciesResolved(element, attributeName);
		var cssClass = "validField";
		var readOnly = (meta["readonly"] || (!editPermission)) ? "readOnly " : "";
		var disabled = depResolved ? "" : "disabled ";
		if (!value) { value = ""; }
		
			if (type === "Text") {
				//
				// TEXT
				//
				theValue = "value=\"" + value + "\"";
				theEvent = "onblur=\"javascript:performSetAttributeValue(getElement(_data,"+id+"), '" + attributeName + "', this.value);\" ";
				theEvent += "onkeydown=\"javascript:if (event.keyCode == 13) { performSetAttributeValue(getElement(_data,"+id+"), " + "'" + attributeName + "', this.value);}\" ";
				code += "<input class='tableField' type='text' name='attribute_"+id+"."+attributeName+"' id='attribute_"+id+"."+attributeName+"' "+theValue+" " + theEvent + " " + readOnly+disabled + "></input>";
				
			} else if (type === "Alternative") {
				//
				// ALTERNATIVE
				//
				theEvent = "onchange=\"javascript:performSetAttributeValue(getElement(_data,"+id+"), '" + attributeName + "', this.checked.toString());\" ";
				theValue = (value==="true" ? "checked" : "");
				code += "<input id='attribute_"+id+"."+attributeName+"' type='checkbox' " + theValue + " " + theEvent + " " + readOnly+disabled +" />";
				
			} else if (type === "Chooser") {
				//
				// CHOOSER
				//
				code += "-";
				
			} else if (type === "StaticMapping") {
				//
				// STATIC MAPPING
				//
				var items = meta["items"];
				theEvent  = "onblur=\"javascript:performSetAttributeValue(getElement(_data,"+id+"), '" + attributeName + "', this.value);\" ";
				theEvent += "onclick=\"javascript:performSetAttributeValue(getElement(_data,"+id+"), '" + attributeName + "', this.value);\"";
				code += "<select class='tableField' name='attribute_"+id+"."+attributeName+"' id='attribute_"+id+"."+attributeName + "' " + theEvent + " " + readOnly+disabled+">";
				code += "<option value=''></option>";
				for (var itemValue in items) {
					var selected = (itemValue === value ? "selected" : "");
					var itemLabel = items[itemValue];
					code += "<option " + selected + " value='" + itemValue + "'>" + itemLabel + "</option>";
				}
				code += "</select>";

			} else if (type === "DynamicMapping") {
				//
				// DYNAMIC MAPPING
				//
				var items = {};
				items[""] = "";
				var elementList = lookupElement(_data, meta.elementFilterType);
				for (var i=0; i<elementList.length; i++) {
					var res = resolveCheck(meta.elementFilterCheck, elementList[i], null);
					if (res) {
						items[elementList[i].attributes.id] = elementList[i].attributes.id;
					}
				}
				
				theEvent  = "onblur=\"javascript:performSetAttributeValue(getElement(_data,"+id+"), '" + attributeName + "', this.value);\" ";
				theEvent += "onclick=\"javascript:performSetAttributeValue(getElement(_data,"+id+"), '" + attributeName + "', this.value);\"";
				code += "<select class='tableField' name='attribute_"+id+"."+attributeName+"' id='attribute_"+id+"."+attributeName + "' " + theEvent + " " + readOnly+disabled+">";
				for (var itemValue in items) {
					var selected = (itemValue === value ? "selected" : "");
					var itemLabel = items[itemValue];
					code += "<option " + selected + " value='" + itemValue + "'>" + itemLabel + "</option>";
				}
				code += "</select>";
				
			}
		
/*
		if ((attributeType === "MappedSelectableStringAttribute") || (attributeType === "SelectableStringAttribute")) {
			//
			// <SELECT>
			//
			var items = {};
			if (attributeType === "SelectableStringAttribute") {
				// Dynamic Selection
				items[""] = "";
				var elementList = lookupElement(_data, meta.elementFilterType);
				for (var i=0; i<elementList.length; i++) {
					var res = resolveCheck(meta.elementFilterCheck, elementList[i], null);
					if (res) {
						items[elementList[i].attributes.id] = elementList[i].attributes.id;
					}
				}
			} else {
				// Static Selection
				items = meta["items"];
			}
			theEvent  = "onblur=\"javascript:performSetAttributeValue(getElement(_data,"+id+"), '" + attributeName + "', this.value);\" ";
			theEvent += "onclick=\"javascript:performSetAttributeValue(getElement(_data,"+id+"), '" + attributeName + "', this.value);\"";
			code += "<select class='tableField' name='attribute_"+id+"."+attributeName+"' id='attribute_" + id+"."+attributeName + "' " + theEvent + " " + " " + readOnly+disabled+">";
			for (var itemValue in items) {
				var selected = (itemValue === value ? "selected" : "");
				var itemLabel = items[itemValue];
				code += "<option " + selected + " value='" + itemValue + "'>" + itemLabel + "</option>";
			}
			code += "</select>";
		} else {
			//
			// <INPUT>
			//
			if (type === "Boolean") {
				theEvent = "onchange=\"javascript:performSetAttributeValue(getElement(_data,"+id+"), '" + attributeName + "', this.checked.toString());\" ";
				theValue = (value==="true" ? "checked" : "");
				code += "<input id='attribute_"+id+"."+attributeName+"' type='checkbox' " + theValue + " " + theEvent + " " + readOnly+disabled +" />";
			} else {
				theValue = "value=\"" + value + "\"";
				theEvent = "onblur=\"javascript:performSetAttributeValue(getElement(_data,"+id+"), '" + attributeName + "', this.value);\" ";
				theEvent += "onkeydown=\"javascript:if (event.keyCode == 13) { performSetAttributeValue(getElement(_data,"+id+"), " + "'" + attributeName + "', this.value);}\" ";
				code += "<input class='tableField' type='text' name='attribute_"+id+"."+attributeName+"' id='attribute_"+id+"."+attributeName+"' "+theValue+" " + theEvent + " " + readOnly+disabled + "></input>";
			}
		}
*/
	} else {
		// attribute does not exist!
		code = "-";
	}
	return code;
}
