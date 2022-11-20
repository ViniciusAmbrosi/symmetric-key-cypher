package unisinos.com.br.cipher;

public class CipherBlockChainHandler
{
    public int[] ApplyChaining(int[] messageBlock, int[] previousBlock)
    {
        int[] cbcResult = new int[messageBlock.length];

        for (int i = 0; i < cbcResult.length; i++) {

            //apply xor between current and previously encrypted message or initialization vector
            cbcResult[i] = messageBlock[i] ^ previousBlock[i];
        }
        return cbcResult;
    }
}
