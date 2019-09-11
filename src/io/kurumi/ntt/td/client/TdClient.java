package io.kurumi.ntt.td.client;

import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import io.kurumi.ntt.td.Client;
import io.kurumi.ntt.td.TdApi;
import io.kurumi.ntt.td.TdApi.*;
import io.kurumi.ntt.td.model.TMsg;
import io.kurumi.ntt.utils.BotLog;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import java.util.concurrent.locks.ReentrantLock;

public class TdClient extends TdListener {

    private Client client = new Client();

    private ExecutorService updatePool = Executors.newSingleThreadExecutor();
    private ExecutorService asyncPool = Executors.newFixedThreadPool(16);

    private AtomicLong requestId = new AtomicLong(1);
    private ReentrantLock executionLock = new ReentrantLock();
    private AtomicBoolean status;
    private ConcurrentHashMap<Long, TdCallback<?>> callbacks = new ConcurrentHashMap<>();

    private LinkedList<TdHandler> handlers = new LinkedList<>();
    private LinkedList<TdListener> listeners = new LinkedList<>();

    private AtomicBoolean hasAuth = new AtomicBoolean(false);
    private SetTdlibParameters params;
    private Log log = LogFactory.get(TdClient.class);

    public User me;

    public TdClient(TdOptions options) {

        addListener(this);

        params = new SetTdlibParameters(options.build());

    }

    public TdPoint point = new TdPoint();

    public HashMap<String, TdListener> functions = new HashMap<>();
    public HashMap<String, TdListener> adminFunctions = new HashMap<>();
    public HashMap<String, TdListener> payloads = new HashMap<>();
    public HashMap<String, TdListener> adminPayloads = new HashMap<>();
    public HashMap<String, TdListener> points = new HashMap<>();
    public HashMap<String, TdListener> callbackQuerys = new HashMap<>();

    public void addListener(TdHandler handler) {

        handlers.add(handler);

        if (handler instanceof TdListener) {

            TdListener listener = (TdListener) handler;

            listener.client = this;

            listeners.add(listener);

            listener.init();


        }

    }

    public void clearListeners() {

        handlers.clear();
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

            try {

                me = execute(new GetMe());

            } catch (TdException e) {
            }

        }

    }

    @Override
    public void onNewMessage(UpdateNewMessage update) {

        Message message = update.message;

        if (message.senderUserId == me.id) return;

        final User user = message.isChannelPost ? null : (User) E(new GetUser(message.senderUserId));

        final TMsg msg = new TMsg(this, update.message);

        TdPointData data = null;

        if (msg.isPrivate()) {

            data = getPrivatePoint(user.id);

        } else if (msg.isBasicGroup() || msg.isSuperGroup()) {

            data = getGroupPoint(msg.chatId, user.id);

        }

        if (data != null) {

            if (!points.containsKey(data.point)) {

                log.warn("无效的指针 : {}", data.point);

                return;

            }

            points.get(data.point).onPoint(user, msg, data.point, data);

            return;

        }

        if (msg.isCommand()) {

            final String command;
            final String[] params;

            if (msg.isStartPayload()) {

                command = msg.payload()[0];
                params = ArrayUtil.remove(msg.payload(), 0);

                if (isAdmin(msg.sender) && adminPayloads.containsKey(command)) {

                    adminPayloads.get(command).onPayload(user, msg, command, params);

                } else if (payloads.containsKey(command)) {

                    payloads.get(command).onPayload(user, msg, command, params);

                } else {

                    onPayload(user, msg, command, params);

                }

                return;

            }

            command = msg.command();
            params = msg.fixedParams();

            final TdListener function;

            if (isAdmin(msg.sender) && adminFunctions.containsKey(command)) {

                function = adminFunctions.get(command);

            } else if (functions.containsKey(command)) {

                function = functions.get(command);

            } else {

                function = this;

            }

            if (function.asyncFunction()) {

                asyncPool.execute(new Runnable() {

                    @Override
                    public void run() {

                        try {

                            function.onFunction(user, msg, command, params);

                        } catch (Exception e) {

                            log.error("TdError - Async\n\n{}", BotLog.parseError(e));

                        }

                    }

                });

            } else {

                function.onFunction(user, msg, command, params);

            }

        }

    }


    public <T extends TdApi.Object> T E(Function function) {

        try {

            return execute(function);

        } catch (TdException e) {

            return null;

        }

    }

    @Override
    public <T extends TdApi.Object> T execute(Function function) throws TdException {

        if (this.executionLock.isLocked()) {

            throw new IllegalStateException("ClientActor is destroyed");

        }

        while (!hasAuth.get()) {

            ThreadUtil.safeSleep(10);

        }

        final AtomicReference<TdApi.Object> responseAtomicReference = new AtomicReference<>();
        final AtomicBoolean executedAtomicBoolean = new AtomicBoolean(false);

        execute(function, new TdCallback<T>() {

            @Override
            public void onCallback(boolean isOk, T result, TdApi.Error error) {

                if (isOk) {

                    responseAtomicReference.set(result);

                } else {

                    responseAtomicReference.set(error);

                }

                executedAtomicBoolean.set(true);

            }

        });

        while (!executedAtomicBoolean.get()) {
        }

        TdApi.Object response = responseAtomicReference.get();

        if (response instanceof TdApi.Error) {

            throw new TdException((TdApi.Error) response);

        }

        return (T) response;

    }

    @Override
    public void execute(Function function, TdCallback<?> callback) {

        if (this.executionLock.isLocked()) {

            throw new IllegalStateException("ClientActor is destroyed");

        }

        long requestId = this.requestId.getAndIncrement();

        callbacks.put(requestId, callback);

        send(requestId, function);

    }

    @Override
    public long send(Function function) {

        if (this.executionLock.isLocked()) {

            throw new IllegalStateException("Client is destroyed");

        }

        long requestId = this.requestId.getAndIncrement();

        client.send(requestId, function);

        return requestId;

    }

    public void send(long requestId, Function function) {

        if (this.executionLock.isLocked()) {

            throw new IllegalStateException("Client is destroyed");

        }

        client.send(requestId, function);

    }

    public void start() {

        stop();

        this.status = new AtomicBoolean(true);

        final AtomicBoolean status = this.status;

        new Thread() {

            @Override
            public void run() {

                while (status.get()) {

                    LinkedList<Client.Event> responseList = client.receive(20, 1000);

                    for (Client.Event event : responseList) processEvent(event);

                }

            }

        }.start();

    }

    private void processEvent(final Client.Event event) {

        // StaticLog.debug("Event : {}",event.object.getClass().getSimpleName());

        if (event.requestId != 0L) {

            if (!callbacks.containsKey(event.requestId)) return;

            try {

                TdCallback<?> callback = callbacks.get(event.requestId);

                if (event.object instanceof TdApi.Error) {

                    callback.onCallback(false, null, (TdApi.Error) event.object);

                } else {

                    ((TdCallback<TdApi.Object>) callback).onCallback(true, event.object, null);

                }

            } catch (Exception e) {

                log.error("TdError - Sync\n\n{}", BotLog.parseError(e));

            }

        } else if (event.object instanceof Update) {

            updatePool.execute(new Runnable() {

                @Override
                public void run() {

                    for (TdHandler handler : handlers) {

                        try {

                            handler.onEvent(event.object);

                        } catch (Exception e) {

                            log.error("TdError - Sync\n\n{}", BotLog.parseError(e));

                        }

                    }

                }

            });

        }

    }

    public void stop() {

        if (status != null && status.get()) {
            status.set(false);
        }

    }

    public void destroy() {

        if (status.get()) {

            stop();

            while (status.get()) {
                ThreadUtil.safeSleep(233);
            }

        } else {

            executionLock.lock();

        }

        client.destroyClient();

    }

}
