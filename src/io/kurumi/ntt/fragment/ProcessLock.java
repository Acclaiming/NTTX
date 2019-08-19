package io.kurumi.ntt.fragment;

import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.TimeUnit;

public class ProcessLock<T> extends ReentrantLock {

    public AtomicBoolean used = new AtomicBoolean(false);

    public T obj;

    public Condition condition = newCondition();

    public T waitFor() {

		if (obj != null || used.get()) return obj;

        try {

            lock();

			if (obj != null) return obj;

            condition.await(1000,TimeUnit.MILLISECONDS);

			used.set(true);

            return obj;

        } catch (InterruptedException e) {

            return null;

        } finally {

            unlock();

        }

    }

    public void send(T obj) {

		if (used.getAndSet(true)) return;

        try {

            lock();

            this.obj = obj;

            condition.signalAll();

        } finally {

            unlock();

        }

    }

}

