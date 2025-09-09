package org.wuerthner.sport.json;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.json.JsonValue;

import org.wuerthner.sport.api.Attribute;
import org.wuerthner.sport.api.Check;
import org.wuerthner.sport.api.ModelElement;
import org.wuerthner.sport.api.ModelElementFactory;
import org.wuerthner.sport.api.Variable;
import org.wuerthner.sport.api.attributetype.Display;
import org.wuerthner.sport.api.attributetype.DynamicMapping;
import org.wuerthner.sport.api.attributetype.DynamicMultiSelect;
import org.wuerthner.sport.api.attributetype.StaticMapping;
import org.wuerthner.sport.api.attributetype.StaticMultiSelect;
import org.wuerthner.sport.attribute.MessageAttribute;
import org.wuerthner.sport.core.ValidationResult;
import org.wuerthner.sport.core.ValidationResult.ValidationResultEntry;
import org.wuerthner.sport.persistence.dao.GenericDao.DocumentReference;

public class SpeedyJson {
	
	public static JsonObject create(ModelElement element) {
		JsonObjectBuilder json = Json.createObjectBuilder();
		json.add("id", element.getTechnicalId());
		json.add("type", element.getType());
		// json.add("order", element.getOrder());
		JsonObjectBuilder jsonAttributes = getJsonAttributes(element, false, false);
		json.add("attributes", jsonAttributes);
		JsonArrayBuilder childArray = Json.createArrayBuilder();
		for (ModelElement child : element.getChildren()) {
			JsonObject jsonChild = create(child);
			childArray.add(jsonChild);
		}
		json.add("children", childArray.build());
		return json.build();
	}
	
	public static JsonValue createModel(ModelElementFactory factory) {
		List<ModelElement> elementList = factory.createElementList();
		JsonObjectBuilder json = Json.createObjectBuilder();
		for (ModelElement element : elementList) {
			JsonObjectBuilder jsonElement = Json.createObjectBuilder();
			jsonElement.add("attributes", getJsonAttributes(element, true, false));
			jsonElement.add("childTypes", getJsonChildTypes(element));
			jsonElement.add("category", element.getCategory());
			jsonElement.add("isCategory", false);
			json.add(element.getType(), jsonElement.build());
			if (!element.getCategory().equals(element.getType())) {
				// add attribute specification for categories (i.e. type != category):
				JsonObjectBuilder jsonCategory = Json.createObjectBuilder();
				jsonCategory.add("attributes", getJsonAttributes(element, true, true));
				jsonCategory.add("isCategory", true);
				json.add(element.getCategory(), jsonCategory.build());
			}
		}
		return json.build();
	}
	
	public static JsonValue createDocumentMap(List<DocumentReference> documentList) {
		JsonObjectBuilder json = Json.createObjectBuilder();
		for (DocumentReference ref : documentList) {
			json.add(ref.NAME, ref.ID);
		}
		return json.build();
	}
	
	@SuppressWarnings("unchecked")
	public static JsonValue createJsonFromMap(Map<String, Object> map) {
		JsonObjectBuilder builder = Json.createObjectBuilder();
		for (Entry<String,Object> entry : map.entrySet()) {
			String key = entry.getKey();
			Object val = entry.getValue();
			if (val instanceof Check) {
				builder.add(key, createJsonFromMap(((Check)val).getProperties()));
			} else if (val instanceof List) {
				JsonArrayBuilder array = Json.createArrayBuilder();
				List<?> list = (List<?>) val;
				for (Object obj : list) {
					if (obj instanceof Map) {
						array.add(createJsonFromMap((Map<String,Object>)obj));
					} else {
						array.add(""+obj);
					}
				}
				builder.add(key, array);
			} else if (val instanceof ModelElement) {
				builder.add(key, "callerElement");
			} else {
				builder.add(key, ""+val);
			}
		}
		JsonObject obj = builder.build();
		return obj;
	}
	
	public static JsonObject createCommandMessageFromMap(Map<String, Object> map) {
		JsonObjectBuilder builder = Json.createObjectBuilder();
		for (Map.Entry<String, Object> entry : map.entrySet()) {
			String key = entry.getKey();
			Object value = entry.getValue();
			if (value instanceof JsonValue) {
				builder.add(key, (JsonValue) value);
			} else if (value instanceof String) {
				builder.add(key, (String) value);
			} else if (value instanceof Long) {
				builder.add(key, (Long) value);
			} else if (value instanceof Integer) {
				builder.add(key, (Integer) value);
			} else if (value instanceof ModelElement) {
				builder.add(key, create((ModelElement) value));
			} else if (value instanceof List) {
				List<?> rowList = (List<?>) value;
				JsonArrayBuilder rowArray = Json.createArrayBuilder();
				for (Object row : rowList) {
					if (row instanceof List) {
						List<?> itemList = (List<?>) row;
						JsonArrayBuilder itemArray = Json.createArrayBuilder();
						for (Object item : itemList) {
							itemArray.add(String.valueOf(item));
						}
						rowArray.add(itemArray);
					} else {
						rowArray.add(String.valueOf(row));
					}
				}
				builder.add(key, rowArray);
			} else {
				throw new RuntimeException("Value of type '" + value.getClass().getSimpleName() + "' not supported!");
			}
		}
		return builder.build();
	}
	
	public static Map<String, String> jsonToMap(JsonObject json) {
		Map<String, String> map = new HashMap<>();
		map.put("command", json.getString("command"));
		if (json.containsKey("data"))
			map.put("data", "" + json.get("data"));
		if (json.containsKey("selectedId"))
			map.put("selectedId", "" + json.get("selectedId"));
		if (json.containsKey("name"))
			map.put("name", json.getString("name"));
		if (json.containsKey("rootId"))
			map.put("rootId", "" + json.get("rootId"));
		if (json.containsKey("id"))
			map.put("id", "" + json.get("id"));
		if (json.containsKey("value"))
			map.put("value", json.getString("value"));
		if (json.containsKey("attribute"))
			map.put("attribute", json.getString("attribute"));
		if (json.containsKey("index"))
			map.put("index", "" + json.get("index"));
		if (json.containsKey("array"))
			map.put("array", json.getString("array"));
		if (json.containsKey("parentId"))
			map.put("parentId", "" + json.get("parentId"));
		return map;
	}
	
	private static JsonObjectBuilder getJsonAttributes(ModelElement element, boolean metadata, boolean category) {
		JsonObjectBuilder jsonAttributes = Json.createObjectBuilder();
		List<Attribute<?>> attributeList = (category ? element.getCategoryAttributes() : Arrays.asList(element.getAttributes()));
		for (Attribute<?> attribute : attributeList) {
			String key = attribute.getName();
			if (metadata) {
				JsonObjectBuilder jsonMeta = Json.createObjectBuilder();
				jsonMeta.add("name", attribute.getName());
				jsonMeta.add("label", attribute.getLabel());
				jsonMeta.add("type", attribute.getInputType());
				jsonMeta.add("class", attribute.getClass().getSimpleName());
				jsonMeta.add("default", attribute.getDefaultValue().isPresent() ? attribute.getDefaultValue().get().toString() : "");
				jsonMeta.add("description", attribute.getDescription().isPresent() ? attribute.getDescription().get() : "");
				jsonMeta.add("hidden", attribute.isHidden());
				jsonMeta.add("readonly", attribute.isReadonly());
				jsonMeta.add("required", attribute.isRequired());
				if (attribute instanceof StaticMapping) {
					jsonMeta.add("items", getValueMap((StaticMapping<?>) attribute));
				} else if (attribute instanceof DynamicMapping) {
					if (((DynamicMapping)attribute).getElementFilter() != null) {
						jsonMeta.add("elementFilterType", ((DynamicMapping) attribute).getElementFilter().type);
						jsonMeta.add("elementFilterCheck", createJsonFromMap(((DynamicMapping) attribute).getElementFilter().filter.getProperties()));
					}
				}
				if (attribute instanceof Display) {
					jsonMeta.add("twoColumns", ((MessageAttribute)attribute).hasTwoColumns());
					List<Variable<?>> variableList = ((MessageAttribute)attribute).getVariableList();
					JsonArrayBuilder variableArray = Json.createArrayBuilder(); 
					for (Variable<?> variable : variableList) {
						JsonValue jsonMap = createJsonFromMap(variable.getProperties());
						variableArray.add(jsonMap);
					}
					jsonMeta.add("variables", variableArray);
				}
				jsonMeta.add("dependencies", getJsonDependencies(attribute));
				jsonMeta.add("validation", getValidation(attribute));
				jsonAttributes.add(key, jsonMeta);
			} else {
				String value = element.getAttributeValue(attribute.getName());
				if (attribute instanceof StaticMultiSelect || attribute instanceof DynamicMultiSelect) {
					jsonAttributes.add(key,jsonify(value));
				} else {
					jsonAttributes.add(key, value);
				}
			}
		}
		return jsonAttributes;
	}
	
	private static JsonArray jsonify(String value) {
		String SEP = "\\|";
		JsonArrayBuilder valList = Json.createArrayBuilder();
		Arrays.stream(value.replaceFirst("^\\[", "").replaceAll("\\]$", "").replaceAll("\\\\,", SEP).split(","))
			.map(s -> s.trim().replaceAll(SEP,",")).forEach(s -> valList.add(s));
		return valList.build();
	}

	private static JsonValue getValueMap(StaticMapping<?> attribute) {
		Map<String, ?> valueMap = attribute.getValueMap();
		JsonObjectBuilder jsonMap = Json.createObjectBuilder();
		for (Map.Entry<String, ?> entry : valueMap.entrySet()) {
			jsonMap.add("" + entry.getValue(), entry.getKey());
		}
		return jsonMap.build();
	}
	
	private static JsonValue getValidation(Attribute<?> attribute) {
		JsonArrayBuilder valList = Json.createArrayBuilder();
		for (Check check : attribute.getValidators()) {
			JsonObjectBuilder jsonValAttribute = Json.createObjectBuilder();
			for (Map.Entry<String, Object> entry : check.getProperties().entrySet()) {
				jsonValAttribute.add(entry.getKey(), ""+entry.getValue()); // todo: if Check, entry.getPropetries()
			}
			valList.add(jsonValAttribute.build());
		}
		return valList.build();
	}
	
	private static JsonValue getJsonDependencies(Attribute<?> attribute) {
		JsonArrayBuilder depList = Json.createArrayBuilder();
		for (Check check : attribute.getDependencies()) {
			JsonObjectBuilder jsonDepAttribute = Json.createObjectBuilder();
			for (Map.Entry<String, Object> entry : check.getProperties().entrySet()) {
				jsonDepAttribute.add(entry.getKey(), ""+entry.getValue()); // todo: if Check, entry.getPropetries()
			}
			depList.add(jsonDepAttribute.build());
		}
		return depList.build();
	}
	
	private static JsonArrayBuilder getJsonChildTypes(ModelElement element) {
		JsonArrayBuilder jsonChildTypes = Json.createArrayBuilder();
		// for (Class<? extends ModelElement> child : element.getChildTypes()) {
		// try {
		// String type = child.newInstance().getType();
		// jsonChildTypes.add(type);
		// } catch (InstantiationException | IllegalAccessException e) {
		// System.out.println("ERROR: " + child + " cannot be instantiated");
		// e.printStackTrace();
		// }
		// }
		for (String childType : element.getChildTypes()) {
			jsonChildTypes.add(childType);
		}
		return jsonChildTypes;
	}
	
	public static JsonValue createValidationResult(ValidationResult result) {
		JsonArrayBuilder resultList = Json.createArrayBuilder();
		for (ValidationResultEntry entry : result.getEntries()) {
			JsonObjectBuilder jsonEntry = Json.createObjectBuilder();
			jsonEntry.add("attributeLabel", entry.attributeLabel == null ? "" : entry.attributeLabel);
			jsonEntry.add("attributeName", entry.attributeName == null ? "" : entry.attributeName);
			jsonEntry.add("elementFqId", entry.elementFqId);
			jsonEntry.add("elementId", entry.elementId);
			jsonEntry.add("elementLabel", entry.elementLabel == null ? "" : entry.elementLabel);
			jsonEntry.add("elementTId", entry.elementTId);
			jsonEntry.add("elementType", entry.elementType);
			jsonEntry.add("type", entry.type);
			jsonEntry.add("validationMessage", entry.validationMessage);
			resultList.add(jsonEntry.build());
		}
		return resultList.build();
	}
	
	public static JsonValue createJsonParameterList(List<Attribute<?>> parameterList, ModelElement selectedElement) {
		JsonArrayBuilder resultList = Json.createArrayBuilder();
		for (Attribute<?> parameter : parameterList) {
			JsonObjectBuilder json = Json.createObjectBuilder();
			json.add("name", parameter.getName());
			json.add("label", parameter.getLabel());
			json.add("defaultValue", (parameter.getDefaultValue().isPresent() ? parameter.getDefaultValue().get().toString() : ""));
			json.add("type", parameter.getInputType());
			if (parameter instanceof StaticMapping) {
				json.add("codeList", getCodeList(((StaticMapping<Object>) parameter).getValueMap()));
			} else if (parameter instanceof DynamicMapping) {
				json.add("codeList", getCodeList(((DynamicMapping) parameter).getValueMap(selectedElement)));
			}
			resultList.add(json);
		}
		return resultList.build();
	}
	
	private static JsonValue getCodeList(Map<String, Object> codeMap) {
		JsonObjectBuilder json = Json.createObjectBuilder();
		for (Map.Entry<String, Object> entry : codeMap.entrySet()) {
			json.add(entry.getKey(), ""+entry.getValue());
		}
		return json.build();
	}
}
