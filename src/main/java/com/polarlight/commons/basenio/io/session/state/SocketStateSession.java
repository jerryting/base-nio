package com.polarlight.commons.basenio.io.session.state;

import java.io.IOException;
import java.nio.channels.SocketChannel;

import com.polarlight.commons.basenio.exception.NioBaseWriteException;
import com.polarlight.commons.basenio.filterchain.IoFilter;
import com.polarlight.commons.basenio.process.IoProcessor;
import com.polarlight.commons.basenio.service.IoHandler;

public final class SocketStateSession extends AbstractStateSession {

	public SocketStateSession(SocketChannel sch, IoHandler handler, IoFilter filter, long maxIdleMS, int bufSize) {
		super(sch, handler, filter, maxIdleMS, bufSize);
	}

	@SuppressWarnings("unchecked")
	@Override
	public synchronized void write(final byte[] message) throws NioBaseWriteException, IOException {
		
		if (isClosing()) {
			throw new NioBaseWriteException("invalid session or session timeout. being destroyed ");
		}
		if (message == null || message.length == 0) {
			throw new NioBaseWriteException("not available data to write");
		}
		if (message.length > getBufSize() - IoFilter.BOUND_BYTES_NUMBER) {
			throw new NioBaseWriteException(
					"writeBytes length exceed session bufsize (buffersize - BOUND_BYTES_NUMBER)");
		}

		IoProcessor<SocketStateSession> runProcessor = (IoProcessor<SocketStateSession>) this.getRunnableProcessor();
		if (!runProcessor.isValid(this)) {
			throw new NioBaseWriteException("invalid session or session timeout. being destroyed ");
		}
		if (writeAbleQueue.add(message)) {
			this.setWriteAble(!writeAbleQueue.isEmpty());
		}
	}

	@Override
	public void writeLocal() {
		boolean isEmpty = writeAbleQueue.isEmpty();
		if (isEmpty) {
			this.setWriteAble(!isEmpty);
			return;
		}
		IoFilter filter = this.getIoFilter(); // add length
		byte[] message = writeAbleQueue.peek();
		if (message != null) {
			try {
				filter.filterWrite(this, message);
			} catch (NioBaseWriteException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			writeAbleQueue.remove();
		}
	}

	/**
	 * 
	 * @return
	 * @throws IOException
	 */
	public synchronized byte[] read() throws IOException {
		byte[] receivedBytes = null;
		receivedBytes = this.getIoFilter().filterReceive(this, receivedBytes);
		return receivedBytes;
	}

	@Override
	@SuppressWarnings("unchecked")
	public synchronized void close() {
		IoProcessor<SocketStateSession> runProcessor = (IoProcessor<SocketStateSession>) this.getRunnableProcessor();
		readBuf = null;
		this.getIoFilter().removeFilter();
		runProcessor.destroy(this);
	}
}