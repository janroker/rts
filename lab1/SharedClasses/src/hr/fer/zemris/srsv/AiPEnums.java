package hr.fer.zemris.srsv;

public class AiPEnums {

	public static enum AiPDirection {
		NS, SN, WE, EW // north-south, south-north ...
	}

	public static enum AiPStatus {
		WAITING, MOVING, DONE
	}

	public static enum AiPType {
		CAR, PEDESTRIAN
	}

	public static enum AiPPosition {
		NW, NE, SW, SE // north-west ...
	}
	
}
