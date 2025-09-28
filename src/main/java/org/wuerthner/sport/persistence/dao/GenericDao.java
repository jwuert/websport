package org.wuerthner.sport.persistence.dao;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.Collectors;

import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.json.JsonObject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;

import org.wuerthner.sport.api.ModelElement;
import org.wuerthner.sport.api.ModelElementFactory;
import org.wuerthner.sport.core.AbstractModelElement;
import org.wuerthner.sport.json.SpeedyJson;
import org.wuerthner.sport.persistence.entity.AttributeEntity;
import org.wuerthner.sport.persistence.entity.ContextEntity;
import org.wuerthner.sport.persistence.entity.GenericEntity;
import org.wuerthner.sport.util.Logger;

@Stateless // @Stateless is essential for the FetchType.LAZY of the GenericEntity to work!!!
public class GenericDao {
	private final static String DOCUMENT_QUERY = "select g.id,g.type,a.value from GenericEntity g, AttributeEntity a where g.id=g.parentId and a.parentId=g.id and a.key='id' and g.deleted=0";
	private final static String DOCUMENT_TYPE_QUERY = "select g.id,g.type,g.type from GenericEntity g where g.deleted=0 and g.inClipboard=0 and g.id=g.parentId";
	private final static String DOCUMENT_TYPE_QUERY2 = "select g from GenericEntity g where g.deleted=0 and g.inClipboard=0 and g.id=g.parentId and g.type=:type";
	private final static String DOCUMENT_TYPE_QUERY3 = "select g.id,a.value from GenericEntity g, AttributeEntity a where g.id=g.parentId and a.parentId=g.id and a.key='id' and g.deleted=0 and g.type=:type";
	private final static String DOCUMENT_TYPE_QUERY3a = "select g.id,a.value from GenericEntity g, AttributeEntity a where g.id=g.parentId and a.parentId=g.id and a.key='id' and g.deleted=0 and g.type='Study'";
	private final static String ELEMENT_TYPE_QUERY = "select g from GenericEntity g where g.deleted=0 and g.inClipboard=0 and g.id<>g.parentId and g.type=:type";
	private final static String ELEMENT_COUNT = "select count(g.id) from GenericEntity g";
	private final static String WIPE_GENERICS_QUERY = "delete from GenericEntity";
	private final static String WIPE_ATTRIBUTES_QUERY = "delete from AttributeEntity";
	private final static String WIPE_CONTEXT_QUERY = "delete from ContextEntity";
	private final static String PERMISSION_QUERY = "select p from PermissionEntity p";
	private final static String TIDY1 = "delete from GenericEntity e where e.deleted=1";
	private final static String TIDY2 = "delete from GenericEntity e where e.parentId not in (select e2.id from GenericEntity e2)";
	private final static String TIDY3 = "delete from AttributeEntity a where a.parentId not in (select e.id from GenericEntity e)";
	
	private final static Logger logger = Logger.getLogger(GenericDao.class);
	
	@Inject
	private ModelElementFactory factory;
	
	@PersistenceContext
	private EntityManager entityManager;
	
	public List<DocumentReference> getDocuments(String type) {
		List<DocumentReference> docReferences;
		//try {
			@SuppressWarnings("unchecked")
			List<Object[]> list = entityManager.createQuery(DOCUMENT_TYPE_QUERY3).setParameter("type", type).getResultList();
			docReferences = list.stream().map(array -> new DocumentReference(array))
					// TODO: .filter(docref -> (context == null ? true : accessService.hasReadAccess(this.getElement(docref.ID, context), context)))
					.collect(Collectors.toList());
			System.out.println("# of documents: " + docReferences.size());
		//} catch (Exception e) {
		//	docReferences = new ArrayList<>();
		//}
		return docReferences;
	}
	
	public ModelElement getElement(long id) {
		GenericEntity entity = entityManager.find(GenericEntity.class, id);
		if (entity == null) {
			System.out.println("entity with id=" + id + " does not exist!");
			List<GenericEntity> resultList = entityManager.createQuery("select g from GenericEntity g", GenericEntity.class).getResultList();
			for (GenericEntity e : resultList)
				System.out.println("-> " + e.getId() + ", " + e.getType() + ", " + e.getParentId());
		}
		return create2(entity, -1);
	}
	
	public Long persistTree(ModelElement element) {
		Long topLevelId = -1L;
		synchronized (element) {
			topLevelId = persistElement(element);
			for (ModelElement child : element.getChildren()) {
				persistTree((ModelElement) child);
			}
		}
		return topLevelId;
	}
	
	public Long persistElement(ModelElement element) {
		Long newId = -1L;
		if (true) { // TODO: access control: if (accessService.hasWriteAccess(element, context)) {
			synchronized (element) {
				long parentId = element.getParent().getTechnicalId();
				boolean topLevel = (element == element.getParent());
				// boolean accepted = topLevel || dao.get(parentId).get().acceptsAsChildType(element.getType());
				// if (!accepted) {
				// throw new RuntimeException("invalid parent!");
				// }
				if (parentId == 0 && !topLevel) {
					// assure that a potentially new (unpersisted) parent is stored first!
					persistElement((ModelElement) element.getParent());
				}
				// newId = dao.put(element);
				newId = put(element);
			}
		}
		return newId;
	}
	
	// public void persistAttribute(AttributeKeyValue attributeKeyValue) {
	// ModelElement element = this.getWithFullModel(attributeKeyValue.getId());
	// element.performSetAttributeValueAsStringOperation(attributeKeyValue.getKey(), attributeKeyValue.getValue());
	// this.persistElement(element);
	// }
	
	public void persistEntity(GenericEntity e) {
		entityManager.persist(e);
	}
	
	public void persistEntity(ContextEntity ce) {
		entityManager.persist(ce);
	}
	
	public ContextEntity getContext(long wrkId) {
		return entityManager.find(ContextEntity.class, wrkId);
	}
	
	public void deleteElement(ModelElement element) {
		if (true) { // TODO: access control: if (accessService.hasWriteAccess(element, context)) {
			element.setDeleted(true);
			persistElement(element);
		}
	}
	
	private ModelElement create2(GenericEntity source, int depth) {
		// depth: -1='full depth', 0='source only', 1='source and children', etc
		ModelElement target = factory.createElement(source.getType());
		// copy
		target.setType(source.getType());
		target.setTechnicalId(source.getId());
		((AbstractModelElement) target).setAttributeMap(createMap(source.getAttributeList()));
		target.setDeleted(source.getDeleted() == 1);
		target.setInClipboad(source.getInClipboard() == 1);
		// target.setGroup(source.getGroup());
		// target.setAccess(source.getAccess());
		// target.setOrder(source.getOrder());
		
		target.setTechnicalId(source.getId());
		target.setModified(new Date(source.getModified().getTime()));
		target.setModifiedBy(source.getModifiedBy());
		target.setCreated(new Date(source.getCreated().getTime()));
		target.setCreatedBy(source.getCreatedBy());
		
		// children
		if (depth != 0 && source.getChildren() != null) {
			if (source.getChildren() != null) {
				for (GenericEntity childEntity : source.getChildren()) {
					if (childEntity.getId() != source.getId()) {
						ModelElement child = create2(childEntity, depth - 1);
						((AbstractModelElement) target).addChild(child);
					}
				}
			}
		}
		return target;
	}
	
	private TreeMap<String, String> createMap(List<AttributeEntity> attributeEntityList) {
		TreeMap<String, String> map = new TreeMap<>();
		if (attributeEntityList != null) {
			for (AttributeEntity attributeEntity : attributeEntityList) {
				map.put(attributeEntity.getKey(), attributeEntity.getValue());
			}
		}
		return map;
	}
	
	public void flush() {
		entityManager.flush();
	}
	
	public class DocumentReference {
		public final Long ID;
		public final String NAME;
		
		public DocumentReference(Object[] array) {
			this.ID = (Long) (array[0] == null ? -1L : array[0]);
			this.NAME = (String) (array[1] == null ? "" : array[1]);
		}
	}
	
	public void wipe() {
		System.out.println("====================================");
		System.out.println("########## WIPE! ###################");
		System.out.println("====================================");
		Query q1 = entityManager.createQuery(WIPE_CONTEXT_QUERY);
		Query q2 = entityManager.createQuery(WIPE_ATTRIBUTES_QUERY);
		Query q3 = entityManager.createQuery(WIPE_GENERICS_QUERY);
		
		q1.executeUpdate();
		q2.executeUpdate();
		q3.executeUpdate();
		
		entityManager.flush();
	}
	
	public void tidy() {
		System.out.println("====================================");
		System.out.println("########## TIDY! ###################");
		System.out.println("====================================");
		// Query q1 = entityManager.createQuery(TIDY1);
		// Query q2 = entityManager.createQuery(TIDY2);
		// Query q3 = entityManager.createQuery(TIDY3);
		//
		// q1.executeUpdate();
		// q2.executeUpdate();
		// q3.executeUpdate();
		boolean done = false;
		while (!done) {
			List<Integer> idList = entityManager.createQuery("select g.id from GenericEntity g where g.deleted=0", Integer.class).getResultList();
			int numberOfZombieGenerics = entityManager.createQuery("delete from GenericEntity g1 where g1.parentId not in (:idList)").setParameter("idList", idList).executeUpdate();
			List<Integer> updatedIdList = entityManager.createQuery("select g.id from GenericEntity g", Integer.class).getResultList();
			int numberOfZombieAttributes = entityManager.createQuery("delete from AttributeEntity a where a.parentId not in (:updateIdList)").setParameter("updateIdList", updatedIdList).executeUpdate();
			System.out.println("=> " + numberOfZombieGenerics + ", " + numberOfZombieAttributes);
			if (numberOfZombieGenerics == 0 && numberOfZombieAttributes == 0) {
				done = true;
			}
		}
		
		entityManager.flush();
	}
	
	//
	// PERSIST
	//
	
	private Long put(ModelElement element) {
		if (element == null) {
			throw new RuntimeException("Element must not be null");
		}
		GenericEntity entity = null;
		long id = element.getTechnicalId();
		if (id > 0) {
			entity = entityManager.find(GenericEntity.class, element.getTechnicalId());
		}
		// modifiy entity (create, update or delete)
		if (element.isDeleted()) {
			if (entity != null) {
				updateEntity(entity, element); // we don't really delete it - because of UNDO!
			}
		} else {
			if (entity != null) {
				updateEntity(entity, element);
			} else {
				entity = createAndPersistEntity(element);
			}
		}
		// write changes to database and return changed timestamps/users to the client
		flush();
		if (entity != null) {
			id = entity.getId();
			updateElement(element, entity);
			return id;
		} else {
			return null;
		}
	}
	
	private void updateEntity(GenericEntity target, ModelElement source) {
		checkForConcurrentModification(target, source);
		long userId = 4711; // TODO: context.getUserId();
		// User user = entityManager.find(User.class, userId);
		// entity.setModifiedBy(user.getId());
		copyDataFieldsToEntity(target, source, userId);
	}
	
	private void copyDataFieldsToEntity(GenericEntity target, ModelElement source, long userId) {
		// System.out.println(target.getType() + ": " + target.getId() + ", " + target.getParent() + ", " + target.getParentId());
		// System.out.println(" " + source.getType() + ": " + source.getTechnicalId() + ", " + source.getParent().getTechnicalId() + ", " + source.getParent() + ", " + (source==source.getParent()));
		GenericEntity parent;
		if ((source == source.getParent())) {
			parent = target;
		} else {
			parent = entityManager.find(GenericEntity.class, source.getParent().getTechnicalId());
		}
		target.setType(source.getType());
		target.setId(source.getTechnicalId());
		target.setParent(parent);
		target.setAttributeList(updateElementEntityList(target, ((AbstractModelElement) source).getAttributeMap(), source, userId));
		target.setDeleted(source.isDeleted() ? 1 : 0);
		target.setInClipboard(source.isInClipboard() ? 1 : 0);
		target.setGroup(0); // source.getGroup());
		target.setAccess(0); // source.getAccess());
		// target.setOrder(0); // source.getOrder());
	}
	
	private void checkForConcurrentModification(GenericEntity entity, ModelElement element) {
		Date readTime = element.getModified();
		Date lastWriteTime = new Date(entity.getModified().getTime());
		if (readTime == null) {
			throw new RuntimeException("Missing 'modified' value in instance of " + ModelElement.class.getName());
		}
		if (!lastWriteTime.equals(readTime)) {
			throw new RuntimeException(
					"Instance of " + ModelElement.class.getName() + " (id " + element.getId() + ") was concurrently modified (worker id " + entity.getModifiedBy() + ") [" + lastWriteTime + ":" + readTime + "]");
		}
	}
	
	private List<AttributeEntity> updateElementEntityList(GenericEntity elementEntity, Map<String, String> origMap, ModelElement source, long userId) {
		if (origMap == null) {
			origMap = new HashMap<>();
		}
		
		Map<String, String> attributeMap = new HashMap<>(origMap);
		
		// User user = entityManager.find(User.class, userId);
		
		List<AttributeEntity> attributeEntityList = elementEntity.getAttributeList();
		if (attributeEntityList == null) {
			attributeEntityList = new ArrayList<>();
		}
		boolean modified = false;
		
		// delete or update elements
		Iterator<AttributeEntity> attributeEntityIterator = attributeEntityList.iterator();
		while (attributeEntityIterator.hasNext()) {
			AttributeEntity attributeEntity = attributeEntityIterator.next();
			String key = attributeEntity.getKey();
			String value = attributeMap.remove(key);
			if (value == null) {
				attributeEntityIterator.remove();
				attributeEntity.setParent(null);
				entityManager.remove(attributeEntity);
				modified = true;
			} else if (!value.equals(attributeEntity.getValue())) {
				attributeEntity.setValue(value);
				// elementEntity.setModifiedBy(user.getId());
				modified = true;
			}
		}
		
		// ignore new elements with null values
		Iterator<Map.Entry<String, String>> attributeMapIterator = attributeMap.entrySet().iterator();
		while (attributeMapIterator.hasNext()) {
			Map.Entry<String, String> entry = attributeMapIterator.next();
			if (entry.getValue() == null) {
				attributeMapIterator.remove();
			}
		}
		
		// add new elements
		for (Map.Entry<String, String> entry : attributeMap.entrySet()) {
			AttributeEntity attributeEntity = new AttributeEntity();
			attributeEntity.setKey(entry.getKey());
			attributeEntity.setValue(entry.getValue());
			// elementEntity.setCreatedBy(user.getId());
			
			attributeEntity.setCreated(new Date());
			// elementEntity.setModifiedBy(user.getId());
			attributeEntityList.add(attributeEntity);
			attributeEntity.setParent(elementEntity);
			entityManager.persist(attributeEntity);
			modified = true;
		}
		
		if (modified) {
			entityManager.lock(elementEntity, LockModeType.WRITE); // force update of modified field in map entity
			// mapEntity.setModifiedBy(user.getId());
		}
		return attributeEntityList;
	}
	
	private GenericEntity createAndPersistEntity(ModelElement genericElement) {
		long userId = 4711; // TODO!
		GenericEntity entity;
		try {
			entity = GenericEntity.class.newInstance();
		} catch (InstantiationException | IllegalAccessException exc) {
			throw new RuntimeException("Failed to create instance of " + GenericEntity.class.getName(), exc);
		}
		entity.setId(genericElement.getTechnicalId());
		entityManager.persist(entity);
		
		genericElement.setModified(entity.getModified());
		// fill data fields after persisting, because this might create child entities
		
		entity.setCreatedBy(userId);
		entity.setCreated(new Date());
		entity.setModifiedBy(userId);
		entity.setGroup(0);
		// entity.setAccess(Access.DEFAULT_ACCESS);
		copyDataFieldsToEntity(entity, genericElement, userId);
		
		if (genericElement.getTechnicalId() == 0) {
			genericElement.setTechnicalId(entity.getId());
		}
		return entity;
	}
	
	private void updateElement(ModelElement element, GenericEntity entity) {
		element.setTechnicalId(entity.getId());
		element.setModified(new Date(entity.getModified().getTime()));
		element.setModifiedBy(entity.getModifiedBy());
		element.setCreated(new Date(entity.getCreated().getTime()));
		element.setCreatedBy(entity.getCreatedBy());
	}
	
	//
	// some methods to support stateless api
	//
	
	public List<JsonObject> getElements(String type, String documentId) {
		//List<GenericEntity> resultList = entityManager.createQuery("select g from GenericEntity g", GenericEntity.class).getResultList();
		Long rootId = (Long) entityManager.createQuery("select a.parentId from AttributeEntity a where a.key='id' and a.value=:documentId").setParameter("documentId", documentId).getSingleResult();
		List<GenericEntity> resultList = entityManager.createQuery("select g from GenericEntity g where g.parentId=:parentId and g.type=:type")
			.setParameter("parentId", rootId)
			.setParameter("type", type)
			.getResultList();
		List<JsonObject> modelElementList = new ArrayList<>();
		for (GenericEntity e : resultList) {
			ModelElement me = create2(e, 0);
			modelElementList.add(SpeedyJson.create(me));
		}
		return modelElementList;
	}

	public Optional<ModelElement> getElement(String elementType, String elementId) {
		List<GenericEntity> resultList = entityManager.createQuery("select g from GenericEntity g, AttributeEntity a where a.parentId=g.id and a.key='id' and g.deleted=0 and g.type=:type and a.value=:id")
			.setParameter("type", elementType)
			.setParameter("id", elementId)
			.getResultList();
		if (resultList.size() != 1) {
			logger.info("No specific element id='"+elementId+"' of type '"+elementType+"' (size: " + resultList.size() + ")");
			return Optional.empty();
		}
		ModelElement me = create2(resultList.get(0), 0);
		return Optional.of(me);
	}
	
	public Optional<ModelElement> getElement(String elementType, String attributeName, String attributeValue) {
		List<GenericEntity> resultList = entityManager.createQuery("select g from GenericEntity g, AttributeEntity a where a.parentId=g.id and a.key=:attribute and g.deleted=0 and g.type=:type and a.value=:value")
			.setParameter("type", elementType)
			.setParameter("attribute", attributeName)
			.setParameter("value", attributeValue)
			.getResultList();
		if (resultList.size() != 1) {
			logger.info("No specific element "+attributeName+"='"+attributeValue+"' of type '"+elementType+"' (size: " + resultList.size() + ")");
			return Optional.empty();
		}
		ModelElement me = create2(resultList.get(0), 0);
		return Optional.of(me);
	}
	
	public void changeAttribute(String elementType, String elementId, String attributeId, String value) {
		List<AttributeEntity> resultList = entityManager.createQuery(
				"select a from GenericEntity g, AttributeEntity a1, AttributeEntity a where a1.parentId=g.id and a.parentId=g.id and a1.key='id' and g.deleted=0 and g.type=:type and a1.value=:id and a.key=:attribute")
				.setParameter("type", elementType)
				.setParameter("id", elementId)
				.setParameter("attribute", attributeId)
				.getResultList();
		if (resultList.size() != 1) {
			throw new RuntimeException("No specific attribute id='"+attributeId+"' for element id='"+elementId+"' of type '"+elementType+"' (size: " + resultList.size() + ")");
		}
		AttributeEntity attribute = resultList.get(0);
		attribute.setValue(value);
		entityManager.persist(attribute);
	}

    public String getUserName(String username) {
        List<String> list = entityManager.createNativeQuery("select concat(concat(u.fname, ' '), u.lname) from USER u where u.username=?1")
                .setParameter(1, username)
                .getResultList();
        String result = (list.isEmpty() ? "unknown username" : list.size()>1 ? "ambiguous username" : list.get(0));
        return result;
    }

    public Map<String,String> getUserMap() {
        List<String> list = entityManager.createNativeQuery("select concat(concat(concat(concat(u.username, '|'), u.fname), ' '), u.lname) from USER u")
                .getResultList();
        Map<String,String> userMap = new HashMap<>();
        for (String entry : list) {
            String[] pair = entry.split("\\|");
            userMap.put(pair[0], pair[1]);
        }
        return userMap;
    }

    public String getUserIdByUUID(String uuid) {
        List<String> list = entityManager.createNativeQuery("select u.username from USER u where u.id=?1")
                .setParameter(1, uuid)
                .getResultList();
        String result = (list.isEmpty() ? "unknown id" : list.size()>1 ? "ambiguous id" : list.get(0));
        return result;
    }
}
