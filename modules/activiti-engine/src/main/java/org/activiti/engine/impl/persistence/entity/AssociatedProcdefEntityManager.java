package org.activiti.engine.impl.persistence.entity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.impl.persistence.AbstractManager;
import org.activiti.engine.repository.AssociatedProcdef;


public class AssociatedProcdefEntityManager extends AbstractManager{

	/**
	 * 根据业务标识获取该业务下有哪些流程定义
	 * @param businessFlag
	 * @return
	 */
	public List<AssociatedProcdef> findAssociatedProcdefByBizKey(String businessFlag){
		return getDbSqlSession().selectList("selectAssociatedProcdefByBizKey", businessFlag);
	}
	

	/**
	 * 获取流程定义在该业务下的顺序
	 * @param businessFlag
	 * @param procDefId
	 * @return
	 */
	public Integer findProcessDefinitionOrder(String associatedBizKey,String procDefId){
		Map<String,String> param=new HashMap<String, String>();
		param.put("associatedBizKey", associatedBizKey);
		param.put("procDefId", procDefId);
		
		return (Integer) getDbSqlSession().selectOne("selectProcessDefinitionOrder", param);
	}
}
