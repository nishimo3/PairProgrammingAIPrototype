package jp.nishimo.bearpg.core.conversation;

import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import com.fasterxml.jackson.databind.ObjectMapper;

import jp.nishimo.bearpg.config.Configuration;
import jp.nishimo.bearpg.core.notify.UserNotifier;

public class DocomoTalker {
	private static final String url = "https://api.apigw.smt.docomo.ne.jp/dialogue/v1/dialogue?APIKEY=";
	
	public static class TalkerData {
		public String utt;
		public String yomi;
		public String mode;
		public String da;
		public String context;
	}
	
	public DocomoTalker(){
	}
	
	public void exec(){
		if(!Configuration.enableFlgs.getOrDefault("enableTalker", false)){
			// Nothing to do
			return;
		}
		
	   	UserNotifier userNotifier = new UserNotifier();
		String message = "ご用件は何でしょうか？";
		while(true){
			if(!message.equals("")){
			   	String reply = userNotifier.notifyWithReply(message, UserNotifier.IMAGE_QUESTION);
			   	if(!reply.equals("")){
			   		message = send(reply);
			   	} else {
			   		break;
			   	}
			} else {
				break;
			}
		}
	}
	
	public String send(String sentence){
		String ret = "";
		try{
			JSONObject sendData = new JSONObject();
			sendData.put("utt", sentence);
			
			CloseableHttpClient client = HttpClients.createDefault();
			StringEntity body = new StringEntity(sendData.toString(), "UTF-8");
			HttpPost post = new HttpPost(url + Configuration.docomoTalkerApiKey);
			post.setEntity(body);
			
			CloseableHttpResponse response = client.execute(post);
			int sc = response.getStatusLine().getStatusCode();
			if(sc == HttpStatus.SC_OK){
				HttpEntity entity = response.getEntity();
				String html = EntityUtils.toString(entity, "UTF-8");

				ObjectMapper mapper = new ObjectMapper();
				TalkerData talkerData = mapper.readValue(html, TalkerData.class);
				ret = talkerData.utt;
			}
			client.close();
		}catch(Exception e){
			e.printStackTrace();
		}
		return ret;
	}
}
