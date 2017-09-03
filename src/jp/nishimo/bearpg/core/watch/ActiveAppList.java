package jp.nishimo.bearpg.core.watch;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import jp.nishimo.bearpg.config.Configuration;
import jp.nishimo.bearpg.utils.Command;
import jp.nishimo.bearpg.utils.Util;

public class ActiveAppList {

	public class ActiveApp {
		private String appName;
		private boolean isAskedTool;
		private boolean isProgrammingTool;
		
		public ActiveApp(){			
		}
		
		public ActiveApp(String _appName){
			appName = _appName; 
			isAskedTool = false;
			isProgrammingTool = false;
		}
		
		public boolean setSerializeData(String data){
			boolean ret = false;
			String datas[] = data.split(",");
			if(datas.length == 3){
				appName = datas[0];
				isAskedTool = convString2Boolean(datas[1]);
				isProgrammingTool = convString2Boolean(datas[2]);
				ret = true;
			}
			return ret;
		}
		
		public boolean eq(String _appName){
			if(appName.equals(_appName)){
				return true;
			}
			return false;
		}
		
		public void setIsProgrammingTool(boolean _isProgrammingTool){
			isProgrammingTool = _isProgrammingTool;
		}
		
		public void setIisAskTool(boolean _isAskedTool){
			isAskedTool = _isAskedTool;
		}
		
		public String getString(){
			return appName + "," + isAskedTool + "," + isProgrammingTool;
		}
		
		private boolean convString2Boolean(String data){
			if(data.equals("true")){
				return true;
			}
			return false;
		}
	}
	
	private List<ActiveApp> activeAppLists;
	
	public ActiveAppList(){
		activeAppLists = new ArrayList<ActiveApp>();
	}
	
	public void loadAppList(){
		String data = Util.read(Configuration.appListFilePath);
		String[] dataLines = data.split("\n");
		for(int i = 0; i < dataLines.length; i++){
			ActiveApp active = new ActiveApp();
			if(active.setSerializeData(dataLines[i])){
				activeAppLists.add(active);
			}
		}
	}
	
	public void saveAppList(){
		if(activeAppLists.size() > 0){
			String str = "";
			for(int i = 0; i < activeAppLists.size(); i++){
				ActiveApp activeApp = activeAppLists.get(i);
				str = str + activeApp.getString() + "\n";
			}
			Util.write(Configuration.appListFilePath, str);
		}
	}

	public void updateApplicationAppList(){
		Command command = new Command();
		String commandStr = "swift " + Configuration.swiftProgramDir + "/ActiveAppLists.swift";
		String[] commands = {"/bin/sh", "-c", commandStr};
		String result = command.commandExec(commands);
		String[] resultNewLine = result.split("\n");
		
		for(int i = 0; i < resultNewLine.length; i++){
//			System.out.println(resultNewLine[i]);
			boolean findFlg = false;
			for(int j = 0; j < activeAppLists.size(); j++){
				ActiveApp activeApp = activeAppLists.get(j);
				if(activeApp.eq(resultNewLine[i])){
					findFlg = true;
					break;
				}
			}
			if(!findFlg){
				activeAppLists.add(new ActiveApp(resultNewLine[i]));
			}
		}
	}
	
	public String getNoAskTool(){
		for(int i = 0; i < activeAppLists.size(); i++){
			ActiveApp activeApp = activeAppLists.get(i);
			if(!activeApp.isAskedTool){
				return activeApp.appName;
			}
		}
		return "";
	}
	
	public void setProgrammingTool(String appName, boolean ispTool){
		for(int i = 0; i < activeAppLists.size(); i++){
			ActiveApp activeApp = activeAppLists.get(i);
			if(appName.equals(activeApp.appName)){
				activeApp.isAskedTool = true;
				activeApp.isProgrammingTool = ispTool;
				
				saveAppList();
				break;
			}
		}
	}
	
	public List<String> getProgrammingTool(){
		List<String> ret = new ArrayList<String>();
		for(int i = 0; i < activeAppLists.size(); i++){
			ActiveApp activeApp = activeAppLists.get(i);
			if(activeApp.isProgrammingTool){
				ret.add(activeApp.appName);
			}
		}
		return ret;
	}	
}
