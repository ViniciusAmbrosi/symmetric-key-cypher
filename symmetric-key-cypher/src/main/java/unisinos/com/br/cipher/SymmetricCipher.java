package unisinos.com.br.cipher;

import htsjdk.samtools.cram.io.DefaultBitInputStream;
import htsjdk.samtools.cram.io.DefaultBitOutputStream;
import org.apache.commons.lang3.StringUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.concurrent.ThreadLocalRandom;

public class SymmetricCipher {

    //this vector is used to initialize cipher block chain (CBC), and will be uniquely generated for each operation
    private int[] initializationVector = new int[48];
    private CipherPaddingHandler cipherPaddingHandler;

    public void SymmetricCipher()
    {
        PopulateInitializationVector();
        this.cipherPaddingHandler = new CipherPaddingHandler();
    }

    public byte[] Encrypt(byte[] fileByteArray)
    {
        ByteArrayOutputStream encryptedByteOutputStream = new ByteArrayOutputStream();
        try(var encryptedBitOutputStream = new DefaultBitOutputStream(encryptedByteOutputStream))
        {
            cipherPaddingHandler.ProcessPaddingForEncryption(fileByteArray, encryptedBitOutputStream);
        }
        catch(Exception e)
        {
            System.out.println("Failed while encrypting file.");
        }

        return new byte[] { 1, 2 };
    }

    public String Decrypt()
    {
        return "1 2";
    }

    //randomly generates values for initialization vector
    private void PopulateInitializationVector()
    {
        for (int i = 0; i < initializationVector.length; i++)
        {
            initializationVector[i] = ThreadLocalRandom.current().nextInt(0, 100 + 1);
        }
    }
}
