package com.polarlight.commons.basenio.io.session.state;

import java.nio.channels.CancelledKeyException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.polarlight.commons.basenio.filterchain.IoFilter;
import com.polarlight.commons.basenio.process.IoProcessor;
import com.polarlight.commons.basenio.service.IoHandler;

/**
 * 
 * @author DJ
 *
 */
public abstract class AbstractStateSession implements IoStateSession {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	private SocketChannel sch;

	private SelectionKey selectionKey;

	private long sessionid;

	private static AtomicLong idGenerator = new AtomicLong(0);

	private Object runnableProcessor;
	// buffer for reading from channel
	protected byte[] readBuf;
	// queue for writing to channel
	protected Queue<byte[]> writeAbleQueue = new ConcurrentLinkedQueue<byte[]>();

	private int bufPos = 0;

	private IoFilter filter;

	private IoHandler handler;

	private long lastActiveTimeStamp = System.currentTimeMillis();

	private long timeoutCount = 0;

	private long lastTimeoutTimeStamp = System.currentTimeMillis();

	private int DEFAULT_BUF_SIZE = Short.MAX_VALUE + 4;// Short.Max_Value +
														// 4bytes

	private int MIN_BUF_SIZE = 256 + 4;

	private int MAX_BUF_SIZE = Integer.MAX_VALUE + 4;

	private int DEFAULT_TIMEOUT_COUNT = 5;

	private int DEFAULT_TIMEOUT_SPAN_MS = 30 * 1000;

	private static long DEFAULT_MAX_IDLE_SPAN_MS = Long.MAX_VALUE;

	// write operating for processor
	public abstract void writeLocal();

	/**
	 * @param sch
	 * @param handler
	 * @param filter
	 * @param maxIdleMS
	 * @param bufSize
	 */
	public AbstractStateSession(final SocketChannel sch, final IoHandler handler, final IoFilter filter, long maxIdleMS,
			int bufSize) {
		this.sch = sch;
		this.setHandler(handler);
		this.sessionid = idGenerator.incrementAndGet();
		DEFAULT_BUF_SIZE = (bufSize <= MIN_BUF_SIZE - 4 || bufSize >= MAX_BUF_SIZE - 4) ? DEFAULT_BUF_SIZE
				: bufSize + 4;
		this.readBuf = new byte[DEFAULT_BUF_SIZE];
		this.filter = filter;
		DEFAULT_MAX_IDLE_SPAN_MS = maxIdleMS <= 0 ? DEFAULT_MAX_IDLE_SPAN_MS : maxIdleMS;
	}

	@Override
	public long getSessionid() {
		return sessionid;
	}

	@Override
	public IoHandler getHandler() {
		return handler;
	}

	@Override
	public long getLastActiveTimeStamp() {
		return lastActiveTimeStamp;
	}

	@Override
	public boolean isClosing() {

		if (!selectionKey.isValid()) {
			logger.debug("checksession[key invalid] session will be closed : " + this.toString());
			return true;
		}

		long curms = System.currentTimeMillis();

		if (DEFAULT_TIMEOUT_SPAN_MS > (curms - this.lastTimeoutTimeStamp)
				&& this.timeoutCount >= this.DEFAULT_TIMEOUT_COUNT) {
			logger.debug("checksession[exception timeout]session will be closed : " + this.toString());
			return true;
		} else if ((curms - this.lastActiveTimeStamp) >= DEFAULT_MAX_IDLE_SPAN_MS) {
			logger.debug("checksession[max idle] session will be closed : " + this.toString());
			return true;
		} else {
			return false;
		}
	}

	@Override
	public String toString() {
		try {
			return getClass().getSimpleName() + "@" + getSessionid() + ":"
					+ sch.socket().getInetAddress().getHostAddress() + ":" + sch.socket().getPort();
		} catch (Exception e) {
			return getClass().getSimpleName() + "@" + getSessionid();
		}
	}

	/**
	 * @param flag
	 * @throws IllegalArgumentException
	 * @throws CancelledKeyException
	 */
	protected synchronized void setWriteAble(boolean flag) throws IllegalArgumentException, CancelledKeyException {
		if (selectionKey == null || !selectionKey.isValid()) {
			return;
		}
		int oldset = selectionKey.interestOps();
		int newset = oldset;
		if (flag) {
			newset |= SelectionKey.OP_WRITE;
		} else {
			newset &= ~SelectionKey.OP_WRITE;
		}
		if (newset != oldset) {
			selectionKey.interestOps(newset);
		}
	}

	public void setSessionid(long sessionid) {
		this.sessionid = sessionid;
	}

	public Object getRunnableProcessor() {
		return this.runnableProcessor;
	}

	public void setRunnableProcessor(final IoProcessor<? extends AbstractStateSession> runnableProcessor) {
		this.runnableProcessor = runnableProcessor;
	}

	public SocketChannel getSch() {
		return sch;
	}

	public void setHandler(IoHandler handler) {
		this.handler = handler;
	}

	public SelectionKey getSelectionKey() {
		return selectionKey;
	}

	public void setSelectionKey(SelectionKey selectionKey) {
		this.selectionKey = selectionKey;
	}

	public int getBufPos() {
		return bufPos;
	}

	public void setBufPos(int bufPos) {
		this.bufPos = bufPos;
	}

	public byte[] getReadBuf() {
		return this.readBuf;
	}

	public int getBufSize() {
		return this.DEFAULT_BUF_SIZE;
	}

	public void resetBuf() {
		bufPos = 0;
		this.readBuf[0] = (byte) 0;
		this.readBuf[1] = (byte) 0;
	}

	public IoFilter getIoFilter() {
		return filter;
	}

	public synchronized void setLastActiveTimeStamp(long lastActiveTimeStamp) {
		this.lastActiveTimeStamp = lastActiveTimeStamp;
	}

	public long getTimeoutCount() {
		return timeoutCount;
	}

	public synchronized void setTimeoutFlag() {

		long curms = System.currentTimeMillis();
		if (DEFAULT_TIMEOUT_SPAN_MS < (curms - this.lastTimeoutTimeStamp)
				&& this.timeoutCount < this.DEFAULT_TIMEOUT_COUNT) {
			lastTimeoutTimeStamp = curms;
			timeoutCount = 0;
		} else if (DEFAULT_TIMEOUT_SPAN_MS > (curms - this.lastTimeoutTimeStamp)) {
			timeoutCount = timeoutCount >= DEFAULT_TIMEOUT_COUNT ? DEFAULT_TIMEOUT_COUNT : ++timeoutCount;
		}
	}

	@Override
	public String getIP() {
		return sch.socket().getInetAddress().getHostAddress();
	}
}