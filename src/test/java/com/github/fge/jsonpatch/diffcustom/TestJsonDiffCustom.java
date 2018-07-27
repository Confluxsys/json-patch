/**
 * 
 */
package com.github.fge.jsonpatch.diffcustom;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.fge.jackson.jsonpointer.JsonPointer;
import com.github.fge.jackson.jsonpointer.JsonPointerException;
import com.github.fge.jsonpatch.JsonDiffConstants;
import com.github.fge.jsonpatch.JsonDiffException;
import com.github.fge.jsonpatch.diff.JsonDiff;
import com.google.common.collect.Lists;

/**
 * @author Ritesh
 *
 */
public class TestJsonDiffCustom {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	private JsonNode patches;
	private Map<JsonPointer, String> attributesKeyFeilds;
	@BeforeTest
	public void initialize() throws JsonPointerException {
				
		attributesKeyFeilds = new HashMap<>();
		attributesKeyFeilds.put(new JsonPointer("/Profiles"), "Profile");
		attributesKeyFeilds.put(new JsonPointer("/Groups"), "Group");
		attributesKeyFeilds.put(new JsonPointer("/Roles"), "Role");
		attributesKeyFeilds.put(new JsonPointer("/User Licenses"), "License");
		attributesKeyFeilds.put(new JsonPointer("/IT Resource"), null);
		attributesKeyFeilds.put(new JsonPointer("/Grouppp"), null); 
		//NULL as Key Should return REMOVE AND ADD instead of REPLACE Element itself as KEY
		
		attributesKeyFeilds.put(new JsonPointer("/a"), "a");
	}

	@Test(dataProvider = "Array Operation", dataProviderClass = JsonDataProvider.class)
	public void testArrayOperation(JsonNode oldState, JsonNode newState) throws JsonPointerException, JsonDiffException {

		patches = JsonDiff.asJson(oldState, newState, attributesKeyFeilds);
		logger.info("{}", patches.toString());

		// Testing the Truthfulness of Values
		logger.debug("Total patches to apply are : {}", patches.size());
		for (JsonNode patch : patches) {

			String operation = patch.get(JsonDiffConstants.OPERATION).asText();

			if (operation.equals(JsonDiffConstants.REMOVE)) {
				removeValidator(oldState, newState, patch);
			} else if (operation.equals(JsonDiffConstants.REPLACE)) {
				replaceValidator(oldState, newState, patch);
			} else if (operation.equals(JsonDiffConstants.ADD)) {
				addValidator(oldState, newState, patch);
			} else {
				Assert.fail();
			}

		}
	}

	// Method to validate Array removal operation
	private void removeValidator(JsonNode oldState, JsonNode newState, JsonNode patch) throws JsonPointerException {

		String pathString = patch.get(JsonDiffConstants.PATH).asText();
		JsonPointer path = new JsonPointer(pathString);

		if (patch.has(JsonDiffConstants.ORIGINAL_VALUE)) {
			assertTrue(path.parent().get(oldState).isArray());
			// New State Should have the removed Element
			Assert.assertEquals(path.get(oldState), patch.get(JsonDiffConstants.ORIGINAL_VALUE));

			JsonNode newStatepathContent = path.parent().get(newState);// .iterator();
			List<JsonNode> newStateAtPath = Lists.newArrayList(newStatepathContent.iterator());

			// New State Should Not have the removed Elelment
			assertFalse(newStateAtPath.contains(patch.get(JsonDiffConstants.ORIGINAL_VALUE)));
		} else {
			// It is a non-array Remove operation
		}
	}

	// Method to validate replace operation
	private void replaceValidator(JsonNode oldState, JsonNode newState, JsonNode patch) throws JsonPointerException {
		JsonNode value = patch.get(JsonDiffConstants.VALUE);

		String pathString = patch.get(JsonDiffConstants.PATH).asText();
		JsonPointer path = new JsonPointer(pathString);

		if (patch.has(JsonDiffConstants.ORIGINAL_VALUE)) {
			// Means that the Parent to the operation path is Array
			assertTrue(path.parent().parent().get(oldState).isArray());

			assertEquals(path.get(newState), value);
			//

		} else {
			// It is a non-array Remove operation
		}

	}

	// Method to validate add operation
	private void addValidator(JsonNode oldState, JsonNode newState, JsonNode patch) throws JsonPointerException {
		JsonNode value = patch.get(JsonDiffConstants.VALUE);
		String pathString = patch.get(JsonDiffConstants.PATH).asText();
		JsonPointer path = new JsonPointer(pathString);

		if (pathString.contains("-")) {
			JsonNode newStatepathContent = path.parent().get(newState);// .iterator();
			List<JsonNode> newStateAtPath = Lists.newArrayList(newStatepathContent.iterator());
			assertTrue(newStateAtPath.contains(value));

			JsonNode oldStatepathContent = path.parent().get(oldState);// .iterator();
			List<JsonNode> oldStateAtPath = Lists.newArrayList(oldStatepathContent.iterator());
			//Value must be absent at oldState
			assertFalse(oldStateAtPath.contains(value));
		} else {
			// Not an Array Add Operation
		}
	}
}
