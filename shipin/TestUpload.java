import org.springframework.web.multipart.MultipartFile;
import org.springframework.mock.web.MockMultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.UUID;

public class TestUpload {
    public static void main(String[] args) {
        try {
            // 测试文件路径
            String testFilePath = "E:\\shipin\\day07\\shipinbofang - 副本\\test.txt";
            File testFile = new File(testFilePath);
            
            // 创建MockMultipartFile
            FileInputStream fileInputStream = new FileInputStream(testFile);
            MultipartFile multipartFile = new MockMultipartFile(
                    "file",
                    testFile.getName(),
                    "text/plain",
                    fileInputStream
            );
            
            // 视频存储路径
            String videoPath = "D:/shipin/video/";
            File dir = new File(videoPath);
            if (!dir.exists()) {
                dir.mkdirs();
                System.out.println("创建目录成功: " + videoPath);
            } else {
                System.out.println("目录已存在: " + videoPath);
            }

            // 生成唯一文件名
            String originalFilename = multipartFile.getOriginalFilename();
            String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
            String fileName = UUID.randomUUID().toString() + suffix;
            String filePath = videoPath + fileName;

            // 保存文件
            File dest = new File(filePath);
            multipartFile.transferTo(dest);
            System.out.println("文件保存成功: " + filePath);

            // 返回视频路径（相对路径）
            String relativePath = "/video/" + fileName;
            System.out.println("相对路径: " + relativePath);
            
            // 关闭流
            fileInputStream.close();
            
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("文件上传失败: " + e.getMessage());
        }
    }
}