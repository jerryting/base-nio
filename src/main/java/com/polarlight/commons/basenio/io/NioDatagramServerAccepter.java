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
import com.polarlight.commons.basenio.polling.AbstractPollingIoDatagramAccepter;

/**
 * Accepter abstract class . main class
 * 
 * @author DJ
 * @since 2.0
 */
public final class NioDatagramServerAccepter extends AbstractPollingIoDatagramAccepter<SocketStatelessSession> {

	private final static Logger logger = LoggerFactory.getLogger(NioDatagramServerAccepter.class);
	/* accept selector */
	private Selector selector;

	protected SelectionKey skey = null;

	public NioDatagramServerAccepter() throws Exception {
		this(Executors.newCachedThreadPool());
	}

	private NioDatagramServerAccepter(Executor executor) throws Exception {
		super(executor);
	}

	@Override
	protected void bindLocal(SocketAddress sa) throws IOException {
		DatagramChannel serv = DatagramChannel.open();
		// serv.bind(sa);//jdk1.7 later
		serv.socket().bind(sa); // before jdk1.6
		serv.socket().setBroadcast(true);
		serv.configureBlocking(false);
		serv.socket().setReuseAddress(true);
		skey = serv.register(selector, SelectionKey.OP_READ, null);
		this.servSessionMain = initSession(skey);
		logger.debug(sa.toString() + " is binding...");
	}

	@Override
	protected SocketStatelessSession initSession(SelectionKey sk) {
		DatagramChannel dch = (DatagramChannel) sk.channel();
		SocketStatelessSession session = new SocketStatelessSession(dch, sk, this.getHandler(), this.filterChain,
				this.getSessionBufSize(), SESSION_MODE.MODE_SERVER);
		return session;
	}

	@Override
	protected int select(int selectms) throws ClosedSelectorException, IOException {
		return selector.select(selectms);
	}

	@Override
	protected void init() throws IOException {
		selector = Selector.open();
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
	protected void write(SocketStatelessSession session) {
		servSessionMain.writeLocalDirect();
	}

	@Override
	public void shutdown() {
		if (skey == null || selector == null) {
			return;
		}
		super.shutdown();
		try {
			skey.channel().close();
			skey.cancel();
		} catch (IOException e) {
			logger.warn("server shutdown: " + e.toString(), e);
		}
		try {
			selector.wakeup();
			selector.close();
		} catch (Exception e) {
			logger.warn("server shutdown: " + e.toString(), e);
		}
	}
}