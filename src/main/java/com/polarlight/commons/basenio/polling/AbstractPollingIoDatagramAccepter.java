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
public abstract class AbstractPollingIoDatagramAccepter<S extends AbstractStatelessSession> {

	private final Logger logger = LoggerFactory.getLogger(getClass());
	/*
	 * */
	private final static int SELECTOR_MS = 1;

	protected Executor executor = null;

	private AtomicReference<Processor> processorRef = new AtomicReference<Processor>();

	private static AtomicLong idGenerator = new AtomicLong(0);

	private String threadName = "";

	// protected DatagramChannel servCh;

	protected IoBoundaryFilterChain filterChain;

	protected int sessionBufSize = 0;

	private volatile boolean stopRun = false;

	private IoDatagramHandler handler = null;

	protected S servSessionMain;

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
	protected abstract void bindLocal(SocketAddress sa) throws IOException;

	/**
	 * @param selectms
	 * @return
	 * @throws IOException
	 */
	protected abstract int select(int selectms) throws ClosedSelectorException, IOException;

	/**
	 * @throws IOException
	 */
	protected abstract void init() throws IOException;

	/**
	 * @param session
	 * @return
	 * @throws IOException
	 */
	protected abstract byte[] read(S session) throws IOException;

	/**
	 * @param session
	 */
	protected abstract void write(S session);

	/**
	 * init client_mode session
	 * 
	 * @param sk
	 * @return
	 */
	protected abstract S initSession(SelectionKey sk);

	/**
	 * 
	 * @param executor
	 */
	public AbstractPollingIoDatagramAccepter(Executor executor) {
		this.executor = executor;
		this.threadName = getClass().getSimpleName() + "-" + idGenerator.incrementAndGet();
		this.filterChain = new BaseBoundaryFilterChain();
		this.filterChain.addFilter("DEFAULT", new DefaultBoundaryIoFilter());
	}

	/**
	 * bind a port for udp server
	 * 
	 * @param sa
	 * @return server stateless main session that can be used to [sendto]
	 *         message to any remote address
	 * @throws IOException
	 * @throws HandlerNoDefineException
	 */
	public final IoStatelessSession bind(final SocketAddress sa) throws IOException, HandlerNoDefineException {
		if (handler == null) {
			throw new HandlerNoDefineException("Handler No Defined");
		}
		try {
			init();

			bindLocal(sa);

		} catch (IOException e) {
			logger.warn("bind: ", e);
			throw e;
		}
		startProcessor();
		return servSessionMain;
	}

	/**
	 * 
	 */
	private void startProcessor() {
		Processor processor = processorRef.get();
		if (processor == null) {
			processor = new Processor();
			processorRef.set(processor);
			executor.execute(new NamingRunnable(processor, this.threadName));
		}
	}

	/**
	 * shutdown the server, stop run all thread and NIO server module
	 */
	protected void shutdown() {
		this.stopRun = true;
		ExecutorService es = (ExecutorService) executor;
		try {
			es.shutdownNow();
		} catch (Exception e) {
		}
	}

	/**
	 * 
	 */
	private void process() {
		Iterator<SelectionKey> iterator = selectKeys();
		while (iterator.hasNext()) {
			SelectionKey sk = iterator.next();
			iterator.remove();
			if (sk.isValid() && sk.isReadable()) {
				S session = initSession(sk);
				byte[] b = null;
				try {
					b = read(session);
				} catch (IOException e) {
					e.printStackTrace();
				}
				if (b != null) {
					session.getHandler().messageReceived(session, b); // fire
				}

			} else if (sk.isValid() && sk.isWritable()) {
				write(servSessionMain);
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

			logger.debug("accepter thread startup... ...");
			while (!stopRun) {
				try {

					int selected = select(SELECTOR_MS);

					if (selected > 0) {
						process();
					}

				} catch (ClosedSelectorException e) {
					logger.error("accepter: " + e.toString(), e);
					break;
				} catch (Exception e) {
					logger.error("accepter: " + e.toString(), e);
				}
			}
			logger.debug("accepter thread shutdown... ...");
		}
	}
}