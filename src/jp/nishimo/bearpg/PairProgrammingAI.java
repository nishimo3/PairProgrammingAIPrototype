package jp.nishimo.bearpg;

import java.awt.AWTException;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.swing.JFileChooser;

import jp.nishimo.bearpg.config.Configuration;
import jp.nishimo.bearpg.core.conversation.DocomoTalker;
import jp.nishimo.bearpg.core.plugins.base.MessageInfo;
import jp.nishimo.bearpg.core.threads.NotifyServer;
import jp.nishimo.bearpg.core.threads.PluginServer;
import jp.nishimo.bearpg.core.threads.ReceiveMessageServer;
import jp.nishimo.bearpg.core.threads.WatchServer;
import jp.nishimo.bearpg.core.watch.FileWatcher;
import jp.nishimo.bearpg.plugins.FindBugsPlugin;

public class PairProgrammingAI {
	public static FileWatcher fileWatcher = null;
	public static WatchServer watchServer = null;
	public static NotifyServer notifyServer = null;
	public static PluginServer pluginServer = null;
	public static ReceiveMessageServer receiveMessageServer = null;
	private boolean isActive = false;
	
	public PairProgrammingAI(){
		fileWatcher = new FileWatcher();
		watchServer = new WatchServer();
		notifyServer = new NotifyServer();
		pluginServer = new PluginServer();
		receiveMessageServer = new ReceiveMessageServer();
	}
	
	public void loop(){
		Configuration.load();
		
		receiveMessageServer.start();
		notifyServer.start();
		pluginServer.start();
		
		try {
			Image image = null;
			URL url = this.getClass().getClassLoader().getResource("image/duck_icon.png");
			image = ImageIO.read(url);

	        TrayIcon icon = new TrayIcon(image);
	        icon.setToolTip("ペアプログラミングAI");
	        icon.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					// Nothing to do
				}
	        });
	        icon.addMouseListener(new MouseAdapter() {
	            public void mouseClicked(MouseEvent e) {
	                 if(e.getClickCount() == 2 && e.getButton() == 1){
	                	 DocomoTalker talker = new DocomoTalker();
	                	 talker.exec();
	                 }
	            }
	        });

	        PopupMenu menu = new PopupMenu();
	        final MenuItem watchItem = new MenuItem("プロジェクトのお手伝いを開始する");
	        watchItem.addActionListener(new ActionListener(){
				@Override
				public void actionPerformed(ActionEvent e) {
					if(isActive){
						// Watching -> Waiting
						watchItem.setLabel("プロジェクトのお手伝いを開始する");
						fileWatcher.end();
						watchServer.end();
						isActive = false;
						
						String notifyMessage = "プログラミングのお手伝いを終了します";
						MessageInfo messageInfo = new MessageInfo(notifyMessage, null, "", MessageInfo.NotifyType.NOTHING);
						notifyServer.setMessage(messageInfo);
					} else {
						// Waiting -> Watching
						JFileChooser filechooser = new JFileChooser(".");
						filechooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
						filechooser.setDialogTitle("ペダックがお手伝いするプロジェクトを選択して下さい");
						watchItem.setEnabled(false);
						
						int selected = filechooser.showOpenDialog(null);
						if(selected == JFileChooser.APPROVE_OPTION){
							// Start WatchFolder
							File file = filechooser.getSelectedFile();
							String filepath = file.getAbsolutePath();
							fileWatcher.start(filepath);
							watchServer.start();
							
							// Change MenuTitle
							watchItem.setLabel("プロジェクトのお手伝いを終了する");
							isActive = true;
							
							String notifyMessage = "プログラミングのお手伝いを開始します";
							MessageInfo messageInfo = new MessageInfo(notifyMessage, null, "", MessageInfo.NotifyType.NOTHING);
							notifyServer.setMessage(messageInfo);
						}
						watchItem.setEnabled(true);
					}
				}
	        });
	        
	        MenuItem settingItem = new MenuItem("ペダックの設定を開く");
	        settingItem.addActionListener(new ActionListener(){
				@Override
				public void actionPerformed(ActionEvent e) {
					final SettingPanel dialog = new SettingPanel();
					dialog.setVisible(true);
				}
	        });
	        
	        MenuItem exitItem = new MenuItem("ペダックを終了する");
	        exitItem.addActionListener(new ActionListener(){
				@Override
				public void actionPerformed(ActionEvent e) {
					receiveMessageServer.end();
					notifyServer.end();
					pluginServer.end();
					Configuration.save();
					
					System.exit(0);
				}
	        });
	        menu.add(watchItem);
	        menu.add(settingItem);
	        menu.add(exitItem);
	        icon.setPopupMenu(menu);
	        SystemTray.getSystemTray().add(icon);
		} catch (IOException e1) {
			e1.printStackTrace();
		} catch (AWTException e1) {
			e1.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		FindBugsPlugin.run();
		
		PairProgrammingAI main = new PairProgrammingAI();
		main.loop();
	}
}