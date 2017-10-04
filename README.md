## Confluxsys builds now enabled

Builds are now verified by Confluxsys

## What this is

This is an implementation of [RFC 6902 (JSON Patch)](http://tools.ietf.org/html/rfc6902) and [RFC
7386 (JSON
Merge Patch)](http://tools.ietf.org/html/rfc7386) written in Java,
which uses [Jackson](https://github.com/FasterXML/jackson-databind) (2.2.x) at its core.
There are a few customization for more accuracy but it still supports all the original standards of the implementations mentioned. 

Its features are:

* {de,}serialization of JSON Patch and JSON Merge Patch instances with Jackson;
* full support for RFC 6902 operations, including `test`;
* JSON "diff" (RFC 6902 and custom) with custom operations factorization.


## Using it in your project

In-progress

## JSON "diff" factorization

When computing the difference between two JSON texts (in the form of `JsonNode` instances), the diff
will factorize value removals and additions as moves and copies.

For instance, given this node to patch:

```json
{ "a": "b" }
```

in order to obtain:

```json
{ "c": "b" }
```

the implementation will return the following patch:

```json
[ { "op": "move", "from": "/a", "path": "/c" } ]
```

## JSON "diff" customization

The Custom diff only compute difference in form of add, remove and replace. The add, remove and replace operation is same as specified in rfc6901 but only in case of object,there is change in case of array diff operations the remove and replace operations have attached extra {original_value: value} which helps us to keep track of the state before the difference was calculated. 

In case of Absense of Key the Algorithm treats whole object as Key and calculates fine-grained difference between the JsonNode.

This Library Supports Custom Operation in Case of Array
as ...
```json
Remove Operation
source     :  { "a": [ { "a": "b" }, { "a": "x", "b": "c"	} ] }
target     :  { "a": [ { "a": "b" } ] }
Difference :  [ { "op": "remove", "path": "/a/1", "original_value": { "a": "x", "b": "c" } } ]
```

and: 
 
```json
Replace Operation
source     : { "a": [ { "a": "b", "b": "a" } ] }
target     : { "a": [ { "a": "b", "b": "c" } ] }
Difference : [ { "op": "replace", "path": "/a/0/b", "value": "c", "original_value": { "a": "b", "b": "a" } } ] 
```
It is able to do even more than that. See the test files in the project.

## Note about the `test` operation and numeric value equivalence

RFC 6902 mandates that when testing for numeric values, however deeply nested in the tested value,
a test is successful if the numeric values are _mathematically equal_. That is, JSON texts:

```json
1
```

and:

```json
1.00
```

must be considered equal.

This implementation obeys the RFC; for this, it uses the numeric equivalence of
[jackson-coreutils](https://github.com/fge/jackson-coreutils).

## Sample usage

### JSON Patch

You have to choices to build a `JsonPatch` instance: use Jackson deserialization, or initialize one
directly from a `JsonNode`. Examples:

```
// Using Jackson
final ObjectMapper mapper = new ObjectMapper();
final InputStream in = ...;
final JsonPatch patch = mapper.readValue(in, JsonPatch.class);
// From a JsonNode
final JsonPatch patch = JsonPatch.fromJson(node);
```

You can then apply the patch to your data:

```java
// orig is also a JsonNode
final JsonNode patched = patch.apply(orig);
```

### JSON diff

The main class is `JsonDiff`. It returns the patch as a `JsonPatch` or as a `JsonNode`. Sample usage:

```java
final JsonPatch patch = JsonDiff.asJsonPatch(source, target);
final JsonNode patchNode = JsonDiff.asJson(source, target);
```

### JSON Merge Patch

As for `JsonPatch`, you may use either Jackson or "direct" initialization:

```java
// With Jackson
final JsonMergePatch patch = mapper.readValue(in, JsonMergePatch.class);
// With a JsonNode
final JsonMergePatch patch = JsonMergePatch.fromJson(node);
```

Applying a patch also uses an `.apply()` method:

```java
// orig is also a JsonNode
final JsonNode patched = patch.apply(orig);
```

### JSON diff custom

The main class is `JsonDiff`. It returns the patch as a `JsonPatch` or as a `JsonNode`. Sample usage:

```java
final JsonPatch patch = JsonDiff.asJsonPatch(source, target, attributeKeyFieldMap);
final JsonNode patchNode = JsonDiff.asJson(source, target, attributeKeyFieldMap);
```
attributeKeyFieldMap is the Map of <JsonPointer,String>, read RFC6902 for JsonPointer.
JsonPointer contains all the Key Fields Inside an Array object which you want to treat as primary Key.

### Important note

The API offers **no guarantee at all** about patch "reuse";
that is, the generated patch is only guaranteed to safely transform the given
source to the given target. Do not expect it to give the result you expect on
another source/target pair!
