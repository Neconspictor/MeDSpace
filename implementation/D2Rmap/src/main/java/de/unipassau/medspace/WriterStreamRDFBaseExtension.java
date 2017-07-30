package de.unipassau.medspace;

import org.apache.jena.riot.system.PrefixMap;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.riot.system.StreamRDFWrapper;
import org.apache.jena.riot.writer.WriterStreamRDFBase;
import org.apache.jena.shared.PrefixMapping;
import java.lang.reflect.Field;

/**
 * Created by David Goeth on 27.07.2017.
 */
public class WriterStreamRDFBaseExtension {

  private WriterStreamRDFBase source;
  private PrefixMap map;

  public WriterStreamRDFBaseExtension(StreamRDF stream) {

    if (stream instanceof StreamRDFWrapper) {
      source = initSource((StreamRDFWrapper) stream);
    } else if (stream instanceof WriterStreamRDFBase) {
      source = (WriterStreamRDFBase)stream;
    }

    map = initPMap(source);
  }

  public void addPrefixMapping(PrefixMapping mapping) {
    map.putAll(mapping);
  }

  public WriterStreamRDFBase getSource() {
    return source;
  }


  private WriterStreamRDFBase initSource(StreamRDFWrapper stream) {
    Field other = getField(StreamRDFWrapper.class, "other");
    other.setAccessible(true);
    return getClassMember(other, stream, WriterStreamRDFBase.class);
  }

  private PrefixMap initPMap(WriterStreamRDFBase stream) {
    Field pMapField = getField(WriterStreamRDFBase.class,"pMap");
    pMapField.setAccessible(true);
    return getClassMember(pMapField, stream, PrefixMap.class);
  }


  private static Field getField(Class clazz, String fieldName)
      throws IllegalStateException {
    try {
      return clazz.getDeclaredField(fieldName);
    } catch (NoSuchFieldException e) {
      Class superClass = clazz.getSuperclass();
      if (superClass == null) {
        throw new IllegalStateException("Field " + fieldName +  " not found");
      } else {
        return getField(superClass, fieldName);
      }
    }
  }

  private static <T> T getClassMember (Field field, Object instance, Class<T> clazz) throws IllegalStateException {
    try {
     return (T) field.get(instance);
    } catch (IllegalAccessException e) {
      throw new IllegalStateException("Couldn't access field " + field);
    } catch (ClassCastException e) {
      throw new IllegalStateException("Couldn't cast field object to type " + clazz);
    }
  }
}