package com.alibaba.json.bvt.feature;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.annotation.JSONField;
import com.alibaba.fastjson.serializer.SerializeConfig;
import com.alibaba.fastjson.serializer.SerializerFeature;
import junit.framework.TestCase;
import org.junit.Assert;


public class FeaturesTest3 extends TestCase {
    public void test_0() throws Exception {
        SerializeConfig config = new SerializeConfig();
        config.setAsmEnable(false);
        String text = JSON.toJSONString(new FeaturesTest3.Entity(), config);
        Assert.assertEquals("{\"value\":0}", text);
    }

    public void test_1() throws Exception {
        SerializeConfig config = new SerializeConfig();
        config.setAsmEnable(true);
        String text = JSON.toJSONString(new FeaturesTest3.Entity(), config);
        Assert.assertEquals("{\"value\":0}", text);
    }

    public static class Entity {
        private Integer value;

        @JSONField(serialzeFeatures = { SerializerFeature.WriteMapNullValue, SerializerFeature.WriteNullNumberAsZero })
        public Integer getValue() {
            return value;
        }
    }
}
