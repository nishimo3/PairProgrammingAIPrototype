package jp.nishimo.bearpg.core.watch;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import jp.nishimo.bearpg.PairProgrammingAI;
import jp.nishimo.bearpg.config.Configuration;
import jp.nishimo.bearpg.core.notify.UserNotifier;
import jp.nishimo.bearpg.core.plugins.base.MessageInfo;
import jp.nishimo.bearpg.utils.Command;

public class UseApp {
	
	public class AppInfo {
		private String appName;
		private int count;
		
		public AppInfo(String _appName){
			appName = _appName;
			count = 1;
		}
		
		public void inc(){
			count = count + 1;
		}
		
		public boolean eq(String _appName){
			if(appName.equals(_appName)){
				return true;
			}
			return false;
		}
		
		public String getAppName(){
			return appName;
		}
		
		public int getCount(){
			return count;
		}
	}
	
	private static final int TIMER_1MIN = 60;
	private static final int APP_RECORD_MAX =10; // 計測時間(分)
	
	private List<AppInfo> activeAppFor1min;
	private int timer1min = 0;
	
	private List<String> appHistoryUntil1hour;
	private boolean doNotNotifyAppFlg;
	private int doNotNotifyAppTime;
	
	private ActiveAppList activeAppList;

	public UseApp(){
		activeAppList = new ActiveAppList();
		activeAppFor1min = new ArrayList<AppInfo>();
		appHistoryUntil1hour = new ArrayList<String>();
	}
	
	public void start(){
		activeAppList.loadAppList();
		timer1min = 0;
		doNotNotifyAppFlg = false;
	}
	
	public void finish(){
		activeAppList.saveAppList();
	}
	
	public void exec(){
		registerActiveApp();
		
		timer1min++;
		if(timer1min >= TIMER_1MIN){
			// 起動中のアプリの一覧を更新
			activeAppList.updateApplicationAppList();
			String askTool = activeAppList.getNoAskTool();
			if(!askTool.equals("")){
				String message = "「" + askTool +  "」はプログラミングで使用するツールですか？";
				UserNotifier notifier = new UserNotifier();
				String ans = notifier.notifyWithReply(message, UserNotifier.IMAGE_QUESTION);
				if(!ans.equals("")){
					boolean ispTool = false;
					if(ans.toLowerCase().equals("yes")){
						ispTool = true;
					}
					activeAppList.setProgrammingTool(askTool, ispTool);
				}
			}

			// 1分間で最も使用されていたアプリを登録
			appHistoryUntil1hour.add(mostActiveAppNameFor1min());
			if(appHistoryUntil1hour.size() >= APP_RECORD_MAX){
				// 連続して長時間使用している場合は休憩を促す
				if(doNotNotifyAppFlg){
					doNotNotifyAppTime--;
					if(doNotNotifyAppTime <= 0){
						doNotNotifyAppFlg = false;
					}
				}
				if(!doNotNotifyAppFlg && isLongWork1hour()){
					// send
					sendMessage();
					doNotNotifyAppFlg = true;
					doNotNotifyAppTime = APP_RECORD_MAX;
				}
				appHistoryUntil1hour.remove(0);
			}
			activeAppFor1min.clear();
			timer1min = 0;
//			System.out.println(appHistoryUntil1hour);
//			System.out.println(doNotNotifyAppFlg);
//			System.out.println(doNotNotifyAppTime);
		}
	}
	
	private void registerActiveApp(){
		Command command = new Command();
//		String commandStr = "osascript -e 'name of (info for (path to frontmost application))'";
		String commandStr = "swift " + Configuration.swiftProgramDir + "/ActiveApp.swift";
		String[] commands = {"/bin/sh", "-c", commandStr};
		String result = command.commandExec(commands);
		String[] activeApps = result.split("\n");
		
		for(int i = 0; i < activeApps.length; i++){
			String name = activeApps[i];
			
			boolean findFlg = false;
			for(int j = 0; j < activeAppFor1min.size(); j++){
				AppInfo appInfo = activeAppFor1min.get(j);
				if(appInfo.eq(name)){
					appInfo.inc();
					findFlg = true;
					break;
				}
			}
			if(!findFlg){
				activeAppFor1min.add(new AppInfo(name));
			}
		}		
	}
	
	private String mostActiveAppNameFor1min(){
		int maxIndex = 0;
		int maxValue = 0;
		for(int i = 0; i < activeAppFor1min.size(); i++){
			AppInfo appInfo = activeAppFor1min.get(i);
			if(appInfo.getCount() > maxValue){
				maxIndex = i;
				maxValue = appInfo.getCount();
			}
		}
/*
		System.out.println("-------------------------");
		for(AppInfo info : activeAppFor1min){
			System.out.println(info.getAppName() + " " + info.getCount());
		}
		System.out.println("MAX INDEX:" + maxIndex);
		System.out.println("-------------------------");
*/
		AppInfo mostAppInfo = activeAppFor1min.get(maxIndex);
		return mostAppInfo.getAppName();
	}
	
	private boolean isLongWork1hour(){
		boolean ret = false;
		List<AppInfo> tmpAppInfos = new ArrayList<AppInfo>();
		
		for(int i = 0; i < appHistoryUntil1hour.size(); i++){
			String appName = appHistoryUntil1hour.get(i);
			boolean flg = false;
			for(int j = 0; j < tmpAppInfos.size(); j++){
				AppInfo appInfo = tmpAppInfos.get(j);
				if(appName.equals(appInfo.getAppName())){
					flg = true;
					appInfo.inc();
					break;
				}
			}
			if(!flg){
				tmpAppInfos.add(new AppInfo(appName));
			}
		}

		List<String> pToolLists = activeAppList.getProgrammingTool();
		int useAppCount = 0;
		for(int i = 0; i < tmpAppInfos.size(); i++){
			AppInfo appInfo = tmpAppInfos.get(i);
			for(int j = 0; j < pToolLists.size(); j++){
				if(appInfo.appName.equals(pToolLists.get(j))){
					useAppCount = useAppCount + appInfo.count;
				}
			}
		}
		double percent = (double)useAppCount / (double)appHistoryUntil1hour.size();
		if(percent >= 0.7){
			ret = true;
		}
		return ret;
	}
	
	private void sendMessage(){
		String message1_1 = "頑張って!!!";
		String message1_2 = "頑張って下さい";
		String message1_3 = "ファイト、いっぱーつ";
		List<String> message1 = Arrays.asList(message1_1, message1_2, message1_3);
		
		String message3_1 = "そろそろ、休憩しませんか？";
		String message3_2 = "コーヒーでも飲みませんか？";
		String message3_3 = "からだのストレッチをしましょう！";
		List<String> message3 = Arrays.asList(message3_1, message3_2, message3_3);
		
		Random rnd = new Random();
		int index = rnd.nextInt(3);
		String notifyMessage = message3.get(index);
		PairProgrammingAI.notifyServer.setMessage(new MessageInfo(notifyMessage, null, "", MessageInfo.NotifyType.NOTHING));
	}
}
