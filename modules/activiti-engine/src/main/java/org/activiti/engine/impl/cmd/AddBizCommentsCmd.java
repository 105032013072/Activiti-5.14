package org.activiti.engine.impl.cmd;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.impl.RepositoryServiceImpl;
import org.activiti.engine.impl.bpmn.helper.BizCommentRecorder;
import org.activiti.engine.impl.bpmn.helper.StringUtils;
import org.activiti.engine.impl.db.PersistentObject;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.BizCommentEntity;
import org.activiti.engine.impl.persistence.entity.HistoricTaskInstanceEntity;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.repository.BizComment;

public class AddBizCommentsCmd implements Command<Void>,Serializable{

	private String taskId;
	
	private String msg;
	
	private String userId;
	
	private String advice;
	
	private HistoricTaskInstanceEntity  historicTaskInstanceEntity;
	
	public AddBizCommentsCmd(String taskId,String msg,String userId,String advice){
		this.taskId=taskId;
		this.msg=msg;
		this.userId=userId;
		this.advice=advice;
	}
	
	/*public AddBizCommentsCmd(HistoricTaskInstanceEntity  historicTaskInstanceEntity){
		this.historicTaskInstanceEntity=historicTaskInstanceEntity;
	}*/
	
	@Override
	public Void execute(CommandContext commandContext) {
		System.out.println("AddBizCommentsCmd: "+taskId);
		if(StringUtils.isEmpty(taskId)){
			throw new ActivitiIllegalArgumentException("taskId is empty");
		}
		
		HistoricTaskInstanceEntity  historicTaskInstanceEntity=commandContext.getHistoricTaskInstanceEntityManager().findHistoricTaskInstanceById(taskId);
		if(historicTaskInstanceEntity==null){
			System.out.println("historicTaskInstanceEntity is null");
			return null;
		}
		
		RepositoryService repositoryService = commandContext.getProcessEngineConfiguration().getRepositoryService();
		ProcessDefinitionEntity processDefinitionEntity = (ProcessDefinitionEntity) ((RepositoryServiceImpl) repositoryService).getDeployedProcessDefinition(historicTaskInstanceEntity.getProcessDefinitionId());
		ActivityImpl activityImpl=processDefinitionEntity.findActivity(historicTaskInstanceEntity.getTaskDefinitionKey());
		
		
		if(activityImpl.getProperty("name")!=null){
		
			if(activityImpl.getProperty("name").toString().endsWith("_start")){
				List<BizCommentEntity> list=	commandContext.getBizCommentEntityManager().findBizCommentByProcInstIdAndActId(historicTaskInstanceEntity.getProcessInstanceId(), historicTaskInstanceEntity.getTaskDefinitionKey());
				System.out.println("find list: "+list);
				if(isStartProcess(list)){
					System.out.println("isStartProcess=======");
					BizCommentEntity bizCommentEntity=list.get(0);
					System.out.println("getbizCommentEntity"+bizCommentEntity.getId());
					bizCommentEntity.setTaskId(taskId);
					bizCommentEntity.setMessage(msg);
					bizCommentEntity.setAdvice(advice);
					bizCommentEntity.setUserId(userId);
					bizCommentEntity.setCompleteTime(new Date());
					
					//需要再新创建一个
					BizCommentEntity newEntity=BizCommentRecorder.copyBizEntity(list.get(0));
					newEntity.setTaskId(taskId);
					//newEntity.setMessage(msg);
					//newEntity.setAdvice(advice);
					//newEntity.setUserId(userId);
					//newEntity.setCompleteTime(new Date());
					commandContext.getBizCommentEntityManager().insert(newEntity);
					return null;
				}
			}
		}
		
		
		
		System.out.println("not to isStartProcess chooese");
		List<BizCommentEntity> list=commandContext.getBizCommentEntityManager().findBizCommentByProcInstIdAndActId(historicTaskInstanceEntity.getProcessInstanceId(), historicTaskInstanceEntity.getTaskDefinitionKey());
		System.out.println(list);
		for (BizCommentEntity bizCommentEntity : list) {
			/*if(StringUtils.isEmpty(bizCommentEntity.getMessage())){//操作信息为空，直接更新信息
				bizCommentEntity.setMessage(msg);
				bizCommentEntity.setAdvice(advice);
				bizCommentEntity.setUserId(userId);
				bizCommentEntity.setCompleteTime(new Date());
				
				return null;
			}*/
			
			//if(taskId.equals(bizCommentEntity.getTaskId()) && StringUtils.isEmpty(bizCommentEntity.getMessage())){
			if(taskId.equals(bizCommentEntity.getTaskId()) && bizCommentEntity.getCompleteTime()==null){
				bizCommentEntity.setMessage(msg);
				bizCommentEntity.setAdvice(advice);
				bizCommentEntity.setUserId(userId);
				bizCommentEntity.setCompleteTime(new Date());
				return null;
			}
		}
		
		//程序能够执行到这说明list中的元素都已经更新了，需要新增
		if(list.size()>0){
			//根据集合的第一个元素拷贝
			BizCommentEntity newEntity=BizCommentRecorder.copyBizEntity(list.get(0));
			newEntity.setTaskId(taskId);
			newEntity.setMessage(msg);
			newEntity.setAdvice(advice);
			newEntity.setUserId(userId);
			newEntity.setCompleteTime(new Date());
			commandContext.getBizCommentEntityManager().insert(newEntity);
		}

		return null;
	}
	
	public boolean isStartProcess(List<BizCommentEntity> list){
	    System.out.println("list size: "+list.size());
		if(list.size()==1){
	    	BizCommentEntity bizCommentEntity=list.get(0);
	    	System.out.println(bizCommentEntity.getId()+"  message: "+bizCommentEntity.getMessage());
	    	if(StringUtils.isEmpty(bizCommentEntity.getMessage())){
	    		return true;
	    	}else{
	    		return false;
	    	}
	    }else{
	    	return false;
	    }
		
	}

}
