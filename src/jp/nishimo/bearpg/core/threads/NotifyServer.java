package jp.nishimo.bearpg.core.threads;

import java.util.ArrayList;
import java.util.List;

import jp.nishimo.bearpg.core.notify.UserNotifier;
import jp.nishimo.bearpg.core.plugins.base.MessageInfo;

public class NotifyServer {
		
	public class NotifyServerThread extends Thread{
		private boolean isRunning = false;
		private List<MessageInfo> messages;
		
		public NotifyServerThread(){
			isRunning = false;
			messages = new ArrayList<MessageInfo>();
		}
		
		public void run(){
			isRunning = true;
			
			while(isRunning){
				if(messages.size() > 0){
					MessageInfo message = messages.get(0);
					UserNotifier userNotifier = new UserNotifier();
					switch(message.getNotifyType()){
					case NOTHING:
						userNotifier.notify(message.getMessage(), message.getImageFilePath());
						break;
					case REPLY:
						userNotifier.notifyWithReply(message.getMessage(), message.getImageFilePath());
						break;
					case LINK:
						userNotifier.notifyWithLink(message.getMessage(), message.getImageFilePath(), message.getLink());
						break;
					default:
						break;
					}
					messages.remove(0);
				}
				MySleep.exec(1000);
			}
		}
		
		public void setMessage(MessageInfo message){
			messages.add(message);
		}
		
		public void finish(){
			if(isRunning){
				isRunning = false;
			}
		}
	}
	
	private NotifyServerThread notifyServerThread = null;
	
	public NotifyServer(){
	}
	
	public boolean start(){
		notifyServerThread = new NotifyServerThread();
		notifyServerThread.start();
		return notifyServerThread.isRunning;
	}
	
	public void setMessage(MessageInfo messageInfo){
		notifyServerThread.setMessage(messageInfo);
	}
	
	public void end(){
		if(notifyServerThread != null){
			notifyServerThread.finish();
		}
	}
}
