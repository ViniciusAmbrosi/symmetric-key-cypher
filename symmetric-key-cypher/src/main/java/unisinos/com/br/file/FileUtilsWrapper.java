package unisinos.com.br.file;

import org.apache.commons.io.FileUtils;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

//reusing class created in previous task. Encode-Decode found at https://github.com/ViniciusAmbrosi/encode-decode
public class FileUtilsWrapper {

    public void WriteToFile(String fileName, String content){
        try
        {
            FileUtils.write(
                    new File("C:\\Project\\encoder\\encoder\\resources\\" + fileName),
                    content,
                    StandardCharsets.UTF_8);
        }
        catch(Exception e)
        {
            System.out.println("Failure when writing to file.");
        }
    }

    public void WriteByteArrayToFile(String fileName, byte[] content){
        try
        {
            FileUtils.writeByteArrayToFile(
                    new File("C:\\Project\\encoder\\encoder\\resources\\" + fileName),
                    content);
        }
        catch(Exception e)
        {
            System.out.println("Failure when writing to file.");
        }
    }

    public byte[] ReadFromFile(String fileName)
    {
        try
        {
            return Files.readAllBytes(Paths.get(fileName));
        }
        catch (IOException e) {
            System.out.println("Failure when reading from file.");
            return null;
        }
    }
}
