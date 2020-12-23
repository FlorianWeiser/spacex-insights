package main;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;

import com.google.gson.JsonArray;
import com.google.gson.JsonParser;

// this class loads the required Json objects from the SpaceX API
public class DataLoader {
	
	// set url from which data is loaded
	private static final String BASE_URL = "https://api.spacexdata.com/v4/";

	// store each JsonData object with associated JsonArray
	private HashMap<JsonData, JsonArray> jsonBuffer = new HashMap<>();
	
	// get buffered JsonArray which belongs to a JsonData object
	public JsonArray get(JsonData jd) {
		return jsonBuffer.get(jd);
	}
	
	public DataLoader() {
		// list of all JsonData that will be loaded from SpaceX API
		JsonData[] jsonData = JsonData.class.getEnumConstants();
		
		// get JsonObjects from baseURL/path asynchronously
		Thread[] JsonLoaderThreads = new Thread[jsonData.length];
		for(int i = 0; i<jsonData.length; i++) {
			final int j = i; // declare j final so it can be used in lambda function
			JsonLoaderThreads[i] = new Thread(() -> {
				// establish connection with SpaceX API
				URL url;
				try {
					url = new URL(BASE_URL + jsonData[j].getUrl());
					InputStreamReader reader = new InputStreamReader(url.openStream());
					JsonArray result = JsonParser.parseReader(reader).getAsJsonArray();
					jsonBuffer.put(jsonData[j], result);
				} catch (IOException e) {
					System.out.println("Could not connect to url " + BASE_URL + jsonData[j].getUrl());
					jsonBuffer.put(jsonData[j], new JsonArray());
				}
			});
			JsonLoaderThreads[i].start();
		}
		
		// join all threads
		for(int i = 0; i<jsonData.length; i++) {
			try {
				JsonLoaderThreads[i].join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}