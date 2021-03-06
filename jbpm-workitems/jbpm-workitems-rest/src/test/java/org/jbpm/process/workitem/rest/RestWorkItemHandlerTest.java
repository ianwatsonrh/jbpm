/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jbpm.process.workitem.rest;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import javax.ws.rs.ext.RuntimeDelegate;

import org.apache.cxf.endpoint.Server;
import org.apache.cxf.jaxrs.JAXRSServerFactoryBean;
import org.apache.cxf.jaxrs.provider.JAXBElementProvider;
import org.drools.core.process.instance.impl.WorkItemImpl;
import org.jbpm.bpmn2.handler.WorkItemHandlerRuntimeException;
import org.jbpm.process.workitem.core.TestWorkItemManager;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.kie.api.runtime.process.WorkItemManager;

import static org.junit.Assert.*;

@RunWith(Parameterized.class)
public class RestWorkItemHandlerTest {

    @Parameters(name = "Http Client 4.3 api = {0}")
    public static Collection<Object[]> parameters() {
        Object[][] locking = new Object[][]{
                {true},
                {false},
        };
        return Arrays.asList(locking);
    }

    private final boolean httpClient43;

    private final static String serverURL = "http://localhost:9998/test";
    private static Server server;

    public RestWorkItemHandlerTest(boolean httpClient43) {
        this.httpClient43 = httpClient43;
    }

    @SuppressWarnings({"rawtypes"})
    @BeforeClass
    public static void initialize() throws Exception {

        SimpleRESTApplication application = new SimpleRESTApplication();
        RuntimeDelegate delegate = RuntimeDelegate.getInstance();

        JAXRSServerFactoryBean bean = delegate.createEndpoint(application,
                                                              JAXRSServerFactoryBean.class);
        bean.setProvider(new JAXBElementProvider());
        bean.setAddress("http://localhost:9998" + bean.getAddress());
        server = bean.create();
        server.start();
    }

    @AfterClass
    public static void destroy() throws Exception {
        if (server != null) {
            server.stop();
            server.destroy();
        }
    }

    @Before
    public void setClientApiVersion() {
        RESTWorkItemHandler.HTTP_CLIENT_API_43 = httpClient43;
    }

    @Test
    public void testGETOperation() {
        RESTWorkItemHandler handler = new RESTWorkItemHandler();

        WorkItemImpl workItem = new WorkItemImpl();
        workItem.setParameter("Url",
                              serverURL);
        workItem.setParameter("Method",
                              "GET");

        WorkItemManager manager = new TestWorkItemManager();
        handler.executeWorkItem(workItem,
                                manager);

        Map<String, Object> results = ((TestWorkItemManager) manager).getResults(workItem.getId());
        assertNotNull("results cannot be null", results);
        String result = (String) results.get("Result");
        assertNotNull("result cannot be null",
                      result);
        assertEquals("Hello from REST",
                     result);
        int responseCode = (Integer) results.get("Status");
        assertNotNull(responseCode);
        assertEquals(200,
                     responseCode);
        String responseMsg = (String) results.get("StatusMsg");
        assertNotNull(responseMsg);
        assertEquals("request to endpoint " + workItem.getParameter("Url") + " successfully completed OK",
                     responseMsg);
    }

    @Test
    public void testGETOperationWithJSONHeader() {
        RESTWorkItemHandler handler = new RESTWorkItemHandler();

        WorkItemImpl workItem = new WorkItemImpl();
        workItem.setParameter("Url",
                              serverURL);
        workItem.setParameter("Method",
                              "GET");
        workItem.setParameter("AcceptHeader", "application/json");

        WorkItemManager manager = new TestWorkItemManager();
        handler.executeWorkItem(workItem,
                                manager);

        Map<String, Object> results = ((TestWorkItemManager) manager).getResults(workItem.getId());
        int responseCode = (Integer) results.get("Status");
        assertNotNull(responseCode);
        assertEquals(406,
                     responseCode);
        String responseMsg = (String) results.get("StatusMsg");
        assertNotNull(responseMsg);
        assertEquals("endpoint " + serverURL + " could not be reached: ",
                     responseMsg);
    }

    @Test
    public void testGETOperationWithXMLHeader() {
        RESTWorkItemHandler handler = new RESTWorkItemHandler();

        WorkItemImpl workItem = new WorkItemImpl();
        workItem.setParameter("Url",
                              serverURL);
        workItem.setParameter("Method",
                              "GET");
        workItem.setParameter("AcceptHeader", "application/xml");

        WorkItemManager manager = new TestWorkItemManager();
        handler.executeWorkItem(workItem,
                                manager);

        Map<String, Object> results = ((TestWorkItemManager) manager).getResults(workItem.getId());
        int responseCode = (Integer) results.get("Status");
        assertNotNull(responseCode);
        assertEquals(406,
                     responseCode);
        String responseMsg = (String) results.get("StatusMsg");
        assertNotNull(responseMsg);
        assertEquals("endpoint " + serverURL + " could not be reached: ",
                     responseMsg);
    }

    @Test
    public void testGETOperationWithTextHeader() {
        RESTWorkItemHandler handler = new RESTWorkItemHandler();

        WorkItemImpl workItem = new WorkItemImpl();
        workItem.setParameter("Url",
                              serverURL);
        workItem.setParameter("Method",
                              "GET");
        workItem.setParameter("AcceptHeader", "text/plain");

        WorkItemManager manager = new TestWorkItemManager();
        handler.executeWorkItem(workItem,
                                manager);

        Map<String, Object> results = ((TestWorkItemManager) manager).getResults(workItem.getId());
        int responseCode = (Integer) results.get("Status");
        assertNotNull(responseCode);
        assertEquals(200,
                     responseCode);
        String responseMsg = (String) results.get("StatusMsg");
        assertNotNull(responseMsg);
        assertEquals("request to endpoint " + workItem.getParameter("Url") + " successfully completed OK",
                     responseMsg);
    }

    @Test
    public void testGETOperationWithCustomTimeout() {
        RESTWorkItemHandler handler = new RESTWorkItemHandler();

        WorkItemImpl workItem = new WorkItemImpl();
        workItem.setParameter("Url",
                              serverURL);
        workItem.setParameter("Method",
                              "GET");
        workItem.setParameter("ConnectTimeout",
                              "30000");
        workItem.setParameter("ReadTimeout",
                              "25000");

        WorkItemManager manager = new TestWorkItemManager();
        handler.executeWorkItem(workItem,
                                manager);

        Map<String, Object> results = ((TestWorkItemManager) manager).getResults(workItem.getId());
        String result = (String) results.get("Result");
        assertNotNull("result cannot be null",
                      result);
        assertEquals("Hello from REST",
                     result);
        int responseCode = (Integer) results.get("Status");
        assertNotNull(responseCode);
        assertEquals(200,
                     responseCode);
        String responseMsg = (String) results.get("StatusMsg");
        assertNotNull(responseMsg);
        assertEquals("request to endpoint " + workItem.getParameter("Url") + " successfully completed OK",
                     responseMsg);
    }

    @Test
    public void testGETOperationWithInvalidTimeout() {
        RESTWorkItemHandler handler = new RESTWorkItemHandler();

        WorkItemImpl workItem = new WorkItemImpl();
        workItem.setParameter("Url",
                              serverURL);
        workItem.setParameter("Method",
                              "GET");
        workItem.setParameter("ConnectTimeout",
                              "");
        workItem.setParameter("ReadTimeout",
                              "");

        WorkItemManager manager = new TestWorkItemManager();
        handler.executeWorkItem(workItem,
                                manager);

        Map<String, Object> results = ((TestWorkItemManager) manager).getResults(workItem.getId());
        String result = (String) results.get("Result");
        assertNotNull("result cannot be null",
                      result);
        assertEquals("Hello from REST",
                     result);
        int responseCode = (Integer) results.get("Status");
        assertNotNull(responseCode);
        assertEquals(200,
                     responseCode);
        String responseMsg = (String) results.get("StatusMsg");
        assertNotNull(responseMsg);
        assertEquals("request to endpoint " + workItem.getParameter("Url") + " successfully completed OK",
                     responseMsg);
    }

    @Test
    public void testGETOperationWithQueryParam() {
        RESTWorkItemHandler handler = new RESTWorkItemHandler();

        WorkItemImpl workItem = new WorkItemImpl();
        workItem.setParameter("Url",
                              serverURL + "?param=test");
        workItem.setParameter("Method",
                              "GET");

        WorkItemManager manager = new TestWorkItemManager();
        handler.executeWorkItem(workItem,
                                manager);

        Map<String, Object> results = ((TestWorkItemManager) manager).getResults(workItem.getId());
        String result = (String) results.get("Result");
        assertNotNull("result cannot be null",
                      result);
        assertEquals("Hello from REST test",
                     result);
        int responseCode = (Integer) results.get("Status");
        assertNotNull(responseCode);
        assertEquals(200,
                     responseCode);
        String responseMsg = (String) results.get("StatusMsg");
        assertNotNull(responseMsg);
        assertEquals("request to endpoint " + workItem.getParameter("Url") + " successfully completed OK",
                     responseMsg);
    }

    @Test
    public void testPOSTOperation() {
        RESTWorkItemHandler handler = new RESTWorkItemHandler();
        String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" +
                "<person><age>25</age><name>Post john</name></person>";

        WorkItemImpl workItem = new WorkItemImpl();
        workItem.setParameter("Url",
                              serverURL + "/xml");
        workItem.setParameter("Method",
                              "POST");
        workItem.setParameter("ContentType",
                              "application/xml");
        workItem.setParameter("Content",
                              "<person><name>john</name><age>25</age></person>");

        WorkItemManager manager = new TestWorkItemManager();
        handler.executeWorkItem(workItem,
                                manager);

        Map<String, Object> results = ((TestWorkItemManager) manager).getResults(workItem.getId());
        String result = (String) results.get("Result");
        assertNotNull("result cannot be null",
                      result);
        assertEquals(expected,
                     result);
        int responseCode = (Integer) results.get("Status");
        assertNotNull(responseCode);
        assertEquals(200,
                     responseCode);
        String responseMsg = (String) results.get("StatusMsg");
        assertNotNull(responseMsg);
        assertEquals("request to endpoint " + workItem.getParameter("Url") + " successfully completed OK",
                     responseMsg);
    }

    @Test
    public void testPOSTOperationWthJSONHeader() {
        RESTWorkItemHandler handler = new RESTWorkItemHandler();
        String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" +
                "<person><age>25</age><name>Post john</name></person>";

        WorkItemImpl workItem = new WorkItemImpl();
        workItem.setParameter("Url",
                              serverURL + "/xml");
        workItem.setParameter("Method",
                              "POST");
        workItem.setParameter("ContentType",
                              "application/xml");
        workItem.setParameter("Content",
                              "<person><name>john</name><age>25</age></person>");

        workItem.setParameter("AcceptHeader", "application/json");

        WorkItemManager manager = new TestWorkItemManager();
        handler.executeWorkItem(workItem,
                                manager);

        Map<String, Object> results = ((TestWorkItemManager) manager).getResults(workItem.getId());
        int responseCode = (Integer) results.get("Status");
        assertNotNull(responseCode);
        assertEquals(406,
                     responseCode);
        String responseMsg = (String) results.get("StatusMsg");
        assertNotNull(responseMsg);
        assertEquals("endpoint " + serverURL + "/xml could not be reached: ",
                     responseMsg);
    }

    @Test
    public void testPOSTOperationWithXMLHeader() {
        RESTWorkItemHandler handler = new RESTWorkItemHandler();
        String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" +
                "<person><age>25</age><name>Post john</name></person>";

        WorkItemImpl workItem = new WorkItemImpl();
        workItem.setParameter("Url",
                              serverURL + "/xml");
        workItem.setParameter("Method",
                              "POST");
        workItem.setParameter("ContentType",
                              "application/xml");
        workItem.setParameter("Content",
                              "<person><name>john</name><age>25</age></person>");
        workItem.setParameter("AcceptHeader", "application/xml");

        WorkItemManager manager = new TestWorkItemManager();
        handler.executeWorkItem(workItem,
                                manager);

        Map<String, Object> results = ((TestWorkItemManager) manager).getResults(workItem.getId());
        String result = (String) results.get("Result");
        assertNotNull("result cannot be null",
                      result);
        assertEquals(expected,
                     result);
        int responseCode = (Integer) results.get("Status");
        assertNotNull(responseCode);
        assertEquals(200,
                     responseCode);
        String responseMsg = (String) results.get("StatusMsg");
        assertNotNull(responseMsg);
        assertEquals("request to endpoint " + workItem.getParameter("Url") + " successfully completed OK",
                     responseMsg);
    }


    @Test
    public void testPOSTOperationWithPathParamAndNoContent() {
        RESTWorkItemHandler handler = new RESTWorkItemHandler();

        WorkItemImpl workItem = new WorkItemImpl();
        workItem.setParameter("Url",
                              serverURL + "/john");
        workItem.setParameter("Method",
                              "POST");

        WorkItemManager manager = new TestWorkItemManager();
        handler.executeWorkItem(workItem,
                                manager);

        Map<String, Object> results = ((TestWorkItemManager) manager).getResults(workItem.getId());
        String result = (String) results.get("Result");
        assertNotNull("result cannot be null",
                      result);
        assertEquals("Created resource with name john",
                     result);
        int responseCode = (Integer) results.get("Status");
        assertNotNull(responseCode);
        assertEquals(200,
                     responseCode);
        String responseMsg = (String) results.get("StatusMsg");
        assertNotNull(responseMsg);
        assertEquals("request to endpoint " + workItem.getParameter("Url") + " successfully completed OK",
                     responseMsg);
    }

    @Test
    public void testPUTOperation() {
        RESTWorkItemHandler handler = new RESTWorkItemHandler();
        String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" +
                "<person><age>25</age><name>Put john</name></person>";

        WorkItemImpl workItem = new WorkItemImpl();
        workItem.setParameter("Url",
                              serverURL + "/xml");
        workItem.setParameter("Method",
                              "PUT");
        workItem.setParameter("ContentType",
                              "application/xml");
        workItem.setParameter("Content",
                              "<person><name>john</name><age>25</age></person>");

        WorkItemManager manager = new TestWorkItemManager();
        handler.executeWorkItem(workItem,
                                manager);

        Map<String, Object> results = ((TestWorkItemManager) manager).getResults(workItem.getId());
        String result = (String) results.get("Result");
        assertNotNull("result cannot be null",
                      result);
        assertEquals(expected,
                     result);
        int responseCode = (Integer) results.get("Status");
        assertNotNull(responseCode);
        assertEquals(200,
                     responseCode);
        String responseMsg = (String) results.get("StatusMsg");
        assertNotNull(responseMsg);
        assertEquals("request to endpoint " + workItem.getParameter("Url") + " successfully completed OK",
                     responseMsg);
    }

    @Test
    public void testPUTOperationWithXMLHeader() {
        RESTWorkItemHandler handler = new RESTWorkItemHandler();
        String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" +
                "<person><age>25</age><name>Put john</name></person>";

        WorkItemImpl workItem = new WorkItemImpl();
        workItem.setParameter("Url",
                              serverURL + "/xml");
        workItem.setParameter("Method",
                              "PUT");
        workItem.setParameter("ContentType",
                              "application/xml");
        workItem.setParameter("Content",
                              "<person><name>john</name><age>25</age></person>");
        workItem.setParameter("AcceptHeader", "application/xml");

        WorkItemManager manager = new TestWorkItemManager();
        handler.executeWorkItem(workItem,
                                manager);

        Map<String, Object> results = ((TestWorkItemManager) manager).getResults(workItem.getId());
        String result = (String) results.get("Result");
        assertNotNull("result cannot be null",
                      result);
        assertEquals(expected,
                     result);
        int responseCode = (Integer) results.get("Status");
        assertNotNull(responseCode);
        assertEquals(200,
                     responseCode);
        String responseMsg = (String) results.get("StatusMsg");
        assertNotNull(responseMsg);
        assertEquals("request to endpoint " + workItem.getParameter("Url") + " successfully completed OK",
                     responseMsg);
    }

    @Test
    public void testPUTOperationWithJSONHeader() {
        RESTWorkItemHandler handler = new RESTWorkItemHandler();
        String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" +
                "<person><age>25</age><name>Put john</name></person>";

        WorkItemImpl workItem = new WorkItemImpl();
        workItem.setParameter("Url",
                              serverURL + "/xml");
        workItem.setParameter("Method",
                              "PUT");
        workItem.setParameter("ContentType",
                              "application/xml");
        workItem.setParameter("Content",
                              "<person><name>john</name><age>25</age></person>");
        workItem.setParameter("AcceptHeader", "application/json");

        WorkItemManager manager = new TestWorkItemManager();
        handler.executeWorkItem(workItem,
                                manager);

        Map<String, Object> results = ((TestWorkItemManager) manager).getResults(workItem.getId());
        int responseCode = (Integer) results.get("Status");
        assertNotNull(responseCode);
        assertEquals(406,
                     responseCode);
        String responseMsg = (String) results.get("StatusMsg");
        assertNotNull(responseMsg);
        assertEquals("endpoint " + serverURL + "/xml could not be reached: ",
                     responseMsg);
    }

    @Test
    public void testDELETEOperation() {
        RESTWorkItemHandler handler = new RESTWorkItemHandler();
        String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" +
                "<person><age>-1</age><name>deleted john</name></person>";

        WorkItemImpl workItem = new WorkItemImpl();
        workItem.setParameter("Url",
                              serverURL + "/xml/john");
        workItem.setParameter("Method",
                              "DELETE");

        WorkItemManager manager = new TestWorkItemManager();
        handler.executeWorkItem(workItem,
                                manager);

        Map<String, Object> results = ((TestWorkItemManager) manager).getResults(workItem.getId());
        String result = (String) results.get("Result");
        assertNotNull("result cannot be null",
                      result);
        assertEquals(expected,
                     result);
        int responseCode = (Integer) results.get("Status");
        assertNotNull(responseCode);
        assertEquals(200,
                     responseCode);
        String responseMsg = (String) results.get("StatusMsg");
        assertNotNull(responseMsg);
        assertEquals("request to endpoint " + workItem.getParameter("Url") + " successfully completed OK",
                     responseMsg);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUnsupportedOperation() {
        RESTWorkItemHandler handler = new RESTWorkItemHandler();

        WorkItemImpl workItem = new WorkItemImpl();
        workItem.setParameter("Url",
                              serverURL + "/xml/john");
        workItem.setParameter("Method",
                              "HEAD");

        WorkItemManager manager = new TestWorkItemManager();
        handler.executeWorkItem(workItem,
                                manager);
    }

    @Test
    public void testHandleErrorOnNotSuccessfulResponse() {
        RESTWorkItemHandler handler = new RESTWorkItemHandler();

        WorkItemImpl workItem = new WorkItemImpl();
        workItem.setParameter("Url",
                              serverURL + "/notexisting");
        workItem.setParameter("Method",
                              "GET");
        workItem.setParameter("HandleResponseErrors",
                              "true");

        WorkItemManager manager = new TestWorkItemManager();
        try {
            handler.executeWorkItem(workItem,
                                    manager);
            fail("Should throw exception as it was instructed to do so");
        } catch (WorkItemHandlerRuntimeException ex) {

            RESTServiceException e = (RESTServiceException) ex.getCause().getCause();
            assertEquals(405,
                         e.getStatus());
            assertEquals(serverURL + "/notexisting",
                         e.getEndoint());
            assertEquals("",
                         e.getResponse());
        }
    }

    @Test
    public void testGETOperationWithXmlTrasformation() {
        RESTWorkItemHandler handler = new RESTWorkItemHandler();

        WorkItemImpl workItem = new WorkItemImpl();
        workItem.setParameter("Url",
                              serverURL + "/xml");
        workItem.setParameter("Method",
                              "GET");
        workItem.setParameter("ResultClass",
                              Person.class.getName());

        WorkItemManager manager = new TestWorkItemManager();
        handler.executeWorkItem(workItem,
                                manager);

        Map<String, Object> results = ((TestWorkItemManager) manager).getResults(workItem.getId());
        Person result = (Person) results.get("Result");
        assertNotNull("result cannot be null",
                      result);
        assertEquals("Person Xml",
                     result.getName());
        int responseCode = (Integer) results.get("Status");
        assertNotNull(responseCode);
        assertEquals(200,
                     responseCode);
        String responseMsg = (String) results.get("StatusMsg");
        assertNotNull(responseMsg);
        assertEquals("request to endpoint " + workItem.getParameter("Url") + " successfully completed OK",
                     responseMsg);
    }

    @Test
    public void testGETOperationWithJsonTrasformation() {
        RESTWorkItemHandler handler = new RESTWorkItemHandler();

        WorkItemImpl workItem = new WorkItemImpl();
        workItem.setParameter("Url",
                              serverURL + "/json");
        workItem.setParameter("Method",
                              "GET");
        workItem.setParameter("ResultClass",
                              Person.class.getName());

        WorkItemManager manager = new TestWorkItemManager();
        handler.executeWorkItem(workItem,
                                manager);

        Map<String, Object> results = ((TestWorkItemManager) manager).getResults(workItem.getId());
        Person result = (Person) results.get("Result");
        assertNotNull("result cannot be null",
                      result);
        assertEquals("Person Json",
                     result.getName());
        int responseCode = (Integer) results.get("Status");
        assertNotNull(responseCode);
        assertEquals(200,
                     responseCode);
        String responseMsg = (String)results.get("StatusMsg");
        assertNotNull(responseMsg);
        assertEquals("request to endpoint " + workItem.getParameter("Url") + " successfully completed OK",
                     responseMsg);
    }

    @Test
    public void testPOSTOperationWithXmlTransformation() {
        RESTWorkItemHandler handler = new RESTWorkItemHandler();

        WorkItemImpl workItem = new WorkItemImpl();
        workItem.setParameter("Url",
                              serverURL + "/xml");
        workItem.setParameter("Method",
                              "POST");
        workItem.setParameter("ContentType",
                              "Application/XML;charset=utf-8");
        workItem.setParameter("Content",
                              "<person><name>john</name><age>25</age></person>");
        workItem.setParameter("ResultClass",
                              Person.class.getName());

        WorkItemManager manager = new TestWorkItemManager();
        handler.executeWorkItem(workItem,
                                manager);

        Map<String, Object> results = ((TestWorkItemManager) manager).getResults(workItem.getId());
        Person result = (Person) results.get("Result");
        assertNotNull("result cannot be null",
                      result);
        assertEquals("Post john",
                     result.getName());
        assertEquals(25,
                     result.getAge().intValue());
        int responseCode = (Integer) results.get("Status");
        assertNotNull(responseCode);
        assertEquals(200,
                     responseCode);
        String responseMsg = (String) results.get("StatusMsg");
        assertNotNull(responseMsg);
        assertEquals("request to endpoint " + workItem.getParameter("Url") + " successfully completed OK",
                     responseMsg);
    }

    @Test
    public void testPUTOperationWithXmlTransformation() {
        RESTWorkItemHandler handler = new RESTWorkItemHandler();

        WorkItemImpl workItem = new WorkItemImpl();
        workItem.setParameter("Url",
                              serverURL + "/xml");
        workItem.setParameter("Method",
                              "PUT");
        workItem.setParameter("ContentType",
                              "Application/Xml;charset=utf-8");
        workItem.setParameter("Content",
                              "<person><name>john</name><age>25</age></person>");
        workItem.setParameter("ResultClass",
                              Person.class.getName());

        WorkItemManager manager = new TestWorkItemManager();
        handler.executeWorkItem(workItem,
                                manager);

        Map<String, Object> results = ((TestWorkItemManager) manager).getResults(workItem.getId());
        Person result = (Person) results.get("Result");
        assertNotNull("result cannot be null",
                      result);
        assertEquals("Put john",
                     result.getName());
        assertEquals(25,
                     result.getAge().intValue());
        int responseCode = (Integer) results.get("Status");
        assertNotNull(responseCode);
        assertEquals(200,
                     responseCode);
        String responseMsg = (String) results.get("StatusMsg");
        assertNotNull(responseMsg);
        assertEquals("request to endpoint " + workItem.getParameter("Url") + " successfully completed OK",
                     responseMsg);
    }

    @Test
    public void testPOSTOperationWithCompleteXmlTransformation() {
        RESTWorkItemHandler handler = new RESTWorkItemHandler();

        Person request = new Person();
        request.setAge(25);
        request.setName("john");

        WorkItemImpl workItem = new WorkItemImpl();
        workItem.setParameter("Url",
                              serverURL + "/xml");
        workItem.setParameter("Method",
                              "POST");
        workItem.setParameter("ContentType",
                              "application/xml");
        workItem.setParameter("Content",
                              request);
        workItem.setParameter("ResultClass",
                              Person.class.getName());

        WorkItemManager manager = new TestWorkItemManager();
        handler.executeWorkItem(workItem,
                                manager);

        Map<String, Object> results = ((TestWorkItemManager) manager).getResults(workItem.getId());
        Person result = (Person) results.get("Result");
        assertNotNull("result cannot be null",
                      result);
        assertEquals("Post john",
                     result.getName());
        assertEquals(25,
                     result.getAge().intValue());
        int responseCode = (Integer) results.get("Status");
        assertNotNull(responseCode);
        assertEquals(200,
                     responseCode);
        String responseMsg = (String) results.get("StatusMsg");
        assertNotNull(responseMsg);
        assertEquals("request to endpoint " + workItem.getParameter("Url") + " successfully completed OK",
                     responseMsg);
    }

    @Test
    public void testGETOperationWithXmlCharsetTrasformation() {
        RESTWorkItemHandler handler = new RESTWorkItemHandler();

        WorkItemImpl workItem = new WorkItemImpl();
        workItem.setParameter("Url",
                              serverURL + "/xml-charset");
        workItem.setParameter("Method",
                              "GET");
        workItem.setParameter("ResultClass",
                              Person.class.getName());

        WorkItemManager manager = new TestWorkItemManager();
        handler.executeWorkItem(workItem,
                                manager);

        Map<String, Object> results = ((TestWorkItemManager) manager).getResults(workItem.getId());
        Person result = (Person) results.get("Result");
        assertNotNull("result cannot be null",
                      result);
        assertEquals("Person Xml",
                     result.getName());
        int responseCode = (Integer) results.get("Status");
        assertNotNull(responseCode);
        assertEquals(200,
                     responseCode);
        String responseMsg = (String) results.get("StatusMsg");
        assertNotNull(responseMsg);
        assertEquals("request to endpoint " + workItem.getParameter("Url") + " successfully completed OK",
                     responseMsg);
    }

    @Test
    public void testGETOperationWithJsonCharsetTrasformation() {
        RESTWorkItemHandler handler = new RESTWorkItemHandler();

        WorkItemImpl workItem = new WorkItemImpl();
        workItem.setParameter("Url",
                              serverURL + "/json-charset");
        workItem.setParameter("Method",
                              "GET");
        workItem.setParameter("ResultClass",
                              Person.class.getName());

        WorkItemManager manager = new TestWorkItemManager();
        handler.executeWorkItem(workItem,
                                manager);

        Map<String, Object> results = ((TestWorkItemManager) manager).getResults(workItem.getId());
        Person result = (Person) results.get("Result");
        assertNotNull("result cannot be null",
                      result);
        assertEquals("Person Json",
                     result.getName());
        int responseCode = (Integer) results.get("Status");
        assertNotNull(responseCode);
        assertEquals(200,
                     responseCode);
        String responseMsg = (String) results.get("StatusMsg");
        assertNotNull(responseMsg);
        assertEquals("request to endpoint " + workItem.getParameter("Url") + " successfully completed OK",
                     responseMsg);
    }

    @Test
    public void testPUTOperationWithDefaultCharset() {
        RESTWorkItemHandler handler = new RESTWorkItemHandler();
        String nonAsciiData = "\u0418\u0432\u0430\u043d";
        String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" +
                "<person><age>25</age><name>Put ????</name></person>";

        WorkItemImpl workItem = new WorkItemImpl();
        workItem.setParameter("Url",
                              serverURL + "/xml-charset");
        workItem.setParameter("Method",
                              "PUT");
        workItem.setParameter("ContentType",
                              "application/xml");
        workItem.setParameter("Content",
                              "<person><name>" + nonAsciiData + "</name><age>25</age></person>");

        WorkItemManager manager = new TestWorkItemManager();
        handler.executeWorkItem(workItem,
                                manager);

        Map<String, Object> results = ((TestWorkItemManager) manager).getResults(workItem.getId());
        String result = (String) results.get("Result");
        assertNotNull("result cannot be null",
                      result);
        assertEquals(expected,
                     result);
        int responseCode = (Integer) results.get("Status");
        assertNotNull(responseCode);
        assertEquals(200,
                     responseCode);
        String responseMsg = (String) results.get("StatusMsg");
        assertNotNull(responseMsg);
        assertEquals("request to endpoint " + workItem.getParameter("Url") + " successfully completed OK",
                     responseMsg);
    }

    @Test

    public void testPUTOperationWithCharsetSpecified() {
        RESTWorkItemHandler handler = new RESTWorkItemHandler();
        String nonAsciiData = "\u0418\u0432\u0430\u043d";
        String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" +
                "<person><age>25</age><name>Put " + nonAsciiData + "</name></person>";

        WorkItemImpl workItem = new WorkItemImpl();
        workItem.setParameter("Url",
                              serverURL + "/xml-charset");
        workItem.setParameter("Method",
                              "PUT");
        workItem.setParameter("ContentType",
                              "application/xml; charset=utf-8");
        workItem.setParameter("Content",
                              "<person><name>" + nonAsciiData + "</name><age>25</age></person>");

        WorkItemManager manager = new TestWorkItemManager();
        handler.executeWorkItem(workItem,
                                manager);

        Map<String, Object> results = ((TestWorkItemManager) manager).getResults(workItem.getId());
        String result = (String) results.get("Result");
        assertNotNull("result cannot be null",
                      result);
        assertEquals(expected,
                     result);
        int responseCode = (Integer) results.get("Status");
        assertNotNull(responseCode);
        assertEquals(200,
                     responseCode);
        String responseMsg = (String) results.get("StatusMsg");
        assertNotNull(responseMsg);
        assertEquals("request to endpoint " + workItem.getParameter("Url") + " successfully completed OK",
                     responseMsg);
    }
}