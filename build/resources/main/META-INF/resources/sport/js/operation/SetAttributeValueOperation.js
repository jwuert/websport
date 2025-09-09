/*
 * 
 */

class SetAttributeValueOperation {
	
	constructor(element, attributeName, value) {
		this.element = element;
		this.attributeName = attributeName;
		this.oldValue = element.attributes[attributeName];
		this.newValue = value;
	}
  
	undoable() {
		return true;
	}
	
	execute() {
		setAttributeValue(this.element, this.attributeName, this.newValue);
	}
	
	undo() {
		setAttributeValue(this.element, this.attributeName, this.oldValue);
	}
	
	info() {
		return "SetAttributeValueOperation element: " + this.element.attributes.id + " [" + this.element.type + "], attribute " + this.attributeName + " = '" + this.newValue + "' ('" + this.oldValue +"')"; 
	}
	
	shortInfo() {
		return this.element.attributes.id + "." + this.attributeName + " = '" + this.newValue + "'" + (this.oldValue ? " (was: '" + this.oldValue + "')" : "");
	}
}