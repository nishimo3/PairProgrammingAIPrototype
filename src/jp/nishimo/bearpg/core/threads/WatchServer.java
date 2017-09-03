package jp.nishimo.bearpg.core.threads;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

import jp.nishimo.bearpg.PairProgrammingAI;
import jp.nishimo.bearpg.config.Configuration;
import jp.nishimo.bearpg.core.information.GoogleNewsUpdater;
import jp.nishimo.bearpg.core.parse.KeywordExtractor;
import jp.nishimo.bearpg.core.plugins.base.MessageInfo;
import jp.nishimo.bearpg.core.watch.UseApp;
import jp.nishimo.bearpg.utils.Util;

public class WatchServer {
	
	public class ChangedFileWatcher {
		
		public class ChangedFile {
			private static final int DEFAULT_UNUSED_TIME = 10; //10s間は同じファイルも変更に対して何も行わない(デフォルト)
			
			private int unusedTime = DEFAULT_UNUSED_TIME; 
			private List<String> files;
			private int count = 0;
			
			public ChangedFile(List<String> _files, int _unusedTime){
				files = new ArrayList<String>(_files);
				unusedTime = _unusedTime;
				count = _unusedTime;
			}

			public ChangedFile(List<String> _files){
				files = new ArrayList<String>(_files);
				count = unusedTime;
			}
		}
		
		private List<ChangedFile> unusedFiles;
		
		public ChangedFileWatcher(){
			unusedFiles = new ArrayList<ChangedFile>();
		}
		
		public void watch(List<String> datas){
			if(datas.size() != 0){
				boolean isFind = false;
				for(ChangedFile ccf : unusedFiles){
					if(isEqualSet(ccf.files, datas)){
						isFind = true;
					}
				}
				if(!isFind){
					apply(datas);
				}
			}
			
			if(unusedFiles.size() != 0){
				// 一定期間に変更されたファイルは再度，確認はしないようにする
				List<ChangedFile> delete = new ArrayList<ChangedFile>();
				for(ChangedFile ccf : unusedFiles){
					if(ccf.count-- < 0){
						delete.add(ccf);
					}
				}
				for(ChangedFile ccf : delete){
					unusedFiles.remove(ccf);
				}
			}
		}
		
		private boolean isEqualSet(List<String> a, List<String> b){
			boolean ret = false;
			if(a.size() == b.size()){
				int counter = 0;
				for(String sa : a){
					for(String sb : b){
						if(sa.equals(sb)){
							counter++;
							break;
						}
					}
				}
				if(counter == a.size()){
					ret = true;
				}
			}
			return ret;
		}
		
		private void apply(List<String> datas){
			String data = datas.get(0);
			String suffix = Util.getSuffix(data);
			if(suffix.equals("java")){
 				KeywordExtractor extractor = new KeywordExtractor();
				String url = extractor.execSearchKeyword(data);
				MessageInfo messageInfo = new MessageInfo("このサイトを参考するといいかもしれませんよ", null, url, MessageInfo.NotifyType.LINK);
    			PairProgrammingAI.notifyServer.setMessage(messageInfo);
			} else if(suffix.equals("class")){
				String strDatas = String.join(" ", datas);
				strDatas = strDatas.replaceAll("\\$", Matcher.quoteReplacement("\\$"));
				PairProgrammingAI.pluginServer.setMessage("FindBugsPlugin", strDatas);
				unusedFiles.add(new ChangedFile(datas));
			}
		}
	}
	
	public class WatchServerThread extends Thread {
		private UseApp useApp;
		private GoogleNewsUpdater newsUpdater;
		private boolean isRunning = false;
		
		private ChangedFileWatcher classFileWatcher;
		private ChangedFileWatcher sourceFileWatcher;
		
		public WatchServerThread(){
			useApp = new UseApp();
			newsUpdater = new GoogleNewsUpdater();
			isRunning = false;
			
			classFileWatcher = new ChangedFileWatcher();
			sourceFileWatcher = new ChangedFileWatcher();
		}
		
		public void run(){
			isRunning = true;
			useApp.start();
			
			while(isRunning){
				// 使用アプリケーションから連続作業時間を特定
				if(Configuration.enableFlgs.getOrDefault("enableCheckWorkTime", false)){
					useApp.exec();
				}
				
				// 静的解析 & QAサイトの検索
				watchFiles();
				
				// 新着ニュースを通知
				if(Configuration.enableFlgs.getOrDefault("enableGoogleNews", false)){
					newsUpdater.exec();
				}
				MySleep.exec(1000);
			}
			useApp.finish();
		}
		
		public void finish(){
			if(isRunning){
				isRunning = false;
			}
		}

		private void watchFiles(){
			if(Configuration.enableFlgs.getOrDefault("enableStaticAnalysis", false)){
				List<String> changedClassFiles = PairProgrammingAI.fileWatcher.getChangedClassFiles();
				classFileWatcher.watch(changedClassFiles);
			}
			if(Configuration.enableFlgs.getOrDefault("enableSearchDevelopQA", false)){
				List<String> changedSourceFiles = PairProgrammingAI.fileWatcher.getChangedSourceFiles();
				sourceFileWatcher.watch(changedSourceFiles);
			}
			PairProgrammingAI.fileWatcher.reset();
		}
	}
	
	private WatchServerThread watchUserOperationThread = null;
	
	public WatchServer(){
	}
	
	public boolean start(){
		watchUserOperationThread = new WatchServerThread();
		watchUserOperationThread.start();
		return watchUserOperationThread.isRunning;
	}
	
	public void end(){
		if(watchUserOperationThread != null){
			watchUserOperationThread.finish();
		}
	}
}
