package com.gotokart.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.net.URI;

@Configuration
public class S3Config {

    /**
     * Without this override, the SDK uses the legacy global hostname
     * bucket.s3.amazonaws.com for us-east-1 presigned URLs. Browsers may
     * fail CORS preflight when that endpoint redirects to the regional host.
     * Forcing https://s3.{region}.amazonaws.com makes PUT URLs use the
     * regional virtual-hosted style (e.g. bucket.s3.us-east-1.amazonaws.com).
     */
    private static URI regionalS3Endpoint(String region) {
        return URI.create("https://s3." + region + ".amazonaws.com");
    }

    @Bean
    public S3Client s3Client(@Value("${aws.region}") String region) {
        return S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(DefaultCredentialsProvider.create())
                .endpointOverride(regionalS3Endpoint(region))
                .build();
    }

    @Bean
    public S3Presigner s3Presigner(@Value("${aws.region}") String region) {
        return S3Presigner.builder()
                .region(Region.of(region))
                .credentialsProvider(DefaultCredentialsProvider.create())
                .endpointOverride(regionalS3Endpoint(region))
                .build();
    }
}
