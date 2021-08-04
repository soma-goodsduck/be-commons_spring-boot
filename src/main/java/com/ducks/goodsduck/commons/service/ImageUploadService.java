package com.ducks.goodsduck.commons.service;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.ducks.goodsduck.commons.model.dto.ImageDto;
import com.ducks.goodsduck.commons.model.entity.Image;
import com.ducks.goodsduck.commons.model.enums.ImageType;
import com.ducks.goodsduck.commons.util.AwsSecretsManagerUtil;
import com.ducks.goodsduck.commons.util.PropertyUtil;
import com.mortennobel.imagescaling.AdvancedResizeOp;
import com.mortennobel.imagescaling.MultiStepRescaleOp;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import javax.management.RuntimeErrorException;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@NoArgsConstructor
@Slf4j
public class ImageUploadService {

    private final JSONObject jsonOfAwsSecrets = AwsSecretsManagerUtil.getSecret();
    
    private final String localFilePath = jsonOfAwsSecrets.optString("spring.file.path.local", PropertyUtil.getProperty("spring.file.path.local"));
    private final String itemS3Bucket = jsonOfAwsSecrets.optString("cloud.aws.s3.itemBucket", PropertyUtil.getProperty("cloud.aws.s3.itemBucket"));
    private final String profileS3Bucket = jsonOfAwsSecrets.optString("cloud.aws.s3.profileBucket", PropertyUtil.getProperty("cloud.aws.s3.profileBucket"));
    private final String chatS3Bucket = jsonOfAwsSecrets.optString("cloud.aws.s3.chatBucket", PropertyUtil.getProperty("cloud.aws.s3.chatBucket"));
    private final String accessKey = jsonOfAwsSecrets.optString("cloud.aws.credentials.accessKey", PropertyUtil.getProperty("cloud.aws.credentials.accessKey"));
    private final String secretKey = jsonOfAwsSecrets.optString("cloud.aws.credentials.secretKey", PropertyUtil.getProperty("cloud.aws.credentials.secretKey"));
    private final String region = jsonOfAwsSecrets.optString("cloud.aws.region.static", PropertyUtil.getProperty("cloud.aws.region.static"));

    public String getFilePath(String fileName) {
        return localFilePath + fileName;
    }

    public List<Image> uploadImages(List<MultipartFile> multipartFiles, ImageType imageType) throws IOException {

        List<Image> images = new ArrayList<>();

        for (MultipartFile multipartFile : multipartFiles) {
            if(!multipartFile.isEmpty()) {
                Image image = uploadImage(multipartFile, imageType);
                images.add(image);
            }
        }

        return images;
    }

    /** S3에 이미지 업로드 + 리사이징 **/
    public Image uploadImage(MultipartFile multipartFile, ImageType imageType) throws IOException {

        // S3 셋팅
        AWSCredentials awsCredentials = new BasicAWSCredentials(accessKey, secretKey);
        AmazonS3 s3Client = AmazonS3ClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(awsCredentials))
                .withRegion(region)
                .build();

        if(multipartFile.isEmpty()) {
            return null;
        }

        String originName = multipartFile.getOriginalFilename();
        String uploadName = createUploadName(originName);
        String ext = extractExt(originName);
        Long bytes = multipartFile.getSize();

        BufferedImage image = ImageIO.read(multipartFile.getInputStream());

        // 1MB 이상에서만 리사이징
        if(bytes >= 1048576) {

            int width = image.getWidth();
            int height = image.getHeight();
            int newWidth = width;
            int newHeight = height;

            if(width > height) {
                newHeight = 500;
                newWidth = getNewWidth(newHeight, width, height);
            } else {
                newWidth = 500;
                newHeight = getNewHeight(newWidth, width, height);
            }

            MultiStepRescaleOp rescale = new MultiStepRescaleOp(newWidth, newHeight);
            rescale.setUnsharpenMask(AdvancedResizeOp.UnsharpenMask.Soft);

            BufferedImage resizedImage = rescale.filter(image, null);

            uploadImageToS3(s3Client, uploadName, ext, resizedImage, imageType);
        } else {
            uploadImageToS3(s3Client, uploadName, ext, image, imageType);
        }

        if(imageType.equals(ImageType.ITEM)) {
            return new Image(originName, uploadName, s3Client.getUrl(itemS3Bucket, uploadName).toString());
        } else if(imageType.equals(ImageType.PROFILE)) {
            return new Image(originName, uploadName, s3Client.getUrl(profileS3Bucket, uploadName).toString());
        } else if(imageType.equals(ImageType.CHAT)) {
            return new Image(originName, uploadName, s3Client.getUrl(chatS3Bucket, uploadName).toString());
        } else {
            return null;
        }
    }

    // TODO : 추후 확인 후 삭제 예정
//    public List<ImageDto> uploadImages(List<MultipartFile> multipartFiles, ImageType imageType) throws IOException {
//
//        List<ImageDto> imageDtos = new ArrayList<>();
//
//        for (MultipartFile multipartFile : multipartFiles) {
//            if(!multipartFile.isEmpty()) {
//                ImageDto imageDto = uploadImage(multipartFile, imageType);
//                imageDtos.add(imageDto);
//            }
//        }
//
//        return imageDtos;
//    }

//    public ImageDto uploadImage(MultipartFile multipartFile, ImageType imageType) throws IOException {
//
//        // S3 셋팅
//        AWSCredentials awsCredentials = new BasicAWSCredentials(accessKey, secretKey);
//        AmazonS3 s3Client = AmazonS3ClientBuilder.standard()
//                .withCredentials(new AWSStaticCredentialsProvider(awsCredentials))
//                .withRegion(region)
//                .build();
//
//        if(multipartFile.isEmpty()) {
//            return null;
//        }
//
//        String originName = multipartFile.getOriginalFilename();
//        String uploadName = createUploadName(originName);
//        String ext = extractExt(originName);
//        Long bytes = multipartFile.getSize();
//
//        BufferedImage image = ImageIO.read(multipartFile.getInputStream());
//
//        // 1MB 이상에서만 리사이징
//        if(bytes >= 1048576) {
//
//            int width = image.getWidth();
//            int height = image.getHeight();
//            int newWidth = width;
//            int newHeight = height;
//
//            if(width > height) {
//                newHeight = 500;
//                newWidth = getNewWidth(newHeight, width, height);
//            } else {
//                newWidth = 500;
//                newHeight = getNewHeight(newWidth, width, height);
//            }
//
//            MultiStepRescaleOp rescale = new MultiStepRescaleOp(newWidth, newHeight);
//            rescale.setUnsharpenMask(AdvancedResizeOp.UnsharpenMask.Soft);
//
//            BufferedImage resizedImage = rescale.filter(image, null);
//
//            uploadImageToS3(s3Client, uploadName, ext, resizedImage, imageType);
//        } else {
//            uploadImageToS3(s3Client, uploadName, ext, image, imageType);
//        }
//
//        if(imageType.equals(ImageType.ITEM)) {
//            return new ImageDto(originName, uploadName, s3Client.getUrl(itemS3Bucket, uploadName).toString());
//        } else if(imageType.equals(ImageType.PROFILE)) {
//            return new ImageDto(originName, uploadName, s3Client.getUrl(profileS3Bucket, uploadName).toString());
//        } else if(imageType.equals(ImageType.CHAT)) {
//            return new ImageDto(originName, uploadName, s3Client.getUrl(chatS3Bucket, uploadName).toString());
//        } else {
//            return null;
//        }
//    }

    private void uploadImageToS3(AmazonS3 s3Client, String uploadName, String ext, BufferedImage image, ImageType imageType) throws IOException {

        ByteArrayOutputStream imageOS = new ByteArrayOutputStream();
        ImageIO.write(image, ext, imageOS);

        InputStream imageIS = new ByteArrayInputStream(imageOS.toByteArray());

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(imageOS.size());

        if (ext.equals("jpg")) {
            metadata.setContentType("jpg");
        } else if (ext.equals("jpeg")) {
            metadata.setContentType("jpeg");
        } else if (ext.equals("png")) {
            metadata.setContentType("png");
        } else if (ext.equals("gif")) {
            metadata.setContentType("gif");
        }

        if(imageType.equals(ImageType.ITEM)) {
            s3Client.putObject(new PutObjectRequest(itemS3Bucket, uploadName, imageIS, metadata));
        } else if(imageType.equals(ImageType.PROFILE)) {
            s3Client.putObject(new PutObjectRequest(profileS3Bucket, uploadName, imageIS, metadata));
        } else if(imageType.equals(ImageType.CHAT)) {
            s3Client.putObject(new PutObjectRequest(chatS3Bucket, uploadName, imageIS, metadata));
        } else {
            throw new RuntimeException("fail to upload image");
        }
    }

    private Integer getNewWidth(int newHeight, int width, int height) {
        return newHeight * width / height;
    }

    private Integer getNewHeight(int newWidth, int width, int height) {
        return newWidth* height / width;
    }

    private String createUploadName(String orginName) {

        // 업로드 파일명 (중복때문에 클라이언트에서 보낸 파일 이름이랑 다르게)
        String uuid = UUID.randomUUID().toString();

        // .png .img .jpeg
        String ext = extractExt(orginName);

        return uuid + "." + ext;
    }

    private String extractExt(String orginName) {

        int idx = orginName.lastIndexOf(".");
        String ext = orginName.substring(idx + 1);

        return ext;
    }

    /** local에 이미지 업로드 **/
//    public ImageDto uploadImage(MultipartFile multipartFile) throws IOException {
//
//        if(multipartFile.isEmpty()) {
//            return null;
//        }
//
//        String orginName = multipartFile.getOriginalFilename();
//        String uploadName = createUploadName(orginName);
//
//        // path에 이미지 저장
//        multipartFile.transferTo((new File(getFilePath(uploadName))));
//
//        return new ImageDto(orginName, uploadName);
//    }

    /** local에 이미지 업로드 + 리사이징 **/
//    public ImageDto uploadImage(MultipartFile multipartFile) throws IOException {
//
//        if(multipartFile.isEmpty()) {
//            return null;
//        }
//
//        String orginName = multipartFile.getOriginalFilename();
//        String uploadName = createUploadName(orginName);
//        String ext = extractExt(orginName);
//        Long bytes = multipartFile.getSize();
//
//        // 1MB 이상에서만 리사이징
//        if(bytes >= 1048576) {
//            BufferedImage image = ImageIO.read(multipartFile.getInputStream());
//
//            int width = image.getWidth();
//            int height = image.getHeight();
//            int newWidth = width;
//            int newHeight = height;
//
//            if(width > height) {
//                newHeight = 500;
//                newWidth = getNewWidth(newHeight, width, height);
//            } else {
//                newWidth = 500;
//                newHeight = getNewHeight(newWidth, width, height);
//            }
//
//            MultiStepRescaleOp rescale = new MultiStepRescaleOp(newWidth, newHeight);
//            rescale.setUnsharpenMask(AdvancedResizeOp.UnsharpenMask.Soft);
//
//            BufferedImage resizedImage = rescale.filter(image, null);
//
//            ImageIO.write(resizedImage, ext, new File(getFilePath(uploadName)));
//        } else {
//            multipartFile.transferTo((new File(getFilePath(uploadName))));
//        }
//
//        return new ImageDto(orginName, uploadName);
//    }
}
