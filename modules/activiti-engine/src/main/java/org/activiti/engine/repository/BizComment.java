package org.activiti.engine.repository;

import java.util.Date;

public interface BizComment {

	String getId();

	void setId(String id);

	String getAssociatedBizKey();

	void setAssociatedBizKey(String associatedBizKey);

	String getAssociatedInstId();

	void setAssociatedInstId(String associatedInstId);

	String getProcInstId();

	void setProcInstId(String procInstId);

	String getProcInstName();

	void setProcInstName(String procInstName);

	String getProcDefId();

	void setProcDefId(String procDefId);

	String getBusinessKey();

	void setBusinessKey(String businessKey);

	String getActId();

	void setActId(String actId);

	String getActName();

	void setActName(String actName);

	String getActType();

	void setActType(String actType);

	String getTaskId();

	void setTaskId(String taskId);
	
	 String getUserId() ;


	 void setUserId(String userId) ;

	Date getCompleteTime();

	void setCompleteTime(Date completeTime);

	String getMessage();

	void setMessage(String message);

	boolean isStartAct();

	void setStartAct(boolean startAct);

	Integer getOrder();

	void setOrder(Integer order);
	
	 String getGroups();

	 void setGroups(String groups) ;

	 String getAdvice() ;
	 
	 void setAdvice(String advice) ;
}
