/**
 * The MIT License
 *
 * Copyright (c) 2018, CloudBees, Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package jenkins.security.apitoken;


import com.gargoylesoftware.htmlunit.HttpMethod;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.WebRequest;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSpan;
import com.gargoylesoftware.htmlunit.util.NameValuePair;
import hudson.model.User;
import java.net.URL;
import java.util.Arrays;
import jenkins.security.ApiTokenProperty;
import net.sf.json.JSONObject;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.JenkinsRule.WebClient;


public class ApiTokenStatsTest {
    @Rule
    public JenkinsRule j = new JenkinsRule();

    @Test
    public void roundtrip() throws Exception {
        j.jenkins.setCrumbIssuer(null);
        j.jenkins.setSecurityRealm(j.createDummySecurityRealm());
        User u = User.getById("foo", true);
        ApiTokenProperty t = u.getProperty(ApiTokenProperty.class);
        Assert.assertNotNull(t.getTokenStore());
        Assert.assertNotNull(t.getTokenStats());
        // test the authentication via Token
        WebClient wc = j.createWebClient().withBasicCredentials(u.getId()).withThrowExceptionOnFailingStatusCode(false);
        final String TOKEN_NAME = "New Token Name";
        WebRequest request = new WebRequest(new URL(((((((j.getURL()) + "user/") + (u.getId())) + "/descriptorByName/") + (ApiTokenProperty.class.getName())) + "/generateNewToken")), HttpMethod.POST);
        request.setRequestParameters(Arrays.asList(new NameValuePair("newTokenName", TOKEN_NAME)));
        Page page = wc.getPage(request);
        Assert.assertEquals(200, page.getWebResponse().getStatusCode());
        String responseContent = page.getWebResponse().getContentAsString();
        JSONObject jsonObject = JSONObject.fromObject(responseContent);
        JSONObject jsonData = jsonObject.getJSONObject("data");
        String tokenName = jsonData.getString("tokenName");
        String tokenValue = jsonData.getString("tokenValue");
        String tokenUuid = jsonData.getString("tokenUuid");
        Assert.assertEquals(TOKEN_NAME, tokenName);
        WebClient restWc = j.createWebClient().withBasicCredentials(u.getId(), tokenValue);
        checkUserIsConnected(restWc, u.getId());
        HtmlPage config = wc.goTo(((u.getUrl()) + "/configure"));
        Assert.assertEquals(200, config.getWebResponse().getStatusCode());
        Assert.assertThat(config.getWebResponse().getContentAsString(), Matchers.containsString(tokenUuid));
        Assert.assertThat(config.getWebResponse().getContentAsString(), Matchers.containsString(tokenName));
        final int NUM_CALL_WITH_TOKEN = 5;
        // one is already done with checkUserIsConnected
        for (int i = 1; i < NUM_CALL_WITH_TOKEN; i++) {
            restWc.goToXml("whoAmI/api/xml");
        }
        HtmlPage configWithStats = wc.goTo(((u.getUrl()) + "/configure"));
        Assert.assertEquals(200, configWithStats.getWebResponse().getStatusCode());
        HtmlSpan useCounterSpan = configWithStats.getDocumentElement().getOneHtmlElementByAttribute("span", "class", "token-use-counter");
        Assert.assertThat(useCounterSpan.getTextContent(), Matchers.containsString(("" + NUM_CALL_WITH_TOKEN)));
        revokeToken(wc, u.getId(), tokenUuid);
        // token is no more valid
        checkUserIsNotConnected(restWc);
        HtmlPage configWithoutToken = wc.goTo(((u.getUrl()) + "/configure"));
        Assert.assertEquals(200, configWithoutToken.getWebResponse().getStatusCode());
        Assert.assertThat(configWithoutToken.getWebResponse().getContentAsString(), Matchers.not(Matchers.containsString(tokenUuid)));
        Assert.assertThat(configWithoutToken.getWebResponse().getContentAsString(), Matchers.not(Matchers.containsString(tokenName)));
    }
}
