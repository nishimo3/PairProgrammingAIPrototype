package jp.nishimo.bearpg.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class Util {
	
	public static String getSuffix(String name){
		if(name != null){
			int dot = name.lastIndexOf(".");
			if(dot != -1){
				return name.substring(dot + 1);
			}
		}
		return null;
	}
	
	public static String read(String filepath){
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
	
	public static void write(String filepath, String str){
		try{
			File outFile = new File(filepath);
			FileWriter out = new FileWriter(outFile);
			out.write(str);
			out.close();
		} catch(IOException e) {
        }
	}
	
}
