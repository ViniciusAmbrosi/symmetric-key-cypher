package unisinos.com.br.cipher;

import htsjdk.samtools.cram.io.DefaultBitInputStream;
import htsjdk.samtools.cram.io.DefaultBitOutputStream;
import htsjdk.samtools.util.RuntimeEOFException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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

    public byte[] Encrypt(byte[] fileByteArray)
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
                    var cipherBlockChainMessage = cipherBlockChainHandler.ApplyChaining(
                            messageBlock,
                            previousEncryptedBlock == null ? initializationVector : previousEncryptedBlock);

                    //do encrypt
                    previousEncryptedBlock = cipherBlockChainMessage.clone();
                    for (int[] subKey : cipherKeyScheduler.GetSubKeys()) {
                        previousEncryptedBlock = SubstituteAndTranspose(previousEncryptedBlock, subKey);
                    }

                    //write to output stream
                    for (int j = 0; j < previousEncryptedBlock.length; j++) {
                        encryptedBitOutputStream.write(previousEncryptedBlock[j] == 1);
                    }
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

    public String Decrypt(byte[] fileByteArray)
    {
        ByteArrayOutputStream bytesOutput = new ByteArrayOutputStream();
        try(var bitOutputStream = new DefaultBitOutputStream(bytesOutput))
        {
            ByteArrayInputStream byteArray = new ByteArrayInputStream(fileByteArray);

            try(var bitInputStream = new DefaultBitInputStream(byteArray))
            {
                //reads two bytes from the header to fetch information added during encryption
                int paddingAmount = cipherPaddingHandler.ProcessPaddingForDecryption(bitInputStream);

            }
            catch (Exception e)
            {
                System.out.println("Failed while reading source file.");
                throw e;
            }
        }
        catch (Exception e)
        {
            System.out.println("Failed while decrypting file.");
        }

        //return bytesOutput.toByteArray();
        return "";
    }

    private int[] SubstituteAndTranspose(int[] messageBits, int[] key)
    {
        int[] xorResult = new int[messageBits.length];

        for (int i = 0; i < xorResult.length; i++)
        {
            xorResult[i] = messageBits[i] ^ key[i];
        }

        return xorResult;
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
