package unisinos.com.br.cipher;

import htsjdk.samtools.cram.io.BitInputStream;
import htsjdk.samtools.cram.io.DefaultBitInputStream;
import htsjdk.samtools.cram.io.DefaultBitOutputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.concurrent.ThreadLocalRandom;

public class SymmetricCipher {

    //this vector is used to initialize cipher block chain (CBC), and will be uniquely generated for each operation
    private int[] initializationVector = new int[48];
    private CipherPaddingHandler cipherPaddingHandler;

    public SymmetricCipher()
    {
        PopulateInitializationVector();
        this.cipherPaddingHandler = new CipherPaddingHandler();
    }

    public byte[] Encrypt(byte[] fileByteArray)
    {
        ByteArrayOutputStream encryptedByteOutputStream = new ByteArrayOutputStream();
        try(var encryptedBitOutputStream = new DefaultBitOutputStream(encryptedByteOutputStream))
        {
            //adds padding count to header for process on decrypt
            cipherPaddingHandler.ProcessPaddingForEncryption(fileByteArray, encryptedBitOutputStream);

            //starts reading file to encrypt
            ByteArrayInputStream byteArray = new ByteArrayInputStream(fileByteArray);
            try(var bitInputStream = new DefaultBitInputStream(byteArray))
            {
                int[] messageBlock = GetBlock(bitInputStream);
                //do cbc
                //do encrypt
                //write to output stream
            }
            catch (Exception e)
            {
                System.out.println("Failed while reading source file.");
                throw e;
            }
        }
        catch(Exception e)
        {
            System.out.println("Failed while encrypting file.");
        }

        return encryptedByteOutputStream.toByteArray();
    }

    public String Decrypt()
    {
        return "1 2";
    }

    //Get message block for encryption
    private int[] GetBlock(DefaultBitInputStream inputStream) throws IOException
    {
        int[] messageBits = new int[48];
        int currentBit = 0;
        boolean hasData = true;

        while(hasData)
        {
            for (currentBit = 0; currentBit < 48; currentBit++) {
                if(inputStream.available() > 0)
                {
                    messageBits[currentBit] = inputStream.readBit() ? 1 : 0;
                }
                else
                {
                    hasData = false;
                    break;
                }
            }

            //add padding to block
            for (int j = currentBit; j < 48; j++) {
                messageBits[j] = 0;
            }

            currentBit = 0;
        }

        return messageBits;
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
