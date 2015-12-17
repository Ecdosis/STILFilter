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

package mml.filters.english.harpur;
import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
import org.json.simple.JSONValue;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.File;
/**
 * A filter to convert STIL markup extracted from XML to MML style properties
 * @author desmond
 */
public class Filter extends mml.filters.Filter
{
    public Filter()
    {
        super();
        /**
         * Set up the map for quick lookup of property names
         */
        // source in table-harpur.json. convert with JSCompacter
        table = 
        "[{\"from\":{\"name\":\"div\",\"key\":\"type\",\"value\":\"tit"
        +"lepage\"},\"to\":\"titlepage\"}, {\"from\":{\"name\":\"div\""
        +",\"key\":\"type\",\"value\":\"dedication\"},\"to\":\"dedicat"
        +"ion\"}, {\"from\":{\"name\":\"div\",\"key\":\"type\",\"value"
        +"\":\"poempart\"},\"to\":\"poempart\"}, {\"from\":{\"name\":\""
        +"div\",\"key\":\"type\",\"value\":\"song\"},\"to\":\"song\"}"
        +", {\"from\":{\"name\":\"div\",\"key\":\"type\",\"value\":\"c"
        +"olophon\"},\"to\":\"colophon\"}, {\"from\":{\"name\":\"head\""
        +"},\"to\":\"head\"}, {\"from\":{\"name\":\"head\",\"key\":\""
        +"type\",\"value\":\"title\"},\"to\":\"title\"}, {\"from\":{\""
        +"name\":\"head\",\"key\":\"type\",\"value\":\"subtitle\"},\"t"
        +"o\":\"subtitle\"}, {\"from\":{\"name\":\"head\",\"key\":\"ty"
        +"pe\",\"value\":\"parthead\"},\"to\":\"parthead\"}, {\"from\""
        +":{\"name\":\"lg\"},\"to\":\"stanza\"}, {\"from\":{\"name\":\""
        +"p\"},\"to\":\"paragraph\"}, {\"from\":{\"name\":\"trailer\"},\"t"
        +"o\":\"trailer\"}, {\"from\":{\"name\":\"fw\"},\"to\":\"first"
        +"word\"}, {\"from\":{\"name\":\"q\"},\"to\":\"quote1\"}, {\"f"
        +"rom\":{\"name\":\"l\"},\"to\":\"line\"}, {\"from\":{\"name\""
        +":\"l\",\"key\":\"rend\",\"value\":\"indent1\"},\"to\":\"line"
        +"-indent1\"}, {\"from\":{\"name\":\"l\",\"key\":\"rend\",\"va"
        +"lue\":\"indent2\"},\"to\":\"line-indent2\"}, {\"from\":{\"na"
        +"me\":\"l\",\"key\":\"rend\",\"value\":\"indent3\"},\"to\":\""
        +"line-indent3\"}, {\"from\":{\"name\":\"l\",\"key\":\"rend\","
        +"\"value\":\"indent4\"},\"to\":\"line-indent4\"}, {\"from\":{"
        +"\"name\":\"l\",\"key\":\"rend\",\"value\":\"indent5\"},\"to\""
        +":\"line-indent5\"}, {\"from\":{\"name\":\"l\",\"key\":\"typ"
        +"e\",\"value\":\"F\"},\"to\":\"line-final\"}, {\"from\":{\"na"
        +"me\":\"hi\",\"key\":\"rend\",\"value\":\"ul\"},\"to\":\"unde"
        +"rlined\"}, {\"from\":{\"name\":\"hi\",\"key\":\"rend\",\"val"
        +"ue\":\"sc\"},\"to\":\"smallcaps\"}, {\"from\":{\"name\":\"hi"
        +"\",\"key\":\"rend\",\"value\":\"b\"},\"to\":\"bold\"}, {\"fr"
        +"om\":{\"name\":\"hi\",\"key\":\"rend\",\"value\":\"it\"},\"t"
        +"o\":\"italics\"}, {\"from\":{\"name\":\"hi\",\"key\":\"rend\""
        +",\"value\":\"dul\"},\"to\":\"double-underlined\"}, {\"from\""
        +":{\"name\":\"hi\",\"key\":\"rend\",\"value\":\"ss\"},\"to\""
        +":\"superscript\"}, {\"from\":{\"name\":\"hi\",\"key\":\"rend"
        +"\",\"value\":\"erasure\"},\"to\":\"erased\"}, {\"from\":{\"n"
        +"ame\":\"hi\",\"key\":\"rend\",\"value\":\"bl\"},\"to\":\"bla"
        +"ck-letter\"}, {\"from\":{\"name\":\"hi\",\"key\":\"rend\",\""
        +"value\":\"pencil\"},\"to\":\"pencil\"}, {\"from\":{\"name\":"
        +"\"hi\",\"key\":\"rend\",\"value\":\"del-pencil\"},\"to\":\"d"
        +"el-pencil\"}, {\"from\":{\"name\":\"emph\"},\"to\":\"emphasi"
        +"s\"}, {\"from\":{\"name\":\"unclear\"},\"to\":\"unclear\"}, "
        +"{\"from\":{\"name\":\"expan\"},\"to\":\"expanded\"}, {\"from"
        +"\":{\"name\":\"metamark\"},\"to\":\"metamark\"}, {\"from\":{"
        +"\"name\":\"author\"},\"to\":\"author\"}, {\"from\":{\"name\""
        +":\"date\"},\"to\":\"date\"}, {\"from\":{\"name\":\"sic\"},\""
        +"to\":\"sic\"}, {\"from\":{\"name\":\"divider\",\"key\":\"typ"
        +"e\",\"value\":\"single\"},\"to\":\"divider-single\"}, {\"fro"
        +"m\":{\"name\":\"divider\",\"key\":\"type\",\"value\":\"diamo"
        +"nd\"},\"to\":\"divider-diamond\"}, {\"from\":{\"name\":\"div"
        +"ider\",\"key\":\"type\",\"value\":\"double\"},\"to\":\"divid"
        +"er-double\"}]\n";
        initMap();
    }
    private String cleanRef( String ref )
    {
        // remove trailing file type
        int index = ref.indexOf(".");
        if ( index > 0 )
            ref = ref.substring(0,index);
        // remove leading 0s
        int j = 0;for ( int i=0;i<ref.length();i++ )
            if ( ref.charAt(i)=='0' )
                j++;
        if ( j > 0 )
            ref = ref.substring(j);
        return ref;
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
            if ( attr.containsKey("n") )
            {
                ref = (String)attr.get("n");
                // n overrides facs
                break;
            }
            else if ( attr.containsKey("facs") && ref.length()==0 )
            {
                String value = (String)attr.get("facs");
                int index = value.lastIndexOf("/");
                if ( index != -1 )
                    ref = value.substring(index+1);
                else
                    ref = value;
                ref = cleanRef(ref);
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
                Filter f = new Filter();
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
