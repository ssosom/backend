package com.sosom.s3.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import lombok.RequiredArgsConstructor;
import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class S3Service {

    @Value("${s3.folderName}")
    private String folderName;
    @Value("${s3.bucketName}")
    private String bucketName;

    private final AmazonS3 s3Client;

    public String base64Upload(String base64Encoding,String type){
        String fileName = folderName + UUID.randomUUID();
        byte[] decodeBytes = Base64.decodeBase64(base64Encoding);
        InputStream inputStream = new ByteArrayInputStream(decodeBytes);

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(decodeBytes.length);
        metadata.setContentType(type);

        PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName,fileName,inputStream,metadata)
                .withCannedAcl(CannedAccessControlList.PublicRead);

        s3Client.putObject(putObjectRequest);

        return fileName;
    }
}
