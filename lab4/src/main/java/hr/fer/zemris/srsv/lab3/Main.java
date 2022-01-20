package hr.fer.zemris.srsv.lab3;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import hr.fer.zemris.srsv.lab3.context.Context;

public class Main {

	public static void main(String[] args) throws InterruptedException, ExecutionException {
		Context context = new Context(4, 6, 10);

		ScheduledExecutorService ses = Executors.newSingleThreadScheduledExecutor();
		ses.scheduleAtFixedRate(new PeriodicAction(context, 1, 1), 1000, 1000, TimeUnit.MILLISECONDS).get();
	}

}
