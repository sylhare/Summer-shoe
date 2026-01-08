package sun.flower.cache;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import tools.jackson.databind.ObjectMapper;
import sun.flower.model.Example;

public class MapperTest {

    protected ObjectMapper objectMapper = new MemcachedConfiguration().objectMapper();
    private final Example example = new Example("hello");

    @Test
    public void serializeTest() {
        assertEquals("{\"name\":\"hello\"}", objectMapper.writeValueAsString(example));
    }

    @Test
    public void deserializeTest() {
        Example deserializedExample = objectMapper.readValue("{\"name\":\"hello\"}", Example.class);
        assertEquals(example.name, deserializedExample.name);
    }
}
