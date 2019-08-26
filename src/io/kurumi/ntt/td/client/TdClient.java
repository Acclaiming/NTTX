package io.kurumi.ntt.td.client;

import io.kurumi.ntt.td.Client;
import io.kurumi.ntt.td.TdApi;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;
import io.kurumi.ntt.td.TdApi.Object;
import java.util.concurrent.ConcurrentHashMap;
import java.util.LinkedList;
import io.kurumi.ntt.td.Client.Event;
import cn.hutool.core.thread.ThreadUtil;

public class TdClient {

	private Client client = new Client();
	private AtomicLong requestId = new AtomicLong(1);
	private ReentrantLock executionLock = new ReentrantLock();
	private AtomicBoolean status;
	private ConcurrentHashMap<Long, TdCallback<?>> handlers = new ConcurrentHashMap<>();
	private LinkedList<ITdListener> listeners = new LinkedList<>();
	
	public TdClient(TdOptions options) {

		send(new TdApi.SetTdlibParameters(options.build()));

	}
	
	public void addListener(ITdListener listener) {
		
		listeners.add(listener);
		
		listener.onInit(this);
		
	}
	
	public void clearListeners() {
		
		listeners.clear();
		
	}
	
	public <T extends TdApi.Object> T execute(TdApi.Function function) throws TdException {

		if (this.executionLock.isLocked()) {

            throw new IllegalStateException("ClientActor is destroyed");

		}

        TdApi.Object response = this.client.execute(function);

		if (response instanceof TdApi.Error) {

			throw new TdException((TdApi.Error)response);

		}

		return (T)response;

	}

	public void execute(TdApi.Function function,TdCallback callback) {

		if (this.executionLock.isLocked()) {

            throw new IllegalStateException("ClientActor is destroyed");

		}

		long requestId = this.requestId.getAndIncrement();

		handlers.put(requestId,callback);

		send(requestId,function);

	}

	public long send(TdApi.Function function) {

        if (this.executionLock.isLocked()) {

            throw new IllegalStateException("Client is destroyed");

        }

		long requestId = this.requestId.getAndIncrement();

        client.send(requestId,function);

		return requestId;

    }

	public void send(long requestId,TdApi.Function function) {

        if (this.executionLock.isLocked()) {

            throw new IllegalStateException("Client is destroyed");

        }

        client.send(requestId,function);

    }

	public void start() {

		stop();

		final AtomicBoolean status = this.status;

		status.set(true);

		new Thread() {

			@Override
			public void run() {

				while (status.get()) {

					LinkedList<Client.Event> responseList = client.receive(233,1000);

					if (responseList.size() < 1) {

						continue;

					}

					for (Client.Event event : responseList) processEvent(event);

					executionLock.lock();

				}

			}

		}.start();

	}

	private void processEvent(Client.Event event) {

		if (event.requestId != 0L) {
			
			if (!handlers.containsKey(event.requestId)) return;
			
			TdCallback<?> callback = handlers.get(event.requestId);

			if (event.object instanceof TdApi.Error) {
				
				callback.onCallback(false,null,(TdApi.Error)event.object);
				
			} else {
				
				((TdCallback<TdApi.Object>)callback).onCallback(true,event.object,null);
				
			}
			
		} else {
			
			for (ITdListener listener : listeners) {
				
				listener.onEvent(event.object);
				
			}
			
		}

	}

	public void stop() {

		if (status != null && status.get()) { status.set(false); }

		status = new AtomicBoolean(false);

	}

	public void destroy() {

		if (status.get()) {

			stop();

			while (!executionLock.isLocked()) { ThreadUtil.safeSleep(233); }

		} else {

			executionLock.lock();

		}

        client.destroyClient();

	}

}
