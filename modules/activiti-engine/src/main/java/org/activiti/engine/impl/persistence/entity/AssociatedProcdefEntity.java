package org.activiti.engine.impl.persistence.entity;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.activiti.engine.impl.db.HasRevision;
import org.activiti.engine.impl.db.PersistentObject;
import org.activiti.engine.repository.AssociatedProcdef;

public class AssociatedProcdefEntity implements AssociatedProcdef,PersistentObject,Serializable,HasRevision{

	private static final long serialVersionUID = 1L;

	private String id;

	protected int revision;
	
	private String associatedBizKey;

    private String procDefId;

    private Integer order;

    @Override
	public Object getPersistentState() {
		
		Map<String, Object> persistentState = new HashMap<String, Object>();
		persistentState.put("associatedBizKey", this.associatedBizKey);
		persistentState.put("procDefId", this.procDefId);
		persistentState.put("order", this.order);
		return persistentState;
	}
    
    
    public String getAssociatedBizKey() {
        return associatedBizKey;
    }


    public void setAssociatedBizKey(String associatedBizKey) {
        this.associatedBizKey = associatedBizKey;
    }

   
    public String getProcDefId() {
        return procDefId;
    }

    public void setProcDefId(String procDefId) {
        this.procDefId = procDefId;
    }

  
    public Integer getOrder() {
        return order;
    }

    public void setOrder(Integer order) {
        this.order = order;
    }


	@Override
	public String getId() {
		
		return this.id;
	}


	@Override
	public void setId(String id) {
		this.id=id;
		
	}

	public int getRevision() {
		return revision;
	}


	public void setRevision(int revision) {
		this.revision = revision;
	}


	@Override
	public int getRevisionNext() {
		return revision+1;
	}
	


	
}