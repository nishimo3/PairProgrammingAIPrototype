package jp.nishimo.bearpg.utils;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jp.nishimo.bearpg.config.Configuration;
import pt.tumba.spell.SpellChecker;

public class CheckEnglishSpell {
	
	public CheckEnglishSpell(){
	}
	
	public boolean exec(String word){
		boolean ret = false;
		try {
			SpellChecker sc = new SpellChecker();
			sc.initialize(Configuration.spellCheckConfigFilePath);
			String newWord = sc.spellCheckWord(word);
			String[] xmlStr = parseSimpleXml(newWord);
			
			if(xmlStr[0].equals("misspell") || xmlStr[0].equals("suggestion")){
				ret = false;
			} else if(xmlStr[0].equals("plain")){
				ret = true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ret;
	}
	
	private String[] parseSimpleXml(String word){
		List<String> regexs = Arrays.asList("</", "<", ">");
		
		for(int i = 0; i < regexs.size(); i++){
			String regex = regexs.get(i);
			Pattern p = Pattern.compile(regex);
			Matcher m = p.matcher(word);
			word = m.replaceAll("###");
		}
		
		String[] ret = new String[2];
		String[] tmp = word.split("###");
		int j = 0;
		for(int i = 0; i < tmp.length; i++){
			if(!tmp[i].equals("")){
				if(j < 2){
					ret[j] = tmp[i];
				}
				j++;
			}
		}
		return ret;
	}
}
