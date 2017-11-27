import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.awt.AWTException;

public class kish{
	Robot bot = new Robot();
	boolean start = true;
	long kishin_start = System.currentTimeMillis();
	long haku_start = System.currentTimeMillis();
	long end;
	long kishin_timer;
	long haku_timer;
	/*
	 * Key Bindings Below
	 */
	int teleport = KeyEvent.VK_SPACE;
	int kishin = KeyEvent.VK_F1;
	int booster = KeyEvent.VK_F2;
	int haku = KeyEvent.VK_F3;
	/*
	 * Kanna buff durations, for rebuffing purposes
	 */
	long kishin_duration = 150000;
	long booster_duration = 240000;
	long haku_duration = 600000;

	public static void main(String[] args) throws AWTException {
		new kish();
	}

	public kish() throws AWTException {
		bot.delay(2000);
		System.out.println("Starting");
		while(true) {
			bot.delay(2000);
			end = System.currentTimeMillis();
			kishin_timer = end - kishin_start;
			if (kishin_timer >= kishin_duration) {
				System.out.println("Kishin");
				keyPress(kishin, 1000);
				kishin_start = System.currentTimeMillis();
			}
		}
	}

	public void keyPress(int e, int delay) {
		bot.keyPress(e);
		bot.delay(delay);
		bot.keyRelease(e);
	}
}