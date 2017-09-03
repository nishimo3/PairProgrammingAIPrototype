package jp.nishimo.bearpg.core.parse;

import java.io.BufferedReader;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

import jp.nishimo.bearpg.config.Configuration;
import jp.nishimo.bearpg.utils.CheckEnglishSpell;

public class KeywordExtractor {
	
	public static class AppearData {
		public String word;
		public int count;
		
		public AppearData(String _word){
			word = _word;
			count = 1;
		}
		
		public void inc(){
			count++;
		}
		
		public void print(){
			System.out.println("[" + count + "]" + word);
		}
	}
	
	public static class AppearDataComparator implements Comparator<AppearData> {
		public int compare(AppearData s1, AppearData s2) {
			int v1 = s1.count;
			int v2 = s2.count;
	        if (v1 < v2) {
	            return 1;
	        } else if (v1 == v2) {
	            return 0;
	        } else {
	            return -1;
	        }
		}
	}
	
	public void exec(String filepath){
		String key = splitKeyword(filepath);
		normalize(key);
	}
	
	public String execSearchKeyword(String filepath){
		String key = splitKeyword(filepath);
		List<AppearData> keywords = normalize(key);
		String keyStr = "";
		int size = 4;
		if(size > keywords.size()){
			size = keywords.size();
		}
		for(int i = 0; i < size; i++){
			AppearData appearData = keywords.get(i);
			keyStr = keyStr + appearData.word + "+";
		}
		keyStr = keyStr + "java";
		
		String ret = "";
		Random rnd = new Random();
        int rand = rnd.nextInt(2);
        if(rand == 0){
        	ret = "http://qiita.com/search?utf8=%E2%9C%93&sort=&q=" + keyStr;
        } else {
        	ret = "https://stackoverflow.com/search?q=" + keyStr;
        }
		return ret;
	}
	
	public void spellChecker(String filepath){
		CheckEnglishSpell ces = new CheckEnglishSpell();
		String key = splitKeyword(filepath);
		
		//重複を排除する、同時に数字のみ、記号のみは無視する
		List<String> newKeys = new ArrayList<String>();
		String[] keys = key.split(" ");
		for(int i = 0; i < keys.length; i++){
			// アルファベットのみ
			if(!keys[i].matches("[a-zA-Z]+")){
				continue;
			}

			boolean flg = false;
			for(int j = 0; j < newKeys.size(); j++){
				if(keys[i].equals(newKeys.get(j))){
					flg = true;
					break;
				}
			}
			if(!flg){
				newKeys.add(keys[i]);
			}
		}

		// スペルミスのチェックを行う
		for(int i = 0; i < newKeys.size(); i++){
			if(!ces.exec(newKeys.get(i))){
				System.out.println("[SPELL MISS]" + newKeys.get(i));
			}
		}
	}
	
	private String splitKeyword(String filepath){
		List<String> regexs = new ArrayList<String>();
		boolean executable = false;
		
		if(getSuffix(filepath).equals("java")){			
			String[] reservedWords = read(Configuration.javaSyntaxFilePath).split(",");
			List<String> aaa = Arrays.asList(reservedWords);
//			List<String> bbb = Arrays.asList("\\.", "<", ">", ",", "_", "@", "\\*", "\\+", "\\-", "/", ";", ":", "=", "!", "&", "|", "\\(", "\\)", "\\{", "\\}", "\\[", "\\]", "\"", "\n");
			List<String> bbb = Arrays.asList("\\.", "<", ">", ",", "_", "@", "\\*", "\\+", "\\-", "/", ";", ":", "=", "!", "\\(", "\\)", "\\{", "\\}", "\\[", "\\]", "\"", "\n");
			regexs.addAll(aaa);
			regexs.addAll(bbb);
			executable = true;
		}
		
		String newStr = "";
		if(executable){
			String file = readSource(filepath);
			List<String> keywords = getWordLists(file, regexs);
			for(int i = 0; i < keywords.size(); i++){
				String keyword = keywords.get(i);
				if(keyword.length() > 1){
					newStr = newStr + keyword + " ";
				}
			}
		}
		return newStr;
	}
	
	private List<AppearData> normalize(String newStr){
		// EnglishAnalyzer analyzer = new EnglishAnalyzer();
		Analyzer analyzer = new StandardAnalyzer();
		TokenStream tStream = analyzer.tokenStream("", new StringReader(newStr));
		
		List<AppearData> appearDatas = new ArrayList<AppearData>();
		try {
			tStream.reset();
			while(tStream.incrementToken()){
				CharTermAttribute attribute = tStream.getAttribute(CharTermAttribute.class);
                String word = attribute.toString();
                
                boolean findFlg = false;
                for(int i = 0; i < appearDatas.size(); i++){
                	AppearData appearData = appearDatas.get(i);
                	if(word.equals(appearData.word)){
                		appearData.inc();
                		findFlg = true;
                		break;
                	}
                }
                if(!findFlg){
                    appearDatas.add(new AppearData(word));
                }
			}
			tStream.end();
			tStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			analyzer.close();
		}
		
		Collections.sort(appearDatas, new AppearDataComparator());
		return appearDatas;
		/*
		for(int i = 0; i < appearDatas.size(); i++){
			AppearData appearData = appearDatas.get(i);
			appearData.print();
		}
		*/
	}
		
	private List<String> getWordLists(String sentence, List<String> regexs){
		String newSentence = sentence;

		for(int i = 0; i < regexs.size(); i++){
			String regex = regexs.get(i);
			
			Pattern p = Pattern.compile(regex);
			Matcher m = p.matcher(newSentence);
			newSentence = m.replaceAll(" ");
		}

		String[] words = newSentence.split(" ");		
		List<String> newWords = new ArrayList<String>();
		for(int i = 0; i < words.length; i++){
			if(isAllLowerOrUpperCase(words[i])){
				newWords.add(words[i].toLowerCase());
			} else {
				List<String> c = getWordSpliter(words[i]);
				if(c.size() > 0){
					newWords.addAll(c);
				}
			}
		}
		return newWords;
	}
	
	private boolean isAllLowerOrUpperCase(String str){
		if(!str.matches(".*[0-9].*")){
			if(str.toLowerCase().equals(str) || str.toUpperCase().equals(str)){
				return true;
			}
		}
		return false;
	}
	
	private static List<String> getWordSpliter(String name){
		List<String> ret = new ArrayList<String>();
		
		List<String> words = splitAlphaOrDigit(name);
		for(int i = 0; i < words.size(); i++){
//			System.out.println(words.get(i));
			List<String> tmp = getWordsCamelCase(words.get(i));
			ret.addAll(tmp);
		}
		return ret;
	}
	
	private static List<String> splitAlphaOrDigit(String name){
		List<String> ret = new ArrayList<String>();
		
		int oldUpperPos = 0;
		int oldKind = -1;
		for(int i = 0; i < name.length(); i++){
			int kind = getKind(name.charAt(i));
			
			if(kind != oldKind){
				String s = name.substring(oldUpperPos, i);
//				System.out.println(oldUpperPos + " - " + i + " " + s);
				if(!s.isEmpty()){
					ret.add(s);
				}
				oldUpperPos = i;
			}
			oldKind = kind;
		}
		
		if(oldUpperPos != name.length()){
			String s = name.substring(oldUpperPos, name.length());
			if(!s.isEmpty()){
				ret.add(s);
			}
		}
		return ret;
	}
	
	private static int getKind(char c){
		int kind = -1;
		if(Character.isAlphabetic(c)){
			kind = 0;
		} else if(Character.isDigit(c)){
			kind = 1;
		}
		return kind;
	}
	
	private static List<String> getWordsCamelCase(String name){
		List<String> ret = new ArrayList<String>();
		
		int oldUpperPos = 0;
		boolean isUpperConti = false;
		for(int i = 0; i < name.length() - 1; i++){
			boolean isAddWord = false;
			if(!isUpperConti){
				if(Character.isUpperCase(name.charAt(i))){
					isAddWord = true;
					if(Character.isUpperCase(name.charAt(i + 1))){
						isUpperConti = true;
					}
				}
			} else {
				if(Character.isLowerCase(name.charAt(i + 1))){
					isAddWord = true;
					isUpperConti = false;
				}
			}
			
			if(isAddWord){
				String s = name.substring(oldUpperPos, i);
				if(!s.isEmpty()){
					ret.add(s.toLowerCase());
				}
				oldUpperPos = i;
			}
		}
		if(oldUpperPos != name.length()){
			String s = name.substring(oldUpperPos, name.length());
			if(!s.isEmpty()){
				ret.add(s.toLowerCase());
			}
		}
		return ret;
	}
	
	private String read(String filepath){
		String ret = "";
		try{
			File file = new File(filepath);
			BufferedReader br = new BufferedReader(new FileReader(file));
			
			String str = br.readLine();
			while(str != null){
				ret = ret + str + "\n";
			    str = br.readLine();
			}
			br.close();
		} catch(IOException e) {
        }
		return ret;
	}
	
	private static String readSource(String filepath){
		String ret = "";
		try{
			File file = new File(filepath);
			BufferedReader br = new BufferedReader(new FileReader(file));
			
			String str = br.readLine();
			boolean isComment = false;
			while(str != null){
				if(!isComment && str.indexOf("/*") != -1){
					str = str.substring(0, str.indexOf("/*"));
					isComment = true;
				} else if(isComment && str.indexOf("*/") != -1){
					str = str.substring(str.indexOf("*/") + 2);
					isComment = false;
				} else if(isComment){
					str = "";
				}
				if(str.indexOf("//") != -1){
					str = str.substring(0, str.indexOf("//"));
				}
				
				if(!str.equals("")){
					ret = ret + str + "\n";					
				}
			    str = br.readLine();
			}
			br.close();
		} catch(IOException e) {
        }
		return ret;
	}
	
	private String getSuffix(String name){
		if(name != null){
			int dot = name.lastIndexOf(".");
			if(dot != -1){
				return name.substring(dot + 1);
			}
		}
		return null;
	}
}
