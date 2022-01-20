package hr.fer.zemris.srsv.lab3.model;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

import hr.fer.zemris.srsv.lab3.composite.Container;
import hr.fer.zemris.srsv.lab3.context.Context;
import hr.fer.zemris.srsv.lab3.model.LiftState.Direction;
import hr.fer.zemris.srsv.lab3.model.LiftState.DoorsState;
import hr.fer.zemris.srsv.lab3.util.Procedure;

public class Lift extends Container<Person> {

    private LiftState state;
    private int capacity;
    private Set<Request> assignedRequests = new HashSet<>();

    public Lift(int capacity, Floor current) {
	this.capacity = capacity;
	this.state = new LiftState(null, DoorsState.CLOSED, current);
    }

    public LiftState getState() {
	return state;
    }

    public int getCapacity() {
	return capacity;
    }

    public boolean isFull() {
	return this.capacity <= this.size();
    }

    public boolean shouldStop(Floor f) {
	for (Request r : assignedRequests) {
	    if (r.getPerson().getStart().equals(f))
		return true;
	}
	for (Person p : this.getElements()) {
	    if (p.getDestination().equals(f))
		return true;
	}
	return false;
    }

    private boolean shouldStop() { // ako nije izmedu katova, ako se uopce krece, i ako ima zahtjeva na tom katu
	return (!this.state.isUnderCurrent() && this.state.getDirection() != null
		&& this.shouldStop(this.state.getCurrent()));
    }

    public void assignRequests(Set<Request> reqs) {
	assignedRequests.addAll(reqs);
    }

    /**
     * Return true if accepted, false if not
     * 
     * @param req
     * @return
     */
    public boolean assignRequest(Request req, List<Floor> floors) {
	if (!this.isAcceptable(req, floors)) {
	    return false;
	}
	assignedRequests.add(req);
	if (this.state.getDirection() == null) {
	    setDirection(req);
	}
	return true;
    }

    private void setDirection(Request req) {
	if (this.isReqAtTheSameFloor(req))
	    this.state.setDirection(req.getDirection());
	else if (this.isRequestBelow(req))
	    this.state.setDirection(Direction.DOWN);
	else
	    this.state.setDirection(Direction.UP);
    }

    private void letPersonsOutIfTheirFloor(Floor current) {
	for (Person p : this.getElements()) {
	    if (p.getDestination().equals(current)) {
		this.removeElement(p);
		current.addPersonLeaving(p);
	    }
	}
    }

    private void takeAssignedReqOrReturnIfUnable(Context context, Floor current) {
	Iterator<Request> it = this.assignedRequests.iterator();
	while (it.hasNext()) {
	    Request r = it.next();
	    if (this.isAcceptable(r, context.getFloors())) { // posluži
		if (current.equals(r.getPerson().getStart())) {
		    it.remove();
		    this.addElement(r.getPerson());
		    current.removeElement(r.getPerson());

		    if (!r.getDirection().equals(this.state.getDirection())) { // ako se treba okrenuti kod requesta
			this.setDirection(r);
		    }
		}
	    } else { // return req
		it.remove();
		context.addRequest(r);
	    }
	}
    }

    public boolean exchangePassengers(Context context, Procedure assignReqs) {
	if (!this.shouldStop())
	    return false;

	this.state.setDoors(DoorsState.OPENED);

	letPersonsOutIfTheirFloor(this.state.getCurrent());

	Direction prev = this.state.getDirection();

	takeAssignedReqOrReturnIfUnable(context, this.state.getCurrent());
	if (this.getElements().size() <= 0 && this.assignedRequests.size() <= 0) {
	    this.state.setDirection(null);
	}

	boolean dirChange = false;
	Direction current = this.state.getDirection();
	if (prev != null && current != null) {
	    dirChange = !prev.equals(current);
	} else if (!(prev == null && current == null)) {
	    dirChange = true;
	}

	if (dirChange) {
	    assignReqs.procedure();
	}

	return true;

    }

    /**
     * @param context
     * @return true if should stop
     */
    public boolean move(Context context) {
	Direction direction = this.state.getDirection();

	this.state.setDoors(DoorsState.CLOSED);

	if (direction != null) {
	    int indexOf = context.getFloors().indexOf(this.state.getCurrent());
	    this.switchPosition(context, direction, indexOf);
	}

	return this.shouldStop();
    }

    private void switchPosition(Context context, Direction direction, int indexOf) {
	boolean underCurrent = this.state.isUnderCurrent();

	switch (direction) {
	case DOWN: {
	    if (underCurrent) {
		this.state.setCurrent(context.getFloors().get(indexOf - 1));
	    }
	    break;
	}
	case UP: {
	    if (!underCurrent) {
		this.state.setCurrent(context.getFloors().get(indexOf + 1));
	    }
	    break;
	}
	}

	this.state.setUnderCurrent(!underCurrent);
    }

    public boolean isAcceptable(Request r, List<Floor> floors) {
	Direction direction = this.getState().getDirection();
	if (direction == null)
	    return true;

	if (this.isFull())
	    return false;

	return this.distance(r, floors) < Integer.MAX_VALUE;
    }

    public int distance(Request r, List<Floor> floors) {
	Floor start = r.getPerson().getStart();

	if (this.state.getDirection() == null) { // nema smjer
	    return calculateDistance(start, floors);
	} else {
	    Supplier<Boolean> onUp = () -> this.isReqAbove(r);
	    Supplier<Boolean> onDown = () -> this.isRequestBelow(r);
	    Supplier<Boolean> onNone = () -> true;

	    if (this.isReqAtTheSameFloor(r) || switchForDirection(onUp, onDown, onNone)) { // zahtjev je na putanji

		if (r.getDirection().equals(this.state.getDirection())) { // imaju isti smjer i može se poslužiti
		    return calculateDistance(start, floors);

		} else { // nemaju isti smjer, ali je na putanji i može se okrenuti i poslužiti kada dode
			 // do njega
		    return distanceForCanMakeATurn(r, floors);
		}
	    }
	}
	return Integer.MAX_VALUE;
    }

    private <R> R switchForDirection(Supplier<R> onUp, Supplier<R> onDown, Supplier<R> onNone) {
	switch (this.state.getDirection()) {
	case UP: {
	    return onUp.get();
	}
	case DOWN: {
	    return onDown.get();
	}
	}
	return onNone.get();
    }

    private int distanceForCanMakeATurn(Request r, List<Floor> floors) {
	Floor start = r.getPerson().getStart();

	Supplier<Boolean> onUp = () -> {
	    for (Person p : this.getElements()) {
		if (p.getDestination().getFloorNum() > start.getFloorNum())
		    return false;
	    }
	    for (Request aReq : this.assignedRequests) {
		if (aReq.getDirection().equals(this.state.getDirection())) {
		    if (aReq.getPerson().getStart().getFloorNum() >= start.getFloorNum()) {
			return false;
			// jer ako je u asajnanim,
			// mora ic u smjeru kretanja
			// pa ce mu kraj bit negdje
			// iznad...
		    }
		} else {
		    if (aReq.getPerson().getStart().getFloorNum() > start.getFloorNum()) {
			return false;
		    }
		}
	    }
	    return true;
	};
	Supplier<Boolean> onDown = () -> {
	    for (Person p : this.getElements()) {
		if (p.getDestination().getFloorNum() < start.getFloorNum())
		    return false;
	    }
	    for (Request aReq : this.assignedRequests) {
		if (aReq.getDirection().equals(this.state.getDirection())) {
		    if (aReq.getPerson().getStart().getFloorNum() <= start.getFloorNum()) {
			return false;
			// jer ako je u asajnanim,
			// mora ic u smjeru kretanja
			// pa ce mu kraj bit negdje
			// iznad...
		    }
		} else {
		    if (aReq.getPerson().getStart().getFloorNum() < start.getFloorNum()) {
			return false;
		    }
		}
	    }
	    return true;
	};
	Supplier<Boolean> onNone = () -> true;

	if (switchForDirection(onUp, onDown, onNone))
	    return calculateDistance(start, floors);

	return Integer.MAX_VALUE;
    }

    private int calculateDistance(Floor f, List<Floor> floors) {
	int idxOfF = floors.indexOf(f);
	int idxOfCurr = floors.indexOf(this.state.getCurrent());

	int diff = idxOfF - idxOfCurr;
	int rez = Math.abs(diff);

	rez = rez * 2; // zbog između katova
	if (diff < 0) { // curr if above f
	    if (this.state.isUnderCurrent())
		rez -= 1;
	} else if (diff > 0) { // curr is under f
	    if (this.state.isUnderCurrent())
		rez += 1;
	}

	return rez;
    }

    private boolean isReqAtTheSameFloor(Request r) {
	return (this.getState().getCurrent().equals(r.getPerson().getStart()) && !this.state.isUnderCurrent());
    }

    private boolean isReqAbove(Request r) {
	int lfloorNum = this.getState().getCurrent().getFloorNum();
	int rfloorNum = r.getPerson().getStart().getFloorNum();
	return lfloorNum < rfloorNum || (lfloorNum == rfloorNum && this.getState().isUnderCurrent());
    }

    private boolean isRequestBelow(Request r) {
	int lfloorNum = this.getState().getCurrent().getFloorNum();
	int rfloorNum = r.getPerson().getStart().getFloorNum();
	return lfloorNum > rfloorNum;
    }

    public Set<Request> getRequests() {
	return Collections.unmodifiableSet(this.assignedRequests);
    }

    @Override
    public String toString() {
	return "Lift [state=" + state + ", capacity=" + capacity + "]";
    }

}
