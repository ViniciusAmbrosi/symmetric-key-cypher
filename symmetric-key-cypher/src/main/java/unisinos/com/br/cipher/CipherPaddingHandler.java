package unisinos.com.br.cipher;

import htsjdk.samtools.cram.io.DefaultBitInputStream;
import htsjdk.samtools.cram.io.DefaultBitOutputStream;
import htsjdk.samtools.util.RuntimeEOFException;
import org.apache.commons.lang3.StringUtils;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Arrays;

public class CipherPaddingHandler
{
    public void ProcessPaddingForEncryption(
            byte[] fileByteArray,
            DefaultBitOutputStream encryptedBitOutputStream) throws IOException
    {
        int paddingCount = CalculateRequiredPadding(fileByteArray);
        String paddingText = StringUtils.leftPad(String.valueOf(paddingCount), 2, '0');

        ByteArrayInputStream paddingBytesInput = new ByteArrayInputStream(paddingText.getBytes());
        try(var paddingBitInputStream = new DefaultBitInputStream(paddingBytesInput))
        {
            while (true) {
                try {
                    encryptedBitOutputStream.write(paddingBitInputStream.readBit());
                } catch (RuntimeEOFException e) {
                    break;
                }
            }
        }
        catch (IOException e) {
            System.out.println("Failed when processing padding for input file.");
            throw e;
        }

        System.out.println("");
    }

    public int ProcessPaddingForDecryption(DefaultBitInputStream decryptionBitInputStream)
    {
        StringBuilder paddingHeader = new StringBuilder();

        for (int j = 0; j < 16; j++) {
            paddingHeader.append(decryptionBitInputStream.readBit() ? "1" : "0");
        }

        StringBuilder paddingBits = new StringBuilder();
        Arrays.stream(SplitBitsInByteArray(paddingHeader.toString()))
                .forEach(s -> paddingBits.append((char) Integer.parseInt(s, 2)));

        var paddingString = paddingBits.toString();
        return Integer.valueOf(paddingString);
    }

    private String[] SplitBitsInByteArray(String paddingHeader)
    {
        return paddingHeader.toString().split("(?<=\\G.{8})");
    }

    private int CalculateRequiredPadding(byte[] fileByteArray)
    {
        return Math.abs(((fileByteArray.length * 8) % 48) - 48);
    }
}
