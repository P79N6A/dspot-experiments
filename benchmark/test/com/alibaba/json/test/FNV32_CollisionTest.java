package com.alibaba.json.test;


import java.text.NumberFormat;
import java.util.Random;
import junit.framework.TestCase;


/**
 * Created by wenshao on 08/01/2017.
 */
public class FNV32_CollisionTest extends TestCase {
    char[] digLetters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789_".toCharArray();

    // char[] digLetters = "0123456789".toCharArray();
    Random r = new Random();

    int[] powers = new int[10];

    {
        for (int i = 0; i < (powers.length); ++i) {
            powers[i] = ((int) (Math.pow(digLetters.length, i)));
        }
    }

    public void test_fnv_hash() throws Exception {
        int COUNT = (1000 * 1000) * 1000;
        long id_hash_64 = FNV32_CollisionTest.fnv_hash("name".toCharArray());
        int id_hash_32 = ((int) (id_hash_64));
        System.out.println(((("name : " + id_hash_32) + ", ") + id_hash_64));
        long v = 0;
        long time = System.currentTimeMillis();
        NumberFormat format = NumberFormat.getInstance();
        for (int len = 1; len <= 7; ++len) {
            char[] chars = new char[len];
            long n = ((long) (Math.pow(digLetters.length, chars.length)));
            for (; v < n; ++v) {
                long hash = -2128831035;
                for (int i = 0; i < (chars.length); ++i) {
                    int power = powers[(((chars.length) - i) - 1)];
                    int d = ((int) ((v / power) % (digLetters.length)));
                    char c = digLetters[d];
                    hash ^= c;
                    hash *= 16777619;
                }
                if (hash == id_hash_64) {
                    int hash_32 = ((int) (hash));
                    System.out.println(((((("collision : " + (build(v, len))) + "? hash64 : ") + hash) + ", hash 32 ") + hash_32));
                    break;
                }
                if ((v != 0) && ((v % ((1000 * 1000) * 100)) == 0)) {
                    long now = System.currentTimeMillis();
                    long millis = now - time;
                    time = now;
                    System.out.println(((("millis : " + millis) + ", ") + (format.format(v))));
                }
            }
            System.out.println(("end : " + len));
        }
    }
}
