/**
 * Copyright 2015, The Querydsl Team (http://www.querydsl.com/team)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.querydsl.apt.domain;


import QConstructorTest_Category.category._super;
import com.querydsl.core.annotations.QueryEntity;
import com.querydsl.core.annotations.QuerySupertype;
import org.junit.Assert;
import org.junit.Test;


public class ConstructorTest {
    @QuerySupertype
    public static class CategorySuperclass {}

    @QueryEntity
    public static class Category<T extends ConstructorTest.Category<T>> extends ConstructorTest.CategorySuperclass {
        public Category(int i) {
        }
    }

    @QueryEntity
    public static class ClassWithConstructor {
        public ClassWithConstructor() {
        }
    }

    @Test
    public void classes_are_available() {
        Assert.assertNotNull(QConstructorTest_CategorySuperclass.class);
        Assert.assertNotNull(QConstructorTest_Category.class);
        Assert.assertNotNull(QConstructorTest_ClassWithConstructor.class);
    }

    @Test
    public void category_super_reference_is_correct() {
        Assert.assertEquals(QConstructorTest_CategorySuperclass.class, _super.getClass());
        Assert.assertEquals(ConstructorTest.Category.class, _super.getType());
    }
}
