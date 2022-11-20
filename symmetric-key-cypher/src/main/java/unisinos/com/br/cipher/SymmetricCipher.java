package unisinos.com.br.cipher;

import htsjdk.samtools.cram.io.DefaultBitInputStream;
import htsjdk.samtools.cram.io.DefaultBitOutputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

public class SymmetricCipher {

    //this vector is used to initialize cipher block chain (CBC), and will be uniquely generated for each operation
    private int[] initializationVector = new int[48];
    private CipherPaddingHandler cipherPaddingHandler;
    private CipherBlockChainHandler cipherBlockChainHandler;
    private CipherKeyScheduler cipherKeyScheduler;

    public SymmetricCipher(String cipherKey) throws IOException
    {
        PopulateInitializationVector();
        this.cipherPaddingHandler = new CipherPaddingHandler();
        this.cipherBlockChainHandler = new CipherBlockChainHandler();
        this.cipherKeyScheduler = new CipherKeyScheduler();

        this.cipherKeyScheduler.ScheduleKeys(cipherKey.getBytes());
    }

    public byte[] Encrypt(byte[] fileByteArray, String cipherKey)
    {
        ByteArrayOutputStream encryptedByteOutputStream = new ByteArrayOutputStream();
        try(var encryptedBitOutputStream = new DefaultBitOutputStream(encryptedByteOutputStream))
        {
            //adds padding count to header for process on decrypt
            cipherPaddingHandler.ProcessPaddingForEncryption(fileByteArray, encryptedBitOutputStream);

            //starts reading file to encrypt
            int[] previousEncryptedBlock = null;

            ByteArrayInputStream byteArray = new ByteArrayInputStream(fileByteArray);
            try(var bitInputStream = new DefaultBitInputStream(byteArray))
            {
                ArrayList<int[]> messageBlocks = GetMessageBlocks(bitInputStream);

                for (int[] messageBlock : messageBlocks)
                {
                    //do cbc
                    cipherBlockChainHandler.ApplyChaining(
                            messageBlock,
                            previousEncryptedBlock == null ? initializationVector : previousEncryptedBlock);

                    //do encrypt
                    previousEncryptedBlock = new int[24];

                    //write to output stream
                }
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

    //Get message blocks for encryption
    private ArrayList<int[]>  GetMessageBlocks(DefaultBitInputStream inputStream) throws IOException
    {
        ArrayList<int[]> arrayList = new ArrayList<>();

        int currentBit = 0;
        boolean hasData = true;

        while(hasData)
        {
            int[] messageBits = new int[48];

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
            arrayList.add(messageBits);
        }

        return arrayList;
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
