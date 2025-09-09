/*
 * parent and child are both elements of the data model
 */

class RemoveChildOperation {
	
	constructor(child) {
		this.child = child;
		this.parent = (child.id==child.parentId ? null : getElement(_data, child.parentId));
	}
	
	undoable() {
		return (this.parent!=null);
	}
	
	execute() {
		// if parent exists, remove child from parent + unlink child
		if (this.parent && (this.parent.id != this.child.id)) {
			this.parent.children.splice( this.parent.children.indexOf(this.child), 1 );
			this.child.parentId = this.child.id;
		}
	}
	
	undo() {
		// if parent exists, add child to parent + link child:parent
		if (this.parent && (this.parent.id != this.child.id)) {
			this.parent.children.push(this.child);
			this.child.parentId = this.parent.id;
		}
	}
	
	info() {
		return "RemoveChildOperation parent: " + (this.parent ? this.parent.attributes.id + " [" + this.parent.type + "], " : "") + " child: " + this.child.attributes.id + " [" + this.child.type + "]"; 
	}
	
	shortInfo() {
		return "Remove " + this.child.attributes.id + " " + (this.parent ? "from " + this.parent.attributes.id : "");
	}
}