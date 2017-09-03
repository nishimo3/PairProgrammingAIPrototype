package jp.nishimo.bearpg.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class Command {	
	public String commandExec(String[] commands){
		Runtime runtime = Runtime.getRuntime();
		try {
			Process p = runtime.exec(commands);
			InputStream in = p.getInputStream();
			return inputStreamToString(in);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private String inputStreamToString(InputStream in) throws IOException{        
	    BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
	    StringBuffer buf = new StringBuffer();
	    String str;
	    while ((str = reader.readLine()) != null) {
	            buf.append(str);
	            buf.append("\n");
	    }
	    return buf.toString();
	}
}
