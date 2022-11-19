package unisinos.com.br;

import org.apache.commons.lang3.StringUtils;
import unisinos.com.br.file.FileUtilsWrapper;

public class App
{
    private static FileUtilsWrapper fileUtilsWrapper = new FileUtilsWrapper();

    //arg 0 - source file path
    //arg 1 - cypher key
    //by running this method it will cypher and decipher the file, hence no action.
    public static void main(String[] args)
    {
        if(StringUtils.isEmpty(args[0]))
        {
            System.out.println("No file path provided.");
            return;
        }

        byte[] fileByteArray = fileUtilsWrapper.ReadFromFile(args[0]);
        var cypherKey = args[1];

        if(fileByteArray == null)
        {
            System.out.println("File path provided could not be read.");
        }

        if(StringUtils.isEmpty(cypherKey) || cypherKey.length() > 4)
        {
            cypherKey = "1234";
            System.out.println("Cypher key is either too big or was not provided, using 1234 instead.");
        }

        //apply cypher with fileByteArray && cypherKey


    }
}
