package com.orientechnologies.orient.core.db.record;


import com.orientechnologies.orient.core.command.script.OCommandScript;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.OCommandSQL;
import java.util.Collection;
import org.junit.Assert;
import org.junit.Test;


/**
 *
 *
 * @author Luca Garulli (l.garulli--(at)--orientdb.com)
 */
public class TestTypeGuessingWorkingWithSQLAndMultiValues {
    private ODatabaseDocumentTx db;

    @Test
    public void testLinkedValue() {
        Iterable<ODocument> result = db.command(new OCommandScript("sql", ("insert into client set name = \'James Bond\', phones = [\'1234\', \'34567\'], addresses = [{\'city\':\'Shanghai\', \'zip\':\'3999\'}, {\'city\':\'New York\', \'street\':\'57th Ave\'}]\n" + "update client add addresses = [{'@type':'d','@class':'Address','city':'London', 'zip':'67373'}] return after"))).execute();
        Assert.assertTrue(result.iterator().hasNext());
        ODocument doc = result.iterator().next();
        Collection<ODocument> addresses = ((Collection<ODocument>) (doc.field("addresses")));
        Assert.assertEquals(addresses.size(), 3);
        for (ODocument a : addresses)
            Assert.assertTrue(a.getClassName().equals("Address"));

        result = db.command(new OCommandSQL("update client add addresses = [{'city':'London', 'zip':'67373'}] return after")).execute();
        Assert.assertTrue(result.iterator().hasNext());
        doc = result.iterator().next();
        addresses = ((Collection<ODocument>) (doc.field("addresses")));
        Assert.assertEquals(addresses.size(), 4);
        for (ODocument a : addresses)
            Assert.assertTrue(a.getClassName().equals("Address"));

    }
}
