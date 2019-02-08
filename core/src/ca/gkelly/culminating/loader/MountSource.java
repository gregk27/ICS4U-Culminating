package ca.gkelly.culminating.loader;

import org.json.simple.JSONObject;

import com.badlogic.gdx.graphics.Texture;

public class MountSource {
	public int x;
	public int y;
	
	public Texture texture;
	
	public int health;
	public final int MAX_HEALTH;
	
	public final int BASE_COST;
	
	public MountPoint.Type type;
	
	public String name;
	
	public MountSource(String texturePath, JSONObject json){
		name = (String) json.get("name");

		texture = new Texture(texturePath);
		
		JSONObject mountPoint = ((JSONObject) json.get("mountPoint"));
		x = Math.toIntExact((Long) mountPoint.get("x"));
		y = Math.toIntExact((Long) mountPoint.get("y"));
		
		MAX_HEALTH = Math.toIntExact((Long) json.get("maxHealth"));
		BASE_COST = Math.toIntExact((Long) json.get("cost"));
		
		
		type = MountPoint.Type.valueOf(((String) json.get("type")).toUpperCase());
	}
}
