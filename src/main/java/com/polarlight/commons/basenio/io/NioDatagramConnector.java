package com.polarlight.commons.basenio.io;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.channels.ClosedSelectorException;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.polarlight.commons.basenio.io.session.stateless.AbstractStatelessSession.SESSION_MODE;
import com.polarlight.commons.basenio.io.session.stateless.SocketStatelessSession;
import com.polarlight.commons.basenio.polling.AbstractPollingIoDatagramProcessor;

/**
 * 
 * @author DJ
 * @since 2.0
 */
public class NioDatagramConnector extends AbstractPollingIoDatagramProcessor<SocketStatelessSession> {

	private final static Logger logger = LoggerFactory.getLogger(NioDatagramConnector.class);
	/* connector selector */
	private Selector selector;

	private SelectionKey skey = null;

	/**
	 * 
	 * @throws Exception
	 */
	public NioDatagramConnector() throws Exception {
		this(Executors.newCachedThreadPool());
	}

	/**
	 * 
	 * @param executor
	 * @throws Exception
	 */
	private NioDatagramConnector(Executor executor) throws Exception {
		super(executor);
	}

	@Override
	protected void connectLocal(SocketAddress sa) throws IOException {
		selector = Selector.open();
		DatagramChannel dch = DatagramChannel.open();
		dch.configureBlocking(false);
		skey = dch.register(selector, SelectionKey.OP_READ, null);
		dch.connect(sa);
		logger.debug(sa.toString() + " is connected...");
	}

	@Override
	protected SocketStatelessSession initSession() {
		SocketStatelessSession session = new SocketStatelessSession((DatagramChannel) skey.channel(), skey,
				this.getHandler(), this.filterChain, this.getSessionBufSize(), SESSION_MODE.MODE_CLIENT);
		return session;
	}

	@Override
	protected int select(int selectms) throws ClosedSelectorException, IOException {
		return selector.select(selectms);
	}

	@Override
	protected Iterator<SelectionKey> selectKeys() {
		return selector.selectedKeys().iterator();
	}

	@Override
	protected byte[] read(SocketStatelessSession session) throws IOException {
		return session.read();
	}

	@Override
	public void close() { // no good way for destroying mem

		if (skey == null || selector == null) {
			return;
		}
		super.close();
		try {
			DatagramChannel dch = (DatagramChannel) skey.channel();
			dch.disconnect();
		} catch (IOException e) {
			logger.error("client shutdown:" + e.toString(), e);
		}

		skey.cancel();

		try {
			this.selector.wakeup();
			this.selector.close();
		} catch (IOException e) {
			logger.error("client shutdown:" + e.toString(), e);
		}
	}

	@Override
	protected void write(SocketStatelessSession session) {
		session.writeLocal();
	}
}