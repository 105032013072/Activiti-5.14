package org.activiti.engine.impl.bpmn.helper;

public class StringUtils {

	public static boolean isNotEmpty(String str){
		if(str==null ||"".equals(str)) return false;
		else return true;
	} 
	
	public static boolean isEmpty(String str){
		if(str==null ||"".equals(str)) return true;
		else return false;
	} 
}
