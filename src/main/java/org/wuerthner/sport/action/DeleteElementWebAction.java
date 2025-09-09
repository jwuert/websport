package org.wuerthner.sport.action;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.json.Json;
import jakarta.json.JsonObjectBuilder;

import org.wuerthner.sport.api.ModelElement;
import org.wuerthner.sport.api.ModelElementFactory;
import org.wuerthner.sport.core.ModelState;
import org.wuerthner.sport.persistence.dao.GenericDao;
import org.wuerthner.sport.persistence.dao.GenericDao.DocumentReference;

@Stateless
public class DeleteElementWebAction implements DeleteElementWebActionInterface {
	
	@Inject
	private GenericDao dao;
	
	@Inject
	public ModelElementFactory factory;
	
	@Override
	public String getId() {
		return "deleteElement";
	}
	
	@Override
	public Map<String, Object> invoke(ModelElementFactory factory, ModelState modelState, Map<String, String> parameterMap) {
		//
		// id: long
		//
		Map<String, Object> resultMap = new HashMap<>();
		if (modelState.hasSelectedElement()) {
			ModelElement selectedElement = modelState.getSelectedElement();
			selectedElement.setDeleted(true);
			dao.persistTree(selectedElement);
			dao.tidy();
			List<DocumentReference> refList = dao.getDocuments(factory.getRootElementType());
			JsonObjectBuilder json = Json.createObjectBuilder();
			json.add("command", "setDocumentList");
			
			resultMap.put("command", "setDocumentList");
			resultMap.put("data", refList.stream().map(ref -> ref.NAME + ":" + ref.ID));
		} else {
			resultMap.put("command", "info");
			resultMap.put("header", "error");
			resultMap.put("message", "No selected element given!");
		}
		return resultMap;
	}
}
