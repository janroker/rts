package hr.fer.zemris.srsv.lab3.composite;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public abstract class Container<T extends Element> implements Element {

	private Set<T> elements = Collections.newSetFromMap(new ConcurrentHashMap<>());

	public Container() {
	}

	public boolean removeElement(T e) {
		return elements.remove(e);
	}

	public boolean addElement(T e) {
		return elements.add(e);
	}

	public Set<T> getElements() {
		return Set.copyOf(elements);
	}

	public int size() {
		return elements.size();
	}

	public void clearElements() {
		elements.clear();
	}

}
