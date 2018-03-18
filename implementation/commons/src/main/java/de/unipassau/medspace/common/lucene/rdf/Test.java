package de.unipassau.medspace.common.lucene.rdf;

import de.unipassau.medspace.common.rdf.mapping.*;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexableField;
import org.javatuples.Pair;

import java.io.File;
import java.io.IOException;

/**
 * Created by David Goeth on 3/18/2018.
 */
public class Test {

  public void foo() throws IOException {


    FileMapping fileMapping = null;
    File root = null;

    LuceneDocFileAdapter<Image> fileAdapter= new LuceneDocFileAdapter<>(fileMapping, root, null);
    DocumentAdapter<Image, Document, IndexableField> imageAdapter = new DDSM_CaseAdapter<>(fileAdapter);

    Document doc = imageAdapter.convert(new Image("test", new File("./")));

  }

  private static class Image extends IdentifiableFile {

    /**
     * TODO
     *
     * @param id
     * @param source
     */
    public Image(String id, File source) {
      super(id, source);
    }
  }

  private static class DDSM_CaseAdapter<ClassType extends Identifiable> extends LuceneClassAdapter<ClassType> {
    public DDSM_CaseAdapter(LuceneClassAdapter<? super ClassType> decorator) {
      super(decorator);
    }

    @Override
    protected void addFields(ClassType source, Document doc) throws IOException {

    }

    @Override
    protected String getValue(Pair<String, PropertyMapping> pair, IndexableField field) {
      return null;
    }
  }
}