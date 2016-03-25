package test;

import java.io.IOException;
import java.util.Arrays;

import com.polarlight.commons.basenio.exception.NioBaseWriteException;
import com.polarlight.commons.basenio.io.session.state.IoStateSession;
import com.polarlight.commons.basenio.service.IoHandler;

public class ClientHandler implements IoHandler {

	@Override
	public void socketCreated(IoStateSession session) {
		System.out.println(session.toString() + " client session created");
		byte[] b = {0, 0, 1, 76, 84, 48, 48, 49, 0, -120, -17, 46, -66, 127, 0, 0, 80, 20, -62, -76, -1, 127, 0, 0,
				0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 16, 0, 66, 70, 55, 50, 54, 68, 53, 55, 70, 68, 53, 68, 67, 53, 51, 70,
				51, 54, 54, 65, 55, 65, 70, 49, 56, 53, 53, 66, 52, 55, 55, 55, 0, 0, 12, 41, 25, 80, -10, 0, 0, 1,
				76, 84, 48, 48, 49, 0, -120, -17, 46, -66, 127, 0, 0, 80, 20, -62, -76, -1, 127, 0, 0, 0, 0, 0, 0,
				0, 0, 0, 0, 0, 0, 16, 0, 66, 70, 55, 50, 54, 68, 53, 55, 70, 68, 53, 68, 67, 53, 51, 70, 51, 54, 54,
				65, 55, 65, 70, 49, 56, 53, 53, 66, 52, 55, 55, 55, 0, 0, 12, 41, 25, 80, -10, 0, 0, 1, 76, 84, 48,
				48, 49, 0, -120, -17, 46, -66, 127, 0, 0, 80, 20, -62, -76, -1, 127, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
				0, 0, 16, 0, 66, 70, 55, 50, 54, 68, 53, 55, 70, 68, 53, 68, 67, 53, 51, 70, 51, 54, 54, 65, 55, 65,
				70, 49, 56, 53, 53, 66, 52, 55, 55, 55, 0, 0, 12, 41, 25, 80, -10, 0, 0, 1, 76, 84, 48, 48, 49, 0,
				-120, -17, 46, -66, 127, 0, 0, 80, 20, -62, -76, -1, 127, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 16, 0,
				66, 70, 55, 50, 54, 68, 53, 55, 70, 68, 53, 68, 67, 53, 51, 70, 51, 54, 54, 65, 55, 65, 70, 49, 56,
				53, 53, 66, 52, 55, 55, 55, 0, 0, 12, 41, 25, 80, -10, 0, 0, 1, 76, 84, 48, 48, 49, 0, -120, -17,
				46, -66, 127, 0, 0, 80, 20, -62, -76, -1, 127, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 16, 0, 66, 70,
				55, 50, 54, 68, 53, 55, 70, 68, 53, 68, 67, 53, 51, 70, 51, 54, 54, 65, 55, 65, 70, 49, 56, 53, 53,
				66, 52, 55, 55, 55, 0, 0, 12, 41, 25, 80, -10, 0, 0, 1, 76, 84, 48, 48, 49, 0, -120, -17, 46, -66,
				127, 0, 0, 80, 20, -62, -76, -1, 127, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 16, 0, 66, 70, 55, 50, 54,
				68, 53, 55, 70, 68, 53, 68, 67, 53, 51, 70, 51, 54, 54, 65, 55, 65, 70, 49, 56, 53, 53, 66, 52, 55,
				55, 55, 0, 0, 12, 41, 25, 80, -10, 0, 0, 1, 76, 84, 48, 48, 49, 0, -120, -17, 46, -66, 127, 0, 0,
				80, 20, -62, -76, -1, 127, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 16, 0, 66, 70, 55, 50, 54, 68, 53,
				55, 70, 68, 53, 68, 67, 53, 51, 70, 51, 54, 54, 65, 55, 65, 70, 49, 56, 53, 53, 66, 52, 55, 55, 55,
				0, 0, 12, 41, 25, 80, -10, 0, 0, 1, 76, 84, 48, 48, 49, 0, -120, -17, 46, -66, 127, 0, 0, 80, 20,
				-62, -76, -1, 127, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 16, 0, 66, 70, 55, 50, 54, 68, 53, 55, 70,
				68, 53, 68, 67, 53, 51, 70, 51, 54, 54, 65, 55, 65, 70, 49, 56, 53, 53, 66, 52, 55, 55, 55, 0, 0,
				12, 41, 25, 80, -10, 0, 0, 1, 76, 84, 48, 48, 49, 0, -120, -17, 46, -66, 127, 0, 0, 80, 20, -62,
				-76, -1, 127, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 16, 0, 66, 70, 55, 50, 54, 68, 53, 55, 70, 68, 53,
				68, 67, 53, 51, 70, 51, 54, 54, 65, 55, 65, 70, 49, 56, 53, 53, 66, 52, 55, 55, 55, 0, 0, 12, 41,
				25, 80, -10, 0, 0, 1, 76, 84, 48, 48, 49, 0, -120, -17, 46, -66, 127, 0, 0, 80, 20, -62, -76, -1,
				127, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 16, 0, 66, 70, 55, 50, 54, 68, 53, 55, 70, 68, 53, 68, 67,
				53, 51, 70, 51, 54, 54, 65, 55, 65, 70, 49, 56, 53, 53, 66, 52, 55, 55, 55, 0, 0, 12, 41, 25, 80,
				-10, 0, 0, 1, 76, 84, 48, 48, 49, 0, -120, -17, 46, -66, 127, 0, 0, 80, 20, -62, -76, -1, 127, 0, 0,
				0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 16, 0, 66, 70, 55, 50, 54, 68, 53, 55, 70, 68, 53, 68, 67, 53, 51, 70,
				51, 54, 54, 65, 55, 65, 70, 49, 56, 53, 53, 66, 52, 55, 55, 55, 0, 0, 12, 41, 25, 80, -10, 0, 0, 1,
				76, 84, 48, 48, 49, 0, -120, -17, 46, -66, 127, 0, 0, 80, 20, -62, -76, -1, 127, 0, 0, 0, 0, 0, 0,
				0, 0, 0, 0, 0, 0, 16, 0, 66, 70, 55, 50, 54, 68, 53, 55, 70, 68, 53, 68, 67, 53, 51, 70, 51, 54, 54,
				65, 55, 65, 70, 49, 56, 53, 53, 66, 52, 55, 55, 55, 0, 0, 12, 41, 25, 80, -10, 0, 0, 1, 76, 84, 48,
				48, 49, 0, -120, -17, 46, -66, 127, 0, 0, 80, 20, -62, -76, -1, 127, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
				0, 0, 16, 0, 66, 70, 55, 50, 54, 68, 53, 55, 70, 68, 53, 68, 67, 53, 51, 70, 51, 54, 54, 65, 55, 65,
				70, 49, 56, 53, 53, 66, 52, 55, 55, 55, 0, 0, 12, 41, 25, 80, -10, 0, 0, 1, 76, 84, 48, 48, 49, 0,
				-120, -17, 46, -66, 127, 0, 0, 80, 20, -62, -76, -1, 127, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 16, 0,
				66, 70, 55, 50, 54, 68,70, 55, 50, 54, 68,70, 55};
		try {
			session.write(b);
		} catch (NioBaseWriteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void socketClosed(IoStateSession session) {
		System.out.println(session.toString() + " client closed");
	}

	@Override
	public void messageReceived(IoStateSession session, byte[] message) {
		// TODO Auto-generated method stub
		System.out.println("client rev: " + session + "  " + Arrays.toString(message));
		byte[] b = {1, 76, 84, 48, 48, 49, 0, -120, -17, 46, -66, 127, 55, 50, 54, 68, 53, 55, 70, 68, 53, 68,
				67, 53, 51, 70, 51, 54, 54, 65, 55, 65, 70, 49, 56, 53, 53, 66, 52, 55, 55, 55, 0, 0, 12, 41, 25, 80,
				-10 };
		try {
			session.write(b);
		} catch (NioBaseWriteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
