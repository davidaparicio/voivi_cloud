package io.vertx.microservices;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.http.HttpClient;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.ext.web.handler.sockjs.BridgeOptions;
import io.vertx.ext.web.handler.sockjs.PermittedOptions;
import io.vertx.ext.web.handler.sockjs.SockJSHandler;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;

public class Receiver extends AbstractVerticle {
  private static org.slf4j.Logger logger = LoggerFactory.getLogger(Receiver.class);
  private SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy-hh:mm:ss");
  private String hostname = "Unknown";

  @Override
  public void start() throws Exception {
    try { InetAddress addr; addr = InetAddress.getLocalHost();
      hostname = addr.getHostName() + " (" + addr.getHostAddress() + ") ";
    } catch (UnknownHostException ex) {
      hostname = "localhost";
    }

    Router router = Router.router(vertx);

    router.route("/assets/*").handler(StaticHandler.create("assets"));
    router.get("/").handler(this::hello);
    setupSockJsBridge(router);

    vertx.createHttpServer()
            .requestHandler(router::accept)
            .listen(config().getInteger("port"), handler -> {
              if (handler.succeeded()) {
                System.out.println("[Receiver] - http://localhost:8080/");
              } else {
                System.out.println("[Receiver] - Failed to listen on port 8080");
              }
            });

    vertx.setPeriodic(10000, handler -> { System.out.println("[Receiver] handler");
      vertx.eventBus().publish("monitoring", new JsonObject().put("message", "Hello from Java").put("from", "Receiver"));
    });
  }

  private void hello(RoutingContext context) {
    String paramSentence = context.request().getParam("name");
    Date start = Calendar.getInstance().getTime();
    Future<String> issuer = Future.future();
    sendSentence("issuer", issuer, paramSentence);

    CompositeFuture.any(Arrays.asList(issuer)).setHandler(ar -> {
      if (ar.succeeded()) {
        // All futures succeeded
        System.out.println("[Receiver] I received the final result");
      } else {
        // At least one future failed
        System.out.println("[Receiver] I received an incomplete result");
      }
      Date end = Calendar.getInstance().getTime();
      JsonObject result = new JsonObject();
      result
              .put("Receiver", new JsonObject().put("Receiver", paramSentence).put("from", hostname + "| " + Thread.currentThread().getName()).put("duration", end.getTime() - start.getTime() + "ms"))
              .put("Issuer", resultEventBus(issuer));
      context.response().putHeader("content-type", "application/json").end(result.encodePrettily());
    });
  }

  private void sendSentence(String serviceName, Future future, String rawObject) {
    vertx.eventBus().send(serviceName, new JsonObject().put("sentence", rawObject), /*new DeliveryOptions().setSendTimeout(10000),*/ reply -> {
      if (reply.succeeded()) {System.out.println("[Receiver] Received from " + serviceName + ":\n" + reply.result().body());
        future.complete(reply.result().body().toString());
      } else {System.out.println("[Receiver] ERROR from " + serviceName +  reply.cause());
        future.fail(reply.cause());
      }
    });
  }

  private void setupSockJsBridge(Router router) {
    SockJSHandler sockJSHandler = SockJSHandler.create(vertx);
    BridgeOptions options = new BridgeOptions()
            .addOutboundPermitted(
                    new PermittedOptions().setAddress("issuer"))
            .addInboundPermitted(
                    new PermittedOptions().setAddress("issuer"));

    sockJSHandler.bridge(options);
    router.route("/eventbus/*").handler(sockJSHandler);
  }

  private String resultEventBus(Future<String> fut){
    if (fut.succeeded()) {
      if (fut.result() != null && !fut.result().isEmpty()){
        return fut.result();
      } else {
        return "Result empty";
      }
    } else if (fut.failed()) {
      return fut.cause().toString();
    } else {
      if (fut.isComplete()){
        return "Future Complete - Not implemented yet";
      } else {
        return "Future Incomplete - Not implemented yet";
      }
    }
  }
}
