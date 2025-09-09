class CompareAction {
	constructor() {}
	
	isEnabled() {
		return _dirty;
	}
	
	getLabel() {
		return "Compare";
	}
	
	init() {
		this.invoke();
	}
	
	invoke() {
		if (ws) {
			ws.send("{\"command\":\"compare\", \"rootId\":" + _data.id + ", \"data\":" + JSON.stringify(_data) + "}");
		} else {
			window.location.href = "../jsp/logout.jsp";
		}
	}
}

function showComparison(delta) {
	console.log(delta);
	var header = "Comparison";
	var info = "";
	info += "<div class='modal-content'>";
	info += " <div class='modal-header'>";
	info += "  <h4 class='modal-title'>" + header + "</h4>";
	info += " </div>";
	info += " <div class='modal-body'>";
	
	info += "<table class='table table-sm'>";
	info += "  <thead>";
	info += "    <tr>";
	for (var col in delta[0]) {
		info += "      <th scope=\"col\">" + delta[0][col] + "</th>";
	}
	info += "    </tr>";
	info += "  </thead>";
	info += "  <tbody>";
	for (var i=1; i<delta.length; i++) {
		info += "    <tr>";
		for (var col in delta[i]) {
			info += "      <td>" + delta[i][col] + "</td>";
		}
		info += "    </tr>";
	}
	info += "  </tbody>";
	info += "</table>";


	info += " </div>";
	info += " <div class='modal-footer'>";
	info += "  <button id='okBtn' type='button' class='btn btn-default' onclick='javascript:document.getElementById(\"comparison\").style.display=\"none\"'>OK</button>";
	info += " </div>";
	info += "</div>";
	
	document.getElementById("comparison").innerHTML=info;
	document.getElementById("comparison").style.display='inline';
	// document.getElementById("comparison").style.width='auto';

}