package jp.nishimo.bearpg.core.threads;

import java.util.ArrayList;
import java.util.List;

import jp.nishimo.bearpg.core.plugins.PluginManager;

public class PluginServer {	
	
	public class PluginMessageInfo {
		private String pluginName;
		private String data;
		
		public PluginMessageInfo(String _pluginName, String _data){
			pluginName = _pluginName;
			data = _data;
		}
	}
	
	public class PluginServerThread extends Thread {
		private boolean isRunning = false;
		private List<PluginMessageInfo> messages;
		private PluginManager pluginManager;
		
		public PluginServerThread(){
			isRunning = false;
			pluginManager = new PluginManager();
			messages = new ArrayList<PluginMessageInfo>();
		}
		
		public void run(){
			isRunning = true;
			
			while(isRunning){
				if(messages.size() > 0){
					PluginMessageInfo message = messages.get(0);
					pluginManager.sendRequest(message.pluginName, message.data);					
					messages.remove(0);
				}
				MySleep.exec(1000);
			}
		}
		
		public void setMessage(PluginMessageInfo message){
			messages.add(message);
		}
		
		public void finish(){
			if(isRunning){
				isRunning = false;
			}
		}
	}

	private PluginServerThread pluginServerThread = null;
	
	public PluginServer(){
	}
	
	public boolean start(){
		pluginServerThread = new PluginServerThread();
		pluginServerThread.start();
		return pluginServerThread.isRunning;
	}
	
	public void setMessage(String pluginName, String data){
		pluginServerThread.setMessage(new PluginMessageInfo(pluginName, data));
	}
	
	public void end(){
		if(pluginServerThread != null){
			pluginServerThread.finish();
		}
	}
}
