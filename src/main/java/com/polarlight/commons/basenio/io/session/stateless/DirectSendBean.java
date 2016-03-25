package com.polarlight.commons.basenio.io.session.stateless;

/**
 * data transfer object for UDP server <sendto>
 * @author DJ
 * @since 2.0
 */
public class DirectSendBean {
	/**
	 * bytes data that will be sent
	 */
	private byte[] sendData;
	/**
	 * remote ip
	 */
	private String remoteIp;
	/**
	 * remote port
	 */
	private int remotePort;

	public byte[] getSendData() {
		return sendData;
	}

	public void setSendData(byte[] sendData) {
		this.sendData = sendData;
	}

	public String getRemoteIp() {
		return remoteIp;
	}

	public void setRemoteIp(String remoteIp) {
		this.remoteIp = remoteIp;
	}

	public int getRemotePort() {
		return remotePort;
	}

	public void setRemotePort(int remotePort) {
		this.remotePort = remotePort;
	}

}
