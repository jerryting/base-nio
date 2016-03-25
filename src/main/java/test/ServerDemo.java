package test;

import java.net.InetSocketAddress;

import com.polarlight.commons.basenio.io.NioSocketServerAccepter;

public class ServerDemo {

	public static void main(String[] args) throws InterruptedException {

		try {
			/** initialize instance **/
			NioSocketServerAccepter nioserv = new NioSocketServerAccepter();
			/**set callback object*/
			nioserv.setHandler(new ServerHandler());
			/**set session buffer size (byte)*/
			nioserv.setSessionBufSize(1024);
			/**session idle timespan (ms)*/
			nioserv.setMaxSessionIdleMS(30000 * 1000);
			/*server startup*/
			nioserv.bind(new InetSocketAddress("0.0.0.0", 4567));
			Thread.sleep(1000000);
			nioserv.shutdown();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}