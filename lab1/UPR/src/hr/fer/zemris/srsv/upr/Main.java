package hr.fer.zemris.srsv.upr;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import hr.fer.zemris.srsv.AiPEnums.AiPDirection;
import hr.fer.zemris.srsv.AiPEnums.AiPType;
import hr.fer.zemris.srsv.AiPMessage;
import hr.fer.zemris.srsv.SharedConstants;
import hr.fer.zemris.srsv.State;

public class Main {
	public static final Map<AiPDirection, Integer> objectsOnDirection;

	public static volatile State prev_state = State.ALL_RED;
	public static volatile State current_state = State.ALL_RED;
	public static volatile int numSecondsSinceStateChange = 0;
	public static volatile int currentPeriod = 15;

	private static final ScheduledExecutorService executorService
		= Executors.newSingleThreadScheduledExecutor();

	public static Socket uprSocket;

	static {
		objectsOnDirection = Collections.synchronizedMap(new EnumMap<>(AiPDirection.class));
		objectsOnDirection.put(AiPDirection.SN, 0);
		objectsOnDirection.put(AiPDirection.WE, 0);
	}

	public static void main(String[] args) throws ClassNotFoundException {

		try (ServerSocket serverSocket = new ServerSocket(SharedConstants.UPR_PORT)) {
			System.out.println("UPR is listening on port " + SharedConstants.UPR_PORT);

			Main.uprSocket = new Socket("localhost", SharedConstants.SEM_PORT_UPR);
			scheduleStatePeriod();

			while (true) {
				handleObjectAperance(serverSocket);
			}

		} catch (IOException ex) {
			ex.printStackTrace();
		}

	}

	private static void handleObjectAperance(ServerSocket serverSocket)
		throws IOException, ClassNotFoundException {

		Socket socket = serverSocket.accept();
		System.out.println("New client connected");

		ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(socket.getInputStream()));
		AiPMessage aipMessage = (AiPMessage) ois.readObject();
		System.out.println("UPR received new Object message: " + aipMessage);

		AiPDirection dir = getEqvivalentDirection(aipMessage.getDirection());
		if (dir.equals(AiPDirection.WE)) {
			handleNewWE(aipMessage, dir);
		} else {
			handleNewSN(aipMessage, dir);
		}

		socket.close();
	}

	private static void handleNewSN(AiPMessage aipMessage, AiPDirection dir) {
		switch (current_state) {
			case SN_GREEN: {
				break;
			}
			case SN_GREEN_CARS: {
				if (aipMessage.getType().equals(AiPType.PEDESTRIAN)) {
					objectsNumIncrement(dir);
				}
				break;
			}
			default: {
				objectsNumIncrement(dir);
			}
		}
	}

	private static void handleNewWE(AiPMessage aipMessage, AiPDirection dir) {
		switch (current_state) {
			case WE_GREEN: {
				break;
			}
			case WE_GREEN_CARS: {
				if (aipMessage.getType().equals(AiPType.PEDESTRIAN)) {
					objectsNumIncrement(dir);
				}
				break;
			}
			default: {
				objectsNumIncrement(dir);
			}
		}
	}

	private static void objectsNumIncrement(AiPDirection dir) {
		int value = objectsOnDirection.get(dir) + 1;
		objectsOnDirection.put(dir, value);
	}

	private static void scheduleStatePeriod() {
		executorService.scheduleAtFixedRate(new StateChanger(), 5, 5, TimeUnit.SECONDS);
	}

	private static AiPDirection getEqvivalentDirection(AiPDirection d) {
		switch (d) {
			case SN:
				return AiPDirection.SN;
			case NS:
				return AiPDirection.SN;
			case EW:
				return AiPDirection.WE;
			case WE:
				return AiPDirection.WE;
			default:
				throw new RuntimeException();
		}
	}

	public static AiPDirection getCurrentGreenD() {
		switch (current_state) {
			case ALL_RED:
				return null;
			case SN_GREEN:
				return AiPDirection.SN;
			case SN_GREEN_CARS:
				return AiPDirection.SN;
			case WE_GREEN:
				return AiPDirection.WE;
			case WE_GREEN_CARS:
				return AiPDirection.WE;
			default:
				return null;
		}
	}

}
