package hr.fer.zemris.srsv.ras;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;

import hr.fer.zemris.srsv.AiPMessage;
import hr.fer.zemris.srsv.SharedConstants;
import hr.fer.zemris.srsv.ras.Constants.Position;
import hr.fer.zemris.srsv.ras.Constants.TrackPosition;

public class Main {

	// TODO communication with sem ?

	private static char[][] screen
		= {
			{ ' ', ' ', ' ', '|', ' ', '|', ' ', '|', ' ', ' ', ' ' },
			{ ' ', ' ', ' ', '|', ' ', '|', ' ', '|', ' ', ' ', ' ' },
			{ ' ', ' ', ' ', '|', ' ', '|', ' ', '|', ' ', ' ', ' ' },
			{ '-', '-', '-', '+', ' ', '|', ' ', '+', '-', '-', '-' },
			{ ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ' },
			{ '-', '-', '-', ' ', ' ', ' ', ' ', ' ', '-', '-', '-' },
			{ ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ' },
			{ '-', '-', '-', '+', ' ', '|', ' ', '+', '-', '-', '-' },
			{ ' ', ' ', ' ', '|', ' ', '|', ' ', '|', ' ', ' ', ' ' },
			{ ' ', ' ', ' ', '|', ' ', '|', ' ', '|', ' ', ' ', ' ' },
			{ ' ', ' ', ' ', '|', ' ', '|', ' ', '|', ' ', ' ', ' ' } };

	public static void main(String[] args) throws ClassNotFoundException {

		try (ServerSocket serverSocket = new ServerSocket(SharedConstants.RAS_PORT)) {
			System.out.println("RAS is listening on port " + SharedConstants.RAS_PORT);
			printScreen();

			while (true) {
				Socket socket = serverSocket.accept();
				System.out.println("New client connected");

				ObjectInputStream ois
					= new ObjectInputStream(new BufferedInputStream(socket.getInputStream()));
				AiPMessage aipMessage = (AiPMessage) ois.readObject();

				System.out.println("------------------");
				System.out.println("RAS received new message: " + aipMessage);
				System.out.println("------------------");

				handleAiPMessage(aipMessage);
				printScreen();

				socket.close();
			}

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private static void printScreen() {
		System.out.println("            (sjever)");
		System.out.println();
		System.out.println("          " + new String(screen[0]));
		System.out.println("          " + new String(screen[1]));
		System.out.println("          " + new String(screen[2]));
		System.out.println("          " + new String(screen[3]));
		System.out.println("          " + new String(screen[4]));
		System.out.println("(zapad)   " + new String(screen[5]) + "   (istok)");
		System.out.println("          " + new String(screen[6]));
		System.out.println("          " + new String(screen[7]));
		System.out.println("          " + new String(screen[8]));
		System.out.println("          " + new String(screen[9]));
		System.out.println("          " + new String(screen[10]));
		System.out.println();
		System.out.println("            (jug)");

	}

	private static void handleAiPMessage(AiPMessage aipMessage) {
		switch (aipMessage.getStatus()) {
			case WAITING: {
				handleNewWaiting(aipMessage);
				break;
			}
			case MOVING: {
				handleNewMoving(aipMessage);
				break;
			}
			case DONE: {
				handleNewDone(aipMessage);
				break;
			}
		}

		//updateScreen();

	}

	private static void updateScreen(Position p) {
		/*
		 * Constants.positionCounters.entrySet().forEach(e -> {
		 */
		if (Constants.waitingPedestrians.contains(p)) {
			updateScreenWaitingPedestrian(p);
		} else if (Constants.waitingCars.contains(p)) {
			updateScreenWaitingCar(p);
		} else if (Constants.movingPedestrians.contains(p)) {
			updateScreenMovingPedestrian(p);
		} else if (Constants.movingCars.contains(p)) {
			updateScreenMovingCars(p);
		} else {
			throw new RuntimeException("Unknown position");
		}
		/* }); */
	}

	private static void updateScreenMovingCars(Position p) {
		TrackPosition tp = Constants.getCarTrackPosition(p);
		if (Constants.positionCounters.get(p) > 0) {
			setCarTrack(tp, 'A');
		} else {
			setCarTrack(tp, ' ');
		}
	}

	private static void setCarTrack(TrackPosition tp, char ch) {
		for (Position p : Constants.carTrackPositions.get(tp)) {
			screen[p.y][p.x] = ch;
		}
	}

	private static void updateScreenMovingPedestrian(Position p) {
		TrackPosition tp = Constants.getPedestrianTrackPosition(p);
		if (Constants.positionCounters.get(p) > 0) {
			setPedestrianTrack(tp, 'p');
		} else {
			setPedestrianTrack(tp, ' ');
		}
	}

	private static void setPedestrianTrack(TrackPosition tp, char ch) {
		for (Position p : Constants.pedestrianTrackPositions.get(tp)) {
			screen[p.y][p.x] = ch;
		}
	}

	private static void updateScreenWaitingCar(Position p) {
		if (Constants.positionCounters.get(p) > 0) {
			screen[p.y][p.x] = 'A';
		} else {
			screen[p.y][p.x] = ' ';
		}
	}

	private static void updateScreenWaitingPedestrian(Position p) {
		if (Constants.positionCounters.get(p) > 0) {
			screen[p.y][p.x] = 'p';
		} else {
			screen[p.y][p.x] = ' ';
		}
	}

	private static void handleNewWaiting(AiPMessage aipMessage) {
		Position p = null;
		switch (aipMessage.getType()) {
			case PEDESTRIAN: {
				p = getPedestrianWaitingP(aipMessage);
				break;
			}
			case CAR: {
				p = getCarsWaitingP(aipMessage);
				break;
			}
		}
		incrementPosition(p);
		updateScreen(p);
	}

	private static void handleNewMoving(AiPMessage aipMessage) {
		Position old = null;
		Position nw = null;
		switch (aipMessage.getType()) {
			case PEDESTRIAN: {
				old = getPedestrianWaitingP(aipMessage);
				nw = getPedestrianMovingP(aipMessage);
				break;
			}
			case CAR: {
				old = getCarsWaitingP(aipMessage);
				nw = getCarsMovingP(aipMessage);
				break;
			}
		}
		decrementPosition(old);
		incrementPosition(nw);
		updateScreen(old);
		updateScreen(nw);
	}

	private static void handleNewDone(AiPMessage aipMessage) {
		Position old = null;
		switch (aipMessage.getType()) {
			case PEDESTRIAN: {
				old = getPedestrianMovingP(aipMessage);
				break;
			}
			case CAR: {
				old = getCarsMovingP(aipMessage);
				break;
			}
		}
		decrementPosition(old);
		updateScreen(old);
	}

	private static Position getCarsWaitingP(AiPMessage aipMessage) {
		return Constants.waitingCarsPositionsMap.get(aipMessage.getDirection());
	}

	private static void incrementPosition(Position p) {
		int curr = Constants.positionCounters.get(p) + 1;
		Constants.positionCounters.put(p, curr);
	}

	private static void decrementPosition(Position p) {
		int curr = Constants.positionCounters.get(p) - 1;
		Constants.positionCounters.put(p, curr);
	}

	private static Position getPedestrianWaitingP(AiPMessage aipMessage) {
		return Constants.waitingPedestrianPositionsMap
			.get(aipMessage.getPosition())
			.get(aipMessage.getDirection());
	}

	private static Position getCarsMovingP(AiPMessage aipMessage) {
		return Constants.movingCarsPositionsMap.get(aipMessage.getDirection());
	}

	private static Position getPedestrianMovingP(AiPMessage aipMessage) {
		return Constants.movingPedestrianPositionsMap
			.get(aipMessage.getPosition())
			.get(aipMessage.getDirection());
	}

}
