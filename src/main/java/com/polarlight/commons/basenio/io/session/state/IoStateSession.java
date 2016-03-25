package com.polarlight.commons.basenio.io.session.state;

import java.io.IOException;

import com.polarlight.commons.basenio.exception.NioBaseWriteException;
import com.polarlight.commons.basenio.service.IoHandler;

public interface IoStateSession {
	/**
	 * return inner identifier(long) of this session
	 * 
	 * @return
	 */
	public long getSessionid();

	/**
	 * find callback main handler
	 * 
	 * @return
	 */
	public IoHandler getHandler();

	/**
	 * write bytes<no protocol length> to special session
	 * 
	 * @param message
	 * @throws IOException
	 */
	public void write(final byte[] message) throws NioBaseWriteException, IOException;

	/**
	 * return the last active timestamp in this session
	 * 
	 * @return long
	 */
	public long getLastActiveTimeStamp();

	/**
	 * return the status<closing-session> True-the session will be closed or
	 * that is in unregister queue.
	 * 
	 * @return
	 */
	public boolean isClosing();

	/**
	 * to close this session
	 */
	public void close();

	/**
	 * unique String identifier of current session eg.
	 * ScoketSession@1:192.168.1.1:8080
	 * 
	 * @return
	 */
	public String toString();

	/**
	 * return session IP
	 * 
	 * @return
	 */
	public String getIP();
}