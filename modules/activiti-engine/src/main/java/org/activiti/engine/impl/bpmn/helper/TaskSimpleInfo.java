package org.activiti.engine.impl.bpmn.helper;

import java.io.Serializable;

public class TaskSimpleInfo implements Serializable{

	private static final long serialVersionUID = 1L;

	private String taskId;
	
	private String taskDefiniotinKey;
	
	private String assignee;

	public String getTaskId() {
		return taskId;
	}

	public void setTaskId(String taskId) {
		this.taskId = taskId;
	}

	public String getAssignee() {
		return assignee;
	}

	public void setAssignee(String assignee) {
		this.assignee = assignee;
	}

	public String getTaskDefiniotinKey() {
		return taskDefiniotinKey;
	}

	public void setTaskDefiniotinKey(String taskDefiniotinKey) {
		this.taskDefiniotinKey = taskDefiniotinKey;
	}

	@Override
	public String toString() {
		
		return "taskId:="+taskId;
	}
	
	
	
	
}
