/**
 * 
 */
package com.github.fge.jsonpatch.diffcustom;

import java.io.File;
import java.io.IOException;

import org.testng.annotations.DataProvider;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author Ritesh
 *
 */
public class JsonDataProvider {
	private static ObjectMapper objectMapper = new ObjectMapper();

	@DataProvider(name = "Provide Data To Json-Diff 1")
	public static Object[][] provideDataDiff1() throws JsonProcessingException, IOException {
		JsonNode beforeNode1 = objectMapper.readTree(new File("src/test/resources/jsonpatch/diffcustom/old1.json"));
		JsonNode afterNode1 = objectMapper.readTree(new File("src/test/resources/jsonpatch/diffcustom/new1.json"));
		return new Object[][] { { beforeNode1, afterNode1 } };
	}

	@DataProvider(name = "Provide Data To Json-Diff 2")
	public static Object[][] provideDataDiff2() throws JsonProcessingException, IOException {
		JsonNode beforeNode2 = objectMapper.readTree(new File("src/test/resources/jsonpatch/diffcustom/old2.json"));
		JsonNode afterNode2 = objectMapper.readTree(new File("src/test/resources/jsonpatch/diffcustom/new2.json"));

		return new Object[][] { { beforeNode2, afterNode2 } };
	}

	@DataProvider(name = "Provide Data To Json-Diff 3")
	public static Object[][] provideDataDiff3() throws JsonProcessingException, IOException {
		JsonNode beforeNode3 = objectMapper.readTree(new File("src/test/resources/jsonpatch/diffcustom/old3.json"));
		JsonNode afterNode3 = objectMapper.readTree(new File("src/test/resources/jsonpatch/diffcustom/new3.json"));
		return new Object[][] { { beforeNode3, afterNode3 } };
	}

	@DataProvider(name = "Provide Data To Json-Diff 4")
	public static Object[][] provideDataDiff4() throws JsonProcessingException, IOException {
		JsonNode beforeNode4 = objectMapper.readTree(new File("src/test/resources/jsonpatch/diffcustom/old4.json"));
		JsonNode afterNode4 = objectMapper.readTree(new File("src/test/resources/jsonpatch/diffcustom/new4.json"));

		return new Object[][] { { beforeNode4, afterNode4 } };
	}

	@DataProvider(name = "Provide Data To Json-Diff 5")
	public static Object[][] provideDataDiff5() throws JsonProcessingException, IOException {
		JsonNode beforeNode5 = objectMapper.readTree(new File("src/test/resources/jsonpatch/diffcustom/old5.json"));
		JsonNode afterNode5 = objectMapper.readTree(new File("src/test/resources/jsonpatch/diffcustom/new5.json"));

		return new Object[][] { { beforeNode5, afterNode5 } };
	}

	@DataProvider(name = "Provide Data To Json-Diff 6")
	public static Object[][] provideDataDiff6() throws JsonProcessingException, IOException {
		JsonNode beforeNode6 = objectMapper.readTree(new File("src/test/resources/jsonpatch/diffcustom/old6.json"));
		JsonNode afterNode6 = objectMapper.readTree(new File("src/test/resources/jsonpatch/diffcustom/new6.json"));

		return new Object[][] { { beforeNode6, afterNode6 } };
	}

	@DataProvider(name = "Provide Data To Json-Diff 7")
	public static Object[][] provideDataDiff7() throws JsonProcessingException, IOException {
		JsonNode sampleold = objectMapper.readTree(new File("src/test/resources/jsonpatch/diffcustom/sampleold.json"));
		JsonNode samplenew = objectMapper.readTree(new File("src/test/resources/jsonpatch/diffcustom/samplenew.json"));
		
		return new Object[][] { { sampleold, samplenew } };
	}
}
