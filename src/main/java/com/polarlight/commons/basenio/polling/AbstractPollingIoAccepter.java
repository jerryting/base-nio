package com.polarlight.commons.basenio.polling;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.channels.ClosedSelectorException;
import java.nio.channels.SelectionKey;
import java.util.Iterator;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.polarlight.commons.basenio.exception.HandlerNoDefineException;
import com.polarlight.commons.basenio.exception.ProcessorPoolException;
import com.polarlight.commons.basenio.filterchain.BaseFilterChain;
import com.polarlight.commons.basenio.filterchain.DefaultUnboundaryIoFilter;
import com.polarlight.commons.basenio.filterchain.IoFilterChain;
import com.polarlight.commons.basenio.io.session.state.AbstractStateSession;
import com.polarlight.commons.basenio.process.IoProcessor;
import com.polarlight.commons.basenio.process.NioProcessPool;
import com.polarlight.commons.basenio.service.IoHandler;
import com.polarlight.commons.basenio.utils.NamingRunnable;

/**
 * @author DJ
 *
 * @param <S>
 */
public abstract class AbstractPollingIoAccepter<S extends AbstractStateSession> {

	private final Logger logger = LoggerFactory.getLogger(getClass().getSimpleName());
	/** thread pool executor */
	private final Executor executor;

	/** callback handle object(synchronized callback & singleton) */
	private IoHandler handler = null;

	/** TCP client I/O processing sub thread (fixed size pool) */
	private IoProcessor<S> processor;

	/** BYTE data processing obj */
	protected IoFilterChain filterChain;

	/** socket client 's idle time by millisecond */
	private long maxSessionIdleMS = 0;

	/** socket client read-buffer size */
	private int sessionBufSize = 0;

	/** inner thread object reference */
	private AtomicReference<Accepter> atomicAccepter = new AtomicReference<Accepter>();

	/** thread counter */
	private static AtomicLong idGenerator = new AtomicLong(0);

	/** current thread name */
	private String threadName;

	/** thread running & stopping flag */
	protected volatile boolean stopRun = false;

	/** the max processor size by getting machine CPUT count */
	private static int JVM_PROCESSOR_COUNT = 0;

	private final static int SELECTOR_MS = 1;

	/**
	 * inner method for listening
	 * 
	 * @param sa
	 * @return
	 * @throws IOException
	 */
	protected abstract void bindLocal(SocketAddress sa) throws IOException;

	/**
	 * polling server main channel
	 * 
	 * @return
	 * @throws IOException
	 */
	protected abstract int select(int selectms) throws ClosedSelectorException, IOException;

	/**
	 * return ready-set
	 * 
	 * @return
	 */
	protected abstract Iterator<SelectionKey> selectKeys();

	/**
	 * initialize server socket
	 * 
	 * @throws IOException
	 */
	protected abstract void init() throws IOException;

	/**
	 * accept socket client method
	 * 
	 * @param sk
	 * @return
	 * @throws IOException
	 */
	protected abstract S accept(SelectionKey sk) throws IOException;

	// All Exception reserved temporarily
	public AbstractPollingIoAccepter(Class<? extends IoProcessor<S>> processorType, Executor executor,
			int processorSize) throws ProcessorPoolException {
		if (executor == null) {
			this.executor = Executors.newCachedThreadPool();
		} else {
			this.executor = executor;
		}
		this.filterChain = new BaseFilterChain();
		this.filterChain.addFilter("DEFAULT", new DefaultUnboundaryIoFilter());
		this.processor = new NioProcessPool<S>(processorType, processorSize);
		this.threadName = getClass().getSimpleName() + "-" + idGenerator.incrementAndGet();
	}

	public AbstractPollingIoAccepter(Class<? extends IoProcessor<S>> processorType, Executor executor)
			throws ProcessorPoolException {
		this(processorType, executor, JVM_PROCESSOR_COUNT);
	}

	/**
	 * get current JVM available processor count NOTICE : MAX for android device
	 * is 4; DEFAULT is 2 MAX for standard server is (count+1)
	 * 
	 * @return
	 */
	static {
		int count = Runtime.getRuntime().availableProcessors();
		JVM_PROCESSOR_COUNT = (count <= 0 || count >= 4) ? 2 : count + 1;
		// JVM_PROCESSOR_COUNT = count + 1; // For standard server
	}

	/***
	 * bind a host:port to local & startup listen thread
	 * 
	 * @param sa
	 *            a host:port SocketAddress
	 * @throws HandlerNoDefineException
	 * @throws IOException
	 */
	public final void bind(SocketAddress sa) throws HandlerNoDefineException, IOException {

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

		startAccepter();
	}

	/**
	 * start up accepter thread
	 */
	private void startAccepter() {
		Accepter acc = atomicAccepter.get();
		if (acc == null) {
			acc = new Accepter();
			atomicAccepter.set(acc);
			executor.execute(new NamingRunnable(acc, this.threadName));
		}
	}

	/**
	 * shutdown the server, stop run all thread and NIO server module
	 */
	protected void shutdown() {
		this.stopRun = true;
		try {
			this.processor.close();
		} catch (IOException e) {
			logger.error("server shutdown: " + e.toString(), e);
		}
		ExecutorService es = (ExecutorService) executor;
		try {
			es.shutdownNow();
		} catch (Exception e) {
		}
	}

	/**
	 * set main callback handler
	 * 
	 * @param handler
	 *            Class<? implements IoHandler > instance
	 * @throws HandlerNoDefineException
	 */
	public void setHandler(final IoHandler handler) throws HandlerNoDefineException {
		if (handler == null) {
			throw new HandlerNoDefineException("Handler No Defined");
		}
		this.handler = handler;
	}

	public IoHandler getHandler() {
		return this.handler;
	}

	/**
	 * process OP_ACCEPT event, and then initialize a TCP session, After that,
	 * dispatch the session to one processor thread
	 * 
	 * @param iterator
	 * @throws IOException
	 */
	private void processConnection(Iterator<SelectionKey> iterator) throws IOException {
		while (iterator.hasNext()) {
			SelectionKey sk = iterator.next();
			iterator.remove();
			S session = accept(sk);
			if (session != null) {
				processor.add(session);
			}
		}
	}

	public long getMaxSessionIdleMS() {
		return maxSessionIdleMS;
	}

	/**
	 * set socket state session 's max idle time span(millisecond)
	 * 
	 * @param maxSessionIdleMS
	 *            if value<=0 . default value will be used (Long.MAXVALUE)
	 */
	public void setMaxSessionIdleMS(long maxSessionIdleMS) {
		this.maxSessionIdleMS = maxSessionIdleMS;
	}

	public int getSessionBufSize() {
		return sessionBufSize;
	}

	/**
	 * set socket state session 's channel buffer size (byte)
	 * 
	 * @param sessionBufSize
	 *            value range is between <B>(256bytes+4bytes)</B> and
	 *            <B>(Integer.Max_Value[2G] + 4bytes)</B> . default value is
	 *            Short.Max_Value[32k]+4bytes
	 */
	public void setSessionBufSize(int sessionBufSize) {
		this.sessionBufSize = sessionBufSize;
	}

	public IoFilterChain getFilterChain() {
		return this.filterChain;
	}

	public void setFilterChain(IoFilterChain chain) {
		this.filterChain = chain;
	}

	/***
	 * [socket server]Accepter polling inner thread class
	 * 
	 * @author DJ
	 */
	private class Accepter implements Runnable {
		@Override
		public void run() {
			logger.debug("accepter thread startup... ...");
			while (!stopRun) {
				try {
					if (select(SELECTOR_MS) > 0) {
						processConnection(selectKeys());
					}
				} catch (ClosedSelectorException e) {
					logger.error("accepter :", e);
					break;
				} catch (Exception e) {
					logger.error("accepter :", e);
				}
			}
			logger.debug("accepter thread shutdown... ...");
		}
	}
}