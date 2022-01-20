package hr.fer.zemris.srsv.upr;

import java.util.function.Consumer;

import hr.fer.zemris.srsv.State;
import hr.fer.zemris.srsv.AiPEnums.AiPDirection;

public class StateSwitchers {

	public static final Consumer<State> allRed = (prevState) ->
		{
			if (!shouldChange()) return;

			switch (prevState) {
				case ALL_RED:
					Main.current_state = State.SN_GREEN;
					break;
				case SN_GREEN:
					throw new RuntimeException("Wrong prev state");
				case SN_GREEN_CARS:
					Main.current_state = State.WE_GREEN;
					break;
				case WE_GREEN:
					throw new RuntimeException("Wrong prev state");
				case WE_GREEN_CARS:
					Main.current_state = State.SN_GREEN;
					break;
				default:
					throw new RuntimeException("Wrong prev state");
			}

			Main.prev_state = State.ALL_RED;
			setPeriod(null);
		};

	public static final Consumer<State> snGreen = (prevState) ->
		{
			if (!shouldChange()) return;

			Main.current_state = State.SN_GREEN_CARS;
			Main.prev_state = State.SN_GREEN;
			setPeriod(10);
		};

	public static final Consumer<State> snGreenCars = (prevState) ->
		{
			if (!shouldChange()) return;

			Main.current_state = State.ALL_RED;
			Main.prev_state = State.SN_GREEN_CARS;
			setPeriod(5);
		};

	public static final Consumer<State> weGreen = (prevState) ->
		{
			if (!shouldChange()) return;

			Main.current_state = State.WE_GREEN_CARS;
			Main.prev_state = State.WE_GREEN;
			setPeriod(10);
		};

	public static final Consumer<State> weGreenCars = (prevState) ->
		{
			if (!shouldChange()) return;

			Main.current_state = State.ALL_RED;
			Main.prev_state = State.WE_GREEN_CARS;
			setPeriod(5);
		};;

	private static boolean shouldChange() {
		if (Main.numSecondsSinceStateChange < Main.currentPeriod) return false;
		Main.numSecondsSinceStateChange = 0;
		return true;
	}

	private static void setPeriod(Integer period) {
		if (period != null) {
			Main.currentPeriod = period;
			return;
		}

		AiPDirection dir = Main.getCurrentGreenD();

		if (Main.objectsOnDirection.get(dir) > 0) Main.currentPeriod = 20;
		else Main.currentPeriod = 5;
	}

}
