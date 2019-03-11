package com.alibaba.json.bvt.date;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import java.util.Date;
import junit.framework.TestCase;
import org.junit.Assert;


public class DateTest_dotnet_4 extends TestCase {
    public void test_date() throws Exception {
        String text = "{\"date\":\"/Date(1461081600321+5000)/\"}";
        JSONObject model = JSON.parseObject(text);
        Assert.assertEquals(1461081600321L, ((Date) (model.getObject("date", Date.class))).getTime());
    }

    private static class Model {
        private Date date;

        public Date getDate() {
            return date;
        }

        public void setDate(Date date) {
            this.date = date;
        }
    }
}
