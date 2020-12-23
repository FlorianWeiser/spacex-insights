package main;

public enum JsonData {
	// define which values will be loaded from SpaceX API and their url
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