package com.alibaba.json.bvt.serializer.date;


import com.alibaba.fastjson.JSON;
import java.util.Date;
import junit.framework.TestCase;


public class DateTest4_indian extends TestCase {
    public void test_date() throws Exception {
        Date date1 = JSON.parseObject("{\"gmtCreate\":\"2018-09-11T21:29:34+0530\"}", DateTest4_indian.VO.class).getGmtCreate();
        TestCase.assertNotNull(date1);
        Date date2 = JSON.parseObject("{\"gmtCreate\":\"2018-09-11T21:29:34+0500\"}", DateTest4_indian.VO.class).getGmtCreate();
        Date date3 = JSON.parseObject("{\"gmtCreate\":\"2018-09-11T21:29:34+0545\"}", DateTest4_indian.VO.class).getGmtCreate();
        Date date4 = JSON.parseObject("{\"gmtCreate\":\"2018-09-11T21:29:34+1245\"}", DateTest4_indian.VO.class).getGmtCreate();
        Date date5 = JSON.parseObject("{\"gmtCreate\":\"2018-09-11T21:29:34+1345\"}", DateTest4_indian.VO.class).getGmtCreate();
        long delta_2_1 = (date2.getTime()) - (date1.getTime());
        TestCase.assertEquals(1800000, delta_2_1);
        long delta_3_1 = (date3.getTime()) - (date1.getTime());
        TestCase.assertEquals((-900000), delta_3_1);
        long delta_4_3 = (date4.getTime()) - (date3.getTime());
        TestCase.assertEquals((-25200000), delta_4_3);
        long delta_5_4 = (date5.getTime()) - (date4.getTime());
        TestCase.assertEquals(17100000, delta_5_4);
    }

    public static class VO {
        private Date gmtCreate;

        public Date getGmtCreate() {
            return gmtCreate;
        }

        public void setGmtCreate(Date gmtCreate) {
            this.gmtCreate = gmtCreate;
        }
    }
}
