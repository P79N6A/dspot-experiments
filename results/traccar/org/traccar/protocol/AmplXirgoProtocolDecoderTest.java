

package org.traccar.protocol;


public class AmplXirgoProtocolDecoderTest extends org.traccar.ProtocolTest {
    @org.junit.Test
    public void testDecodeNew() throws java.lang.Exception {
        org.traccar.protocol.XirgoProtocolDecoder decoder = new org.traccar.protocol.XirgoProtocolDecoder(new org.traccar.protocol.XirgoProtocol());
        verifyPosition(decoder, text("$$355922061611345,6001,2016/08/25,20:10:51,51.13042,-114.22752,1197,44.7,0.0,0.0,2622,27,12,0.8,1,0.0,13.9,24,1,0,0.0,-70,-809,688##"));
        verifyPosition(decoder, text("$$355922061611345,6001,2016/08/25,20:10:38,51.12948,-114.22637,1203,34.8,0.0,0.0,1377,215,12,0.8,1,0.0,13.8,28,1,0,0.0,-309,-566,754##"));
        verifyPosition(decoder, text("$$354898045650537,6031,2015/02/26,15:47:26,33.42552,-112.30308,287.8,0,0,0,0,0.0,7,1.2,2,0.0,12.2,22,1,0,82.3"));
        verifyPosition(decoder, text("$$355922060162167,6015,2016/04/21,17:26:52,39.83267,-76.66139,230,0.0,0.0,0.0,779,0,8,1.2,0,0.0,13.0,19,1,1C4BJWDG4GL191009,X0z1-1137CD1,0402,3GATT,0,83.9,-70,-715,738##"));
        verifyPosition(decoder, text("$$355922060162167,4002,2016/04/21,17:04:50,39.83253,-76.66102,232,0.0,0.0,0.0,0,0,12,1.2,0,0.0,9.2,15,1,0,0.0,35,-8,1059##"));
    }

    @org.junit.Test
    public void testDecodeOld() throws java.lang.Exception {
        org.traccar.protocol.XirgoProtocolDecoder decoder = new org.traccar.protocol.XirgoProtocolDecoder(new org.traccar.protocol.XirgoProtocol());
        verifyPosition(decoder, text("$$354660046140722,6001,2013/01/22,15:36:18,25.80907,-80.32531,7.1,19,165.2,11,0.8,11.1,17,1,1,3.9,2##"), position("2013-01-22 15:36:18.000", true, 25.80907, (-80.32531)));
        verifyPosition(decoder, text("$$357207059646786,4003,2015/05/19,15:54:56,-20.21422,-70.14927,37.5,1.8,0.0,11,0.8,12.9,31,297,1,0,0.0,0.0,0,1,1,1##"));
        verifyPosition(decoder, text("$$354898045650537,6031,2015/02/26,15:47:26,33.42552,-112.30308,287.8,0,0,0,0,0.0,7,1.2,2,0.0,12.2,22,1,0,82.3"));
        verifyPosition(decoder, text("$$357207059646786,4003,2015/05/19,15:55:27,-20.21421,-70.14920,33.6,0.4,0.0,11,0.8,12.9,31,297,1,0,0.0,0.0,0,1,1,1##"));
    }
}

