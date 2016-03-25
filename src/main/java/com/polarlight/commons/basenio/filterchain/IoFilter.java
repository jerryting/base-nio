package com.polarlight.commons.basenio.filterchain;

import java.io.IOException;

import com.polarlight.commons.basenio.exception.NioBaseWriteException;
import com.polarlight.commons.basenio.io.session.state.AbstractStateSession;

/**
 * unbound message filter interface
 * 
 * @author DJ
 *
 */
public interface IoFilter {
	
	public final int BOUND_BYTES_NUMBER = 4;

	/**
	 * reader filter
	 * 
	 * @param session
	 * @param filteBytes
	 * @return
	 * @throws IOException
	 */
	public byte[] filterReceive(AbstractStateSession session, byte[] filteBytes) throws IOException;

	/**
	 * writer filter
	 * 
	 * @param session
	 * @param writeBytes
	 * @throws NioBaseWriteException
	 * @throws IOException
	 */
	public void filterWrite(AbstractStateSession session, byte[] writeBytes) throws NioBaseWriteException, IOException;

	/**
	 * 
	 */
	public void removeFilter();
}