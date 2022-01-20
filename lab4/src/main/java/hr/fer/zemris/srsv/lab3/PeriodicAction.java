package hr.fer.zemris.srsv.lab3;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import hr.fer.zemris.srsv.lab3.context.Context;
import hr.fer.zemris.srsv.lab3.model.Floor;
import hr.fer.zemris.srsv.lab3.model.Lift;
import hr.fer.zemris.srsv.lab3.model.LiftState;
import hr.fer.zemris.srsv.lab3.model.Person;
import hr.fer.zemris.srsv.lab3.model.Request;

public class PeriodicAction implements Runnable {
    private static String SPACE = " ";

    private Context context;
    private static long PERIODS;
    private int periodsForNewPerson;
    private int periodsForElevatorMovement;

    /**
     * 
     * @param context
     * @param milisForNewPerson
     * @param multiplierForElevatorMovement
     */
    public PeriodicAction(Context context, int periodsForNewPerson, int periodsForElevatorMovement) {
	this.context = context;
	this.periodsForNewPerson = periodsForNewPerson;
	this.periodsForElevatorMovement = periodsForElevatorMovement;
    }

    @Override
    public void run() {
	try {
	    PERIODS += 1;

	    if (PERIODS % this.periodsForNewPerson == 0) {
		generatePerson();
		assignRequests();
		print();
	    }

	    if (PERIODS % this.periodsForElevatorMovement == 0) {
		moveElevators();
	    }

	} catch (Exception e) {
	    e.printStackTrace();
	}
    }

    private void moveElevators() {
	for (Floor f : context.getFloors()) {
	    f.clearLeavingPersons();
	}
	for (Lift l : context.getLifts()) {
	    l.exchangePassengers(context, () -> assignRequests());
	}
	// print();
	for (Lift l : context.getLifts()) {
	    l.move(context);
	}
	// print();
	for (Lift l : context.getLifts()) {
	    l.exchangePassengers(context, () -> assignRequests());
	}
	print();
    }

    private void assignRequests() {
	Map<Request, TreeSet<Lift>> mapReqMatchingLifts = new HashMap<>();
	for (Request r : context.getRequests()) {
	    mapReqMatchingLifts.put(r, new TreeSet<>(getLiftComparator(r)));
	}

	for (Lift l : context.getLifts()) {
	    for (Request r : context.getMatchingRequests(l)) {
		Set<Lift> set = mapReqMatchingLifts.get(r);
		set.add(l);
	    }
	}

	for (Entry<Request, TreeSet<Lift>> e : mapReqMatchingLifts.entrySet()) {
	    TreeSet<Lift> set = e.getValue();

	    boolean accepted = false;
	    while (!accepted && !set.isEmpty()) { // jer req moze postat ne prihvatljiv za liftove koji promjene smjer
		accepted = set.pollFirst().assignRequest(e.getKey(), context.getFloors());
	    }
	    if (accepted)
		context.takeRequest(e.getKey());
	}
    }

    private Comparator<Lift> getLiftComparator(Request r) {
	return (l1, l2) -> {
	    int distance1 = l1.distance(r, context.getFloors());
	    int distance2 = l2.distance(r, context.getFloors());

	    if (Math.abs(distance1 - distance2) < 2) { // kad je razlika u "blizini" manja od 2 (1, 0, -*).
		return Integer.compare(l1.getCapacity(), l2.getCapacity()); // lift manjeg kapaciteta ide prije
	    } else {
		return Integer.compare(distance1, distance2); // bliži ide prije
	    }

	};
    }

    private void generatePerson() {
	Floor f = context.getRandomFloor();
	if (f.getElements().size() < 10)
	    context.addRequest(new Request(new Person(f, context.getRandomChar(), context.getRandomFloorButNot(f))));
    }

    private void print() {
	System.out.println();
	System.out.println("------------------------------------------------------------");

	System.out.println("Active reqs: " + context.getRequests());
	for (Lift l : context.getLifts()) {
	    System.out.println("Lift " + l + " requests: " + l.getRequests());
	}

	System.out.println(row1());
	System.out.println(row2());
	System.out.println(row3());

	String rowWithFloor = constructFloorRow();
	String rowWithoutFloor = constructRowNoFloor();

	Map<Floor, List<String>> map1 = new TreeMap<>();
	Map<Floor, List<String>> map2 = new TreeMap<>();
	createStringsForFloors(map1, map2);

	for (Entry<Floor, List<String>> e : map1.entrySet()) {
	    List<String> value = e.getValue();
	    System.out.format(rowWithFloor,
		    concatWithStream(new Object[] { e.getKey().getFloorNum() }, value.toArray()));
	    if (map2.containsKey(e.getKey())) {
		List<String> list = map2.get(e.getKey());
		System.out.format(rowWithoutFloor, list.toArray());
	    }
	}

	footer();

	System.out.println("------------------------------------------------------------");
    }

    private Object[] concatWithStream(Object[] array1, Object[] array2) {
	return Stream.concat(Arrays.stream(array1), Arrays.stream(array2)).toArray();
    }

    private void createStringsForFloors(Map<Floor, List<String>> map1, Map<Floor, List<String>> map2) {
	for (Floor f : context.getFloors()) {
	    map1.put(f, new ArrayList<>());
	    if (f.getFloorNum() > 1) {
		map2.put(f, new ArrayList<>());
	    }
	}

	for (Floor f : context.getFloors()) {
	    List<String> list1 = map1.get(f);
	    list1.add(f.getElements().stream().map(p -> p.getName().toString()).collect(Collectors.joining()));
	    for (Lift l : context.getLifts()) {
		List<String> list2 = map2.get(f);
		if (l.getState().getCurrent().equals(f)) {
		    String s = l.getElements().stream().map(p -> p.getName().toString())
			    .collect(Collectors.joining("", "[", ""));
		    if (l.getState().isUnderCurrent()) {
			list1.add(SPACE.repeat(l.getCapacity() + 2));
			if (list2 != null)
			    list2.add(s + SPACE.repeat((l.getCapacity() + 1) - s.length()) + "]");
		    } else {
			list1.add(s + SPACE.repeat((l.getCapacity() + 1) - s.length()) + "]");
			if (list2 != null)
			    list2.add(SPACE.repeat(l.getCapacity() + 2));

		    }
		} else {
		    list1.add(SPACE.repeat(l.getCapacity() + 2));
		    if (list2 != null)
			list2.add(SPACE.repeat(l.getCapacity() + 2));

		}
	    }
	    list1.add(f.getPersonsLeaving().stream().map(p -> p.getName().toString()).collect(Collectors.joining()));
	}
    }

    private void footer() {
	int numCharsLifts = context.getLifts().stream().mapToInt(l -> l.getCapacity() + 2).sum()
		+ context.getLifts().size() + 1;
	String eqRow = "=".repeat(15 + numCharsLifts);
	System.out.println(eqRow);

	List<Person> persons = new ArrayList<>();
	for (Floor f : context.getFloors()) {
	    persons.addAll(f.getElements());
	}
	for (Lift l : context.getLifts()) {
	    persons.addAll(l.getElements());
	}

	StringBuilder sb1 = new StringBuilder().append("Putnici: ");
	StringBuilder sb2 = new StringBuilder().append("     od: ");
	StringBuilder sb3 = new StringBuilder().append("     do: ");

	for (Person p : persons) {
	    sb1.append(p.getName());
	    sb2.append(p.getStart().getFloorNum());
	    sb3.append(p.getDestination().getFloorNum());
	}

	System.out.println(sb1.toString());
	System.out.println(sb2.toString());
	System.out.println(sb3.toString());
    }

    private String constructRowNoFloor() {
	StringBuilder sb = new StringBuilder();
	int numLifts = context.getLifts().size();
	sb.append("  ").append("=".repeat(13)).append("|");
	for (int i = 0; i < numLifts; i++) {
	    sb.append("%s|");
	}
	sb.append("\n");
	return sb.toString();
    }

    private String constructFloorRow() {
	StringBuilder sb = new StringBuilder();
	int numLifts = context.getLifts().size();
	sb.append("%d: %12s|");
	for (int i = 0; i < numLifts; i++) {
	    sb.append("%s|");
	}
	sb.append(" %s\n");
	return sb.toString();
    }

    private String row1() {
	StringBuilder sb = new StringBuilder();
	sb.append(SPACE.repeat(15));
	sb.append(SPACE);
	String lift = "Lift";
	int i = 0;
	for (Lift l : context.getLifts()) {
	    i++;
	    int cap = l.getCapacity();
	    int rest = (cap + 2) - (lift.length() + 1);
	    sb.append(SPACE.repeat((rest / 2))).append(lift).append(i).append(SPACE.repeat(rest - (rest / 2)))
		    .append(SPACE);
	}
	return sb.toString();
    }

    private String row2() {
	String s1 = "Smjer/vrata:";
	String s2 = "D Z";
	StringBuilder sb = new StringBuilder();
	sb.append(s1).append(SPACE.repeat(15 - s1.length())).append(SPACE);
	for (Lift l : context.getLifts()) {
	    int cap = l.getCapacity();
	    int rest = (cap + 2) - (s2.length());
	    LiftState state = l.getState();
	    sb.append(SPACE.repeat((rest / 2))).append(state.getDirectionStr()).append(SPACE)
		    .append(state.getDoorsStr()).append(SPACE.repeat(rest - (rest / 2))).append(SPACE);
	}
	return sb.toString();
    }

    private String row3() {
	String s1 = "Stajanja:";
	StringBuilder sb = new StringBuilder();
	sb.append(s1);
	sb.append("=".repeat(15 - s1.length())).append("=");
	int numFloors = context.getFloors().size();
	for (Lift l : context.getLifts()) {
	    int cap = l.getCapacity();
	    int rest = (cap) - (numFloors);
	    String s2 = constructRow3Destinations(numFloors, l.getElements());
	    sb.append("=").append(SPACE.repeat((rest / 2))).append(s2).append(SPACE.repeat(rest - (rest / 2)))
		    .append("==");
	}
	sb.append(" Izašli");
	return sb.toString();
    }

    private String constructRow3Destinations(int numFloors, Set<Person> persons) {
	char[] arr = "_".repeat(numFloors).toCharArray();
	for (Person p : persons) {
	    arr[p.getDestination().getFloorNum() - 1] = '*';
	}

	return new String(arr);
    }
}
