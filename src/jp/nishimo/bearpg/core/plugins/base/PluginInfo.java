package jp.nishimo.bearpg.core.plugins.base;

public class PluginInfo {
	public enum PluginKind {
		Editor,
		VersionManagement,
		CodeAnalysis,
		CI,
		Search,
	}
	public String pName;
	public String pExplain;
	public String pVer;
	public PluginKind pKind;
}
