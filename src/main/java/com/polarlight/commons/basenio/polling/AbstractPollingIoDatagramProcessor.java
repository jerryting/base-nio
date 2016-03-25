package com.polarlight.commons.basenio.polling;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.channels.ClosedSelectorException;
import java.nio.channels.SelectionKey;
import java.util.Iterator;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.polarlight.commons.basenio.exception.HandlerNoDefineException;
import com.polarlight.commons.basenio.filterchain.bound.BaseBoundaryFilterChain;
import com.polarlight.commons.basenio.filterchain.bound.DefaultBoundaryIoFilter;
import com.polarlight.commons.basenio.filterchain.bound.IoBoundaryFilterChain;
import com.polarlight.commons.basenio.io.session.stateless.AbstractStatelessSession;
import com.polarlight.commons.basenio.io.session.stateless.IoStatelessSession;
import com.polarlight.commons.basenio.service.IoDatagramHandler;
import com.polarlight.commons.basenio.utils.NamingRunnable;

/**
 * 
 * @author DJ
 * @since 2.0
 * @param <S>
 */
public abstract class AbstractPollingIoDatagramProcessor<S extends AbstractStatelessSession> {

	private final Logger logger = LoggerFactory.getLogger(getClass());
	/***/
	private final static int SELECTOR_MS = 1;

	/** thread pool executor */
	protected Executor executor = null;

	/** inner thread atomic reference */
	private AtomicReference<Processor> processorRef = new AtomicReference<Processor>();

	/** thread counter */
	private static AtomicLong idGenerator = new AtomicLong(0);

	private String threadName = "";

	/** UDP channel */
	// protected DatagramChannel dch;

	/** current UDP session */
	private S session;

	protected IoBoundaryFilterChain filterChain;

	protected int sessionBufSize = 0;

	private volatile boolean stopRun = false;

	private IoDatagramHandler handler = null;

	/**
	 * 
	 * @return
	 */
	protected abstract Iterator<SelectionKey> selectKeys();

	/**
	 * @param sa
	 * @return
	 * @throws IOException
	 */
	protected abstract void connectLocal(SocketAddress sa) throws IOException;

	/**
	 * @param selectms
	 * @return
	 * @throws IOException
	 */
	protected abstract int select(int selectms) throws ClosedSelectorException, IOException;

	/**
	 * 
	 * @return
	 */
	protected abstract S initSession();

	/***
	 * 
	 * @param session
	 * @return
	 * @throws IOException
	 */
	protected abstract byte[] read(S session) throws IOException;

	/***
	 * 
	 * @param session
	 */
	protected abstract void write(S session);

	/**
	 * 
	 * @param executor
	 */
	public AbstractPollingIoDatagramProcessor(Executor executor) {
		this.executor = executor;
		this.threadName = getClass().getSimpleName() + "-" + idGenerator.incrementAndGet();
		this.filterChain = new BaseBoundaryFilterChain();
		this.filterChain.addFilter("DEFAULT", new DefaultBoundaryIoFilter());
	}

	/**
	 * @param sa
	 * @throws IOException
	 * @throws HandlerNoDefineException
	 */
	public final IoStatelessSession connect(SocketAddress sa) throws IOException, HandlerNoDefineException {
		if (handler == null) {
			throw new HandlerNoDefineException("Handler No Defined");
		}
		try {

			connectLocal(sa);

			session = initSession();

		} catch (IOException e) {
			logger.warn("connect: ", e);
			throw e;
		}
		startProcessor();
		return session;
	}

	/**
	 * startup inner processor thread for channel pooling
	 */
	private void startProcessor() {
		Processor processor = processorRef.get();
		if (processor == null) {
			processor = new Processor();
			processorRef.set(processor);
			executor.execute(new NamingRunnable(processor, this.threadName));
		}
	}

	protected void close() {
		this.stopRun = true;
		ExecutorService es = (ExecutorService) executor;
		try {
			es.shutdownNow();
		} catch (Exception e) {
		}
	}

	/**
	 * @throws IOException
	 * 
	 */
	private void process() throws IOException {
		Iterator<SelectionKey> iterator = selectKeys();
		while (iterator.hasNext()) {
			SelectionKey sk = iterator.next();
			iterator.remove();
			if (sk.isValid() && sk.isReadable()) {
				// System.out.println("r "+ sk.isWritable());
				byte[] b = null;
				b = read(session);
				if (b != null) {
					session.getHandler().messageReceived(session, b);
				}
			} else if (sk.isValid() && sk.isWritable()) {
				// System.out.println("w "+ sk.isReadable());
				write(session);
			}
		}
	}

	public int getSessionBufSize() {
		return sessionBufSize;
	}

	public void setSessionBufSize(int sessionBufSize) {
		this.sessionBufSize = sessionBufSize;
	}

	public IoDatagramHandler getHandler() {
		return handler;
	}

	public void setHandler(IoDatagramHandler handler) {
		this.handler = handler;
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
					int selected = select(SELECTOR_MS);

					if (selected > 0) {
						process();
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