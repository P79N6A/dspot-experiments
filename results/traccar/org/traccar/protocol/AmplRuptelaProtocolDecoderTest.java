

package org.traccar.protocol;


public class AmplRuptelaProtocolDecoderTest extends org.traccar.ProtocolTest {
    @org.junit.Test
    public void testDecode() throws java.lang.Exception {
        org.traccar.protocol.RuptelaProtocolDecoder decoder = new org.traccar.protocol.RuptelaProtocolDecoder(new org.traccar.protocol.RuptelaProtocol());
        verifyPositions(decoder, binary("01a4000315bc70f9b69244000458068f4a0030000d11398a1c0c19fd056524040b000c0a00090c0005010031f40032fd0033f200ce47002400002500001c010199000195010196010086000900aa0000001e0ff000d3ffff0043ffff01930000019200000194000002220000022300000200300000000200af000e872401008e000000000000000058068f4a0031000d11398a1c0c19fd056524040b000c0a00090400870000880000a90000820010008b0002021e0000021f0000021d0000021c0000022400000225000000890000008505f00220000002210000008300000084000002260000022700000228000003008a00000000008d00000000008c000000000058068f4a0032000d11398a1c0c19fd056524040b000c0a000905019f01005800001b1f00ad0000cfb10b02290000022a0000022b0000022c0000022d00000012000000130000001d367400c52f8000740055023e0502060097000000000096000058520041007746cb00d0000003f1005c0007c21b0072001864880058068f4a0033000d11398a1c0c19fd056524040b000c0a000900000001008e0000000000000000e815"));
        verifyPositions(decoder, binary("033d000315bc70f9b69244000858068f3b0030010d11354e1c0c17a5055d54560c00000900050c0005010031f30032fb0033f300ce00002400002500001c010199000195010196010086000900aa0000001e0ff300d3ffff0043ffff01930000019200000194000002220000022300000200300000000000af000e872401008e000000000000000058068f3b0031010d11354e1c0c17a5055d54560c00000900050400870000880000a90000820010008b0000021e0000021f0000021d0000021c0000022400000225000000890000008500000220000002210000008300000084000002260000022700000228000003008a00000000008d00000000008c000000000058068f3b0032010d11354e1c0c17a5055d54560c000009000505019f01005800001b1f00ad0000cfac0b02290000022a0000022b0000022c0000022d00000012000000130000001d31b100c5000000740000023e0502060097000000000096000058520041007746be00d0000003f1005c0007c2150072001864880058068f3b0033010d11354e1c0c17a5055d54560c000009000500000001008e000000000000000058068f3b0130000d11354e1c0c17a5055d54560d00000900070c0005010031f30032fb0033f300ce00002400002500001c010199000195010196010086000900aa0000001e0ff300d3ffff0043ffff01930000019200000194000002220000022300000200300000000000af000e872401008e000000000000000058068f3b0131000d11354e1c0c17a5055d54560d00000900070400870000880000a90000820010008b0000021e0000021f0000021d0000021c0000022400000225000000890000008500000220000002210000008300000084000002260000022700000228000003008a00000000008d00000000008c000000000058068f3b0132000d11354e1c0c17a5055d54560d000009000705019f01005800001b1f00ad0000cfac0b02290000022a0000022b0000022c0000022d00000012000000130000001d31ae00c5000000740000023e0502060097000000000096000058520041007746be00d0000003f1005c0007c2150072001864880058068f3b0133000d11354e1c0c17a5055d54560d000009000700000001008e0000000000000000084d"));
        verifyPositions(decoder, binary("0050000310f5615f419c0100015613d8ed0000fff5b37a035af37801e700000900000d07071b0c020003001c01202cad000500064302a81d33e61e100116317cd3ffff174ad60241000077fa960000f232003c2e"));
        verifyPositions(decoder, binary("00560003116e7438a7a50100015565cbb9000020fd21300f113f4600005f000600090d090805011b13cf00020003001c012029ad00041d31dd1e0ebd160000c50000047200000000d0000000004100016a2a960000a5a300c9ee"));
        verifyPositions(decoder, binary("00a10003116e7438a7a5010002553dddbe000020fddaff0f12289b007200000600000c070805011b18cf00020003001c01201dad01041d32d81e0d7d160000c50000047200000000d000000000410000b1ae960000a5a300553dddd4000020fdd96f0f122bfe005c16f80700050b090805011b18cf00020003001c01201ead01041d338a1e0d8d160000c50000047200000000d000000000410000b1bd960000a5a3001681"));
        verifyPositions(decoder, binary("007900000b1a2a5585c30100024e9c036900000f101733208ff45e07b31b570a001009090605011b1a020003001c01ad01021d338e16000002960000601a41014bc16d004e9c038400000f104fdf20900d20075103b00a001308090605011b1a020003001c01ad01021d33b116000002960000601a41014bc1ea0028f9"));
        verifyPositions(decoder, binary("009200000c07a6bacd4701000552db5cc20000187b8b251ace478e087c044c0a000009070000000052db5cfe0000187b8ab01ace47190879044c0900000b070000000052db5d3a0000187b8b251ace474b089d044c09000009070000000052db5d760000187b8b9a1ace475c08cd044c08000009070000000052db5db20000187b8b141ace46e708b3044c08000009070000000041cb"));
    }
}

