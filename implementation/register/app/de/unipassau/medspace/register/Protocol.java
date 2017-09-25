package de.unipassau.medspace.register;

import de.unipassau.medspace.register.common.Datasource;

import java.util.List;

public class Protocol {

  public static class OperationStatus {
    public static class Ok {}
    public static class NoChangesDone{}
  }

  public static class AddDatasource {
    public final Datasource datasource;
    public AddDatasource(Datasource datasource){
      this.datasource = datasource;
    }
  }

  public static class DatasourceNoRespond {
    public final Datasource datasource;
    public DatasourceNoRespond(Datasource datasource){
      this.datasource = datasource;
    }
  }

  public static class GetDatasources {}

  public static class GetDatasourcesResult {
    public final List<Datasource> datasources;

    public GetDatasourcesResult(List<Datasource> datasources) {
      this.datasources = datasources;
    }
  }

  public static class RemoveDatasource {
    public final Datasource datasource;
    public RemoveDatasource(Datasource datasource){
      this.datasource = datasource;
    }
  }
}