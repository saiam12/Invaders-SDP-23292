package engine;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive unit tests for GsonJsonMapper class.
 * Tests JSON serialization/deserialization functionality.
 */
class GsonJsonMapperTest {

    private GsonJsonMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new GsonJsonMapper();
    }

    @Test
    void testConstructor_InitializesGson() {
        assertNotNull(mapper, "GsonJsonMapper should be initialized");
    }

    @Test
    void testToJson_SimpleObject() {
        TestObject obj = new TestObject("test", 42);
        String json = mapper.toJson(obj);
        
        assertNotNull(json, "JSON should not be null");
        assertTrue(json.contains("\"name\":\"test\""), "JSON should contain name field");
        assertTrue(json.contains("\"value\":42"), "JSON should contain value field");
    }

    @Test
    void testToJson_NullObject() {
        String json = mapper.toJson(null);
        assertEquals("null", json, "Null object should serialize to 'null'");
    }

    @Test
    void testToJson_EmptyString() {
        String json = mapper.toJson("");
        assertNotNull(json, "Empty string should serialize");
        assertEquals("\"\"", json, "Empty string should be quoted");
    }

    @Test
    void testToJson_Integer() {
        String json = mapper.toJson(123);
        assertEquals("123", json, "Integer should serialize correctly");
    }

    @Test
    void testToJson_Boolean() {
        String jsonTrue = mapper.toJson(true);
        String jsonFalse = mapper.toJson(false);
        
        assertEquals("true", jsonTrue, "true should serialize correctly");
        assertEquals("false", jsonFalse, "false should serialize correctly");
    }

    @Test
    void testToJson_StringWithSpecialCharacters() {
        String json = mapper.toJson("Hello \"World\"\n\t");
        assertNotNull(json, "String with special chars should serialize");
        assertTrue(json.contains("\\\""), "Quotes should be escaped");
    }

    @Test
    void testToJson_ObjectWithNestedObject() {
        NestedObject nested = new NestedObject("outer", new TestObject("inner", 99));
        String json = mapper.toJson(nested);
        
        assertNotNull(json, "Nested object should serialize");
        assertTrue(json.contains("\"outer\":\"outer\""), "Should contain outer field");
        assertTrue(json.contains("\"inner\""), "Should contain inner object");
    }

    @Test
    void testFromJson_SimpleObject() {
        String json = "{\"name\":\"test\",\"value\":42}";
        TestObject obj = mapper.fromJson(json, TestObject.class);
        
        assertNotNull(obj, "Deserialized object should not be null");
        assertEquals("test", obj.name, "Name should match");
        assertEquals(42, obj.value, "Value should match");
    }

    @Test
    void testFromJson_NullString() {
        TestObject obj = mapper.fromJson(null, TestObject.class);
        assertNull(obj, "Null JSON should return null");
    }

    @Test
    void testFromJson_EmptyString() {
        assertThrows(Exception.class, () -> {
            mapper.fromJson("", TestObject.class);
        }, "Empty string should throw exception");
    }

    @Test
    void testFromJson_InvalidJson() {
        assertThrows(Exception.class, () -> {
            mapper.fromJson("not valid json", TestObject.class);
        }, "Invalid JSON should throw exception");
    }

    @Test
    void testFromJson_Integer() {
        Integer num = mapper.fromJson("456", Integer.class);
        assertNotNull(num, "Integer should deserialize");
        assertEquals(456, num, "Value should match");
    }

    @Test
    void testFromJson_Boolean() {
        Boolean bool = mapper.fromJson("true", Boolean.class);
        assertNotNull(bool, "Boolean should deserialize");
        assertTrue(bool, "Value should be true");
    }

    @Test
    void testFromJson_String() {
        String str = mapper.fromJson("\"hello\"", String.class);
        assertNotNull(str, "String should deserialize");
        assertEquals("hello", str, "Value should match");
    }

    @Test
    void testRoundTrip_SimpleObject() {
        TestObject original = new TestObject("roundtrip", 777);
        String json = mapper.toJson(original);
        TestObject deserialized = mapper.fromJson(json, TestObject.class);
        
        assertNotNull(deserialized, "Deserialized object should not be null");
        assertEquals(original.name, deserialized.name, "Name should survive round trip");
        assertEquals(original.value, deserialized.value, "Value should survive round trip");
    }

    @Test
    void testRoundTrip_NestedObject() {
        NestedObject original = new NestedObject("parent", new TestObject("child", 123));
        String json = mapper.toJson(original);
        NestedObject deserialized = mapper.fromJson(json, NestedObject.class);
        
        assertNotNull(deserialized, "Deserialized object should not be null");
        assertEquals(original.outer, deserialized.outer, "Outer field should match");
        assertEquals(original.inner.name, deserialized.inner.name, "Inner name should match");
        assertEquals(original.inner.value, deserialized.inner.value, "Inner value should match");
    }

    @Test
    void testToJson_ArrayOfObjects() {
        TestObject[] array = {
            new TestObject("first", 1),
            new TestObject("second", 2),
            new TestObject("third", 3)
        };
        
        String json = mapper.toJson(array);
        assertNotNull(json, "Array should serialize");
        assertTrue(json.contains("\"first\""), "Should contain first element");
        assertTrue(json.contains("\"second\""), "Should contain second element");
        assertTrue(json.contains("\"third\""), "Should contain third element");
    }

    @Test
    void testFromJson_MissingFields() {
        String json = "{\"name\":\"incomplete\"}";
        TestObject obj = mapper.fromJson(json, TestObject.class);
        
        assertNotNull(obj, "Object should deserialize even with missing fields");
        assertEquals("incomplete", obj.name, "Name should be set");
        assertEquals(0, obj.value, "Missing value should default to 0");
    }

    @Test
    void testFromJson_ExtraFields() {
        String json = "{\"name\":\"test\",\"value\":42,\"extra\":\"ignored\"}";
        TestObject obj = mapper.fromJson(json, TestObject.class);
        
        assertNotNull(obj, "Object should deserialize with extra fields");
        assertEquals("test", obj.name, "Name should be set");
        assertEquals(42, obj.value, "Value should be set");
    }

    @Test
    void testToJson_NullFields() {
        TestObject obj = new TestObject(null, 0);
        String json = mapper.toJson(obj);
        
        assertNotNull(json, "Object with null fields should serialize");
    }

    @Test
    void testFromJson_NullFields() {
        String json = "{\"name\":null,\"value\":42}";
        TestObject obj = mapper.fromJson(json, TestObject.class);
        
        assertNotNull(obj, "Object should deserialize with null fields");
        assertNull(obj.name, "Name should be null");
        assertEquals(42, obj.value, "Value should be set");
    }

    @Test
    void testToJson_LargeNumber() {
        String json = mapper.toJson(Long.MAX_VALUE);
        assertNotNull(json, "Large number should serialize");
        assertTrue(json.contains(String.valueOf(Long.MAX_VALUE)), "Should contain the number");
    }

    @Test
    void testToJson_UnicodeCharacters() {
        String json = mapper.toJson("Hello 世界 🌍");
        assertNotNull(json, "Unicode should serialize");
        assertTrue(json.length() > 0, "JSON should have content");
    }

    @Test
    void testMultipleSerializations_SameObject() {
        TestObject obj = new TestObject("consistent", 100);
        String json1 = mapper.toJson(obj);
        String json2 = mapper.toJson(obj);
        
        assertEquals(json1, json2, "Same object should produce same JSON");
    }

    @Test
    void testMultipleDeserializations_SameJson() {
        String json = "{\"name\":\"consistent\",\"value\":100}";
        TestObject obj1 = mapper.fromJson(json, TestObject.class);
        TestObject obj2 = mapper.fromJson(json, TestObject.class);
        
        assertEquals(obj1.name, obj2.name, "Deserialized objects should have same name");
        assertEquals(obj1.value, obj2.value, "Deserialized objects should have same value");
    }

    // Helper classes for testing
    static class TestObject {
        String name;
        int value;

        TestObject() {}

        TestObject(String name, int value) {
            this.name = name;
            this.value = value;
        }
    }

    static class NestedObject {
        String outer;
        TestObject inner;

        NestedObject() {}

        NestedObject(String outer, TestObject inner) {
            this.outer = outer;
            this.inner = inner;
        }
    }
}