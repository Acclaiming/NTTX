package io.kurumi.ntt.td.client;

import cn.hutool.core.thread.ThreadUtil;
import io.kurumi.ntt.td.Client;
import io.kurumi.ntt.td.TdApi.AuthorizationState;
import io.kurumi.ntt.td.TdApi.AuthorizationStateReady;
import io.kurumi.ntt.td.TdApi.AuthorizationStateWaitEncryptionKey;
import io.kurumi.ntt.td.TdApi.AuthorizationStateWaitTdlibParameters;
import io.kurumi.ntt.td.TdApi.CheckDatabaseEncryptionKey;
import io.kurumi.ntt.td.TdApi.Error;
import io.kurumi.ntt.td.TdApi.Function;
import io.kurumi.ntt.td.TdApi.Object;
import io.kurumi.ntt.td.TdApi.SetTdlibParameters;
import io.kurumi.ntt.td.TdApi.UpdateAuthorizationState;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

public class TdClient extends TdListener {

	private Client client = new Client();
	private ExecutorService executors = Executors.newFixedThreadPool(10);
	private AtomicLong requestId = new AtomicLong(1);
	private ReentrantLock executionLock = new ReentrantLock();
	private AtomicBoolean status;
	private ConcurrentHashMap<Long, TdCallback<?>> handlers = new ConcurrentHashMap<>();
	private LinkedList<ITdListener> listeners = new LinkedList<>();
	private AtomicBoolean hasAuth = new AtomicBoolean(false);
	private SetTdlibParameters params;

	public TdClient(TdOptions options) {

		addListener(this);

		params = new SetTdlibParameters(options.build());

		send(params);

	}

	public boolean hasAuth() {

		return this.hasAuth.get();

	}

	void setAuth(boolean hasAuth) {

		this.hasAuth.set(hasAuth);

	}

	public void addListener(ITdListener listener) {

		listeners.add(listener);

		listener.onInit(this);

	}

	public void clearListeners() {

		listeners.clear();

	}

	@Override
	public void onUpdateAuthorizationState(UpdateAuthorizationState state) {

		AuthorizationState authState = state.authorizationState;

		if (authState instanceof AuthorizationStateWaitTdlibParameters) {

			send(params);

		} else if (authState instanceof AuthorizationStateWaitEncryptionKey) {

			send(new CheckDatabaseEncryptionKey());

		} else if (authState instanceof AuthorizationStateReady) {

			hasAuth.set(true);

			onInit(this);

		}

	}

	public <T extends Object> T execute(Function function) throws TdException {

		if (this.executionLock.isLocked()) {

            throw new IllegalStateException("ClientActor is destroyed");

		}
		
		while (!hasAuth()) {
			
			ThreadUtil.safeSleep(233);
			
		}

        Object response = this.client.execute(function);

		if (response instanceof Error) {

			throw new TdException((Error)response);

		}

		return (T)response;

	}

	public void execute(Function function,TdCallback<?> callback) {

		if (this.executionLock.isLocked()) {

            throw new IllegalStateException("ClientActor is destroyed");

		}

		long requestId = this.requestId.getAndIncrement();

		handlers.put(requestId,callback);

		send(requestId,function);

	}

	public long send(Function function) {

        if (this.executionLock.isLocked()) {

            throw new IllegalStateException("Client is destroyed");

        }

		long requestId = this.requestId.getAndIncrement();

        client.send(requestId,function);

		return requestId;

    }

	public void send(long requestId,Function function) {

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

				}
				
				executionLock.lock();

			}

		}.start();

	}

	private void processEvent(Client.Event event) {

		if (event.requestId != 0L) {

			if (!handlers.containsKey(event.requestId)) return;

			TdCallback<?> callback = handlers.get(event.requestId);

			if (event.object instanceof Error) {

				callback.onCallback(false,null,(Error)event.object);

			} else {

				((TdCallback<Object>)callback).onCallback(true,event.object,null);

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
