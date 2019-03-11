package com.baeldung.repository;


import com.baeldung.config.MongoConfig;
import com.baeldung.model.Action;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;


/**
 * This test requires:
 * * mongodb instance running on the environment
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = MongoConfig.class)
public class ActionRepositoryLiveTest {
    @Autowired
    private MongoOperations mongoOps;

    @Autowired
    private ActionRepository actionRepository;

    @Test
    public void givenSavedAction_TimeIsRetrievedCorrectly() {
        String id = "testId";
        ZonedDateTime now = ZonedDateTime.now(ZoneOffset.UTC);
        actionRepository.save(new Action(id, "click-action", now));
        Action savedAction = actionRepository.findById(id).get();
        Assert.assertEquals(now.withNano(0), savedAction.getTime().withNano(0));
    }
}
