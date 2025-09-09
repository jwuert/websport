/*
 * parent and child are both elements of the data model
 */

class AddChildOperation {
	
	constructor(parent, child) {
		this.newParent = parent;
		this.oldParent = (child.id==child.parentId ? null : getElement(_data, child.parentId));
		this.child = child;
	}
	
	undoable() {
		return true;
	}
  
	execute() {
		// if oldParent exists, remove child from oldParent + unlink child
		if (this.oldParent && (this.oldParent.id != this.child.id)) {
			this.oldParent.children.splice( this.oldParent.children.indexOf(this.child), 1 );
			this.child.parentId = this.child.id;
		}
		// add child to newParent + link child:newParent
		if (this.newParent.id != this.child.id) {
			this.newParent.children.push(this.child);
			this.child.parentId = this.newParent.id;
		}
	}
	
	undo() {
		// remove child from newParent + unlink child
		if (this.newParent.id != this.child.id) {
			this.newParent.children.splice( this.newParent.children.indexOf(this.child), 1 );
			this.child.parentId = this.child.id;
		}
		// if oldParent exists, add child to oldParent + link child:oldParent
		if (this.oldParent && (this.oldParent.id != this.child.id)) {
			this.oldParent.children.push(this.child);
			this.child.parentId = this.oldParent.id;
		}
	}
	
	info() {
		return "AddChildOperation old parent: " + (this.oldParent ? this.oldParent.attributes.id + " [" + this.oldParent.type + "], " : "") + "new parent: " + this.newParent.attributes.id + " [" + this.newParent.type + "], child: " + this.child.attributes.id + " [" + this.child.type + "]"; 
	}
	
	shortInfo() {
		return "Add " + this.child.attributes.id + " " + (this.oldParent ? "from "+this.oldParent.attributes.id : "") + " to " + this.newParent.attributes.id;
	}
}