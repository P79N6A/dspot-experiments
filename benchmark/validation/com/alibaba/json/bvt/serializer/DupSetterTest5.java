/**
 * www.yiji.com Inc.
 * Copyright (c) 2014 All Rights Reserved
 */
/**
 * ????:
 * qzhanbo@yiji.com 2015-03-01 00:55 ??
 */
package com.alibaba.json.bvt.serializer;


import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;
import junit.framework.TestCase;


public class DupSetterTest5 extends TestCase {
    public void testDup() {
        DupSetterTest5.V1 vo = new DupSetterTest5.V1();
        vo.status = 3;
        String json = JSONObject.toJSONString(vo);
        JSONObject.parseObject(json, DupSetterTest5.V1.class);
    }

    public static class V0 {
        @JSONField(name = "status")
        public long status2;
    }

    public static class V1 extends DupSetterTest5.V0 {
        private Integer status;

        public Integer getStatus() {
            return status;
        }

        public void setStatus(Integer status) {
            this.status = status;
        }
    }
}
