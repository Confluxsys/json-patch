/**
 * 
 */
package com.github.fge.jsonpatch.diffcustom;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jackson.jsonpointer.JsonPointer;
import com.github.fge.jackson.jsonpointer.JsonPointerException;
import com.github.fge.jsonpatch.JsonDiffException;
import com.github.fge.jsonpatch.JsonPatchException;
import com.github.fge.jsonpatch.diff.JsonDiff;

/**
 * @author ruchika
 *
 */
public class TestDiffGenerator {
	private ObjectMapper objectMapper = new ObjectMapper();
	private Logger logger = LoggerFactory.getLogger(TestDiffGenerator.class);
	private JsonNode patch;
	private Map<JsonPointer, String> attributesKeyFeilds;

	@Test(priority = 1, description = "Missing Primary Key in source ", expectedExceptions = {
			JsonDiffException.class })
	public void missingPrimaryAtSource()
			throws  IOException, JsonPointerException, JsonPatchException {

		attributesKeyFeilds = new HashMap<JsonPointer, String>();
		attributesKeyFeilds.put(new JsonPointer("/Roles"), "ROLE");
		JsonNode oldJson = objectMapper.readTree(new File("src/test/resources/old.json"));
		JsonNode newJson = objectMapper.readTree(new File("src/test/resources/new.json"));

		patch = JsonDiff.asJson(oldJson, newJson, attributesKeyFeilds);
		logger.info("{}", patch.toString());
	}

	@Test(priority = 2, description = "Missing Primary Key at Destination", expectedExceptions = {
			JsonDiffException.class })
	public void missingPrimaryAtDestination()
			throws IOException, JsonPointerException, JsonPatchException{

		attributesKeyFeilds = new HashMap<JsonPointer, String>();
		attributesKeyFeilds.put(new JsonPointer("/Roles"), "ROLE");
		JsonNode oldJson = objectMapper.readTree(new File("src/test/resources/old.json"));
		JsonNode newJson = objectMapper.readTree(new File("src/test/resources/new.json"));

		patch = JsonDiff.asJson(newJson, oldJson, attributesKeyFeilds);
		logger.info("{}", patch.toString());

	}
}
