package hr.fer.zemris.srsv.lab3.model;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import hr.fer.zemris.srsv.lab3.composite.Container;

public class Floor extends Container<Person> implements Comparable<Floor> {

	private int floorNum;
	private Set<Person> personsLeaving = Collections.newSetFromMap(new ConcurrentHashMap<>());

	public Floor(int floorNum) {
		this.floorNum = floorNum;
	}

	public int getFloorNum() {
		return floorNum;
	}

	public boolean addPersonLeaving(Person p) {
		return personsLeaving.add(p);
	}

	public Set<Person> getPersonsLeaving() {
		return Collections.unmodifiableSet(personsLeaving);
	}

	public void clearLeavingPersons() {
		personsLeaving.clear();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Objects.hash(floorNum);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		Floor other = (Floor) obj;
		return floorNum == other.floorNum;
	}

	@Override
	public int compareTo(Floor o) {
		return Integer.compare(o.getFloorNum(), this.floorNum);
	}

	@Override
	public String toString() {
		return "Floor [floorNum=" + floorNum + "]";
	}

}
