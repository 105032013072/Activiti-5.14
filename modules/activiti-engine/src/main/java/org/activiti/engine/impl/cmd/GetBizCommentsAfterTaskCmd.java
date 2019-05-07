package org.activiti.engine.impl.cmd;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.ActivityImplConstants;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.impl.RepositoryServiceImpl;
import org.activiti.engine.impl.bpmn.helper.BizCommentRecorder;
import org.activiti.engine.impl.bpmn.helper.StringUtils;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.BizCommentEntity;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.HistoricProcessInstanceEntity;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.persistence.entity.TaskEntity;
import org.activiti.engine.impl.pvm.PvmTransition;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.repository.BizComment;

public class GetBizCommentsAfterTaskCmd implements Command<List<BizComment>>, Serializable{
	
	private static final long serialVersionUID = 1L;
	
	private String businessKey;
	
	public GetBizCommentsAfterTaskCmd(String businessKey){
		this.businessKey=businessKey;
	}

	
	
	@Override
	public List<BizComment> execute(CommandContext commandContext) {
		if(StringUtils.isEmpty(businessKey)){
			throw new ActivitiIllegalArgumentException("businessKey is empty");
		}
		
		List<BizComment> result=new ArrayList<BizComment>();
		List<BizComment> sortResult=new ArrayList<BizComment>();
		List<BizComment> bizCommentList=commandContext.getBizCommentEntityManager().findBizCommentByBusinessKey(businessKey);
		
		if(bizCommentList!=null && bizCommentList.size()>0){
			BizComment first=getFirst(bizCommentList);
			result=commandContext
					.getBizCommentEntityManager()
					.findBizCommentAfterOrder(first.getAssociatedInstId(), first.getOrder(),first.getProcDefId(), ActivityImplConstants.ACTIVITYIMPL_TYPE_USERTASK);
			
			//对结果集进行排序：
			List<BizComment> unfinishBizList=new ArrayList<BizComment>();
			Set<String> processInstanceSet=new LinkedHashSet<String>();//按照流程实例放入的顺序保存
			Map<String,List<BizComment>> bizMap=new HashMap<String, List<BizComment>>();
			
			for (BizComment bizComment : result) {
				if(bizComment.getActName()!=null){
					if(bizComment.getActName().endsWith("_end")) continue;
				}
			
				//未执行过的
				if(StringUtils.isEmpty(bizComment.getProcInstId())){
					unfinishBizList.add(bizComment);
				}else{
					HistoricProcessInstanceEntity historicProcessInstanceEntity=commandContext.getHistoricProcessInstanceEntityManager().findHistoricProcessInstance(bizComment.getProcInstId());
					if(historicProcessInstanceEntity==null){//被删除的流程实例
						unfinishBizList.add(bizComment);
					}else{
						//按照流程实例分组
						processInstanceSet.add(bizComment.getProcInstId());
						List<BizComment> mapResult=bizMap.get(bizComment.getProcInstId());
						if(mapResult==null){
							mapResult=new ArrayList<BizComment>();
							bizMap.put(bizComment.getProcInstId(), mapResult);
						}
						mapResult.add(bizComment);
					}
				}
			}
			
			//对每个组内的排序
			Map<String,List<BizComment>> afterSort=new HashMap<String, List<BizComment>>();//保存排序后最终的结果
			for (Entry<String, List<BizComment>> entry : bizMap.entrySet()) {
				List<BizComment> groupSortList=new ArrayList<BizComment>();
				String processInstanceId=entry.getKey();
				
				
				
				//按是否完成分组
				List<BizComment> finishList=new ArrayList<BizComment>();
				Map<String,BizComment> finishMap=new HashMap<String, BizComment>();
				Map<String,BizComment> unfinishMap=new HashMap<String, BizComment>();
				for (BizComment bizComment : entry.getValue()) {
					/*if(bizComment.getCompleteTime()==null){
						unfinishMap.put(bizComment.getActId(), bizComment);
					}else{
						finishList.add(bizComment);
					}*/
					if(StringUtils.isNotEmpty(bizComment.getTaskId())&&bizComment.getCompleteTime()!=null){
						finishList.add(bizComment);
						finishMap.put(bizComment.getActId(), bizComment);
					}else{
						unfinishMap.put(bizComment.getActId(), bizComment);
					}
					
				}
				//对于unfinishMap，需要排除由于条件不满足而没有执行的结点
				/*Set<String> hisActSet=getfinishAct(processInstanceId, commandContext);
				for (String hisAct : hisActSet) {
					unfinishMap.remove(hisAct);
				}*/
				
				//已完成先按照完成时间排序：
				if(finishList.size()>0){
					Collections.sort(finishList, new Comparator<BizComment>() {
			            public int compare(BizComment arg0, BizComment arg1) {
			            	return arg0.getCompleteTime().compareTo(arg1.getCompleteTime());
			            }
			        });
					groupSortList.addAll(finishList);
				}
				
				
				
				//未完成需要按照开始的第一个结点排序
			/*	if(unfinishMap.size()>0){
					//根据流程实例获取businesKey
				    List<BizComment> sort=new ArrayList<BizComment>();
					HistoricProcessInstanceEntity historicProcessInstanceEntity=commandContext.getHistoricProcessInstanceEntityManager().findHistoricProcessInstance(processInstanceId);
					List<BizComment> temp=commandContext.getBizCommentEntityManager().findBizCommentByBusinessKey(historicProcessInstanceEntity.getBusinessKey());
					BizComment fisrt=getFirst(temp);
					//获取所有可到的结点
					List<String> arriveList=commandContext.getProcessEngineConfiguration().getHistoryService().getAfterActivityCmd(fisrt.getTaskId());
				    for (String arrive : arriveList) {
                      if(unfinishMap.containsKey(arrive)){
                    	  sort.add(unfinishMap.get(arrive));
                      }else if(currentTaskArrive.contains(arrive)){//又是当前活动结点可达的后续结点，说明该结点处于回退路径上，数据显示时新增
                    	  BizComment sourceEntity=finishMap.get(arrive);
                          if(sourceEntity!=null){
                        	  BizComment copy=BizCommentRecorder.copyBizEntity((BizCommentEntity) sourceEntity);
                        	  copy.setCompleteTime(null);
                              copy.setTaskId(null);
                              copy.setUserId(null);
                              copy.setMessage(null);
                              copy.setAdvice(null);
                              sort.add(copy);
                          }
                      }
					}
				    groupSortList.addAll(sort); 
				}*/
				if(unfinishMap.size()>0){//有未完成的节点
					//该流程实例是否结束：
					List<ActivityImpl> currentTaskArrive=new ArrayList<ActivityImpl>();
					ExecutionEntity executionEntity=commandContext.getExecutionEntityManager().findExecutionById(processInstanceId);
					if(executionEntity!=null){//是当前正在活动的流程实例
						//获取当前活动任务Id
						List<TaskEntity>  taskList=commandContext.getTaskEntityManager().findTasksByProcessInstanceId(executionEntity.getId());
						if(taskList.size()>0){
							currentTaskArrive=commandContext.getProcessEngineConfiguration().getHistoryService().getAfterActivityCmd(taskList.get(0).getId());
						}
						List<BizComment> sort=new ArrayList<BizComment>();
						for (ActivityImpl activityImpl : currentTaskArrive) {
							if(ActivityImplConstants.ACTIVITYIMPL_TYPE_USERTASK.equals(activityImpl.getProperty("type"))){
								BizComment bizComment=new BizCommentEntity();
								bizComment.setActId(activityImpl.getId());
								if(activityImpl.getProperty("name")!=null){
									bizComment.setActName(activityImpl.getProperty("name").toString());
								}
								
								BizCommentRecorder.setOperationId((BizCommentEntity)bizComment, activityImpl);
								sort.add(bizComment);
							}
						}
						groupSortList.addAll(sort); 
						
					}
					
				}
				
				afterSort.put(processInstanceId, groupSortList);//该流程实例对应的日志完成排序保。
				
			}
			
			//所有流程实例对应的组按顺序组合
			for (String processInstId : processInstanceSet) {
				if(afterSort.containsKey(processInstId)){
					sortResult.addAll(afterSort.get(processInstId));
				}
			}
			sortResult.addAll(unfinishBizList);
		
		}else{
			throw new ActivitiIllegalArgumentException("can,t find BizComment by businessKey="+businessKey);
		}
		return sortResult;
		
		
		
		
		
		
	}
	
	
	private BizComment getFirst(List<BizComment> searchResult){
		BizComment result=null;
		for (BizComment bizComment : searchResult) {
			if(bizComment.getCompleteTime()!=null){
				result=bizComment;
				break;
			} 
		}
		
		if(result==null && !searchResult.isEmpty()){//所有的完成时间都为空
			result=searchResult.get(0);
			
		}
		return result;
	}
	
	/**
	 * 从流程图的角度获取已经走过的结点
	 * @param processInstId
	 * @return
	 */
	public Set<String> getfinishAct(String processInstId,CommandContext commandContext){
		HistoricProcessInstanceEntity historicProcessInstance=	commandContext.getHistoricProcessInstanceEntityManager().findHistoricProcessInstance(processInstId);
	    //获取流程当前活动结点
		List<TaskEntity> taskList=commandContext.getTaskEntityManager().findTasksByProcessInstanceId(historicProcessInstance.getId());
		RepositoryService repositoryService = commandContext.getProcessEngineConfiguration().getRepositoryService();
	 	ProcessDefinitionEntity processDefinitionEntity = (ProcessDefinitionEntity) ((RepositoryServiceImpl) repositoryService).getDeployedProcessDefinition(historicProcessInstance.getProcessDefinitionId());
		Set<String> result=new HashSet<String>();
		if(taskList.isEmpty()){//所有结点已经执行过
	    	for (ActivityImpl activityImpl : processDefinitionEntity.getActivities()) {
	    		result.add(activityImpl.getId());
			}
	    }else{
	    	for (TaskEntity taskEntity : taskList) {
	    		result.addAll(bfs(taskEntity.getTaskDefinitionKey(), processDefinitionEntity));
			}
	    	

	    }
		return result;
	}
	
	/**
	 * 获取从开始结点，可到达指定结点的所有结点（start结点逆序广度遍历）
	 * @param start
	 * @param processDefinitionEntity
	 * @return
	 */
	private List<String> bfs(String start,ProcessDefinitionEntity processDefinitionEntity ){
		 ActivityImpl startActivity = processDefinitionEntity.findActivity(start);

		    List<String> result=new ArrayList<String>();
	 		// 广度优先遍历
	 		LinkedList<ActivityImpl> queue = new LinkedList<ActivityImpl>();// 待搜索队列
	 		List<String> searchList = new ArrayList<String>();// 保存已经走过的节点
	 		queue.offer(startActivity);// 开始节点如队列
	 		while (!queue.isEmpty()) {
	 			ActivityImpl activityImpl = queue.poll();
	 			if (searchList.contains(activityImpl.getId())) {// 已经走过，跳过
	 				continue;
	 			}
	 		

	 			for (PvmTransition neighborPvm : activityImpl.getIncomingTransitions()) {
	 				ActivityImpl neighborAct = (ActivityImpl) neighborPvm.getSource();
	 				queue.offer(neighborAct);
	 			}

	 			searchList.add(activityImpl.getId());
	 			result.add(activityImpl.getId());
	 		}
	 		
	 		if(!result.isEmpty()){
	 			result.remove(start);
	 		}
	 		
	 		return result;

	}

}
