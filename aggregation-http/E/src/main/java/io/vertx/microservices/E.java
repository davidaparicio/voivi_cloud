package io.vertx.microservices;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class E extends AbstractVerticle {
  private static org.slf4j.Logger logger = LoggerFactory.getLogger(E.class);
  private SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy-hh:mm:ss");
  private String hostname = "Unknown";

  @Override
  public void start() throws Exception {
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
    Router router = Router.router(vertx);
    router.get("/").handler(this::getREST);
    vertx.eventBus().consumer("e", message -> {
      Date start = Calendar.getInstance().getTime();
      System.out.println("I have received a message: " + message.body());
      JsonObject jsonMessage = (JsonObject) message.body();
      String sentence = jsonMessage.getString("sentence");
      Date end = Calendar.getInstance().getTime();
      message.reply(new JsonObject()
              .put("E", "Boker Tov " + sentence)
              .put("from", hostname + "| " + Thread.currentThread().getName())
              .put("duration", end.getTime() - start.getTime() +"ms")
      );
    });

    vertx.setPeriodic(10000, handler -> {
      System.out.println("[E] handler");
      vertx.eventBus().publish("monitoring",
              new JsonObject()
                      .put("message", "Hello from Java")
                      .put("from", "E"));
    });

    vertx.createHttpServer()
        .requestHandler(router::accept)
        .listen(config().getInteger("port"));
  }

  private void getREST(RoutingContext routingContext) {
    Date start = Calendar.getInstance().getTime();
    String param = routingContext.request().getParam("name");
    vertx.eventBus().publish("monitoring",
            new JsonObject()
                    .put("message", "Message received")
                    .put("from", hostname + "| " + Thread.currentThread().getName()));
    Date end = Calendar.getInstance().getTime();
    routingContext.response()
            .putHeader("content-type", "application/json")
            .end(new JsonObject()
                    .put("E", "Boker Tov " + param)
                    .put("from", hostname + "| " + Thread.currentThread().getName())
                    .put("duration", end.getTime() - start.getTime() +"ms")
                    .encodePrettily());
  }
}
