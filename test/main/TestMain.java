package main;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.assertEquals;

import java.util.Map;

import org.easymock.EasyMockRunner;
import org.easymock.Mock;
import org.easymock.TestSubject;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.google.gson.Gson;
import com.google.gson.JsonArray;

@RunWith(EasyMockRunner.class)
public class TestMain {
	
	@TestSubject
	private Main main = new Main();
	
	@Mock
	DataLoader dataMock;
	
	@Test
	public void testPersonInSpace() {
		// test if counting multiple astronauts works
		JsonArray pastLaunches = new Gson().fromJson("[{\"id\":\"1\"},"
				+ "{\"id\":\"2\"}]", JsonArray.class);
		
		JsonArray crew = new Gson().fromJson("[{\"name\":\"P1\",\"launches\":[\"1\"]},"
				+ "{\"name\":\"P2\",\"launches\":[\"2\"]},"
				+ "{\"name\":\"P3\",\"launches\":[\"2\"]}]"
				, JsonArray.class);
		
		expect(dataMock.get(JsonData.LAUNCHES_PAST)).andReturn(pastLaunches);
		expect(dataMock.get(JsonData.CREW)).andReturn(crew);
		replay(dataMock);
		
		assertEquals(3, main.personsInSpace());
	}
	
	@Test
	public void testFuturePersonsInSpace() {
		// test if astronauts are only counted if their launch date is in the past
		// the SpaceX API request /launches/past only includes past launches, but /crew might contain future crew members
		JsonArray pastLaunches = new Gson().fromJson("[{\"id\":\"1\"},"
				+ "{\"id\":\"2\"}]", JsonArray.class);
		
		JsonArray crew = new Gson().fromJson("[{\"name\":\"P1\",\"launches\":[\"1\"]},"
				+ "{\"name\":\"P2\",\"launches\":[\"2\"]},"
				+ "{\"name\":\"P3\",\"launches\":[\"3\"]}]"
				, JsonArray.class);
		
		expect(dataMock.get(JsonData.LAUNCHES_PAST)).andReturn(pastLaunches);
		expect(dataMock.get(JsonData.CREW)).andReturn(crew);
		replay(dataMock);
		
		main.setDataLoader(dataMock);
		
		assertEquals(2, main.personsInSpace());
	}
	
	@Test
	public void testLaunchesPerMonth() {
		JsonArray pastLaunches = new Gson().fromJson("[{\"id\":\"1\",\"date_utc\":\"2006-03-24T22:30:00.000Z\"},"
				+ "{\"id\":\"2\",\"date_utc\":\"2006-03-29T21:30:00.000Z\"},"
				+ "{\"id\":\"3\",\"date_utc\":\"2009-03-24T12:00:00.000Z\"},"
				+ "{\"id\":\"4\",\"date_utc\":\"2011-04-24T12:00:00.000Z\"}]",
				JsonArray.class);
		
		expect(dataMock.get(JsonData.LAUNCHES_PAST)).andReturn(pastLaunches);
		replay(dataMock);
		
		main.setDataLoader(dataMock);
		
		Map<String, Long> lpm = main.getLaunchesPerMonth();
		
		// test if launches in January are zero
		assertEquals(Long.valueOf(0), lpm.get("01"));
		
		// test if number of launches in March/April is correct
		assertEquals(Long.valueOf(3), lpm.get("03"));
		assertEquals(Long.valueOf(1), lpm.get("04"));
	}
	
	@Test
	public void testCoreStatus() {
		JsonArray cores = new Gson().fromJson("[{\"id\":\"1\",\"status\":\"active\"},"
				+ "{\"id\":\"2\",\"status\":\"unknown\"},"
				+ "{\"id\":\"3\",\"status\":\"lost\"},"
				+ "{\"id\":\"4\",\"status\":\"active\"}]",
				JsonArray.class);
		
		expect(dataMock.get(JsonData.CORES)).andReturn(cores).andReturn(cores); // called two times by main
		replay(dataMock);
		
		main.setDataLoader(dataMock);
		
		Map<String, Double> result = main.getCoreStatusProbability();
		
		assertEquals(0.25, result.get("unknown"), 0.001);
		assertEquals(0.25, result.get("lost"), 0.001);
		assertEquals(0.5, result.get("active"), 0.001);
	}
	
	@Test
	public void testCoreStatusEmpty(){
		// test if method getCoreStatusProbability works for an empty JsonArray
		JsonArray cores = new Gson().fromJson("[]", JsonArray.class);
		
		expect(dataMock.get(JsonData.CORES)).andReturn(cores).andReturn(cores);
		replay(dataMock);
		
		main.setDataLoader(dataMock);
		
		// test that no exception is thrown
		main.getCoreStatusProbability();
	}
	
	@Test
	public void testRocketPayloads() {
		JsonArray payloads = new Gson().fromJson("[{\"id\":\"1\",\"mass_kg\":30,\"launch\":\"11\"},"
				+ "{\"id\":\"2\",\"mass_kg\":35,\"launch\":\"11\"},"
				+ "{\"id\":\"3\",\"mass_kg\":40,\"launch\":\"12\"},"
				+ "{\"id\":\"4\",\"mass_kg\":50,\"launch\":\"13\"}]",
				JsonArray.class);
		
		JsonArray launches = new Gson().fromJson("[{\"id\":\"11\",\"rocket\":\"100\"},"
				+ "{\"id\":\"12\",\"rocket\":\"101\"},"
				+ "{\"id\":\"13\",\"rocket\":\"100\"}]",
				JsonArray.class);
		
		JsonArray rockets = new Gson().fromJson("[{\"id\":\"100\",\"name\":\"Falcon 1\"},"
				+ "{\"id\":\"101\",\"name\":\"Falcon Heavy\"},"
				+ "{\"id\":\"102\",\"name\":\"Falcon 9\"}]",
				JsonArray.class);
		
		expect(dataMock.get(JsonData.PAYLOADS)).andReturn(payloads);
		expect(dataMock.get(JsonData.LAUNCHES_PAST)).andReturn(launches);
		expect(dataMock.get(JsonData.ROCKETS)).andReturn(rockets);
		replay(dataMock);
		
		main.setDataLoader(dataMock);
		
		Map<String, Integer> result = main.getRocketPayloads();
		
		// test rocket types with launches
		assertEquals(Integer.valueOf(115), result.get("Falcon 1"));
		assertEquals(Integer.valueOf(40), result.get("Falcon Heavy"));
		
		// test if rocket type with no launches returns 0
		assertEquals(Integer.valueOf(0), result.get("Falcon 9"));
	}
	
	@Test
	public void testRocketPayloadsWithNullValues() {
		// test payload with null mass or null launch
		
		JsonArray payloads = new Gson().fromJson("[{\"id\":\"1\",\"mass_kg\":null,\"launch\":\"11\"},"
				+ "{\"id\":\"2\",\"mass_kg\":35,\"launch\":\"11\"},"
				+ "{\"id\":\"3\",\"mass_kg\":40,\"launch\":null},"
				+ "{\"id\":\"4\",\"mass_kg\":50,\"launch\":\"13\"}]",
				JsonArray.class);
		
		JsonArray launches = new Gson().fromJson("[{\"id\":\"11\",\"rocket\":\"100\"},"
				+ "{\"id\":\"12\",\"rocket\":\"101\"},"
				+ "{\"id\":\"13\",\"rocket\":\"100\"}]",
				JsonArray.class);
		
		JsonArray rockets = new Gson().fromJson("[{\"id\":\"100\",\"name\":\"Falcon 1\"},"
				+ "{\"id\":\"101\",\"name\":\"Falcon Heavy\"},"
				+ "{\"id\":\"102\",\"name\":\"Falcon 9\"}]",
				JsonArray.class);
		
		expect(dataMock.get(JsonData.PAYLOADS)).andReturn(payloads);
		expect(dataMock.get(JsonData.LAUNCHES_PAST)).andReturn(launches);
		expect(dataMock.get(JsonData.ROCKETS)).andReturn(rockets);
		replay(dataMock);
		
		main.setDataLoader(dataMock);
		
		Map<String, Integer> result = main.getRocketPayloads();
		
		assertEquals(Integer.valueOf(85), result.get("Falcon 1"));
		assertEquals(Integer.valueOf(0), result.get("Falcon Heavy"));
		assertEquals(Integer.valueOf(0), result.get("Falcon 9"));
	}
}
