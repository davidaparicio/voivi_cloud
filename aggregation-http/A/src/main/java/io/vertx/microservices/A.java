package io.vertx.microservices;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpClient;
import io.vertx.core.json.JsonObject;
import io.vertx.circuitbreaker.CircuitBreaker;
import io.vertx.circuitbreaker.CircuitBreakerOptions;
import io.vertx.servicediscovery.ServiceDiscovery;
import io.vertx.servicediscovery.rest.ServiceDiscoveryRestEndpoint;
import io.vertx.servicediscovery.types.HttpEndpoint;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.ext.web.handler.sockjs.BridgeOptions;
import io.vertx.ext.web.handler.sockjs.PermittedOptions;
import io.vertx.ext.web.handler.sockjs.SockJSHandler;
import org.slf4j.LoggerFactory;

public class A extends AbstractVerticle {
  private static org.slf4j.Logger logger = LoggerFactory.getLogger(A.class);

  private ServiceDiscovery discovery;
  private CircuitBreaker circuitB, circuitC, circuitD, circuitE;
  private HttpClient clientB, clientC, clientD, clientE;

  @Override
  public void start() throws Exception {
    Router router = Router.router(vertx);
    discovery = ServiceDiscovery.create(vertx);
    ServiceDiscoveryRestEndpoint.create(router, discovery);

    CircuitBreakerOptions options = new CircuitBreakerOptions()
        .setMaxFailures(2)
        .setTimeout(2000)
        .setResetTimeout(10000)
        .setFallbackOnFailure(true);

    circuitB = CircuitBreaker.create("B", vertx, options).openHandler(v -> {
      if (clientB != null) {
        clientB.close();
        clientB = null;
      }
    });
    circuitC = CircuitBreaker.create("C", vertx, options).openHandler(v -> {
      if (clientC != null) {
        clientC.close();
        clientC = null;
      }
    });
    ;
    circuitD = CircuitBreaker.create("D", vertx, options).openHandler(v -> {
      if (clientD != null) {
        clientD.close();
        clientD = null;
      }
    });
    ;
    circuitE = CircuitBreaker.create("E", vertx, options).openHandler(v -> {
      if (clientE != null) {
        clientE.close();
        clientE = null;
      }
    });
    ;

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
                }
        );

    vertx.setPeriodic(10000, handler -> {
      System.out.println("[WebVerticle] handler");
      vertx.eventBus().publish("monitoring",
              new JsonObject()
                      .put("message", "Hello from Java")
                      .put("from", "WebVerticle"));
            /*vertx.eventBus().send("events",
                    new JsonObject()
                            .put("message", "Hello from Java")
                            .put("from", "WebVerticle")
                    , reply -> {
                        if (reply.succeeded()) {
                            logger.info("[WebVerticle] Received:\n" + reply.result().body() + " \n[" + Thread.currentThread().getName() + "]");
                        } else {
                            logger.info("[WebVerticle] ERROR:" + reply.cause() + " [" + Thread.currentThread().getName() + "]");
                        }
                    });*/
    });
  }

  private void hello(RoutingContext context) {
    String param = context.request().getParam("name");

    Future<String> b = Future.future();
    Future<String> c = Future.future();
    Future<String> d = Future.future();
    Future<String> e = Future.future();

    getB(client -> {
      invoke("B", client, circuitB, param, b);
    });

    getC(client -> {
      invoke("C", client, circuitC, param, c);
    });

    getD(client -> {
      invoke("D", client, circuitD, param, d);
    });

    getE(client -> {
      invoke("E", client, circuitE, param, e);
    });

    CompositeFuture.all(b, c, d, e).setHandler(ar -> {
      JsonObject result = new JsonObject();
      result
          .put("A", "Hello " + param)
          .put("B", b.result())
          .put("C", c.result())
          .put("D", d.result())
          .put("E", e.result());

      context.response().putHeader("content-type", "application/json").end(result.encodePrettily());
    });
  }

  private void getB(Handler<HttpClient> next) {
    if (clientB != null) {
      next.handle(clientB);
    } else {
      HttpEndpoint.getClient(discovery, new JsonObject().put("name", "B"), ar -> {
        clientB = ar.result();
        next.handle(clientB);
      });
    }
  }

  private void getC(Handler<HttpClient> next) {
    if (clientC != null) {
      next.handle(clientC);
    } else {
      HttpEndpoint.getClient(discovery, new JsonObject().put("name", "C"), ar -> {
        clientC = ar.result();
        next.handle(clientC);
      });
    }
  }

  private void getD(Handler<HttpClient> next) {
    if (clientD != null) {
      next.handle(clientD);
    } else {
      HttpEndpoint.getClient(discovery, new JsonObject().put("name", "D"), ar -> {
        clientD = ar.result();
        next.handle(clientD);
      });
    }
  }

  private void getE(Handler<HttpClient> next) {
    if (clientE != null) {
      next.handle(clientE);
    } else {
      HttpEndpoint.getClient(discovery, new JsonObject().put("name", "E"), ar -> {
        clientE = ar.result();
        next.handle(clientE);
      });
    }
  }

  private void invoke(String name, HttpClient client, CircuitBreaker circuit, String param, Future<String> future) {
    circuit.executeWithFallback(
        circuitFuture -> {
          if (client == null) {
            circuitFuture.fail("No service available");
          } else {
            client.get("/?name=" + param, response -> {
              response.bodyHandler(buffer -> {
                future.complete(buffer.toJsonObject().getString(name));
                circuitFuture.complete();
              });
            }).exceptionHandler(circuitFuture::fail)
              .end();
          }
        },
        v -> {
          // the future has already been completed, a failure or timeout.
          if (!future.isComplete()) future.complete("No service available (fallback)");
          return null;
        }
    );
  }

  private void setupSockJsBridge(Router router) {
    SockJSHandler sockJSHandler = SockJSHandler.create(vertx);
    BridgeOptions options = new BridgeOptions()
        .addOutboundPermitted(
            new PermittedOptions().setAddress("vertx.circuit-breaker"))
        .addInboundPermitted(
            new PermittedOptions().setAddress("vertx.circuit-breaker"))
        .addOutboundPermitted(
            new PermittedOptions().setAddress("monitoring"))
        .addInboundPermitted(
            new PermittedOptions().setAddress("monitoring"));

    sockJSHandler.bridge(options);
    router.route("/eventbus/*").handler(sockJSHandler);
  }
}
