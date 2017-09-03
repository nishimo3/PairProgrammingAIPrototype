package jp.nishimo.bearpg.core.information;

public class NewsInfo {
	private String title;
	private String link;
	private String pubData;
	
	public NewsInfo(String _title, String _link, String _pubData){
		title = _title;
		link = _link;
		pubData = _pubData;
	}
	
	public boolean eq(NewsInfo _newInfo){
		if(title.equals(_newInfo.title) && pubData.equals(_newInfo.pubData)){
			return true;
		}
		return false;
	}
	
	public void print(){
		System.out.println(title + " " + pubData + " " + link);
	}

	public String getTitle(){ return title; }	
	public String getLink(){ return link; }	
	public String getPubData(){ return pubData; }
}
