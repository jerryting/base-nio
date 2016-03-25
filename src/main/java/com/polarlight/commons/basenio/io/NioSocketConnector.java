package com.polarlight.commons.basenio.io;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.channels.ClosedSelectorException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.polarlight.commons.basenio.exception.HandlerNoDefineException;
import com.polarlight.commons.basenio.filterchain.BaseFilterChain;
import com.polarlight.commons.basenio.filterchain.DefaultUnboundaryIoFilter;
import com.polarlight.commons.basenio.filterchain.IoFilterChain;
import com.polarlight.commons.basenio.io.session.state.SocketStateSession;
import com.polarlight.commons.basenio.polling.AbstractPollingIoProcessor;
import com.polarlight.commons.basenio.service.IoHandler;

public final class NioSocketConnector extends AbstractPollingIoProcessor<SocketStateSession> {

	private final Logger logger = LoggerFactory.getLogger(getClass().getSimpleName());

	private Selector selector = null;

	private long maxSessionIdleMS = 0;

	private int sessionBufSize = 0;

	private IoHandler handler = null;

	private IoFilterChain filterChain;

	private SelectionKey skey = null;

	public NioSocketConnector() throws IOException {
		this(Executors.newCachedThreadPool());
	}

	private NioSocketConnector(Executor executor) throws IOException {
		super(executor);
		this.selector = Selector.open();
		this.filterChain = new BaseFilterChain();
		this.filterChain.addFilter("DEFAULT", new DefaultUnboundaryIoFilter());
	}

	/**
	 * client main
	 * 
	 * @param sa
	 * @throws HandlerNoDefineException
	 * @throws IOException
	 */
	public void connect(SocketAddress sa) throws HandlerNoDefineException, IOException {
		if (handler == null) {
			throw new HandlerNoDefineException("Handler No Defined");
		}
		try {
			SocketStateSession session = initLocal(sa);
			if (init(session)) {
				add(session);
			}

		} catch (IOException e) {
			logger.warn("connet: ", e);
			throw e;
		}
	}

	private SocketStateSession initLocal(SocketAddress sa) throws IOException {
		SocketChannel sch = SocketChannel.open(sa);
		SocketStateSession s = new SocketStateSession(sch, this.getHandler(), this.filterChain,
				this.getMaxSessionIdleMS(), this.getSessionBufSize());
		return s;
	}

	@Override
	public void add(SocketStateSession session) {
		session.setRunnableProcessor(this);
		super.add(session);
	}

	@Override
	protected boolean init(SocketStateSession session) throws IOException {
		SocketChannel sch = session.getSch();
		if (sch.finishConnect()) {
			sch.configureBlocking(false);
			sch.socket().setReuseAddress(true);
			skey = sch.register(selector, SelectionKey.OP_READ);
			session.setSelectionKey(skey);
			skey.attach(session);
			return true;
		}
		return false;
	}

	@Override
	protected int select(int selectms) throws ClosedSelectorException, IOException {
		return selector.select(selectms);
	}

	@Override
	protected void wakeup() {
		selector.wakeup();
	}

	@Override
	protected Iterator<SelectionKey> selectKeys() {
		return selector.selectedKeys().iterator();
	}

	/***
	 * read control in this PROCESSOR
	 * 
	 * @throws IOException
	 */

	@Override
	protected byte[] read(SocketStateSession session) throws IOException {
		// read byte[] by XX socket protocol style
		byte[] receivedBytes = null;
		receivedBytes = session.read();
		return receivedBytes;
	}

	@Override
	protected void write(SocketStateSession session) {
		session.writeLocal();
	}

	@Override
	public void close() { // not good way for destroying mem
		this.stopRun = true;
		if (skey == null || selector == null) {
			return;
		}
		try {
			skey.channel().close();
			skey.cancel();
		} catch (IOException e) {
			logger.error("client shutdown:" + e.toString(), e);
		}
		try {
			this.selector.wakeup();
			this.selector.close();
		} catch (IOException e) {
			logger.error("client shutdown:" + e.toString(), e);
		}
		ExecutorService es = (ExecutorService) executor;
		try {
			es.shutdownNow();
		} catch (Exception e) {
		}

	}

	public long getMaxSessionIdleMS() {
		return maxSessionIdleMS;
	}

	/**
	 * set socket state session 's max idle time span(millisecond)
	 * 
	 * @param maxSessionIdleMS
	 *            if value<=0 default value will be used (Long.MAXVALUE)
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

	public IoHandler getHandler() {
		return handler;
	}

	public void setHandler(IoHandler handler) {
		this.handler = handler;
	}
}