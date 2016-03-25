package com.polarlight.commons.basenio.io.session.stateless;

import java.io.IOException;

import com.polarlight.commons.basenio.exception.NioBaseWriteException;
import com.polarlight.commons.basenio.exception.UdpSessionModeException;

/**
 * interface for stateless session
 * 
 * @author DJ
 * @since 2.0
 */
public interface IoStatelessSession {
	/**
	 * write bytes to special session[client_mode]
	 * 
	 * @param message
	 * @throws IOException
	 * @throws UdpSessionModeException
	 */
	public void write(final byte[] message) throws NioBaseWriteException, IOException, UdpSessionModeException;

	/**
	 * write bytes to special session[server_mode]
	 * 
	 * @param message
	 * @throws NioBaseWriteException
	 * @throws IOException
	 * @throws UdpSessionModeException
	 */
	public void writeDirect(final byte[] message, final String remoteIp, final int remotePort)
			throws NioBaseWriteException, IOException, UdpSessionModeException;

	/**
	 * unique String identifier of current session eg.
	 * 
	 * @return
	 */
	public String toString();

	/**
	 * 
	 * @return
	 */
	public String getRemoteIP();

	/**
	 * 
	 * @return
	 */
	public int getRemotePort();

}