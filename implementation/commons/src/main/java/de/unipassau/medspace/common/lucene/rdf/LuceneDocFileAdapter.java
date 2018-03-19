package de.unipassau.medspace.common.lucene.rdf;

import de.unipassau.medspace.common.rdf.mapping.FileMapping;
import de.unipassau.medspace.common.rdf.mapping.IdentifiableFile;
import de.unipassau.medspace.common.rdf.mapping.PropertyMapping;
import de.unipassau.medspace.common.register.Service;
import de.unipassau.medspace.common.util.FileUtil;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexableField;
import org.javatuples.Pair;

import java.io.File;
import java.io.IOException;

/**
 * TODO
 */
public class LuceneDocFileAdapter<FileType extends IdentifiableFile>
    extends LuceneClassAdapter<FileType> {

  /**
   * TODO
   */
  public static final String SOURCE = "SOURCE";

  /**
   * TODO
   */
  public static final String FILE_FOLDER_STRUCTURE_METADATA = "FILE_FOLDER_STRUCTURE_METADATA";

  protected final File root;

  protected final FileMapping fileMapping;

  protected final String downloadService;



  /**
   * TODO
   * @param fileMapping
   * @param root
   * @param downloadService
   * @param decorator
   */
  public LuceneDocFileAdapter(FileMapping fileMapping,
                              File root,
                              String downloadService,
                              LuceneClassAdapter<? super FileType> decorator) {
    super(fileMapping, decorator);

    this.root = root;
    this.fileMapping = fileMapping;
    this.downloadService = downloadService;

    addPair(SOURCE, fileMapping.getSource());
    //this.metaDataFields.add(FILE_FOLDER_STRUCTURE_METADATA);
  }

  @Override
  protected  void addFields(FileType source, Document doc) throws IOException {
      String value = FileUtil.getRelativePath(root, source.getSource());
      doc.add(createField(SOURCE, value));

      // add content of the image folder structure as searchable meta-data
      // we use not File.separaotor as for URIs '/' is always used!
      //List<String> tokens = StringUtil.tokenize(value, "/");
      //String concatenated = StringUtil.concat(tokens, " ");

      //doc.add(createField(FILE_FOLDER_STRUCTURE_METADATA, concatenated));
  }

  @Override
  protected String getValue(Pair<String, PropertyMapping> pair, IndexableField field) {
    PropertyMapping property = pair.getValue1();

    if (isFileReference(property)) {
      return downloadService + field.stringValue();
    }

    return null;
  }

  /**
   * TODO
   * @param property
   * @return
   */
  protected boolean isFileReference(PropertyMapping property) {
    if (fileMapping.getSource().equals(property)) return true;
    return false;
  }
}