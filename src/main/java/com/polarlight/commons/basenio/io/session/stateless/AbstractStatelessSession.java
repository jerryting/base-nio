package com.polarlight.commons.basenio.io.session.stateless;

import java.nio.channels.CancelledKeyException;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.polarlight.commons.basenio.filterchain.bound.IoBoundaryFilter;
import com.polarlight.commons.basenio.service.IoDatagramHandler;

/**
 * 
 * @author DJ
 * @since 2.0
 */
public abstract class AbstractStatelessSession implements IoStatelessSession {

	private DatagramChannel dch;

	private SelectionKey selectionKey;
	/* mode client */
	protected Queue<byte[]> writeAbleQueue;

	protected static Queue<DirectSendBean> directWriteAbleQueue = null;

	private IoBoundaryFilter filter;

	private IoDatagramHandler handler;

	private int bufSize = 32767;

	private String remoteIP;

	private int remotePort;

	private SESSION_MODE mode = SESSION_MODE.MODE_CLIENT;

	public enum SESSION_MODE {
		MODE_SERVER, MODE_CLIENT
	}

	/**
	 * write operating for processor[UDP client]
	 */
	public abstract void writeLocal();

	/**
	 * write-direct operating for processor[UDP server]
	 */
	public abstract void writeLocalDirect();

	/**
	 * @param dch
	 * @param skey
	 * @param handler
	 * @param filter
	 * @param bufSize
	 * @param mode
	 */
	public AbstractStatelessSession(final DatagramChannel dch, SelectionKey skey, final IoDatagramHandler handler,
			final IoBoundaryFilter filter, int bufSize, SESSION_MODE mode) {
		this.dch = dch;
		this.setHandler(handler);
		this.selectionKey = skey;
		bufSize = (bufSize <= 0 || bufSize >= bufSize) ? bufSize : bufSize;
		this.setFilter(filter);
		this.mode = mode;
		if (mode == SESSION_MODE.MODE_SERVER) {
			if (directWriteAbleQueue == null) {
				directWriteAbleQueue = new ConcurrentLinkedQueue<DirectSendBean>();
			}
		} else if (mode == SESSION_MODE.MODE_CLIENT) {
			writeAbleQueue = new ConcurrentLinkedQueue<byte[]>();
		}
	}

	@Override
	public String toString() {
		try {
			return getClass().getSimpleName() + "@" + this.remoteIP + ":" + this.remotePort;
		} catch (Exception e) {
			return getClass().getSimpleName();
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
			// System.out.println("bingo..");
		}
	}

	public IoBoundaryFilter getFilter() {
		return filter;
	}

	public void setFilter(IoBoundaryFilter filter) {
		this.filter = filter;
	}

	public IoDatagramHandler getHandler() {
		return handler;
	}

	public void setHandler(IoDatagramHandler handler) {
		this.handler = handler;
	}

	public DatagramChannel getDch() {
		return dch;
	}

	public void setDch(DatagramChannel dch) {
		this.dch = dch;
	}

	@Override
	public String getRemoteIP() {
		return remoteIP;
	}

	public void setRemoteIP(String remoteIP) {
		this.remoteIP = remoteIP;
	}

	@Override
	public int getRemotePort() {
		return remotePort;
	}

	public void setRemotePort(int remotePort) {
		this.remotePort = remotePort;
	}

	public int getBufSize() {
		return bufSize;
	}

	public void setBufSize(int bufSize) {
		this.bufSize = bufSize;
	}

	public SESSION_MODE getMode() {
		return mode;
	}
}