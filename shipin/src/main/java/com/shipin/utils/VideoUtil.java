package com.shipin.utils;

import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * 视频处理工具类
 */
public class VideoUtil {

    /**
     * 从视频中提取第一帧作为封面
     * @param videoPath 视频文件路径
     * @param outputPath 输出图片路径
     * @return 是否提取成功
     */
    public static boolean extractFirstFrame(String videoPath, String outputPath) {
        FFmpegFrameGrabber grabber = null;
        Java2DFrameConverter converter = null;
        try {
            // 创建FFmpegFrameGrabber对象
            grabber = new FFmpegFrameGrabber(videoPath);
            // 启动抓取器
            grabber.start();
            // 获取第一帧
            Frame frame = grabber.grabImage();
            if (frame == null) {
                return false;
            }
            // 创建Java2DFrameConverter对象
            converter = new Java2DFrameConverter();
            // 将Frame转换为BufferedImage
            BufferedImage bufferedImage = converter.convert(frame);
            if (bufferedImage == null) {
                return false;
            }
            // 确保输出目录存在
            File outputFile = new File(outputPath);
            File outputDir = outputFile.getParentFile();
            if (!outputDir.exists()) {
                outputDir.mkdirs();
            }
            // 保存图片
            ImageIO.write(bufferedImage, "jpg", outputFile);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } finally {
            // 释放资源
            if (grabber != null) {
                try {
                    grabber.stop();
                    grabber.release();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (converter != null) {
                converter.close();
            }
        }
    }
}
