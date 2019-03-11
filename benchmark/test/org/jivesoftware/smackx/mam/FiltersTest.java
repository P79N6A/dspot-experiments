/**
 * Copyright 2016 Fernando Ramirez, 2018 Florian Schmaus
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jivesoftware.smackx.mam;


import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.jivesoftware.smackx.mam.MamManager.MamQueryArgs;
import org.jivesoftware.smackx.xdata.packet.DataForm;
import org.junit.Assert;
import org.junit.Test;
import org.jxmpp.jid.Jid;
import org.jxmpp.jid.JidTestUtil;
import org.jxmpp.util.XmppDateTime;


public class FiltersTest extends MamTest {
    @Test
    public void checkStartDateFilter() throws Exception {
        Date date = new Date();
        MamQueryArgs mamQueryArgs = MamQueryArgs.builder().limitResultsSince(date).build();
        DataForm dataForm = mamQueryArgs.getDataForm();
        List<String> fields = new ArrayList<>();
        fields.add("start");
        List<String> values = new ArrayList<>();
        values.add(XmppDateTime.formatXEP0082Date(date));
        Assert.assertEquals(FiltersTest.getMamXMemberWith(fields, values), dataForm.toXML().toString());
    }

    @Test
    public void checkEndDateFilter() throws Exception {
        Date date = new Date();
        MamQueryArgs mamQueryArgs = MamQueryArgs.builder().limitResultsBefore(date).build();
        DataForm dataForm = mamQueryArgs.getDataForm();
        List<String> fields = new ArrayList<>();
        fields.add("end");
        List<String> values = new ArrayList<>();
        values.add(XmppDateTime.formatXEP0082Date(date));
        Assert.assertEquals(FiltersTest.getMamXMemberWith(fields, values), dataForm.toXML().toString());
    }

    @Test
    public void checkWithJidFilter() throws Exception {
        Jid jid = JidTestUtil.BARE_JID_1;
        MamQueryArgs mamQueryArgs = MamQueryArgs.builder().limitResultsToJid(jid).build();
        DataForm dataForm = mamQueryArgs.getDataForm();
        List<String> fields = new ArrayList<>();
        fields.add("with");
        List<CharSequence> values = new ArrayList<>();
        values.add(jid);
        Assert.assertEquals(FiltersTest.getMamXMemberWith(fields, values), dataForm.toXML().toString());
    }
}
