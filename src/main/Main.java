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

/**
 * Application Main class.
 * Includes the main method and methods to process the JsonData
 */
public class Main {
	
	DataLoader data;
	
	/** 
	 * Main method of the SpaceXInsights console based program.
	 * Prints total payloads, number of persons sent to space, launches by month and core status probability.
	 * This method is not executed when running the application on a Tomcat Server.
	 * @param args	program arguments. If the first argument equals "--timing", additional timing information is printed.
	 */
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
		
		if(args.length >= 1 && args[0] != null && args[0].equals("--timing")) {
			System.out.println("data loading time: " + (stopTime1 - startTime)/1000000000.0 + "s");
			System.out.println("total execution time: " + (stopTime2 - startTime)/1000000000.0 + "s");
		}
	}
	
	/**
	 * Constructor of Main class.
	 * Instantiates a new DataLoader object.
	 */
	public Main(){
		data = new DataLoader();
	}
	
	/**
	 * Change the DataLoader object of the main class.
	 * Especially useful for testing.
	 * @param dl 	The new data loader object
	 */
	public void setDataLoader(DataLoader dl) {
		this.data = dl;
	}
	
	/**
	 * Returns the total payload sent to space by each rocket type.
	 * @return	A map with the rocket types as keys and the total payload sent to space by them as values
	 */
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
	
	/**
	 * Returns the total number of persons sent to space.
	 * Launch dates must be in the past and astronauts with multiple space flights are only counted once.
	 * @return	the SpaceX astronaut count
	 */
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
	
	/**
	 * Returns the number of launches grouped by the month in which they took place.
	 * @return	a map with all twelve months as keys (e.g. the month January is encoded as "01") 
	 * 			and the number of launches in this month as associated value
	 */
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
	
	/**
	 * Returns which status are the most common among SpaceX cores.
	 * @return	a mapping of all possible core status and how likely they are (as a double in percent)
	 */
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