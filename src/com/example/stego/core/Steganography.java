package com.example.stego.core;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

// Основная логика LSB-стеганографии. Поддерживает BMP и PNG (работаем с BufferedImage).
// Формат:
// первые 32 бита: длина сообщения в байтах
// далее messageBytes * 8 бит — само сообщение в UTF-8
// Записываем в младшие биты каналов RGB каждого пикселя (по одному биту в каждый канал).

public class Steganography {

    // Во т этот класс !!!!! Кодирует текст в копию изображения и возвращает новую BufferedImage.

    public static BufferedImage encode(BufferedImage src, String text) throws IllegalArgumentException {
        byte[] messageBytes = text.getBytes(StandardCharsets.UTF_8);
        int messageBits = messageBytes.length * 8;
        int requiredBits = 32 + messageBits; // 32 бита длины + данные

        int width = src.getWidth();
        int height = src.getHeight();
        int availableBits = width * height * 3; // R,G,B per pixel

        if (requiredBits > availableBits) {
            throw new IllegalArgumentException(String.format("Сообщение слишком длинное. Доступно бит: %d, требуется: %d", availableBits, requiredBits));
        }

        BufferedImage dest = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        dest.getGraphics().drawImage(src, 0, 0, null);

        // Сначала запишем длину (int, 32 бита, big-endian)
        int length = messageBytes.length;
        int bitIndex = 0; // индекс бита от 0..requiredBits-1

        for (int i = 31; i >= 0; i--) {
            int bit = BitUtils.getIntBit(length, i);
            setNextLSB(dest, bitIndex++, bit);
        }

        // Теперь данные: по байту, от старшего бита к младшему (7..0)
        for (byte b : messageBytes) {
            for (int i = 7; i >= 0; i--) {
                int bit = BitUtils.getBit(b, i);
                setNextLSB(dest, bitIndex++, bit);
            }
        }

        return dest;
    }

    // Вот этот класс извлекает сообщение из изображения и возвращаетт извлечённый текст в UTF-8.

    public static String decode(BufferedImage src) throws IllegalArgumentException {
        int width = src.getWidth();
        int height = src.getHeight();

        int totalBits = width * height * 3;
        // Сначала читаем 32 бита длины
        int bitIndex = 0;
        int length = 0;
        for (int i = 0; i < 32; i++) {
            int bit = getNextLSB(src, bitIndex++);
            length = (length << 1) | bit;
        }
        if (length < 0) throw new IllegalArgumentException("Некорректная длина сообщения: " + length);

        int messageBits = length * 8;
        if (32 + messageBits > totalBits) {
            throw new IllegalArgumentException("Изображение не содержит полного сообщения (по длине).");
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream(length);
        byte currentByte = 0;
        int bitsCollected = 0;

        for (int i = 0; i < messageBits; i++) {
            int bit = getNextLSB(src, bitIndex++);
            currentByte = (byte) ((currentByte << 1) | bit);
            bitsCollected++;
            if (bitsCollected == 8) {
                baos.write(currentByte);
                currentByte = 0;
                bitsCollected = 0;
            }
        }

        byte[] messageBytes = baos.toByteArray();
        return new String(messageBytes, StandardCharsets.UTF_8);
    }


    private static void setNextLSB(BufferedImage img, int bitIndex, int bitValue) {
        int width = img.getWidth();
        int pixelIndex = bitIndex / 3; // какой пиксель
        int channel = bitIndex % 3;    // 0->R,1->G,2->B
        int x = pixelIndex % width;
        int y = pixelIndex / width;
        int rgb = img.getRGB(x, y); // ARGB в int

        int alpha = (rgb >> 24) & 0xFF;
        int red   = (rgb >> 16) & 0xFF;
        int green = (rgb >> 8)  & 0xFF;
        int blue  = rgb & 0xFF;

        if (channel == 0) {
            red = (red & 0xFE) | (bitValue & 1);
        } else if (channel == 1) {
            green = (green & 0xFE) | (bitValue & 1);
        } else {
            blue = (blue & 0xFE) | (bitValue & 1);
        }

        int newRgb = (alpha << 24) | (red << 16) | (green << 8) | blue;
        img.setRGB(x, y, newRgb);
    }

    private static int getNextLSB(BufferedImage img, int bitIndex) {
        int width = img.getWidth();
        int pixelIndex = bitIndex / 3;
        int channel = bitIndex % 3;
        int x = pixelIndex % width;
        int y = pixelIndex / width;
        int rgb = img.getRGB(x, y);

        int value;
        if (channel == 0) {
            value = (rgb >> 16) & 0xFF; // red
        } else if (channel == 1) {
            value = (rgb >> 8) & 0xFF; // green
        } else {
            value = rgb & 0xFF; // blue
        }
        return value & 1;
    }
}
