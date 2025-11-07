package com.example.stego.core;

//Небольшие утилиты для работы с битами. Егор разберись пожалуйста!!!!!!!
public class BitUtils {
    //Возвращает i-тый бит в байте (0 — младший)
    public static int getBit(byte b, int i) {
        return (b >> i) & 1;
    }

    //Возвращает i-тый бит в int (0 — младший)
    public static int getIntBit(int value, int i) {
        return (value >> i) & 1;
    }

    //Устанавливает i-тый бит в целевом байте (0 — младший) в value (0/1) и возвращает новый байт
    public static byte setBit(byte target, int i, int value) {
        if (value == 0) {
            return (byte) (target & ~(1 << i));
        } else {
            return (byte) (target | (1 << i));
        }
    }
}
