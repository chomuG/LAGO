package com.example.LAGO.service;

import com.example.LAGO.realtime.TickDataSerializer;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class TickChunkReaderService {
    private final RedisTemplate<String, byte[]> redisBin;
    private final StringRedisTemplate redisStr;

    public List<TickDataSerializer.Decoded16B> readChunk(String chunkId) {
        // 1) 메타 읽기
        String metaKey = "ticks:chunk:" + chunkId + ":meta";
        Map<Object,Object> meta = redisStr.opsForHash().entries(metaKey);
        if (meta == null || meta.isEmpty()) throw new IllegalStateException("meta not found: " + chunkId);

        int count    = Integer.parseInt(Objects.toString(meta.get("count")));
        int rawBytes = Integer.parseInt(Objects.toString(meta.get("rawBytes"))); // = count * 16
        LocalDate baseDate = LocalDate.parse(Objects.toString(meta.get("baseDate"))); // yyyy-MM-dd (KST)
        // boolean compressed = Boolean.parseBoolean(Objects.toString(meta.getOrDefault("compressed", "true")));

        // 2) blob 읽기
        String blobKey = "ticks:chunk:" + chunkId + ":blob";
        byte[] blob = redisBin.opsForValue().get(blobKey);
        if (blob == null) throw new IllegalStateException("blob not found: " + chunkId);

        // 3) 압축 해제
        byte[] raw = com.github.luben.zstd.Zstd.decompress(blob, rawBytes);

        // 4) 16B 반복 파싱
        ByteBuffer bb = ByteBuffer.wrap(raw).order(ByteOrder.LITTLE_ENDIAN);
        List<TickDataSerializer.Decoded16B> out = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            out.add(TickDataSerializer.read16B(bb, baseDate));
        }
        return out;
    }
}
