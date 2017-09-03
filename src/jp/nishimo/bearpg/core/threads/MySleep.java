package jp.nishimo.bearpg.core.threads;

public class MySleep {
	
	public static void exec(int ms){
		try {
			Thread.sleep(ms);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
