package io.vertx.microservices;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class C extends AbstractVerticle {
  private static org.slf4j.Logger logger = LoggerFactory.getLogger(C.class);

  @Override
  public void start() throws Exception {
    SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy-hh:mm:ss");
    String hostname = "Unknown";
    try
    {
      InetAddress addr;
      addr = InetAddress.getLocalHost();
      hostname = addr.getHostName() + " ("+ addr.getHostAddress() + ") ";
    }
    catch (UnknownHostException ex)
    {
      hostname = "localhost";
    }
    String finalHostname = hostname;

    Router router = Router.router(vertx);

    router.get("/").handler(context -> {
      Date start = Calendar.getInstance().getTime();
      String param = context.request().getParam("name");
      vertx.eventBus().publish("monitoring",
              new JsonObject()
                      .put("message", "Message received")
                      .put("from", finalHostname + "| " + Thread.currentThread().getName()));
      Date end = Calendar.getInstance().getTime();
      context.response()
          .putHeader("content-type", "application/json")
          .end(new JsonObject()
                  .put("C", "Olá " + param)
                  .put("from", finalHostname + "| " + Thread.currentThread().getName())
                  .put("duration", end.getTime() - start.getTime() +"ms")
                  .encodePrettily());
    });

    vertx.setPeriodic(10000, handler -> {
      System.out.println("[C] handler");
      vertx.eventBus().publish("monitoring",
              new JsonObject()
                      .put("message", "Hello from Java")
                      .put("from", "C"));
    });

    vertx.createHttpServer()
        .requestHandler(router::accept)
        .listen(config().getInteger("port"));
  }
}
