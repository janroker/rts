package hr.fer.zemris.srsv.sem;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Iterator;

import hr.fer.zemris.srsv.AiPEnums.AiPStatus;
import hr.fer.zemris.srsv.AiPMessage;
import hr.fer.zemris.srsv.SEMMessage;
import hr.fer.zemris.srsv.SharedConstants;
import hr.fer.zemris.srsv.State;
import hr.fer.zemris.srsv.UPRMessage;

public class Main {
	private static volatile State currentState;

	public static void main(String[] args) throws ClassNotFoundException {
		serverStart();
	}

	private static void serverStart() throws ClassNotFoundException {
		try (
			ServerSocket uprServerSocket = new ServerSocket(SharedConstants.SEM_PORT_UPR);
			ServerSocket aipServerSocket = new ServerSocket(SharedConstants.SEM_PORT_AIP)
		) {

			System.out
				.println(
					"SEM is listening on ports "
						+ SharedConstants.SEM_PORT_UPR + ", " + SharedConstants.SEM_PORT_AIP);

			uprServerSocketThread(uprServerSocket).start();

			while (true)
				acceptAiP(aipServerSocket);
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	private static void acceptAiP(ServerSocket aipServerSocket) throws IOException, ClassNotFoundException {
		Socket socket = aipServerSocket.accept();
		handleObjectApperance(socket);
	}

	private static void handleObjectApperance(Socket socket) throws IOException, ClassNotFoundException {
		ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(socket.getInputStream()));
		AiPMessage aipMessage = (AiPMessage) ois.readObject();

		System.out.println("New object connected: " + aipMessage);

		if (aipMessage.getStatus() != AiPStatus.WAITING) return; // TODO wrong status?

		switch (aipMessage.getType()) {
			case CAR: {
				handleCarApperance(socket, aipMessage);
				break;
			}
			case PEDESTRIAN: {
				handlePedestrianApperance(socket, aipMessage);
				break;
			}
		}

	}

	private static void handlePedestrianApperance(Socket socket, AiPMessage aipMessage) throws IOException {
		State state = Constants.mappedStatesPedestrians.get(aipMessage.getDirection());
		if (state.equals(currentState)) {
			sendNotification(socket);
			return;
		}

		Constants.waitingMapPedestrians.get(state).add(socket);
	}

	private static void handleCarApperance(Socket socket, AiPMessage aipMessage) throws IOException {
		State state = Constants.mappedStatesCars.get(aipMessage.getDirection());
		State eqvivalentCurState = Constants.equalStatesCars.get(currentState);
		if (state.equals(eqvivalentCurState)) {
			sendNotification(socket);
			return;
		}

		Constants.waitingMapCars.get(state).add(socket);
	}

	private static void sendNotification(Socket socket) throws IOException {
		ObjectOutputStream oos = new ObjectOutputStream(new BufferedOutputStream(socket.getOutputStream()));
		oos.writeObject(new SEMMessage(true));
		oos.flush();
		System.out.println("Sent notification about green light");
		socket.close();
	}

	private static void communicateWithUPR(Socket socket) throws IOException, ClassNotFoundException {
		ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(socket.getInputStream()));
		UPRMessage uprMessage = (UPRMessage) ois.readObject();
		Main.currentState = uprMessage.getCurrentState();
		System.out.println("Received current state change notification from UPR: " + uprMessage);
		notifySubscribers();
	}

	private static void notifySubscribers() throws IOException {
		Iterator<Socket> itCars = Constants.waitingMapCars.get(currentState).iterator();
		Iterator<Socket> itPedestrians = Constants.waitingMapPedestrians.get(currentState).iterator();

		iterateSubscribers(itCars);
		iterateSubscribers(itPedestrians);
	}

	private static void iterateSubscribers(Iterator<Socket> itCars) throws IOException {
		while (itCars.hasNext()) {
			sendNotification(itCars.next());
			itCars.remove();
		}

	}

	private static Thread uprServerSocketThread(ServerSocket serverSocket) {
		return new Thread() {
			@Override
			public void run() {
				while (true) {
					try (Socket socket = serverSocket.accept()) {
						while (true) {
							communicateWithUPR(socket);
						}
					} catch (IOException | ClassNotFoundException e) {
						e.printStackTrace();
					}
				}
			}
		};
	}

}
