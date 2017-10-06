## Confluxsys builds now enabled

Builds are now verified by Confluxsys

## What this is

Along will all the previous work to the library by Travis on [json-patch](https://github.com/java-json-tools/json-patch) this library provides a little custom and more accurate difference in form of add, remove and replace only. 

## JSON "diff" 

The Custom diff only compute difference in form of add, remove and replace. The add, remove and replace operation is same as specified in rfc6901 but only in case of object,there is change in case of array diff operations the remove and replace operations have attached extra {original_value: value} which helps us to keep track of the state before the difference was calculated. 

In case of absence of Key the Algorithm treats whole object as Key and calculates fine-grained difference between the JsonNode.

This Library Supports Custom Operation in Case of Array
as ...
```json
provided the Map<JsonPointer, String> has {"/a": "b"}
```

Remove Operation
```json
source     :  { "a": [ { "a": "b" }, { "a": "x", "b": "c" } ] }
target     :  { "a": [ { "a": "b" } ] }
Difference :  [ { "op": "remove", "path": "/a/1", "original_value": { "a": "x", "b": "c" } } ]
```

Replace Operation
```json
source     : { "a": [ { "a": "b", "b": "a" } ] }
target     : { "a": [ { "a": "b", "b": "c" } ] }
Difference : [ { "op": "replace", "path": "/a/0/b", "value": "c", "original_value": { "a": "b", "b": "a" } } ] 
```
It is able to do even more than that. See the test files in the project.


## Sample usage

### JSON diff 

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
