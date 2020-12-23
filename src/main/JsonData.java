package main;

/**
 * Define which values can be loaded from the SpaceX API and their url
 */
public enum JsonData {
	CORES("cores"), CREW("crew"), LAUNCHES_PAST("launches/past"), PAYLOADS("payloads"), ROCKETS("rockets");
	
	private final String url;

    private JsonData(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }
    
    @Override
    public String toString() {
    	return url;
    }
}