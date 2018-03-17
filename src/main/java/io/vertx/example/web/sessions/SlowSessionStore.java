package main.java.io.vertx.example.web.sessions;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.core.shareddata.LocalMap;
import io.vertx.ext.web.Session;
import io.vertx.ext.web.sstore.impl.LocalSessionStoreImpl;
import io.vertx.ext.web.sstore.impl.SessionImpl;

public class SlowSessionStore extends LocalSessionStoreImpl {

    private final LocalMap<String, Session> localMap;

    public SlowSessionStore(Vertx vertx, String sessionMapName, long reaperInterval) {
        super(vertx, sessionMapName, reaperInterval);
        localMap = vertx.sharedData().getLocalMap(sessionMapName);
    }

    @Override
    public void get(String id, Handler<AsyncResult<Session>> resultHandler) {
        Session session = localMap.get(id);
        if(session == null){
            LoggerFactory.getLogger(SlowSessionStore.class).error("No session in map for: " + id);
        }
        resultHandler.handle(Future.succeededFuture(session));
    }

    @Override
    public void put(Session session, Handler<AsyncResult<Void>> resultHandler) {

        LoggerFactory.getLogger(SlowSessionStore.class).info("Persisting slowly...");

        this.vertx.setTimer(10L* 1000L, timerRes -> {
            final SessionImpl oldSession = (SessionImpl) localMap.get(session.id());
            final SessionImpl newSession = (SessionImpl) session;

            if (oldSession != null) {
                // there was already some stored data in this case we need to validate versions
                if (oldSession.version() != newSession.version()) {
                    resultHandler.handle(Future.failedFuture("Version mismatch"));
                    return;
                }
            }

            newSession.incrementVersion();
            localMap.put(session.id(), session);
            resultHandler.handle(Future.succeededFuture());

            LoggerFactory.getLogger(SlowSessionStore.class).info("Done persisting...");
        });

    }
}
