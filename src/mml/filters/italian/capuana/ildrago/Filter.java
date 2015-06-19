/*
 * This file is part of STILFilter, which is part of the ecdosis suite of 
 * programs, and is required by the MML service.
 *
 *  STILFilter is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  STILFilter is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with STILFilter.  If not, see <http://www.gnu.org/licenses/>.
 *  (c) copyright Desmond Schmidt 2015
 */
package mml.filters.italian.capuana.ildrago;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

/**
 * The Filter Interface
 * @author desmond
 */
public class Filter extends mml.filters.Filter
{
    /**
     * Initialise a Filter object
     */
    public Filter()
    {
        super();
        this.table = 
        "[{\"from\":{\"name\":\"div\"},\"to\":\"section\"}, {\"from\":"
        +"{\"name\":\"p\"},\"to\":\"paragraph\"},{\"from\":{\"name\":\"h"
        +"ead\"},\"to\":\"head\"}]\n";
        initMap();
    }
    /**
     * Extract the page reference from a pb range
     * @param annotations the annotations of the pb range (maybe n as well)
     * @return the page ref devoid of url info, path etc
     * @override
     */
    protected String getPageRef( JSONArray annotations )
    {
        String ref = "";
        for ( int i=0;i<annotations.size();i++ )
        {
            JSONObject attr = (JSONObject)annotations.get(i);
            if ( attr.containsKey("facs") )
            {
                return (String)attr.get("facs");
            }
        }
        return ref;
    }
    /**
     * Append something to the file name before the extension
     * @param orig original file name
     * @param suffix the "suffix" to add
     * @return the modified file name
     */
    private static String appendToFileName( String orig, String suffix )
    {
        String lhs,rhs;
        int index = orig.lastIndexOf(".");
        if ( index != -1 )
        {
            lhs = orig.substring(0,index);
            rhs = orig.substring(index);
        }
        else    // no extension
        {
            lhs = orig;
            rhs = "";
        }
        return lhs+"-"+suffix+rhs;
    }
    /**
     * Debug: write out result
     * @param orig the original file name for conversion
     * @param data the data of the new file
     * @throws Exception 
     */
    static void writeOut( String orig, String data ) throws Exception
    {
        File out = new File(appendToFileName(orig,"converted"));
        if ( out.exists() )
            out.delete();
        out.createNewFile();
        FileOutputStream fos = new FileOutputStream(out);
        fos.write(data.getBytes("UTF-8"));
        fos.close();
    }
    /** just for testing */
    public static void main(String[] args )
    {
        try
        {
            if ( args.length==2 )
            {          
                File stil = new File(args[0]);
                File text = new File(args[1]);
                FileInputStream json = new FileInputStream(stil);
                FileInputStream body = new FileInputStream(text);
                byte[] data1 = new byte[(int)stil.length()];
                byte[] data2 = new byte[(int)text.length()];
                json.read(data1);
                body.read(data2);
                mml.filters.italian.capuana.ildrago.Filter f = new mml.filters.italian.capuana.ildrago.Filter();
                JSONObject markup = (JSONObject)JSONValue.parse(new String(data1,"UTF-8"));
                JSONObject res = f.translate( markup, new String(data2,"UTF-8") );
                writeOut( args[0], res.toJSONString() );
                writeOut( args[1], f.getText() );
            }
            else
                System.out.println("usage: java STILFilter <stil> <text>");
        }
        catch ( Exception e )
        {
            System.out.println(e.getMessage());
        }
    }
}
