package jp.nishimo.bearpg.core.notify;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import jp.nishimo.bearpg.config.Configuration;
import jp.nishimo.bearpg.core.plugins.base.MessageInfo;
import jp.nishimo.bearpg.utils.Command;

public class UserNotifier {
	public static final String IMAGE_BASE1    = "duck_base1.png";
	public static final String IMAGE_BASE2    = "duck_base2.png";
	public static final String IMAGE_MOGAKU   = "duck_mogaku.png";
	public static final String IMAGE_MOZIMOZI = "duck_mozimozi.png";
	public static final String IMAGE_OH       = "duck_oh.png";
	public static final String IMAGE_QUESTION = "duck_question.png";
	public static final String IMAGE_TSUNDERE = "duck_tsundere.png";
	public static final String IMAGE_IDOL     = "duck_idol.png";
	public static final List<String> randImageLists = Arrays.asList(IMAGE_BASE1, IMAGE_BASE2, IMAGE_MOGAKU, IMAGE_MOZIMOZI, IMAGE_OH, IMAGE_TSUNDERE);
	
	public UserNotifier(){
	}
	
	public void notify(String message, String imageFilePath){
		fire(message, imageFilePath, "", MessageInfo.NotifyType.NOTHING);
	}
	
	public String notifyWithReply(String message, String imageFilePath){
		String[] resultNewLine = fire(message, imageFilePath, "", MessageInfo.NotifyType.REPLY);
		String data = "";
		for(int i = 0; i < resultNewLine.length; i++){
			if(!resultNewLine[i].isEmpty()){
				data = data + resultNewLine[i];
			}
		}
		return data;
	}
	
	public void notifyWithLink(String message, String imageFilePath, String link){
		fire(message, imageFilePath, link, MessageInfo.NotifyType.LINK);
	}
	
	private String[] fire(String message, String imageFilePath, String link, MessageInfo.NotifyType kind){
		String title = "ぺだっくくん";
		if(imageFilePath == null){
			Random rnd = new Random();
			int index = rnd.nextInt(randImageLists.size());
			imageFilePath = randImageLists.get(index);
		}

		Command command = new Command();
		String commandStr = "";
		switch(kind){
		case NOTHING:
			commandStr = Configuration.nodeCommandPath + "node " + Configuration.nodeProgramDir + "/notify.js " + title + " \"" + message + "\" \"" + imageFilePath + "\"";
			break;
		case REPLY:
			commandStr = Configuration.nodeCommandPath + "node " + Configuration.nodeProgramDir + "/reply.js " + title + " \"" + message + "\" \"" + imageFilePath + "\"";
			break;
		case LINK:
			commandStr = Configuration.nodeCommandPath + "node "  + Configuration.nodeProgramDir + "/link.js \"" + title + "\" \"" + message + "\" \"" + imageFilePath + "\" \"" + link + "\"";
			break;
		default:
			break;
		}
		if(!commandStr.equals("")){
			String[] commands = {"/bin/sh", "-c", commandStr};
			String result = command.commandExec(commands);
			return result.split("\n");
		} else {
			return null;
		}
	}
}
