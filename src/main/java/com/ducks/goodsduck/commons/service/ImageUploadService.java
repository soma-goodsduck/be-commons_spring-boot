package com.ducks.goodsduck.commons.service;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Metadata;
import com.drew.metadata.MetadataException;
import com.drew.metadata.exif.ExifIFD0Directory;
import com.ducks.goodsduck.commons.model.dto.ImageDto;
import com.ducks.goodsduck.commons.model.entity.Image.Image;
import com.ducks.goodsduck.commons.model.enums.ImageType;
import com.ducks.goodsduck.commons.util.AwsSecretsManagerUtil;
import com.ducks.goodsduck.commons.util.GifSequenceWriter;
import com.ducks.goodsduck.commons.util.PropertyUtil;
import com.madgag.gif.fmsware.GifDecoder;
import com.mortennobel.imagescaling.AdvancedResizeOp;
import com.mortennobel.imagescaling.MultiStepRescaleOp;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.imgscalr.Scalr;
import org.json.JSONObject;
import org.springframework.boot.context.event.ApplicationPreparedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import javax.imageio.stream.FileImageOutputStream;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@NoArgsConstructor
@Slf4j
public class ImageUploadService {

    private static final JSONObject jsonOfAwsSecrets = AwsSecretsManagerUtil.getSecret();

    private static String localFilePath = jsonOfAwsSecrets.optString("spring.file.path.local", PropertyUtil.getProperty("spring.file.path.local"));
    private static String itemS3Bucket = jsonOfAwsSecrets.optString("cloud.aws.s3.itemBucket", PropertyUtil.getProperty("cloud.aws.s3.itemBucket"));
    private static String profileS3Bucket = jsonOfAwsSecrets.optString("cloud.aws.s3.profileBucket", PropertyUtil.getProperty("cloud.aws.s3.profileBucket"));
    private static String chatS3Bucket = jsonOfAwsSecrets.optString("cloud.aws.s3.chatBucket", PropertyUtil.getProperty("cloud.aws.s3.chatBucket"));
    private static String postS3Bucket = jsonOfAwsSecrets.optString("cloud.aws.s3.postBucket", PropertyUtil.getProperty("cloud.aws.s3.postBucket"));
    private static String accessKey = jsonOfAwsSecrets.optString("cloud.aws.credentials.accessKey", PropertyUtil.getProperty("cloud.aws.credentials.accessKey"));
    private static String secretKey = jsonOfAwsSecrets.optString("cloud.aws.credentials.secretKey", PropertyUtil.getProperty("cloud.aws.credentials.secretKey"));
    private static String region = jsonOfAwsSecrets.optString("cloud.aws.region.static", PropertyUtil.getProperty("cloud.aws.region.static"));

    public List<Image> uploadImages(List<MultipartFile> multipartFiles, ImageType imageType, String nickname) throws IOException, ImageProcessingException, MetadataException {

        List<Image> images = new ArrayList<>();

        for (MultipartFile multipartFile : multipartFiles) {
            if(!multipartFile.isEmpty()) {
                Image image = uploadImage(multipartFile, imageType, nickname);
                if(image != null) {
                    images.add(image);
                }
            }
        }

        return images;
    }

    /** S3에 이미지 업로드 + 리사이징 + 워터마크 **/
    public Image uploadImage(MultipartFile multipartFile, ImageType imageType, String nickname) throws IOException, ImageProcessingException, MetadataException {

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
        String EXT = extractExt(originName);
        String ext = EXT.toLowerCase();
        Long bytes = multipartFile.getSize();


        GifDecoder gifDecoder = new GifDecoder();
        gifDecoder.read(multipartFile.getInputStream());

//        gifDecoder.getDelay()

        if(!ext.equals("gif")) {

            BufferedImage image = ImageIO.read(multipartFile.getInputStream());

            // FEAT : 파일 회전 체크
            int orientation = 1;
            Metadata metadata = ImageMetadataReader.readMetadata(multipartFile.getInputStream());
            ExifIFD0Directory directory = metadata.getFirstDirectoryOfType(ExifIFD0Directory.class);
            if(directory != null) {
                orientation = directory.getInt(ExifIFD0Directory.TAG_ORIENTATION);
            }

            switch (orientation) {
                case 6:
                    image = Scalr.rotate(image, Scalr.Rotation.CW_90, null);
                    break;
                case 3:
                    image = Scalr.rotate(image, Scalr.Rotation.CW_180, null);
                    break;
                case 8:
                    image = Scalr.rotate(image, Scalr.Rotation.CW_270, null);
                    break;
                default:
                    break;
            }

            // FEAT : 파일이 1MB 이상일 경우 리사이징
            if(bytes >= 1048576) {

                BufferedImage resizedImage = getResizedImage(image);

                if (imageType.equals(ImageType.CHAT)) {
                    BufferedImage watermarkedImage = getWatermarkedImage(resizedImage, nickname);
                    uploadImageToS3(s3Client, uploadName, ext, watermarkedImage, imageType);
                } else {
                    uploadImageToS3(s3Client, uploadName, ext, resizedImage, imageType);
                }

            } else {
                if(imageType.equals(ImageType.CHAT)) {
                    BufferedImage watermarkedImage = getWatermarkedImage(image, nickname);
                    uploadImageToS3(s3Client, uploadName, ext, watermarkedImage, imageType);
                } else {
                    uploadImageToS3(s3Client, uploadName, ext, image, imageType);
                }
            }
        } else {
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType("gif");
            s3Client.putObject(new PutObjectRequest(itemS3Bucket, uploadName, multipartFile.getInputStream(), metadata));
        }

        if(imageType.equals(ImageType.ITEM)) {
            return new Image(originName, uploadName, s3Client.getUrl(itemS3Bucket, uploadName).toString());
        } else if(imageType.equals(ImageType.PROFILE)) {
            return new Image(originName, uploadName, s3Client.getUrl(profileS3Bucket, uploadName).toString());
        } else if(imageType.equals(ImageType.CHAT)) {
            return new Image(originName, uploadName, s3Client.getUrl(chatS3Bucket, uploadName).toString());
        }else if(imageType.equals(ImageType.POST)) {
            return new Image(originName, uploadName, s3Client.getUrl(postS3Bucket, uploadName).toString());
        } else {
            return null;
        }
    }

    private BufferedImage getResizedImage(BufferedImage image) {

        int width = image.getWidth();
        int height = image.getHeight();
        int newWidth = width;
        int newHeight = height;

        if (width > height) {
            newHeight = 500;
            newWidth = getNewWidth(newHeight, width, height);
        } else {
            newWidth = 500;
            newHeight = getNewHeight(newWidth, width, height);
        }

        MultiStepRescaleOp rescale = new MultiStepRescaleOp(newWidth, newHeight);
        rescale.setUnsharpenMask(AdvancedResizeOp.UnsharpenMask.Soft);

        return rescale.filter(image, null);
    }

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
        } else if(imageType.equals(ImageType.POST)) {
            s3Client.putObject(new PutObjectRequest(postS3Bucket, uploadName, imageIS, metadata));
        } else {
            throw new RuntimeException("Fail to upload image in ImageUploadService.uploadImageToS3");
        }
    }

    private BufferedImage getWatermarkedImage(BufferedImage image, String nickname) {

        BufferedImage watermarkedImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);

        Graphics graphics = watermarkedImage.getGraphics();
        graphics.drawImage(image, 0, 0, null);

        graphics.setFont(new Font("dejavu", Font.BOLD, 15));
        graphics.setColor(new Color(255, 255, 255, 40));

        StringBuilder sb = new StringBuilder();
        sb.appendCodePoint(169) ;
        sb.append(" GOODSDUCK");
        String watermark = sb.toString();

        // 워터마크 1Line Center
//        graphics.drawString(watermark, image.getWidth()/2 - 80, image.getHeight()/2);

        // 워터마크 2Line Center
        graphics.drawString(watermark, image.getWidth()/2 - 60, image.getHeight()/2 - 10);
        graphics.drawString(nickname, image.getWidth()/2 - 45, image.getHeight()/2 + 10);

        graphics.dispose();

        return watermarkedImage;
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

    public String getFilePath(String fileName) {
        return localFilePath + fileName;
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

    // TODO : 주석예정 (테스트용)
    /** local에 이미지 업로드 + 리사이징 + 워터마크 **/
    public ImageDto uploadImageWithWatermark(MultipartFile multipartFile) throws IOException {

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

            BufferedImage resizedImage = getResizedImage(image);
            BufferedImage watermarkedImage = getWatermarkedImage(resizedImage, "makkk");

            ImageIO.write(watermarkedImage, ext, new File(getFilePath(uploadName)));
        } else {
            BufferedImage watermarkedImage = getWatermarkedImage(image, "makkk");

            ImageIO.write(watermarkedImage, ext, new File(getFilePath(uploadName)));
        }

        return new ImageDto(orginName, uploadName);
    }

    public void resizeGIF(MultipartFile multipartFile) throws IOException {

        // TODO: 임시로 항상 리사이징 하게끔 설정해놓음.
        final int GIF_SIZE_LIMIT =  10;
        String orginName = multipartFile.getOriginalFilename();
        String uploadName = createUploadName(orginName);

        BufferedImage imageOfFrame;
        BufferedImage resizedImageOfFrame;

        GifDecoder gifDecoder = new GifDecoder();
        gifDecoder.read(multipartFile.getInputStream());

        int frameCount = gifDecoder.getFrameCount();
        Long bytes =  multipartFile.getSize();

        if (bytes > GIF_SIZE_LIMIT) {
            ImageOutputStream outputStream = new FileImageOutputStream(new File(getFilePath(uploadName)));
            GifSequenceWriter writer = new GifSequenceWriter(outputStream, gifDecoder.getFrame(0).getType(), true);

            for (int i = 0; i < frameCount; i++) {
                writer.configureRootMetadata(gifDecoder.getDelay(i), true);
                imageOfFrame = gifDecoder.getFrame(i);
                resizedImageOfFrame = getResizedBufferedImageOfFrame(imageOfFrame);
                writer.writeToSequence(resizedImageOfFrame);
            }

            writer.close();
            outputStream.close();
        }

        AWSCredentials awsCredentials = new BasicAWSCredentials(accessKey, secretKey);
        AmazonS3 s3Client = AmazonS3ClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(awsCredentials))
                .withRegion(region)
                .build();

        s3Client.putObject(new PutObjectRequest(itemS3Bucket, uploadName, new File(getFilePath(uploadName))));

        File deleteFile = new File(getFilePath(uploadName));
        deleteFile.delete();
    }

    private BufferedImage getResizedBufferedImageOfFrame(BufferedImage image) {

        final int STANDARD_LENGTH = 500;
        int width = image.getWidth();
        int height = image.getHeight();
        int newWidth = width;
        int newHeight = height;

        if (width > height) {
            newHeight = STANDARD_LENGTH;
            newWidth = getNewWidth(newHeight, width, height);
        } else if (height > width) {
            newWidth = STANDARD_LENGTH;
            newHeight = getNewHeight(newWidth, width, height);
        }

        MultiStepRescaleOp rescale = new MultiStepRescaleOp(newWidth, newHeight);
        rescale.setUnsharpenMask(AdvancedResizeOp.UnsharpenMask.Soft);

        return rescale.filter(image, null);
    }

    @EventListener
    public void setIfLocal(ApplicationPreparedEvent event) {
        if (jsonOfAwsSecrets.isEmpty()) {
            localFilePath = PropertyUtil.getProperty("spring.file.path.local");
            itemS3Bucket = PropertyUtil.getProperty("cloud.aws.s3.itemBucket");
            profileS3Bucket = PropertyUtil.getProperty("cloud.aws.s3.profileBucket");
            chatS3Bucket = PropertyUtil.getProperty("cloud.aws.s3.chatBucket");
            postS3Bucket = PropertyUtil.getProperty("cloud.aws.s3.postBucket");
            accessKey = PropertyUtil.getProperty("cloud.aws.credentials.accessKey");
            secretKey = PropertyUtil.getProperty("cloud.aws.credentials.secretKey");
            region = PropertyUtil.getProperty("cloud.aws.region.static");
        }
    }
}
