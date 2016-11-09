package io.vertx.microservices;

import edu.stanford.nlp.ie.util.RelationTriple;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.naturalli.NaturalLogicAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.util.PropertiesUtils;
import edu.stanford.nlp.util.Triple;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Properties;

public class C extends AbstractVerticle {
  private static org.slf4j.Logger logger = LoggerFactory.getLogger(C.class);
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
      Triple tripleParam = findSubject(param);
      Date end = Calendar.getInstance().getTime();
      context.response()
          .putHeader("content-type", "application/json")
          .end(new JsonObject()
                  .put("C", new JsonObject()
                    .put("subject", tripleParam.first())
                    .put("verb", tripleParam.second())
                    .put("object", tripleParam.third())
                  )
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

  public static void init() {
    // Create the Stanford CoreNLP pipeline
    Properties props = PropertiesUtils.asProperties("annotators", "tokenize,ssplit,pos,lemma,depparse,natlog,openie");
    pipeline = new StanfordCoreNLP(props); //process the pipeline
  }

  public Triple findSubject(String paragraph) {
    Triple subjectTriple = new Triple("","","");
    // Annotate an example document.
    Annotation doc = new Annotation(paragraph);
    pipeline.annotate(doc);
    // Loop over sentences in the document
    int sentenceNo = 0;
    for (CoreMap sentence : doc.get(CoreAnnotations.SentencesAnnotation.class)) {
      logger.trace("Sentence #" + ++sentenceNo + ": " + sentence.get(CoreAnnotations.TextAnnotation.class));
      // Get the OpenIE triples for the sentence
      Collection<RelationTriple> triples = sentence.get(NaturalLogicAnnotations.RelationTriplesAnnotation.class);
      // Print the triples
      if (triples != null && !(triples.isEmpty())) {
        RelationTriple triple = triples.iterator().next();
        subjectTriple.setFirst(triple.subjectLemmaGloss());
        subjectTriple.setSecond(triple.relationLemmaGloss());
        subjectTriple.setThird(triple.objectLemmaGloss());
        logger.trace("(" +
                triple.subjectLemmaGloss() + "," +
                triple.relationLemmaGloss() + "," +
                triple.objectLemmaGloss() + ")");
      } else {
        subjectTriple.setFirst("*");
        subjectTriple.setSecond("*");
        subjectTriple.setThird("*");
      }
    }
    return subjectTriple;
  }

}
