package org.seasar.doma.internal.apt.annot;

import static org.seasar.doma.internal.util.AssertionUtil.assertNotNull;

import java.util.Map;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.ExecutableElement;
import org.seasar.doma.Insert;
import org.seasar.doma.internal.apt.Context;

public class InsertAnnot extends ModifyAnnot {

  protected InsertAnnot(AnnotationMirror annotationMirror) {
    super(annotationMirror);
  }

  public static InsertAnnot newInstance(ExecutableElement method, Context ctx) {
    assertNotNull(ctx);
    AnnotationMirror annotationMirror = ctx.getElements().getAnnotationMirror(method, Insert.class);
    if (annotationMirror == null) {
      return null;
    }
    InsertAnnot result = new InsertAnnot(annotationMirror);
    for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry :
        ctx.getElements().getElementValuesWithDefaults(annotationMirror).entrySet()) {
      String name = entry.getKey().getSimpleName().toString();
      AnnotationValue value = entry.getValue();
      if ("sqlFile".equals(name)) {
        result.sqlFile = value;
      } else if ("queryTimeout".equals(name)) {
        result.queryTimeout = value;
      } else if ("excludeNull".equals(name)) {
        result.excludeNull = value;
      } else if ("include".equals(name)) {
        result.include = value;
      } else if ("exclude".equals(name)) {
        result.exclude = value;
      } else if ("sqlLog".equals(name)) {
        result.sqlLog = value;
      }
    }
    return result;
  }
}
