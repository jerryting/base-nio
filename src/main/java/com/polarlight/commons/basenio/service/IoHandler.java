package com.polarlight.commons.basenio.service;

import com.polarlight.commons.basenio.io.session.state.IoStateSession;

public interface IoHandler {
	/**
	 * the callback interface when a new seeson create a connection
	 * 
	 * @param session
	 */
	public void socketCreated(final IoStateSession session);

	/**
	 * the callback interface when the session has been closed
	 * 
	 * @param session
	 */
	public void socketClosed(final IoStateSession session);

	/**
	 * the callback interface when the session received byte[] data.
	 * 
	 * @param session
	 * @param message
	 */
	public void messageReceived(final IoStateSession session, final byte[] message);
}