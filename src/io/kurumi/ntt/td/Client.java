package io.kurumi.ntt.td;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;
import java.util.HashMap;
import java.util.LinkedList;

public final class Client {

    private final long clientId;
    private final ReentrantLock receiveLock = new ReentrantLock();
    private final ReentrantLock executionLock = new ReentrantLock();
    private static native long createNativeClient();
    private static native void nativeClientSend(long nativeClientId,long eventId,TdApi.Function function);
    private static native int nativeClientReceive(long nativeClientId,long[] eventIds,TdApi.Object[] events,double timeout);
    private static native TdApi.Object nativeClientExecute(TdApi.Function function);
    private static native void destroyNativeClient(long nativeClientId);

	static {
		
		System.load("libs/td/libtdjni.so");
		
		Log.setVerbosityLevel(2);
		
	}
	
    public Client() {

        this.clientId = createNativeClient();

    }

    public void send(long requestId,TdApi.Function function) {

        if (this.executionLock.isLocked()) {

            throw new IllegalStateException("Client is destroyed");

        }

        nativeClientSend(this.clientId,requestId,function);

    }

	public static class Event {

		public long requestId;
	 	public TdApi.Object object;

		public Event(long requestId,TdApi.Object object) {

			this.requestId = requestId;
			this.object = object;

		}

	}

    public LinkedList<Event> receive(double timeout,int eventSize) {

        if (this.executionLock.isLocked()) {

            throw new IllegalStateException("Client is destroyed");
        }

		LinkedList<Event> responseList = new LinkedList<>();

		long[] eventIds = new long[eventSize];

		TdApi.Object[] events = new TdApi.Object[eventSize];

        if (this.receiveLock.isLocked()) {

            throw new IllegalThreadStateException("Thread: " + Thread.currentThread().getName() + " trying receive incoming updates but shouldn't be called simultaneously from two different threads!");

		}

        this.receiveLock.lock();

		int resultSize = nativeClientReceive(this.clientId,eventIds,events,timeout);

        this.receiveLock.unlock();

        for (int i = 0; i < resultSize; i++) {

            responseList.add(new Event(eventIds[i],events[i]));

        }

        return responseList;

    }

    public TdApi.Object execute(TdApi.Function function) {

        if (this.executionLock.isLocked()) {

            throw new IllegalStateException("Client is destroyed");

        }

        return nativeClientExecute(function);

    }

    public void destroyClient() {

        this.executionLock.lock();

        destroyNativeClient(this.clientId);

    }

}
