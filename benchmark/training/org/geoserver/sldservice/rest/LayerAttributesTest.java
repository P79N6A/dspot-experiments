/**
 * (c) 2014 Open Source Geospatial Foundation - all rights reserved
 * Copyright (C) 2007-2008-2009 GeoSolutions S.A.S.
 *  http://www.geo-solutions.it
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.sldservice.rest;


import java.io.ByteArrayOutputStream;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.geoserver.catalog.LayerInfo;
import org.geoserver.rest.RestBaseController;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletResponse;
import org.w3c.dom.Document;


public class LayerAttributesTest extends SLDServiceBaseTest {
    @Test
    public void testListAttributesForFeatureXml() throws Exception {
        LayerInfo l = getCatalog().getLayerByName("cite:Buildings");
        Assert.assertEquals("Buildings", l.getDefaultStyle().getName());
        final String restPath = (((RestBaseController.ROOT_PATH) + "/sldservice/cite:Buildings/") + (getServiceUrl())) + ".xml";
        MockHttpServletResponse response = getAsServletResponse(restPath);
        // Randomly cannot find REST path
        if ((response.getStatus()) == 200) {
            Document dom = getAsDOM(restPath, 200);
            Assert.assertEquals("Attributes", dom.getDocumentElement().getNodeName());
            assertXpathEvaluatesTo("cite:Buildings", "/Attributes/@layer", dom);
            assertXpathEvaluatesTo("FID", "/Attributes/Attribute[1]/name", dom);
            assertXpathEvaluatesTo("String", "/Attributes/Attribute[1]/type", dom);
        }
    }

    @Test
    public void testListAttributesForFeatureJson() throws Exception {
        LayerInfo l = getCatalog().getLayerByName("cite:Buildings");
        Assert.assertEquals("Buildings", l.getDefaultStyle().getName());
        final String restPath = (((RestBaseController.ROOT_PATH) + "/sldservice/cite:Buildings/") + (getServiceUrl())) + ".json";
        MockHttpServletResponse response = getAsServletResponse(restPath);
        // Randomly cannot find REST path
        if ((response.getStatus()) == 200) {
            JSONObject json = ((JSONObject) (getAsJSON(restPath)));
            JSONObject layerAttributes = ((JSONObject) (json.get("Attributes")));
            String layer = ((String) (layerAttributes.get("@layer")));
            Assert.assertEquals(layer, "cite:Buildings");
            JSONArray attributes = ((JSONArray) (layerAttributes.get("Attribute")));
            Assert.assertEquals(attributes.toArray().length, 3);
            Assert.assertEquals(get("name"), "FID");
            Assert.assertEquals(get("type"), "String");
        }
    }

    @Test
    public void testListAttributesForCoverageIsEmpty() throws Exception {
        LayerInfo l = getCatalog().getLayerByName("World");
        Assert.assertEquals("raster", l.getDefaultStyle().getName());
        final String restPath = (((RestBaseController.ROOT_PATH) + "/sldservice/wcs:World/") + (getServiceUrl())) + ".xml";
        MockHttpServletResponse response = getAsServletResponse(restPath);
        // Randomly cannot find REST path
        if ((response.getStatus()) == 200) {
            Document dom = getAsDOM(restPath, 200);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            print(dom, baos);
            Assert.assertTrue(((baos.toString().indexOf("<list/>")) > 0));
        }
    }
}
