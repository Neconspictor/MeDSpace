package de.unipassau.medspace.register;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import de.unipassau.medspace.common.register.Datasource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.inject.ApplicationLifecycle;
import play.libs.Json;

import javax.inject.Inject;
import java.io.*;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * Created by David Goeth on 10.10.2017.
 */
public class RegisterLifecycle {

  private static final Logger log = LoggerFactory.getLogger(RegisterLifecycle.class);
  private static final String DATASOURCES_STORE_LOCAL_PATH = "./datasources.json";

  private final Register register;

  @Inject
  public RegisterLifecycle(ApplicationLifecycle lifecycle) throws IOException {

    log.info("Load saved datasources to register...");
    SimpleModule simpleModule = new SimpleModule();
    simpleModule.addKeyDeserializer(Datasource.Builder.class, new DatasourceKeyDeserializer());
    Json.mapper().registerModule(simpleModule);


    Map<Datasource, Timestamp> datasources = null;
    try {
      datasources = loadFromDisk();
    } catch (IOException e) {
     log.error("Couldn't load stored datasources from disk", e);
    }
    register = new Register(datasources);

    lifecycle.addStopHook(() -> {
      log.info("shutdown is executing...");
      try {
        saveToDisk(getRegister());
      } catch (IOException e) {
        log.error("Couldn't store registered datasources to disk", e);
      }
      log.info("shutdown cleanup done.");
      return CompletableFuture.completedFuture(null);
    });
  }

  public static String getDatasourcesStoreLocalPath() {
    return DATASOURCES_STORE_LOCAL_PATH;
  }

  private Map<Datasource, Timestamp> loadFromDisk() throws IOException {
    File datasourceFile = new File(DATASOURCES_STORE_LOCAL_PATH);
    Map<Datasource.Builder, Timestamp> datasourceBuilders = null;
    Map<Datasource, Timestamp> datasources = new HashMap<>();

    if (datasourceFile.exists()) {
      ObjectMapper mapper = new ObjectMapper();

      JsonNode root = null;
      try(FileInputStream in = new FileInputStream(datasourceFile)) {
        root = mapper.reader().readTree(in);
      }

      Iterator<JsonNode> elements = root.elements();
      while(elements.hasNext()) {
        JsonNode elem = elements.next();
        Datasource.Builder builder = Json.fromJson(elem.get("key"), Datasource.Builder.class);
        Timestamp timestamp = Json.fromJson(elem.get("value"), Timestamp.class);
        datasources.put(builder.build(), timestamp);
      }

      /*datasourceBuilders = Json.mapper()
          .reader()
          .forType(new TypeReference<Map<Datasource.Builder,Timestamp>>() {})
          .readValue(root);

      //now build the final datasource map
      datasourceBuilders.forEach((builder, timestamp) -> {
        datasources.put(builder.build(), timestamp);
      });*/

    }

    return datasources;
  }

  private void saveToDisk(Register register) throws IOException {
    log.info("Save registered datasources to disk...");
    Map<Datasource, Timestamp> datasources = register.getDatasources();
    Map<Datasource.Builder, Timestamp> datasourceBuilders = new HashMap<>();
    datasources.forEach((datasource, timestamp) -> {
      datasourceBuilders.put(new Datasource.Builder(datasource), timestamp);
    });
    try (FileOutputStream out = new FileOutputStream(DATASOURCES_STORE_LOCAL_PATH)) {
      //JsonNode node = Json.toJson(datasourceBuilders);

      ArrayNode root = Json.newArray();

      for (Map.Entry<Datasource.Builder, Timestamp> entry : datasourceBuilders.entrySet()) {
        JsonNode key = Json.toJson(entry.getKey());
        JsonNode value = Json.toJson(entry.getValue());
        ObjectNode pair = Json.newObject();
        pair.putPOJO("key", entry.getKey())
                   .putPOJO("value", entry.getValue());
        root.add(pair);
      }
      Json.mapper().writer().writeValue(out, root);
    }
  }

  public Register getRegister() {
    return register;
  }

  public static class DatasourceKeyDeserializer extends com.fasterxml.jackson.databind.KeyDeserializer {
    @Override
    public Object deserializeKey(String key, DeserializationContext ctxt) throws IOException, JsonProcessingException {
      // The key is a Datasource-Builder object
      key = key.substring(1, key.length() - 1);
      return Json.mapper()
          .reader()
          .forType(Datasource.Builder.class)
          .readValue(key);
    }
  }
}