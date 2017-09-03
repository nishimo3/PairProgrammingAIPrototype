package jp.nishimo.bearpg;

import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Rectangle;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import jp.nishimo.bearpg.config.Configuration;

public class SettingPanel extends JDialog implements ChangeListener{
	
	private GridBagLayout gridBagLayout;
	private Container cont;
	
	public class CkBoxGroup {
		JLabel jLabel;
		JCheckBox ckBox; 

		public CkBoxGroup(JLabel _jLabel, JCheckBox _ckBox1){
			jLabel = _jLabel;
			ckBox = _ckBox1;
		}
	}
	private HashMap<String, String> convertKeys;
	private List<CkBoxGroup> chBoxGroups;
	
	public SettingPanel(){
		this.setTitle("設定パネル");
		GraphicsEnvironment env = java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment();
		Rectangle desktopBounds = env.getMaximumWindowBounds();

		int width = 400;
		int height = 200;
		int x = (int)(desktopBounds.getWidth() / (double)2) - (width / 2);
		int y = (int)(desktopBounds.getHeight() / (double)2) - (height / 2);
		this.setBounds(x, y, width, height);
		this.setAlwaysOnTop(true);
		
		gridBagLayout = new GridBagLayout();
		cont =  this.getContentPane();
		cont.setLayout(gridBagLayout);
		
		convertKeys = new HashMap<String, String>();
		convertKeys.put("疲労チェック", "enableCheckWorkTime");
		convertKeys.put("静的解析", "enableStaticAnalysis");
		convertKeys.put("Qiita & StackOverflow検索", "enableSearchDevelopQA");
		convertKeys.put("Googleニュース", "enableGoogleNews");
		convertKeys.put("雑談", "enableTalker");
		
		chBoxGroups = new ArrayList<CkBoxGroup>();
		chBoxGroups.add(createCheckBox("疲労チェック", 0));
		chBoxGroups.add(createCheckBox("静的解析", 1));
		chBoxGroups.add(createCheckBox("Qiita & StackOverflow検索", 2));
		chBoxGroups.add(createCheckBox("Googleニュース", 3));
		chBoxGroups.add(createCheckBox("雑談", 4));

		/*
		this.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e){
				super.windowClosing(e);
				System.out.println("Window Closing!");
			}
		});
		*/
	}
	
	private CkBoxGroup createCheckBox(String title, int grdiy){
		// Create CheckBox
		JLabel jLabel = new JLabel(title);
		jLabel.setHorizontalAlignment(JLabel.LEFT);		
		boolean isEnable = Configuration.enableFlgs.getOrDefault(convertKeys.get(title), false);
		
		JCheckBox ckBox; 
		if(isEnable){
			ckBox = new JCheckBox("Enable", true);
		} else {
			ckBox = new JCheckBox("Disable", false);
		}
		
		CkBoxGroup chBoxGroup = new CkBoxGroup(jLabel, ckBox);
		ckBox.addChangeListener(this);
		
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
	    gbc.gridy = grdiy;
	    gbc.anchor = GridBagConstraints.WEST;
	    gridBagLayout.setConstraints(jLabel, gbc);
	    
		gbc.gridx = 1;
	    gbc.gridy = grdiy;
	    gridBagLayout.setConstraints(ckBox, gbc);
	    
		cont.add(jLabel);
		cont.add(ckBox);

		return chBoxGroup;
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		JCheckBox ckBox = (JCheckBox)e.getSource();
		for(CkBoxGroup ckBoxGroup : chBoxGroups){
			if(ckBoxGroup.ckBox == ckBox){
				if(ckBox.isSelected()){
					ckBox.setText("Enable");
					Configuration.enableFlgs.put(convertKeys.get(ckBoxGroup.jLabel.getText()), true);
				} else {
					ckBox.setText("Disable");
					Configuration.enableFlgs.put(convertKeys.get(ckBoxGroup.jLabel.getText()), false);
				}
				break;
			}
		}
	}
}
