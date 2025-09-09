package org.wuerthner.sport.action;

import java.util.ArrayList;
import java.util.List;

import jakarta.ejb.Stateless;

import org.wuerthner.sport.api.Attribute;
import org.wuerthner.sport.api.ModelElement;
import org.wuerthner.sport.attribute.FileAttribute;

@Stateless
public class ImportWebAction implements ImportWebActionInterface {
	
	@Override
	public String getId() {
		return "import";
	}
	
	@Override
	public boolean requiresData() {
		return false;
	}
	
	@Override
	public List<Attribute<?>> getParameterList(ModelElement selectedElement) {
		List<Attribute<?>> parameterList = new ArrayList<>();
		Attribute<?> attribute = new FileAttribute(ImportAction.PARAMETER_FILE)
				.label("File");
		parameterList.add(attribute);
		return parameterList;
	}
}
