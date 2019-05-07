package org.activiti.engine.impl.cmd;

import java.io.Serializable;

import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;

public class CommandContextHelperCmd implements Command<Void>, Serializable{

	private static final long serialVersionUID = 1L;
	
	public CommandContextHelperCmd(){
		
	}

	@Override
	public Void execute(CommandContext commandContext) {
	
		return null;
	}
}
