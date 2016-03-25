package test;

import java.io.IOException;
import java.util.Arrays;

import com.polarlight.commons.basenio.exception.NioBaseWriteException;
import com.polarlight.commons.basenio.io.session.state.IoStateSession;
import com.polarlight.commons.basenio.service.IoHandler;

public class ServerHandler implements IoHandler {
	@Override
	public void socketCreated(IoStateSession session) {
		System.out.println(session.toString() + " [server] enter into channel");
	}

	@Override
	public void socketClosed(IoStateSession session) {
		System.out.println(session.toString() + "[server] closed");
	}

	@Override
	public void messageReceived(IoStateSession session, byte[] message) {
		// TODO Auto-generated method stub
		System.out.println("[server] rev: " + session + "  " + Arrays.toString(message));
//		byte[] b = { 0, 3, 1, 76, 84, 48, 48, 49, 0, -120, -17};
//		try {
//			session.write(b);
//		} catch (NioBaseWriteException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
	}
}