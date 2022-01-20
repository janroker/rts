package hr.fer.zemris.srsv.ras;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import hr.fer.zemris.srsv.AiPEnums.AiPDirection;
import hr.fer.zemris.srsv.AiPEnums.AiPPosition;

public class Constants {

	public static class Position {

		public Position(int x, int y) {
			super();
			this.x = x;
			this.y = y;
		}

		public int x;
		public int y;

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + x;
			result = prime * result + y;
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) return true;
			if (obj == null) return false;
			if (getClass() != obj.getClass()) return false;
			Position other = (Position) obj;
			if (x != other.x) return false;
			if (y != other.y) return false;
			return true;
		}

		@Override
		public String toString() {
			return "Position [x=" + x + ", y=" + y + "]";
		}

	}

	public static final Map<AiPPosition, Map<AiPDirection, Position>> waitingPedestrianPositionsMap;
	public static final Map<AiPPosition, Map<AiPDirection, Position>> movingPedestrianPositionsMap;
	public static final Map<AiPDirection, Position> waitingCarsPositionsMap;
	public static final Map<AiPDirection, Position> movingCarsPositionsMap;

	public static final Set<Position> waitingPedestrians;
	public static final Set<Position> movingPedestrians;
	public static final Set<Position> waitingCars;
	public static final Set<Position> movingCars;

	public static final Map<TrackPosition, Position[]> pedestrianTrackPositions;
	public static final Map<TrackPosition, Position[]> carTrackPositions;

	public static final Map<Position, Integer> positionCounters;

	static {
		waitingPedestrianPositionsMap = new EnumMap<>(AiPPosition.class);
		for (AiPPosition p : AiPPosition.values()) {
			waitingPedestrianPositionsMap.put(p, new EnumMap<>(AiPDirection.class));
		}

		waitingPedestrianPositionsMap.get(AiPPosition.SE).put(AiPDirection.EW, new Position(8, 9));
		waitingPedestrianPositionsMap.get(AiPPosition.SE).put(AiPDirection.SN, new Position(9, 8));
		waitingPedestrianPositionsMap.get(AiPPosition.NE).put(AiPDirection.EW, new Position(8, 1));
		waitingPedestrianPositionsMap.get(AiPPosition.NE).put(AiPDirection.NS, new Position(9, 2));
		waitingPedestrianPositionsMap.get(AiPPosition.NW).put(AiPDirection.WE, new Position(2, 1));
		waitingPedestrianPositionsMap.get(AiPPosition.NW).put(AiPDirection.NS, new Position(1, 2));
		waitingPedestrianPositionsMap.get(AiPPosition.SW).put(AiPDirection.WE, new Position(2, 9));
		waitingPedestrianPositionsMap.get(AiPPosition.SW).put(AiPDirection.SN, new Position(1, 8));

		movingPedestrianPositionsMap = new EnumMap<>(AiPPosition.class);
		for (AiPPosition p : AiPPosition.values()) {
			movingPedestrianPositionsMap.put(p, new EnumMap<>(AiPDirection.class));
		}
		movingPedestrianPositionsMap.get(AiPPosition.SE).put(AiPDirection.EW, new Position(5, 9));
		movingPedestrianPositionsMap.get(AiPPosition.SE).put(AiPDirection.SN, new Position(9, 5));
		movingPedestrianPositionsMap.get(AiPPosition.NE).put(AiPDirection.EW, new Position(5, 1));
		movingPedestrianPositionsMap.get(AiPPosition.NE).put(AiPDirection.NS, new Position(9, 5));
		movingPedestrianPositionsMap.get(AiPPosition.NW).put(AiPDirection.WE, new Position(5, 1));
		movingPedestrianPositionsMap.get(AiPPosition.NW).put(AiPDirection.NS, new Position(1, 5));
		movingPedestrianPositionsMap.get(AiPPosition.SW).put(AiPDirection.WE, new Position(5, 9));
		movingPedestrianPositionsMap.get(AiPPosition.SW).put(AiPDirection.SN, new Position(1, 5));

		waitingCarsPositionsMap = new EnumMap<>(AiPDirection.class);
		waitingCarsPositionsMap.put(AiPDirection.SN, new Position(6, 10));
		waitingCarsPositionsMap.put(AiPDirection.EW, new Position(10, 4));
		waitingCarsPositionsMap.put(AiPDirection.NS, new Position(4, 0));
		waitingCarsPositionsMap.put(AiPDirection.WE, new Position(0, 6));

		movingCarsPositionsMap = new EnumMap<>(AiPDirection.class);
		movingCarsPositionsMap.put(AiPDirection.SN, new Position(6, 5));
		movingCarsPositionsMap.put(AiPDirection.EW, new Position(5, 4));
		movingCarsPositionsMap.put(AiPDirection.NS, new Position(4, 5));
		movingCarsPositionsMap.put(AiPDirection.WE, new Position(5, 6));

		Position[] parrWaiting
			= new Position[] {
				new Position(8, 9), new Position(9, 8), new Position(8, 1), new Position(9, 2),
				new Position(2, 1), new Position(1, 2), new Position(2, 9), new Position(1, 8) };

		Position[] parrMoving
			= new Position[] {
				new Position(5, 9), new Position(9, 5), new Position(5, 1), new Position(1, 5) };

		Position[] carrWaiting
			= new Position[] {
				new Position(6, 10), new Position(10, 4), new Position(4, 0), new Position(0, 6) };

		Position[] carrMoving
			= new Position[] {
				new Position(6, 5), new Position(5, 4), new Position(4, 5), new Position(5, 6) };

		positionCounters = new HashMap<>();
		List.of(parrWaiting, parrMoving, carrWaiting, carrMoving).forEach(arr ->
			{
				for (Position p : arr) {
					positionCounters.put(p, 0);
				}
			});

		waitingPedestrians = new HashSet<>(Arrays.asList(parrWaiting));
		movingPedestrians = new HashSet<>(Arrays.asList(parrMoving));
		waitingCars = new HashSet<>(Arrays.asList(carrWaiting));
		movingCars = new HashSet<>(Arrays.asList(carrMoving));

		pedestrianTrackPositions = new EnumMap<>(TrackPosition.class);
		pedestrianTrackPositions
			.put(TrackPosition.E, new Position[] { new Position(9, 4), new Position(9, 6) });
		pedestrianTrackPositions
			.put(TrackPosition.N, new Position[] { new Position(4, 1), new Position(6, 1) });
		pedestrianTrackPositions
			.put(TrackPosition.W, new Position[] { new Position(1, 4), new Position(1, 6) });
		pedestrianTrackPositions
			.put(TrackPosition.S, new Position[] { new Position(4, 9), new Position(6, 9) });

		carTrackPositions = new EnumMap<>(TrackPosition.class);
		carTrackPositions.put(TrackPosition.E, generateCarTrackPositions(TrackPosition.E));
		carTrackPositions.put(TrackPosition.N, generateCarTrackPositions(TrackPosition.N));
		carTrackPositions.put(TrackPosition.W, generateCarTrackPositions(TrackPosition.W));
		carTrackPositions.put(TrackPosition.S, generateCarTrackPositions(TrackPosition.S));

	}

	private static Position[] generateCarTrackPositions(TrackPosition tp) {
		Position[] positions = new Position[9];
		for (int i = 1; i < 10; i++) {
			switch (tp) {
				case E: {
					positions[i - 1] = new Position(6, i);
					break;
				}
				case N: {
					positions[i - 1] = new Position(i, 4);
					break;
				}
				case W: {
					positions[i - 1] = new Position(4, i);
					break;
				}
				case S: {
					positions[i - 1] = new Position(i, 6);
					break;
				}
			}
		}
		return positions;
	}

	public enum TrackPosition {
		N, S, W, E
	}

	public static TrackPosition getPedestrianTrackPosition(Position p) {
		if (p.equals(new Position(5, 9))) return TrackPosition.S;
		if (p.equals(new Position(9, 5))) return TrackPosition.E;
		if (p.equals(new Position(5, 1))) return TrackPosition.N;
		else return TrackPosition.W;
	}

	public static TrackPosition getCarTrackPosition(Position p) {
		if (p.equals(new Position(5, 6))) return TrackPosition.S;
		if (p.equals(new Position(6, 5))) return TrackPosition.E;
		if (p.equals(new Position(5, 4))) return TrackPosition.N;
		else return TrackPosition.W;
	}

}
