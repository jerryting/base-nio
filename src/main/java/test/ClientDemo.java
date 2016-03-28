package test;

import java.net.InetSocketAddress;

import com.polarlight.commons.basenio.io.NioSocketConnector;
import com.polarlight.commons.basenio.service.IoHandler;

public class ClientDemo {

	/**
           this is a test
	 @param args
	 */
	public static void main(String[] args) {
		try {
			byte[] b = {76, 84, 48, 48, 49, 0,
					-120, -17, 46, -66, 127, 0, 0, 80, 20, -62, -76, -1, 127, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 16, 0,
					66, 70, 55, 50, 54, 68,70, 55, 50, 54, 68,70, 55};
			/** initialize instance **/
			System.out.println(b.length);
			 NioSocketConnector connector = new NioSocketConnector();
			 /**
			 *set session buffer size(byte)
			 */
			 connector.setSessionBufSize(1025);
			 connector.setMaxSessionIdleMS(30000 * 1000);
			 /** set callback object*/
			 IoHandler handler = new ClientHandler();
			 connector.setHandler(handler);
			 /** connect to tcp server*/
			 connector.connect(new InetSocketAddress("127.0.0.1", 4567));
			 // connector.connect(new InetSocketAddress("192.168.12.9",1521));
			 // Thread.sleep(5*1000);
			 // connector.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
