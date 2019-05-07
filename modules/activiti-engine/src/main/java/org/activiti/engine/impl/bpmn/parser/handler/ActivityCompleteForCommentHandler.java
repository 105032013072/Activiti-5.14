package org.activiti.engine.impl.bpmn.parser.handler;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.ExecutionListener;

public class ActivityCompleteForCommentHandler implements ExecutionListener{

	@Override
	public void notify(DelegateExecution execution) throws Exception {
		
		
		
		
		/*System.out.println("执行结点："+execution.getCurrentActivityId());
		BizCommentRecorder businessCommentRecorder=new BizCommentRecorder(Context.getProcessEngineConfiguration());
		businessCommentRecorder.updateComments(execution.getProcessInstanceId(), execution.getCurrentActivityId());*/
	}

}
