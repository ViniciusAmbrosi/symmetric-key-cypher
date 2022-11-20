package unisinos.com.br.file;

import org.apache.commons.io.FileUtils;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

//reusing class created in previous task. Encode-Decode found at https://github.com/ViniciusAmbrosi/encode-decode
public class FileUtilsWrapper
{

    public void WriteByteArrayToFile(String fileName, byte[] content){
        try
        {
            FileUtils.writeByteArrayToFile(
                    new File("C:\\Project\\teoria-informacao\\symmetric-key-cypher\\symmetric-key-cypher\\resources\\" + fileName),
                    content);
        }
        catch(Exception e)
        {
            System.out.println("Failure when writing to file.");
        }
    }

    public byte[] ReadFromFile(String fileName, boolean isPathAbsolute)
    {
        try
        {
            var fullPath = isPathAbsolute ?
                    fileName :
                    "C:\\Project\\teoria-informacao\\symmetric-key-cypher\\symmetric-key-cypher\\resources\\" + fileName;

            return Files.readAllBytes(Paths.get(fullPath));
        }
        catch (IOException e) {
            System.out.println("Failure when reading from file.");
            return null;
        }
    }
}
