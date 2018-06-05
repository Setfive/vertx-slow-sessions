package main.java.io.vertx.example.web.sessions;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
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

    vertx.deployVerticle(RequestVerticle.class.getName(), new DeploymentOptions(), r -> { });

    router.route().handler(CookieHandler.create());
    router.route().handler(SessionHandler.create(LocalSessionStore.create(vertx)));

    router.get("/test-fast").handler(routingContext -> {

        vertx.eventBus().send("requestservice.postservice", "http://app.s5srv.com/slow.php?q=notslow&i=REQUESTED", postReply -> {
            System.out.println("On fast got: " + postReply.result().body());
            routingContext
                    .response()
                    .putHeader("content-type", "text/html")
                    .end("<html><body><h1>" + postReply.result().body() + "</h1></body></html>");
        });

    });

    router.get("/test-slow").handler(routingContext -> {
        for(int i = 0; i < 10; i++) {

            String url = "http://app.s5srv.com/slow.php?q=notslow&i=" + i;
            if (i == 1 || i == 3 || i == 8) {
                url = "http://app.s5srv.com/slow.php?i=" + i;
            }

            vertx.eventBus().send("requestservice.postservice", url, postReply -> {
                System.out.println(postReply.result().body());
            });
        }

        routingContext
                .response()
                .putHeader("content-type", "text/html")
                .end("<html><body><h1>Queued requests</h1></body></html>");
    });

    vertx.createHttpServer().requestHandler(router::accept).listen(8080);
  }
}
