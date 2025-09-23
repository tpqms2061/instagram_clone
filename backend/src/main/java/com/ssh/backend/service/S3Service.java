package com.ssh.backend.service;

import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.ssh.backend.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Date;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class S3Service {

    private final AmazonS3 s3client;

    @Value("${AWS_BUCKET_NAME}")
    private String bucketName;

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;
    private static final int IMAGE_SIZE = 1080;

    //multipartFile : Body의 from -data의 파일을 가르킴
    public String uploadFile(MultipartFile file, String folder) {
        if (s3client == null) {
            log.warn("S3 client not configured. Using local storage fallback.");
            throw new RuntimeException("S3 not configured");
        }
        //유효성 검사 및
        validateFile(file);
        String fileName = generateFileName(file, folder);

        try {
            BufferedImage originalImage = ImageIO.read(file.getInputStream());
            if (originalImage == null) {
                throw new BadRequestException("Invalid image file");
            }
// 1대1 비율로 만들기
            BufferedImage squareImage = cropToSquare(originalImage);

            BufferedImage resizedImage = resizeImage(squareImage, IMAGE_SIZE, IMAGE_SIZE);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ImageIO.write(resizedImage, "jpg", outputStream);
            byte[] imageBytes = outputStream.toByteArray();
            InputStream inputStream = new ByteArrayInputStream(imageBytes);

          /*  // === (1) 업로드할 객체의 메타데이터 설정 ===
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType(file.getContentType()); // 파일 MIME 타입
            metadata.setContentLength(file.getSize());       // 파일 크기 (바이트 단위)

            // === (2) 업로드 요청 객체 생성 ===
            // PutObjectRequest : "S3에 이 파일을 이런 메타데이터와 함께 업로드해줘"
            PutObjectRequest putObjectRequest = new PutObjectRequest(
                    bucketName,                 // 업로드할 버킷 이름
                    fileName,                   // 버킷 내에서 저장될 객체 이름(key)
                    file.getInputStream(),  //파일은 데이터가 많으니까 스트림형태로
                    metadata                // 위에서 만든 메타데이터

            );*/
            ImageIO.write(resizedImage, "jpg", outputStream);
            PutObjectRequest putObjectRequest = getPutObjectRequest(file, outputStream, fileName);

            // === (3) 업로드 실행 ===
            // AmazonS3Client(s3client)가 AWS S3에 업로드 요청을 보냄
            s3client.putObject(putObjectRequest);
            log.info("Successfully upload file to S3 : {}", fileName);
            return fileName;

        } catch (IOException e) {
            log.error("Failed to upload file to S3", e);
            throw new RuntimeException("Failed to upload filed ", e);
        }
    }

    private PutObjectRequest getPutObjectRequest(MultipartFile file, ByteArrayOutputStream outputStream, String fileName) {
        byte[] imageBytes = outputStream.toByteArray();
        InputStream inputStream = new ByteArrayInputStream(imageBytes);

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType("image/jpeg");
        metadata.setContentLength(imageBytes.length);

        return new PutObjectRequest(
                bucketName,
                fileName,
                inputStream,
                metadata
        );
    }

//객체 만들어서 필요한 넣어주고 실행후 반환
    public String generatePresignedUrl(String fileKey, int expirationMinutes) {
        if (s3client == null || fileKey == null) {
            return null;
        }

        Date expiration = new Date();
        long expTimeMillis = expiration.getTime();
        expTimeMillis += 1000L * 60 * expirationMinutes;
        expiration.setTime(expTimeMillis);

        GeneratePresignedUrlRequest generatePresignedUrlRequest = new GeneratePresignedUrlRequest(
                bucketName,
                fileKey
        )
                .withMethod(HttpMethod.GET)
                .withExpiration(expiration);

        URL url = s3client.generatePresignedUrl(generatePresignedUrlRequest);
        return url.toString();
    }

    private BufferedImage cropToSquare(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        int squareSize = Math.min(width, height);

        int x = (width - squareSize) / 2;
        int y = (height - squareSize) /2;

        return image.getSubimage(x, y, squareSize, squareSize);

    }

    //자바버전의 포토샵 => 큰 이미지를 작은 이미지로 변환시킬때 사용
    private BufferedImage resizeImage(BufferedImage originalImage, int targetWidth, int targetHeight) {
        //그림판 만들기
        BufferedImage resizedImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
        //붓 팔레트 준비
        Graphics2D graphics2D = resizedImage.createGraphics();

        //INTERPOLATION :
        graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        graphics2D.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        //계단현상 방지
        graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);


        graphics2D.drawImage(originalImage, 0, 0, targetWidth, targetHeight, null);
        graphics2D.dispose();

        return resizedImage;
    }


    //파일 유효성 검사
    private void validateFile(MultipartFile file) {
        //1. 파일 유무 확인
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        //2. 파일이 적합한 용량인지 확인
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("File size exceeds 5MB");
        }

        //3. 파일의 type이 img 인지 확인
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("Only image files are allowed");
        }
    }

    //업로드할때 파일이름 정하는 것  // 사용자에게 이름을 맡기면 관리하기가 까다로워서 우리가 이름을 바꿔서 저장
    private String generateFileName(MultipartFile file, String folder) {
        String originalFileName = file.getOriginalFilename();
        String extension = originalFileName != null && originalFileName.contains(".")
                ? originalFileName.substring(originalFileName.lastIndexOf("."))
                : "";

        return folder + "/" + UUID.randomUUID().toString() + "jpg";
        //랜덤한 유효한 아이디 생성
    }

}
