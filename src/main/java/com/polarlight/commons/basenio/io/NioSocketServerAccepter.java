package com.polarlight.commons.basenio.io;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.channels.ClosedSelectorException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.polarlight.commons.basenio.filterchain.DefaultUnboundaryIoFilter;
import com.polarlight.commons.basenio.io.session.state.SocketStateSession;
import com.polarlight.commons.basenio.polling.AbstractPollingIoAccepter;

/**
 * Accepter abstract class . main class
 * 
 * @author DJ
 *
 */
public final class NioSocketServerAccepter extends AbstractPollingIoAccepter<SocketStateSession> {

	private final static Logger logger = LoggerFactory.getLogger(NioSocketServerAccepter.class);
	/* accept selector */
	private Selector selector;

	private SelectionKey skey = null;

	public NioSocketServerAccepter() throws Exception {
		super(NioProcessor.class, null);
		this.getFilterChain().addFilter("DEFAULT", new DefaultUnboundaryIoFilter());
	}

	@Override
	protected void bindLocal(SocketAddress sa) throws IOException {
		ServerSocketChannel serv = ServerSocketChannel.open();
		// serv.bind(sa); //jdk1.7 later
		serv.socket().bind(sa);// before jdk1.6
		serv.configureBlocking(false);
		serv.socket().setReuseAddress(true);
		skey = serv.register(selector, SelectionKey.OP_ACCEPT, null);
		logger.debug(sa.toString() + " is listening...");
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
	protected SocketStateSession accept(SelectionKey sk) throws IOException {
		// SelectionKey sk = servCh.keyFor(selector); // good way!!
		if (sk == null || !sk.isValid() || !sk.isAcceptable()) {
			return null;
		}
		SocketChannel sch = ((ServerSocketChannel) sk.channel()).accept();
		if (sch == null) {
			return null;
		}

		SocketStateSession s = new SocketStateSession(sch, this.getHandler(), this.filterChain,
				this.getMaxSessionIdleMS(), this.getSessionBufSize());
		logger.debug(s + " enter into server channel");
		return s;
	}

	@Override
	public void shutdown() {
		if (skey == null || selector == null) {
			return;
		}
		super.shutdown();
		try {
			skey.channel().close();
			skey.channel();
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

	@Override
	protected Iterator<SelectionKey> selectKeys() {
		return selector.selectedKeys().iterator();
	}
}