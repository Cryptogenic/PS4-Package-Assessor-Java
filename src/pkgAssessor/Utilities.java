/* This package is distributed under the Apache 2.0 License. */
package pkgAssessor;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;

/**
 * Contains code for evaluating the package, converting hex to ascii, and bytes
 * to a hex string.
 *
 * @author  Specter
 * @author  Smithy
 * @version 1.0
 * @since   2016-05-29
 */
public class Utilities {
    
    /**
     * Returns an ArrayList of strings for the information for a package evaluated.
     * The package is passed to the method via the parameter pkgFile.
     * <p>
     * Each index of the ArrayList contains a specific piece of information
     * about the package;
     * <p>
     * [0] The raw binary of the package
     * <p>
     * [1] The file magic (PKG, CNT, or UNK)
     * <p>
     * [2] The content ID of the package
     * <p>
     * [3] The package size in bytes as a hex string
     * <p>
     * [4] The size of the data .section of the package in bytes as a hex string
     * <p>
     * [5] The offset of the data .section of the package as a hex string
     * <p>
     * [6-26] Contents of the package (CNT magic), each index contains one piece of content
     *
     * @author Specter
     * @author Smithy
     * @param  pkgFile  the file to be evaluated
     * @throws Exception if an error occurs while opening the file or parsing it
     * @return      the information about the package as a string ArrayList  
     */
    public static ArrayList<String> evaluatePackage(File pkgFile) throws Exception
    {
        /* Stores all information needed for the package */
        ArrayList<String> pkgInfo = new ArrayList<String>();
        
        /* Stores specific piece of information being fetched for readability */
        String pkgBinary = "";
        String pkgID;
        String pkgSize;
        String pkgDataSize;
        String pkgDataOffset;
        String pkgContentsAddress;
        
        /* Allows 20 pieces of content to be stored for pushing into pkgInfo */
        String[] pkgContents = new String[20];
        
        /* Contains the address for the content */
        int contentAddress = 0;
        
        FileInputStream pkgInputStream = new FileInputStream(pkgFile.getPath());
        
        /* Get full package binary, add to return list */
        
        int length;
        byte data[] = new byte[(int)pkgFile.length() + 1];
        
        do
        {
            length = pkgInputStream.read(data);
        } while(length != -1);
        
        pkgBinary = bytesToHex(data);
        
        pkgInfo.add(pkgBinary); // [0] Package Binary
        
        /* 
            We must check file magic, as some of the offsets are different;
            - content ID (CNT: 0x40-0x53, PKG: 0x30-0x63)
            - CNT contains ASCII readable contents, PKG does not
        */
        
        /* Check file magic for CNT header */
        if(pkgBinary.substring(0, 8).equals("7F434E54"))
        {
            /* .CNT magic header */
            System.out.println("File contains a .CNT magic header...");
            
            /* Set pkgInfo[1] to contain header magic */
            pkgInfo.add("CNT");
            
            /* Get content ID - Offset 0x40 - 0x53 */
            pkgID = hexToAscii(pkgBinary.substring(0x40 * 2, 0x64 * 2), false);
            pkgInfo.add(pkgID);
        }
        /* Check file magic for PKG header */
        else if(pkgBinary.substring(0, 8).equals("7F504B47"))
        {
            /* .PKG magic header */
            System.out.println("File contains a .PKG magic header...");
            
            /* Set pkgInfo[1] to contain header magic */
            pkgInfo.add("PKG");
            
            /* Get content ID - Offset 0x30 - 0x63 */
            pkgID = hexToAscii(pkgBinary.substring(0x30 * 2, 0x54 * 2), false);
            pkgInfo.add(pkgID);
        }
        else
        {
            /* Unrecognized magic header */
            System.out.println("File has unknown magic header, aborting...");
            
            /* Set pkgInfo[0] to contain header magic */
            pkgInfo.add("UNK");
        }
        
        /* If the file magic is unknown, file is not supported, skip fetching other information */
        if(pkgBinary.substring(0, 8).equals("7F434E54") || pkgBinary.substring(0, 8).equals("7F504B47"))
        {
            /* Supported file */
            System.out.println("Supported package, evaluating...");
            
            /* Get package size - Offset 0x18 - 0x1F */
            pkgSize = pkgBinary.substring(0x18 * 2, 0x20 * 2);
            pkgInfo.add("0x" + pkgSize + " (" + Long.parseLong(pkgSize, 16) + " bytes)");

            /* Get package data .section size - Offset 0x28 - 0x2F */
            pkgDataSize = pkgBinary.substring(0x28 * 2, 0x30 * 2);
            pkgInfo.add("0x" + pkgDataSize + " (" + Long.parseLong(pkgDataSize, 16) + " bytes)");

            /* Get package data .section offset - Offset 0x20 - 0x27 */
            pkgDataOffset = pkgBinary.substring(0x20 * 2, 0x28 * 2);
            pkgInfo.add("0x" + pkgDataOffset);
            
            /* 
                Get package contents, more information:
                Address of contents is stored at 0x2B30. We then must add 0x1 to get
                actual contents.
            */
            if(pkgBinary.substring(0, 8).equals("7F434E54"))
            {
                pkgContentsAddress = (pkgBinary.substring(0x2B30 * 2, 0x2B34 * 2));

                contentAddress = (Integer.parseInt(pkgContentsAddress, 16) * 2) + 0x2;
                System.out.println("Found package contents address: 0x" + pkgContentsAddress);
                
                /* Read from contentAddress to contentAddress+400 (200 bytes or 400 half-bytes) */
                pkgContents = hexToAscii(pkgBinary.substring(contentAddress, contentAddress + 400), true).split("\\|", 19);
                
                for(int i = 0; i < 20; i++)
                {
                    if(i < pkgContents.length)
                    {
                        if(!pkgContents[i].equals(""))
                        {
                            pkgInfo.add(pkgContents[i]);
                        }
                    }
                }
            }
            else
            {
                /* Other magic headers like .PKG do not support ASCII contents */
                pkgInfo.add("This file magic does not contain readable ASCII content.");
            }
        }
        return pkgInfo;
    }
    
    /* Necessary array for converting bytes to a hex string */
    final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();
    
    /**
     * Contains the implementation for the XML GUI interface.
     *
     * @author  maybeWeCouldStealAVan (StackOverflow)
     * @param bytesBuffer the array of bytes to be converted
     * @return string of the hexadecimal representation of the array of bytes
     */
    public static String bytesToHex(byte[] bytesBuffer) {
        char[] hexChars = new char[bytesBuffer.length * 2];
        for ( int j = 0; j < bytesBuffer.length; j++ ) {
            int v = bytesBuffer[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }
    
    /**
     * Returns the ASCII version of a hex string
     * <p>
     * @author Specter
     * @param  buffer  the string to convert to ASCII
     * @param  convertNullBytes  if true, replaces null bytes with pipelines
     * @return      converted string
     */
    public static String hexToAscii(String buffer, boolean convertNullBytes)
    {
        StringBuilder ascii = new StringBuilder();
        
        for (int i = 0; i < buffer.length(); i+=2) {
            String hexByte = buffer.substring(i, i+2);
            if(!hexByte.equals("00") || !convertNullBytes)
            {
                ascii.append((char)Integer.parseInt(hexByte, 16));
            }
            else
            {
                ascii.append("|");
            }
        }
        
        return ascii.toString();
    }
}