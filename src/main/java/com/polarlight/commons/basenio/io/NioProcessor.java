package com.polarlight.commons.basenio.io;

import java.io.IOException;
import java.nio.channels.ClosedSelectorException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.concurrent.Executor;

import com.polarlight.commons.basenio.io.session.state.SocketStateSession;
import com.polarlight.commons.basenio.polling.AbstractPollingIoProcessor;

public final class NioProcessor extends AbstractPollingIoProcessor<SocketStateSession> {

	private Selector selector;

	public NioProcessor(Executor executor) throws IOException {
		super(executor);
		selector = Selector.open();
	}

	@Override
	protected boolean init(SocketStateSession session) throws IOException {
		SocketChannel sch = session.getSch();
		if (sch != null) { // Defense
			if (!sch.isConnected()) {
				return false;
			}
			sch.socket().setReuseAddress(true);
			sch.configureBlocking(false);
			SelectionKey sk = sch.register(selector, SelectionKey.OP_READ);
			session.setSelectionKey(sk);
			sk.attach(session);
		}
		return true;
	}

	@Override
	public int select(int selectms) throws ClosedSelectorException,IOException {
		return selector.select(selectms);
	}

	@Override
	public void wakeup() {
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
	public void close() throws IOException { // no good way for destroying mem
		this.stopRun = true;
		this.selector.wakeup();
		this.selector.close();
	}
}