import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.awt.AWTException;

public class Bot{
	private Robot bot, holdBot;
	// long kishin_start = System.currentTimeMillis();
	// long haku_start = System.currentTimeMillis();
	// long end;
	// long kishin_timer;
	// long haku_timer;
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
	long buff_duration = 60000;
	// long kishin_duration = 150000;
	// long booster_duration = 240000;
	// long haku_duration = 600000;

	public static void main(String[] args) throws AWTException {
		new Bot();
	}

	public Bot() throws AWTException {
		bot = new Robot();
		bot.setAutoDelay(100);
		holdBot = new Robot();
		bot.delay(2000);
		System.out.println("Starting");
		long start = System.currentTimeMillis();
		// long cur = System.currentTimeMillis();
		// while (System.currentTimeMillis() - cur < 500) {
		// 	holdBot.keyPress(kishin);
		// }
		// holdBot.keyRelease(kishin);
		// cur = System.currentTimeMillis();
		// while (System.currentTimeMillis() - cur < 500) {
		// 	holdBot.keyPress(booster);
		// }
		// holdBot.keyRelease(booster);
		// cur = System.currentTimeMillis();
		// while (System.currentTimeMillis() - cur < 500) {
		// 	holdBot.keyPress(haku);
		// }
		// holdBot.keyRelease(haku);
		// long start = System.currentTimeMillis();
		buff();
		while(true) {
			// end = System.currentTimeMillis();
			// kishin_timer = end - kishin_start;
			// haku_timer = end - haku_start;
			if (System.currentTimeMillis() - start > buff_duration) {
				System.out.println("Buffing");
				// keyDown(kishin, 500);
				// keyDown(booster, 500);
				// keyDown(haku, 500);
				buff();
				// kishin_start = System.currentTimeMillis();
				start = System.currentTimeMillis();
			}
			// if (haku_timer > haku_duration) {
			// 	System.out.println("Haku");
			// 	keyDown(haku, 500);
			// 	haku_start = System.currentTimeMillis();
			// }
			// keyPress(teleport);
			bot.keyPress(teleport);
			bot.keyRelease(teleport);
		}
	}

	// public void keyPress(int e) {
	// 	bot.keyPress(e);
	// 	bot.keyRelease(e);
	// }

	// public void keyDown(int e, int delay) {
	// 	long cur = System.currentTimeMillis();
	// 	while (System.currentTimeMillis() - cur < delay) {
	// 		holdBot.keyPress(e);
	// 	}
	// 	holdBot.keyRelease(e);
	// }

	public void buff() {
		long cur = System.currentTimeMillis();
		while (System.currentTimeMillis() - cur < 500) {
			holdBot.keyPress(kishin);
		}
		holdBot.keyRelease(kishin);
		holdBot.delay(500);
		cur = System.currentTimeMillis();
		while (System.currentTimeMillis() - cur < 500) {
			holdBot.keyPress(booster);
		}
		holdBot.keyRelease(booster);
		holdBot.delay(500);
		cur = System.currentTimeMillis();
		while (System.currentTimeMillis() - cur < 500) {
			holdBot.keyPress(haku);
		}
		holdBot.delay(500);
		holdBot.keyRelease(haku);
	}
}