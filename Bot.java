import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.awt.AWTException;

public class Bot{
	private Robot bot, holdBot;

	int teleport = KeyEvent.VK_E;
	int kishin = KeyEvent.VK_F1;
	int booster = KeyEvent.VK_F2;
	int haku = KeyEvent.VK_F3;

	long buff_duration = 60000;

	public static void main(String[] args) throws AWTException {
		new Bot();
	}

	public Bot() throws AWTException {
		bot = new Robot();
		bot.setAutoDelay(100);
		holdBot = new Robot();
		bot.delay(2000);
		System.out.println("Starting");
		long start_timer = System.currentTimeMillis();
		long buff_timer = System.currentTimeMillis();
		long lastPrint = System.currentTimeMillis();
		buff();
		while(true) {
			if (System.currentTimeMillis() - start_timer > 7200000) {
				System.exit(0);
			}
			if (System.currentTimeMillis() - lastPrint > 10000) {
					lastPrint = System.currentTimeMillis();
					long time = System.currentTimeMillis() - start_timer;
					System.out.println(String.format("Running for %s minutes and %s seconds.", (time/1000/60), (time/1000)%60));
				}
			if (System.currentTimeMillis() - buff_timer > buff_duration) {
				System.out.println("Buffing");
				buff();
				buff_timer = System.currentTimeMillis();
			}
			bot.keyPress(teleport);
			bot.keyRelease(teleport);
		}
	}

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