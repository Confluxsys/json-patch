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

public class TestRemoveOperations {

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
	public void testRemoveFieldOpeation() throws JsonProcessingException, IOException, JsonPatchException {

		Boolean performStrictValidation = false;

		JsonNode originalData = objectMapper
				.readTree(new File("src/test/resources/jsonpatch/patchcustom/SampleData.json"));
		JsonNode Operations = objectMapper
				.readTree(new File("src/test/resources/jsonpatch/patchcustom/removeOperations1.json"));

		logger.info("Before Applying Patch: {}", originalData);
		logger.info("Operations: {}", Operations);
		patch = JsonPatch.fromJson(Operations);
		patched = patch.apply(originalData, performStrictValidation);

		JsonNode expectedOutput = objectMapper
				.readTree(new File("src/test/resources/jsonpatch/patchcustom/expected/expectedRemove1.json"));

		this.evaluatePatch(patched, expectedOutput);
	}

	@Test(priority = 2)
	public void testRemoveObjectOpeation() throws JsonProcessingException, IOException, JsonPatchException {
		Boolean performStrictValidation = false;

		JsonNode originalData = objectMapper
				.readTree(new File("src/test/resources/jsonpatch/patchcustom/SampleData.json"));
		JsonNode Operations = objectMapper
				.readTree(new File("src/test/resources/jsonpatch/patchcustom/removeOperations2.json"));

		logger.info("Before Applying Patch: {}", originalData);
		logger.info("Operations: {}", Operations);
		patch = JsonPatch.fromJson(Operations);
		patched = patch.apply(originalData, performStrictValidation);

		JsonNode expectedOutput = objectMapper
				.readTree(new File("src/test/resources/jsonpatch/patchcustom/expected/expectedRemove2.json"));

		this.evaluatePatch(patched, expectedOutput);
	}

	@Test(priority = 3)
	public void testRemoveCompleteArrayOpeation() throws JsonProcessingException, IOException, JsonPatchException {
		Boolean performStrictValidation = false;

		JsonNode originalData = objectMapper
				.readTree(new File("src/test/resources/jsonpatch/patchcustom/SampleData.json"));
		JsonNode Operations = objectMapper
				.readTree(new File("src/test/resources/jsonpatch/patchcustom/removeOperations3.json"));

		logger.info("Before Applying Patch: {}", originalData);
		logger.info("Operations: {}", Operations);
		patch = JsonPatch.fromJson(Operations);
		patched = patch.apply(originalData, performStrictValidation);

		JsonNode expectedOutput = objectMapper
				.readTree(new File("src/test/resources/jsonpatch/patchcustom/expected/expectedRemove3.json"));

		this.evaluatePatch(patched, expectedOutput);
	}

	@Test(priority = 2)
	public void testRemoveDuplicateObjectOpeation() throws JsonProcessingException, IOException, JsonPatchException {
		Boolean performStrictValidation = false;

		JsonNode originalData = objectMapper
				.readTree(new File("src/test/resources/jsonpatch/patchcustom/SampleData.json"));
		JsonNode Operations = objectMapper
				.readTree(new File("src/test/resources/jsonpatch/patchcustom/removeOperations2.json"));

		logger.info("Before Applying Patch: {}", originalData);
		logger.info("Operations: {}", Operations);
		patch = JsonPatch.fromJson(Operations);
		patched = patch.apply(originalData, performStrictValidation);

		JsonNode expectedOutput = objectMapper
				.readTree(new File("src/test/resources/jsonpatch/patchcustom/expected/expectedRemove2.json"));
		this.evaluatePatch(patched, expectedOutput);
		JsonNode jsonPatch = patch.apply(patched, performStrictValidation);

		this.evaluatePatch(jsonPatch, expectedOutput);
	}

}
