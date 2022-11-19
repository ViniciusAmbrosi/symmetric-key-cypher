package unisinos.com.br.cipher;

import htsjdk.samtools.cram.io.DefaultBitInputStream;
import htsjdk.samtools.cram.io.DefaultBitOutputStream;
import org.apache.commons.lang3.StringUtils;
import java.io.ByteArrayInputStream;
import java.io.IOException;

public class CipherPaddingHandler
{
    public void ProcessPaddingForEncryption(
            byte[] fileByteArray,
            DefaultBitOutputStream encryptedBitOutputStream) throws IOException
    {
        int paddingCount = CalculateRequiredPadding(fileByteArray);
        String paddingText = StringUtils.leftPad(String.valueOf(paddingCount), 2, '0');

        ByteArrayInputStream paddingBytesInput = new ByteArrayInputStream(paddingText.getBytes());
        try(var paddingBitOutputStream = new DefaultBitInputStream(paddingBytesInput))
        {
            while(paddingBitOutputStream.available() > 0)
            {
                encryptedBitOutputStream.write(paddingBitOutputStream.readBit());
            }
        }
        catch (IOException e) {
            System.out.println("Failed when processing padding for input file.");
            throw e;
        }
    }

    private int CalculateRequiredPadding(byte[] fileByteArray)
    {
        int numberOfBitsInFile = (fileByteArray.length * 8) % 48;
        return Math.abs(numberOfBitsInFile - 48);
    }
}
