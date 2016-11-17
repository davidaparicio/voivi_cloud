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

public class A extends AbstractVerticle {
  private static org.slf4j.Logger logger = LoggerFactory.getLogger(A.class);
  //private ServiceDiscovery discovery;

  @Override
  public void start() throws Exception {
    Router router = Router.router(vertx);
    //discovery = ServiceDiscovery.create(vertx);
    //ServiceDiscoveryRestEndpoint.create(router, discovery);

    router.route("/assets/*").handler(StaticHandler.create("assets"));
    router.get("/A").handler(this::hello);
    setupSockJsBridge(router);

    vertx.createHttpServer()
      .requestHandler(router::accept)
      .listen(config().getInteger("port"), handler -> {
        if (handler.succeeded()) {
          System.out.println("[WebServer] - http://localhost:8080/");
        } else {
          System.out.println("[WebServer] - Failed to listen on port 8080");
        }
      });

    vertx.setPeriodic(10000, handler -> { System.out.println("[WebVerticle] handler");
      vertx.eventBus().publish("monitoring", new JsonObject().put("message", "Hello from Java").put("from", "WebVerticle"));
    });
  }

  private void hello(RoutingContext context) {
    String paramSentence = context.request().getParam("name");
    Future<String> b = Future.future();
    Future<String> c = Future.future();
    Future<String> d = Future.future();
    Future<String> e = Future.future();
    sendSentence("b", b, paramSentence);
    sendSentence("c", c, paramSentence);
    sendSentence("d", d, paramSentence);
    sendSentence("e", e, paramSentence);

    CompositeFuture.all(b, c, d, e).setHandler(ar -> {
      if (ar.succeeded()) {
        // All futures succeeded
        System.out.println("[WebVerticle] I received the final result");
      } else {
        // At least one future failed
        System.out.println("[WebVerticle] I received an incomplete result");
      }
      JsonObject result = new JsonObject();
      result
              .put("A", paramSentence)
              .put("B", resultEventBus(b))
              .put("C", resultEventBus(c))
              .put("D", resultEventBus(d))
              .put("E", resultEventBus(e));
      context.response().putHeader("content-type", "application/json").end(result.encodePrettily());
    });
  }

  private void sendSentence(String serviceName, Future future, String rawObject) {
    vertx.eventBus().send(serviceName, new JsonObject().put("sentence", rawObject), /*new DeliveryOptions().setSendTimeout(10000),*/ reply -> {
      if (reply.succeeded()) {System.out.println("[WebVerticle] Received from " + serviceName + ":\n" + reply.result().body());
        future.complete(reply.result().body().toString());
      } else {System.out.println("[WebVerticle] ERROR from " + serviceName +  reply.cause());
        future.fail(reply.cause());
      }
    });
  }

  private void setupSockJsBridge(Router router) {
    SockJSHandler sockJSHandler = SockJSHandler.create(vertx);
    BridgeOptions options = new BridgeOptions()
      .addOutboundPermitted(
        new PermittedOptions().setAddress("b"))
      .addInboundPermitted(
        new PermittedOptions().setAddress("b"))
      .addOutboundPermitted(
        new PermittedOptions().setAddress("c"))
      .addInboundPermitted(
        new PermittedOptions().setAddress("c"))
      .addOutboundPermitted(
        new PermittedOptions().setAddress("d"))
      .addInboundPermitted(
        new PermittedOptions().setAddress("d"))
      .addOutboundPermitted(
        new PermittedOptions().setAddress("e"))
      .addInboundPermitted(
        new PermittedOptions().setAddress("e"))
      .addOutboundPermitted(
        new PermittedOptions().setAddress("monitoring"))
      .addInboundPermitted(
        new PermittedOptions().setAddress("monitoring"));

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
