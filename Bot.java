import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.awt.AWTException;
import java.awt.event.KeyListener;

public class Bot{
	Robot bot = new Robot();
	long kishin_start = System.currentTimeMillis();
	long haku_start = System.currentTimeMillis();
	long end;
	long kishin_timer;
	long haku_timer;
	int teleport = KeyEvent.VK_SPACE;
	int kishin = KeyEvent.VK_F1;
	int booster = KeyEvent.VK_F2;
	int haku = KeyEvent.VK_F3;
	long kishin_duration = 150000;
	long booster_duration = 240000;
	long haku_duration = 600000;

	public static void main(String[] args) throws AWTException {
		new Bot();
	}

	public Bot() throws AWTException {
		bot.delay(2000);
		buff(kishin);
		buff(haku);
		System.out.println("Starting");
		while(true) {
			bot.delay(50);
			end = System.currentTimeMillis();
			kishin_timer = end - kishin_start;
			haku_timer = end - haku_start;
			bot.keyPress(teleport);
			if (kishin_timer >= kishin_duration) {
				System.out.println("Kishin");
				buff(kishin);
				kishin_start = System.currentTimeMillis();
			}

			if (haku_timer >= haku_duration) {
				System.out.println("Haku");
				buff(haku);
				haku_start = System.currentTimeMillis();
			}
		}
	}

	public void buff(int e) {
		bot.delay(500);
		bot.keyPress(e);
	}
}