package com.polarlight.commons.basenio.io.session.stateless;

import java.io.IOException;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;

import com.polarlight.commons.basenio.exception.NioBaseWriteException;
import com.polarlight.commons.basenio.exception.UdpSessionModeException;
import com.polarlight.commons.basenio.filterchain.bound.IoBoundaryFilter;
import com.polarlight.commons.basenio.service.IoDatagramHandler;

/**
 * 
 * @author DJ
 * @since 2.0
 */
public final class SocketStatelessSession extends AbstractStatelessSession {

	public SocketStatelessSession(DatagramChannel dch, SelectionKey selectionKey, IoDatagramHandler handler,
			IoBoundaryFilter filter, int bufSize, SESSION_MODE mode) {
		super(dch, selectionKey, handler, filter, bufSize, mode);
	}

	@Override
	public synchronized void write(final byte[] message)
			throws NioBaseWriteException, IOException, UdpSessionModeException {
		if (this.getMode() != SESSION_MODE.MODE_CLIENT) {
			throw new UdpSessionModeException(
					"wrong session mode method. ur mode:" + this.getMode() + ",please use <writeDirect>");
		}
		if (message == null || message.length == 0) {
			throw new NioBaseWriteException("not available data to write");
		}
		if (writeAbleQueue.add(message)) {
			this.setWriteAble(!writeAbleQueue.isEmpty());
		}
	}

	@Override
	public synchronized void writeDirect(final byte[] message, final String remoteIp, final int remotePort)
			throws NioBaseWriteException, IOException, UdpSessionModeException {
		if (this.getMode() != SESSION_MODE.MODE_SERVER) {
			throw new UdpSessionModeException(
					"wrong session mode method. ur mode:" + this.getMode() + ",please use <write>");
		}
		if (message == null || message.length == 0) {
			throw new NioBaseWriteException("not available data to write");
		}
		DirectSendBean o = new DirectSendBean();
		o.setSendData(message);
		if (remoteIp == null || remoteIp.equals("") || remotePort <= 0) {
			o.setRemoteIp(this.getRemoteIP());
			o.setRemotePort(this.getRemotePort());
		} else {
			o.setRemoteIp(remoteIp);
			o.setRemotePort(remotePort);
		}
		if (directWriteAbleQueue.add(o)) {
			this.setWriteAble(!directWriteAbleQueue.isEmpty());
		}
	}

	@Override
	public void writeLocal() {
		byte[] message = writeAbleQueue.peek();
		if (message != null) {
			try {
				this.getFilter().filterWrite(this, message);
			} catch (IOException e) {
				e.printStackTrace();
			}
			writeAbleQueue.remove();
		}
		boolean isEmpty = writeAbleQueue.isEmpty();
		if (isEmpty) {
			this.setWriteAble(!isEmpty);
			return;
		}
	}

	@Override
	public void writeLocalDirect() {
		boolean isEmpty = directWriteAbleQueue.isEmpty();
		// System.out.println(isEmpty);
		if (isEmpty) {
			this.setWriteAble(!isEmpty);
			return;
		}
		DirectSendBean dsb = directWriteAbleQueue.peek();
		if (dsb != null) {
			try {
				this.getFilter().filterWriteDirect(this, dsb);
			} catch (IOException e) {
				e.printStackTrace();
			}
			directWriteAbleQueue.remove();
		}
	}

	/**
	 * 
	 * @return
	 * @throws IOException
	 */
	public synchronized byte[] read() throws IOException {
		byte[] receivedBytes = null;
		receivedBytes = this.getFilter().filterReceive(this, receivedBytes);
		return receivedBytes;
	}
}