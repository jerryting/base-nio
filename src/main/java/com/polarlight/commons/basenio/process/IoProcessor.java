package com.polarlight.commons.basenio.process;

import java.io.IOException;

import com.polarlight.commons.basenio.io.session.state.AbstractStateSession;

public interface IoProcessor<S extends AbstractStateSession> {
	/**
	 * add session to queue
	 * 
	 * @param session
	 */
	public void add(S session);
	/**
	 * detroy session. remove that from curqueue to unregisterqueue & ready for
	 * close-socketchannel operating
	 * 
	 * @param session
	 */
	public void destroy(S session);
	/**
	 * Is an invalid session
	 * 
	 * @param session
	 * @return
	 */
	public boolean isValid(S session);
	/**
	 * @throws IOException *
	 * 
	 */
	public void close() throws IOException;
}