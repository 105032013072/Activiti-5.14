package org.activiti.engine.impl.bpmn.helper;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.activiti.engine.ActivityImplConstants;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.delegate.Expression;
import org.activiti.engine.impl.RepositoryServiceImpl;
import org.activiti.engine.impl.bpmn.behavior.MultiInstanceActivityBehavior;
import org.activiti.engine.impl.bpmn.behavior.UserTaskActivityBehavior;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.cmd.CommandContextHelperCmd;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.AssociatedProcdefEntity;
import org.activiti.engine.impl.persistence.entity.BizCommentEntity;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.pvm.PvmTransition;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.impl.task.TaskDefinition;
import org.activiti.engine.impl.util.thread.ExecutorTemplate;
import org.activiti.engine.repository.AssociatedProcdef;
import org.activiti.engine.runtime.ProcessInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 用于新增或者更新BusinessComment
 * 
 * @author huangxw
 *
 */
public class BizCommentRecorder {

	//private ProcessEngineConfigurationImpl processEngineConfigurationImpl;

	private CommandContext commandContext;
	

	private List<TaskSimpleInfo> taskList=new ArrayList<TaskSimpleInfo>();
	
	private static Logger log = LoggerFactory.getLogger(BizCommentRecorder.class);

	public BizCommentRecorder(CommandContext commandContext) {
		this.commandContext=commandContext;
	}
	
	public BizCommentRecorder(CommandContext commandContext,List<TaskSimpleInfo> taskList) {
		this.commandContext=commandContext;
		if(taskList!=null){
			this.taskList=taskList;
		}
		
	}
	

	
	

	/**
	 * 启动流程时创建批注或者更新批注
	 * 
	 * @param associatedBizKey
	 * @param processInstance
	 * @param previousProcessInstId
	 */
	public void createOrUpdateComments(final String associatedBizKey, final ProcessInstance processInstance, final String previousProcessInstId,final String processStarter) {
		try{
			

			/*if (StringUtils.isEmpty(associatedBizKey) && StringUtils.isEmpty(previousProcessInstId)) {
				createFullComments(associatedBizKey, processInstance, commandContext);
			} else if (StringUtils.isNotEmpty(previousProcessInstId)) {// 不是该业务下的第一个流程
				updateProcInstInfoForComments(processInstance, previousProcessInstId, commandContext);
			} else if (StringUtils.isNotEmpty(associatedBizKey)) {
				// 判断是否是该业务下的第一个流程

				Integer order = commandContext.getAssociatedProcdefEntityManager().findProcessDefinitionOrder(associatedBizKey, processInstance.getProcessDefinitionId());

				if (order == null) {
					log.error("can't create FullBusinessComments  because can't find business ProcessDefinition by associatedBizKey=" + associatedBizKey + " and processDefinitionId= "
							+ processInstance.getProcessDefinitionId());
				} else if (order != 1) {
					log.error("can't create FullBusinessComments  because businessProcdef.order!=1 and previousProcessInstId is null");
				} else {
					createFullComments(associatedBizKey, processInstance, commandContext);
				}
			}*/
			
			
			if (StringUtils.isEmpty(associatedBizKey) && StringUtils.isEmpty(previousProcessInstId)) {
				createFullComments(associatedBizKey, processInstance, commandContext,processStarter);
			} else if (StringUtils.isNotEmpty(associatedBizKey)) {
				//查询流程关联表，是否是第一个流程
				Integer order =commandContext.getAssociatedProcdefEntityManager().findProcessDefinitionOrder(associatedBizKey, processInstance.getProcessDefinitionId());
				if(order==null){//associatedBizKey无效
					log.warn("can.t find order by associatedBizKey="+associatedBizKey);
					updateProcInstInfoForComments(processInstance, previousProcessInstId, commandContext);
				}else if(order==1){
					createFullComments(associatedBizKey, processInstance, commandContext,processStarter);
				}else{//非第一个流程
					updateProcInstInfoForComments(processInstance, previousProcessInstId, commandContext);
				}
			} else  {
				updateProcInstInfoForComments(processInstance, previousProcessInstId, commandContext);
			}

			
		}catch(Exception e){
			System.out.println(e.toString());
		}
	}

	/**
	 * 创建全量的日志
	 * 
	 * @param associatedBizKey
	 * @param processInstance
	 */
	private void createFullComments(String associatedBizKey, ProcessInstance processInstance, CommandContext commandContext,String processStarter) throws Exception {
		// 获取associatedBizKey 下的流程定义
		List<AssociatedProcdef> procDefList = new ArrayList<AssociatedProcdef>();
		if (StringUtils.isNotEmpty(associatedBizKey)) {
			procDefList = commandContext.getAssociatedProcdefEntityManager().findAssociatedProcdefByBizKey(associatedBizKey);
		}
		if (procDefList==null||procDefList.size()==0) {
			AssociatedProcdef businessProcdefEntity = new AssociatedProcdefEntity();
			businessProcdefEntity.setOrder(1);
			businessProcdefEntity.setProcDefId(processInstance.getProcessDefinitionId());
			procDefList.add(businessProcdefEntity);
		}

		// 解析流程定义，获取全量的批注实体
		String associatedInstId = UUID.randomUUID().toString().replace("-", "").toLowerCase();
		List<String> allActList=new ArrayList<String>();
		
		Map<String,List<TaskSimpleInfo>> taskMap= parserListToMap();
		for (AssociatedProcdef businessProcdef : procDefList) {
			if (businessProcdef.getOrder() == 1) {
				parserBusinessComment(associatedBizKey, associatedInstId, businessProcdef.getProcDefId(), commandContext, businessProcdef.getOrder() == 1, processInstance,allActList,taskMap,processStarter);
			} else {
				parserBusinessComment(associatedBizKey, associatedInstId, businessProcdef.getProcDefId(), commandContext, businessProcdef.getOrder() == 1, null,allActList,taskMap,processStarter);
			}

		}
	}
  private Map<String,List<TaskSimpleInfo>> parserListToMap(){
	  Map<String,List<TaskSimpleInfo>> taskMap=new HashMap<String, List<TaskSimpleInfo>>();
	  
	  for (TaskSimpleInfo taskSimpleInfo : taskList) {
		if(taskSimpleInfo!=null){
			List<TaskSimpleInfo> mapValue=taskMap.get(taskSimpleInfo.getTaskDefiniotinKey());
			if(mapValue==null){
				mapValue=new ArrayList<TaskSimpleInfo>();
				taskMap.put(taskSimpleInfo.getTaskDefiniotinKey(), mapValue);
			}
			mapValue.add(taskSimpleInfo);
			
		}
	}
	  
	  return taskMap;
  }
	
	
	/**
	 * 更新批注的流程实例信息
	 * 
	 * @param processInstance
	 * @param lastProcessInstId
	 *            上一个流程实例的Id
	 */
	private void updateProcInstInfoForComments(ProcessInstance processInstance, String previousProcessInstId, CommandContext commandContext) throws Exception {
		
		if(StringUtils.isEmpty(previousProcessInstId)){
			log.error("updateProcInstInfoForComments error ,previousProcessInstId is empty");
			return ;
		}
		// 获取业务实例Id
		String associatedInstId = commandContext.getBizCommentEntityManager().findAssociatedInstIdByProcessInstId(previousProcessInstId);
		
		if (StringUtils.isNotEmpty(associatedInstId)) {
			List<BizCommentEntity> list = commandContext.getBizCommentEntityManager().findBizCommentByAssociatedInstIdAndProcDefId(associatedInstId, processInstance.getProcessDefinitionId());
			Map<String,List<TaskSimpleInfo>> taskMap= parserListToMap();
			
			for (BizCommentEntity bizCommentEntity : list) {
				bizCommentEntity.setBusinessKey(processInstance.getBusinessKey());
				bizCommentEntity.setProcInstId(processInstance.getId());
				if (bizCommentEntity.isStartAct()) {
					bizCommentEntity.setCompleteTime(new Date());
				}
				
				//对于产生的用户任务需要更新taskId到Comment中
				setTaskInfo(bizCommentEntity,taskMap,commandContext);
			}
		}

	}
	
	/**
	 * 任务创建时更新任务信息或者新增操作日志
	 * @param processInstId
	 * @param taskId
	 * @param taskDefinitonKey
	 */
	public void updateOrInsertTaskComment(final String processInstId,final String taskId,final String taskDefinitonKey,final String assignee){
		try{
			//根据任务定义和流程实例获取日志
			List<BizCommentEntity> list=commandContext.getBizCommentEntityManager().findBizCommentByProcInstIdAndActId(processInstId, taskDefinitonKey);
			for (BizCommentEntity bizCommentEntity : list) {
				if(StringUtils.isEmpty(bizCommentEntity.getTaskId())){//直接更新信息
					bizCommentEntity.setTaskId(taskId);
					/*if(StringUtils.isNotEmpty(assignee)){
						bizCommentEntity.setUserId(assignee);
					}*/
					
					return ;
				}
				
				if(bizCommentEntity.getCompleteTime()==null){
					bizCommentEntity.setTaskId(taskId);
					return ;
				}
			}
			
			//程序能够执行到这说明list中的元素都已经更新了，需要新增
			if(list.size()>0){
				//根据集合的第一个元素拷贝
				BizCommentEntity newEntity=copyBizEntity(list.get(0));
				newEntity.setTaskId(taskId);
				/*if(StringUtils.isNotEmpty(assignee)){
					newEntity.setUserId(assignee);
				}*/
				
				commandContext.getBizCommentEntityManager().insert(newEntity);
			}
			
		}catch(Exception e){
			System.out.println(e.toString());
		}
	}
	
	
	/**
	 * 更改批注信息
	 */
	/*public void updateComments(final String processInstId, final String activityId) {
		executorTemplate.submit(new Runnable() {
			@Override
			public void run() {
				CommandContext commandContext = createContext();
				try{
					// TODO 需要考虑多实例
					
					// 根据流程实例ID和节点ID获取对应的批注实体
					BizCommentEntity bizCommentEntity = commandContext.getBizCommentEntityManager().findBizCommentByProcInstIdAndActId(processInstId, activityId);
					if(bizCommentEntity!=null){
						bizCommentEntity.setCompleteTime(new Date());
						if (ActivityImplConstants.ACTIVITYIMPL_TYPE_USERTASK.equals(bizCommentEntity.getActType())) {
							// 查询任务实体
							List<HistoricTaskInstance> hisList = commandContext.getProcessEngineConfiguration().getHistoryService().createHistoricTaskInstanceQuery().processInstanceId(processInstId)
									.taskDefinitionKey(activityId)
									.orderByHistoricTaskInstanceStartTime()
									.desc()
									.list();
							if (hisList!=null && hisList.size()>0) {
								bizCommentEntity.setTaskId(hisList.get(0).getId());
								bizCommentEntity.setUserId(hisList.get(0).getAssignee());
							}
						}
					}
					
					
				}catch(Exception e){
					log.error(e.toString());
				}finally {
					closeCommandContext(commandContext);
				}
			}

		});
	}*/


	
	private void parserBusinessComment(String associatedBizKey, String associatedInstId, String processDefId, CommandContext commandContext, boolean isfirst,
			ProcessInstance processInstance,List<String> allActList,Map<String,List<TaskSimpleInfo>> taskMap,String processStarter) {
		System.out.println("parserBusinessComment: "+taskMap.entrySet());
		for (Entry<String, List<TaskSimpleInfo>> entry : taskMap.entrySet()) {
			System.out.println("foreach taskMap: "+entry.getValue());
		}
		// 获取流程定义对象
		RepositoryService repositoryService = commandContext.getProcessEngineConfiguration().getRepositoryService();
		ProcessDefinitionEntity processDefinitionEntity = (ProcessDefinitionEntity) ((RepositoryServiceImpl) repositoryService).getDeployedProcessDefinition(processDefId);

		ActivityImpl startActivity = processDefinitionEntity.getInitial();

		// 广度优先遍历
		LinkedList<ActivityImpl> queue = new LinkedList<ActivityImpl>();// 待搜索队列
		List<String> searchList = new ArrayList<String>();// 保存已经走过的节点
		queue.offer(startActivity);// 开始节点如队列
		while (!queue.isEmpty()) {
			ActivityImpl activityImpl = queue.poll();
			if (searchList.contains(activityImpl.getId())) {// 已经走过，跳过
				continue;
			}

			for (PvmTransition neighborPvm : activityImpl.getOutgoingTransitions()) {
				/*if(neighborPvm.getProperty("name")!=null){
					if(neighborPvm.getProperty("name").toString().endsWith("_bak")){
						continue;
					}
				}*/
				ActivityImpl neighborAct = (ActivityImpl) neighborPvm.getDestination();
				queue.offer(neighborAct);
			}

			searchList.add(activityImpl.getId());
			allActList.add(activityImpl.getId());
			// 构造操作日志实体
			BizCommentEntity businessCommentEntity = buildBizCommentEntity(associatedBizKey, associatedInstId, processDefId, activityImpl);
			setOperationId(businessCommentEntity, activityImpl);
			
			
			
			
			if (isfirst && businessCommentEntity.isStartAct()) {
				businessCommentEntity.setCompleteTime(new Date());
			}
			businessCommentEntity.setOrder(allActList.size());
			
			

			if (processInstance != null) {
				businessCommentEntity.setBusinessKey(processInstance.getBusinessKey());
				businessCommentEntity.setProcInstId(processInstance.getId());
				//businessCommentEntity.setProcInstName(processInstance.getName());
			}
			//对于产生的用户任务需要更新taskId到Comment中
			//setTaskInfo(businessCommentEntity,taskMap,commandContext);
			
			//对于开始录入结点，需要在新增一条
			/*if(activityImpl.getProperty("name")!=null){
				if(activityImpl.getProperty("name").toString().endsWith("_start")){
					businessCommentEntity.setUserId(processStarter);
					BizCommentEntity newBizCommentEntity=copyBizEntity(businessCommentEntity);
					newBizCommentEntity.setGroups(businessCommentEntity.getGroups());
					newBizCommentEntity.setUserId(businessCommentEntity.getUserId());
					newBizCommentEntity.setTaskId(businessCommentEntity.getTaskId());
					newBizCommentEntity.setMessage("用户录入");
					newBizCommentEntity.setCompleteTime(new Date());
					
					commandContext.getBizCommentEntityManager().insert(newBizCommentEntity);
				}
			}*/
			
			
			commandContext.getBizCommentEntityManager().insert(businessCommentEntity);
		}


	}
	
	
	public static void setOperationId(BizCommentEntity businessCommentEntity,ActivityImpl activityImpl){
		//为用户任务设置操作人
		if(ActivityImplConstants.ACTIVITYIMPL_TYPE_USERTASK.equals(activityImpl.getProperty("type"))){
			if(activityImpl.getActivityBehavior() instanceof UserTaskActivityBehavior){
				UserTaskActivityBehavior userTaskActivityBehavior=(UserTaskActivityBehavior) activityImpl.getActivityBehavior();
				String operationId =getUserTaskOperationId(userTaskActivityBehavior.getTaskDefinition());
				businessCommentEntity.setGroups(operationId);
			}else if(activityImpl.getActivityBehavior()instanceof MultiInstanceActivityBehavior){
				MultiInstanceActivityBehavior multiInstanceActivityBehavior=(MultiInstanceActivityBehavior) activityImpl.getActivityBehavior();
			    String operationId=getMutiUserTaskOperationId(multiInstanceActivityBehavior);
			    businessCommentEntity.setGroups(operationId);
			}else{
				log.error("can't parser behavior "+activityImpl.getActivityBehavior());
			}
		}
	}
	
	
	private static  String getUserTaskOperationId(TaskDefinition taskDefinition){
		StringBuffer buffer=new StringBuffer();
		Expression assigneeExpression=	taskDefinition.getAssigneeExpression();
		if(assigneeExpression!=null){
			buffer.append(assigneeExpression.getExpressionText());
		}else if(taskDefinition.getCandidateGroupIdExpressions()!=null){
			for (Expression expression : taskDefinition.getCandidateGroupIdExpressions()) {
				buffer.append(expression.getExpressionText()+",");
			}
		}else if(taskDefinition.getCandidateUserIdExpressions()!=null){
			for (Expression expression : taskDefinition.getCandidateUserIdExpressions()) {
				buffer.append(expression.getExpressionText()+",");
			}
		}
		
		
		if(buffer.toString().endsWith(",")){
			buffer=new StringBuffer(buffer.substring(0, buffer.length()-1));
		}
	   return buffer.toString();
		
	}
	
	private static String getMutiUserTaskOperationId(MultiInstanceActivityBehavior multiInstanceActivityBehavior){
		String result="";
		if(multiInstanceActivityBehavior.getCollectionExpression()!=null){
			Expression expression=multiInstanceActivityBehavior.getCollectionExpression();
		    String exString=expression.getExpressionText().replace("[", "").replace("]", null);
		    result=exString;
		}
		return result;
		
	
		
	}
	
	

	private void setTaskInfo(BizCommentEntity businessCommentEntity, Map<String, List<TaskSimpleInfo>> taskMap,CommandContext commandContext) {
		if(taskMap.containsKey(businessCommentEntity.getActId())){
			List<TaskSimpleInfo> list=taskMap.get(businessCommentEntity.getActId());
			if(list!=null){
				for(int i=0;i<list.size();i++){
					TaskSimpleInfo info=list.get(i);
					if(i==0){
						businessCommentEntity.setTaskId(info.getTaskId());
						/*if(StringUtils.isNotEmpty(info.getAssignee())){
							businessCommentEntity.setUserId(info.getAssignee());
						}*/
						
					}else{//为会签节点
						BizCommentEntity newEntitiy=copyBizEntity(businessCommentEntity);
						newEntitiy.setTaskId(info.getTaskId());
						/*if(StringUtils.isNotEmpty(info.getAssignee())){
							newEntitiy.setUserId(info.getAssignee());
						}*/
						
						commandContext.getBizCommentEntityManager().insert(newEntitiy);
					}
				}
				
				/*TaskSimpleInfo remove=	list.remove(0);
				businessCommentEntity.setTaskId(remove.getTaskId());
				businessCommentEntity.setUserId(remove.getAssignee());*/
				
				
			}
		}
		
	}

	private BizCommentEntity buildBizCommentEntity(String associatedBizKey, String associatedInstId, String processDefId, ActivityImpl activityImpl) {
		BizCommentEntity businessCommentEntity = new BizCommentEntity();
		businessCommentEntity.setActId(activityImpl.getId());
		if (activityImpl.getProperty("name") != null) {
			businessCommentEntity.setActName(activityImpl.getProperty("name").toString());
		}
		businessCommentEntity.setActType(activityImpl.getProperty("type").toString());
		businessCommentEntity.setAssociatedBizKey(associatedBizKey);
		businessCommentEntity.setAssociatedInstId(associatedInstId);
		businessCommentEntity.setProcDefId(processDefId);
		if (ActivityImplConstants.ACTIVITYIMPL_TYPE_STARTEVENT.equals(activityImpl.getProperty("type").toString())) {
			businessCommentEntity.setStartAct(true);
		}
		return businessCommentEntity;
	}
	
	
	public static  BizCommentEntity copyBizEntity(BizCommentEntity sourceEntity){
		//根据集合的第一个元素拷贝
		BizCommentEntity newEntity=new BizCommentEntity();
		newEntity.setActId(sourceEntity.getActId());
		newEntity.setActName(sourceEntity.getActName());
		newEntity.setActType(sourceEntity.getActType());
		newEntity.setAssociatedBizKey(sourceEntity.getAssociatedBizKey());;
		newEntity.setAssociatedInstId(sourceEntity.getAssociatedInstId());
		newEntity.setBusinessKey(sourceEntity.getBusinessKey());
		newEntity.setOrder(sourceEntity.getOrder());
		newEntity.setProcDefId(sourceEntity.getProcDefId());
		newEntity.setProcInstId(sourceEntity.getProcInstId());
		newEntity.setProcInstName(sourceEntity.getProcInstName());
		newEntity.setStartAct(sourceEntity.isStartAct());
		newEntity.setGroups(sourceEntity.getGroups());
		return newEntity;
	}
	

}
