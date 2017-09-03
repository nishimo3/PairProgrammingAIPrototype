package jp.nishimo.bearpg.core.watch;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jp.nishimo.bearpg.core.parse.KeywordExtractor;
import jp.nishimo.bearpg.utils.Util;

public class FileWatcher {
	
	public class FileWatcherThread extends Thread {
		private String filepath = "";
		private boolean isRunning = false;
		private Map<WatchKey, Path> keys;
		
		private List<String> changedSourceFiles;
		private List<String> changedClassFiles;
		
		public FileWatcherThread(String _filepath){
			filepath = _filepath;
			isRunning = false;
			keys = new HashMap<WatchKey, Path>();
			
			changedSourceFiles = new ArrayList<String>();
			changedClassFiles = new ArrayList<String>();
		}
		
		public void run(){
			if(filepath.equals("")){
				return;
			}
			
			try {
				List<Path> fileLists = new ArrayList<Path>();
				fileLists.add((new File(filepath)).toPath());
				readFolder(new File(filepath), fileLists);
				
				WatchService watcher = FileSystems.getDefault().newWatchService();
				for(int i = 0; i < fileLists.size(); i++){
					Path dir = fileLists.get(i);
					registerKeys(watcher, dir);
				}
				isRunning = true;
				
				while(isRunning){
					try {
						WatchKey key = watcher.take();
						for(WatchEvent<?> event : key.pollEvents()) {
							if(event.kind() == StandardWatchEventKinds.OVERFLOW){
								continue;
							}
							
							WatchEvent<Path> ev = (WatchEvent<Path>)event;
							Path dir = keys.get(key);
							Path name = ev.context();
							Path child = dir.resolve(name);
							
							if(Files.isDirectory(child, LinkOption.NOFOLLOW_LINKS)) {
								System.out.println("Directory:" + child.toString() + " " + event.kind().name());
				                if(event.kind() == StandardWatchEventKinds.ENTRY_CREATE){
									registerKeys(watcher, child);
				                }
							} else {
								System.out.println("Files:" + child.toString() + " " + event.kind().name());
								if((event.kind() == StandardWatchEventKinds.ENTRY_CREATE) || (event.kind() == StandardWatchEventKinds.ENTRY_MODIFY)){
									String filepath = child.toString();
									changeFile(filepath);
								} else if(event.kind() == StandardWatchEventKinds.ENTRY_DELETE){
									// 不要な情報を削除する
									unregisterKeys(child);
								}
							}
						}
						key.reset();
//						printKeys();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		public void finish(){
			if(isRunning){
				isRunning = false;
			}
		}
		
		private void changeFile(String filepath){
			// Java Only
			String suffix = Util.getSuffix(filepath);
			if(suffix.equals("java")){
				changedSourceFiles.add(filepath);
			} else if(suffix.equals("class")){
				changedClassFiles.add(filepath);
			}
		}

		private void registerKeys(WatchService watcher, Path path){
			try {
				WatchKey watchKey = path.register(watcher, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_MODIFY);
				keys.put(watchKey, path);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		private void unregisterKeys(Path child){
			List<WatchKey> delTarget = new ArrayList<WatchKey>();
			for(WatchKey k : keys.keySet()){
				Path path = keys.get(k);
				String pathName = path.toString();
				String deleteName = child.toString();
				String deleteName2 = deleteName + "/";
				if(pathName.equals(deleteName) || (pathName.startsWith(deleteName2))){
					delTarget.add(k);
				}
			}
			for(WatchKey k : delTarget){
				keys.remove(k);										
			}
		}
		
		private void printKeys(){
			System.out.println("------------------ KEYS ------------------------------------");
			for(WatchKey k : keys.keySet()){
				System.out.println(keys.get(k));
			}
		}
		
		private void readFolder(File dir, List<Path> retfiles){
			File[] files = dir.listFiles();
			
			if(files == null){
				return ;
			}
			
			for(File file : files){
				if(file.exists() == false){
					continue;
				} else if(file.isDirectory()){
					retfiles.add(file.toPath());
					readFolder(file, retfiles);
				}
			}
		}
	}
	
	private FileWatcherThread watchFileThread = null;
	
	public FileWatcher(){
	}
	
	public boolean start(String filepath){
		watchFileThread = new FileWatcherThread(filepath);
		watchFileThread.start();
		return watchFileThread.isRunning;
	}
	
	public void reset(){
		watchFileThread.changedSourceFiles.clear();
		watchFileThread.changedClassFiles.clear();
	}
	
	public List<String> getChangedSourceFiles(){
		return watchFileThread.changedSourceFiles;
	}
	
	public List<String> getChangedClassFiles(){
		return watchFileThread.changedClassFiles;
	}
	
	public void end(){
		if(watchFileThread != null){
			watchFileThread.finish();
		}
	}
}
