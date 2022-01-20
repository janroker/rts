package hr.fer.zemris.srsv.sem;

import java.net.Socket;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import hr.fer.zemris.srsv.AiPEnums.AiPDirection;
import hr.fer.zemris.srsv.State;

public class Constants {

	public static final Map<State, Set<Socket>> waitingMapCars;
	public static final Map<State, Set<Socket>> waitingMapPedestrians;
	public static final Map<AiPDirection, State> mappedStatesCars;
	public static final Map<AiPDirection, State> mappedStatesPedestrians;
	public static final Map<State, State> equalStatesCars;

	static {
		waitingMapPedestrians = new EnumMap<>(State.class);
		waitingMapCars = new EnumMap<>(State.class);

		for (State s : State.values()) {
			waitingMapPedestrians.put(s, new HashSet<>());
			waitingMapCars.put(s, new HashSet<>());
		}

		waitingMapCars.put(State.SN_GREEN_CARS, waitingMapCars.get(State.SN_GREEN));
		waitingMapCars.put(State.WE_GREEN_CARS, waitingMapCars.get(State.WE_GREEN));

		mappedStatesCars = new EnumMap<>(AiPDirection.class);
		mappedStatesCars.put(AiPDirection.EW, State.WE_GREEN);
		mappedStatesCars.put(AiPDirection.WE, State.WE_GREEN);
		mappedStatesCars.put(AiPDirection.NS, State.SN_GREEN);
		mappedStatesCars.put(AiPDirection.SN, State.SN_GREEN);

		mappedStatesPedestrians = new EnumMap<>(AiPDirection.class);
		mappedStatesPedestrians.put(AiPDirection.EW, State.WE_GREEN);
		mappedStatesPedestrians.put(AiPDirection.WE, State.WE_GREEN);
		mappedStatesPedestrians.put(AiPDirection.NS, State.SN_GREEN);
		mappedStatesPedestrians.put(AiPDirection.SN, State.SN_GREEN);

		equalStatesCars = new EnumMap<>(State.class);
		for (State s : State.values()) {
			equalStatesCars.put(s, s);
		}
		equalStatesCars.put(State.SN_GREEN_CARS, State.SN_GREEN);
		equalStatesCars.put(State.WE_GREEN_CARS, State.WE_GREEN);
		
	}

}
