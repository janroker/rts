package hr.fer.zemris.srsv.aip;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import hr.fer.zemris.srsv.AiPEnums.AiPDirection;
import hr.fer.zemris.srsv.AiPEnums.AiPPosition;
import hr.fer.zemris.srsv.AiPEnums.AiPStatus;
import hr.fer.zemris.srsv.AiPEnums.AiPType;
import hr.fer.zemris.srsv.AiPMessage;
import hr.fer.zemris.srsv.SEMMessage;
import hr.fer.zemris.srsv.SharedConstants;

public class AiP implements Runnable {
	private static volatile AtomicInteger COUNT = new AtomicInteger(1);
	private static AiPType[] types = AiPType.values();
	private static AiPDirection[] directions = AiPDirection.values();
	private static Map<AiPPosition, AiPDirection[]> positionDirections;
	private static AiPPosition[] positions = AiPPosition.values();

	static {
		positionDirections = new EnumMap<>(AiPPosition.class);
		positionDirections.put(AiPPosition.SE, new AiPDirection[] { AiPDirection.SN, AiPDirection.EW });
		positionDirections.put(AiPPosition.NE, new AiPDirection[] { AiPDirection.NS, AiPDirection.EW });
		positionDirections.put(AiPPosition.NW, new AiPDirection[] { AiPDirection.NS, AiPDirection.WE });
		positionDirections.put(AiPPosition.SW, new AiPDirection[] { AiPDirection.SN, AiPDirection.WE });
	}

	private Socket semSocket;

	private int id;
	private AiPType type;
	private AiPDirection direction;
	private AiPStatus status;
	private AiPPosition position;

	public AiP() throws IOException, ClassNotFoundException {
		this.semSocket = new Socket(InetAddress.getByName("localhost"), SharedConstants.SEM_PORT_AIP);
		this.init();
		System.out.println("New car or pedestrian: " + this);
	}

	@Override
	public void run() {
		try {
			this.sendSpawnedMessage();
			this.subscribeOnSem();
			this.job();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void init() {
		this.id = COUNT.getAndIncrement();
		this.type = types[Main.random.nextInt(types.length)];
		this.position
			= this.type.equals(AiPType.CAR) ? null : positions[Main.random.nextInt(positions.length)];
		this.direction
			= this.type.equals(AiPType.CAR)
				? directions[Main.random.nextInt(directions.length)]
				: positionDirections.get(this.position)[Main.random.nextInt(2)];
		this.status = AiPStatus.WAITING;
	}

	private void sendSpawnedMessage() throws IOException {
		AiPMessage msg = new AiPMessage(id, type, direction, status, position);
		try (
			Socket uprSocket = new Socket(InetAddress.getByName("localhost"), SharedConstants.UPR_PORT);
			Socket rasSocket = new Socket(InetAddress.getByName("localhost"), SharedConstants.RAS_PORT);
		) {
			List.of(uprSocket, rasSocket).forEach(s -> sendMessage(msg, s));
		}
		System.out.format("Object id:%d sent spawned message to UPR and RAS\n", this.id);
	}

	private void subscribeOnSem() throws IOException {
		AiPMessage msg = new AiPMessage(id, type, direction, status, position);
		this.sendMessage(msg, semSocket);
		System.out.println("Sent message: " + msg + " to SEM");
	}

	private void job() throws IOException, ClassNotFoundException {
		System.out.println("Object id " + this.id + " waiting for green light");
		waitForGreenLight();
		System.out.println("Object id " + this.id + " green light came... moving");
		this.status = AiPStatus.MOVING;
		publishStatusToRas();
		passTheRas();
		this.status = AiPStatus.DONE;
		publishStatusToRas();
		this.close();
	}

	private void passTheRas() {
		System.out.println("Object id:" + this.id + " passing the intersection ");
		long milis
			= System.currentTimeMillis()
				+ (this.type == AiPType.CAR ? SharedConstants.carPassage : SharedConstants.pedestrianPassage)
					* 1000;
		while (true) {
			try {
				Thread.sleep(milis - System.currentTimeMillis());
				break;
			} catch (InterruptedException e) {
				// ignore
			}
		}
		System.out.println("Object id:" + this.id + " passed the intersection ");
	}

	private void publishStatusToRas() throws IOException {
		AiPMessage msg = new AiPMessage(id, type, direction, status, position);
		try (Socket socket = new Socket(InetAddress.getByName("localhost"), SharedConstants.RAS_PORT)) {
			this.sendMessage(msg, socket);
		}
		System.out.println("Sent message to RAS: " + msg);
	}

	private void waitForGreenLight() throws IOException, ClassNotFoundException {
		ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(semSocket.getInputStream()));
		SEMMessage semMessage = (SEMMessage) ois.readObject();
		System.out.println(semMessage);
	}

	private void sendMessage(AiPMessage msg, Socket socket) {
		try {
			ObjectOutputStream oos
				= new ObjectOutputStream(new BufferedOutputStream(socket.getOutputStream()));
			oos.writeObject(msg);
			oos.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void close() {
		try {
			this.semSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		Main.numAiPs--;
		System.out.println("Object id:" + this.id + " done");
	}

	@Override
	public String toString() {
		return "AiP [id="
			+ id + ", type=" + type + ", direction=" + direction + ", status=" + status + ", position="
			+ position + "]";
	}

}
