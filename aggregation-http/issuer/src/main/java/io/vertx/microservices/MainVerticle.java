package io.vertx.microservices;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import java.net.InetAddress;
import java.net.UnknownHostException;


public class MainVerticle extends AbstractVerticle {

  private static final Logger LOGGER = LoggerFactory.getLogger(MainVerticle.class.getName());

  //private Record record;
  //private ServiceDiscovery discovery;

  @Override
  public void start(Future future) throws Exception {
    DeploymentOptions options = new DeploymentOptions().setConfig(config());
    vertx.deployVerticle(Issuer.class.getName(), options);
    /*if (!config().getBoolean("openshift", false)) {
      discovery = ServiceDiscovery.create(vertx);
      publishService(future, discovery, "D");
    } else {*/
      future.complete();
    //}
  }

  @Override
  public void stop(Future future) throws Exception {
    /*if (discovery != null && record != null) {
      discovery.unpublish(record.getRegistration(), ar -> {
        LOGGER.info("D has been un-published");
        future.complete();
      });
    } else {*/
      future.complete();
    //}
  }

  /*private void publishService(Future future, ServiceDiscovery discovery, String name) {
    String myCurrentIp = "";
    try {
      myCurrentIp = InetAddress.getLocalHost().getHostAddress();
    } catch (UnknownHostException e) {
      myCurrentIp = "localhost";
    } finally {
      discovery.publish(HttpEndpoint.createRecord(name, myCurrentIp, config().getInteger("port"), "/"),
              published -> {
                if (published.succeeded()) {
                  this.record = published.result();
                  LOGGER.info(name + " has been published");
                  future.complete();
                } else {
                  future.fail("Cannot publish " + name + ": " + published.cause());
                }
              }
      );
    }
  }*/
}
