function fullValidation(element) {
	var validationMessages = [];
	var type = element["type"];
	for (var key in _model[type].attributes) {
		var meta = _model[type]["attributes"][key];
		var dependencies = meta["dependencies"];
		var depResolved = hasDependenciesResolved(element, key);
		var disabled = meta["readonly"] ? true : !depResolved;
		if (!disabled) {
			var messages = validate(element, key)
			for (var i=0; i<messages.length; i++) {
				validationMessages.push(element.type + " '" + element.attributes.id + "', attribute '" + meta.label + "': " + messages[i]);
			}
		}
	}
	var children = element["children"];
	for (var i=0; i<children.length; i++) {
		var messages = fullValidation(children[i]);
		Array.prototype.push.apply(validationMessages, messages);
	}
	return validationMessages;
}