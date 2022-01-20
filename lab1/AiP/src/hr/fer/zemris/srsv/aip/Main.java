package hr.fer.zemris.srsv.aip;

import java.io.IOException;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class Main {

	public static final Random random = new Random();
	public static volatile int numAiPs = 0;

	private static ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(4);

	public static void main(String[] args) {
		final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
		executorService.scheduleAtFixedRate(createCarsAndPedestrians(), 5, 5, TimeUnit.SECONDS); // TODO
	}

	private static Runnable createCarsAndPedestrians() {

		return () ->
			{
				if (numAiPs > 30) return;

				int numObjects = numAiPs > 15 ? Main.random.nextInt(5) : Main.random.nextInt(10);
				numAiPs += numObjects;

				for (int i = 0; i < numObjects; i++) {
					try {
						executor.execute(new AiP());
					} catch (ClassNotFoundException | IOException e) {
						e.printStackTrace();
					}

				}
			};

	}

}
