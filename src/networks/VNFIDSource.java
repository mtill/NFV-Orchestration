package networks;

public class VNFIDSource {

	private VNFIDSource() {
		throw new AssertionError();
	}
	
	static long id = 0;
	
	public static void set(long id) {
		VNFIDSource.id = id;
	}
	
	public static long getID() {
		if(id == Long.MAX_VALUE)
			id = 0;
		
		return id++;
	}
	
	public static void reset() {
		id = 0;
	}
	
}
