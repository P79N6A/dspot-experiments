package com.jayway.jsonpath;


import Option.DEFAULT_PATH_LEAF_TO_NULL;
import Option.SUPPRESS_EXCEPTIONS;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.jayway.jsonpath.spi.json.GsonJsonProvider;
import com.jayway.jsonpath.spi.mapper.GsonMappingProvider;
import com.jayway.jsonpath.spi.mapper.MappingException;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import org.junit.Test;


public class GsonJsonProviderTest extends BaseTest {
    private static final String JSON = "[" + (((((((((((((((((("{\n" + "   \"foo\" : \"foo0\",\n") + "   \"bar\" : 0,\n") + "   \"baz\" : true,\n") + "   \"gen\" : {\"eric\" : \"yepp\"}") + "},") + "{\n") + "   \"foo\" : \"foo1\",\n") + "   \"bar\" : 1,\n") + "   \"baz\" : true,\n") + "   \"gen\" : {\"eric\" : \"yepp\"}") + "},") + "{\n") + "   \"foo\" : \"foo2\",\n") + "   \"bar\" : 2,\n") + "   \"baz\" : true,\n") + "   \"gen\" : {\"eric\" : \"yepp\"}") + "}") + "]");

    @Test
    public void json_can_be_parsed() {
        JsonObject node = JsonPath.using(BaseTest.GSON_CONFIGURATION).parse(BaseTest.JSON_DOCUMENT).read("$");
        assertThat(node.get("string-property").getAsString()).isEqualTo("string-value");
    }

    @Test
    public void strings_are_unwrapped() {
        JsonElement node = JsonPath.using(BaseTest.GSON_CONFIGURATION).parse(BaseTest.JSON_DOCUMENT).read("$.string-property");
        String unwrapped = JsonPath.using(BaseTest.GSON_CONFIGURATION).parse(BaseTest.JSON_DOCUMENT).read("$.string-property", String.class);
        assertThat(unwrapped).isEqualTo("string-value");
        assertThat(unwrapped).isEqualTo(node.getAsString());
    }

    @Test
    public void ints_are_unwrapped() {
        JsonElement node = JsonPath.using(BaseTest.GSON_CONFIGURATION).parse(BaseTest.JSON_DOCUMENT).read("$.int-max-property");
        int unwrapped = JsonPath.using(BaseTest.GSON_CONFIGURATION).parse(BaseTest.JSON_DOCUMENT).read("$.int-max-property", int.class);
        assertThat(unwrapped).isEqualTo(Integer.MAX_VALUE);
        assertThat(unwrapped).isEqualTo(node.getAsInt());
    }

    @Test
    public void longs_are_unwrapped() {
        JsonElement node = JsonPath.using(BaseTest.GSON_CONFIGURATION).parse(BaseTest.JSON_DOCUMENT).read("$.long-max-property");
        long val = JsonPath.using(BaseTest.GSON_CONFIGURATION).parse(BaseTest.JSON_DOCUMENT).read("$.long-max-property", Long.class);
        assertThat(val).isEqualTo(Long.MAX_VALUE);
        assertThat(val).isEqualTo(node.getAsLong());
    }

    @Test
    public void doubles_are_unwrapped() {
        final String json = "{double-property = 56.78}";
        JsonElement node = JsonPath.using(BaseTest.GSON_CONFIGURATION).parse(json).read("$.double-property");
        Double val = JsonPath.using(BaseTest.GSON_CONFIGURATION).parse(json).read("$.double-property", Double.class);
        assertThat(val).isEqualTo(56.78);
        assertThat(val).isEqualTo(node.getAsDouble());
    }

    @Test
    public void bigdecimals_are_unwrapped() {
        final BigDecimal bd = BigDecimal.valueOf(Long.MAX_VALUE).add(BigDecimal.valueOf(10.5));
        final String json = ("{bd-property = " + (bd.toString())) + "}";
        JsonElement node = JsonPath.using(BaseTest.GSON_CONFIGURATION).parse(json).read("$.bd-property");
        BigDecimal val = JsonPath.using(BaseTest.GSON_CONFIGURATION).parse(json).read("$.bd-property", BigDecimal.class);
        assertThat(val).isEqualTo(bd);
        assertThat(val).isEqualTo(node.getAsBigDecimal());
    }

    @Test
    public void small_bigdecimals_are_unwrapped() {
        final BigDecimal bd = BigDecimal.valueOf(10.5);
        final String json = ("{bd-property = " + (bd.toString())) + "}";
        JsonElement node = JsonPath.using(BaseTest.GSON_CONFIGURATION).parse(json).read("$.bd-property");
        BigDecimal val = JsonPath.using(BaseTest.GSON_CONFIGURATION).parse(json).read("$.bd-property", BigDecimal.class);
        assertThat(val).isEqualTo(bd);
        assertThat(val).isEqualTo(node.getAsBigDecimal());
    }

    @Test
    public void bigintegers_are_unwrapped() {
        final BigInteger bi = BigInteger.valueOf(Long.MAX_VALUE).add(BigInteger.TEN);
        final String json = ("{bi-property = " + (bi.toString())) + "}";
        JsonElement node = JsonPath.using(BaseTest.GSON_CONFIGURATION).parse(json).read("$.bi-property");
        BigInteger val = JsonPath.using(BaseTest.GSON_CONFIGURATION).parse(json).read("$.bi-property", BigInteger.class);
        assertThat(val).isEqualTo(bi);
        assertThat(val).isEqualTo(node.getAsBigInteger());
    }

    @Test
    public void small_bigintegers_are_unwrapped() {
        final BigInteger bi = BigInteger.valueOf(Long.MAX_VALUE);
        final String json = ("{bi-property = " + (bi.toString())) + "}";
        JsonElement node = JsonPath.using(BaseTest.GSON_CONFIGURATION).parse(json).read("$.bi-property");
        BigInteger val = JsonPath.using(BaseTest.GSON_CONFIGURATION).parse(json).read("$.bi-property", BigInteger.class);
        assertThat(val).isEqualTo(bi);
        assertThat(val).isEqualTo(node.getAsBigInteger());
    }

    @Test
    public void int_to_long_mapping() {
        assertThat(JsonPath.using(BaseTest.GSON_CONFIGURATION).parse("{\"val\": 1}").read("val", Long.class)).isEqualTo(1L);
    }

    @Test
    public void an_Integer_can_be_converted_to_a_Double() {
        assertThat(JsonPath.using(BaseTest.GSON_CONFIGURATION).parse("{\"val\": 1}").read("val", Double.class)).isEqualTo(1.0);
    }

    @Test
    public void list_of_numbers() {
        JsonArray objs = JsonPath.using(BaseTest.GSON_CONFIGURATION).parse(BaseTest.JSON_DOCUMENT).read("$.store.book[*].display-price");
        assertThat(objs.iterator()).extracting("asDouble").containsExactly(8.95, 12.99, 8.99, 22.99);
    }

    @Test
    public void an_object_can_be_mapped_to_pojo() {
        String json = "{\n" + ((("   \"foo\" : \"foo\",\n" + "   \"bar\" : 10,\n") + "   \"baz\" : true\n") + "}");
        GsonJsonProviderTest.TestClazz testClazz = JsonPath.using(BaseTest.GSON_CONFIGURATION).parse(json).read("$", GsonJsonProviderTest.TestClazz.class);
        assertThat(testClazz.foo).isEqualTo("foo");
        assertThat(testClazz.bar).isEqualTo(10L);
        assertThat(testClazz.baz).isEqualTo(true);
    }

    @Test
    public void test_type_ref() throws IOException {
        TypeRef<List<GsonJsonProviderTest.FooBarBaz<GsonJsonProviderTest.Gen>>> typeRef = new TypeRef<List<GsonJsonProviderTest.FooBarBaz<GsonJsonProviderTest.Gen>>>() {};
        List<GsonJsonProviderTest.FooBarBaz<GsonJsonProviderTest.Gen>> list = JsonPath.using(BaseTest.GSON_CONFIGURATION).parse(GsonJsonProviderTest.JSON).read("$", typeRef);
        assertThat(list.get(0).gen.eric).isEqualTo("yepp");
    }

    @Test(expected = MappingException.class)
    public void test_type_ref_fail() throws IOException {
        TypeRef<List<GsonJsonProviderTest.FooBarBaz<Integer>>> typeRef = new TypeRef<List<GsonJsonProviderTest.FooBarBaz<Integer>>>() {};
        JsonPath.using(BaseTest.GSON_CONFIGURATION).parse(GsonJsonProviderTest.JSON).read("$", typeRef);
    }

    // https://github.com/json-path/JsonPath/issues/351
    @Test
    public void no_error_when_mapping_null() throws IOException {
        Configuration configuration = Configuration.builder().mappingProvider(new GsonMappingProvider()).jsonProvider(new GsonJsonProvider()).options(DEFAULT_PATH_LEAF_TO_NULL, SUPPRESS_EXCEPTIONS).build();
        String json = "{\"M\":[]}";
        String result = JsonPath.using(configuration).parse(json).read("$.M[0].A[0]", String.class);
        assertThat(result).isNull();
    }

    public static class FooBarBaz<T> {
        public T gen;

        public String foo;

        public Long bar;

        public boolean baz;
    }

    public static class Gen {
        public String eric;
    }

    public static class TestClazz {
        public String foo;

        public Long bar;

        public boolean baz;
    }
}
