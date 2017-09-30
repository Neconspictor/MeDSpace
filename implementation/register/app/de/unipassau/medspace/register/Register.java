package de.unipassau.medspace.register;

import akka.actor.AbstractActor;
import akka.actor.Props;
import de.unipassau.medspace.register.Protocol.OperationStatus;
import de.unipassau.medspace.register.common.Datasource;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by David Goeth on 14.09.2017.
 */
public class Register extends AbstractActor {

  private final Map<String, Datasource> datasources;

  public Register() {
    // datasources has to be thread-safe
    this.datasources = new ConcurrentHashMap<String, Datasource>();
  }

  public static Props getProps() {
    return Props.create(Register.class);
  }

  @Override
  public Receive createReceive() {
    return receiveBuilder()
        .match(Protocol.AddDatasource.class, this::addDatasource)
        .match(Protocol.RemoveDatasource.class, this::removeDatasource)
        .match(Protocol.DatasourceNoRespond.class, this::datasourceNoRespond)
        .match(Protocol.GetDatasources.class, this::getDatasources)
        .build();
  }

  private void addDatasource(Protocol.AddDatasource message) {
    Datasource datasource = message.datasource;
    if (datasources.get(datasource.getUrl()) == null) {
      datasources.put(datasource.getUrl().toExternalForm(), datasource);
      sender().tell(new OperationStatus.Ok(), self());
    } else {
      sender().tell(new OperationStatus.NoChangesDone(), self());
    }
  }

  private void datasourceNoRespond(Protocol.DatasourceNoRespond message) {
    // For now just remove the datasource
    removeDatasource(new Protocol.RemoveDatasource(message.datasource));
  }

  private void getDatasources(Protocol.GetDatasources message) {
    List<Datasource> list = new ArrayList<>(datasources.values());
    sender().tell(new Protocol.GetDatasourcesResult(list), self());
  }

  private void removeDatasource(Protocol.RemoveDatasource message) {
    Datasource datasource = message.datasource;
    if (datasources.get(datasource.getUrl()) != null) {
      datasources.remove(datasource.getUrl());
      sender().tell(new OperationStatus.Ok(), self());
    } else {
      sender().tell(new OperationStatus.NoChangesDone(), self());
    }
  }
}