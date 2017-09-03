package jp.nishimo.bearpg.core.information;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import jp.nishimo.bearpg.PairProgrammingAI;
import jp.nishimo.bearpg.core.plugins.base.MessageInfo;

public class GoogleNewsUpdater {
	private static final String topicRssUrl = "https://news.google.com/news?hl=ja&ned=us&ie=UTF-8&oe=UTF-8&output=rss&topic=t";
	private static final String KeyRssUrl = "https://news.google.com/news?hl=ja&ned=us&ie=UTF-8&oe=UTF-8&output=rss&q=programming";
	
	private static final int NOTIFY_INTERVAL = 15 * 60; // 15min
	private static final int RAND_MAX = 4;
	
	private List<NewsInfo> newsInfos;
	private boolean isTopic = false;
	private int timer = 0;
	private int nextNotifyTime = 0;
	
	public GoogleNewsUpdater(){
		newsInfos = new ArrayList<NewsInfo>();
		isTopic = false;
		timer = 0;
		nextNotifyTime = ((new Random()).nextInt(RAND_MAX) + 1) * NOTIFY_INTERVAL;
	}
	
	public void exec(){
		if(timer++ > nextNotifyTime){
			NewsInfo newsInfo = getNewsInfo();
			if(newsInfo != null){
				newsInfo.print();
				MessageInfo messageInfo = new MessageInfo(newsInfo.getTitle(), null, newsInfo.getLink(), MessageInfo.NotifyType.LINK);
				PairProgrammingAI.notifyServer.setMessage(messageInfo);
			}			
			timer = 0;
			nextNotifyTime = ((new Random()).nextInt(RAND_MAX) + 1) * NOTIFY_INTERVAL;
		}
	}
	
	private NewsInfo getNewsInfo(){
		try {
			String rssUrl = (isTopic) ? topicRssUrl : KeyRssUrl;
			isTopic = (isTopic) ? false : true;

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(rssUrl);

            Element root = document.getDocumentElement();
            NodeList itemlist = root.getElementsByTagName("item");
            
            for(int i = 0; i < itemlist.getLength(); i++){
                Element element = (Element)itemlist.item(i);
                NodeList itemTitle = element.getElementsByTagName("title");
                NodeList itemLink = element.getElementsByTagName("link");
                NodeList itemPubDate = element.getElementsByTagName("pubDate");
                
                String title = itemTitle.item(0).getFirstChild().getNodeValue();
                String link = itemLink.item(0).getFirstChild().getNodeValue();
                String pubDate = itemPubDate.item(0).getFirstChild().getNodeValue();

                NewsInfo newNewsInfo = new NewsInfo(title, link, pubDate);
                boolean findFlg = false;
                for(int j = 0; j < newsInfos.size(); j++){
                	NewsInfo newInfo = newsInfos.get(j);
                	if(newInfo.eq(newNewsInfo)){
                		findFlg = true;
                		break;
                	}
                }
                if(!findFlg){
                	newsInfos.add(newNewsInfo);
                	return newNewsInfo;
                }
            }
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
		return null;
	}
}
