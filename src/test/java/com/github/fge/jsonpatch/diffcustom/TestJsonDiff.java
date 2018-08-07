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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jackson.jsonpointer.JsonPointer;
import com.github.fge.jackson.jsonpointer.JsonPointerException;
import com.github.fge.jsonpatch.JsonDiffException;
import com.github.fge.jsonpatch.diff.JsonDiff;

public class TestJsonDiff {
	private Map<JsonPointer, String> attributesKeyFeildsTestJsonDiff;
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	private ObjectMapper objectMapper;

	@BeforeTest
	public void initialize() throws JsonPointerException {
		objectMapper = new ObjectMapper();
		attributesKeyFeildsTestJsonDiff = new HashMap<>();
		attributesKeyFeildsTestJsonDiff.put(new JsonPointer("/Profiles"), "Profile");
		attributesKeyFeildsTestJsonDiff.put(new JsonPointer("/Groups"), "Group");
		attributesKeyFeildsTestJsonDiff.put(new JsonPointer("/Roles"), "Role");
		attributesKeyFeildsTestJsonDiff.put(new JsonPointer("/User Licenses"), "License");
		attributesKeyFeildsTestJsonDiff.put(new JsonPointer("/IT Resource"), null);
		attributesKeyFeildsTestJsonDiff.put(new JsonPointer("/Grouppp"), "b");
	}

	@Test(dataProvider = "Provide Data To Json-Diff 1", dataProviderClass = JsonDataProvider.class)
	public void Computing1(JsonNode beforeNode, JsonNode afterNode)
			throws JsonDiffException, IOException, JsonPointerException {
		JsonNode patch = JsonDiff.asJson(beforeNode, afterNode, attributesKeyFeildsTestJsonDiff);
		logger.info("{}", patch.toString());

		JsonNode expectedPatch = objectMapper
				.readTree(new File("src/test/resources/jsonpatch/diffcustom/expected/expectedDiff1.json"));

		logger.info("e: {}", expectedPatch);
		logger.info("p: {}", patch);
		Assert.assertEquals(patch, expectedPatch);
		logger.debug("Total patches to apply are : {}", patch.size());
	}

	@Test(dataProvider = "Provide Data To Json-Diff 2", dataProviderClass = JsonDataProvider.class)
	public void Computing2(JsonNode beforeNode, JsonNode afterNode)
			throws JsonDiffException, IOException, JsonPointerException {
		JsonNode patch = JsonDiff.asJson(beforeNode, afterNode, attributesKeyFeildsTestJsonDiff);
		logger.info("{}", patch.toString());

		JsonNode expectedPatch = objectMapper
				.readTree(new File("src/test/resources/jsonpatch/diffcustom/expected/expectedDiff2.json"));

		Assert.assertEquals(patch, expectedPatch);
		logger.debug("Total patches to apply are : {}", patch.size());
	}

	@Test(dataProvider = "Provide Data To Json-Diff 3", dataProviderClass = JsonDataProvider.class)
	public void Computing3(JsonNode beforeNode, JsonNode afterNode)
			throws JsonDiffException, IOException, JsonPointerException{
		JsonNode patch = JsonDiff.asJson(beforeNode, afterNode, attributesKeyFeildsTestJsonDiff);
		logger.info("{}", patch.toString());

		JsonNode expectedPatch = objectMapper
				.readTree(new File("src/test/resources/jsonpatch/diffcustom/expected/expectedDiff3.json"));

		Assert.assertEquals(patch, expectedPatch);
		logger.debug("Total patches to apply are : {}", patch.size());
	}

	@Test(dataProvider = "Provide Data To Json-Diff 4", dataProviderClass = JsonDataProvider.class)
	public void Computing4(JsonNode beforeNode, JsonNode afterNode)
			throws JsonDiffException, IOException, JsonPointerException{
		JsonNode patch = JsonDiff.asJson(beforeNode, afterNode, attributesKeyFeildsTestJsonDiff);
		logger.info("{}", patch.toString());

		JsonNode expectedPatch = objectMapper
				.readTree(new File("src/test/resources/jsonpatch/diffcustom/expected/expectedDiff4.json"));

		Assert.assertEquals(patch, expectedPatch);
		logger.debug("Total patches to apply are : {}", patch.size());
	}

	@Test(dataProvider = "Provide Data To Json-Diff 5", dataProviderClass = JsonDataProvider.class)
	public void Computing5(JsonNode beforeNode, JsonNode afterNode)
			throws JsonDiffException, IOException, JsonPointerException{
		JsonNode patch = JsonDiff.asJson(beforeNode, afterNode, attributesKeyFeildsTestJsonDiff);
		logger.info("{}", patch.toString());

		JsonNode expectedPatch = objectMapper
				.readTree(new File("src/test/resources/jsonpatch/diffcustom/expected/expectedDiff5.json"));

		Assert.assertEquals(patch, expectedPatch);
		logger.debug("Total patches to apply are : {}", patch.size());
	}

	@Test(dataProvider = "Provide Data To Json-Diff 6", dataProviderClass = JsonDataProvider.class)
	public void Computing6(JsonNode beforeNode, JsonNode afterNode)
			throws JsonDiffException, IOException, JsonPointerException{
		JsonNode patch = JsonDiff.asJson(beforeNode, afterNode, attributesKeyFeildsTestJsonDiff);
		logger.info("{}", patch.toString());

		JsonNode expectedPatch = objectMapper
				.readTree(new File("src/test/resources/jsonpatch/diffcustom/expected/expectedDiff6.json"));

		Assert.assertEquals(patch, expectedPatch);
		logger.debug("Total patches to apply are : {}", patch.size());
	}

	@Test(dataProvider = "Provide Data To Json-Diff 7", dataProviderClass = JsonDataProvider.class)
	public void Computing7(JsonNode beforeNode, JsonNode afterNode)
			throws JsonDiffException, IOException, JsonPointerException{
		JsonNode patch = JsonDiff.asJson(beforeNode, afterNode, attributesKeyFeildsTestJsonDiff);
		logger.info("{}", patch.toString());

		JsonNode expectedPatch = objectMapper.createArrayNode();
		// .readTree(new
		// File("src/test/resources/jsonpatch/diffcustom/expected/expectedDiff7.json"));

		Assert.assertEquals(patch, expectedPatch);
		logger.debug("Total patches to apply are : {}", patch.size());
	}

	@Test(testName = "Test to fix the bug that old state Key's value is null where as new State's Key-> Value is Array, operation is add and not replace...")
	public void testBugFixWhileOldStateNullAndNewStateArray()
			throws JsonDiffException, IOException, JsonPointerException{
		JsonNode beforeNode = objectMapper
				.readTree(new File("src/test/resources/jsonpatch/diffcustom/old_nullValue.json"));
		JsonNode afterNode = objectMapper.readTree(new File("src/test/resources/jsonpatch/diffcustom/new_arrayWithValues.json"));
		JsonNode patch = JsonDiff.asJson(beforeNode, afterNode, null);
		
		JsonNode expectedPatch = objectMapper.readTree(new File("src/test/resources/jsonpatch/diffcustom/expected/expectedDiff_ArrayAdd.json"));

		logger.info("{}", patch.toString());
		Assert.assertEquals(patch, expectedPatch);

	}

}
