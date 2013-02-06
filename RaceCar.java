
import lejos.nxt.Button;

public class RaceCar {
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		GameThread gt = new GameThread();
		//gt.setDaemon(true);	
		gt.start();

	}

}
