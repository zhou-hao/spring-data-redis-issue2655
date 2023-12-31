package com.example.demo;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.RedisElementReader;
import org.springframework.data.redis.serializer.RedisElementWriter;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.lang.Nullable;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Objects;


class Issue2655Tests {


    @Test
    void testRedisElementReaderReturnNull() {

        LettuceConnectionFactory factory
                = new LettuceConnectionFactory(
                LettuceConnectionFactory
                        .createRedisConfiguration("redis://localhost:6379")
        );
        factory.afterPropertiesSet();
        ObjectRedisElementReader reader = new ObjectRedisElementReader();

        RedisElementReader<String> keyReader = RedisElementReader.from(RedisSerializer.string());
        RedisElementWriter<String> keyWriter = RedisElementWriter.from(RedisSerializer.string());

        RedisSerializationContext<String, Object> serializationContext = RedisSerializationContext
                .<String, Object>newSerializationContext()
                .key(keyReader, keyWriter)
                .value(reader, RedisElementWriter.from(RedisSerializer.java()))
                .hashKey(keyReader, keyWriter)
                .hashValue(reader, RedisElementWriter.from(RedisSerializer.java()))
                .build();
        ReactiveRedisTemplate<String, Object> template = new ReactiveRedisTemplate<>(factory, serializationContext);

        template.opsForSet()
                .add("test-set", "a", "b", "c")
                .block();

        List<Object> test = template
                .opsForSet()
                .members("test-set")
                .collectList()
                .block();

        Assertions.assertNotNull(test);
        System.out.println(test);
        Assertions.assertEquals(2, test.size());
    }


    static class ObjectRedisElementReader implements RedisElementReader<Object> {

        static final RedisElementReader<Object> reader = RedisElementReader.from(RedisSerializer.java());

        @Override
        @Nullable
        public Object read(ByteBuffer buffer) {
            Object val = reader.read(buffer);
            // mock return null value
            return Objects.equals("b", val) ? null : val;
        }

    }


}
