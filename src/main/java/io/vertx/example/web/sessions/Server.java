package main.java.io.vertx.example.web.sessions;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.Session;
import io.vertx.ext.web.handler.CookieHandler;
import io.vertx.ext.web.handler.SessionHandler;
import io.vertx.ext.web.sstore.LocalSessionStore;
import io.vertx.ext.web.sstore.impl.LocalSessionStoreImpl;

import java.util.function.Consumer;
/*
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public class Server extends AbstractVerticle {

  // Convenience method so you can run it in your IDE
  public static void main(String[] args) {

      // System.setProperty("vertx.cwd", ".");
      Consumer<Vertx> runner = vertx -> {
          try {
              vertx.deployVerticle(Server.class.getName());
          } catch (Throwable t) {
              t.printStackTrace();
          }
      };

      VertxOptions options = new VertxOptions();
      Vertx vertx = Vertx.vertx(options);
      runner.accept(vertx);
  }

  @Override
  public void start() throws Exception {

    Router router = Router.router(vertx);

    router.route().handler(CookieHandler.create());
    router.route().handler(SessionHandler.create(LocalSessionStore.create(vertx)));
    router.route().handler(routingContext -> {

      Session session = routingContext.session();

      Integer cnt = session.get("hitcount");
      cnt = (cnt == null ? 0 : cnt) + 1;

      session.put("hitcount", cnt);

      HttpClientOptions httpClientOptions = new HttpClientOptions();
      httpClientOptions.setMaxPoolSize(100);
      HttpClient httpClient = vertx.createHttpClient(httpClientOptions);

      routingContext.response().putHeader("content-type", "text/html")
                               .end("<html><body><h1>Hitcount: " + cnt + "</h1></body></html>");
    });

    vertx.createHttpServer().requestHandler(router::accept).listen(8080);
  }
}
