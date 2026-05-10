package com.shipin.utils;


import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.Random;

/**
 * 验证码生成工具类
 */
@Slf4j
@Component
public class CaptchaUtil {

    // 验证码字符集
    private static final String CODE_CHARSET = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    // 验证码长度
    private static final int CODE_LENGTH = 4;
    // 图片宽度
    private static final int IMAGE_WIDTH = 120;
    // 图片高度
    private static final int IMAGE_HEIGHT = 40;
    // 干扰线数量
    private static final int LINE_COUNT = 5;
    // 字体大小
    private static final int FONT_SIZE = 20;

    /**
     * 生成随机验证码
     * @return 随机验证码字符串
     */
    public String generateCode() {
        return RandomStringUtils.random(CODE_LENGTH, CODE_CHARSET);
    }

    /**
     * 生成验证码图片
     * @param code 验证码字符串
     * @return base64编码的图片
     */
    public String generateImage(String code) {
        // 创建图片对象
        BufferedImage image = new BufferedImage(IMAGE_WIDTH, IMAGE_HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics g = image.getGraphics();

        try {
            // 设置背景色
            g.setColor(getRandomColor(200, 250));
            g.fillRect(0, 0, IMAGE_WIDTH, IMAGE_HEIGHT);

            // 设置字体
            Font font = new Font("Arial", Font.BOLD, FONT_SIZE);
            g.setFont(font);

            // 绘制干扰线
            Random random = new Random();
            for (int i = 0; i < LINE_COUNT; i++) {
                int x1 = random.nextInt(IMAGE_WIDTH);
                int y1 = random.nextInt(IMAGE_HEIGHT);
                int x2 = random.nextInt(IMAGE_WIDTH);
                int y2 = random.nextInt(IMAGE_HEIGHT);
                g.setColor(getRandomColor(100, 200));
                g.drawLine(x1, y1, x2, y2);
            }

            // 绘制验证码
            for (int i = 0; i < code.length(); i++) {
                String str = code.substring(i, i + 1);
                g.setColor(getRandomColor(50, 150));
                g.drawString(str, 25 * i + 15, 25);
            }

            // 绘制干扰点
            for (int i = 0; i < 100; i++) {
                int x = random.nextInt(IMAGE_WIDTH);
                int y = random.nextInt(IMAGE_HEIGHT);
                g.setColor(getRandomColor(50, 150));
                g.fillOval(x, y, 1, 1);
            }

            // 将图片转换为base64编码
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "png", baos);
            byte[] imageBytes = baos.toByteArray();
            return "data:image/png;base64," + Base64.getEncoder().encodeToString(imageBytes);
        } catch (IOException e) {
            log.error("生成验证码图片失败", e);
            return null;
        } finally {
            g.dispose();
        }
    }

    /**
     * 生成随机颜色
     * @param min 最小色值
     * @param max 最大色值
     * @return 随机颜色
     */
    private Color getRandomColor(int min, int max) {
        Random random = new Random();
        int r = min + random.nextInt(max - min);
        int g = min + random.nextInt(max - min);
        int b = min + random.nextInt(max - min);
        return new Color(r, g, b);
    }
}