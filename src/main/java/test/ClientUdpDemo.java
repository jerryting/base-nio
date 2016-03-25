package test;

import java.net.InetSocketAddress;
import java.util.Arrays;

import com.polarlight.commons.basenio.io.NioDatagramConnector;
import com.polarlight.commons.basenio.io.session.stateless.IoStatelessSession;
import com.polarlight.commons.basenio.service.IoDatagramHandler;

public class ClientUdpDemo {

	public static void main(String[] args) throws Exception {
		NioDatagramConnector connector = new NioDatagramConnector();
		connector.setHandler(new IoDatagramHandler() {
			@Override
			public void messageReceived(IoStatelessSession session, byte[] message) {
				System.out.println("client recv:" + session + "|" + Arrays.toString(message));
			}
		});
		IoStatelessSession session = connector.connect(new InetSocketAddress("255.255.255.255", 1573));
		byte[] xx = { 34, 65, 78, 90 };
		int i = 0;
		while (true) {
			if (i >= 10) {
				connector.close();
				break;
			}
			session.write(xx);
			Thread.sleep(1000);
			i++;
		}
	}
}