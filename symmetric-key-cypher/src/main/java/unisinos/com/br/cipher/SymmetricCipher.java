package unisinos.com.br.cipher;

import htsjdk.samtools.cram.io.DefaultBitInputStream;
import htsjdk.samtools.cram.io.DefaultBitOutputStream;
import htsjdk.samtools.util.RuntimeEOFException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class SymmetricCipher {

    //this vector is used to initialize cipher block chain (CBC), and will be uniquely generated for each operation
    private final int[] initializationVector = new int[48];
    private final CipherPaddingHandler cipherPaddingHandler;
    private final CipherBlockChainHandler cipherBlockChainHandler;
    private final CipherKeyScheduler cipherKeyScheduler;

    public SymmetricCipher(String cipherKey) throws IOException
    {
        this.cipherPaddingHandler = new CipherPaddingHandler();
        this.cipherBlockChainHandler = new CipherBlockChainHandler();
        this.cipherKeyScheduler = new CipherKeyScheduler();

        this.cipherKeyScheduler.ScheduleKeys(cipherKey.getBytes());
        PopulateInitializationVector();
    }

    public byte[] Encrypt(byte[] message, String key)
    {
        ByteArrayOutputStream bytesOutput = new ByteArrayOutputStream();
        try(var bitOutputStream = new DefaultBitOutputStream(bytesOutput))
        {
            //adds padding count to header for process on decrypt
            cipherPaddingHandler.ProcessPaddingForEncryption(message, bitOutputStream);

            //starts reading file to encrypt
            int[] previousEncryptedBlock = null;

            ByteArrayInputStream byteArray = new ByteArrayInputStream(message);
            try(var bitInputStream = new DefaultBitInputStream(byteArray))
            {
                ArrayList<int[]> messageBlocks = GetMessageBlocks(bitInputStream, true);

                for (int[] messageBlock : messageBlocks)
                {
                    //do cbc
                    int[] cbcResult = cipherBlockChainHandler.ApplyChaining(
                            messageBlock,
                            previousEncryptedBlock == null ? initializationVector : previousEncryptedBlock);

                    //do encrypt
                    previousEncryptedBlock = EncryptMessageBits(cbcResult);

                    //write to output stream
                    for (int encryptedBlock : previousEncryptedBlock) {
                        bitOutputStream.write(encryptedBlock == 1);
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

        return bytesOutput.toByteArray();
    }

    public byte[] Decrypt(byte[] encryptedFileByteArray) throws IOException
    {
        ByteArrayOutputStream bytesOutput = new ByteArrayOutputStream();
        try(var bitOutputStream = new DefaultBitOutputStream(bytesOutput))
        {
            ByteArrayInputStream byteArray = new ByteArrayInputStream(encryptedFileByteArray);
            try(var bitInputStream = new DefaultBitInputStream(byteArray))
            {
                int paddingAmount = cipherPaddingHandler.ProcessPaddingForDecryption(bitInputStream);

                ArrayList<int[]> messageBlocks = GetMessageBlocks(bitInputStream, false);

                List<Integer> decryptedResult = new ArrayList<>();
                int[] previousBlock = null;

                for (int[] messageBlock : messageBlocks)
                {
                    //decrypts - have to start from ending to decrypt
                    int[] result = DecryptMessageBits(messageBlock);

                    //do cbc
                    var cipherBlockChainMessage = cipherBlockChainHandler.ApplyChaining(result, previousBlock == null ? initializationVector : previousBlock);

                    //load previous encrypted block for next operation
                    previousBlock = new int[messageBlock.length];
                    for (int j = 0; j < messageBlock.length; j++) {
                        previousBlock[j] = messageBlock[j];
                    }

                    //load decryptResult
                    for (int j = 0; j < cipherBlockChainMessage.length; j++) {
                        decryptedResult.add(cipherBlockChainMessage[j]);
                    }
                }

                for (int j = 0; j < decryptedResult.size() - paddingAmount; j++) {
                    bitOutputStream.write(decryptedResult.get(j) == 1);
                }

                return bytesOutput.toByteArray();
            }
        }
    }

    private ArrayList<int[]>  GetMessageBlocks(DefaultBitInputStream inputStream, boolean encryption)
    {
        ArrayList<int[]> arrayList = new ArrayList<>();

        int currentBit = 0;
        boolean hasData = true;

        while(hasData)
        {
            int[] messageBits = new int[48];

            for (currentBit = 0; currentBit < 48; currentBit++) {
                try
                {
                    messageBits[currentBit] = inputStream.readBit() ? 1 : 0;
                }
                catch (RuntimeEOFException ex)
                {
                    break;
                }
            }

            if(encryption)
            {
                //add padding to block if doing encryption
                for (int j = currentBit; j < 48; j++) {
                    messageBits[j] = 0;
                }
            }

            if(currentBit < 48)
            {
                hasData = false;
            }
            if(currentBit != 0)
            {
                arrayList.add(messageBits);
            }
        }

        return arrayList;
    }

    private int[] DecryptMessageBits(int[] messageBits) throws IOException {
        var subKeys = cipherKeyScheduler.GetSubKeys();

        //need to go back to front for decryption
        for (int keyIndex = subKeys.length - 1; keyIndex >= 0; keyIndex--) {
            messageBits = SubstituteAndTranspose(messageBits, subKeys[keyIndex]);
        }

        return messageBits;
    }

    private int[] EncryptMessageBits(int[] messageBits) throws IOException {
        for (int[] subKey : cipherKeyScheduler.GetSubKeys()) {
            messageBits = SubstituteAndTranspose(messageBits, subKey);
        }

        return messageBits;
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

    private void PopulateInitializationVector() {
        for (int i = 0; i < initializationVector.length; i++)
        {
            //generates either 0 or 1
            initializationVector[i] = ThreadLocalRandom.current().nextInt(0, 1 + 1);
        }
    }
}
