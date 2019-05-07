package org.activiti.engine.impl.cmd;

import java.io.Serializable;
import java.util.List;

import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.impl.bpmn.helper.StringUtils;
import org.activiti.engine.impl.db.PersistentObject;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.repository.BizComment;

public class DeleteBizCommentCmd implements Command<Void>,Serializable{

	private String taskId;
	
	public  DeleteBizCommentCmd(String taskId){
		this.taskId=taskId;
	}
	
	
	@Override
	public Void execute(CommandContext commandContext) {
		if(StringUtils.isEmpty(taskId)){
			throw new ActivitiIllegalArgumentException("taskId is empty");
		}
		
		List<BizComment> list=	commandContext.getBizCommentEntityManager().findBizCommentByTaskId(taskId);
		for (BizComment bizComment : list) {//只删除第一条
			if(bizComment.getCompleteTime()==null){
				commandContext.getBizCommentEntityManager().delete((PersistentObject) bizComment);
				return null;
			}
		}
		
		return null;
	}

}
