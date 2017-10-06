/*
 * Copyright (c) 2014, Francis Galiegue (fgaliegue@gmail.com)
 *
 * This software is dual-licensed under:
 *
 * - the Lesser General Public License (LGPL) version 3.0 or, at your option, any
 *   later version;
 * - the Apache Software License (ASL) version 2.0.
 *
 * The text of this file and of both licenses is available at the root of this
 * project or, if you have the jar distribution, in directory META-INF/, under
 * the names LGPL-3.0.txt and ASL-2.0.txt respectively.
 *
 * Direct link to the sources:
 *
 * - LGPL 3.0: https://www.gnu.org/licenses/lgpl-3.0.txt
 * - ASL 2.0: http://www.apache.org/licenses/LICENSE-2.0.txt
 */

package com.github.fge.jsonpatch.diff;

import com.github.fge.jsonpatch.diff.DiffProcessor;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.fge.jackson.JacksonUtils;
import com.github.fge.jackson.JsonNumEquals;
import com.github.fge.jackson.NodeType;
import com.github.fge.jackson.jsonpointer.JsonPointer;
import com.github.fge.jackson.jsonpointer.JsonPointerException;
import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.JsonPatchMessages;
import com.github.fge.msgsimple.bundle.MessageBundle;
import com.github.fge.msgsimple.load.MessageBundles;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Equivalence;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import javax.annotation.ParametersAreNonnullByDefault;


import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * JSON "diff" implementation
 *
 * <p>
 * This class generates a JSON Patch (as in, an RFC 6902 JSON Patch) given two
 * JSON values as inputs. The patch can be obtained directly as a
 * {@link JsonPatch} or as a {@link JsonNode}.
 * </p>
 *
 * <p>
 * Note: there is <b>no guarantee</b> about the usability of the generated patch
 * for any other source/target combination than the one used to generate the
 * patch.
 * </p>
 *
 * <p>
 * This class always performs operations in the following order: removals,
 * additions and replacements. It then factors removal/addition pairs into move
 * operations, or copy operations if a common element exists, at the same
 * {@link JsonPointer pointer}, in both the source and destination.
 * </p>
 *
 * <p>
 * You can obtain a diff either as a {@link JsonPatch} directly or, for
 * backwards compatibility, as a {@link JsonNode}.
 * </p>
 *
 * @since 1.2
 */
@ParametersAreNonnullByDefault
public final class JsonDiff {
	private static final MessageBundle BUNDLE = MessageBundles.getBundle(JsonPatchMessages.class);
	private static final ObjectMapper MAPPER = JacksonUtils.newMapper();

	private static final Equivalence<JsonNode> EQUIVALENCE = JsonNumEquals.getInstance();

	private JsonDiff() {
	}

	/**
	 * Generate a JSON patch for transforming the source node into the target
	 * node
	 *
	 * @param source
	 *            the node to be patched
	 * @param target
	 *            the expected result after applying the patch
	 * @return the patch as a {@link JsonPatch}
	 *
	 * @since 1.9
	 */
	public static JsonPatch asJsonPatch(final JsonNode source, final JsonNode target) {
		BUNDLE.checkNotNull(source, "common.nullArgument");
		BUNDLE.checkNotNull(target, "common.nullArgument");
		final Map<JsonPointer, JsonNode> unchanged = getUnchangedValues(source, target);
		final DiffProcessor processor = new DiffProcessor(unchanged);

		generateDiffs(processor, JsonPointer.empty(), source, target);
		return processor.getPatch();
	}

	/**
	 * Generate a JSON patch for transforming the source node into the target
	 * node
	 *
	 * @param source
	 *            the node to be patched
	 * @param target
	 *            the expected result after applying the patch
	 * @return the patch as a {@link JsonNode}
	 */
	public static JsonNode asJson(final JsonNode source, final JsonNode target) {
		final String s;
		try {
			s = MAPPER.writeValueAsString(asJsonPatch(source, target));
			return MAPPER.readTree(s);
		} catch (IOException e) {
			throw new RuntimeException("cannot generate JSON diff", e);
		}
	}

	private static void generateDiffs(final DiffProcessor processor, final JsonPointer pointer, final JsonNode source,
			final JsonNode target) {
		if (EQUIVALENCE.equivalent(source, target))
			return;

		final NodeType firstType = NodeType.getNodeType(source);
		final NodeType secondType = NodeType.getNodeType(target);

		/*
		 * Node types differ: generate a replacement operation.
		 */
		if (firstType != secondType) {
			processor.valueReplaced(pointer, source, target);
			return;
		}

		/*
		 * If we reach this point, it means that both nodes are the same type,
		 * but are not equivalent.
		 *
		 * If this is not a container, generate a replace operation.
		 */
		if (!source.isContainerNode()) {
			processor.valueReplaced(pointer, source, target);
			return;
		}

		/*
		 * If we reach this point, both nodes are either objects or arrays;
		 * delegate.
		 */
		if (firstType == NodeType.OBJECT)
			generateObjectDiffs(processor, pointer, (ObjectNode) source, (ObjectNode) target);
		else // array
			generateArrayDiffs(processor, pointer, (ArrayNode) source, (ArrayNode) target);
	}

	private static void generateObjectDiffs(final DiffProcessor processor, final JsonPointer pointer,
			final ObjectNode source, final ObjectNode target) {
		final Set<String> firstFields = Sets.newTreeSet(Sets.newHashSet(source.fieldNames()));
		final Set<String> secondFields = Sets.newTreeSet(Sets.newHashSet(target.fieldNames()));

		for (final String field : Sets.difference(firstFields, secondFields))
			processor.valueRemoved(pointer.append(field), source.get(field));

		for (final String field : Sets.difference(secondFields, firstFields))
			processor.valueAdded(pointer.append(field), target.get(field));

		for (final String field : Sets.intersection(firstFields, secondFields))
			generateDiffs(processor, pointer.append(field), source.get(field), target.get(field));
	}

	private static void generateArrayDiffs(final DiffProcessor processor, final JsonPointer pointer,
			final ArrayNode source, final ArrayNode target) {
		final int firstSize = source.size();
		final int secondSize = target.size();
		final int size = Math.min(firstSize, secondSize);

		/*
		 * Source array is larger; in this case, elements are removed from the
		 * target; the index of removal is always the original arrays's length.
		 */
		for (int index = size; index < firstSize; index++)
			processor.valueRemoved(pointer.append(size), source.get(index));

		for (int index = 0; index < size; index++)
			generateDiffs(processor, pointer.append(index), source.get(index), target.get(index));

		// Deal with the destination array being larger...
		for (int index = size; index < secondSize; index++)
			processor.valueAdded(pointer.append("-"), target.get(index));
	}

	@VisibleForTesting
	static Map<JsonPointer, JsonNode> getUnchangedValues(final JsonNode source, final JsonNode target) {
		final Map<JsonPointer, JsonNode> ret = Maps.newHashMap();
		computeUnchanged(ret, JsonPointer.empty(), source, target);
		return ret;
	}

	private static void computeUnchanged(final Map<JsonPointer, JsonNode> ret, final JsonPointer pointer,
			final JsonNode first, final JsonNode second) {
		if (EQUIVALENCE.equivalent(first, second)) {
			ret.put(pointer, second);
			return;
		}

		final NodeType firstType = NodeType.getNodeType(first);
		final NodeType secondType = NodeType.getNodeType(second);

		if (firstType != secondType)
			return; // nothing in common

		// We know they are both the same type, so...

		switch (firstType) {
		case OBJECT:
			computeObject(ret, pointer, first, second);
			break;
		case ARRAY:
			computeArray(ret, pointer, first, second);
		default:
			/* nothing */
		}
	}

	private static void computeObject(final Map<JsonPointer, JsonNode> ret, final JsonPointer pointer,
			final JsonNode source, final JsonNode target) {
		final Iterator<String> firstFields = source.fieldNames();

		String name;

		while (firstFields.hasNext()) {
			name = firstFields.next();
			if (!target.has(name))
				continue;
			computeUnchanged(ret, pointer.append(name), source.get(name), target.get(name));
		}
	}

	private static void computeArray(final Map<JsonPointer, JsonNode> ret, final JsonPointer pointer,
			final JsonNode source, final JsonNode target) {
		final int size = Math.min(source.size(), target.size());

		for (int i = 0; i < size; i++)
			computeUnchanged(ret, pointer.append(i), source.get(i), target.get(i));
	}

	// Custom changes to existing Methods
	/**
	 * Generate a JSON patch for transforming the source node into the target
	 * node
	 * 
	 * @param source
	 *            the node to be patched
	 * @param target
	 *            the expected result after applying the patch
	 * @param arrayObjectPrimaryKeyFields
	 *            the map of Key Fields with its path to detect operations in an
	 *            Array Object
	 * @return JsonNode
	 * @throws JsonPointerException
	 * 
	 *             This is a custom Diff generation Function which needs a
	 *             MAp<JsonPointer, String> which contains All those primary
	 *             Keys for the Values in Array that you want to find difference
	 *             for.
	 */
	public static JsonNode asJson(final JsonNode source, final JsonNode target,
			final Map<JsonPointer, String> arrayObjectPrimaryKeyFields) throws JsonPointerException {
		final String difference;

		try {
			// we pass the source, target and arrayObjectPrimaryKeyFieldsto
			// JsonPatch to
			// find difference
			difference = MAPPER.writeValueAsString(asJsonPatch(source, target, arrayObjectPrimaryKeyFields));
			// We get the final difference between the source and target as a
			// string

			return MAPPER.readTree(difference);
			// returning the difference as a JsonNode
		} catch (IOException e) {
			throw new RuntimeException("cannot generate JSON diff", e);
		}
	}

	/**
	 * Generate a JSON patch for transforming the source node into the target
	 * node
	 * 
	 * @param source
	 *            the node to be patched
	 * @param target
	 *            the expected result after applying the patch
	 * @param arrayObjectPrimaryKeyFields
	 *            the map of Key Fields with its path to detect operations in an
	 *            Array Object
	 * @return JsonPatch
	 * @throws IOException
	 * @throws JsonPointerException
	 * 
	 *             This Method Generates difference in form of JsonPatch that
	 *             help's us to further use this to be directly implementable on
	 *             a JsonNode using Json Merge Patch
	 */
	public static JsonPatch asJsonPatch(final JsonNode source, final JsonNode target,
			final Map<JsonPointer, String> arrayObjectPrimaryKeyFields) throws IOException, JsonPointerException {
		BUNDLE.checkNotNull(source, "common.nullArgument");
		BUNDLE.checkNotNull(target, "common.nullArgument");

		/*
		 * To initialize DiffProcessor --old Implementation we need a Map of
		 * unchanged values which are helpful in evaluating copy and move
		 * operations in add and remove operation. We don't support such
		 * operation. THe Copy and Move are Neglected while calculating
		 * difference
		 * 
		 */
		final Map<JsonPointer, JsonNode> unchanged = Maps.newHashMap();
		/*
		 * Initializing DiffProcessor to store the Difference operations.
		 */
		final DiffProcessor processor = new DiffProcessor(unchanged);

		/*
		 * The method to calculate difference
		 */
		generateDiffs(processor, JsonPointer.empty(), source, target, arrayObjectPrimaryKeyFields);
		return processor.getPatch();
	}

	/**
	 * This Method decides what kind of Object or a value in an JsonNode is and
	 * then Selects appropriate action to be perform.
	 * 
	 * @param processor
	 * 
	 * @param pointer
	 * 
	 * @param source
	 *            The node to be patched
	 * @param target
	 *            The expected result after applying the patch
	 * @param arrayObjectPrimaryKeyFields
	 *            The map of Key Fields with its path to detect operations in an
	 *            Array Object
	 * @throws IOException
	 * @throws JsonPointerException
	 * 
	 *             {@value} : JsonNode treat's null as a node so no null pointer
	 *             exception can be checked or occur
	 */
	private static void generateDiffs(final DiffProcessor processor, final JsonPointer pointer, final JsonNode source,
			final JsonNode target, Map<JsonPointer, String> arrayObjectPrimaryKeyFields)
			throws IOException, JsonPointerException {

		if (EQUIVALENCE.equivalent(source, target))
			return;
		final NodeType firstType = NodeType.getNodeType(source);
		final NodeType secondType = NodeType.getNodeType(target);

		/*
		 * Handles cases of 1. All Add. 2. All remove. 3. Add [], {}, null. 4.
		 * Remove [], {}, null. in case of Array, Object, ValueNode besides
		 * null.
		 */
		if (source.size() == 0 && target.size() == 0) {
			// sizes are zero for String , Integer, boolean , Empty Object,
			// Empty Array, null {@value}
			if (firstType != secondType) {
				return;
			}
		}

		// All add Elements
		else if (source.size() == 0 && target.size() != 0) {
			if (target.isArray()) {
				for (JsonNode eachElementAtTarget : target) {
					// Adding all Target Array Object one at a time
					processor.valueAdded(pointer.append("-"), eachElementAtTarget);
				}
				return;
			} else {
				// if not array we can add it directly
				processor.valueAdded(pointer, target);
				return;
			}
		}
		// All Remove Elements
		else if (source.size() != 0 && target.size() == 0) {
			if (source.isArray()) {
				for (int sourceIndex = 0; sourceIndex < source.size(); sourceIndex++) {
					// Removing Each source Array Objects one at a time
					processor.arrayObjectValueRemoved(pointer.append(sourceIndex), source.get(sourceIndex));
				}
				// As the whole Node is processed we return
				return;
			} else {
				processor.valueRemoved(pointer, source);
				return;
			}
		}
		/*
		 * If we reach here, it means that neither of both are empty and both
		 * are not equivalent.
		 */

		/*
		 * Now if, Node types differ: generate a replacement operation. FIXME
		 */
		if (firstType != secondType) {
			processor.valueReplaced(pointer, source, target);
			return;
		}
		/*
		 * If we reach this point, it means that both nodes are the same type,
		 * but are not equivalent.
		 *
		 * If this is not a container, generate a replace operation.
		 */
		if (!source.isContainerNode()) {
			processor.valueReplaced(pointer, source, target);
			return;
		}
		/*
		 * If we reach this point, both nodes are either objects or arrays;
		 * delegate.
		 */
		if (firstType == NodeType.OBJECT) {
			// Calculate difference For Objects .
			generateObjectDiffs(processor, pointer, (ObjectNode) source, (ObjectNode) target,
					arrayObjectPrimaryKeyFields);

		} else {
			// Calculate difference For Array's.
			generateArrayDiffs(processor, pointer, (ArrayNode) source, (ArrayNode) target, arrayObjectPrimaryKeyFields);

		}

	}

	/**
	 * This method is used to evaluate difference between objects in JsonNode
	 * 
	 * @param processor
	 * @param pointer
	 * @param source
	 * @param target
	 * @param arrayObjectPrimaryKeyFields
	 * @throws IOException
	 * @throws JsonPointerException
	 * 
	 * 
	 */
	private static void generateObjectDiffs(final DiffProcessor processor, final JsonPointer pointer,
			final ObjectNode source, final ObjectNode target, Map<JsonPointer, String> arrayObjectPrimaryKeyFields)
			throws IOException, JsonPointerException {
		final Set<String> firstFields = Sets.newTreeSet(Sets.newHashSet(source.fieldNames()));
		final Set<String> secondFields = Sets.newTreeSet(Sets.newHashSet(target.fieldNames()));
		// this for loop is for calculating removed elements

		/*
		 * This loop evaluates the Fields at source that are not in target Node
		 */
		for (final String field : Sets.difference(firstFields, secondFields)) {
			// Element To Remove
			JsonNode sourceField = source.get(field);

			if ((sourceField.size() != 0)) {
				// Source removal Array
				for (int index = 0; index < sourceField.size(); index++) {
					// each single array Element Removal
					processor.valueRemoved(pointer.append(field).append(index), sourceField.get(index));
				}
			} else {
				/*
				 * IF Empty node Removal i.e value String, int, etc removal
				 * which has size as zero, so this handles Removal of null or []
				 * or {} cases.
				 */
				if (sourceField.isValueNode() && (!sourceField.isNull())) {
					processor.valueRemoved(pointer.append(field), sourceField);
				}

			}
		}
		/*
		 * This loop evaluates the Fields at target that are not in source Node
		 */
		for (final String field : Sets.difference(secondFields, firstFields)) {
			/*
			 * IF Empty node Added i.e value String, int, etc removal which has
			 * size as zero, so this handles Removal of null or [] or {} cases.
			 * node.size() == 0 means [] or {} node.isvalueNode() means String,
			 * int, Boolean , null etc but we don't support null so we neglect
			 * null
			 */
			if ((target.get(field).size() != 0) || target.get(field).isValueNode()) {
				if (!target.get(field).isNull()) {
					processor.valueAdded(pointer.append(field), target.get(field));
				}
			}
		}
		/*
		 * This loop evaluates the common elements in both nodes
		 */
		for (final String field : Sets.intersection(firstFields, secondFields)) {
			// REPLACE //Neglecting COMMON Elements by Equivalence
			if (!(EQUIVALENCE.equivalent(source.get(field), target.get(field)))) {
				generateDiffs(processor, pointer.append(field), source.get(field), target.get(field),
						arrayObjectPrimaryKeyFields);
			}
		}
	}

	/**
	 * This method is to Find difference between Array Node
	 * 
	 * @param processor
	 * @param pointer
	 * @param source
	 * @param target
	 * @param arrayObjectPrimaryKeyFields
	 * @throws IOException
	 * @throws JsonPointerException
	 * 
	 */
	private static void generateArrayDiffs(final DiffProcessor processor, final JsonPointer pointer,
			final ArrayNode source, final ArrayNode target, final Map<JsonPointer, String> arrayObjectPrimaryKeyFields)
			throws IOException, JsonPointerException {

		final int sourceSize = source.size();

		// Few Added, Few Removed Elements
		if (arrayObjectPrimaryKeyFields.containsKey(pointer)) {

			// Get the Appropriate Key From arrayObjectPrimaryKeyFields Map
			String primaryField = arrayObjectPrimaryKeyFields.get(pointer);

			if (primaryField != null) {

				Map<String, JsonNode> targetKeyNodeMap = new HashMap<>();
				Map<String, JsonNode> sourceKeyNodeMap = new HashMap<>();

				for (JsonNode eachAtTarget : target) {
					JsonNode targetPrimaryFeildValue = eachAtTarget.get(primaryField);
					if (!targetPrimaryFeildValue.isValueNode()) {
						/*
						 * If the code reaches here means that the Given Value
						 * of the arrayObjectPrimaryFeild is an container node
						 */
						throw new IllegalArgumentException("primayKey missing in Array -> Object : {}" + eachAtTarget);
					} else {
						targetKeyNodeMap.put(targetPrimaryFeildValue.asText(), eachAtTarget);
					}
				}

				for (JsonNode eachAtSource : source) {
					JsonNode sourcePrimaryFieldValue = eachAtSource.get(primaryField);
					if (!sourcePrimaryFieldValue.isValueNode()) {
						/*
						 * If the code reaches here means that the Given Value
						 * of the arrayObjectPrimaryFeild is an container node
						 */
						throw new IllegalArgumentException("primayKey missing in Array -> Object : {}" + eachAtSource);
					} else {
						sourceKeyNodeMap.put(sourcePrimaryFieldValue.asText(), eachAtSource);
					}
				}

				Set<String> targetPrimaryFieldValues = targetKeyNodeMap.keySet();
				Set<String> sourcePrimaryFieldValues = sourceKeyNodeMap.keySet();

				/*
				 * we have all target primary values in targetPrimaryFieldValues
				 * we have all source primary values in sourcePrimaryFieldValues
				 */

				// Newly added objects to array
				for (String addedObject : Sets.difference(targetPrimaryFieldValues, sourcePrimaryFieldValues)) {
					processor.valueAdded(pointer.append("-"), targetKeyNodeMap.get(addedObject));
				}

				// old removed objects in array
				for (String removedObject : Sets.difference(sourcePrimaryFieldValues, targetPrimaryFieldValues)) {
					JsonNode sourceRemovedObject = sourceKeyNodeMap.get(removedObject);
					for (int i = 0; i < sourceSize; i++) {
						if (source.get(i).equals(sourceRemovedObject)) {
							processor.arrayObjectValueRemoved(pointer.append(i), source.get(i));
						}
					}
				}

				// common Key value with same or different other attributes
				for (String commonOrReplaceObject : Sets.intersection(sourcePrimaryFieldValues,
						targetPrimaryFieldValues)) {

					JsonNode sourceObject = sourceKeyNodeMap.get(commonOrReplaceObject);
					JsonNode targetObject = targetKeyNodeMap.get(commonOrReplaceObject);
					// neglect common elements
					if (!sourceObject.equals(targetObject)) {
						for (int i = 0; i < sourceSize; i++) {
							if (source.get(i).equals(sourceObject)) {
								generateCustomDiffs(processor, pointer.append(i), sourceObject, targetObject);
							}
						}
					}
				}
			} else {
				// As Given Key is Null, treat whole Object itself as a Key.
				generateArrayDiffForNullOrNoKey(processor, pointer, source, target);
			}
		} else {
			/*
			 * Key is not given, so treat whole object as Key to calculate diff
			 */
			generateArrayDiffForNullOrNoKey(processor, pointer, source, target);
		}

	}

	/**
	 * @param processor
	 * @param pointer
	 * @param source
	 * @param target
	 *            This Method is used to evaluate custom replace operation
	 *            within an Array.
	 */
	public static void generateCustomDiffs(final DiffProcessor processor, JsonPointer pointer, final JsonNode source,
			final JsonNode target) {
		final Set<String> sourceFields = Sets.newTreeSet(Sets.newHashSet(source.fieldNames()));
		final Set<String> targetFields = Sets.newTreeSet(Sets.newHashSet(target.fieldNames()));
		for (String field : sourceFields) {
			if (!(source.get(field).equals(target.get(field)))) {
				processor.arrayObjectValueReplaced(pointer.append(field), source, target.get(field));
			}
		}
		for (final String field : Sets.difference(targetFields, sourceFields)) {
			processor.arrayObjectValueReplaced(pointer.append(field), source, target.get(field));
		}
	}

	/**
	 * @param processor
	 * @param pointer
	 * @param source
	 * @param target
	 *            This method is invoked to find diff in an array element for
	 *            which key is null or not specified in the Map<JsonPointer,
	 *            String> provided by user.
	 */
	private static void generateArrayDiffForNullOrNoKey(final DiffProcessor processor, final JsonPointer pointer,
			final ArrayNode source, final ArrayNode target) {
		{
			// Treat Whole Thing as an Key itself

			List<JsonNode> targetList = Lists.newArrayList(target.iterator());
			List<JsonNode> sourceList = Lists.newArrayList(source.iterator());
			List<JsonNode> sourceListcopy = sourceList;

			// All removed elements are in sourceList
			sourceList.removeAll(targetList);

			for (int i = 0; i < source.size(); i++) {
				// Removing each old element at source
				if (sourceList.contains(source.get(i))) {
					processor.arrayObjectValueRemoved(pointer.append(i), source.get(i));
				}
			}

			// All newly added Elements are in taargetList
			targetList.removeAll(sourceListcopy);

			// Adding each new Element at Target
			for (JsonNode eachAddedInTarget : targetList) {
				processor.valueAdded(pointer.append("-"), eachAddedInTarget);
			}

		}
	}

}
