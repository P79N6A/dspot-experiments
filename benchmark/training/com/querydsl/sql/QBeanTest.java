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
package com.querydsl.sql;


import com.querydsl.core.types.FactoryExpression;
import com.querydsl.core.types.Projections;
import com.querydsl.sql.domain.Employee;
import com.querydsl.sql.domain.QEmployee;
import org.junit.Assert;
import org.junit.Test;


public class QBeanTest {
    private final QEmployee e = new QEmployee("e");

    @Test
    public void direct_to_managed_type() {
        FactoryExpression<Employee> expr = Projections.bean(Employee.class, e.superiorId);
        Employee e = expr.newInstance(3);
        Assert.assertEquals(Integer.valueOf(3), e.getSuperiorId());
    }

    @Test
    public void direct_to_custom_type() {
        FactoryExpression<Employee> expr = Projections.bean(Employee.class, e.firstname, e.lastname);
        Employee e = expr.newInstance("John", "Smith");
        Assert.assertEquals("John", e.getFirstname());
        Assert.assertEquals("Smith", e.getLastname());
    }

    @Test
    public void alias_to_managed_type() {
        FactoryExpression<Employee> expr = Projections.bean(Employee.class, e.superiorId.as("id"));
        Employee e = expr.newInstance(3);
        Assert.assertEquals(3, e.getId().intValue());
    }

    @Test
    public void alias_to_custom_type() {
        FactoryExpression<Employee> expr = Projections.bean(Employee.class, e.firstname.as("lastname"), e.lastname.as("firstname"));
        Employee e = expr.newInstance("John", "Smith");
        Assert.assertEquals("Smith", e.getFirstname());
        Assert.assertEquals("John", e.getLastname());
    }
}
