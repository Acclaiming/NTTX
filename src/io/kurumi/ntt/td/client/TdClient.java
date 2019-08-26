package io.kurumi.ntt.td.client;

import cn.hutool.core.thread.ThreadUtil;
import io.kurumi.ntt.td.Client;
import io.kurumi.ntt.td.TdApi;
import io.kurumi.ntt.td.TdApi.*;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
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

	public User me;

	public TdClient(TdOptions options) {

		listeners.add(this);

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
	public void onAuthorizationState(UpdateAuthorizationState state) {

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

	@Override
	public void onInit(TdClient client) {

		try {

			me = execute(new GetMe());
			
		} catch (TdException e) {}

	}

	public <T extends TdApi.Object> T execute(Function function) throws TdException {

		if (this.executionLock.isLocked()) {

            throw new IllegalStateException("ClientActor is destroyed");

		}

		while (!hasAuth()) {

			ThreadUtil.safeSleep(233);

		}

		final AtomicReference<TdApi.Object> responseAtomicReference = new AtomicReference<>();
        final AtomicBoolean executedAtomicBoolean = new AtomicBoolean(false);

		execute(function,new TdCallback<T>() {

				@Override
				public void onCallback(boolean isOk,T result,TdApi.Error error) {

					if (isOk) {

						responseAtomicReference.set(result);

					} else {

						responseAtomicReference.set(error);

					}

					executedAtomicBoolean.set(true);

				}

			});

		while (!executedAtomicBoolean.get()) {}

        TdApi.Object response = responseAtomicReference.get();

		if (response instanceof TdApi.Error) {

			throw new TdException((TdApi.Error)response);

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

		this.status = new AtomicBoolean(false);

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

	}

	public void destroy() {

		if (status.get()) {

			stop();

			while (status.get()) { ThreadUtil.safeSleep(233); }

		} else {

			executionLock.lock();

		}

        client.destroyClient();

	}

}
