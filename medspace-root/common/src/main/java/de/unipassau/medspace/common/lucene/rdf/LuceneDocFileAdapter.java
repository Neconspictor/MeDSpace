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
 * A lucence class-document adapter for RDF types representing a file.
 */
public class LuceneDocFileAdapter<FileType extends IdentifiableFile>
    extends LuceneClassAdapter<FileType> {

  /**
   * Specifies a lucene field that is used to store the URL to the source file.
   */
  public static final String SOURCE = "SOURCE";

  /**
   * Specifies a lucene field that is used to store meta data about the file folder structure.
   */
  public static final String FILE_FOLDER_STRUCTURE_METADATA = "FILE_FOLDER_STRUCTURE_METADATA";

  /**
   * Used to create relative paths for source files.
   */
  protected final File root;

  /**
   * The class mapping for this class.
   */
  protected final FileMapping fileMapping;

  /**
   * The donwload service URL is used in combination with the relative file path (created with the root file)
   * to create a donwload link for the source file.
   */
  protected final String downloadService;



  /**
   * Creates a new LuceneDocFileAdapter object.
   * @param fileMapping The class mapping for this class.
   * @param root Used to create relative paths for source files.
   * @param downloadService The donwload service URL is used in combination with the relative file path (created with the root file)
   * to create a donwload link for the source file.
   * @param decorator Another LuceneClassAdapter that should be used as a decorator.
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
   * Checks if a given property mapping is equal to the file mapping property of this object.
   * @param property The property mapping to check.
   * @return true if the property mapping and 'fileMapping' are equal.
   */
  protected boolean isFileReference(PropertyMapping property) {
    if (fileMapping.getSource().equals(property)) return true;
    return false;
  }
}