package com.gotokart.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.time.Duration;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class S3StorageService {

    private static final Map<String, String> EXT = Map.of(
            "image/jpeg", ".jpg",
            "image/jpg",  ".jpg",
            "image/png",  ".png",
            "image/webp", ".webp"
    );
    private static final Set<String> ALLOWED = EXT.keySet();

    private final S3Presigner presigner;
    private final S3Client s3Client;

    @Value("${aws.s3.bucket}")
    private String bucket;

    @Value("${aws.s3.presign-ttl-seconds}")
    private long ttlSeconds;

    @Value("${aws.s3.public-base-url}")
    private String publicBaseUrl;

    @Value("${aws.region}")
    private String region;

    public PresignedUpload presignUpload(String contentType) {
        String ct = (contentType == null) ? "" : contentType.toLowerCase();
        if (!ALLOWED.contains(ct)) {
            throw new IllegalArgumentException(
                    "Unsupported contentType: " + contentType + ". Allowed: " + ALLOWED);
        }

        String key = "products/" + UUID.randomUUID() + EXT.get(ct);

        PutObjectRequest putReq = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(ct)
                .build();

        PutObjectPresignRequest presignReq = PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofSeconds(ttlSeconds))
                .putObjectRequest(putReq)
                .build();

        var signed = presigner.presignPutObject(presignReq);

        return new PresignedUpload(
                signed.url().toString(),
                key,
                ct,
                ttlSeconds,
                publicUrl(key)
        );
    }

    public String publicUrl(String key) {
        if (key == null || key.isBlank()) return null;
        if (publicBaseUrl != null && !publicBaseUrl.isBlank()) {
            String base = publicBaseUrl.endsWith("/")
                    ? publicBaseUrl.substring(0, publicBaseUrl.length() - 1)
                    : publicBaseUrl;
            return base + "/" + key;
        }
        return "https://" + bucket + ".s3." + region + ".amazonaws.com/" + key;
    }

    public void uploadObject(String key, byte[] data, String contentType) {
        s3Client.putObject(
                PutObjectRequest.builder()
                        .bucket(bucket)
                        .key(key)
                        .contentType(contentType)
                        .build(),
                RequestBody.fromBytes(data));
    }

    public void deleteObject(String key) {
        if (key == null || key.isBlank()) return;
        try {
            s3Client.deleteObject(DeleteObjectRequest.builder()
                    .bucket(bucket).key(key).build());
        } catch (S3Exception ignored) {
            // best-effort cleanup; don't fail the API call if S3 delete fails
        }
    }

    public record PresignedUpload(
            String url,
            String key,
            String contentType,
            long expiresInSeconds,
            String publicUrl
    ) {}
}
