package io.vertx.microservices;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import org.slf4j.LoggerFactory;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.neural.rnn.RNNCoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.util.PropertiesUtils;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;

public class B extends AbstractVerticle {
  private static org.slf4j.Logger logger = LoggerFactory.getLogger(B.class);
  private static StanfordCoreNLP pipeline;

  @Override
  public void start() throws Exception {
    init();
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
      double sentimentParam = findSentiment(param);
      Date end = Calendar.getInstance().getTime();
      context.response()
          .putHeader("content-type", "application/json")
          .end(new JsonObject()
                  .put("B", sentimentParam)
                  .put("from", finalHostname + "| " + Thread.currentThread().getName())
                  .put("duration", end.getTime() - start.getTime() +"ms")
                  .encodePrettily());
    });

    vertx.setPeriodic(10000, handler -> {
      System.out.println("[B] handler");
      vertx.eventBus().publish("monitoring",
              new JsonObject()
                      .put("message", "Hello from Java")
                      .put("from", "B"));
    });

    vertx.createHttpServer()
        .requestHandler(router::accept)
        .listen(config().getInteger("port"));
  }

  public void init() {
    // Create the Stanford CoreNLP pipeline
    Properties props = PropertiesUtils.asProperties("annotators", "tokenize, ssplit, parse, sentiment");
    pipeline = new StanfordCoreNLP(props); //process the pipeline
  }

  public double findSentiment(String paragraph) {
    double mainSentiment = -1.;
    if (paragraph != null && paragraph.length() > 0) {
      int longest = 0;
      Annotation annotation = pipeline.process(paragraph);
      int sentenceNo = 0;
      for (CoreMap sentence : annotation.get(CoreAnnotations.SentencesAnnotation.class)) {
        sentenceNo++;
        Tree tree = sentence.get(SentimentCoreAnnotations.SentimentAnnotatedTree.class);
        String partText = sentence.toString();
        int sentiment = RNNCoreAnnotations.getPredictedClass(tree);
        logger.trace("Sentence #" + sentenceNo + ": (" + sentiment + ") " + partText);
        if (partText.length() > longest) {
          mainSentiment = sentiment;
          longest = partText.length();
        }
      }
    }
    return mainSentiment;
  }

}
