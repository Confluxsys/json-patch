package com.github.fge.jsonpatch.diffcustom;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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

public class TestCompositeKey {
	private Map<JsonPointer, Set<String>> testJsonDiffCompositeMap;
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	private ObjectMapper objectMapper;

	@BeforeTest
	public void initialize() throws JsonPointerException {
		objectMapper = new ObjectMapper();

		testJsonDiffCompositeMap = new HashMap<>();
		Set<String> set1 = new HashSet<>();
		set1.add("Profile");
		Set<String> set2 = new HashSet<>();
		set2.add("Group");
		Set<String> set3 = new HashSet<>();
		set3.add("Role");
		Set<String> set4 = new HashSet<>();
		set4.add("License");
		Set<String> set5 = new HashSet<>();
		set5.add("b");
		set5.add("Group");
		testJsonDiffCompositeMap.put(new JsonPointer("/Profiles"), set1);
		testJsonDiffCompositeMap.put(new JsonPointer("/Groups"), set2);
		testJsonDiffCompositeMap.put(new JsonPointer("/Roles"), set3);
		testJsonDiffCompositeMap.put(new JsonPointer("/User Licenses"), set4);
		testJsonDiffCompositeMap.put(new JsonPointer("/IT Resource"), null);
		testJsonDiffCompositeMap.put(new JsonPointer("/Grouppp"), set5);

	}

	@Test(dataProvider = "Provide Data To Json-Diff 1", dataProviderClass = JsonDataProvider.class)
	public void Computing1(JsonNode beforeNode, JsonNode afterNode)
			throws JsonDiffException, IOException, JsonPointerException {
		JsonNode patch = JsonDiff.asJson(beforeNode, afterNode, testJsonDiffCompositeMap);
		logger.info("{}", patch.toString());

		JsonNode expectedPatch = objectMapper
				.readTree(new File("src/test/resources/jsonpatch/diffcustom/expected/expectedDiffComposite1.json"));

		logger.info("e: {}", expectedPatch);
		logger.info("p: {}", patch);
		Assert.assertEquals(patch, expectedPatch);
		logger.debug("Total patches to apply are : {}", patch.size());
	}

	@Test(dataProvider = "Provide Data To Json-Diff 2", dataProviderClass = JsonDataProvider.class)
	public void Computing2(JsonNode beforeNode, JsonNode afterNode)
			throws JsonDiffException, IOException, JsonPointerException {
		JsonNode patch = JsonDiff.asJson(beforeNode, afterNode, testJsonDiffCompositeMap);
		logger.info("{}", patch.toString());

		JsonNode expectedPatch = objectMapper
				.readTree(new File("src/test/resources/jsonpatch/diffcustom/expected/expectedDiff2.json"));

		Assert.assertEquals(patch, expectedPatch);
		logger.debug("Total patches to apply are : {}", patch.size());
	}

	@Test(dataProvider = "Provide Data To Json-Diff 3", dataProviderClass = JsonDataProvider.class)
	public void Computing3(JsonNode beforeNode, JsonNode afterNode)
			throws JsonDiffException, IOException, JsonPointerException {

		JsonNode patch = JsonDiff.asJson(beforeNode, afterNode, testJsonDiffCompositeMap);
		logger.info("{}", patch.toString());

		JsonNode expectedPatch = objectMapper
				.readTree(new File("src/test/resources/jsonpatch/diffcustom/expected/expectedDiff3.json"));

		Assert.assertEquals(patch, expectedPatch);
		logger.debug("Total patches to apply are : {}", patch.size());
	}

	@Test(dataProvider = "Provide Data To Json-Diff 4", dataProviderClass = JsonDataProvider.class)
	public void Computing4(JsonNode beforeNode, JsonNode afterNode)
			throws JsonDiffException, IOException, JsonPointerException {
		JsonNode patch = JsonDiff.asJson(beforeNode, afterNode, testJsonDiffCompositeMap);
		logger.info("{}", patch.toString());

		JsonNode expectedPatch = objectMapper
				.readTree(new File("src/test/resources/jsonpatch/diffcustom/expected/expectedDiff4.json"));

		Assert.assertEquals(patch, expectedPatch);
		logger.debug("Total patches to apply are : {}", patch.size());
	}

	@Test(dataProvider = "Provide Data To Json-Diff 5", dataProviderClass = JsonDataProvider.class)
	public void Computing5(JsonNode beforeNode, JsonNode afterNode)
			throws JsonDiffException, IOException, JsonPointerException {
		JsonNode patch = JsonDiff.asJson(beforeNode, afterNode, testJsonDiffCompositeMap);
		logger.info("{}", patch.toString());

		JsonNode expectedPatch = objectMapper
				.readTree(new File("src/test/resources/jsonpatch/diffcustom/expected/expectedDiff5.json"));

		Assert.assertEquals(patch, expectedPatch);
		logger.debug("Total patches to apply are : {}", patch.size());
	}

	@Test(dataProvider = "Provide Data To Json-Diff 6", dataProviderClass = JsonDataProvider.class)
	public void Computing6(JsonNode beforeNode, JsonNode afterNode)
			throws JsonDiffException, IOException, JsonPointerException {
		JsonNode patch = JsonDiff.asJson(beforeNode, afterNode, null);
		logger.info("{}", patch.toString());

		JsonNode expectedPatch = objectMapper
				.readTree(new File("src/test/resources/jsonpatch/diffcustom/expected/expectedDiff6.json"));

		Assert.assertEquals(patch, expectedPatch);
		logger.debug("Total patches to apply are : {}", patch.size());
	}

	@Test(dataProvider = "Provide Data To Json-Diff 7", dataProviderClass = JsonDataProvider.class)
	public void Computing7(JsonNode beforeNode, JsonNode afterNode)
			throws JsonDiffException, IOException, JsonPointerException {
		JsonNode patch = JsonDiff.asJson(beforeNode, afterNode, testJsonDiffCompositeMap);
		logger.info("{}", patch.toString());

		JsonNode expectedPatch = objectMapper.createArrayNode();
		// .readTree(new
		// File("src/test/resources/jsonpatch/diffcustom/expected/expectedDiff7.json"));

		Assert.assertEquals(patch, expectedPatch);
		logger.debug("Total patches to apply are : {}", patch.size());
	}

	@Test(testName = "Test to fix the bug that old state Key's value is null where as new State's Key-> Value is Array, operation is add and not replace...")
	public void testBugFixWhileOldStateNullAndNewStateArray()
			throws JsonDiffException, IOException, JsonPointerException {
		JsonNode beforeNode = objectMapper
				.readTree(new File("src/test/resources/jsonpatch/diffcustom/beforeNode.json"));
		JsonNode afterNode = objectMapper.readTree(new File("src/test/resources/jsonpatch/diffcustom/afterNode.json"));
		JsonNode patch = JsonDiff.asJson(beforeNode, afterNode, null);
		logger.info("{}", patch.toString());

	}
}
