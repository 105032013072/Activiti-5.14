package org.activiti.engine.impl.persistence.entity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.impl.db.PersistentObject;
import org.activiti.engine.impl.persistence.AbstractManager;
import org.activiti.engine.repository.BizComment;

public class BizCommentEntityManager extends AbstractManager{
	
	/**
	 * 根据业务实例ID和流程定义ID获取批注
	 * @param businessInstId
	 * @param ProcDefId
	 * @return
	 */
	public List<BizCommentEntity> findBizCommentByAssociatedInstIdAndProcDefId(String associatedInstId,String procDefId){
		Map<String,String> param=new HashMap<String, String>();
		param.put("associatedInstId", associatedInstId);
		param.put("procDefId", procDefId);   
		return getDbSqlSession().selectList("selectBizCommentByAssociatedInstIdAndProcDefId", param);
	}
	
	/**
	 * 根据流程实例ID和节点ID获取批注
	 * @param processInstId
	 * @param actId
	 * @return
	 */
	public List<BizCommentEntity> findBizCommentByProcInstIdAndActId(String processInstId,String actId){
		Map<String,String> param=new HashMap<String, String>();
		param.put("processInstId", processInstId);
		param.put("actId", actId);                                 
		return getDbSqlSession().selectList("selectBizCommentByProcInstIdAndActId", param);
	}
   
	/**
	 * 根据任务Id查询对应的批注
	 * @param taskId
	 * @return
	 */
	public List<BizComment> findBizCommentByTaskId(String taskId){
		return getDbSqlSession().selectList("selectBizCommentByTaskId", taskId);
	}
	
	public List<BizComment> findBizCommentByBusinessKey(String businessKey){
		return (List<BizComment>) getDbSqlSession().selectList("selectBizCommentByBusinessKey", businessKey);
	}
	
	/**
	 * 查询指定顺序之后的批注信息（按order的顺序返回）
	 * @param businessInstId
	 * @param order
	 * @param activityImplConstants ：指定批注信息对应的节点类型，若为空则表示查询所有类型
	 * @return
	 */
	public List<BizComment> findBizCommentAfterOrder(String businessInstId,Integer order,String processDefinitonId,String activityImplConstants){
		Map<String,Object> param=new HashMap<String, Object>();
		param.put("businessInstId", businessInstId);
		param.put("order", order);
		param.put("actType", activityImplConstants);
		param.put("processDefinitionId", processDefinitonId);
		
		return getDbSqlSession().selectList("selectBizCommentAfterOrder", param);
		
	}
	
	/**
	 * 根据流程实例ID获取对应的业务实例ID
	 * @param processInstId
	 * @return
	 */
	public String findAssociatedInstIdByProcessInstId(String processInstId){
		List<String> list=getDbSqlSession().selectList("selectAssociatedInstIdByProcessInstId", processInstId);
		if(list!=null &&list.size()>0){
			return list.get(0);
		}else return null;
	}
	
	/**
	 * 根据流程实例删除操作日志
	 * @param processInstId
	 */
	public void deleteBizCommentByProcessInstId(String processInstId){
		List<BizComment> list=getDbSqlSession().selectList("selectByProcessInstId", processInstId);
		for (BizComment bizComment : list) {
			getDbSqlSession().delete((PersistentObject) bizComment);
		}
		
	}
	
}
