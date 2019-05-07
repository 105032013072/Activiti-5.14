/* Licensed under the Apache License, Version 2.0 (the "License");
/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.activiti.engine.impl.cmd;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.activiti.engine.ActVariable;
import org.activiti.engine.ActivitiException;
import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.ActivitiObjectNotFoundException;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.impl.bpmn.helper.BizCommentRecorder;
import org.activiti.engine.impl.bpmn.helper.TaskSimpleInfo;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.deploy.DeploymentManager;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;


/**
 * @author Tom Baeyens
 * @author Joram Barrez
 */
public class StartProcessInstanceCmd<T> implements Command<ProcessInstance>, Serializable {

  private static final long serialVersionUID = 1L;
  protected String processDefinitionKey;
  protected String processDefinitionId;
  protected Map<String, Object> variables;
  protected String businessKey;
  
  //==associated
	protected String associatedBizKey;//业务标识
	protected String previousProcessInstId;//上一个流程实例Id
	protected String processStarter;
  
  public StartProcessInstanceCmd(String processDefinitionKey, String processDefinitionId, String businessKey, Map<String, Object> variables) {
    this.processDefinitionKey = processDefinitionKey;
    this.processDefinitionId = processDefinitionId;
    this.businessKey = businessKey;
    this.variables = variables;
  }
  
  public StartProcessInstanceCmd(String processDefinitionKey, String processDefinitionId, String businessKey, Map<String, Object> variables,String associatedBizKey,String previousProcessInstId,String processStarter) {
	    this.processDefinitionKey = processDefinitionKey;
	    this.processDefinitionId = processDefinitionId;
	    this.businessKey = businessKey;
	    this.variables = variables;
	    this.associatedBizKey=associatedBizKey;
	    this.previousProcessInstId=previousProcessInstId;
	    this.processStarter=processStarter;
   }
  
  
  public ProcessInstance execute(CommandContext commandContext) {
    DeploymentManager deploymentCache = Context
      .getProcessEngineConfiguration()
      .getDeploymentManager();
    
    // Find the process definition
    ProcessDefinitionEntity processDefinition = null;
    if (processDefinitionId!=null) {
      processDefinition = deploymentCache.findDeployedProcessDefinitionById(processDefinitionId);
      if (processDefinition == null) {
        throw new ActivitiObjectNotFoundException("No process definition found for id = '" + processDefinitionId + "'", ProcessDefinition.class);
      }
    } else if(processDefinitionKey != null){
      processDefinition = deploymentCache.findDeployedLatestProcessDefinitionByKey(processDefinitionKey);
      if (processDefinition == null) {
        throw new ActivitiObjectNotFoundException("No process definition found for key '" + processDefinitionKey +"'", ProcessDefinition.class);
      }
    } else {
      throw new ActivitiIllegalArgumentException("processDefinitionKey and processDefinitionId are null");
    }
    
    // Do not start process a process instance if the process definition is suspended
    if (processDefinition.isSuspended()) {
      throw new ActivitiException("Cannot start process instance. Process definition " 
              + processDefinition.getName() + " (id = " + processDefinition.getId() + ") is suspended");
    }

    // Start the process instance
    ExecutionEntity processInstance = processDefinition.createProcessInstance(businessKey);
    if (variables!=null) {
      processInstance.setVariables(variables);
    }
    processInstance.start();
    
		// 全量操作日志
		RuntimeService runtimeService = commandContext.getProcessEngineConfiguration().getRuntimeService();

		List<TaskSimpleInfo> result = (List<TaskSimpleInfo>) runtimeService
				.getVariable(processInstance.getProcessInstanceId(), ActVariable.START_PROCESS_TASKIDLIST);

		BizCommentRecorder businessCommentRecorder = new BizCommentRecorder(
				commandContext, result);
		businessCommentRecorder.createOrUpdateComments(associatedBizKey, processInstance, previousProcessInstId,processStarter);

		// 将删除流程变量
		runtimeService.removeVariable(processInstance.getProcessInstanceId(), ActVariable.START_PROCESS_TASKIDLIST);
    
    return processInstance;
  }
}
