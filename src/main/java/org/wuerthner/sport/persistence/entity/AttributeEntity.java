package org.wuerthner.sport.persistence.entity;

import java.sql.Timestamp;
import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.persistence.Version;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

@Entity
@Table(name = "GENERIC_ATTRIBUTE")
public class AttributeEntity {
	
	@Id
	// @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ATTRIBUTE_SEQUENCE")
	// @SequenceGenerator(name = "ATTRIBUTE_SEQUENCE", sequenceName = "GENERIC_ATTR_SEQ", allocationSize = 50)
	@GeneratedValue
	@Column(name = "ID")
	private long id;
	
	@Column(name = "PARENT_ID", insertable = false, updatable = false)
	private long parentId; // relational field (read only)
	
	@ManyToOne(fetch = FetchType.EAGER) // LAZY
	@JoinColumn(name = "PARENT_ID")
	private GenericEntity parent;
	
	@Column(name = "KEY0")
	private String key;
	
	@Column(name = "VALUE")
	private String value;
	
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
	
	//
	// GETTERS/SETTERS
	//
	
	public long getId() {
		return id;
	}
	
	public void setId(long id) {
		if (id >= 0) {
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
	
	public String getKey() {
		return key;
	}
	
	public void setKey(String key) {
		this.key = key;
	}
	
	public String getValue() {
		return value;
	}
	
	public void setValue(String value) {
		this.value = value;
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
	public Timestamp getCreated() {
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
	
	// public String toString() {
	// return "Attribute " + key;
	// }
}
