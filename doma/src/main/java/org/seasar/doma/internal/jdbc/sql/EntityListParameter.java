/*
 * Copyright 2004-2009 the Seasar Foundation and the Others.
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
package org.seasar.doma.internal.jdbc.sql;

import static org.seasar.doma.internal.util.AssertionUtil.*;

import java.util.List;

import org.seasar.doma.jdbc.entity.EntityMeta;
import org.seasar.doma.jdbc.entity.EntityMetaFactory;

/**
 * @author taedium
 * 
 */
public class EntityListParameter<E> implements ListParameter<EntityMeta<E>> {

    protected final EntityMetaFactory<E> entityMetaFactory;

    protected final List<E> entities;

    public EntityListParameter(EntityMetaFactory<E> entityMetaFactory,
            List<E> entities) {
        assertNotNull(entityMetaFactory, entities);
        this.entityMetaFactory = entityMetaFactory;
        this.entities = entities;
    }

    @Override
    public EntityMeta<E> getElementHolder() {
        return entityMetaFactory.createEntityMeta();
    }

    @Override
    public void putElementHolder(EntityMeta<E> entityMeta) {
        entities.add(entityMeta.getEntity());
    }

    @Override
    public <R, P, TH extends Throwable> R accept(
            CallableSqlParameterVisitor<R, P, TH> visitor, P p) throws TH {
        return visitor.visitEntityListParameter(this, p);
    }

}
