package org.activiti.engine.impl.bpmn.listener;

import java.util.ArrayList;
import java.util.List;

import org.activiti.engine.ActVariable;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.TaskListener;
import org.activiti.engine.impl.bpmn.helper.BizCommentRecorder;
import org.activiti.engine.impl.bpmn.helper.TaskSimpleInfo;
import org.activiti.engine.impl.cmd.StartProcessInstanceCmd;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.interceptor.Command;

public class TaskCreateListenerForComment implements TaskListener{

	private static final long serialVersionUID = 1L;

	@Override
	public void notify(DelegateTask delegateTask) {
		
		// 由发起流程命令触发的不更新操作日志。操作的更新在发起流程的命令中执行。
		Command currentCommand = Context.getCommandContext().getCommand();
		if (currentCommand instanceof StartProcessInstanceCmd){
			System.out.println("TaskCreateListenerForComment: instanceof StartProcessInstanceCmd");
			//在流程变量中保存由发起流程产生的任务信息
			RuntimeService runtimeService=Context.getProcessEngineConfiguration().getRuntimeService();

			List<TaskSimpleInfo>  result=(List<TaskSimpleInfo>) runtimeService.getVariable(delegateTask.getProcessInstanceId(), ActVariable.START_PROCESS_TASKIDLIST);
			if(result==null){
				result=new ArrayList<TaskSimpleInfo>();
				runtimeService.setVariable(delegateTask.getProcessInstanceId(), ActVariable.START_PROCESS_TASKIDLIST, result);
			}
			TaskSimpleInfo taskSimpleInfo=new TaskSimpleInfo();
			taskSimpleInfo.setAssignee(delegateTask.getAssignee());
			taskSimpleInfo.setTaskId(delegateTask.getId());
			taskSimpleInfo.setTaskDefiniotinKey(delegateTask.getTaskDefinitionKey());
			result.add(taskSimpleInfo);
			
			System.out.println("TaskCreateListenerForComment: instanceof StartProcessInstanceCmd variable: "+result);
			runtimeService.setVariable(delegateTask.getProcessInstanceId(), ActVariable.START_PROCESS_TASKIDLIST, result);
			return ;
		}
			
		
		System.out.println("创建任务："+delegateTask.getId());
		BizCommentRecorder bizCommentRecorder=new BizCommentRecorder(Context.getCommandContext());
		bizCommentRecorder.updateOrInsertTaskComment(delegateTask.getProcessInstanceId(), delegateTask.getId(), delegateTask.getTaskDefinitionKey(), delegateTask.getAssignee());
	} 

}
