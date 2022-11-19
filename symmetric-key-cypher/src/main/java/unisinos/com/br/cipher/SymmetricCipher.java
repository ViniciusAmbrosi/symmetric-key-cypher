package unisinos.com.br.cipher;

import java.util.concurrent.ThreadLocalRandom;

public class SymmetricCipher {

    //this vector is used to initialize cipher block chain (CBC), and will be uniquely generated for each operation
    private int[] initializationVector = new int[48];

    public void SymmetricCipher()
    {
        PopulateInitializationVector();
    }

    public byte[] Encrypt()
    {
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
