package com.example.stego;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

//Загрузка и сохранние изображения

public class ImageUtils {

    public static BufferedImage loadImage(File file) throws IOException {
        BufferedImage img = ImageIO.read(file);
        if (img == null) throw new IOException("Не удалось прочитать изображение: " + file.getAbsolutePath());
        // Приводим к TYPE_3BYTE_BGR или TYPE_INT_ARGB, чтобы комфртно работать
        if (img.getType() == BufferedImage.TYPE_INT_ARGB || img.getType() == BufferedImage.TYPE_3BYTE_BGR) {
            return img;
        } else {
            BufferedImage converted = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_ARGB);
            converted.getGraphics().drawImage(img, 0, 0, null);
            return converted;
        }
    }

    public static void saveImage(BufferedImage image, File outFile, String formatName) throws IOException {
        // formatName например "png" или "bmp"
        if (!ImageIO.write(image, formatName, outFile)) {
            throw new IOException("Не удалось сохранить изображение в формате " + formatName);
        }
    }
}
