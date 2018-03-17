# vertx-slow-sessions

This demonstrates an issue where Vert.x doesn't wait for it's session handler to close before completing the HTTP request.

## Run with
```bash
$ mvn package
$ java -jar target/web-examples-3.5.1.jar
```

Now curl with cookies enabled:
```bash
$ curl -v -c cookies localhost:8080
$ curl -v -c cookies localhost:8080
```

You'll notice the request completes but the hitcount doesn't increment because the session hasn't been flushed yet.
