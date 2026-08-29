package util;

public class Sleep {

	public static void ms( int delay ) {
		try {
			Thread.sleep( delay );
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
