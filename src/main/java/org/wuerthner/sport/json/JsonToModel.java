package org.wuerthner.sport.json;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonValue;
import jakarta.json.JsonValue.ValueType;

import org.wuerthner.sport.api.Attribute;
import org.wuerthner.sport.api.ModelElement;
import org.wuerthner.sport.api.ModelElementFactory;
import org.wuerthner.sport.core.AbstractModelElement;
import org.wuerthner.sport.persistence.dao.GenericDao;

@Stateless
public class JsonToModel {
	// private Comparator<? super ModelElement> compareByOrder = (el1, el2) -> el1.getOrder() - el2.getOrder();
	
	@Inject
	public ModelElementFactory factory;
	
	@Inject
	public GenericDao genericDao;
	
	public <T> ModelElement createTree(JsonObject jsonObject) {
		long id = jsonObject.getInt("id");
		String type = jsonObject.getString("type");
		JsonObject attributes = jsonObject.getJsonObject("attributes");
		JsonArray children = jsonObject.getJsonArray("children");
		ModelElement element = factory.createElement(type);
		element.setTechnicalId(id);
		for (Attribute<?> a : element.getAttributes()) {
			@SuppressWarnings("unchecked")
			Attribute<T> attribute = (Attribute<T>) a;
			if (attributes.keySet().contains(attribute.getName())) {
				String value = attributes.getString(attribute.getName());
				element.performTransientSetAttributeValueOperation(attribute, attribute.getValue(value));
			}
		}
		for (int i = 0; i < children.size(); i++) {
			ModelElement child = createTree(children.getJsonObject(i));
			((AbstractModelElement) element).addChild(child);
		}
		return element;
	}
	
	public <T> ModelElement mergeTree(ModelElement root, JsonObject jsonObject, Map<Integer, ModelElement> preliminaryIdMap) {
		int id = jsonObject.getInt("id");
		String type = jsonObject.getString("type");
		// int order = jsonObject.getInt("order");
		JsonObject attributes = jsonObject.getJsonObject("attributes");
		JsonArray jsonChildren = jsonObject.getJsonArray("children");
		ModelElement element;
		if (id < 0) {
			element = factory.createElement(type);
			// element.setOrder(order);
			preliminaryIdMap.put(id, element);
		} else {
			element = loadElement(root, id);
			if (element == null) {
				// this is the case when the element had been added before (so it has an id>0), has been removed via undo, and has been re-added via redo!
				// alternative, we might change this by taking the "deleted" flag on the database into account! (Or perform real deletion on the database!)
				element = factory.createElement(type);
				// element.setOrder(order);
				preliminaryIdMap.put(id, element);
			}
		}
		for (Attribute<?> a : element.getAttributes()) {
			@SuppressWarnings("unchecked")
			Attribute<T> attribute = (Attribute<T>) a;
			if (attributes.keySet().contains(attribute.getName())) {
				JsonValue jsonValue = attributes.get(attribute.getName());
				String value;
				if (jsonValue.getValueType()==ValueType.ARRAY) {
					value = stringify((JsonArray)jsonValue);
				} else {
					value = attributes.getString(attribute.getName());
				}
				element.performTransientSetAttributeValueOperation(attribute, attribute.getValue(value));
			}
		}
		List<Long> jsonIdList = new ArrayList<>();
		// add the children from the json array:
		for (int i = 0; i < jsonChildren.size(); i++) {
			JsonObject jsonChild = jsonChildren.getJsonObject(i);
			ModelElement modelElementChild = mergeTree(element, jsonChild, preliminaryIdMap);
			// if (!element.getChildren().stream().filter(el -> el.getCategory().equals(modelElementChild.getCategory())).map(el -> el.getOrder()).collect(Collectors.toList()).contains(modelElementChild.getOrder())) {
			// is this correct?
			if (!element.getChildren().stream().map(el -> el.getId()).collect(Collectors.toList()).contains(modelElementChild.getId())) {
				((AbstractModelElement) element).addChild(modelElementChild); // ###
			}
			jsonIdList.add(modelElementChild.getTechnicalId());
		}
		// remove the children that are not part of the json array:
		List<ModelElement> toBeRemoved = element.getChildren().stream().filter(child -> !jsonIdList.contains(child.getTechnicalId())).collect(Collectors.toList());
		for (ModelElement child : toBeRemoved) {
			element.performTransientRemoveChildOperation(child);
			genericDao.persistTree(child);
		}
		return element;
	}
	
	private String stringify(JsonArray jsonArray) {
		String result = jsonArray.stream().map(val -> val.toString().replaceAll("^\"", "").replaceAll("\"$", "")).collect(Collectors.joining(","));
		return result;
	}

	// this can probably go away:
	public void verifyOrder(ModelElement element) {
		// List<Class<? extends ModelElement>> childTypes = element.getChildTypes();
		// for (Class<? extends ModelElement> childType : childTypes) {
		// List<ModelElement> children = element.getChildren(childType);
		List<String> childTypes = element.getChildTypes();
		for (String childType : childTypes) {
			List<ModelElement> children = element.getChildrenByType(childType);
			// children.sort(compareByOrder);
			int newOrder = 0;
			for (ModelElement child : children) {
				// child.setOrder(newOrder++);
			}
		}
		element.getChildren().forEach(child -> verifyOrder(child));
	}
	
	private ModelElement loadElement(ModelElement element, long id) {
		ModelElement result = null;
		if (element.getTechnicalId() == id) {
			result = element;
		} else {
			for (ModelElement child : element.getChildren()) {
				result = loadElement(child, id);
				if (result != null) {
					break;
				}
			}
		}
		return result;
	}
}
