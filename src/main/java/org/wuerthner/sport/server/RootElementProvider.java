package org.wuerthner.sport.server;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import jakarta.ejb.Stateless;
import jakarta.inject.Inject;

import org.wuerthner.sport.api.ModelElement;
import org.wuerthner.sport.core.ElementFilter;
import org.wuerthner.sport.persistence.dao.GenericDao;
import org.wuerthner.sport.persistence.dao.GenericDao.DocumentReference;

@Stateless
public class RootElementProvider {
	
	@Inject
	private GenericDao genericDao;
	
	public List<ModelElement> getElements(String rootType, ElementFilter elementFilter) {
		List<ModelElement> resultList = new ArrayList<>();
		List<DocumentReference> documents = genericDao.getDocuments(rootType);
		if (documents.isEmpty()) {
			throw new RuntimeException("No document available!");
		} else if (documents.size() > 1) {
			throw new RuntimeException("Ambiguous documents available!");
		} else {
			Long rootId = documents.get(0).ID;
			ModelElement root = genericDao.getElement(rootId);
			resultList = root.lookupByType(elementFilter.type).stream().filter(el -> elementFilter.filter.evaluate(el, null)).collect(Collectors.toList());
		}
		return resultList;
	}
}
