package com.querydsl.apt.domain;


import QAbstractProperties3Test_CompoundContainer.compoundContainer.containable.compound.name;
import com.querydsl.core.annotations.PropertyType;
import com.querydsl.core.annotations.QueryType;
import java.io.Serializable;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.junit.Ignore;
import org.junit.Test;

import static FetchType.EAGER;
import static GenerationType.AUTO;
import static InheritanceType.TABLE_PER_CLASS;


@Ignore
public class AbstractProperties3Test {
    @MappedSuperclass
    public static class BaseEntity {}

    @Entity
    public static class Compound extends AbstractProperties3Test.BaseEntity {
        String name;
    }

    @Entity
    @Inheritance(strategy = TABLE_PER_CLASS)
    public abstract static class Containable extends AbstractProperties3Test.BaseEntity implements Serializable {
        @Id
        @GeneratedValue(strategy = AUTO, generator = "containable_seq_gen")
        @SequenceGenerator(name = "containable_seq_gen", sequenceName = "seq_containable")
        @Column(name = "id")
        Long id;

        @QueryType(PropertyType.ENTITY)
        public abstract AbstractProperties3Test.Compound getCompound();
    }

    @MappedSuperclass
    @Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
    @Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL)
    public abstract static class CompoundContainer extends AbstractProperties3Test.BaseEntity implements Serializable {
        @Id
        @GeneratedValue(strategy = GenerationType.AUTO, generator = "compound_container_seq_gen")
        @SequenceGenerator(name = "compound_container_seq_gen", sequenceName = "seq_compound_container", allocationSize = 1000)
        @Column(name = "compound_container_id")
        Long id;

        @JoinColumn(name = "containable_id")
        @OneToOne(fetch = EAGER)
        AbstractProperties3Test.Containable containable;
    }

    @Test
    public void test() {
        name.isNotNull();
    }
}
