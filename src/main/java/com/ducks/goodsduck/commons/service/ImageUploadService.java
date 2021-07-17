package com.ducks.goodsduck.commons.service;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.ducks.goodsduck.commons.model.dto.ImageDto;
import com.ducks.goodsduck.commons.util.PropertyUtil;
import com.mortennobel.imagescaling.AdvancedResizeOp;
import com.mortennobel.imagescaling.MultiStepRescaleOp;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@NoArgsConstructor
@Slf4j
public class ImageUploadService {

    private final String localFilePath = PropertyUtil.getProperty("spring.file.path.local");
    private final String s3Bucket = PropertyUtil.getProperty("cloud.aws.s3.bucket");
    private final String accessKey = PropertyUtil.getProperty("cloud.aws.credentials.accessKey");
    private final String secretKey = PropertyUtil.getProperty("cloud.aws.credentials.secretKey");
    private final String region = PropertyUtil.getProperty("cloud.aws.region.static");

    public String getFilePath(String fileName) {
        return localFilePath + fileName;
    }

    public List<ImageDto> uploadImages(List<MultipartFile> multipartFiles) throws IOException {

        List<ImageDto> imageDtos = new ArrayList<>();

        for (MultipartFile multipartFile : multipartFiles) {
            if(!multipartFile.isEmpty()) {
                ImageDto imageDto = uploadImage(multipartFile);
                imageDtos.add(imageDto);
            }
        }

        return imageDtos;
    }

    /** S3에 이미지 업로드 + 리사이징 **/
    public ImageDto uploadImage(MultipartFile multipartFile) throws IOException {

        // S3 셋팅
        AWSCredentials awsCredentials = new BasicAWSCredentials(accessKey, secretKey);
        AmazonS3 s3Client = AmazonS3ClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(awsCredentials))
                .withRegion(region)
                .build();

        if(multipartFile.isEmpty()) {
            return null;
        }

        String orginName = multipartFile.getOriginalFilename();
        String uploadName = createUploadName(orginName);
        String ext = extractExt(orginName);
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

            // S3 셋팅
            ByteArrayOutputStream imageOS = new ByteArrayOutputStream();
            ImageIO.write(resizedImage, ext, imageOS);

            InputStream imageIS = new ByteArrayInputStream(imageOS.toByteArray());

            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(imageOS.size());

            uploadImageToS3(s3Client, uploadName, ext, resizedImage);
        } else {
            uploadImageToS3(s3Client, uploadName, ext, image);
        }

        return new ImageDto(orginName, uploadName, s3Client.getUrl(s3Bucket, uploadName).toString());
    }

    private void uploadImageToS3(AmazonS3 s3Client, String uploadName, String ext, BufferedImage image) throws IOException {

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

        s3Client.putObject(new PutObjectRequest(s3Bucket, uploadName, imageIS, metadata));
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
