/*******************************************************************************
 * Copyright (C) 2023 the Eclipse BaSyx Authors
 * 
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 * 
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 * 
 * SPDX-License-Identifier: MIT
 ******************************************************************************/

package org.eclipse.digitaltwin.basyx.submodelrepository.http;

import static org.junit.Assert.assertEquals;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.core5.http.ParseException;
import org.eclipse.digitaltwin.basyx.submodelservice.DummySubmodelFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.HttpStatus;

/**
 * Tests the SubmodelElement specific parts of the SubmodelRepository HTTP/REST
 * API
 * 
 * @author schnicke
 *
 */
public class TestSubmodelRepositorySubmodelElementsHTTP {
	private String submodelAccessURL = "http://localhost:8080/submodels";

	private ConfigurableApplicationContext appContext;

	@Before
	public void startAASRepo() throws Exception {
		appContext = new SpringApplication(DummySubmodelRepositoryComponent.class).run(new String[] {});
	}

	@After
	public void shutdownAASRepo() {
		appContext.close();
	}

	@Test
	public void getSubmodelElements() throws FileNotFoundException, IOException, ParseException {
		String id = DummySubmodelFactory.SUBMODEL_TECHNICAL_DATA_ID;
		String requestedSubmodelElements = requestSubmodelElementsJSON(id);

		String submodelElementJSON = getSubmodelElementsJSON();
		SubmodelRepositoryHTTPTestUtils.assertSameJSONContent(submodelElementJSON, requestedSubmodelElements);
	}

	@Test
	public void getSubmodelElementsOfNonExistingSubmodel() throws ParseException, IOException {
		CloseableHttpResponse response = requestSubmodelElements("nonExisting");
		assertEquals(HttpStatus.NOT_FOUND.value(), response.getCode());
	}

	@Test
	public void getSubmodelElement() throws FileNotFoundException, IOException, ParseException {
		String expectedElement = getSubmodelElementJSON();
		CloseableHttpResponse response = requestSubmodelElement(DummySubmodelFactory.SUBMODEL_TECHNICAL_DATA_ID, DummySubmodelFactory.SUBMODEL_TECHNICAL_DATA_PROPERTY_ID_SHORT);

		assertEquals(HttpStatus.OK.value(), response.getCode());
		SubmodelRepositoryHTTPTestUtils.assertSameJSONContent(expectedElement, SubmodelRepositoryHTTPTestUtils.getResponseAsString(response));
	}

	@Test
	public void getSubmodelElementOfNonExistingSubmodel() throws FileNotFoundException, IOException, ParseException {
		CloseableHttpResponse response = requestSubmodelElement("nonExisting", "doesNotMatter");

		assertEquals(HttpStatus.NOT_FOUND.value(), response.getCode());
	}

	@Test
	public void getNonExistingSubmodelElement() throws FileNotFoundException, IOException, ParseException {
		CloseableHttpResponse response = requestSubmodelElement(DummySubmodelFactory.SUBMODEL_TECHNICAL_DATA_ID, "nonExisting");

		assertEquals(HttpStatus.NOT_FOUND.value(), response.getCode());
	}

	@Test
	public void getPropertyValue() throws IOException, ParseException {
		CloseableHttpResponse response = requestSubmodelElementValue(DummySubmodelFactory.SUBMODEL_TECHNICAL_DATA_ID, DummySubmodelFactory.SUBMODEL_TECHNICAL_DATA_PROPERTY_ID_SHORT);

		assertEquals(HttpStatus.OK.value(), response.getCode());

		String expectedElement = DummySubmodelFactory.SUBMODEL_TECHNICAL_DATA_PROPERTY_VALUE;

		SubmodelRepositoryHTTPTestUtils.assertSameJSONContent(expectedElement, SubmodelRepositoryHTTPTestUtils.getResponseAsString(response));
	}

	@Test
	public void getNonExistingSubmodelElementValue() throws IOException {
		CloseableHttpResponse response = requestSubmodelElementValue(DummySubmodelFactory.SUBMODEL_TECHNICAL_DATA_ID, "nonExisting");

		assertEquals(HttpStatus.NOT_FOUND.value(), response.getCode());
	}

	@Test
	public void getSubmodelElementValueOfNonExistingSubmodel() throws IOException {
		CloseableHttpResponse response = requestSubmodelElementValue("nonExisting", "doesNotMatter");

		assertEquals(HttpStatus.NOT_FOUND.value(), response.getCode());
	}

	@Test
	public void setPropertyValue() throws IOException, ParseException {
		String expected = "200";

		CloseableHttpResponse writeResponse = writeSubmodelElementValue(DummySubmodelFactory.SUBMODEL_TECHNICAL_DATA_ID, DummySubmodelFactory.SUBMODEL_TECHNICAL_DATA_PROPERTY_ID_SHORT, expected);
		assertEquals(HttpStatus.OK.value(), writeResponse.getCode());

		CloseableHttpResponse getResponse = requestSubmodelElementValue(DummySubmodelFactory.SUBMODEL_TECHNICAL_DATA_ID, DummySubmodelFactory.SUBMODEL_TECHNICAL_DATA_PROPERTY_ID_SHORT);
		assertEquals(expected, SubmodelRepositoryHTTPTestUtils.getResponseAsString(getResponse));
	}

	@Test
	public void setNonExistingSubmodelElementValue() throws IOException {
		CloseableHttpResponse response = writeSubmodelElementValue(DummySubmodelFactory.SUBMODEL_TECHNICAL_DATA_ID, "nonExisting", "doesNotMatter");
		assertEquals(HttpStatus.NOT_FOUND.value(), response.getCode());
	}

	@Test
	public void setSubmodelElementValueOfNonExistingSubmodel() throws IOException {
		CloseableHttpResponse response = writeSubmodelElementValue("nonExisting", "doesNotMatter", "doesNotMatter");

		assertEquals(HttpStatus.NOT_FOUND.value(), response.getCode());
	}

	private CloseableHttpResponse writeSubmodelElementValue(String submodelId, String smeIdShort, String value) throws IOException {
		return SubmodelRepositoryHTTPTestUtils.executeSetOnURL(createSubmodelElementValueURL(submodelId, smeIdShort), value);
	}

	private String createSubmodelElementValueURL(String submodelId, String smeIdShort) {
		return submodelAccessURL + "/" + submodelId + "/submodel/submodel-elements/" + smeIdShort + "?content=value";
	}

	private CloseableHttpResponse requestSubmodelElementValue(String submodelId, String smeIdShort) throws IOException {
		return SubmodelRepositoryHTTPTestUtils.executeGetOnURL(createSubmodelElementValueURL(submodelId, smeIdShort));

	}

	private String requestSubmodelElementsJSON(String id) throws IOException, ParseException {
		CloseableHttpResponse response = requestSubmodelElements(id);

		return SubmodelRepositoryHTTPTestUtils.getResponseAsString(response);
	}

	private CloseableHttpResponse requestSubmodelElement(String submodelId, String smeIdShort) throws IOException {
		return SubmodelRepositoryHTTPTestUtils.executeGetOnURL(createSpecificSubmodelElementURL(submodelId, smeIdShort));
	}

	private String createSpecificSubmodelElementURL(String submodelId, String smeIdShort) {
		return createSubmodelElementsURL(submodelId) + "/" + smeIdShort;
	}

	private CloseableHttpResponse requestSubmodelElements(String submodelId) throws IOException {
		return SubmodelRepositoryHTTPTestUtils.executeGetOnURL(createSubmodelElementsURL(submodelId));
	}

	private String createSubmodelElementsURL(String submodelId) {
		return submodelAccessURL + "/" + submodelId + "/submodel/submodel-elements";
	}

	private String getSubmodelElementsJSON() throws FileNotFoundException, IOException {
		return SubmodelRepositoryHTTPTestUtils.readJSONStringFromFile("classpath:SubmodelElements.json");
	}

	private String getSubmodelElementJSON() throws FileNotFoundException, IOException {
		return SubmodelRepositoryHTTPTestUtils.readJSONStringFromFile("classpath:SubmodelElement.json");
	}

}
