package unisinos.com.br;

import org.apache.commons.lang3.StringUtils;
import unisinos.com.br.cipher.SymmetricCipher;
import unisinos.com.br.file.FileUtilsWrapper;

public class App
{
    private static final String DEFAULT_CYPHER_KEY = "abcd";
    private static final FileUtilsWrapper FILE_UTILS_WRAPPER = new FileUtilsWrapper();

    public static void main(String[] args) throws Exception
    {
        var cipherKey = DEFAULT_CYPHER_KEY;

        if(args.length < 1 || StringUtils.isEmpty(args[0]))
        {
            System.out.println("No file path provided. Halting execution.");
            return;
        }
        if(args.length < 2 || StringUtils.isEmpty(args[1]) || args[1].length() != 4)
        {
            System.out.println("Cypher key is either invalid or was not provided, using " + DEFAULT_CYPHER_KEY + " instead.");
        }
        else
        {
            cipherKey = args[1];
        }

        byte[] fileByteArray = FILE_UTILS_WRAPPER.ReadFromFile(args[0]);

        if(fileByteArray == null)
        {
            System.out.println("File path provided could not be read. Halting execution.");
            return;
        }

        //apply cypher with fileByteArray
        SymmetricCipher symmetricCipher = new SymmetricCipher(cipherKey);

        byte[] result = symmetricCipher.Encrypt(fileByteArray, cipherKey);
        FILE_UTILS_WRAPPER.WriteByteArrayToFile("encryptResult", result);

        byte[] decrypt = symmetricCipher.Decrypt("C:\\Project\\teoria-informacao\\symmetric-key-cypher\\symmetric-key-cypher\\resources\\encryptResult");
        FILE_UTILS_WRAPPER.WriteByteArrayToFile("decryptResult", decrypt);
    }
}
