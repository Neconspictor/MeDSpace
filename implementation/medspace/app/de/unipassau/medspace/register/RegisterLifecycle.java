package de.unipassau.medspace.register;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import de.unipassau.medspace.common.register.Datasource;

import de.unipassau.medspace.common.register.DatasourceState;
import de.unipassau.medspace.global.config.GlobalConfigProvider;
import de.unipassau.medspace.global.config.mapping.RegisterMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.inject.ApplicationLifecycle;
import play.libs.Json;

import javax.inject.Inject;
import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Lifecycle management for the Register module.
 * Is responsible for reading datasources from a save file on startup and saving registered datasources to the save
 * file when the application terminates. Additionally it is a provider for the Register.
 */
public class RegisterLifecycle {

  private static final Logger log = LoggerFactory.getLogger(RegisterLifecycle.class);
  private static final String DATASOURCES_STORE_LOCAL_PATH = "./datasources.json";

  private final Register register;

  /**
   * Creates a new RegisterLifecycle object.
   * @param lifecycle The application lifecycle.
   * @throws IOException If an IO error occurs.
   */
  @Inject
  public RegisterLifecycle(ApplicationLifecycle lifecycle,
                           GlobalConfigProvider provider) throws IOException {

    log.info("Load saved datasources to register...");
    SimpleModule simpleModule = new SimpleModule();
    simpleModule.addKeyDeserializer(Datasource.class, new DatasourceKeyDeserializer());
    simpleModule.addKeySerializer(Datasource.class, new DatasourceKeySerializer());
    Json.mapper().registerModule(simpleModule);

    RegisterMapping registerMapping = provider.getGlobalConfig().getRegister();


    Map<Datasource, DatasourceState> datasources = null;
    try {
      datasources = loadFromDisk();
    } catch (IOException e) {
     log.error("Couldn't load stored datasources from disk", e);
    }
    register = new Register(datasources, registerMapping.getIOErrorLimit());

    lifecycle.addStopHook(() -> {
      log.info("shutdown is executing...");
      try {
        register.close();
        saveToDisk(register);
      } catch (IOException e) {
        log.error("Couldn't store registered datasources to disk", e);
      }
      log.info("shutdown cleanup done.");
      return CompletableFuture.completedFuture(null);
    });
  }

  private Map<Datasource, DatasourceState> loadFromDisk() throws IOException {
    File datasourceFile = new File(DATASOURCES_STORE_LOCAL_PATH);
    Map<Datasource, DatasourceState> datasources = new HashMap<>();

    if (datasourceFile.exists()) {
      ObjectMapper mapper = new ObjectMapper();

      JsonNode root = null;
      try(FileInputStream in = new FileInputStream(datasourceFile)) {
        root = mapper.reader().readTree(in);
      }

      datasources = Json.mapper()
          .reader()
          .forType(new TypeReference<Map<Datasource,DatasourceState>>() {})
          .readValue(root);
    }

    return datasources;
  }

  private void saveToDisk(Register register) throws IOException {
    log.info("Save registered datasources to disk...");
    Map<Datasource, DatasourceState> datasources = register.getDatasources();

    try (FileOutputStream out = new FileOutputStream(DATASOURCES_STORE_LOCAL_PATH)) {
      JsonNode node = Json.toJson(datasources);
      Json.mapper().writer().writeValue(out, node);
    }
  }

  /**
   * Provides the Register.
   * @return the Register.
   */
  public Register getRegister() {
    return register;
  }

  /**
   * Deserializes a datasource from JSON that is used as a key in a map.
   */
  public static class DatasourceKeyDeserializer extends com.fasterxml.jackson.databind.KeyDeserializer {
    @Override
    public Object deserializeKey(String key, DeserializationContext ctxt) throws IOException {
      return Json.mapper()
          .reader()
          .forType(Datasource.class)
          .readValue(key);
    }
  }

  /**
   * Serializes a datasource to JSON that is used as a key in a map.
   */
  public static class DatasourceKeySerializer extends StdSerializer<Datasource> {

    protected DatasourceKeySerializer() {
      super(Datasource.class, false);
    }

    @Override
    public void serialize(Datasource value, JsonGenerator gen, SerializerProvider provider) throws IOException {
      JsonNode node  = Json.toJson(value);
      String json = Json.mapper().writeValueAsString(node);
      gen.writeFieldName(json);
    }
  }
}