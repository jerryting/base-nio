package com.polarlight.commons.basenio.utils;
/**
 * rename the thread name .
 * @author DJ
 *
 */
public class NamingRunnable implements Runnable {

	/* runnable handler */
	private final Runnable runnable;

	/* runnable identifier */
	private final String threadName;
	/* constructor */
	public NamingRunnable(Runnable runnable, String threadName) {
		this.runnable = runnable;
		this.threadName = threadName;
	}

	/* entrance of thread executor */
	@Override
	public void run() {
		
		Thread currentT = Thread.currentThread();
		String rawName = currentT.getName();
		if (threadName != null) {
			currentT.setName(this.threadName);
		} else {
			currentT.setName(rawName);
		}
		runnable.run();
	}
}
