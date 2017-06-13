package tacKBP;

import java.util.HashMap;

public class KBPEntity {
	private String wiki_title;
	private String type;
	private String entId;
	private String name;
	private String entclass;
	private HashMap<String, String> properties;
	private HashMap<String,String> links;
	
	public String getWiki_title() {
		return wiki_title;
	}
	public void setWiki_title(String wiki_title) {
		this.wiki_title = wiki_title;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getEntId() {
		return entId;
	}
	public void setEntId(String entId) {
		this.entId = entId;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getEntclass() {
		return entclass;
	}
	public void setEntclass(String entclass) {
		this.entclass = entclass;
	}
	public HashMap<String, String> getProperties() {
		return properties;
	}
	public void setProperties(HashMap<String, String> properties) {
		this.properties = properties;
	}
	public HashMap<String, String> getLinks() {
		return links;
	}
	public void setLinks(HashMap<String, String> links) {
		this.links = links;
	}
}
