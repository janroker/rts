package hr.fer.zemris.srsv.lab3.context;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import hr.fer.zemris.srsv.lab3.model.Floor;
import hr.fer.zemris.srsv.lab3.model.Lift;
import hr.fer.zemris.srsv.lab3.model.Request;

public class Context {
	private Random rand = new Random();
	private List<Floor> floors = new ArrayList<>();
	private List<Lift> lifts = new ArrayList<>();
	private List<Character> chars = new ArrayList<>();
	private Set<Request> pendingRequest = Collections.newSetFromMap(new ConcurrentHashMap<>());

	public Context(int numFloors, int... liftCapacities) {
		if (numFloors < 2 || liftCapacities.length <= 0)
			throw new IllegalArgumentException();

		for (int i = 1; i <= numFloors; i++) {
			floors.add(new Floor(i));
		}

		for (int cap : liftCapacities) {
			if (cap <= 0)
				throw new IllegalArgumentException();
			lifts.add(new Lift(cap, floors.get(0)));
		}

		for (char ch : "qwertzuiopasdfghjklyxcvbnmQWERTZUIOPASDFGHJKLYXCVBNM".toCharArray())
			chars.add(ch);
	}

	public List<Floor> getFloors() {
		return Collections.unmodifiableList(floors);
	}

	public List<Lift> getLifts() {
		return Collections.unmodifiableList(lifts);
	}

	public Floor getRandomFloor() {
		return floors.get(rand.nextInt(floors.size()));
	}

	public Floor getRandomFloorButNot(Floor floor) {
		List<Floor> cpy = new ArrayList<>(floors);
		cpy.remove(floor);

		return cpy.get(rand.nextInt(cpy.size()));
	}

	public synchronized char getRandomChar() {
		return chars.get(rand.nextInt(chars.size()));
	}

	public boolean addRequest(Request r) {
		return pendingRequest.add(r);
	}

	public void addRequests(Collection<Request> reqs) {
		this.pendingRequest.addAll(reqs);
	}

	public Set<Request> getMatchingRequests(Lift l) {
		if (l.getState().getDirection() == null)
			return Set.copyOf(pendingRequest);

		return pendingRequest.stream().filter(r -> l.isAcceptable(r, this.getFloors()))
				.collect(Collectors.toUnmodifiableSet());
	}

	public Set<Request> getRequests() {
		return Set.copyOf(pendingRequest);
	}

	public void takeRequests(Set<Request> reqs) {
		pendingRequest.removeAll(reqs);
	}

	public void takeRequest(Request req) {
		pendingRequest.remove(req);
	}

}
