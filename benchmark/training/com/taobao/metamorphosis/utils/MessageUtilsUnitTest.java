/**
 * (C) 2007-2012 Alibaba Group Holding Limited.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * Authors:
 *   wuhua <wq163@163.com> , boyan <killme2008@gmail.com>
 */
package com.taobao.metamorphosis.utils;


import com.taobao.metamorphosis.network.PutCommand;
import com.taobao.metamorphosis.utils.MessageUtils.DecodedMessage;
import java.nio.ByteBuffer;
import org.junit.Assert;
import org.junit.Test;


public class MessageUtilsUnitTest {
    @Test
    public void testMakeBufferDecode() throws Exception {
        final long msgId = 10000L;
        final String topic = "test";
        final PutCommand req = new PutCommand(topic, 1, "hello".getBytes(), null, 0, (-1));
        final ByteBuffer buf = MessageUtils.makeMessageBuffer(msgId, req);
        final DecodedMessage decodedMsg = MessageUtils.decodeMessage(topic, buf.array(), 0);
        Assert.assertNotNull(decodedMsg);
        Assert.assertEquals(topic, decodedMsg.message.getTopic());
        Assert.assertNull(decodedMsg.message.getAttribute());
        Assert.assertEquals(msgId, decodedMsg.message.getId());
        Assert.assertEquals("hello", new String(decodedMsg.message.getData()));
    }
}
