

package org.traccar.protocol;


public class AmplXexunFrameDecoderTest extends org.traccar.ProtocolTest {
    @org.junit.Test
    public void testDecode() throws java.lang.Exception {
        org.traccar.protocol.XexunFrameDecoder decoder = new org.traccar.protocol.XexunFrameDecoder();
        org.junit.Assert.assertEquals(binary("4750524d432c3230353933352e3030302c412c353134302e343335302c4e2c3530312e303638362c452c302e30302c302e30302c3132313031352c30302c303030302e302c412a37302c462c2c696d65693a3335393538373031343731383339322c"), decoder.decode(null, null, binary("313531303132313435392c2b33313635323435343932372c4750524d432c3230353933352e3030302c412c353134302e343335302c4e2c3530312e303638362c452c302e30302c302e30302c3132313031352c30302c303030302e302c412a37302c462c2c696d65693a3335393538373031343731383339322c31323249")));
        org.junit.Assert.assertEquals(binary("4750524d432c3130333733312e3633362c412c343534352e353236362c4e2c30303434382e383235392c452c32312e31322c3237362e30312c3135303631352c2c2c412a35372c4c2c2c20696d65693a3031333934393030323032363637352c"), decoder.decode(null, null, binary("3135303631353132333733312c2b33333634373338343631312c4750524d432c3130333733312e3633362c412c343534352e353236362c4e2c30303434382e383235392c452c32312e31322c3237362e30312c3135303631352c2c2c412a35372c4c2c2c20696d65693a3031333934393030323032363637352c30342c333532322e392c463a332e3732562c302c3134322c32313734342c3230382c30312c303730322c394338430a0d")));
    }
}

