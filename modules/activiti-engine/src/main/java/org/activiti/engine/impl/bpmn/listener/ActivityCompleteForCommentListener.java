package org.activiti.engine.impl.bpmn.listener;

import org.activiti.engine.ActivityImplConstants;
/*import org.activiti.engine.delegate.event.ActivitiEvent;
import org.activiti.engine.delegate.event.ActivitiEventListener;
import org.activiti.engine.delegate.event.impl.ActivitiActivityEventImpl;*/
import org.activiti.engine.impl.bpmn.helper.BizCommentRecorder;
import org.activiti.engine.impl.cmd.StartProcessInstanceCmd;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.interceptor.Command;

/**
 * 监听节点的完成以更新批注信息
 * @author huangxw
 *
 */
public class ActivityCompleteForCommentListener /*implements ActivitiEventListener*/{

	/*@Override
	public void onEvent(ActivitiEvent event) {
		ActivitiActivityEventImpl entityEvent = (ActivitiActivityEventImpl) event;
		
		//由发起流程命令触发并且节点为开始节点的不处理。开始节点的的批注信息更新在发起流程的命令中执行。
		Command currentCommand = Context.getCommandContext().getCommand();
		if ((currentCommand instanceof StartProcessInstanceCmd)
				&&  ActivityImplConstants.ACTIVITYIMPL_TYPE_STARTEVENT.equals(entityEvent.getActivityType()))  
		 return ;
		
		//更新日志
		BizCommentRecorder businessCommentRecorder=new BizCommentRecorder(Context.getProcessEngineConfiguration());
		businessCommentRecorder.updateComments(event.getProcessInstanceId(), entityEvent.getActivityId(), entityEvent.getActivityType());
		
		
	}

	@Override
	public boolean isFailOnException() {
		
		return false;
	}
*/
}
