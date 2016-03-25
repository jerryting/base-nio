package com.polarlight.commons.basenio.filterchain;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.polarlight.commons.basenio.exception.NioBaseWriteException;
import com.polarlight.commons.basenio.io.session.state.AbstractStateSession;
import com.polarlight.commons.basenio.utils.ByteConverter;
import com.polarlight.commons.basenio.utils.ByteConverter.ByteOrder;

/**
 * TCP stream byte filter the default protocol filter. the defaultiofilter
 * associated to Filter CoR when a seesion object is created
 * 
 * @author DJ
 */
public final class DefaultUnboundaryIoFilter implements IoFilter {

	private final static Logger logger = LoggerFactory.getLogger(DefaultUnboundaryIoFilter.class);

	@Override
	public byte[] filterReceive(AbstractStateSession session, byte[] filteBytes) throws IOException {

		SocketChannel sch = session.getSch();
		byte[] sBuf = session.getReadBuf();
		int bufSize = session.getBufSize();

		boolean exceptionProtocol = false;
		boolean isRead = false;
		long activeTimeStamp = System.currentTimeMillis();

		try {
			for (int i = 0; i < 2; i++) {

				int bufPos = session.getBufPos();
				int nextLen = this.nextReadByteLength(sBuf, bufPos);

				if (nextLen == 0) {
					break;
				} else if (nextLen < 0 || nextLen > bufSize - BOUND_BYTES_NUMBER) {
					session.resetBuf();
					exceptionProtocol = true;
					break;
				}

				ByteBuffer bbDst = ByteBuffer.allocate(nextLen);

				int readLen = sch.read(bbDst);

				if (readLen > 0) {
					isRead = true;
					activeTimeStamp = System.currentTimeMillis();
				} else if (readLen == 0) {
					break;
				} else {
					throw new IOException();
				}

				bbDst.flip();
				byte[] byteDst = new byte[readLen];
				bbDst.get(byteDst);
				System.arraycopy(byteDst, 0, sBuf, bufPos, readLen);
				bufPos += readLen;
				session.setBufPos(bufPos);

			}
		} catch (IOException e) {
			logger.warn("receive bytes: " + session.toString(), e);
			throw e;
		} finally {
			if (isRead) {
				session.setLastActiveTimeStamp(activeTimeStamp);
			}
		}

		byte[] fullProtocol = null;

		if (exceptionProtocol) {
			return fullProtocol;
		}

		int curProtocolLen = ByteConverter.decodeInt(sBuf, 0, ByteOrder.Little_Endian);
		if (curProtocolLen > 0 && (curProtocolLen == session.getBufPos() - BOUND_BYTES_NUMBER)) {
			fullProtocol = new byte[curProtocolLen];
			System.arraycopy(sBuf, BOUND_BYTES_NUMBER, fullProtocol, 0, curProtocolLen);
			session.resetBuf();
		} else if (curProtocolLen > 0 && (curProtocolLen < session.getBufPos() - BOUND_BYTES_NUMBER)) {
			session.resetBuf();
		} else if (curProtocolLen <= 0) {
			session.resetBuf();
		} else {
			// continue to read when the next select
		}
		return fullProtocol;
	}

	@Override
	public void filterWrite(AbstractStateSession session, byte[] writeBytes) throws NioBaseWriteException, IOException {

		if (writeBytes == null) {
			throw new NioBaseWriteException("NULL not permitted in writeBytes");
		}

		if (writeBytes.length > session.getBufSize() - BOUND_BYTES_NUMBER) {
			throw new NioBaseWriteException("writeBytes length exceed session (buffer - BOUND_BYTES_NUMBER)");
		}

		int len = writeBytes.length;
		byte[] lengthBytes = new byte[BOUND_BYTES_NUMBER];
		ByteConverter.encodeInt(lengthBytes, 0, ByteOrder.Little_Endian, len);

		byte[] protocol = new byte[len + BOUND_BYTES_NUMBER];

		// LENGTH bytes
		protocol[0] = lengthBytes[0];
		protocol[1] = lengthBytes[1];
		protocol[2] = lengthBytes[2];
		protocol[3] = lengthBytes[3];

		System.arraycopy(writeBytes, 0, protocol, BOUND_BYTES_NUMBER, len);

		ByteBuffer b = ByteBuffer.allocate(len + BOUND_BYTES_NUMBER);
		b.put(protocol);
		b.flip();
		int wLen = 0;

		long activeTimeStamp = System.currentTimeMillis();

		try {
			while (b.hasRemaining()) {
				wLen += session.getSch().write(b);
			}
			activeTimeStamp = System.currentTimeMillis();
		} catch (IOException e) {
			session.setTimeoutFlag();
			logger.warn("write bytes: " + session.toString(), e);
			throw e;
		}

		if (wLen > 0) {
			session.setLastActiveTimeStamp(activeTimeStamp);
		}
	}

	/**
	 * @param protocolBuf
	 * @param pos
	 * @return
	 */
	private int nextReadByteLength(byte[] protocolBuf, int pos) {
		if (pos == 0) {
			return 4;
		} else if (pos == 1) {
			return 3;
		} else if (pos == 2) {
			return 2;
		} else if (pos == 3) {
			return 1;
		} else if (pos >= BOUND_BYTES_NUMBER) {
			int protocolLen = ByteConverter.decodeInt(protocolBuf, 0, ByteOrder.Little_Endian);
			return protocolLen - (pos - BOUND_BYTES_NUMBER);
		} else {
			return 0;
		}
	}

	@Override
	public void removeFilter() {
	}
}