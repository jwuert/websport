function makeBSInputField(element, meta, attributeName, value, editPermission, showLabel, firstAttribute) {
	var id = element.id;
	var label = meta["label"];
	var type = meta["type"];
	var attributeType = meta["class"];
	var dependencies = meta["dependencies"];
	var depResolved = hasDependenciesResolved(element, attributeName);
	var cssClass = "validField";
	var readOnly = (meta["readonly"] || (!editPermission)) ? "readOnly " : "";
	var disabled = depResolved ? "" : "disabled ";
	var theFirst = "";
	var theType = "";
	var theValue = "";
	var theEvent = "";
	var code = "";
	if (!value) { value = ""; }
	
	code += "<div class='form-group' style='margin-bottom:2px'>";

	if (type === "Text") {
		//
		// TEXT
		//
		theFirst = (firstAttribute?"createNavigation(_data);":"");
		theValue = "value=\"" + value + "\"";
		theEvent = "onblur=\"javascript:performSetAttributeValue(_selection, '" + attributeName + "', this.value);"+theFirst+"\" ";
		theEvent += "onkeydown=\"javascript:if (event.keyCode == 13) { performSetAttributeValue(_selection, " + "'" + attributeName + "', this.value);"+theFirst+"}\" ";
		if (showLabel) {
			code += "<label for='attribute_"+id+"."+attributeName+"' style='margin-bottom:0'>"+label+"</label>";
		}
		code += "<input type='text' class='form-control' name='attribute_"+id+"."+attributeName+"' id='attribute_"+id+"."+attributeName+"' "+theValue+" " + theEvent + " " + readOnly+disabled + "></input>";
		
	} else if (type === "Textarea") {
		//
		// TEXTAREA
		//
		theEvent = "onchange=\"javascript:performSetAttributeValue(_selection, '" + attributeName + "', this.value);\"";
		if (showLabel) {
			code += "<label for='attribute_"+id+"."+attributeName+"' style='margin-bottom:0'>"+label+"</label>";
		}
		code += "<textarea class='form-control' id='attribute_"+id+"."+attributeName+"' rows='3' " + theEvent + " " + readOnly+disabled + ">";
		code += value;
		code += "</textarea>";
		
	} else if (type === "Display") {
		//
		// DISPLAY
		//
		if (meta["twoColumns"]) {
			if (showLabel) {
				code += "<label for='attribute_"+id+"."+attributeName+"' style='margin-bottom:0'>"+label+"</label>";
			}
			var varArray = meta["variables"];
			var val = meta["default"];
			for (var i=0; i<varArray.length; i++) {
				var text = resolveVariable(varArray[i], element, null);
				val = val.replace(/\$/, text);
			}
			code += "<div class='alert alert-info' role='alert'>" + val + "</div>";
		} else {
			code += "<div class='alert alert-info' role='alert'>" + label + "</div>";
		}
		
	} else if (type === "Alternative") {
		//
		// ALTERNATIVE
		//
		theEvent = "onchange=\"javascript:performSetAttributeValue(_selection, '" + attributeName + "', this.checked.toString());\" ";
		theValue = (value==="true" ? "checked" : "");
		code += "<input id='attribute_"+id+"."+attributeName+"' class='form-check-input' type='checkbox' " + theValue + " " + theEvent + " " + readOnly+disabled +" />";
		if (showLabel) {
			code += "<label class='form-check-label' for='defaultCheck1'>" + label + "</label>"
		}
		
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
		theEvent  = "onblur=\"javascript:performSetAttributeValue(_selection, '" + attributeName + "', this.value);\" ";
		theEvent += "onclick=\"javascript:performSetAttributeValue(_selection, '" + attributeName + "', this.value);\"";
		if (showLabel) {
			code += "<label for='attribute_"+id+"."+attributeName+"' style='margin-bottom:0'>"+label+"</label>";
		}
		code += "<select class='form-control' name='attribute_"+id+"."+attributeName+"' id='attribute_"+id+"."+attributeName + "' " + theEvent + " " + readOnly+disabled+">";
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
		
		theEvent  = "onblur=\"javascript:performSetAttributeValue(_selection, '" + attributeName + "', this.value);\" ";
		theEvent += "onclick=\"javascript:performSetAttributeValue(_selection, '" + attributeName + "', this.value);\"";
		if (showLabel) {
			code += "<label for='attribute_"+id+"."+attributeName+"' style='margin-bottom:0'>"+label+"</label>";
		}
		code += "<select class='form-control' name='attribute_"+id+"."+attributeName+"' id='attribute_"+id+"."+attributeName + "' " + theEvent + " " + readOnly+disabled+">";
		for (var itemValue in items) {
			var selected = (itemValue === value ? "selected" : "");
			var itemLabel = items[itemValue];
			code += "<option " + selected + " value='" + itemValue + "'>" + itemLabel + "</option>";
		}
		code += "</select>";
		
	} else if (type === "StaticMultiSelect") {
		//
		// STATIC MULTI-SELECT
		//
		var items = meta["items"];
		theEvent  = "onblur=\"javascript:performSetAttributeValue(_selection, '" + attributeName + "', $(this).val());\" ";
		theEvent += "onclick=\"javascript:performSetAttributeValue(_selection, '" + attributeName + "', $(this).val());\"";
		if (showLabel) {
			code += "<label for='attribute_"+id+"."+attributeName+"' style='margin-bottom:0'>"+label+"</label>";
		}
		code += "<select multiple='multiple' style='height:500px' class='form-control' name='attribute_"+id+"."+attributeName+"' id='attribute_"+id+"."+attributeName + "' " + theEvent + " " + readOnly+disabled+">";
		for (var itemValue in items) {
			var selected = (containsValue(value, itemValue) ? "selected" : "");
			var itemLabel = items[itemValue];
			code += "<option " + selected + " value='" + itemValue + "'>" + itemLabel + "</option>";
		}
		code += "</select>";
		
	} else if (type === "DynamicMultiSelect") {
		//
		// DYNAMIC MULTI-SELECT
		//
		var items = {};
		var elementList = lookupElement(_data, meta.elementFilterType);
		for (var i=0; i<elementList.length; i++) {
			var res = resolveCheck(meta.elementFilterCheck, elementList[i], null);
			if (res) {
				items[elementList[i].attributes.id] = elementList[i].attributes.id;
			}
		}
		
		theEvent  = "onblur=\"javascript:performSetAttributeValue(_selection, '" + attributeName + "', $(this).val());\" ";
		theEvent += "onclick=\"javascript:performSetAttributeValue(_selection, '" + attributeName + "', $(this).val());\"";
		if (showLabel) {
			code += "<label for='attribute_"+id+"."+attributeName+"' style='margin-bottom:0'>"+label+"</label>";
		}
		code += "<select multiple='multiple' class='form-control' name='attribute_"+id+"."+attributeName+"' id='attribute_"+id+"."+attributeName + "' " + theEvent + " " + readOnly+disabled+">";
		for (var itemValue in items) {
			var selected = (containsValue(value, itemValue) ? "selected" : "");
			var itemLabel = items[itemValue];
			code += "<option " + selected + " value='" + itemValue + "'>" + itemLabel + "</option>";
		}
		code += "</select>";
		
	}
	code += "</div>";
	code += "<div style='height:12px'><span id=\"val_"+id+"."+attributeName+"\" style=\"display:none\" class=\"theError\"><span></div>";
	return code;
}

function containsValue(value, itemValue) {
	var result = false;
	try {
		result = (value.indexOf(itemValue) >= 0);
	} catch (ex) {
		console.error(ex);
		console.error(value + ", " + itemValue);
	}
	return result;
}

