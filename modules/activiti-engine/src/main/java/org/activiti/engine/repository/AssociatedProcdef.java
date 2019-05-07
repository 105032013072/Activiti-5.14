package org.activiti.engine.repository;

public interface AssociatedProcdef {
	String getId();

	void setId(String id);

	String getAssociatedBizKey();

	void setAssociatedBizKey(String associatedBizKey);

	String getProcDefId();

	void setProcDefId(String procDefId);

	Integer getOrder();

	void setOrder(Integer order);
}
