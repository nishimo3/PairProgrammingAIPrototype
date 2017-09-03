package jp.nishimo.bearpg.core.plugins.base;

public class MessageInfo {
	public enum NotifyType {
		NOTHING,
		REPLY,
		LINK,
	}

	private String message;
	private String imageFilePath;
	private String link;
	private NotifyType notifyType;
	
	public MessageInfo(String _message, String _imageFilePath, String _link, NotifyType _notifyType){
		message = _message;
		imageFilePath = _imageFilePath;
		link = _link;
		notifyType = _notifyType;
	}
	
	public String getMessage(){
		return message;
	}
	
	public String getImageFilePath(){
		return imageFilePath;
	}
	
	public String getLink(){
		return link;
	}
	
	public NotifyType getNotifyType(){
		return notifyType;
	}
}
