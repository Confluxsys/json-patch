package com.github.fge.jsonpatch.patchcustom;

import java.io.File;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.JsonPatchException;

public class TestAddOperations {

	private Logger logger = LoggerFactory.getLogger(this.getClass());
	private ObjectMapper objectMapper = new ObjectMapper();
	private JsonPatch patch;
	private JsonNode patched;

	private void evaluatePatch(JsonNode output, JsonNode expected) {
		logger.info("Output  : {}", output);
		logger.info("Expected: {}", expected);
		String out = output.toString();
		String exp = expected.toString();
		if (!out.equals(exp)) {
			Assert.fail("The Output of the Operations did not match the expected output");
		}

	}

	@Test(priority = 1)
	public void testAddFieldOperation() throws JsonProcessingException, IOException, JsonPatchException {
		Boolean performStrictValidation = false;

		JsonNode originalData = objectMapper
				.readTree(new File("src/test/resources/jsonpatch/patchcustom/SampleData.json"));
		JsonNode Operations = objectMapper
				.readTree(new File("src/test/resources/jsonpatch/patchcustom/addOperations1.json"));

		logger.info("Before Applying Patch: {}", originalData);
		logger.info("Operations: {}", Operations);
		patch = JsonPatch.fromJson(Operations);
		patched = patch.apply(originalData, performStrictValidation);

		JsonNode expectedOutput = objectMapper
				.readTree(new File("src/test/resources/jsonpatch/patchcustom/expected/expectedAdd1.json"));

		this.evaluatePatch(patched, expectedOutput);

	}

	@Test(priority = 2)
	public void testAddObjectOperation() throws JsonProcessingException, IOException, JsonPatchException {
		Boolean performStrictValidation = false;

		JsonNode originalData = objectMapper
				.readTree(new File("src/test/resources/jsonpatch/patchcustom/SampleData.json"));
		JsonNode Operations = objectMapper
				.readTree(new File("src/test/resources/jsonpatch/patchcustom/addOperations2.json"));

		logger.info("Before Applying Patch: {}", originalData);
		logger.info("Operations: {}", Operations);
		patch = JsonPatch.fromJson(Operations);
		patched = patch.apply(originalData, performStrictValidation);

		JsonNode expectedOutput = objectMapper
				.readTree(new File("src/test/resources/jsonpatch/patchcustom/expected/expectedAdd2.json"));

		this.evaluatePatch(patched, expectedOutput);
	}

	@Test(priority = 3)
	public void testAddCompleteArrayOperation() throws JsonProcessingException, IOException, JsonPatchException {
		Boolean performStrictValidation = false;

		JsonNode originalData = objectMapper
				.readTree(new File("src/test/resources/jsonpatch/patchcustom/SampleData.json"));
		JsonNode Operations = objectMapper
				.readTree(new File("src/test/resources/jsonpatch/patchcustom/addOperations3.json"));

		logger.info("Before Applying Patch: {}", originalData);
		logger.info("Operations: {}", Operations);
		patch = JsonPatch.fromJson(Operations);
		patched = patch.apply(originalData, performStrictValidation);

		JsonNode expectedOutput = objectMapper
				.readTree(new File("src/test/resources/jsonpatch/patchcustom/expected/expectedAdd3.json"));

		this.evaluatePatch(patched, expectedOutput);
	}

	@Test(priority = 4)
	public void testAddDuplicateObjectOperation() throws JsonProcessingException, IOException, JsonPatchException {
		// should replace value

		Boolean performStrictValidation = false;
		JsonNode originalData = objectMapper
				.readTree(new File("src/test/resources/jsonpatch/patchcustom/SampleData.json"));
		JsonNode Operations = objectMapper
				.readTree(new File("src/test/resources/jsonpatch/patchcustom/addOperations4.json"));

		logger.info("Before Applying Patch: {}", originalData);
		logger.info("Operations: {}", Operations);
		patch = JsonPatch.fromJson(Operations);
		patched = patch.apply(originalData, performStrictValidation);

		JsonNode expectedOutput = objectMapper
				.readTree(new File("src/test/resources/jsonpatch/patchcustom/expected/expectedAdd4.json"));

		this.evaluatePatch(patched, expectedOutput);

	}

	@Test(priority = 5)
	public void testAddDuplicateArrayObjectOperation() throws JsonProcessingException, IOException, JsonPatchException {
		// should be skipped

		Boolean performStrictValidation = false;

		JsonNode originalData = objectMapper
				.readTree(new File("src/test/resources/jsonpatch/patchcustom/SampleData.json"));
		JsonNode Operations = objectMapper
				.readTree(new File("src/test/resources/jsonpatch/patchcustom/addOperations5.json"));

		logger.info("Before Applying Patch: {}", originalData);
		logger.info("Operations: {}", Operations);
		patch = JsonPatch.fromJson(Operations);
		patched = patch.apply(originalData, performStrictValidation);

		JsonNode expectedOutput = objectMapper
				.readTree(new File("src/test/resources/jsonpatch/patchcustom/expected/expectedAdd5.json"));

		this.evaluatePatch(patched, expectedOutput);
	}

	@Test(priority = 6)
	public void testAddNonExistingPathOperation() throws JsonProcessingException, IOException, JsonPatchException {
		// Should create non-existing path
		Boolean performStrictValidation = false;

		JsonNode originalData = objectMapper
				.readTree(new File("src/test/resources/jsonpatch/patchcustom/SampleData.json"));
		JsonNode Operations = objectMapper
				.readTree(new File("src/test/resources/jsonpatch/patchcustom/addOperations6.json"));

		logger.info("Before Applying Patch: {}", originalData);
		patch = JsonPatch.fromJson(Operations);
		patched = patch.apply(originalData, performStrictValidation);
		logger.info("After Applying Patch:  {}", patched);

		JsonNode expectedOutput = objectMapper
				.readTree(new File("src/test/resources/jsonpatch/patchcustom/expected/expectedAdd6.json"));

		this.evaluatePatch(patched, expectedOutput);
	}

	@Test
	public void testSecenerioAddNonExisitngPath() throws JsonProcessingException, IOException, JsonPatchException {
		Boolean performStrictValidation = false;

		JsonNode originalData = objectMapper
				.readTree(new File("src/test/resources/jsonpatch/patchcustom/SampleData2.json"));
		JsonNode Operations = objectMapper
				.readTree(new File("src/test/resources/jsonpatch/patchcustom/addOperations7.json"));

		logger.info("Before Applying Patch: {}", originalData);
		patch = JsonPatch.fromJson(Operations);
		patched = patch.apply(originalData, performStrictValidation);
		logger.info("After Applying Patch:  {}", patched);

		// JsonNode expectedOutput = objectMapper
		// .readTree(new
		// File("src/test/resources/jsonpatch/patchcustom/expected/expectedAdd6.json"));
		//
		// this.evaluatePatch(patched, expectedOutput);
	}
}
