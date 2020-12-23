package main;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;

public class Main {
	
	DataLoader data;
	
	// run this to only see console output (without running a web server)
	public static void main(String... args) {
		long startTime = System.nanoTime();
		
		Main m;
		m = new Main();
		
		long stopTime1 = System.nanoTime();
		
		System.out.println("### WELCOME TO THE SPACEX INSIGHTS DASHBOARD ###\n");
		
		System.out.println("----- TOTAL PAYLOADS -----");
		HashMap<String, Integer> rocketPayloads = m.getRocketPayloads();
		for(String rocket : rocketPayloads.keySet()){
			System.out.println(rocket + ": " + rocketPayloads.get(rocket) + "kg");
		}
		System.out.println("");
		
		System.out.println("----- NUMBER OF PERSONS SENT TO SPACE -----");
		System.out.println(m.personsInSpace());
		System.out.println("");
		
		System.out.println("----- LAUNCHES BY MONTH -----");
		Map<String, Long> lpm = m.getLaunchesPerMonth();
		for(int i = 1; i <= 12; i++) {
			String month = Month.of(i).getDisplayName(TextStyle.FULL, Locale.US);
			System.out.println(month + ": " + lpm.get(String.format("%02d", i)));
		}
		System.out.println("");
		
		System.out.println("----- CORE STATUS PROBABILITY -----");
		Map<String, Double> sp = m.getCoreStatusProbability();
		for(String status : sp.keySet()) {
			System.out.println(status + ": " + sp.get(status));
		}
		System.out.println("");
		
		long stopTime2 = System.nanoTime();
		System.out.println("data loading time: " + (stopTime1 - startTime)/1000000000.0 + "s");
		System.out.println("total execution time: " + (stopTime2 - startTime)/1000000000.0 + "s");
	}
	
	public Main(){
		data = new DataLoader();
	}
	
	// change data loader
	public void setDataLoader(DataLoader dl) {
		this.data = dl;
	}
	
	// get the total payload sent to space by each rocket type
	public HashMap<String, Integer> getRocketPayloads(){
		
		// map spaceship ids to their names
		HashMap<String, String> spaceshipIds = new HashMap<>();
		for(JsonElement je : data.get(JsonData.ROCKETS)) {
			JsonObject jo = je.getAsJsonObject();
			spaceshipIds.put(jo.get("id").getAsString(), jo.get("name").getAsString());
		}
		
		// map launch ids to rocket ids
		HashMap<String, String> launchRockets = new HashMap<>();
		for(JsonElement je : data.get(JsonData.LAUNCHES_PAST)) {
			JsonObject jo = je.getAsJsonObject();
			launchRockets.put(jo.get("id").getAsString(), jo.get("rocket").getAsString());
		}
		
		// initialize result HashMap
		HashMap<String, Integer> spaceshipPayloads = new HashMap<>();
		
		// set all values to 0 (so that rockets without any launches are also included)
		for(String rocket : spaceshipIds.values()) {
			spaceshipPayloads.put(rocket, 0);
		}
		
		// map spaceship names to their total payloads
		for(JsonElement je : data.get(JsonData.PAYLOADS)) {
			JsonObject jo = je.getAsJsonObject();
			
			if(jo.get("mass_kg") != JsonNull.INSTANCE && jo.get("launch") != JsonNull.INSTANCE) {
				String launchId = jo.get("launch").getAsString();
				
				// only count past launches
				if (launchRockets.containsKey(launchId)) {
					String rocketId = launchRockets.get(launchId);
					String rocketName = spaceshipIds.get(rocketId);
					spaceshipPayloads.put(rocketName, spaceshipPayloads.getOrDefault(rocketName, 0) + jo.get("mass_kg").getAsInt());
				}
			}
		}
		
		return spaceshipPayloads;
	}
	
	// get the total number of persons sent to space
	public int personsInSpace() {
		// return jsonBuffer.get("crew").size(); can be used if the API already only lists crew members with launches in the past
		
		int result = 0;
		
		// get a list of all launches in the past
		HashSet<String> launches = new HashSet<>();
		for(JsonElement je : data.get(JsonData.LAUNCHES_PAST)) {
			JsonObject jo = je.getAsJsonObject();
			launches.add(jo.get("id").getAsString());
		}
		
		// count only crew members who have at least one launch in the past
		for(JsonElement je : data.get(JsonData.CREW)) {
			for(JsonElement jee : je.getAsJsonObject().get("launches").getAsJsonArray()) {
				if(launches.contains(jee.getAsString())) {
					result++;
					break;
				}
			}
		}
		return result;
	}
	
	// get the number of launches grouped by their month of the year
	public Map<String, Long> getLaunchesPerMonth(){
		// define input and output format
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
		SimpleDateFormat formatNew = new SimpleDateFormat("MM"); // change to MMMM to get name of months
		
		// map months to number of launches in this month
		Map<String, Long> launchesPerMonth = StreamSupport.stream(data.get(JsonData.LAUNCHES_PAST).spliterator(), false)
				.collect(Collectors.groupingBy(i -> {
					try {
						return formatNew.format(format.parse(i.getAsJsonObject().get("date_utc").getAsString()));
					} catch (ParseException e) { return "-1"; }
				}, Collectors.counting()));
		
		// add zero values for months not contained in list
		for(int i = 0; i <= 12; i++) {
			String month = String.format("%02d", i);
			if(!launchesPerMonth.containsKey(month))
				launchesPerMonth.put(month, 0L);
		}
		
		return launchesPerMonth;
	}
	
	// get the percentage of each core status
	public Map<String, Double> getCoreStatusProbability(){
		double totalCoreCount = data.get(JsonData.CORES).size();
		Map<String, Double> result = new HashMap<>();
		
		// map months to number of launches in this month
		for(JsonElement core : data.get(JsonData.CORES)) {
			String status = core.getAsJsonObject().get("status").getAsString();
			result.put(status, result.getOrDefault(status, 0.0) + 1);
		}
		
		// divide each entry by totalCoreCount
		for(String key : result.keySet()) {
			result.put(key, result.get(key) / totalCoreCount);
		}
		
		return result;
	}
}