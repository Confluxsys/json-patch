/**
 * 
 */
package com.github.fge.jsonpatch;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.github.fge.jackson.jsonpointer.JsonPointer;

/**
 * @author Ritesh
 *
 */
public abstract class PathOldNewValueOperation extends JsonPatchOperation {

	@JsonSerialize
	protected final JsonNode oldValue;

	@JsonSerialize
	protected final JsonNode newValue;

	/**
	 * @param op
	 * @param path
	 * @param oldValue JSON oldValue
	 * @param newValue JSON newValue
	 */
	protected PathOldNewValueOperation(final String op, final JsonPointer path, final JsonNode oldValue, final JsonNode newValue) {
		super(op, path);
		this.oldValue = oldValue;
		this.newValue = newValue;
	}

	public final void serialize(final JsonGenerator jgen, final SerializerProvider provider) throws IOException, JsonProcessingException {
		jgen.writeStartObject();
		jgen.writeStringField(JsonDiffConstants.OPERATION, op);
		jgen.writeStringField(JsonDiffConstants.PATH, path.toString());
		jgen.writeFieldName(JsonDiffConstants.VALUE);
		jgen.writeTree(newValue);
		jgen.writeFieldName(JsonDiffConstants.ORIGINAL_VALUE);
		jgen.writeTree(oldValue);
		jgen.writeEndObject();
	}

	public final void serializeWithType(final JsonGenerator jgen, final SerializerProvider provider, final TypeSerializer typeSer)
			throws IOException, JsonProcessingException {
		serialize(jgen, provider);
	}

	@Override
	public final String toString() {
		return JsonDiffConstants.OPERATION + ": " + op + "; " + JsonDiffConstants.PATH + ": \"" + path + "\"; " + JsonDiffConstants.VALUE + " : "
				+ newValue + "; " + JsonDiffConstants.ORIGINAL_VALUE + ": \"" + oldValue;
	}
}
