package com.polarlight.commons.basenio.filterchain.bound;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.polarlight.commons.basenio.io.session.stateless.AbstractStatelessSession;
import com.polarlight.commons.basenio.io.session.stateless.DirectSendBean;

/**
 * UDP message byte filter the default protocol filter. the
 * DefaultBoundaryIoFilter associated to Filter CoR when a seesion object is
 * created
 * 
 * @author DJ
 * @since 2.0
 */
public final class DefaultBoundaryIoFilter implements IoBoundaryFilter {

	private final static Logger logger = LoggerFactory.getLogger(DefaultBoundaryIoFilter.class);

	@Override
	public byte[] filterReceive(AbstractStatelessSession session, byte[] filterBytes) throws IOException {
		DatagramChannel dch = session.getDch();
		try {
			ByteBuffer buffer = ByteBuffer.allocate(session.getBufSize());
			buffer.clear();
			SocketAddress sa = dch.receive(buffer);
			InetSocketAddress inetAddress = (InetSocketAddress) sa;
			// session.setRemoteIP(inetAddress.getHostString()); // jdk1.7 later
			session.setRemoteIP(inetAddress.getAddress().getHostAddress()); // before
																			// jdk1.6
			session.setRemotePort(inetAddress.getPort());
			if (buffer.position() == 0) {
				return null;
			} else {
				buffer.flip();
				filterBytes = new byte[buffer.limit()];
				buffer.get(filterBytes);
				return filterBytes;
			}
		} catch (IOException e) {
			logger.warn("receive bytes: " + session.toString(), e);
			throw e;
		}
	}

	@Override
	public void filterWrite(AbstractStatelessSession session, byte[] writeBytes) throws IOException {

		DatagramChannel dch = session.getDch();
		try {
			ByteBuffer buffer = ByteBuffer.allocate(writeBytes.length);
			buffer.put(writeBytes);
			buffer.flip();
			// dch.send(buffer, dch.getRemoteAddress()); //jdk1.7 later
			while (buffer.hasRemaining()) {
				dch.send(buffer, dch.socket().getRemoteSocketAddress()); // before
																			// jdk1.6
			}
		} catch (IOException e) {
			logger.warn("send bytes: " + session.toString(), e);
			throw e;
		}
	}

	@Override
	public void filterWriteDirect(AbstractStatelessSession session, DirectSendBean dsb) throws IOException {
		DatagramChannel dch = session.getDch();
		try {
			byte[] writeBytes = dsb.getSendData();
			ByteBuffer buffer = ByteBuffer.allocate(writeBytes.length);
			buffer.put(writeBytes);
			buffer.flip();
			SocketAddress remoteAddr = new InetSocketAddress(dsb.getRemoteIp(), dsb.getRemotePort());
			dch.send(buffer, remoteAddr);
		} catch (IOException e) {
			logger.warn("send bytes: " + session.toString(), e);
			throw e;
		}
	}

	@Override
	public void removeFilter() {
	}
}