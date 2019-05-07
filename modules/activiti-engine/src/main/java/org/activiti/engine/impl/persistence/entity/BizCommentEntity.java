package org.activiti.engine.impl.persistence.entity;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.activiti.engine.impl.db.HasRevision;
import org.activiti.engine.impl.db.PersistentObject;
import org.activiti.engine.repository.BizComment;


public class BizCommentEntity implements BizComment,PersistentObject,Serializable,HasRevision{

	private static final long serialVersionUID = 1L;

	private String id;

	protected int revision;

	private String associatedBizKey;

	private String associatedInstId;

	private String procInstId;

	private String procInstName;

	private String procDefId;

	private String businessKey;

	private String actId;

	private String actName;

	private String actType;

	private String taskId;
	
	private String userId;
	
	private String groups;
	
	private String advice;

	private Date completeTime;

	private String message;

	private boolean startAct=false;

	private Integer order;
    
    
    @Override
	public Object getPersistentState() {
		
    	 Map<String, Object> persistentState = new HashMap<String, Object>();
    	 persistentState.put("procInstId", this.procInstId);
    	 persistentState.put("procInstName", this.procInstName);
    	 persistentState.put("procDefId", this.procDefId);
    	 persistentState.put("businessKey", this.businessKey);
    	 persistentState.put("taskId", this.taskId);
    	 persistentState.put("userId", this.userId);
    	 persistentState.put("groups", this.groups);
    	 persistentState.put("advice", this.advice);
    	 persistentState.put("completeTime", this.completeTime);
    	 persistentState.put("message", this.message);
    	 
		return persistentState;
	}
    

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAssociatedBizKey() {
        return associatedBizKey;
    }

    public void setAssociatedBizKey(String associatedBizKey) {
        this.associatedBizKey = associatedBizKey;
    }

   
    public String getAssociatedInstId() {
        return associatedInstId;
    }

    public void setAssociatedInstId(String associatedInstId) {
        this.associatedInstId = associatedInstId;
    }


    public String getProcInstId() {
        return procInstId;
    }

    public void setProcInstId(String procInstId) {
        this.procInstId = procInstId;
    }

   
    public String getProcInstName() {
        return procInstName;
    }

 
    public void setProcInstName(String procInstName) {
        this.procInstName = procInstName;
    }

    public String getProcDefId() {
        return procDefId;
    }

    public void setProcDefId(String procDefId) {
        this.procDefId = procDefId;
    }

    public String getBusinessKey() {
        return businessKey;
    }

    public void setBusinessKey(String businessKey) {
        this.businessKey = businessKey;
    }

    
    public String getActId() {
        return actId;
    }

    public void setActId(String actId) {
        this.actId = actId;
    }

   
    public String getActName() {
        return actName;
    }


    public void setActName(String actName) {
        this.actName = actName;
    }

   
    public String getActType() {
        return actType;
    }

 
    public void setActType(String actType) {
        this.actType = actType;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public Date getCompleteTime() {
        return completeTime;
    }

    public void setCompleteTime(Date completeTime) {
        this.completeTime = completeTime;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    

    public boolean isStartAct() {
		return startAct;
	}

	public void setStartAct(boolean startAct) {
		this.startAct = startAct;
	}

	
    public Integer getOrder() {
        return order;
    }

    public void setOrder(Integer order) {
        this.order = order;
    }

	@Override
	public void setRevision(int revision) {
		this.revision=revision;
		
	}

	@Override
	public int getRevision() {
		
		return this.revision;
	}

	@Override
	public int getRevisionNext() {
		
		return revision+1;
	}


	public String getUserId() {
		return userId;
	}


	public void setUserId(String userId) {
		this.userId = userId;
	}


	public String getGroups() {
		return groups;
	}


	public void setGroups(String groups) {
		this.groups = groups;
	}


	public String getAdvice() {
		return advice;
	}


	public void setAdvice(String advice) {
		this.advice = advice;
	}


	@Override
	public String toString() {
		
		return "ID="+id+",actID="+actId;
		//return actId;
	}
	
	

	
}