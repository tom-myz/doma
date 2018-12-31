package org.seasar.doma.internal.apt.annot;

import static org.seasar.doma.internal.util.AssertionUtil.assertNotNull;

import java.util.Map;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;
import org.seasar.doma.Function;
import org.seasar.doma.MapKeyNamingType;
import org.seasar.doma.internal.apt.AptIllegalStateException;
import org.seasar.doma.internal.apt.Context;
import org.seasar.doma.internal.apt.util.AnnotationValueUtil;
import org.seasar.doma.jdbc.SqlLogType;

public class FunctionAnnot {

  protected final AnnotationMirror annotationMirror;

  protected final String defaultName;

  protected AnnotationValue catalog;

  protected AnnotationValue schema;

  protected AnnotationValue name;

  protected AnnotationValue quote;

  protected AnnotationValue queryTimeout;

  protected AnnotationValue mapKeyNaming;

  protected AnnotationValue ensureResultMapping;

  protected AnnotationValue sqlLog;

  protected FunctionAnnot(AnnotationMirror annotationMirror, String defaultName) {
    assertNotNull(annotationMirror, defaultName);
    this.annotationMirror = annotationMirror;
    this.defaultName = defaultName;
  }

  public static FunctionAnnot newInstance(ExecutableElement method, Context ctx) {
    assertNotNull(ctx);
    AnnotationMirror annotationMirror =
        ctx.getElements().getAnnotationMirror(method, Function.class);
    if (annotationMirror == null) {
      return null;
    }
    FunctionAnnot result = new FunctionAnnot(annotationMirror, method.getSimpleName().toString());
    for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry :
        ctx.getElements().getElementValuesWithDefaults(annotationMirror).entrySet()) {
      String name = entry.getKey().getSimpleName().toString();
      AnnotationValue value = entry.getValue();
      if ("catalog".equals(name)) {
        result.catalog = value;
      } else if ("schema".equals(name)) {
        result.schema = value;
      } else if ("name".equals(name)) {
        result.name = value;
      } else if ("quote".equals(name)) {
        result.quote = value;
      } else if ("queryTimeout".equals(name)) {
        result.queryTimeout = value;
      } else if ("mapKeyNaming".equals(name)) {
        result.mapKeyNaming = value;
      } else if ("ensureResultMapping".equals(name)) {
        result.ensureResultMapping = value;
      } else if ("sqlLog".equals(name)) {
        result.sqlLog = value;
      }
    }
    return result;
  }

  public AnnotationValue getQueryTimeout() {
    return queryTimeout;
  }

  public AnnotationValue getMapKeyNaming() {
    return mapKeyNaming;
  }

  public AnnotationValue getSqlLog() {
    return sqlLog;
  }

  public String getCatalogValue() {
    String value = AnnotationValueUtil.toString(catalog);
    if (value == null) {
      throw new AptIllegalStateException("catalog");
    }
    return value;
  }

  public String getSchemaValue() {
    String value = AnnotationValueUtil.toString(schema);
    if (value == null) {
      throw new AptIllegalStateException("schema");
    }
    return value;
  }

  public String getNameValue() {
    String value = AnnotationValueUtil.toString(name);
    if (value == null || value.isEmpty()) {
      return defaultName;
    }
    return value;
  }

  public boolean getQuoteValue() {
    Boolean value = AnnotationValueUtil.toBoolean(quote);
    if (value == null) {
      throw new AptIllegalStateException("quote");
    }
    return value.booleanValue();
  }

  public int getQueryTimeoutValue() {
    Integer value = AnnotationValueUtil.toInteger(queryTimeout);
    if (value == null) {
      throw new AptIllegalStateException("queryTimeout");
    }
    return value.intValue();
  }

  public MapKeyNamingType getMapKeyNamingValue() {
    VariableElement enumConstant = AnnotationValueUtil.toEnumConstant(mapKeyNaming);
    if (enumConstant == null) {
      throw new AptIllegalStateException("mapKeyNaming");
    }
    return MapKeyNamingType.valueOf(enumConstant.getSimpleName().toString());
  }

  public boolean getEnsureResultMappingValue() {
    Boolean value = AnnotationValueUtil.toBoolean(ensureResultMapping);
    if (value == null) {
      throw new AptIllegalStateException("ensureResultMapping");
    }
    return value.booleanValue();
  }

  public SqlLogType getSqlLogValue() {
    VariableElement enumConstant = AnnotationValueUtil.toEnumConstant(sqlLog);
    if (enumConstant == null) {
      throw new AptIllegalStateException("sqlLog");
    }
    return SqlLogType.valueOf(enumConstant.getSimpleName().toString());
  }
}
