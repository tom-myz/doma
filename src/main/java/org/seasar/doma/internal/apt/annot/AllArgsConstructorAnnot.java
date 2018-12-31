package org.seasar.doma.internal.apt.annot;

import static org.seasar.doma.internal.util.AssertionUtil.assertNotNull;

import java.util.Map;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import org.seasar.doma.internal.apt.AptIllegalStateException;
import org.seasar.doma.internal.apt.Context;
import org.seasar.doma.internal.apt.util.AnnotationValueUtil;

public class AllArgsConstructorAnnot {

  protected final AnnotationMirror annotationMirror;

  protected AnnotationValue staticName;

  protected AnnotationValue access;

  protected AllArgsConstructorAnnot(AnnotationMirror annotationMirror) {
    assertNotNull(annotationMirror);
    this.annotationMirror = annotationMirror;
  }

  public static AllArgsConstructorAnnot newInstance(TypeElement typeElement, Context ctx) {
    assertNotNull(ctx);
    AnnotationMirror annotationMirror =
        ctx.getElements()
            .getAnnotationMirror(typeElement, ctx.getOptions().getLombokAllArgsConstructor());
    if (annotationMirror == null) {
      return null;
    }
    AllArgsConstructorAnnot result = new AllArgsConstructorAnnot(annotationMirror);
    for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry :
        ctx.getElements().getElementValuesWithDefaults(annotationMirror).entrySet()) {
      String name = entry.getKey().getSimpleName().toString();
      AnnotationValue value = entry.getValue();
      if ("staticName".equals(name)) {
        result.staticName = value;
      } else if ("access".equals(name)) {
        result.access = value;
      }
    }
    return result;
  }

  public AnnotationMirror getAnnotationMirror() {
    return annotationMirror;
  }

  public AnnotationValue getStaticName() {
    return staticName;
  }

  public AnnotationValue getAccess() {
    return access;
  }

  public String getStaticNameValue() {
    String value = AnnotationValueUtil.toString(staticName);
    if (value == null) {
      throw new AptIllegalStateException("staticConstructor");
    }
    return value;
  }

  public boolean isAccessPrivate() {
    VariableElement enumConstant = AnnotationValueUtil.toEnumConstant(access);
    if (enumConstant == null) {
      throw new AptIllegalStateException("access");
    }
    return "PRIVATE".equals(enumConstant.getSimpleName().toString());
  }

  public boolean isAccessNone() {
    VariableElement enumConstant = AnnotationValueUtil.toEnumConstant(access);
    if (enumConstant == null) {
      throw new AptIllegalStateException("access");
    }
    return "NONE".equals(enumConstant.getSimpleName().toString());
  }
}
