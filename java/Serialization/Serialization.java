/**
  * The 'serialization' class allows to serialize data and to deserialize it.
  * It allows, for example, to save the company’s clocking or employees.
 */

package Serialization;

import java.io.*;

public class Serialization implements Serializable {

    // - - - METHODS - - -
    /**
     * This method allows to save the informations of an object in the file in parameter
     * @param obj : Object
     * @param fileName : String
     */
    public static void saveObject(Object obj, String fileName)
    {
        try(FileOutputStream fis = new FileOutputStream(fileName); //we open or create the file
            ObjectOutputStream oss = new ObjectOutputStream(fis)) //we translate the object in binary
        {
            oss.writeObject(obj); //we write the informations of the object in the file
            System.out.println("Objet sauvegardé");
        }
        catch (IOException error) { //we manage the errors
            error.printStackTrace();
        }
    }

    /**
     * we allow to load a file and return the object who was in the file
     * @param fileName : String
     * @return Object
     */
    public static Object loadObject(String fileName) {
        try (FileInputStream fos = new FileInputStream(fileName); //we open the file
             ObjectInputStream oos = new ObjectInputStream(fos)) // we read the file
        {
            return oos.readObject(); //we rebuilt the object who was in the file and we return it
        }
        catch (IOException | ClassNotFoundException error) { //we manage the potentials errors
            error.printStackTrace();
            return null;
        }
    }

}
