package org.wuerthner.sport.persistence.entity;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.persistence.Version;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

@Entity
@Table(name = "GENERIC_ELEMENT")
public class GenericEntity {
	@Id
	// @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "GENERIC_SEQUENCE")
	// @SequenceGenerator(name = "GENERIC_SEQUENCE", sequenceName = "GENERIC_ELEMENT_SEQ", allocationSize = 50)
	@GeneratedValue
	@Column(name = "ID")
	private long id;
	
	@Column(name = "PARENT_ID", insertable = false, updatable = false)
	private long parentId; // relational field (read only)
	
	@ManyToOne(fetch = FetchType.LAZY) // LAZY
	@JoinColumn(name = "PARENT_ID")
	private GenericEntity parent;
	
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "parent", cascade = CascadeType.REMOVE, orphanRemoval = true) // LAZY
	@OrderBy("id")
	private List<GenericEntity> children;
	
	@OneToMany(fetch = FetchType.EAGER, mappedBy = "parent", cascade = CascadeType.REMOVE, orphanRemoval = true)
	@OrderBy("key")
	private List<AttributeEntity> attributeList;
	
	// properties
	
	@Column(name = "TYPE")
	private String type;

    @Version
    @Column(name = "VERSION")
    private long version;

    @Temporal(TemporalType.TIMESTAMP)
	@Column(name = "MODIFIED")
	private Timestamp modified;
	
	@Column(name = "MODIFIED_BY")
	private long modifiedBy;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "CREATED", updatable = false)
	private Timestamp created;
	
	@Column(name = "CREATED_BY", updatable = false)
	private long createdBy;
	
	@Column(name = "DELETED")
	private int deleted;
	
	@Column(name = "INCLIPBOARD")
	private int inClipboard;
	
	@Column(name = "GRP")
	private long group;
	
	@Column(name = "AC")
	private int access;
	
	// @Column(name = "ORDER")
	// private int order;
	
	public long getId() {
		return id;
	}
	
	public void setId(long id) {
		if (id > 0) {
			this.id = id;
		}
	}
	
	// read only
	public long getParentId() {
		return parentId;
	}
	
	public GenericEntity getParent() {
		return parent;
	}
	
	public void setParent(GenericEntity parent) {
		this.parent = parent;
	}
	
	public List<GenericEntity> getChildren() {
		return children;
	}
	
	public void setChildren(List<GenericEntity> children) {
		this.children = children;
	}
	
	public List<AttributeEntity> getAttributeList() {
		return attributeList;
	}
	
	public void setAttributeList(List<AttributeEntity> attributeList) {
		this.attributeList = attributeList;
	}
	
	public String getType() {
		return type;
	}
	
	public void setType(String type) {
		this.type = type;
	}
	
	// @SuppressFBWarnings(value = "EI_EXPOSE_REP", justification = "Required by JPA")
	public Date getModified() {
		return modified;
	}
	
	// @SuppressFBWarnings(value = "EI_EXPOSE_REP", justification = "Required by JPA")
	public void setModified(Date modified) {
		this.modified = new Timestamp(modified.getTime());
	}
	
	public long getModifiedBy() {
		return modifiedBy;
	}
	
	public void setModifiedBy(long modifiedBy) {
		this.modifiedBy = modifiedBy;
	}
	
	// @SuppressFBWarnings(value = "EI_EXPOSE_REP", justification = "Required by JPA")
	public Date getCreated() {
		return created;
	}
	
	// @SuppressFBWarnings(value = "EI_EXPOSE_REP", justification = "Required by JPA")
	public void setCreated(Date created) {
		this.created = new Timestamp(created.getTime());
	}
	
	public long getCreatedBy() {
		return createdBy;
	}
	
	public void setCreatedBy(long l) {
		this.createdBy = l;
	}
	
	public int getDeleted() {
		return deleted;
	}
	
	public void setDeleted(int deleted) {
		this.deleted = deleted;
	}
	
	public int getInClipboard() {
		return inClipboard;
	}
	
	public void setInClipboard(int inClipboard) {
		this.inClipboard = inClipboard;
	}
	
	public long getGroup() {
		return group;
	}
	
	public void setGroup(long l) {
		this.group = l;
	}
	
	public int getAccess() {
		return access;
	}
	
	public void setAccess(int access) {
		this.access = access;
	}
	
	// public int getOrder() {
	// return order;
	// }
	//
	// public void setOrder(int order) {
	// this.order = order;
	// }
	
	@Override
	public boolean equals(Object obj) {
		return EqualsBuilder.reflectionEquals(this, obj, false);
	}
	
	@Override
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this, false);
	}
	
	// @Override
	// public String toString() {
	// return ToStringBuilder.reflectionToString(this);
	// }
}
