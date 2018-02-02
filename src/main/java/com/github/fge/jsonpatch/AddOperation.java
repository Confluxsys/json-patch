/*
 * Copyright (c) 2014, Francis Galiegue (fgaliegue@gmail.com)
 *
 * This software is dual-licensed under:
 *
 * - the Lesser General Public License (LGPL) version 3.0 or, at your option, any
 *   later version;
 * - the Apache Software License (ASL) version 2.0.
 *
 * The text of both licenses is available under the src/resources/ directory of
 * this project (under the names LGPL-3.0.txt and ASL-2.0.txt respectively).
 *
 * Direct link to the sources:
 *
 * - LGPL 3.0: https://www.gnu.org/licenses/lgpl-3.0.txt
 * - ASL 2.0: http://www.apache.org/licenses/LICENSE-2.0.txt
 */

package com.github.fge.jsonpatch;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.fge.jackson.jsonpointer.JsonPointer;
import com.github.fge.jackson.jsonpointer.JsonPointerException;
import com.github.fge.jackson.jsonpointer.ReferenceToken;
import com.github.fge.jackson.jsonpointer.TokenResolver;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

/**
 * JSON Patch {@code add} operation
 *
 * <p>
 * For this operation, {@code path} is the JSON Pointer where the value should
 * be added, and {@code value} is the value to add.
 * </p>
 *
 * <p>
 * Note that if the target value pointed to by {@code path} already exists, it
 * is replaced. In this case, {@code add} is equivalent to {@code replace}.
 * </p>
 *
 * <p>
 * Note also that a value will be created at the target path <b>if and only
 * if</b> the immediate parent of that value exists (and is of the correct
 * type).
 * </p>
 *
 * <p>
 * Finally, if the last reference token of the JSON Pointer is {@code -} and the
 * immediate parent is an array, the given value is added at the end of the
 * array. For instance, applying:
 * </p>
 *
 * <pre>
 *     { "op": "add", "path": "/-", "value": 3 }
 * </pre>
 *
 * <p>
 * to:
 * </p>
 *
 * <pre>
 *     [ 1, 2 ]
 * </pre>
 *
 * <p>
 * will give:
 * </p>
 *
 * <pre>
 *     [ 1, 2, 3 ]
 * </pre>
 */
public final class AddOperation extends PathValueOperation {
	private static final ReferenceToken LAST_ARRAY_ELEMENT = ReferenceToken.fromRaw("-");

	private ObjectMapper objectMapper = new ObjectMapper();

	@JsonCreator
	public AddOperation(@JsonProperty("path") final JsonPointer path, @JsonProperty("value") final JsonNode value) {
		super("add", path, value);
	}

	@Override
	public JsonNode apply(final JsonNode node) throws JsonPatchException {
		if (path.isEmpty())
			return value;

		/*
		 * OLD-IMPLENETATION TO THROW ERROR Check the parent node: it must exist
		 * and be a container (ie an array or an object) for the add operation
		 * to work.
		 */
		final JsonNode parentNode = path.parent().path(node);
		if (parentNode.isMissingNode()) {
			// throw new
			// JsonPatchException(BUNDLE.getMessage("jsonPatch.noSuchParent"));

			/*
			 * This Method discard the non-existing path and only consider's the
			 * existing path
			 */
			try {

				JsonPointer preexistingPath = pathExistUpto(node, path);
				/*
				 * as we have already checked for missing node the newPath
				 * cannot be empty
				 */

				/*
				 * This is missing path that we need to create
				 */
				JsonPointer missingPath = new JsonPointer(path.toString().replaceFirst(preexistingPath.toString(), ""));
				/*
				 * This method creates the missing path for the value
				 */
				JsonNode newValue = pathBuilder(missingPath, value);

				/*
				 * Last of Existing path to valid is it an array or Object
				 */

				if (preexistingPath.get(node).isArray()) {
					return addToArray(node, preexistingPath, newValue);
				} else {
					return addToObject(node, preexistingPath, newValue);
				}

			} catch (JsonPointerException e) {
				throw new JsonPatchException(BUNDLE.getMessage("jsonPatch.noSuchParent"));
			}

		}
		if (!parentNode.isContainerNode()) {
			if (!parentNode.isValueNode())
				throw new JsonPatchException(BUNDLE.getMessage("jsonPatch.parentNotContainer"));
		}
		return parentNode.isArray() ? addToArray(path, node) : addToObject(path, node);
	}

	/**
	 * Custom add for non-existing path,
	 * 
	 * @param node
	 * @param newPath
	 * @param newValue
	 * @return
	 */
	private JsonNode addToArray(JsonNode node, JsonPointer newPath, JsonNode newValue) {

		final JsonNode ret = node.deepCopy();
		final ArrayNode target = (ArrayNode) newPath.get(ret);
		target.add(newValue);

		return ret;
	}

	private JsonNode addToArray(final JsonPointer path, final JsonNode node) throws JsonPatchException {
		final JsonNode ret = node.deepCopy();
		final ArrayNode target = (ArrayNode) path.parent().get(ret);

		List<JsonNode> existingValues = Lists.newArrayList(target);
		// check duplicate
		if (!existingValues.contains(value)) {
			final TokenResolver<JsonNode> token = Iterables.getLast(path);

			if (token.getToken().equals(LAST_ARRAY_ELEMENT)) {
				target.add(value);
				return ret;
			}

			final int size = target.size();
			final int index;
			try {
				index = Integer.parseInt(token.toString());
			} catch (NumberFormatException ignored) {
				throw new JsonPatchException(BUNDLE.getMessage("jsonPatch.notAnIndex"));
			}

			if (index < 0 || index > size)
				throw new JsonPatchException(BUNDLE.getMessage("jsonPatch.noSuchIndex"));

			target.insert(index, value);
		}

		return ret;
	}

	private JsonNode addToObject(final JsonPointer path, final JsonNode node) throws JsonPatchException {
		final JsonNode ret = node.deepCopy();
		final ObjectNode target = (ObjectNode) path.parent().get(ret);

		String lastOfPath = Iterables.getLast(path).getToken().getRaw();
		if (lastOfPath.equals("-")) {
			if (value.isArray()) {
				throw new JsonPatchException(BUNDLE.getMessage("jsonPatch.noSuchIndex"));
			}
		}
		target.put(lastOfPath, value);

		return ret;
	}

	/**
	 * custom Add to ObjerctNode Adding value to non-existing path
	 * 
	 * @param node
	 * @param newPath
	 * @param newValue
	 * @return
	 * @throws JsonPatchException
	 */
	private JsonNode addToObject(JsonNode node, JsonPointer newPath, JsonNode newValue) throws JsonPatchException {

		final JsonNode ret = node.deepCopy();
		JsonNode target = objectMapper.createObjectNode();

		if (newPath.isEmpty()) {

			if (newValue.isArray()) {
				throw new JsonPatchException(BUNDLE.getMessage("jsonPatch.noSuchIndex"));
			} else {
				List<String> fieldNames = Lists.newArrayList(newValue.fieldNames());
				for (String fieldName : fieldNames) {
					((ObjectNode) ret).put(fieldName, newValue.get(fieldName));
				}
			}
		} else {
			String lastOfPath = Iterables.getLast(newPath).getToken().getRaw();
			if (newPath.get(ret).isObject()) {
				target = newPath.get(ret);
			} else {
				target = newPath.parent().get(ret);
			}

			if (lastOfPath.equals("-")) {
				if (newValue.isArray()) {
					throw new JsonPatchException(BUNDLE.getMessage("jsonPatch.noSuchIndex"));
				}
			} else if (lastOfPath.matches("[0-9]+")) {
				if (newValue.isObject()) {
					// All the Field names to List
					List<String> fieldNames = Lists.newArrayList(newValue.fieldNames());
					for (String fieldName : fieldNames) {
						((ObjectNode) target).put(fieldName, newValue.get(fieldName));
					}
				} else {
					throw new JsonPatchException(BUNDLE.getMessage("jsonPatch.noSuchIndex"));
				}
			} else {
				((ObjectNode) target).put(lastOfPath, newValue);
			}

		}
		return ret;

	}

	private JsonPointer pathExistUpto(final JsonNode node, final JsonPointer path) {
		JsonPointer newPath = path;
		if (!newPath.isEmpty()) {
			JsonNode valueAtPath = path.path(node);
			if (valueAtPath.isMissingNode()) {
				newPath = pathExistUpto(node, newPath.parent());
			}
		}
		return newPath;

	}

	/**
	 * This method is used to create the non-existing path
	 * 
	 * @param node
	 * @param path
	 * @return
	 */
	private JsonNode pathBuilder(JsonPointer path, JsonNode value) {

		if (!path.isEmpty()) {
			String lastOfPath = Iterables.getLast(path).getToken().getRaw();
			path = path.parent();

			ArrayNode childArrayNode = objectMapper.createArrayNode();
			JsonNode childObjectNode = objectMapper.createObjectNode();

			if (lastOfPath.matches("[0-9]+") || (lastOfPath.equals("-"))) {
				childArrayNode.add(value);
				value = pathBuilder(path, childArrayNode);
			} else {
				((ObjectNode) childObjectNode).put(lastOfPath, value);
				value = pathBuilder(path, childObjectNode);
			}
		}

		return value;
	}

}
