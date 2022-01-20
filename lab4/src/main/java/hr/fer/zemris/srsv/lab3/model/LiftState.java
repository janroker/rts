package hr.fer.zemris.srsv.lab3.model;

public class LiftState {

	private Direction direction;
	private DoorsState doors;
	private Floor current;
	private boolean isUnderCurrent = false;

	public LiftState(Direction direction, DoorsState doors, Floor current) {
		super();
		this.direction = direction;
		this.doors = doors;
		this.current = current;
	}

	public Direction getDirection() {
		return direction;
	}

	public String getDirectionStr() {
		if (direction == null)
			return "*";

		String s = null;
		switch (direction) {
		case UP: {
			s = "G";
			break;
		}
		case DOWN: {
			s = "D";
			break;
		}
		}
		return s;
	}

	public void setDirection(Direction direction) {
		this.direction = direction;
	}

	public DoorsState getDoors() {
		return doors;
	}

	public String getDoorsStr() {
		String s = null;
		switch (doors) {
		case CLOSED: {
			s = "Z";
			break;
		}
		case OPENED: {
			s = "O";
			break;
		}
		}
		return s;
	}

	public void setDoors(DoorsState doors) {
		this.doors = doors;
	}

	public Floor getCurrent() {
		return current;
	}

	public boolean isUnderCurrent() {
		return isUnderCurrent;
	}

	public void setUnderCurrent(boolean isUnderCurrent) {
		this.isUnderCurrent = isUnderCurrent;
	}

	public void setCurrent(Floor current) {
		this.current = current;
	}

	public enum Direction {
		UP, DOWN
	}

	public enum DoorsState {
		OPENED, CLOSED
	}

	@Override
	public String toString() {
		return "LiftState [direction=" + direction + ", doors=" + doors + ", current=" + current + ", isUnderCurrent="
				+ isUnderCurrent + "]";
	}

}