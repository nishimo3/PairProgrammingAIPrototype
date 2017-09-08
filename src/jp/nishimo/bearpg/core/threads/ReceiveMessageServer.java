package jp.nishimo.bearpg.core.threads;

import org.apache.xmlrpc.server.PropertyHandlerMapping;
import org.apache.xmlrpc.server.XmlRpcServerConfigImpl;
import org.apache.xmlrpc.server.XmlRpcStreamServer;
import org.apache.xmlrpc.webserver.WebServer;

import jp.nishimo.bearpg.PairProgrammingAI;
import jp.nishimo.bearpg.core.plugins.base.MessageInfo;

public class ReceiveMessageServer {
	private boolean isRunning = false;
	private final WebServer webServer;
	private final int PORT = 5678;
	
	private static void init(){
		System.out.println("----------- ReceiveMessageServer static INIT");
	}
	
	public ReceiveMessageServer(){
		webServer = new WebServer(PORT);
	}

	public String setMessage(String message) {
		System.out.println("----------- ReceiveMessageServer setMessage " + message);
		MessageInfo messageInfo = new MessageInfo(message, null, "", MessageInfo.NotifyType.NOTHING);
		PairProgrammingAI.notifyServer.setMessage(messageInfo);		
		return message;
	}
	
	public boolean start(){	
		if(!this.isRunning){
			try {
				XmlRpcStreamServer server = webServer.getXmlRpcServer();
				
				PropertyHandlerMapping phm = new PropertyHandlerMapping();
				phm.addHandler("ReceiveMessageServer", ReceiveMessageServer.class);
				server.setHandlerMapping(phm);

				XmlRpcServerConfigImpl serverConfig = (XmlRpcServerConfigImpl) server.getConfig();
		        serverConfig.setEnabledForExtensions(true);
		        serverConfig.setContentLengthOptional(false);

		        ReceiveMessageServer.init();
		        webServer.start();
				System.out.println("ReceiveMessageServer starting....");
				
				Thread hook = new Thread(){
				    public void run() {
				    	webServer.shutdown();
				    	System.out.println("ReceiveMessageServer Shutdown !");
				   }
				};
				Runtime.getRuntime().addShutdownHook(hook);
				this.isRunning = true;
			} catch(Exception e){
				System.out.println("ReceiveMessageServer error!");
			}
		}
		return this.isRunning;
	}
	
	public void end(){
		if(this.isRunning){
	    	webServer.shutdown();
			this.isRunning = false;
		}
	}
}
