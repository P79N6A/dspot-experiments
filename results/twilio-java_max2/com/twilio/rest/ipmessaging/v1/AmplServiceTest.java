/**
 * This code was generated by
 * \ / _    _  _|   _  _
 *  | (_)\/(_)(_|\/| |(/_  v1.0.0
 *       /       /
 */


package com.twilio.rest.ipmessaging.v1;


public class AmplServiceTest {
    @mockit.Mocked
    private com.twilio.http.TwilioRestClient twilioRestClient;

    @org.junit.Before
    public void setUp() throws java.lang.Exception {
        com.twilio.Twilio.init("AC123", "AUTH TOKEN");
    }

    @org.junit.Test
    public void testFetchRequest() {
        new mockit.NonStrictExpectations() {
            {
                com.twilio.http.Request request = new com.twilio.http.Request(com.twilio.http.HttpMethod.GET, com.twilio.rest.Domains.IPMESSAGING.toString(), "/v1/Services/ISaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
                twilioRestClient.request(request);
                times = 1;
                result = new com.twilio.http.Response("", 500);
                twilioRestClient.getAccountSid();
                result = "AC123";
            }
        };
        try {
            com.twilio.rest.ipmessaging.v1.Service.fetcher("ISaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa").fetch();
            org.junit.Assert.fail("Expected TwilioException to be thrown for 500");
        } catch (com.twilio.exception.TwilioException e) {
        }
    }

    @org.junit.Test
    public void testFetchResponse() {
        new mockit.NonStrictExpectations() {
            {
                twilioRestClient.request(((com.twilio.http.Request) (any)));
                result = new com.twilio.http.Response("{\"account_sid\": \"ACaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa\",\"consumption_report_interval\": 100,\"date_created\": \"2015-07-30T20:00:00Z\",\"date_updated\": \"2015-07-30T20:00:00Z\",\"default_channel_creator_role_sid\": \"RLaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa\",\"default_channel_role_sid\": \"RLaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa\",\"default_service_role_sid\": \"RLaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa\",\"friendly_name\": \"friendly_name\",\"links\": {},\"notifications\": {},\"post_webhook_url\": \"post_webhook_url\",\"pre_webhook_url\": \"pre_webhook_url\",\"reachability_enabled\": false,\"read_status_enabled\": false,\"sid\": \"ISaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa\",\"typing_indicator_timeout\": 100,\"url\": \"http://www.example.com\",\"webhook_filters\": [\"webhook_filters\"],\"webhook_method\": \"webhook_method\",\"webhooks\": {}}", com.twilio.http.TwilioRestClient.HTTP_STATUS_CODE_OK);
                twilioRestClient.getObjectMapper();
                result = new com.fasterxml.jackson.databind.ObjectMapper();
            }
        };
        org.junit.Assert.assertNotNull(com.twilio.rest.ipmessaging.v1.Service.fetcher("ISaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa").fetch());
    }

    @org.junit.Test
    public void testDeleteRequest() {
        new mockit.NonStrictExpectations() {
            {
                com.twilio.http.Request request = new com.twilio.http.Request(com.twilio.http.HttpMethod.DELETE, com.twilio.rest.Domains.IPMESSAGING.toString(), "/v1/Services/ISaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
                twilioRestClient.request(request);
                times = 1;
                result = new com.twilio.http.Response("", 500);
                twilioRestClient.getAccountSid();
                result = "AC123";
            }
        };
        try {
            com.twilio.rest.ipmessaging.v1.Service.deleter("ISaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa").delete();
            org.junit.Assert.fail("Expected TwilioException to be thrown for 500");
        } catch (com.twilio.exception.TwilioException e) {
        }
    }

    @org.junit.Test
    public void testDeleteResponse() {
        new mockit.NonStrictExpectations() {
            {
                twilioRestClient.request(((com.twilio.http.Request) (any)));
                result = new com.twilio.http.Response("null", com.twilio.http.TwilioRestClient.HTTP_STATUS_CODE_NO_CONTENT);
                twilioRestClient.getObjectMapper();
                result = new com.fasterxml.jackson.databind.ObjectMapper();
            }
        };
        com.twilio.rest.ipmessaging.v1.Service.deleter("ISaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa").delete();
    }

    @org.junit.Test
    public void testCreateRequest() {
        new mockit.NonStrictExpectations() {
            {
                com.twilio.http.Request request = new com.twilio.http.Request(com.twilio.http.HttpMethod.POST, com.twilio.rest.Domains.IPMESSAGING.toString(), "/v1/Services");
                request.addPostParam("FriendlyName", com.twilio.TwilioTest.serialize("friendlyName"));
                twilioRestClient.request(request);
                times = 1;
                result = new com.twilio.http.Response("", 500);
                twilioRestClient.getAccountSid();
                result = "AC123";
            }
        };
        try {
            com.twilio.rest.ipmessaging.v1.Service.creator("friendlyName").create();
            org.junit.Assert.fail("Expected TwilioException to be thrown for 500");
        } catch (com.twilio.exception.TwilioException e) {
        }
    }

    @org.junit.Test
    public void testCreateResponse() {
        new mockit.NonStrictExpectations() {
            {
                twilioRestClient.request(((com.twilio.http.Request) (any)));
                result = new com.twilio.http.Response("{\"account_sid\": \"ACaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa\",\"consumption_report_interval\": 100,\"date_created\": \"2015-07-30T20:00:00Z\",\"date_updated\": \"2015-07-30T20:00:00Z\",\"default_channel_creator_role_sid\": \"RLaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa\",\"default_channel_role_sid\": \"RLaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa\",\"default_service_role_sid\": \"RLaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa\",\"friendly_name\": \"friendly_name\",\"links\": {},\"notifications\": {},\"post_webhook_url\": \"post_webhook_url\",\"pre_webhook_url\": \"pre_webhook_url\",\"reachability_enabled\": false,\"read_status_enabled\": false,\"sid\": \"ISaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa\",\"typing_indicator_timeout\": 100,\"url\": \"http://www.example.com\",\"webhook_filters\": [\"webhook_filters\"],\"webhook_method\": \"webhook_method\",\"webhooks\": {}}", com.twilio.http.TwilioRestClient.HTTP_STATUS_CODE_CREATED);
                twilioRestClient.getObjectMapper();
                result = new com.fasterxml.jackson.databind.ObjectMapper();
            }
        };
        com.twilio.rest.ipmessaging.v1.Service.creator("friendlyName").create();
    }

    @org.junit.Test
    public void testReadRequest() {
        new mockit.NonStrictExpectations() {
            {
                com.twilio.http.Request request = new com.twilio.http.Request(com.twilio.http.HttpMethod.GET, com.twilio.rest.Domains.IPMESSAGING.toString(), "/v1/Services");
                twilioRestClient.request(request);
                times = 1;
                result = new com.twilio.http.Response("", 500);
                twilioRestClient.getAccountSid();
                result = "AC123";
            }
        };
        try {
            com.twilio.rest.ipmessaging.v1.Service.reader().read();
            org.junit.Assert.fail("Expected TwilioException to be thrown for 500");
        } catch (com.twilio.exception.TwilioException e) {
        }
    }

    @org.junit.Test
    public void testReadEmptyResponse() {
        new mockit.NonStrictExpectations() {
            {
                twilioRestClient.request(((com.twilio.http.Request) (any)));
                result = new com.twilio.http.Response("{\"meta\": {\"first_page_url\": \"https://ip-messaging.twilio.com/v1/Services?Page=0&PageSize=50\",\"key\": \"services\",\"next_page_url\": null,\"page\": 0,\"page_size\": 0,\"previous_page_url\": null,\"url\": \"https://ip-messaging.twilio.com/v1/Services\"},\"services\": []}", com.twilio.http.TwilioRestClient.HTTP_STATUS_CODE_OK);
                twilioRestClient.getObjectMapper();
                result = new com.fasterxml.jackson.databind.ObjectMapper();
            }
        };
        org.junit.Assert.assertNotNull(com.twilio.rest.ipmessaging.v1.Service.reader().read());
    }

    @org.junit.Test
    public void testReadFullResponse() {
        new mockit.NonStrictExpectations() {
            {
                twilioRestClient.request(((com.twilio.http.Request) (any)));
                result = new com.twilio.http.Response("{\"meta\": {\"first_page_url\": \"https://ip-messaging.twilio.com/v1/Services?Page=0&PageSize=50\",\"key\": \"services\",\"next_page_url\": null,\"page\": 0,\"page_size\": 1,\"previous_page_url\": null,\"url\": \"https://ip-messaging.twilio.com/v1/Services\"},\"services\": [{\"account_sid\": \"ACaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa\",\"consumption_report_interval\": 100,\"date_created\": \"2015-07-30T20:00:00Z\",\"date_updated\": \"2015-07-30T20:00:00Z\",\"default_channel_creator_role_sid\": \"RLaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa\",\"default_channel_role_sid\": \"RLaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa\",\"default_service_role_sid\": \"RLaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa\",\"friendly_name\": \"friendly_name\",\"links\": {},\"notifications\": {},\"post_webhook_url\": \"post_webhook_url\",\"pre_webhook_url\": \"pre_webhook_url\",\"reachability_enabled\": false,\"read_status_enabled\": false,\"sid\": \"ISaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa\",\"typing_indicator_timeout\": 100,\"url\": \"http://www.example.com\",\"webhook_filters\": [\"webhook_filters\"],\"webhook_method\": \"webhook_method\",\"webhooks\": {}}]}", com.twilio.http.TwilioRestClient.HTTP_STATUS_CODE_OK);
                twilioRestClient.getObjectMapper();
                result = new com.fasterxml.jackson.databind.ObjectMapper();
            }
        };
        org.junit.Assert.assertNotNull(com.twilio.rest.ipmessaging.v1.Service.reader().read());
    }

    @org.junit.Test
    public void testUpdateRequest() {
        new mockit.NonStrictExpectations() {
            {
                com.twilio.http.Request request = new com.twilio.http.Request(com.twilio.http.HttpMethod.POST, com.twilio.rest.Domains.IPMESSAGING.toString(), "/v1/Services/ISaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
                twilioRestClient.request(request);
                times = 1;
                result = new com.twilio.http.Response("", 500);
                twilioRestClient.getAccountSid();
                result = "AC123";
            }
        };
        try {
            com.twilio.rest.ipmessaging.v1.Service.updater("ISaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa").update();
            org.junit.Assert.fail("Expected TwilioException to be thrown for 500");
        } catch (com.twilio.exception.TwilioException e) {
        }
    }

    @org.junit.Test
    public void testUpdateResponse() {
        new mockit.NonStrictExpectations() {
            {
                twilioRestClient.request(((com.twilio.http.Request) (any)));
                result = new com.twilio.http.Response("{\"account_sid\": \"ACaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa\",\"consumption_report_interval\": 100,\"date_created\": \"2015-07-30T20:00:00Z\",\"date_updated\": \"2015-07-30T20:00:00Z\",\"default_channel_creator_role_sid\": \"RLaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa\",\"default_channel_role_sid\": \"RLaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa\",\"default_service_role_sid\": \"RLaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa\",\"friendly_name\": \"friendly_name\",\"links\": {},\"notifications\": {},\"post_webhook_url\": \"post_webhook_url\",\"pre_webhook_url\": \"pre_webhook_url\",\"reachability_enabled\": false,\"read_status_enabled\": false,\"sid\": \"ISaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa\",\"typing_indicator_timeout\": 100,\"url\": \"http://www.example.com\",\"webhook_filters\": [\"webhook_filters\"],\"webhook_method\": \"webhook_method\",\"webhooks\": {}}", com.twilio.http.TwilioRestClient.HTTP_STATUS_CODE_OK);
                twilioRestClient.getObjectMapper();
                result = new com.fasterxml.jackson.databind.ObjectMapper();
            }
        };
        com.twilio.rest.ipmessaging.v1.Service.updater("ISaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa").update();
    }
}

