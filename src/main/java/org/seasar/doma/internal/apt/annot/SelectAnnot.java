package org.seasar.doma.internal.apt.annot;

import static org.seasar.doma.internal.util.AssertionUtil.assertNotNull;

import java.util.Map;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;
import org.seasar.doma.FetchType;
import org.seasar.doma.MapKeyNamingType;
import org.seasar.doma.Select;
import org.seasar.doma.SelectType;
import org.seasar.doma.internal.apt.AptIllegalStateException;
import org.seasar.doma.internal.apt.Context;
import org.seasar.doma.internal.apt.util.AnnotationValueUtil;
import org.seasar.doma.jdbc.SqlLogType;

public class SelectAnnot {

  protected final AnnotationMirror annotationMirror;

  protected AnnotationValue strategy;

  protected AnnotationValue fetch;

  protected AnnotationValue ensureResult;

  protected AnnotationValue ensureResultMapping;

  protected AnnotationValue queryTimeout;

  protected AnnotationValue fetchSize;

  protected AnnotationValue maxRows;

  protected AnnotationValue mapKeyNaming;

  protected AnnotationValue sqlLog;

  protected SelectAnnot(AnnotationMirror annotationMirror) {
    this.annotationMirror = annotationMirror;
  }

  public AnnotationValue getStrategy() {
    return strategy;
  }

  public AnnotationValue getFetch() {
    return fetch;
  }

  public AnnotationValue getEnsureResult() {
    return ensureResult;
  }

  public AnnotationValue getEnsureResultMapping() {
    return ensureResultMapping;
  }

  public AnnotationValue getQueryTimeout() {
    return queryTimeout;
  }

  public AnnotationValue getFetchSize() {
    return fetchSize;
  }

  public AnnotationValue getMaxRows() {
    return maxRows;
  }

  public AnnotationValue getMapKeyNaming() {
    return mapKeyNaming;
  }

  public AnnotationValue getSqlLog() {
    return sqlLog;
  }

  public int getQueryTimeoutValue() {
    Integer value = AnnotationValueUtil.toInteger(queryTimeout);
    if (value == null) {
      throw new AptIllegalStateException("queryTimeout");
    }
    return value.intValue();
  }

  public int getFetchSizeValue() {
    Integer value = AnnotationValueUtil.toInteger(fetchSize);
    if (value == null) {
      throw new AptIllegalStateException("fetchSize");
    }
    return value.intValue();
  }

  public int getMaxRowsValue() {
    Integer value = AnnotationValueUtil.toInteger(maxRows);
    if (value == null) {
      throw new AptIllegalStateException("maxRows");
    }
    return value.intValue();
  }

  public SelectType getStrategyValue() {
    VariableElement enumConstant = AnnotationValueUtil.toEnumConstant(strategy);
    if (enumConstant == null) {
      throw new AptIllegalStateException("strategy");
    }
    return SelectType.valueOf(enumConstant.getSimpleName().toString());
  }

  public FetchType getFetchValue() {
    VariableElement enumConstant = AnnotationValueUtil.toEnumConstant(fetch);
    if (enumConstant == null) {
      throw new AptIllegalStateException("fetch");
    }
    return FetchType.valueOf(enumConstant.getSimpleName().toString());
  }

  public boolean getEnsureResultValue() {
    Boolean value = AnnotationValueUtil.toBoolean(ensureResult);
    if (value == null) {
      throw new AptIllegalStateException("ensureResult");
    }
    return value.booleanValue();
  }

  public boolean getEnsureResultMappingValue() {
    Boolean value = AnnotationValueUtil.toBoolean(ensureResultMapping);
    if (value == null) {
      throw new AptIllegalStateException("ensureResultMapping");
    }
    return value.booleanValue();
  }

  public MapKeyNamingType getMapKeyNamingValue() {
    VariableElement enumConstant = AnnotationValueUtil.toEnumConstant(mapKeyNaming);
    if (enumConstant == null) {
      throw new AptIllegalStateException("mapKeyNaming");
    }
    return MapKeyNamingType.valueOf(enumConstant.getSimpleName().toString());
  }

  public SqlLogType getSqlLogValue() {
    VariableElement enumConstant = AnnotationValueUtil.toEnumConstant(sqlLog);
    if (enumConstant == null) {
      throw new AptIllegalStateException("sqlLog");
    }
    return SqlLogType.valueOf(enumConstant.getSimpleName().toString());
  }

  public AnnotationMirror getAnnotationMirror() {
    return annotationMirror;
  }

  public static SelectAnnot newInstance(ExecutableElement method, Context ctx) {
    assertNotNull(ctx);
    AnnotationMirror annotationMirror = ctx.getElements().getAnnotationMirror(method, Select.class);
    if (annotationMirror == null) {
      return null;
    }
    SelectAnnot result = new SelectAnnot(annotationMirror);
    for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry :
        ctx.getElements().getElementValuesWithDefaults(annotationMirror).entrySet()) {
      String name = entry.getKey().getSimpleName().toString();
      AnnotationValue value = entry.getValue();
      if ("strategy".equals(name)) {
        result.strategy = value;
      } else if ("fetch".equals(name)) {
        result.fetch = value;
      } else if ("ensureResult".equals(name)) {
        result.ensureResult = value;
      } else if ("ensureResultMapping".equals(name)) {
        result.ensureResultMapping = value;
      } else if ("queryTimeout".equals(name)) {
        result.queryTimeout = value;
      } else if ("fetchSize".equals(name)) {
        result.fetchSize = value;
      } else if ("maxRows".equals(name)) {
        result.maxRows = value;
      } else if ("mapKeyNaming".equals(name)) {
        result.mapKeyNaming = value;
      } else if ("sqlLog".equals(name)) {
        result.sqlLog = value;
      }
    }
    return result;
  }
}
