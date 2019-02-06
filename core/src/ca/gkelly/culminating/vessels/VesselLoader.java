package ca.gkelly.culminating.vessels;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class VesselLoader {
	public static ArrayList<Vessel> vessels = new ArrayList<Vessel>();
	public static ArrayList<Mount> mounts = new ArrayList<Mount>();
	
	public static void load() {
		File foo = new File("assets/vessels/PT.json");
		FileReader fr;
		try {
			fr = new FileReader(foo);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return;
		}
		BufferedReader br = new BufferedReader(fr);
	 
		String text = "";
		String line;
		try {
			while((line=br.readLine()) != null) {
				text += line;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println(text);
		
		JSONObject rootJSON;
		
		try {
			rootJSON = (JSONObject) new JSONParser().parse(text);
		} catch (org.json.simple.parser.ParseException e) {
			e.printStackTrace();
			return;
		}
		
		String name = (String) rootJSON.get("name");
		JSONArray mountArray = ((JSONArray) rootJSON.get("mountPoints"));
		
		MountPoint[] mounts = new MountPoint[mountArray.size()];
		for(int i = 0; i<mountArray.size(); i++) {
			JSONObject mountJSON =  (JSONObject) mountArray.get(i);
			
			int x = Math.toIntExact((long) mountJSON.get("x"));
			int y = Math.toIntExact((long) mountJSON.get("y"));
			
			MountPoint.Type t = null;
			
			switch((String) mountJSON.get("type")) {
			case "light":
				t=MountPoint.Type.LIGHT;
				break;

			case "medium":
				t=MountPoint.Type.MEDIUM;
				break;

			case "heavy":
				t=MountPoint.Type.HEAVY;
				break;
			
			}
			
			mounts[i] = new MountPoint(x, y, t);
		}
		
		Vessel v = new Vessel(name, mounts);
		
		vessels.add(v);
		System.out.println("Loaded");
	}
}
