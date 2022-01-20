package hr.fer.zemris.srsv.lab3.model;

import hr.fer.zemris.srsv.lab3.composite.Element;

public class Person implements Element {

	private Character name;
	private Floor start;
	private Floor destination;

	public Person(Floor start, Character name, Floor destination) {
		this.name = name;
		this.start = start;
		this.destination = destination;

		this.start.addElement(this);
	}

	public Character getName() {
		return name;
	}

	public Floor getStart() {
		return start;
	}

	public Floor getDestination() {
		return destination;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((destination == null) ? 0 : destination.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((start == null) ? 0 : start.hashCode());
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
		Person other = (Person) obj;
		if (destination == null) {
			if (other.destination != null)
				return false;
		} else if (!destination.equals(other.destination))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (start == null) {
			if (other.start != null)
				return false;
		} else if (!start.equals(other.start))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Person [name=" + name + "]";
	}

}
