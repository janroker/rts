package hr.fer.zemris.srsv.lab3.model;

import hr.fer.zemris.srsv.lab3.model.LiftState.Direction;

public class Request {
	private Person person;

	public Request(Person person) {
		this.person = person;
	}

	public Direction getDirection() {
		int i = Integer.compare(person.getStart().getFloorNum(), person.getDestination().getFloorNum());
		if (i < 0)
			return Direction.UP;
		else
			return Direction.DOWN;
	}

	public Person getPerson() {
		return person;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((person == null) ? 0 : person.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Request other = (Request) obj;
		if (person == null) {
			if (other.person != null)
				return false;
		} else if (!person.equals(other.person))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Request [person=" + person + ", direction=" + getDirection() + "]";
	}

}
