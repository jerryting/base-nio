package com.polarlight.commons.basenio.service;

import com.polarlight.commons.basenio.io.session.stateless.IoStatelessSession;

/**
 * Datagram channel handler callback
 * 
 * @author DJ
 * @since 2.0
 */
public interface IoDatagramHandler {
	/**
	 * @param session
	 * @param message
	 */
	public void messageReceived(final IoStatelessSession session, final byte[] message);
}