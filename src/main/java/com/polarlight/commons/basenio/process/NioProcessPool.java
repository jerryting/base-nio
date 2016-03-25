package com.polarlight.commons.basenio.process;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.polarlight.commons.basenio.exception.ProcessorPoolException;
import com.polarlight.commons.basenio.io.session.state.AbstractStateSession;

public class NioProcessPool<S extends AbstractStateSession> implements IoProcessor<S> {
	/*
	 * */
	private IoProcessor<S> pool[];

	private int poolSize = 1;

	private final Executor executor;

	private final static Logger logger = LoggerFactory.getLogger(NioProcessPool.class);

	@SuppressWarnings("unchecked")
	public NioProcessPool(Class<? extends IoProcessor<S>> classType, int poolSize) throws ProcessorPoolException {

		this.poolSize = poolSize;

		pool = new IoProcessor[poolSize];

		executor = Executors.newCachedThreadPool();

		Constructor<? extends IoProcessor<S>> constructor = null;

		for (int i = 0; i < poolSize; i++) {
			try {
				constructor = classType.getConstructor(Executor.class);
				pool[i] = constructor.newInstance(this.executor);
			} catch (Exception e) {
				logger.warn("processor pool initial exception: ", e);
				throw new ProcessorPoolException("processor pool initial exception");
			}
		}
	}

	@Override
	public void add(S session) {
		IoProcessor<S> processor = getProcessor(session);
		session.setRunnableProcessor(processor);
		processor.add(session);
	}

	@Override
	public void destroy(S session) {
	}

	/***
	 * Dispatch processor from pool a special simple algorithm used in
	 * ApacheMINA. return pool[0] directly.temparorily. A single processor is
	 * used in Version V1
	 * 
	 * @param session
	 * @return IoProcessor<S>
	 */
	private IoProcessor<S> getProcessor(S session) {
		return pool[(int) (session.getSessionid() % poolSize)];
	}

	@Override
	public boolean isValid(S session) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void close() throws IOException {
		for (IoProcessor<S> processor : pool) {
			processor.close();
		}
		ExecutorService es = (ExecutorService) executor;
		try {
			es.shutdownNow();
		} catch (Exception e) {
		}
	}
}