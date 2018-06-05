package main.java.io.vertx.example.web.sessions;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpMethod;

import java.io.PrintWriter;
import java.io.StringWriter;

public class RequestVerticle extends AbstractVerticle {

    @Override
    public void start() {

        Integer httpClientTimeout = 1000;

        HttpClientOptions httpClientOptions = new HttpClientOptions();

        EventBus eb = vertx.eventBus();
        eb.consumer("requestservice.postservice", message -> {

            HttpClient httpClient;
            httpClient = vertx.createHttpClient(httpClientOptions);

            long httpRequestStartMilliseconds = System.currentTimeMillis();

            String url = message.body().toString();

            HttpClientRequest httpClientRequest = httpClient.requestAbs(HttpMethod.GET, url)
                    .handler(resp -> {
                        resp.bodyHandler(bodyStr -> {
                            long httpRequestTotalMilliseconds = System.currentTimeMillis() - httpRequestStartMilliseconds;
                            System.out.println(url + " TOOK " + httpRequestTotalMilliseconds);
                            message.reply("OK");
                        });
                    });

            httpClientRequest.exceptionHandler(hcrEh -> {
                //logger.info("Hit Request exception");
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                hcrEh.printStackTrace(pw);
                message.reply("ERROR");
            });

            httpClientRequest.setTimeout(httpClientTimeout);
            httpClientRequest.end();
            httpClient.close();
        });

    }

}
