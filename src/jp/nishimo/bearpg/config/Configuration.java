package jp.nishimo.bearpg.config;

import java.util.HashMap;
import java.util.ResourceBundle;

import jp.nishimo.bearpg.utils.Util;

public class Configuration {
	// Program
	public static String nodeCommandPath = "/usr/local/bin/";
	
	// Node Program Directory
	public static String nodeProgramDir = "";
	
	// Swift Program Directory
	public static String swiftProgramDir = "";
	
	// Property Files
	public static String pluginListFilePath = "configuration/pluginList.txt";
	public static String appListFilePath = "configuration/appList.txt";
	
	// Parsing Option Files
	public static String javaSyntaxFilePath = "configuration/java.syntax";
	
	// Spell Check Option Files
	public static String spellCheckConfigFilePath = "configuration/english.txt";
	
	// FindBugs
	public static String findBugsDir = "";
	
	// Settings Configure File
	private static String settingFilePath = "configuration/setting.txt";
	
	// Docomo Talker apiKey
	public static String docomoTalkerApiKey = "";
	
	/* 機能の有効・無効 
	 * 
	 * 疲労チェック enableCheckWorkTime
	 * Googleニュース enableGoogleNews
	 * 静的解析 enableStaticAnalysis
	 * Qiita & StackOverflow 検索結果 enableSearchDevelopQA
	 * Api Reciper
	 * 雑談機能 enableTalker
	 * */
	public static HashMap<String, Boolean> enableFlgs;
	
	public static void load(){
		loadPaths();
		loadSettingParams();
	}
	
	public static void save(){
		saveSettingParams();
	}
	
	private static void loadPaths(){
		ResourceBundle rb = ResourceBundle.getBundle("resource");
		nodeCommandPath = rb.getString("nodeCommandPath");
		nodeProgramDir = rb.getString("nodeProgramDir");
		swiftProgramDir = rb.getString("swiftProgramDir");
		pluginListFilePath = rb.getString("pluginListFilePath");
		appListFilePath = rb.getString("appListFilePath");
		javaSyntaxFilePath = rb.getString("javaSyntaxFilePath");
		spellCheckConfigFilePath = rb.getString("spellCheckConfigFilePath");
		findBugsDir = rb.getString("findBugsDir");
		settingFilePath = rb.getString("settingFilePath");
		docomoTalkerApiKey = rb.getString("docomoTalkerApiKey");
	}
	
	private static void loadSettingParams(){
		enableFlgs = new HashMap<String, Boolean>();
		String data = Util.read(Configuration.settingFilePath);
		String[] dataLines = data.split("\n");
		for(int i = 0; i < dataLines.length; i++){
			String[] param =  dataLines[i].split(",");
			if(param.length == 2){
				enableFlgs.put(param[0], Boolean.valueOf(param[1]));
			}
		}
	}
	
	private static void saveSettingParams(){
		String data = "";
		for(String key : enableFlgs.keySet()){
			data = data + key + "," + enableFlgs.getOrDefault(key, false) + "\n";
		}
		Util.write(Configuration.settingFilePath, data);
	}
}
