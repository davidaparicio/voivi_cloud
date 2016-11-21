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

public class Issuer extends AbstractVerticle {
  private static org.slf4j.Logger logger = LoggerFactory.getLogger(Issuer.class);
  private SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy-hh:mm:ss");
  private String hostname = "Unknown";


  @Override
  public void start() throws Exception {
    try { InetAddress addr; addr = InetAddress.getLocalHost();
      hostname = addr.getHostName() + " ("+ addr.getHostAddress() + ") ";
    } catch (UnknownHostException ex) {
      hostname = "localhost";
    }
    Router router = Router.router(vertx);
    router.get("/").handler(this::getREST);
    vertx.eventBus().consumer("issuer", message -> {
      JsonObject jsonMessage = (JsonObject) message.body();
      String sentence = jsonMessage.getString("sentence");
      message.reply(doJob(sentence));
    });

    vertx.setPeriodic(10000, handler -> { System.out.println("[Issuer] handler");
      vertx.eventBus().publish("monitoring", new JsonObject().put("message", "Hello from Java").put("from", "Issuer"));
    });
    vertx.createHttpServer()
            .requestHandler(router::accept)
            .listen(config().getInteger("port"));
  }

  private JsonObject doJob(String sentence) {
    System.out.println("I have received a message: " + sentence);
    Date start = Calendar.getInstance().getTime();
    Date end = Calendar.getInstance().getTime();
    return new JsonObject()
            .put("Issuer", "Aloha " + sentence)
            .put("from", hostname + "| " + Thread.currentThread().getName())
            .put("duration", end.getTime() - start.getTime() + "ms");
  }

  private void getREST(RoutingContext routingContext) {
    String paramREST = routingContext.request().getParam("name");
    routingContext.response()
            .putHeader("content-type", "application/json")
            .end(doJob(paramREST).encodePrettily());
  }
}
