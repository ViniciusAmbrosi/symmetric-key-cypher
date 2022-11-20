package unisinos.com.br.cipher;

import htsjdk.samtools.cram.io.DefaultBitInputStream;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class CipherKeyScheduler {

    private static final String SPECIAL_CHAR_REGEX = "[^a-zA-Z0-9]";
    private static final byte[][] subKeys = new byte[4][];

    //will be used to apply permutation on keys with size of 4 bytes / 32 bits. Random for each execution.
    private final ArrayList<Integer> keyPermutation;

    //will be used to apply permutation on sub keys with size of 4 bytes / 32 bits. Random for each execution.
    private final ArrayList<Integer> subKeyPermutation;

    public CipherKeyScheduler()
    {
        int size = 32;

        ArrayList<Integer> bitPositions = new ArrayList<>(size);
        for(int i = 1; i <= size; i++) {
            bitPositions.add(i);
        }

        keyPermutation = new ArrayList<Integer>(bitPositions);
        Collections.shuffle(keyPermutation);

        subKeyPermutation = new ArrayList<Integer>(bitPositions);
        Collections.shuffle(subKeyPermutation);
    }

    public void ScheduleKeys(byte[] initialKey) throws IOException
    {
        if (initialKey.length != 4)
        {
            System.out.println("Scheduler only supports keys of 32 bits / 4 bytes.");
            return;
        }

        var permutedKeyBits = GetPermutedKeyBits(initialKey);

        for (int subKeyIndex = 0; subKeyIndex < subKeys.length; subKeyIndex++) {
            String subKey = GenerateSubKey(permutedKeyBits);
            String[] subKeyBits = subKey.split("");

            for (int j = 0; j < subKeyBits.length && j < permutedKeyBits.length; j++) {
                permutedKeyBits[subKeyPermutation.get(j) - 1] = subKeyBits[j];
            }

            StringBuilder subKeyByte = new StringBuilder();
            for (int j = 0; j < permutedKeyBits.length; j++) {
                if ((j + 1) % 8 == 0) { //if 1 byte
                    subKeys[subKeyIndex] = subKeyByte.toString().getBytes();
                    subKeyByte = new StringBuilder();
                } else {
                    subKeyByte.append(permutedKeyBits[j]);
                }
            }
        }
    }

    public int[][] GetSubKeys() throws IOException
    {
        int subKeyIndex = 0;
        int[][] subKeyBits = new int[4][32];

        for (var subKey : subKeys)
        {
            ByteArrayInputStream byteArray = new ByteArrayInputStream(subKey);
            try(var bitInputStream = new DefaultBitInputStream(byteArray))
            {
                int[] keyBits = new int[48];

                for (int i = 0; i < 48; i++) {
                    keyBits[i] = bitInputStream.readBit() ? 1 : 0;
                }

                subKeyBits[subKeyIndex++] = keyBits;
            }
        }

        return subKeyBits;
    }

    private String GenerateSubKey(String[] permutedKeyBits)
    {
        String permutedKey = Arrays.toString(permutedKeyBits).replaceAll(SPECIAL_CHAR_REGEX, "");

        int leftKey = Integer.rotateLeft(Integer.parseInt(permutedKey.substring(0, 16), 2), 2);
        int rightKey = Integer.rotateLeft(Integer.parseInt(permutedKey.substring(16), 2),2);

        return Long.toBinaryString(((long) leftKey << 16) + rightKey);
    }

    private String[] GetPermutedKeyBits(byte[] initialKey) throws IOException {
        ByteArrayInputStream byteArray = new ByteArrayInputStream(initialKey);

        try(var bitInputStream = new DefaultBitInputStream(byteArray))
        {
            String[] bits = new String[32];
            for (int value : keyPermutation) {
                bits[value - 1] = bitInputStream.readBit() ? "1" : "0";
            }

            return  bits;
        }
        catch (IOException e)
        {
            System.out.println("Failed to parse key bits.");
            throw e;
        }
    }
}
