package org.activiti.engine.impl.cmd;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.impl.RepositoryServiceImpl;
import org.activiti.engine.impl.bpmn.helper.StringUtils;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.pvm.PvmTransition;
import org.activiti.engine.impl.pvm.process.ActivityImpl;


public class GetAfterActivtiyCmd implements Command<List<ActivityImpl>>,Serializable{

	
	private static final long serialVersionUID = 1L;
	
	
	private String taskId;
	public  GetAfterActivtiyCmd(String taskId){
		this.taskId=taskId;
	}
	
	@Override
	public List<ActivityImpl> execute(CommandContext commandContext) {
		if(StringUtils.isEmpty(taskId)){
			throw new ActivitiIllegalArgumentException("taskId is empty");
		}
		
		List<ActivityImpl> result=new ArrayList<ActivityImpl>();
		HistoricTaskInstance historicTaskInstance=commandContext.getHistoricTaskInstanceEntityManager().findHistoricTaskInstanceById(taskId);
	    String taskDefKey=	historicTaskInstance.getTaskDefinitionKey();
		
	    
	 // 获取流程定义对象
	 	RepositoryService repositoryService = commandContext.getProcessEngineConfiguration().getRepositoryService();
	 	ProcessDefinitionEntity processDefinitionEntity = (ProcessDefinitionEntity) ((RepositoryServiceImpl) repositoryService).getDeployedProcessDefinition(historicTaskInstance.getProcessDefinitionId());
	 	//ProcessDefinitionEntity processDefinitionEntity = (ProcessDefinitionEntity) ((RepositoryServiceImpl) repositoryService).getDeployedProcessDefinition("leave:1:10504");
	 	
	    ActivityImpl startActivity = processDefinitionEntity.findActivity(taskDefKey);

	 		// 广度优先遍历
	 		LinkedList<ActivityImpl> queue = new LinkedList<ActivityImpl>();// 待搜索队列
	 		List<String> searchList = new ArrayList<String>();// 保存已经走过的节点
	 		queue.offer(startActivity);// 开始节点如队列
	 		while (!queue.isEmpty()) {
	 			ActivityImpl activityImpl = queue.poll();
	 			if (searchList.contains(activityImpl.getId())) {// 已经走过，跳过
	 				continue;
	 			}

	 			//判断当前结点是否已经完成
 				/*List<HistoricTaskInstance> list=commandContext.getProcessEngineConfiguration()
 				                                .getHistoryService()
 				                                .createHistoricTaskInstanceQuery()
 				                                .processInstanceId(historicTaskInstance.getProcessInstanceId())
 				                                .taskDefinitionKey(activityImpl.getId())
 				                                .orderByHistoricTaskInstanceStartTime()
 				                                .desc()
 				                                .list();
 				Boolean choose=false;
 				if(!list.isEmpty()){
 					HistoricTaskInstance targetHis=list.get(0);
 					if(targetHis.getEndTime()!=null){//已经执行
 						choose=true;
 					}
 					
 				}*/
 				List<PvmTransition> outList=new ArrayList<PvmTransition>();
 				
 				Boolean choose=false;
	 			if(choose){
	 				outList=getValidate(activityImpl, commandContext, historicTaskInstance.getProcessInstanceId());
	 			}else{
	 				outList=getOtherOut(activityImpl, commandContext, historicTaskInstance.getProcessInstanceId());
	 			}
 				
	 			for (PvmTransition neighborPvm : outList) {
	 				ActivityImpl neighborAct = (ActivityImpl) neighborPvm.getDestination();
	 				queue.offer(neighborAct);
	 			}
	 			searchList.add(activityImpl.getId());
	 			result.add(activityImpl);
	 		}

		
		return result;
	}
	
	
	public List<PvmTransition> getValidate(ActivityImpl activityImpl, CommandContext commandContext,
			String processInstanceId) {
		List<PvmTransition> result = new ArrayList<PvmTransition>();
		for (PvmTransition neighborPvm : activityImpl.getOutgoingTransitions()) {
			ActivityImpl neighborAct = (ActivityImpl) neighborPvm.getDestination();

			List<HistoricActivityInstance> neighborActlist = commandContext.getProcessEngineConfiguration()
					.getHistoryService().createHistoricActivityInstanceQuery().processInstanceId(processInstanceId)
					.activityId(neighborAct.getId()).orderByHistoricActivityInstanceStartTime().desc().list();

			if (!neighborActlist.isEmpty()) {// 下一结点执行过，是有效结点
				result.add(neighborPvm);
			}
		}
		// for循环结束
		if (result.isEmpty()) {// 所有输出路径都没有执行，说明当前结点被退回
			result.addAll(getOtherOut(activityImpl, commandContext, processInstanceId));
		}
		return result;
	}
			
	public List<PvmTransition> getOtherOut(ActivityImpl activityImpl, CommandContext commandContext,
			String processInstanceId){
		List<PvmTransition> result = new ArrayList<PvmTransition>();
		for (PvmTransition neighborPvm : activityImpl.getOutgoingTransitions()){
			if(neighborPvm.getProperty("name")!=null){
				if(neighborPvm.getProperty("name").toString().endsWith("_bak")){
					continue;
				}
			}
			result.add(neighborPvm);
			
		}
		return result;
		
	}


}
