package jp.nishimo.bearpg.core.plugins;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.xmlrpc.XmlRpcRequest;
import org.apache.xmlrpc.client.AsyncCallback;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;

import jp.nishimo.bearpg.PairProgrammingAI;
import jp.nishimo.bearpg.config.Configuration;
import jp.nishimo.bearpg.core.plugins.base.MessageInfo;
import jp.nishimo.bearpg.core.plugins.base.PluginInfo;

public class PluginManager {
	
	public class PluginList {
		public PluginInfo pInfo;
		public String pName;
		public String pIpAddr;
		public String pPort;
		
		public PluginList(String data){
			String[] datas = data.split(",");
			if(datas.length == 3){
				pName = datas[0];
				pIpAddr = datas[1];
				pPort = datas[2];
			}
		}
	}
	
	private List<PluginList> pluginLists;
	
	public PluginManager(){
		pluginLists = new ArrayList<PluginList>();
		loadPlugins();
	}

	public void sendRequest(String pName, String data){
		for(PluginList pluginList : pluginLists){
			if(pName.equals(pluginList.pName)){
				request(pluginList, data);
			}
		}
	}
	
	private void loadPlugins(){
		String data = read(Configuration.pluginListFilePath);
		
		String[] dataLines = data.split("\n");
		for(int i = 0; i < dataLines.length; i++){
			PluginList plugin = new PluginList(dataLines[i]);
			pluginLists.add(plugin);
		}
	}
	
	private void request(PluginList pList, String data) {
		try{
            XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
            config.setServerURL(new URL("http://"+ pList.pIpAddr +":"+ pList.pPort +"/xmlrpc"));

            XmlRpcClient client = new XmlRpcClient();
            client.setConfig(config);

            Object[] params = new Object[]{new String(data)};
            client.executeAsync(pList.pName + "." + "getInformation", params, new AsyncCallback() {

                @Override
                public void handleResult(XmlRpcRequest pRequest, Object pResult) {
                	String[] result = ((String) pResult).split(" ");
                	if(result != null){
                		MessageInfo messageInfo;
                		if(result.length == 1){
                			messageInfo = new MessageInfo(result[0], null, "", MessageInfo.NotifyType.NOTHING);
                			PairProgrammingAI.notifyServer.setMessage(messageInfo);
                		} else if(result.length == 2){
                			messageInfo = new MessageInfo(result[0], null, result[1], MessageInfo.NotifyType.LINK);
                			PairProgrammingAI.notifyServer.setMessage(messageInfo);
                		}
                	}
                }
                
                @Override
                public void handleError(XmlRpcRequest pRequest, Throwable pError) {
                }
            });
        }catch(Exception e){
            e.printStackTrace();
        }
	}
	
	private String read(String filepath){
		String ret = "";
		try{
			File file = new File(filepath);
			BufferedReader br = new BufferedReader(new FileReader(file));
			
			String str = br.readLine();
			while(str != null){
				ret = ret + str + "\n";
			    str = br.readLine();
			}
			br.close();
		} catch(IOException e) {
        }
		return ret;
	}
}
