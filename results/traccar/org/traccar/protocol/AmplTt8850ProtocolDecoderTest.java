

package org.traccar.protocol;


public class AmplTt8850ProtocolDecoderTest extends org.traccar.ProtocolTest {
    @org.junit.Test
    public void testDecode() throws java.lang.Exception {
        org.traccar.protocol.Tt8850ProtocolDecoder decoder = new org.traccar.protocol.Tt8850ProtocolDecoder(new org.traccar.protocol.Tt8850Protocol());
        verifyPosition(decoder, text(" ,005F,0,GTFRI,020100,135790246811220,,0,0,1,1,4.3,92,70.0,121.354335,31.222073,20090214013254,0460,0000,18d8,6141,90,20090214093254,11F0"));
        verifyPosition(decoder, text(" ,005F,0,GTGEO,020100,135790246811220,,0,0,1,1,4.3,92,70.0,121.354335,31.222073,20090214013254,0460,0000,18d8,6141,90,20090214093254,11F0"));
        verifyPosition(decoder, text(" ,005F,0,GTNMR,020100,135790246811220,,0,0,1,1,4.3,92,70.0,121.354335,31.222073,20090214013254,0460,0000,18d8,6141,90,20090214093254,11F0"));
        verifyPosition(decoder, text(" ,0017,0,GTNMR,,867844000400914,,0,41,1,2,0.0,0,1504.2,-75.569202,6.242850,20150404162835,,,,,97,20150404162836,05EF"));
        verifyNothing(decoder, text(" ,0017,0,GTPNA,,867844000400914,,0,0,1,0,,,,0,0,,,,,,99,20150404190153,0601"));
        verifyPosition(decoder, text(" ,0017,0,GTEPN,,867844000400914,,0,0,1,0,0.0,0,1717.4,-75.598445,6.278578,20150405003116,,,,,95,20150405003358,0607"));
    }
}

