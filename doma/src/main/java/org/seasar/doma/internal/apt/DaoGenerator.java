/*
 * Copyright 2004-2010 the Seasar Foundation and the Others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package org.seasar.doma.internal.apt;

import static org.seasar.doma.internal.util.AssertionUtil.*;

import java.io.IOException;
import java.sql.Connection;
import java.util.Iterator;
import java.util.List;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.TypeElement;
import javax.sql.DataSource;

import org.seasar.doma.AnnotationTarget;
import org.seasar.doma.DomaNullPointerException;
import org.seasar.doma.internal.apt.meta.AbstractCreateQueryMeta;
import org.seasar.doma.internal.apt.meta.ArrayCreateQueryMeta;
import org.seasar.doma.internal.apt.meta.AutoBatchModifyQueryMeta;
import org.seasar.doma.internal.apt.meta.AutoFunctionQueryMeta;
import org.seasar.doma.internal.apt.meta.AutoModifyQueryMeta;
import org.seasar.doma.internal.apt.meta.AutoProcedureQueryMeta;
import org.seasar.doma.internal.apt.meta.BasicInOutParameterMeta;
import org.seasar.doma.internal.apt.meta.BasicInParameterMeta;
import org.seasar.doma.internal.apt.meta.BasicListParameterMeta;
import org.seasar.doma.internal.apt.meta.BasicListResultParameterMeta;
import org.seasar.doma.internal.apt.meta.BasicOutParameterMeta;
import org.seasar.doma.internal.apt.meta.BasicResultParameterMeta;
import org.seasar.doma.internal.apt.meta.CallableSqlParameterMeta;
import org.seasar.doma.internal.apt.meta.CallableSqlParameterMetaVisitor;
import org.seasar.doma.internal.apt.meta.DaoMeta;
import org.seasar.doma.internal.apt.meta.DelegateQueryMeta;
import org.seasar.doma.internal.apt.meta.DomainInOutParameterMeta;
import org.seasar.doma.internal.apt.meta.DomainInParameterMeta;
import org.seasar.doma.internal.apt.meta.DomainListParameterMeta;
import org.seasar.doma.internal.apt.meta.DomainListResultParameterMeta;
import org.seasar.doma.internal.apt.meta.DomainOutParameterMeta;
import org.seasar.doma.internal.apt.meta.DomainResultParameterMeta;
import org.seasar.doma.internal.apt.meta.EntityListParameterMeta;
import org.seasar.doma.internal.apt.meta.EntityListResultParameterMeta;
import org.seasar.doma.internal.apt.meta.QueryMeta;
import org.seasar.doma.internal.apt.meta.QueryMetaVisitor;
import org.seasar.doma.internal.apt.meta.QueryParameterMeta;
import org.seasar.doma.internal.apt.meta.QueryReturnMeta;
import org.seasar.doma.internal.apt.meta.SqlFileBatchModifyQueryMeta;
import org.seasar.doma.internal.apt.meta.SqlFileModifyQueryMeta;
import org.seasar.doma.internal.apt.meta.SqlFileScriptQueryMeta;
import org.seasar.doma.internal.apt.meta.SqlFileSelectQueryMeta;
import org.seasar.doma.internal.apt.mirror.AnnotationMirror;
import org.seasar.doma.internal.apt.type.BasicType;
import org.seasar.doma.internal.apt.type.DomainType;
import org.seasar.doma.internal.apt.type.EntityType;
import org.seasar.doma.internal.apt.type.EnumWrapperType;
import org.seasar.doma.internal.apt.type.IterableType;
import org.seasar.doma.internal.apt.type.IterationCallbackType;
import org.seasar.doma.internal.apt.type.SimpleDataTypeVisitor;
import org.seasar.doma.internal.apt.type.WrapperType;
import org.seasar.doma.internal.jdbc.command.BasicIterationHandler;
import org.seasar.doma.internal.jdbc.command.BasicResultListHandler;
import org.seasar.doma.internal.jdbc.command.BasicSingleResultHandler;
import org.seasar.doma.internal.jdbc.command.DomainIterationHandler;
import org.seasar.doma.internal.jdbc.command.DomainResultListHandler;
import org.seasar.doma.internal.jdbc.command.DomainSingleResultHandler;
import org.seasar.doma.internal.jdbc.command.EntityIterationHandler;
import org.seasar.doma.internal.jdbc.command.EntityResultListHandler;
import org.seasar.doma.internal.jdbc.command.EntitySingleResultHandler;
import org.seasar.doma.internal.jdbc.dao.AbstractDao;
import org.seasar.doma.internal.jdbc.sql.BasicInOutParameter;
import org.seasar.doma.internal.jdbc.sql.BasicInParameter;
import org.seasar.doma.internal.jdbc.sql.BasicListParameter;
import org.seasar.doma.internal.jdbc.sql.BasicListResultParameter;
import org.seasar.doma.internal.jdbc.sql.BasicOutParameter;
import org.seasar.doma.internal.jdbc.sql.BasicResultParameter;
import org.seasar.doma.internal.jdbc.sql.DomainInOutParameter;
import org.seasar.doma.internal.jdbc.sql.DomainInParameter;
import org.seasar.doma.internal.jdbc.sql.DomainListParameter;
import org.seasar.doma.internal.jdbc.sql.DomainListResultParameter;
import org.seasar.doma.internal.jdbc.sql.DomainOutParameter;
import org.seasar.doma.internal.jdbc.sql.DomainResultParameter;
import org.seasar.doma.internal.jdbc.sql.EntityListParameter;
import org.seasar.doma.internal.jdbc.sql.EntityListResultParameter;
import org.seasar.doma.internal.jdbc.util.MetaTypeUtil;
import org.seasar.doma.internal.jdbc.util.ScriptFileUtil;
import org.seasar.doma.internal.jdbc.util.SqlFileUtil;
import org.seasar.doma.jdbc.Config;

/**
 * 
 * @author taedium
 * 
 */
public class DaoGenerator extends AbstractGenerator {

    protected final DaoMeta daoMeta;

    public DaoGenerator(ProcessingEnvironment env, TypeElement daoElement,
            DaoMeta daoMeta) throws IOException {
        super(env, daoElement, Options.getDaoPackage(env), Options
                .getDaoSubpackage(env), "", Options.getDaoSuffix(env));
        assertNotNull(daoMeta);
        this.daoMeta = daoMeta;
    }

    @Override
    public void generate() {
        printPackage();
        printClass();
    }

    protected void printPackage() {
        if (!packageName.isEmpty()) {
            iprint("package %1$s;%n", packageName);
            iprint("%n");
        }
    }

    protected void printClass() {
        iprint("/** */%n");
        for (AnnotationMirror annotation : daoMeta
                .getAnnotationMirrors(AnnotationTarget.CLASS)) {
            iprint("@%1$s(%2$s)%n", annotation.getTypeValue(),
                    annotation.getElementsValue());
        }
        printGenerated();
        iprint("public class %1$s extends %2$s implements %3$s {%n",
                simpleName, AbstractDao.class.getName(), daoMeta.getDaoType());
        print("%n");
        indent();
        printValidateVersionStaticInitializer();
        printFields();
        printConstructors();
        printMethods();
        unindent();
        print("}%n");
    }

    protected void printFields() {
    }

    protected void printConstructors() {
        if (daoMeta.hasUserDefinedConfig()) {
            iprint("/** */%n");
            iprint("public %1$s() {%n", simpleName);
            indent();
            iprint("super(new %1$s());%n", daoMeta.getConfigType());
            unindent();
            iprint("}%n");
            print("%n");
            iprint("/**%n");
            iprint(" * @param connection the connection%n");
            iprint(" */%n");
            iprint("public %1$s(%2$s connection) {%n", simpleName,
                    Connection.class.getName());
            indent();
            iprint("super(new %1$s(), connection);%n", daoMeta.getConfigType());
            unindent();
            iprint("}%n");
            print("%n");
            iprint("/**%n");
            iprint(" * @param dataSource the dataSource%n");
            iprint(" */%n");
            iprint("public %1$s(%2$s dataSource) {%n", simpleName,
                    DataSource.class.getName());
            indent();
            iprint("super(new %1$s(), dataSource);%n", daoMeta.getConfigType());
            unindent();
            iprint("}%n");
            print("%n");
        } else {
            iprint("/**%n");
            iprint(" * @param config the config%n");
            iprint(" */%n");
            for (AnnotationMirror annotation : daoMeta
                    .getAnnotationMirrors(AnnotationTarget.CONSTRUCTOR)) {
                iprint("@%1$s(%2$s)%n", annotation.getTypeValue(),
                        annotation.getElementsValue());
            }
            iprint("public %1$s(", simpleName);
            for (AnnotationMirror annotation : daoMeta
                    .getAnnotationMirrors(AnnotationTarget.CONSTRUCTOR_PARAMETER)) {
                print("@%1$s(%2$s) ", annotation.getTypeValue(),
                        annotation.getElementsValue());
            }
            print("%1$s config) {%n", Config.class.getName());
            indent();
            iprint("super(config);%n");
            unindent();
            iprint("}%n");
            print("%n");
        }
    }

    protected void printMethods() {
        MethodBodyGenerator generator = new MethodBodyGenerator();
        for (QueryMeta queryMeta : daoMeta.getQueryMetas()) {
            printMethod(generator, queryMeta);
        }
    }

    protected void printMethod(MethodBodyGenerator generator, QueryMeta m) {
        iprint("@Override%n");
        iprint("public ");
        if (!m.getTypeParameterNames().isEmpty()) {
            print("<");
            for (Iterator<String> it = m.getTypeParameterNames().iterator(); it
                    .hasNext();) {
                print("%1$s", it.next());
                if (it.hasNext()) {
                    print(", ");
                }
            }
            print("> ");
        }
        print("%1$s %2$s(", m.getReturnMeta().getTypeName(), m.getName());
        for (Iterator<QueryParameterMeta> it = m.getParameterMetas().iterator(); it
                .hasNext();) {
            QueryParameterMeta parameterMeta = it.next();
            String parameterTypeName = parameterMeta.getTypeName();
            if (!it.hasNext() && m.isVarArgs()) {
                parameterTypeName = parameterTypeName.replace("[]", "...");
            }
            print("%1$s %2$s", parameterTypeName, parameterMeta.getName());
            if (it.hasNext()) {
                print(", ");
            }
        }
        print(") ");
        if (!m.getThrownTypeNames().isEmpty()) {
            print("throws ");
            for (Iterator<String> it = m.getThrownTypeNames().iterator(); it
                    .hasNext();) {
                print("%1$s", it.next());
                if (it.hasNext()) {
                    print(", ");
                }
            }
            print(" ");
        }
        print("{%n");
        indent();
        m.accept(generator, null);
        unindent();
        iprint("}%n");
        print("%n");
    }

    protected class MethodBodyGenerator implements QueryMetaVisitor<Void, Void> {

        @Override
        public Void visistSqlFileSelectQueryMeta(SqlFileSelectQueryMeta m,
                Void p) {
            printEnteringStatements(m);
            printPrerequisiteStatements(m);

            iprint("%1$s __query = new %1$s();%n", m.getQueryClass().getName());
            iprint("__query.setConfig(config);%n");
            iprint("__query.setSqlFilePath(\"%1$s\");%n",
                    SqlFileUtil.buildPath(daoMeta.getDaoElement()
                            .getQualifiedName().toString(), m.getName()));
            if (m.getSelectOptionsType() != null) {
                iprint("__query.setOptions(%1$s);%n",
                        m.getSelectOptionsParameterName());
            }
            for (Iterator<QueryParameterMeta> it = m.getParameterMetas()
                    .iterator(); it.hasNext();) {
                QueryParameterMeta parameterMeta = it.next();
                if (parameterMeta.isBindable()) {
                    iprint("__query.addParameter(\"%1$s\", %2$s.class, %1$s);%n",
                            parameterMeta.getName(),
                            parameterMeta.getQualifiedName());
                }
            }
            iprint("__query.setCallerClassName(\"%1$s\");%n", qualifiedName);
            iprint("__query.setCallerMethodName(\"%1$s\");%n", m.getName());
            iprint("__query.setResultEnsured(%1$s);%n", m.getEnsureResult());
            iprint("__query.setQueryTimeout(%1$s);%n", m.getQueryTimeout());
            iprint("__query.setMaxRows(%1$s);%n", m.getMaxRows());
            iprint("__query.setFetchSize(%1$s);%n", m.getFetchSize());
            iprint("__query.prepare();%n");
            final QueryReturnMeta resultMeta = m.getReturnMeta();
            final String commandClassName = m.getCommandClass().getName();
            if (m.getIterate()) {
                IterationCallbackType callbackType = m
                        .getIterationCallbackType();
                final String callbackParamName = m
                        .getIterationCallbackPrameterName();
                callbackType
                        .getTargetType()
                        .accept(new SimpleDataTypeVisitor<Void, Void, RuntimeException>() {

                            @Override
                            public Void visitBasicType(BasicType dataType,
                                    Void p) throws RuntimeException {
                                dataType.getWrapperType()
                                        .accept(new SimpleDataTypeVisitor<Void, Void, RuntimeException>() {

                                            @Override
                                            public Void visitEnumWrapperType(
                                                    EnumWrapperType dataType,
                                                    Void p)
                                                    throws RuntimeException {
                                                iprint("%1$s<%2$s> __command = new %1$s<%2$s>(__query, new %3$s<%2$s, %4$s>(new %5$s(%6$s.class), %7$s));%n",
                                                        commandClassName,
                                                        resultMeta
                                                                .getTypeNameAsTypeParameter(),
                                                        BasicIterationHandler.class
                                                                .getName(),
                                                        dataType.getWrappedType()
                                                                .getTypeNameAsTypeParameter(),
                                                        dataType.getTypeName(),
                                                        dataType.getWrappedType()
                                                                .getQualifiedName(),
                                                        callbackParamName);
                                                return null;
                                            }

                                            @Override
                                            public Void visitWrapperType(
                                                    WrapperType dataType, Void p)
                                                    throws RuntimeException {
                                                iprint("%1$s<%2$s> __command = new %1$s<%2$s>(__query, new %3$s<%2$s, %4$s>(new %5$s(), %6$s));%n",
                                                        commandClassName,
                                                        resultMeta
                                                                .getTypeNameAsTypeParameter(),
                                                        BasicIterationHandler.class
                                                                .getName(),
                                                        dataType.getWrappedType()
                                                                .getTypeNameAsTypeParameter(),
                                                        dataType.getTypeName(),
                                                        callbackParamName);
                                                return null;
                                            }

                                        }, null);

                                return null;
                            }

                            @Override
                            public Void visitDomainType(DomainType dataType,
                                    Void p) throws RuntimeException {
                                iprint("%1$s<%2$s> __command = new %1$s<%2$s>(__query, new %3$s<%2$s, %4$s>(%5$s.getSingletonInternal(), %6$s));%n",
                                        commandClassName,
                                        resultMeta.getTypeNameAsTypeParameter(),
                                        DomainIterationHandler.class.getName(),
                                        dataType.getTypeNameAsTypeParameter(),
                                        getMetaTypeName(dataType.getTypeName()),
                                        callbackParamName);
                                return null;
                            }

                            @Override
                            public Void visitEntityType(EntityType dataType,
                                    Void p) throws RuntimeException {
                                iprint("%1$s<%2$s> __command = new %1$s<%2$s>(__query, new %3$s<%2$s, %4$s>(%5$s.getSingletonInternal(), %6$s));%n",
                                        commandClassName,
                                        resultMeta.getTypeNameAsTypeParameter(),
                                        EntityIterationHandler.class.getName(),
                                        dataType.getTypeName(),
                                        getMetaTypeName(dataType.getTypeName()),
                                        callbackParamName);
                                return null;
                            }

                        }, null);
                if ("void".equals(resultMeta.getTypeName())) {
                    iprint("__command.execute();%n");
                    iprint("__query.complete();%n");
                    iprint("exiting(\"%1$s\", \"%2$s\", null);%n",
                            qualifiedName, m.getName());
                } else {
                    iprint("%1$s __result = __command.execute();%n",
                            resultMeta.getTypeName());
                    iprint("__query.complete();%n");
                    iprint("exiting(\"%1$s\", \"%2$s\", __result);%n",
                            qualifiedName, m.getName());
                    iprint("return __result;%n");
                }
            } else {
                m.getReturnMeta()
                        .getDataType()
                        .accept(new SimpleDataTypeVisitor<Void, Void, RuntimeException>() {

                            @Override
                            public Void visitBasicType(
                                    final BasicType basicType, Void p)
                                    throws RuntimeException {
                                basicType
                                        .getWrapperType()
                                        .accept(new SimpleDataTypeVisitor<Void, Void, RuntimeException>() {

                                            @Override
                                            public Void visitEnumWrapperType(
                                                    EnumWrapperType dataType,
                                                    Void p)
                                                    throws RuntimeException {
                                                iprint("%1$s<%2$s> __command = new %1$s<%2$s>(__query, new %3$s<%2$s>(new %4$s(%5$s.class), false));%n",
                                                        commandClassName,
                                                        dataType.getWrappedType()
                                                                .getTypeNameAsTypeParameter(),
                                                        BasicSingleResultHandler.class
                                                                .getName(),
                                                        dataType.getTypeName(),
                                                        dataType.getWrappedType()
                                                                .getQualifiedName());
                                                return null;
                                            }

                                            @Override
                                            public Void visitWrapperType(
                                                    WrapperType dataType, Void p)
                                                    throws RuntimeException {
                                                iprint("%1$s<%2$s> __command = new %1$s<%2$s>(__query, new %3$s<%2$s>(new %4$s(), %5$s));%n",
                                                        commandClassName,
                                                        dataType.getWrappedType()
                                                                .getTypeNameAsTypeParameter(),
                                                        BasicSingleResultHandler.class
                                                                .getName(),
                                                        dataType.getTypeName(),
                                                        basicType.isPrimitive());
                                                return null;
                                            }

                                        }, null);

                                return null;
                            }

                            @Override
                            public Void visitDomainType(DomainType dataType,
                                    Void p) throws RuntimeException {
                                iprint("%1$s<%2$s> __command = new %1$s<%2$s>(__query, new %3$s<%2$s>(%4$s.getSingletonInternal()));%n",
                                        commandClassName, dataType
                                                .getTypeNameAsTypeParameter(),
                                        DomainSingleResultHandler.class
                                                .getName(),
                                        getMetaTypeName(dataType.getTypeName()));
                                return null;
                            }

                            @Override
                            public Void visitEntityType(EntityType dataType,
                                    Void p) throws RuntimeException {
                                iprint("%1$s<%2$s> __command = new %1$s<%2$s>(__query, new %3$s<%2$s>(%4$s.getSingletonInternal()));%n",
                                        commandClassName, dataType
                                                .getTypeName(),
                                        EntitySingleResultHandler.class
                                                .getName(),
                                        getMetaTypeName(dataType.getTypeName()));
                                return null;
                            }

                            @Override
                            public Void visitIterableType(
                                    final IterableType iterableType, Void p)
                                    throws RuntimeException {
                                iterableType
                                        .getElementType()
                                        .accept(new SimpleDataTypeVisitor<Void, Void, RuntimeException>() {

                                            @Override
                                            public Void visitBasicType(
                                                    BasicType dataType, Void p)
                                                    throws RuntimeException {
                                                dataType.getWrapperType()
                                                        .accept(new SimpleDataTypeVisitor<Void, Void, RuntimeException>() {

                                                            @Override
                                                            public Void visitEnumWrapperType(
                                                                    EnumWrapperType dataType,
                                                                    Void p)
                                                                    throws RuntimeException {
                                                                iprint("%1$s<%2$s> __command = new %1$s<%2$s>(__query, new %3$s<%4$s>(new %5$s(%6$s.class)));%n",
                                                                        commandClassName,
                                                                        iterableType
                                                                                .getTypeName(),
                                                                        BasicResultListHandler.class
                                                                                .getName(),
                                                                        dataType.getWrappedType()
                                                                                .getTypeNameAsTypeParameter(),
                                                                        dataType.getTypeName(),
                                                                        dataType.getWrappedType()
                                                                                .getQualifiedName());
                                                                return null;
                                                            }

                                                            @Override
                                                            public Void visitWrapperType(
                                                                    WrapperType dataType,
                                                                    Void p)
                                                                    throws RuntimeException {
                                                                iprint("%1$s<%2$s> __command = new %1$s<%2$s>(__query, new %3$s<%4$s>(new %5$s()));%n",
                                                                        commandClassName,
                                                                        iterableType
                                                                                .getTypeNameAsTypeParameter(),
                                                                        BasicResultListHandler.class
                                                                                .getName(),
                                                                        dataType.getWrappedType()
                                                                                .getTypeNameAsTypeParameter(),
                                                                        dataType.getTypeName());
                                                                return null;
                                                            }

                                                        }, null);

                                                return null;
                                            }

                                            @Override
                                            public Void visitDomainType(
                                                    DomainType dataType, Void p)
                                                    throws RuntimeException {
                                                iprint("%1$s<%2$s> __command = new %1$s<%2$s>(__query, new %3$s<%4$s>(%5$s.getSingletonInternal()));%n",
                                                        commandClassName,
                                                        iterableType
                                                                .getTypeName(),
                                                        DomainResultListHandler.class
                                                                .getName(),
                                                        dataType.getTypeNameAsTypeParameter(),
                                                        getMetaTypeName(dataType
                                                                .getTypeName()));
                                                return null;
                                            }

                                            @Override
                                            public Void visitEntityType(
                                                    EntityType dataType, Void p)
                                                    throws RuntimeException {
                                                iprint("%1$s<%2$s> __command = new %1$s<%2$s>(__query, new %3$s<%4$s>(%5$s.getSingletonInternal()));%n",
                                                        commandClassName,
                                                        iterableType
                                                                .getTypeName(),
                                                        EntityResultListHandler.class
                                                                .getName(),
                                                        dataType.getTypeName(),
                                                        getMetaTypeName(dataType
                                                                .getTypeName()));
                                                return null;
                                            }

                                        }, null);
                                return null;
                            }

                        }, null);
                iprint("%1$s __result = __command.execute();%n",
                        resultMeta.getTypeName());
                iprint("__query.complete();%n");
                iprint("exiting(\"%1$s\", \"%2$s\", __result);%n",
                        qualifiedName, m.getName());
                iprint("return __result;%n");
            }

            printThrowingStatements(m);
            return null;
        }

        @Override
        public Void visistSqlFileScriptQueryMeta(SqlFileScriptQueryMeta m,
                Void p) {
            printEnteringStatements(m);
            printPrerequisiteStatements(m);

            iprint("%1$s __query = new %1$s();%n", m.getQueryClass().getName());
            iprint("__query.setConfig(config);%n");
            iprint("__query.setScriptFilePath(\"%1$s\");%n",
                    ScriptFileUtil.buildPath(daoMeta.getDaoElement()
                            .getQualifiedName().toString(), m.getName()));
            iprint("__query.setCallerClassName(\"%1$s\");%n", qualifiedName);
            iprint("__query.setCallerMethodName(\"%1$s\");%n", m.getName());
            iprint("__query.setBlockDelimiter(\"%1$s\");%n",
                    m.getBlockDelimiter());
            iprint("__query.setHaltOnError(%1$s);%n", m.getHaltOnError());
            iprint("__query.prepare();%n");
            iprint("%1$s __command = new %1$s(__query);%n", m.getCommandClass()
                    .getName());
            iprint("__command.execute();%n");
            iprint("__query.complete();%n");
            iprint("exiting(\"%1$s\", \"%2$s\", null);%n", qualifiedName,
                    m.getName());

            printThrowingStatements(m);
            return null;
        }

        @Override
        public Void visistAutoModifyQueryMeta(AutoModifyQueryMeta m, Void p) {
            printEnteringStatements(m);
            printPrerequisiteStatements(m);

            iprint("%1$s<%2$s> __query = new %1$s<%2$s>(%3$s.getSingletonInternal());%n",
                    m.getQueryClass().getName(), m.getEntityType()
                            .getTypeNameAsTypeParameter(), getMetaTypeName(m
                            .getEntityType().getTypeNameAsTypeParameter()));
            iprint("__query.setConfig(config);%n");
            iprint("__query.setEntity(%1$s);%n", m.getEntityParameterName());
            iprint("__query.setCallerClassName(\"%1$s\");%n", qualifiedName);
            iprint("__query.setCallerMethodName(\"%1$s\");%n", m.getName());
            iprint("__query.setQueryTimeout(%1$s);%n", m.getQueryTimeout());

            Boolean excludeNull = m.getExcludeNull();
            if (excludeNull != null) {
                iprint("__query.setNullExcluded(%1$s);%n", excludeNull);
            }

            Boolean includeVersion = m.getIncludeVersion();
            if (includeVersion != null) {
                iprint("__query.setVersionIncluded(%1$s);%n", includeVersion);
            }

            Boolean ignoreVersion = m.getIgnoreVersion();
            if (ignoreVersion != null) {
                iprint("__query.setVersionIgnored(%1$s);%n", ignoreVersion);
            }

            List<String> include = m.getInclude();
            if (include != null) {
                iprint("__query.setIncludedPropertyNames(%1$s);%n",
                        toCSVFormat(include));
            }

            List<String> exclude = m.getExclude();
            if (exclude != null) {
                iprint("__query.setExcludedPropertyNames(%1$s);%n",
                        toCSVFormat(m.getExclude()));
            }

            Boolean includeUnchanged = m.getIncludeUnchanged();
            if (includeUnchanged != null) {
                iprint("__query.setUnchangedPropertyIncluded(%1$s);%n",
                        includeUnchanged);
            }

            Boolean suppressOptimisticLockException = m
                    .getSuppressOptimisticLockException();
            if (suppressOptimisticLockException != null) {
                iprint("__query.setOptimisticLockExceptionSuppressed(%1$s);%n",
                        suppressOptimisticLockException);
            }

            iprint("__query.prepare();%n");
            iprint("%1$s __command = new %1$s(__query);%n", m.getCommandClass()
                    .getName());
            iprint("%1$s __result = __command.execute();%n", m.getReturnMeta()
                    .getTypeName());
            iprint("__query.complete();%n");
            iprint("exiting(\"%1$s\", \"%2$s\", __result);%n", qualifiedName,
                    m.getName());
            iprint("return __result;%n");

            printThrowingStatements(m);
            return null;
        }

        @Override
        public Void visistSqlFileModifyQueryMeta(SqlFileModifyQueryMeta m,
                Void p) {
            printEnteringStatements(m);
            printPrerequisiteStatements(m);

            iprint("%1$s __query = new %1$s();%n", m.getQueryClass().getName());
            iprint("__query.setConfig(config);%n");
            iprint("__query.setSqlFilePath(\"%1$s\");%n",
                    SqlFileUtil.buildPath(daoMeta.getDaoElement()
                            .getQualifiedName().toString(), m.getName()));
            for (Iterator<QueryParameterMeta> it = m.getParameterMetas()
                    .iterator(); it.hasNext();) {
                QueryParameterMeta parameterMeta = it.next();
                if (parameterMeta.isBindable()) {
                    iprint("__query.addParameter(\"%1$s\", %2$s.class, %1$s);%n",
                            parameterMeta.getName(),
                            parameterMeta.getQualifiedName());
                }
            }
            iprint("__query.setCallerClassName(\"%1$s\");%n", qualifiedName);
            iprint("__query.setCallerMethodName(\"%1$s\");%n", m.getName());
            iprint("__query.setQueryTimeout(%1$s);%n", m.getQueryTimeout());

            if (m.getEntityParameterName() != null && m.getEntityType() != null) {
                iprint("__query.setEntityAndEntityType(%1$s, %2$s.getSingletonInternal());%n",
                        m.getEntityParameterName(), getMetaTypeName(m
                                .getEntityType().getTypeNameAsTypeParameter()));
            }

            Boolean includeVersion = m.getIncludeVersion();
            if (includeVersion != null) {
                iprint("__query.setVersionIncluded(%1$s);%n", includeVersion);
            }

            Boolean ignoreVersion = m.getIgnoreVersion();
            if (ignoreVersion != null) {
                iprint("__query.setVersionIgnored(%1$s);%n", ignoreVersion);
            }

            Boolean suppressOptimisticLockException = m
                    .getSuppressOptimisticLockException();
            if (suppressOptimisticLockException != null) {
                iprint("__query.setOptimisticLockExceptionSuppressed(%1$s);%n",
                        suppressOptimisticLockException);
            }

            iprint("__query.prepare();%n");
            iprint("%1$s __command = new %1$s(__query);%n", m.getCommandClass()
                    .getName());
            iprint("%1$s __result = __command.execute();%n", m.getReturnMeta()
                    .getTypeName());
            iprint("__query.complete();%n");
            iprint("exiting(\"%1$s\", \"%2$s\", __result);%n", qualifiedName,
                    m.getName());
            iprint("return __result;%n");

            printThrowingStatements(m);
            return null;
        }

        @Override
        public Void visitAutoBatchModifyQueryMeta(AutoBatchModifyQueryMeta m,
                Void p) {
            printEnteringStatements(m);
            printPrerequisiteStatements(m);

            iprint("%1$s<%2$s> __query = new %1$s<%2$s>(%3$s.getSingletonInternal());%n",
                    m.getQueryClass().getName(), m.getEntityType()
                            .getTypeNameAsTypeParameter(), getMetaTypeName(m
                            .getEntityType().getTypeNameAsTypeParameter()));
            iprint("__query.setConfig(config);%n");
            iprint("__query.setEntities(%1$s);%n", m.getEntitiesParameterName());
            iprint("__query.setCallerClassName(\"%1$s\");%n", qualifiedName);
            iprint("__query.setCallerMethodName(\"%1$s\");%n", m.getName());
            iprint("__query.setQueryTimeout(%1$s);%n", m.getQueryTimeout());

            Boolean includeVersion = m.getIncludeVersion();
            if (includeVersion != null) {
                iprint("__query.setVersionIncluded(%1$s);%n", includeVersion);
            }

            Boolean ignoreVersion = m.getIgnoreVersion();
            if (ignoreVersion != null) {
                iprint("__query.setVersionIgnored(%1$s);%n", ignoreVersion);
            }

            List<String> include = m.getInclude();
            if (include != null) {
                iprint("__query.setIncludedPropertyNames(%1$s);%n",
                        toCSVFormat(include));
            }

            List<String> exclude = m.getExclude();
            if (exclude != null) {
                iprint("__query.setExcludedPropertyNames(%1$s);%n",
                        toCSVFormat(exclude));
            }

            Boolean suppressOptimisticLockException = m
                    .getSuppressOptimisticLockException();
            if (suppressOptimisticLockException != null) {
                iprint("__query.setOptimisticLockExceptionSuppressed(%1$s);%n",
                        suppressOptimisticLockException);
            }

            iprint("__query.prepare();%n");
            iprint("%1$s __command = new %1$s(__query);%n", m.getCommandClass()
                    .getName());
            iprint("%1$s __result = __command.execute();%n", m.getReturnMeta()
                    .getTypeName());
            iprint("__query.complete();%n");
            iprint("exiting(\"%1$s\", \"%2$s\", __result);%n", qualifiedName,
                    m.getName());
            iprint("return __result;%n");

            printThrowingStatements(m);
            return null;
        }

        @Override
        public Void visitSqlFileBatchModifyQueryMeta(
                SqlFileBatchModifyQueryMeta m, Void p) {
            printEnteringStatements(m);
            printPrerequisiteStatements(m);

            iprint("%1$s<%2$s> __query = new %1$s<%2$s>(%3$s.class);%n", m
                    .getQueryClass().getName(), m.getElementType()
                    .getTypeNameAsTypeParameter(), m.getElementType()
                    .getQualifiedName());
            iprint("__query.setConfig(config);%n");
            iprint("__query.setElements(%1$s);%n", m.getElementsParameterName());
            iprint("__query.setSqlFilePath(\"%1$s\");%n",
                    SqlFileUtil.buildPath(daoMeta.getDaoElement()
                            .getQualifiedName().toString(), m.getName()));
            iprint("__query.setParameterName(\"%1$s\");%n",
                    m.getElementsParameterName());
            iprint("__query.setCallerClassName(\"%1$s\");%n", qualifiedName);
            iprint("__query.setCallerMethodName(\"%1$s\");%n", m.getName());
            iprint("__query.setQueryTimeout(%1$s);%n", m.getQueryTimeout());

            if (m.getEntityType() != null) {
                iprint("__query.setEntityType(%1$s.getSingletonInternal());%n",
                        getMetaTypeName(m.getEntityType()
                                .getTypeNameAsTypeParameter()));
            }

            Boolean includeVersion = m.getIncludeVersion();
            if (includeVersion != null) {
                iprint("__query.setVersionIncluded(%1$s);%n", includeVersion);
            }

            Boolean ignoreVersion = m.getIgnoreVersion();
            if (ignoreVersion != null) {
                iprint("__query.setVersionIgnored(%1$s);%n", ignoreVersion);
            }

            Boolean suppressOptimisticLockException = m
                    .getSuppressOptimisticLockException();
            if (suppressOptimisticLockException != null) {
                iprint("__query.setOptimisticLockExceptionSuppressed(%1$s);%n",
                        suppressOptimisticLockException);
            }

            iprint("__query.prepare();%n");
            iprint("%1$s __command = new %1$s(__query);%n", m.getCommandClass()
                    .getName());
            iprint("%1$s __result = __command.execute();%n", m.getReturnMeta()
                    .getTypeName());
            iprint("__query.complete();%n");
            iprint("exiting(\"%1$s\", \"%2$s\", __result);%n", qualifiedName,
                    m.getName());
            iprint("return __result;%n");

            printThrowingStatements(m);
            return null;
        }

        @Override
        public Void visitAutoFunctionQueryMeta(AutoFunctionQueryMeta m, Void p) {
            printEnteringStatements(m);
            printPrerequisiteStatements(m);

            QueryReturnMeta resultMeta = m.getReturnMeta();
            iprint("%1$s<%2$s> __query = new %1$s<%2$s>();%n", m
                    .getQueryClass().getName(),
                    resultMeta.getTypeNameAsTypeParameter());
            iprint("__query.setConfig(config);%n");
            iprint("__query.setFunctionName(\"%1$s\");%n", m.getFunctionName());
            CallableSqlParameterStatementGenerator parameterGenerator = new CallableSqlParameterStatementGenerator();
            m.getResultParameterMeta().accept(parameterGenerator, p);
            for (CallableSqlParameterMeta parameterMeta : m
                    .getCallableSqlParameterMetas()) {
                parameterMeta.accept(parameterGenerator, p);
            }
            iprint("__query.setCallerClassName(\"%1$s\");%n", qualifiedName);
            iprint("__query.setCallerMethodName(\"%1$s\");%n", m.getName());
            iprint("__query.setQueryTimeout(%1$s);%n", m.getQueryTimeout());
            iprint("__query.prepare();%n");
            iprint("%1$s<%2$s> __command = new %1$s<%2$s>(__query);%n", m
                    .getCommandClass().getName(),
                    resultMeta.getTypeNameAsTypeParameter());
            iprint("%1$s __result = __command.execute();%n",
                    resultMeta.getTypeName());
            iprint("__query.complete();%n");
            iprint("exiting(\"%1$s\", \"%2$s\", __result);%n", qualifiedName,
                    m.getName());
            iprint("return __result;%n");

            printThrowingStatements(m);
            return null;
        }

        @Override
        public Void visitAutoProcedureQueryMeta(AutoProcedureQueryMeta m, Void p) {
            printEnteringStatements(m);
            printPrerequisiteStatements(m);

            iprint("%1$s __query = new %1$s();%n", m.getQueryClass().getName());
            iprint("__query.setConfig(config);%n");
            iprint("__query.setProcedureName(\"%1$s\");%n",
                    m.getProcedureName());
            CallableSqlParameterStatementGenerator parameterGenerator = new CallableSqlParameterStatementGenerator();
            for (CallableSqlParameterMeta parameterMeta : m
                    .getCallableSqlParameterMetas()) {
                parameterMeta.accept(parameterGenerator, p);
            }
            iprint("__query.setCallerClassName(\"%1$s\");%n", qualifiedName);
            iprint("__query.setCallerMethodName(\"%1$s\");%n", m.getName());
            iprint("__query.setQueryTimeout(%1$s);%n", m.getQueryTimeout());
            iprint("__query.prepare();%n");
            iprint("%1$s __command = new %1$s(__query);%n", m.getCommandClass()
                    .getName());
            iprint("__command.execute();%n");
            iprint("__query.complete();%n");
            iprint("exiting(\"%1$s\", \"%2$s\", null);%n", qualifiedName,
                    m.getName());

            printThrowingStatements(m);
            return null;
        }

        @Override
        public Void visitAbstractCreateQueryMeta(AbstractCreateQueryMeta m,
                Void p) {
            printEnteringStatements(m);
            printPrerequisiteStatements(m);

            QueryReturnMeta resultMeta = m.getReturnMeta();
            iprint("%1$s __query = new %1$s();%n", m.getQueryClass().getName(),
                    resultMeta.getTypeName());
            iprint("__query.setConfig(config);%n");
            iprint("__query.setCallerClassName(\"%1$s\");%n", qualifiedName);
            iprint("__query.setCallerMethodName(\"%1$s\");%n", m.getName());
            iprint("__query.prepare();%n");
            iprint("%1$s<%2$s> __command = new %1$s<%2$s>(__query);%n", m
                    .getCommandClass().getName(), resultMeta.getTypeName());
            iprint("%1$s __result = __command.execute();%n",
                    resultMeta.getTypeName());
            iprint("__query.complete();%n");
            iprint("exiting(\"%1$s\", \"%2$s\", __result);%n", qualifiedName,
                    m.getName());
            iprint("return __result;%n");

            printThrowingStatements(m);
            return null;
        }

        @Override
        public Void visitArrayCreateQueryMeta(ArrayCreateQueryMeta m, Void p) {
            printArrayCreateEnteringStatements(m);
            printPrerequisiteStatements(m);

            QueryReturnMeta resultMeta = m.getReturnMeta();
            iprint("%1$s __query = new %1$s();%n", m.getQueryClass().getName());
            iprint("__query.setConfig(config);%n");
            iprint("__query.setCallerClassName(\"%1$s\");%n", qualifiedName);
            iprint("__query.setCallerMethodName(\"%1$s\");%n", m.getName());
            iprint("__query.setTypeName(\"%1$s\");%n", m.getArrayTypeName());
            iprint("__query.setElements(%1$s);%n", m.getParameterName());
            iprint("__query.prepare();%n");
            iprint("%1$s<%2$s> __command = new %1$s<%2$s>(__query);%n", m
                    .getCommandClass().getName(),
                    resultMeta.getTypeNameAsTypeParameter());
            iprint("%1$s __result = __command.execute();%n",
                    resultMeta.getTypeName());
            iprint("__query.complete();%n");
            iprint("exiting(\"%1$s\", \"%2$s\", __result);%n", qualifiedName,
                    m.getName());
            iprint("return __result;%n");

            printThrowingStatements(m);
            return null;
        }

        @Override
        public Void visitDelegateQueryMeta(DelegateQueryMeta m, Void p) {
            printEnteringStatements(m);

            iprint("%1$s __delegate = new %1$s(config", m.getTo());
            if (m.isDaoAware()) {
                print(", this);%n");
            } else {
                print(");%n");
            }
            QueryReturnMeta resultMeta = m.getReturnMeta();
            if ("void".equals(resultMeta.getTypeName())) {
                iprint("Object __result = null;%n");
                iprint("");
            } else {
                iprint("%1$s __result = ", resultMeta.getTypeName());
            }
            print("__delegate.%1$s(", m.getName());
            for (Iterator<QueryParameterMeta> it = m.getParameterMetas()
                    .iterator(); it.hasNext();) {
                QueryParameterMeta parameterMeta = it.next();
                print("%1$s", parameterMeta.getName());
                if (it.hasNext()) {
                    print(", ");
                }
            }
            print(");%n");
            iprint("exiting(\"%1$s\", \"%2$s\", __result);%n", qualifiedName,
                    m.getName());
            if (!"void".equals(resultMeta.getTypeName())) {
                iprint("return __result;%n");
            }

            printThrowingStatements(m);
            return null;
        }

        protected void printEnteringStatements(QueryMeta m) {
            iprint("entering(\"%1$s\", \"%2$s\"", qualifiedName, m.getName());
            for (Iterator<QueryParameterMeta> it = m.getParameterMetas()
                    .iterator(); it.hasNext();) {
                QueryParameterMeta parameterMeta = it.next();
                print(", %1$s", parameterMeta.getName());
            }
            print(");%n");
            iprint("try {%n");
            indent();
        }

        protected void printArrayCreateEnteringStatements(ArrayCreateQueryMeta m) {
            iprint("entering(\"%1$s\", \"%2$s\", (Object)%3$s);%n",
                    qualifiedName, m.getName(), m.getParameterName());
            iprint("try {%n");
            indent();
        }

        protected void printThrowingStatements(QueryMeta m) {
            unindent();
            iprint("} catch (%1$s __e) {%n", RuntimeException.class.getName());
            indent();
            iprint("throwing(\"%1$s\", \"%2$s\", __e);%n", qualifiedName,
                    m.getName());
            iprint("throw __e;%n");
            unindent();
            iprint("}%n");
        }

        protected void printPrerequisiteStatements(QueryMeta m) {
            for (Iterator<QueryParameterMeta> it = m.getParameterMetas()
                    .iterator(); it.hasNext();) {
                QueryParameterMeta parameterMeta = it.next();
                if (parameterMeta.isNullable()) {
                    continue;
                }
                String paramName = parameterMeta.getName();
                iprint("if (%1$s == null) {%n", paramName);
                iprint("    throw new %1$s(\"%2$s\");%n",
                        DomaNullPointerException.class.getName(), paramName);
                iprint("}%n");
            }
        }
    }

    protected class CallableSqlParameterStatementGenerator implements
            CallableSqlParameterMetaVisitor<Void, Void> {

        @Override
        public Void visitBasicListParameterMeta(final BasicListParameterMeta m,
                Void p) {
            BasicType basicType = m.getBasicType();
            basicType.getWrapperType().accept(
                    new SimpleDataTypeVisitor<Void, Void, RuntimeException>() {

                        @Override
                        public Void visitEnumWrapperType(
                                EnumWrapperType dataType, Void p)
                                throws RuntimeException {
                            iprint("__query.addParameter(new %1$s<%2$s>(new %3$s(%4$s.class), %5$s, \"%5$s\"));%n",
                                    BasicListParameter.class.getName(),
                                    dataType.getWrappedType()
                                            .getTypeNameAsTypeParameter(),
                                    dataType.getTypeName(), dataType
                                            .getWrappedType()
                                            .getQualifiedName(), m.getName());
                            return null;
                        }

                        @Override
                        public Void visitWrapperType(WrapperType dataType,
                                Void p) throws RuntimeException {
                            iprint("__query.addParameter(new %1$s<%2$s>(new %3$s(), %4$s, \"%4$s\"));%n",
                                    BasicListParameter.class.getName(),
                                    dataType.getWrappedType()
                                            .getTypeNameAsTypeParameter(),
                                    dataType.getTypeName(), m.getName());
                            return null;
                        }

                    }, null);
            return null;
        }

        @Override
        public Void visitDomainListParameterMeta(DomainListParameterMeta m,
                Void p) {
            DomainType domainType = m.getDomainType();
            BasicType basicType = domainType.getBasicType();
            iprint("__query.addParameter(new %1$s<%2$s, %3$s>(%4$s.getSingletonInternal(), %5$s, \"%5$s\"));%n",
                    DomainListParameter.class.getName(),
                    basicType.getTypeName(), domainType.getTypeName(),
                    getMetaTypeName(domainType.getTypeName()), m.getName());
            return null;
        }

        @Override
        public Void visitEntityListParameterMeta(EntityListParameterMeta m,
                Void p) {
            EntityType entityType = m.getEntityType();
            iprint("__query.addParameter(new %1$s<%2$s>(%3$s.getSingletonInternal(), %4$s, \"%4$s\"));%n",
                    EntityListParameter.class.getName(),
                    entityType.getTypeName(),
                    getMetaTypeName(entityType.getTypeName()), m.getName());
            return null;
        }

        @Override
        public Void visitBasicInOutParameterMeta(
                final BasicInOutParameterMeta m, Void p) {
            BasicType basicType = m.getBasicType();
            basicType.getWrapperType().accept(
                    new SimpleDataTypeVisitor<Void, Void, RuntimeException>() {

                        @Override
                        public Void visitEnumWrapperType(
                                EnumWrapperType dataType, Void p)
                                throws RuntimeException {
                            iprint("__query.addParameter(new %1$s<%2$s>(new %3$s(%4$s.class), %5$s));%n",
                                    BasicInOutParameter.class.getName(),
                                    dataType.getWrappedType()
                                            .getTypeNameAsTypeParameter(),
                                    dataType.getTypeName(), dataType
                                            .getWrappedType()
                                            .getQualifiedName(), m.getName());
                            return null;
                        }

                        @Override
                        public Void visitWrapperType(WrapperType dataType,
                                Void p) throws RuntimeException {
                            iprint("__query.addParameter(new %1$s<%2$s>(new %3$s(), %4$s));%n",
                                    BasicInOutParameter.class.getName(),
                                    dataType.getWrappedType()
                                            .getTypeNameAsTypeParameter(),
                                    dataType.getTypeName(), m.getName());
                            return null;
                        }

                    }, null);
            return null;
        }

        @Override
        public Void visitDomainInOutParameterMeta(DomainInOutParameterMeta m,
                Void p) {
            DomainType domainType = m.getDomainType();
            BasicType basicType = domainType.getBasicType();
            iprint("__query.addParameter(new %1$s<%2$s, %3$s>(%4$s.getSingletonInternal(), %5$s));%n",
                    DomainInOutParameter.class.getName(),
                    basicType.getTypeNameAsTypeParameter(),
                    domainType.getTypeName(),
                    getMetaTypeName(domainType.getTypeName()), m.getName());
            return null;
        }

        @Override
        public Void visitBasicOutParameterMeta(final BasicOutParameterMeta m,
                Void p) {
            BasicType basicType = m.getBasicType();
            basicType.getWrapperType().accept(
                    new SimpleDataTypeVisitor<Void, Void, RuntimeException>() {

                        @Override
                        public Void visitEnumWrapperType(
                                EnumWrapperType dataType, Void p)
                                throws RuntimeException {
                            iprint("__query.addParameter(new %1$s<%2$s>(new %3$s(%4$s.class), %5$s));%n",
                                    BasicOutParameter.class.getName(), dataType
                                            .getWrappedType()
                                            .getTypeNameAsTypeParameter(),
                                    dataType.getTypeName(), dataType
                                            .getWrappedType()
                                            .getQualifiedName(), m.getName());
                            return null;
                        }

                        @Override
                        public Void visitWrapperType(WrapperType dataType,
                                Void p) throws RuntimeException {
                            iprint("__query.addParameter(new %1$s<%2$s>(new %3$s(), %4$s));%n",
                                    BasicOutParameter.class.getName(), dataType
                                            .getWrappedType()
                                            .getTypeNameAsTypeParameter(),
                                    dataType.getTypeName(), m.getName());
                            return null;
                        }

                    }, null);

            return null;
        }

        @Override
        public Void visitDomainOutParameterMeta(DomainOutParameterMeta m, Void p) {
            DomainType domainType = m.getDomainType();
            BasicType basicType = domainType.getBasicType();
            iprint("__query.addParameter(new %1$s<%2$s, %3$s>(%4$s.getSingletonInternal(), %5$s));%n",
                    DomainOutParameter.class.getName(),
                    basicType.getTypeName(), domainType.getTypeName(),
                    getMetaTypeName(domainType.getTypeName()), m.getName());
            return null;
        }

        @Override
        public Void visitBasicInParameterMeta(final BasicInParameterMeta m,
                Void p) {
            BasicType basicType = m.getBasicType();
            basicType.getWrapperType().accept(
                    new SimpleDataTypeVisitor<Void, Void, RuntimeException>() {

                        @Override
                        public Void visitEnumWrapperType(
                                EnumWrapperType dataType, Void p)
                                throws RuntimeException {
                            iprint("__query.addParameter(new %1$s(new %2$s(%3$s.class, %4$s)));%n",
                                    BasicInParameter.class.getName(), dataType
                                            .getTypeName(), dataType
                                            .getWrappedType().getTypeName(), m
                                            .getName());
                            return null;
                        }

                        @Override
                        public Void visitWrapperType(WrapperType dataType,
                                Void p) throws RuntimeException {
                            iprint("__query.addParameter(new %1$s(new %2$s(%3$s)));%n",
                                    BasicInParameter.class.getName(),
                                    dataType.getTypeName(), m.getName());
                            return null;
                        }

                    }, null);

            return null;
        }

        @Override
        public Void visitDomainInParameterMeta(DomainInParameterMeta m, Void p) {
            DomainType domainType = m.getDomainType();
            BasicType basicType = domainType.getBasicType();
            iprint("__query.addParameter(new %1$s<%2$s, %3$s>(%4$s.getSingletonInternal(), %5$s));%n",
                    DomainInParameter.class.getName(), basicType.getTypeName(),
                    domainType.getTypeName(),
                    getMetaTypeName(domainType.getTypeName()), m.getName());
            return null;
        }

        @Override
        public Void visitBasicListResultParameterMeta(
                BasicListResultParameterMeta m, Void p) {
            BasicType basicType = m.getBasicType();
            basicType.getWrapperType().accept(
                    new SimpleDataTypeVisitor<Void, Void, RuntimeException>() {

                        @Override
                        public Void visitEnumWrapperType(
                                EnumWrapperType dataType, Void p)
                                throws RuntimeException {
                            iprint("__query.setResultParameter(new %1$s<%2$s>(new %3$s(%4$s.class)));%n",
                                    BasicListResultParameter.class.getName(),
                                    dataType.getWrappedType()
                                            .getTypeNameAsTypeParameter(),
                                    dataType.getTypeName(), dataType
                                            .getWrappedType()
                                            .getQualifiedName());
                            return null;
                        }

                        @Override
                        public Void visitWrapperType(WrapperType dataType,
                                Void p) throws RuntimeException {
                            iprint("__query.setResultParameter(new %1$s<%2$s>(new %3$s()));%n",
                                    BasicListResultParameter.class.getName(),
                                    dataType.getWrappedType()
                                            .getTypeNameAsTypeParameter(),
                                    dataType.getTypeName());
                            return null;
                        }

                    }, null);
            return null;
        }

        @Override
        public Void visitDomainListResultParameterMeta(
                DomainListResultParameterMeta m, Void p) {
            DomainType domainType = m.getDomainType();
            BasicType basicType = domainType.getBasicType();
            iprint("__query.setResultParameter(new %1$s<%2$s, %3$s>(%4$s.getSingletonInternal()));%n",
                    DomainListResultParameter.class.getName(),
                    basicType.getTypeName(), domainType.getTypeName(),
                    getMetaTypeName(domainType.getTypeName()));
            return null;
        }

        @Override
        public Void visitEntityListResultParameterMeta(
                EntityListResultParameterMeta m, Void p) {
            EntityType entityType = m.getEntityType();
            iprint("__query.setResultParameter(new %1$s<%2$s>(%3$s.getSingletonInternal()));%n",
                    EntityListResultParameter.class.getName(),
                    entityType.getTypeName(),
                    getMetaTypeName(entityType.getTypeName()));
            return null;
        }

        @Override
        public Void visitBasicResultParameterMeta(BasicResultParameterMeta m,
                Void p) {
            final BasicType basicType = m.getBasicType();
            basicType.getWrapperType().accept(
                    new SimpleDataTypeVisitor<Void, Void, RuntimeException>() {

                        @Override
                        public Void visitEnumWrapperType(
                                EnumWrapperType dataType, Void p)
                                throws RuntimeException {
                            iprint("__query.setResultParameter(new %1$s<%2$s>(new %3$s(%4$s.class), false));%n",
                                    BasicResultParameter.class.getName(),
                                    dataType.getWrappedType()
                                            .getTypeNameAsTypeParameter(),
                                    dataType.getTypeName(), dataType
                                            .getWrappedType()
                                            .getQualifiedName());
                            return null;
                        }

                        @Override
                        public Void visitWrapperType(WrapperType dataType,
                                Void p) throws RuntimeException {
                            iprint("__query.setResultParameter(new %1$s<%2$s>(new %3$s(), %4$s));%n",
                                    BasicResultParameter.class.getName(),
                                    dataType.getWrappedType()
                                            .getTypeNameAsTypeParameter(),
                                    dataType.getTypeName(), basicType
                                            .isPrimitive());
                            return null;
                        }

                    }, null);
            return null;
        }

        @Override
        public Void visitDomainResultParameterMeta(DomainResultParameterMeta m,
                Void p) {
            DomainType domainType = m.getDomainType();
            BasicType basicType = domainType.getBasicType();
            iprint("__query.setResultParameter(new %1$s<%2$s, %3$s>(%4$s.getSingletonInternal()));%n",
                    DomainResultParameter.class.getName(),
                    basicType.getTypeName(), domainType.getTypeName(),
                    getMetaTypeName(domainType.getTypeName()));
            return null;
        }
    }

    public String toCSVFormat(List<String> values) {
        final StringBuilder buf = new StringBuilder();
        if (values.size() > 0) {
            for (String value : values) {
                buf.append("\"");
                buf.append(value);
                buf.append("\", ");
            }
            buf.setLength(buf.length() - 2);
        }
        return buf.toString();
    }

    protected String getMetaTypeName(String qualifiedName) {
        return MetaTypeUtil.getMetaTypeName(qualifiedName);
    }

}
