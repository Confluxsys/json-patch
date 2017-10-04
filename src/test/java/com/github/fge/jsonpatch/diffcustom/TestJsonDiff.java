package com.github.fge.jsonpatch.diffcustom;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.github.fge.jsonpatch.JsonDiffConstants;
import com.github.fge.jsonpatch.diff.JsonDiff;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jackson.jsonpointer.JsonPointer;
import com.github.fge.jackson.jsonpointer.JsonPointerException;

public class TestJsonDiff {
	private Map<JsonPointer, String> attributesKeyFeilds;
	private Logger logger = LoggerFactory.getLogger(this.getClass());

	JsonNode patch;

	@BeforeTest
	public void initialize() throws JsonPointerException {

		attributesKeyFeilds = new HashMap<JsonPointer, String>();
		attributesKeyFeilds.put(new JsonPointer("/Profiles"), "Profile");
		attributesKeyFeilds.put(new JsonPointer("/Groups"), "Group");
		attributesKeyFeilds.put(new JsonPointer("/Roles"), "Role");
		attributesKeyFeilds.put(new JsonPointer("/User Licenses"), "License");
		attributesKeyFeilds.put(new JsonPointer("/IT Resource"), null);
		attributesKeyFeilds.put(new JsonPointer("/Grouppp"), null); //NULL as Key Should return REMOVE AND ADD instead of REPLACE Element itself as KEY
	}

	@Test(dataProvider = "Provide Data To Json-Diff", dataProviderClass = JsonDataProvider.class)
	public void Computing(JsonNode beforeNode, JsonNode afterNode) throws JsonPointerException {

		try {
			patch = JsonDiff.asJson(beforeNode, afterNode, attributesKeyFeilds);
			logger.info("{}", patch.toString());
		} catch (JsonPointerException e) {
			logger.warn("WARNING : {} ", e.toString());
		}

		//Testing the Truthfulness of Values
		JsonNode stateContent;
		logger.debug("Total patches to apply are : {}", patch.size());
		for (int i = 0; i < patch.size(); i++) {
			logger.debug("Testing for PATCH : {}", i);
			String operation = patch.get(i).get(JsonDiffConstants.OPERATION).asText();
			JsonNode value = patch.get(i).get(JsonDiffConstants.VALUE);
			String pathString = patch.get(i).get(JsonDiffConstants.PATH).asText();
			String[] pathArray = pathString.split("/");
			String lastIterable = pathArray[(pathArray.length - 2)];
			JsonPointer path = new JsonPointer(pathString);
			if (operation.equals(JsonDiffConstants.REMOVE)) {
				if (patch.get(i).has(JsonDiffConstants.ORIGINAL_VALUE)) {
					//It is an Array operation  -> denoting our Custom REMOVE Operation 
					logger.debug("Value at patch to REMOVE is : {}", patch.get(i).get(JsonDiffConstants.ORIGINAL_VALUE));

					JsonPointer pointer = new JsonPointer(pathString);
					Boolean valuePresent = false;
					JsonNode valueatAfterNode = pointer.parent().get(afterNode);
					JsonNode valueatBeforeNode = pointer.get(beforeNode);
					if (valueatAfterNode.isArray()) {
						for (JsonNode eachValueatAfterNode : valueatAfterNode) {
							if (valueatBeforeNode == eachValueatAfterNode) {
								valuePresent = true;
							}
						}
						if (valuePresent) {
							Assert.fail();
						}
					}

					//		Change in this code ::DONE			
					//					if (path.parent().get(afterNode).isArray()) {
					//						for (int index = 0; index < afterNode.size(); index++) {
					//							logger.debug("Before Node: {}", beforeNode);
					//							logger.debug("After Node : {}", afterNode);
					//							logger.debug("Index : {}", index);
					//							logger.debug("Last Iterable : {}", lastIterable);
					//							if (afterNode.get(index).has(lastIterable)) {
					//								logger.warn("ERROR : Value Found at Target ");
					//								valuePresent = true;
					//							}
					//						}
					//						//Checking Absence of Value at Target
					//						Assert.assertNotEquals(valuePresent, true);
					//
					//						//Checking Presence of Value at Source
					//						Assert.assertEquals(path.get(beforeNode), patch.get(i).get(JsonDiffConstants.ORIGINAL_VALUE));
					//					}
				} else {

					//It is Not an Array Operation -> RFC 6902 remove operation
					//Checking Absence at target
					Assert.assertEquals(path.get(afterNode), null);
				}
			} else if (operation.equals(JsonDiffConstants.REPLACE)) {
				logger.debug("Value at patch to  REPLACE is : {}", value);
				if (patch.get(i).has("original_value")) {
					logger.debug("Value at patch to REPLACE is : {}", patch.get(i).get(JsonDiffConstants.ORIGINAL_VALUE));
					//It is an Array operation  -> denoting our Custom REPLACE Operation 

				}
				//It is Not an Array Operation -> RFC 6902 replace operation
				//We Always Need to check value in normal or custom case
				logger.debug("Value at Target is : {}", path.get(afterNode));

				if (!value.isNull()) {
					Assert.assertTrue(path.get(afterNode).equals(value));
				}
			} else if (operation.equals(JsonDiffConstants.ADD)) {
				if (lastIterable.equals("-")) {
					logger.debug("Path is : {}", path.parent());
					JsonNode stateparentContent = path.parent().get(afterNode);
					Boolean presence = false;
					for (JsonNode checkChild : stateparentContent) {
						if (checkChild.equals(value)) {
							presence = true;
						}
					}
					if (!presence) {
						//If element is Not found in targetNode (Failure case)
						Assert.fail();
					}
				} else {
					stateContent = path.get(afterNode);
					logger.debug("Path is : {}", path);
					logger.debug("Value at patch to  ADD is : {}", value);
					logger.debug("State Content to ADD is   : {}", stateContent);
					Assert.assertEquals(value, stateContent);
				}

			} else {
				logger.error("ERROR : The Output Patch is Incorrect");
				Assert.fail();

			}
		}
	}

	@Test(testName = "Test to fix the bug that old state Key's value is null where as new State's Key-> Value is Array, operation is add and not replace...")
	public void testBugFixWhileOldStateNullAndNewStateArray() throws JsonPointerException, JsonProcessingException, IOException {
		attributesKeyFeilds = new HashMap<JsonPointer, String>();
		attributesKeyFeilds.put(new JsonPointer("/Application Entitlement"), "Entitlement Name");
		attributesKeyFeilds.put(new JsonPointer("/Role in VEM"), "Role");
		attributesKeyFeilds.put(new JsonPointer("/Application Role"), "Role");
		ObjectMapper objectMapper = new ObjectMapper();
		JsonNode beforeNode = objectMapper.readTree(new File("src/test/resources/jsonpatch/diffcustom/beforeNode.json"));
		JsonNode afterNode = objectMapper.readTree(new File("src/test/resources/jsonpatch/diffcustom/afterNode.json"));
		try {
			patch = JsonDiff.asJson(beforeNode, afterNode, attributesKeyFeilds);
			logger.info("{}", patch.toString());
		} catch (JsonPointerException e) {
			logger.warn("WARNING : {} ", e.toString());
		}

	}

	@Test(testName = "Test to fix the bug that old state Key's value is null where as new State's Key-> Value is Array, operation is add and not replace...")
	public void testBugFixWhileOldStateArrayAndNewStateNull() throws JsonPointerException, JsonProcessingException, IOException {
		attributesKeyFeilds = new HashMap<JsonPointer, String>();
		attributesKeyFeilds.put(new JsonPointer("/Application Entitlement"), "Entitlement Name");
		attributesKeyFeilds.put(new JsonPointer("/Role in VEM"), "Role");
		attributesKeyFeilds.put(new JsonPointer("/Application Role"), "Role");
		ObjectMapper objectMapper = new ObjectMapper();
		//old State
		JsonNode beforeNode = objectMapper.readTree(new File("src/test/resources/jsonpatch/diffcustom/afterNode.json"));
		//New State
		JsonNode afterNode = objectMapper.readTree(new File("src/test/resources/jsonpatch/diffcustom/beforeNode.json"));
		try {
			patch = JsonDiff.asJson(beforeNode, afterNode, attributesKeyFeilds);
			logger.info("{}", patch.toString());
		} catch (JsonPointerException e) {
			logger.warn("WARNING : {} ", e.toString());
		}

	}

}
