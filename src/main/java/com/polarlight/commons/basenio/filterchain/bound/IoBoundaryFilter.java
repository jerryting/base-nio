package com.polarlight.commons.basenio.filterchain.bound;

import java.io.IOException;

import com.polarlight.commons.basenio.exception.NioBaseWriteException;
import com.polarlight.commons.basenio.io.session.stateless.AbstractStatelessSession;
import com.polarlight.commons.basenio.io.session.stateless.DirectSendBean;

/**
 * bound message filter interface
 * 
 * @since 2.0
 * @author DJ
 */
public interface IoBoundaryFilter {
	/**
	 * reader filter
	 * 
	 * @param session
	 * @param filteBytes
	 * @return
	 * @throws IOException
	 */
	public byte[] filterReceive(AbstractStatelessSession session, byte[] filteBytes) throws IOException;

	/**
	 * writer filter[client_mode]
	 * 
	 * @param session
	 * @param writeBytes
	 * @throws NioBaseWriteException
	 * @throws IOException
	 */
	public void filterWrite(AbstractStatelessSession session, byte[] writeBytes) throws IOException;

	/**
	 * writer filter[server_mode]
	 * 
	 * @param session
	 * @param dsb
	 * @throws NioBaseWriteException
	 * @throws IOException
	 */
	public void filterWriteDirect(AbstractStatelessSession session, DirectSendBean dsb) throws IOException;

	/**
	 * 
	 */
	public void removeFilter();
}