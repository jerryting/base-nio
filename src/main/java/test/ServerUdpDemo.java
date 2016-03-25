package test;

import java.net.InetSocketAddress;
import java.nio.channels.DatagramChannel;
import java.util.Arrays;

import com.polarlight.commons.basenio.io.NioDatagramServerAccepter;
import com.polarlight.commons.basenio.io.session.stateless.IoStatelessSession;
import com.polarlight.commons.basenio.service.IoDatagramHandler;

public class ServerUdpDemo {
	public static String remoteIp = "";
	public static int remotePort = 0;

	public static void main(String[] args) {
		try {
			DatagramChannel dmChannel = DatagramChannel.open();
			final byte[] b = { 33, 55, 78, 89, 0, 23 };
			final NioDatagramServerAccepter server = new NioDatagramServerAccepter();

			server.setHandler(new IoDatagramHandler() {

				@Override
				public void messageReceived(IoStatelessSession session, byte[] message) {
					remoteIp = session.getRemoteIP();
					remotePort = session.getRemotePort();
					System.out.println("server recv:" + session + "|" + Arrays.toString(message) + session.getRemoteIP()
							+ "|" + session.getRemotePort());
					// try {
					// session.writeDirect(b, null, 0);
					// } catch (Exception e) {
					// e.printStackTrace();
					// }
				}

			});

			final IoStatelessSession servSession = server.bind(new InetSocketAddress("0.0.0.0", 1573));
			new Thread(new Runnable() {
				@Override
				public void run() {
					int i = 0;
					while (true) {
						try {
//							if (i >= 10000) {
//								server.shutdown();
//								break;
//							}
							final byte[] b = { 45, 5, 78, 89, 2 };
							servSession.writeDirect(b, "192.168.10.255", 1573);
							i++;
							Thread.sleep(500);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
			}).start();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
