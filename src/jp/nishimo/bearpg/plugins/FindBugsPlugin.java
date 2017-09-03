package jp.nishimo.bearpg.plugins;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.xmlrpc.server.PropertyHandlerMapping;
import org.apache.xmlrpc.server.XmlRpcServerConfigImpl;
import org.apache.xmlrpc.server.XmlRpcStreamServer;
import org.apache.xmlrpc.webserver.WebServer;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import jp.nishimo.bearpg.config.Configuration;
import jp.nishimo.bearpg.core.plugins.base.PluginInfo;
import jp.nishimo.bearpg.core.plugins.base.PluginInterface;
import jp.nishimo.bearpg.utils.Command;

public class FindBugsPlugin implements PluginInterface {

	private static void init(){
		System.out.println("----------- FindBugsPlugin static INIT");
	}
	
	public FindBugsPlugin(){
	}
	
	@Override
	public PluginInfo getPluginInfo() {
		PluginInfo pInfo = new PluginInfo();
		pInfo.pName = "FindBugsPlugin";
		pInfo.pExplain = "";
		pInfo.pVer = "3.0.1";
		pInfo.pKind = PluginInfo.PluginKind.CodeAnalysis;
		return pInfo;
	}
	
	@Override
	public String getInformation(String targetFilePath) {
		System.out.println("----------- FindBugsPlugin getInformation " + targetFilePath);
		
		String ret = null;
		if(isWarning(targetFilePath)){
			Command command = new Command();
			String commandStr = Configuration.findBugsDir + "/bin/findbugs -textui -output result.html -html " + targetFilePath;
			String[] commands = {"/bin/sh", "-c", commandStr};
			command.commandExec(commands);
			
			String currentDirectory = getCurrentDirectory();
			if(currentDirectory != null){
				String message = "修正した方がいいところがありますよ";
				String url = "file://" + currentDirectory + "/result.html";
				ret = message + " " + url;
			}
		}
		return ret;
	}
	
	private String getCurrentDirectory(){
		Command command = new Command();
		String commandStr = "pwd";
		String[] commands = {"/bin/sh", "-c", commandStr};
		String result = command.commandExec(commands);
		String[] results = result.split("\n");
		if(results.length == 1){
			return results[0];
		}
		return null;
	}
	
	private boolean isWarning(String targetFilePath){
		Command command = new Command();
		String commandStr = Configuration.findBugsDir + "/bin/findbugs -textui " + targetFilePath;
		String[] commands = {"/bin/sh", "-c", commandStr};
		String result = command.commandExec(commands);
		String[] results = result.split("\n");
		for(String r : results){
			if(r.length() != 0){
				return true;
			}
		}
		return false;
	}
	
	private void readXML(String filepath){
		try {
			Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new File(filepath));
			Node node = document.getDocumentElement();
			
			NodeList nodeLists = node.getChildNodes();
			for(int i = 0; i < nodeLists.getLength(); i++){
				if(nodeLists.item(i).getNodeType() ==  Node.ELEMENT_NODE){
					String nodeName = nodeLists.item(i).getNodeName();
					if(nodeName.equals("BugInstance")){
						NamedNodeMap attrs = nodeLists.item(i).getAttributes();
						if(attrs != null){
							Node attrType = attrs.getNamedItem("type");
							Node attrPriority = attrs.getNamedItem("priority");
							Node attrRank = attrs.getNamedItem("rank");
							Node attrAbbrev = attrs.getNamedItem("abbrev");
							Node attrCategory = attrs.getNamedItem("category");
							
							System.out.println("------------------");
							System.out.println("TYPE:" + attrType.getNodeValue());
							System.out.println("PRIO:" + attrPriority.getNodeValue());
							System.out.println("RANK:" + attrRank.getNodeValue());
							System.out.println("ABBR:" + attrAbbrev.getNodeValue());
							System.out.println("CATE:" + attrCategory.getNodeValue());
						}
						
						NodeList bugNodeLists = nodeLists.item(i).getChildNodes();
						if(bugNodeLists != null){
							for(int j = 0; j < bugNodeLists.getLength(); j++){
								if(bugNodeLists.item(j).getNodeType() ==  Node.ELEMENT_NODE){
									String bugNodeName = bugNodeLists.item(j).getNodeName();
									if(bugNodeName.equals("SourceLine")){
										NamedNodeMap bugAttrs = bugNodeLists.item(j).getAttributes();
										if(attrs != null){
											Node attrSourceFile = bugAttrs.getNamedItem("sourcefile");
											Node attrStart = bugAttrs.getNamedItem("start");
											System.out.println("FILE:" + attrSourceFile.getNodeValue());
											System.out.println("LINE:" + attrStart.getNodeValue());
										}
									}
								}
							}							
						}
					}
				}
			}
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
	}
	
	public static void run(){
		int PORT = 8765;
		try {
			final WebServer webServer = new WebServer(PORT);
			XmlRpcStreamServer server = webServer.getXmlRpcServer();

			PropertyHandlerMapping phm = new PropertyHandlerMapping();
			phm.addHandler("FindBugsPlugin", FindBugsPlugin.class);
			server.setHandlerMapping(phm);

			XmlRpcServerConfigImpl serverConfig = (XmlRpcServerConfigImpl) server.getConfig();
	        serverConfig.setEnabledForExtensions(true);
	        serverConfig.setContentLengthOptional(false);

	        FindBugsPlugin.init();
	        webServer.start();
			System.out.println("FindBugsPlugin starting....");
			
			Thread hook = new Thread(){
			    public void run() {
			    	webServer.shutdown();
			    	System.out.println("FindBugsPlugin Shutdown !");
			   }
			};
			Runtime.getRuntime().addShutdownHook(hook);
		} catch(Exception e){
			System.out.println("FindBugsPlugin error!");
		}
	}
}
