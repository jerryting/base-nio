package com.polarlight.commons.basenio.polling;

import java.io.IOException;
import java.nio.channels.ClosedSelectorException;
import java.nio.channels.SelectionKey;
import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.polarlight.commons.basenio.io.session.state.AbstractStateSession;
import com.polarlight.commons.basenio.process.IoProcessor;
import com.polarlight.commons.basenio.utils.NamingRunnable;

/**
 * 
 * @author DJ
 * @since 2.0
 * @param <S>
 */
public abstract class AbstractPollingIoProcessor<S extends AbstractStateSession> implements IoProcessor<S> {

	private final Logger logger = LoggerFactory.getLogger(getClass());
	/*
	 * */
	private final static int SELECTOR_MS = 1;

	protected Executor executor = null;

	private AtomicReference<Processor> processorRef = new AtomicReference<Processor>();

	/** will be registered session queue */
	private Queue<S> sessionRegisterQueue = new ConcurrentLinkedQueue<S>();

	/** current valid session queue */
	private Queue<S> sessionCurValidQueue = new ConcurrentLinkedQueue<S>();

	/** will be unregestered session queue */
	private Queue<S> sessionUnregisterQueue = new ConcurrentLinkedQueue<S>();

	private static AtomicLong idGenerator = new AtomicLong(0);

	private String threadName = "";

	/** whether existing TCP socket client disconnection */
	private AtomicBoolean isUnregister = new AtomicBoolean(false);

	/***/
	protected volatile boolean stopRun = false;

	/**
	 * 
	 * @return
	 */
	protected abstract Iterator<SelectionKey> selectKeys();

	/**
	 * to initialize a new session
	 * 
	 * @param session
	 * @return
	 * @throws IOException
	 */
	protected abstract boolean init(S session) throws IOException;

	/**
	 * @param selectms
	 * @return
	 * @throws IOException
	 */
	protected abstract int select(int selectms) throws ClosedSelectorException, IOException;

	/**
	 * 
	 */
	protected abstract void wakeup();

	/**
	 * 
	 * @param session
	 * @return
	 * @throws IOException
	 */
	protected abstract byte[] read(S session) throws IOException;

	/**
	 * 
	 * @param session
	 */
	protected abstract void write(S session);

	/**
	 * 
	 * @param executor
	 */
	public AbstractPollingIoProcessor(Executor executor) {

		this.executor = executor;
		this.threadName = getClass().getSimpleName() + "-" + idGenerator.incrementAndGet();
	}

	@Override
	public void add(S session) {
		if (session != null) {
			sessionRegisterQueue.add(session);
			starupProcessor();
		}
	}

	/**
	 * 
	 */
	private void starupProcessor() {
		Processor processor = processorRef.get();
		if (processor == null) {
			processor = new Processor();
			processorRef.set(processor);
			executor.execute(new NamingRunnable(processor, this.threadName));
		}
	}

	/**
	 * @throws IOException
	 */
	private void registerNewSession() throws IOException {
		int size = sessionRegisterQueue.size();
		for (int i = 0; i < size; i++) { // maybe, isEmpty() will be a high
											// performance
			S session = sessionRegisterQueue.poll();
			if (init(session)) {
				sessionCurValidQueue.add(session);
				session.getHandler().socketCreated(session);
			}
		}
		// if(size >0){
		// wakeup();
		// }
	}

	@Override
	public synchronized void destroy(S session) {

		sessionUnregisterQueue.add(session);
		sessionCurValidQueue.remove(session);
		isUnregister.set(true);
		// this.wakeup();
	}

	/**
	 * 
	 */
	private void unregisterSession() {

		int size = sessionUnregisterQueue.size();
		for (int i = 0; i < size; i++) {// maybe, isEmpty() will be a high
										// performance
			S session = sessionUnregisterQueue.poll();
			SelectionKey sk = session.getSelectionKey();
			try {
				sk.channel().close();
			} catch (IOException e) {
				logger.error("session close: " + session, e);
			}
			sk.cancel();
			logger.debug("session close: " + session);
			session.getHandler().socketClosed(session);
			session = null;
		}
		isUnregister.set(false);
	}

	/**
	 * @throws
	 */
	@SuppressWarnings("unchecked")
	void process() {
		Iterator<SelectionKey> iterator = selectKeys();
		while (iterator.hasNext()) {

			SelectionKey sk = iterator.next();
			iterator.remove();
			S session = (S) sk.attachment();
			if (sk.isValid() && sk.isReadable()) {

				// invalid session
				if (!this.isValid(session)) { // defense
					continue;
				}

				byte[] b = null;
				try {
					b = read(session);
				} catch (IOException e) {
					session.setTimeoutFlag();
				}

				if (b != null) {
					session.getHandler().messageReceived(session, b); // fire
																		// handler
																		// [message]
				}

			} else if (sk.isValid() && sk.isWritable()) {
				write(session);
			}
		}
	}

	private void checkSessionsStatus() {

		for (S session : sessionCurValidQueue) {
			if (session.isClosing()) {
				this.destroy(session);
			}
		}

	}

	@Override
	public synchronized boolean isValid(S session) {

		if (!sessionCurValidQueue.contains(session)) { // defense
			if (!sessionUnregisterQueue.contains(session)) {
				sessionUnregisterQueue.add(session);
			}
			return false;
		} else {
			return true;
		}
	}

	/**
	 * processor polling inner thread class
	 * 
	 * @author DJ
	 */
	private class Processor implements Runnable {
		@Override
		public void run() {

			logger.debug("processor thread startup... ...");
			while (!stopRun) {
				try {

					checkSessionsStatus();

					int selected = select(SELECTOR_MS);

					registerNewSession();

					if (selected > 0) {
						process();
					}

					if (isUnregister.get()) {
						unregisterSession();
					}

				} catch (ClosedSelectorException e) {
					logger.error("processor: " + e.toString(), e);
					break;
				} catch (Exception e) {
					logger.error("processor: " + e.toString(), e);
				}
			}
			logger.debug("processor thread shutdown... ...");
		}
	}
}