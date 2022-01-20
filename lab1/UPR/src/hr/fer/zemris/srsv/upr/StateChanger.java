package hr.fer.zemris.srsv.upr;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.Consumer;

import hr.fer.zemris.srsv.State;
import hr.fer.zemris.srsv.UPRMessage;

public class StateChanger implements Runnable {
	private static Map<State, Consumer<State>> stateSwitchers = new EnumMap<>(State.class);

	static {
		stateSwitchers.put(State.ALL_RED, StateSwitchers.allRed);
		stateSwitchers.put(State.SN_GREEN, StateSwitchers.snGreen);
		stateSwitchers.put(State.SN_GREEN_CARS, StateSwitchers.snGreenCars);
		stateSwitchers.put(State.WE_GREEN, StateSwitchers.weGreen);
		stateSwitchers.put(State.WE_GREEN_CARS, StateSwitchers.weGreenCars);
	}

	@Override
	public void run() {
		try {
			Main.numSecondsSinceStateChange += 5;
			System.out.println("numSecondsSinceStateChange: " + Main.numSecondsSinceStateChange);

			State saved = Main.current_state;
			stateSwitchers.get(Main.current_state).accept(Main.prev_state);

			if (!saved.equals(Main.current_state)) {
				System.out
					.println("UPR new state: " + Main.current_state + " new period: " + Main.currentPeriod);

				notifySem();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private void notifySem() {
		UPRMessage msg = new UPRMessage(Main.current_state);
		ObjectOutputStream oos;
		try {
			oos = new ObjectOutputStream(new BufferedOutputStream(Main.uprSocket.getOutputStream()));
			oos.writeObject(msg);
			oos.flush();
			System.out.println("Notified SEM on state change: " + msg);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
