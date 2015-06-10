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
import java.util.HashMap;
import java.util.ArrayList;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.File;
/* specials: ref target="xxx", note resp="xxx" graphic alt="xxx" title="xxx" url="xxx" pb n="xxx" facs="xxx" to be handled specifically in the filter */
/**
 * A filter to convert STIL markup extracted from XML to MML style properties
 * @author desmond
 */
public class Filter implements mml.filters.Filter
{
    /** specifies how to deal with simple property names and attributes */
    // source in table.json. convert with JSCompacter
    static String table = 
    "{ [{\"from\":{\"name\":\"div\",\"key\":\"type\",\"value\":\"t"
    +"itlepage\"},\"to\":\"titlepage\"}, {\"from\":{\"name\":\"div"
    +"\",\"key\":\"type\",\"value\":\"dedication\"},\"to\":\"dedic"
    +"ation\"}, {\"from\":{\"name\":\"div\",\"key\":\"type\",\"val"
    +"ue\":\"poempart\"},\"to\":\"poempart\"}, {\"from\":{\"name\""
    +":\"div\",\"key\":\"type\",\"value\":\"song\"},\"to\":\"song\""
    +"}, {\"from\":{\"name\":\"div\",\"key\":\"type\",\"value\":\""
    +"colophon\"},\"to\":\"colophon\"}, {\"from\":{\"name\":\"hea"
    +"d\",\"key\":\"type\",\"value\":\"title\"},\"to\":\"title\"},"
    +" {\"from\":{\"name\":\"head\",\"key\":\"type\",\"value\":\"s"
    +"ubtitle\"},\"to\":\"subtitle\"}, {\"from\":{\"name\":\"head\""
    +",\"key\":\"type\",\"value\":\"parthead\"},\"to\":\"parthead"
    +"\"}, {\"from\":{\"name\":\"head\"},\"to\":\"head\"}, {\"from"
    +"\":{\"name\":\"lg\"},\"to\":\"stanza\"}, {\"from\":{\"name\""
    +":\"p\"},\"to\":\"para\"}, {\"from\":{\"name\":\"trailer\"},\""
    +"to\":\"trailer\"}, {\"from\":{\"name\":\"fw\"},\"to\":\"fir"
    +"stword\"}, {\"from\":{\"name\":\"q\"},\"to\":\"quote\"}, {\""
    +"from\":{\"name\":\"l\"},\"to\":\"line\"}, {\"from\":{\"name\""
    +":\"line\",\"key\":\"rend\",\"value\":\"indent1\"},\"to\":\""
    +"line-indent1\"}, {\"from\":{\"name\":\"line\",\"key\":\"rend"
    +"\",\"value\":\"indent2\"},\"to\":\"line-indent2\"}, {\"from\""
    +":{\"name\":\"line\",\"key\":\"rend\",\"value\":\"indent3\"}"
    +",\"to\":\"line-indent3\"}, {\"from\":{\"name\":\"line\",\"ke"
    +"y\":\"rend\",\"value\":\"indent4\"},\"to\":\"line-indent4\"}"
    +", {\"from\":{\"name\":\"line\",\"key\":\"rend\",\"value\":\""
    +"indent5\"},\"to\":\"line-indent5\"}, {\"from\":{\"name\":\"l"
    +"ine\",\"key\":\"part\",\"value\":\"F\"},\"to\":\"line-final\""
    +"}, {\"from\":{\"name\":\"hi\",\"key\":\"rend\",\"value\":\""
    +"ul\"},\"to\":\"underlined\"}, {\"from\":{\"name\":\"hi\",\"k"
    +"ey\":\"rend\",\"value\":\"sc\"},\"to\":\"smallcaps\"}, {\"fr"
    +"om\":{\"name\":\"hi\",\"key\":\"rend\",\"value\":\"b\"},\"to"
    +"\":\"bold\"}, {\"from\":{\"name\":\"hi\",\"key\":\"rend\",\""
    +"value\":\"it\"},\"to\":\"italics\"}, {\"from\":{\"name\":\"h"
    +"i\",\"key\":\"rend\",\"value\":\"dul\"},\"to\":\"superscript"
    +"\"}, {\"from\":{\"name\":\"hi\",\"key\":\"rend\",\"value\":\""
    +"ss\"},\"to\":\"underlined\"}, {\"from\":{\"name\":\"hi\",\""
    +"key\":\"rend\",\"value\":\"erasure\"},\"to\":\"erased\"}, {\""
    +"from\":{\"name\":\"hi\",\"key\":\"rend\",\"value\":\"bl\"},"
    +"\"to\":\"black-letter\"}, {\"from\":{\"name\":\"hi\",\"key\""
    +":\"rend\",\"value\":\"pencil\"},\"to\":\"pencil\"}, {\"from\""
    +":{\"name\":\"hi\",\"key\":\"rend\",\"value\":\"del-pencil\""
    +"},\"to\":\"del-pencil\"}, {\"from\":{\"name\":\"emph\"},\"to"
    +"\":\"emphasis\"}, {\"from\":{\"name\":\"unclear\"},\"to\":\""
    +"unclear\"}, {\"from\":{\"name\":\"expan\"},\"to\":\"expanded"
    +"\"}, {\"from\":{\"name\":\"metamark\"},\"to\":\"metamark\"},"
    +" {\"from\":{\"name\":\"author\"},\"to\":\"author\"}, {\"from"
    +"\":{\"name\":\"date\"},\"to\":\"date\"}, {\"from\":{\"name\""
    +":\"sic\"},\"to\":\"sic\"}, {\"from\":{\"name\":\"divider\",\""
    +"key\":\"type\",\"value\":\"single\"},\"to\":\"divider-singl"
    +"e\"}, {\"from\":{\"name\":\"divider\",\"key\":\"type\",\"val"
    +"ue\":\"diamond\"},\"to\":\"divider-diamond\"}, {\"from\":{\""
    +"name\":\"divider\",\"key\":\"type\",\"value\":\"double\"},\""
    +"to\":\"divider-double\"}] } \n";
    /** the quick lookup map */
    static HashMap<String,ArrayList> map;
    /**
     * Set up the map for quick lookup of property names
     */
    static
    {
        map = new HashMap<String,ArrayList>();
        // each element name can combine with various attributes or with none
        JSONArray array = (JSONArray)JSONValue.parse( table );
        for ( Object item : array )
        {
            JSONObject jObj = (JSONObject) item;
            JSONObject from = (JSONObject)jObj.get("from");
            String key = (String) from.get("name");
            if ( !map.containsKey(key) )
                map.put( key, new ArrayList<JSONObject>());
            ArrayList<JSONObject> list = (ArrayList)map.get( key );
            list.add( jObj );
        }
    }
    /** hold the modified text here */
    StringBuilder sb;
    /** the momentary offset generated by inserting a page reference */
    int offset;
    /** the ranges of the destination STIL document */
    ArrayList<JSONObject> destRanges;
    /**
     * Initialise a Filter object
     */
    public Filter()
    {
        this.sb = new StringBuilder();
        this.offset = 0;
        this.destRanges = new ArrayList<JSONObject>();
    }
    /**
     * Does the range have the specified annotation?
     * @param range the old range in question
     * @param attrKey the attribute key
     * @param attrValue the attibute value - both must match
     * @return true if both attribute name and value were found
     */
    private boolean rangeHasAnnotation( JSONObject range, String attrKey, 
        String attrValue )
    {
        JSONArray annotations = (JSONArray) range.get("annotations");
        if ( annotations != null )
        {
            for ( int i=0;i<annotations.size();i++ )
            {
                JSONObject annotation = (JSONObject)annotations.get(i);
                if ( annotation.containsKey(attrKey) 
                    && annotation.get(attrKey).equals(attrValue) ) 
                    return true;
            }
        }
        return false;
    }
    /**
     * Can the named range take no attributes?
     * @param list the list of property and attribute combinations
     * @param rName the name of the range e.g. "div"
     * @return true if there is at least one definition with no attributes
     */
    private boolean listHasBareTag( ArrayList<JSONObject> list, String rName )
    {
        for ( int i=0;i<list.size();i++ )
        {
            JSONObject jObj = list.get(i);
            JSONObject from = (JSONObject) jObj.get("from");
            if ( !from.containsKey("key") )
                return true;
        }
        return false;
    }
    /**
     * Get the modified text
     * @return a string containing the text with inserted page references
     */
    public String getText()
    {
        return sb.toString();
    }
    /**
     * Extract the page reference from a pg range
     * @param annotations the annotations of the pg range (maybe n as well)
     * @return the page ref devoid of url info, path etc
     */
    private String getPageRef( JSONArray annotations )
    {
        String ref = "";
        for ( int i=0;i<annotations.size();i++ )
        {
            JSONObject attr = (JSONObject)annotations.get(i);
            if ( attr.containsKey("facs") )
            {
                String value = (String)attr.get("facs");
                int index = value.lastIndexOf("/");
                if ( index != -1 )
                    ref = value.substring(index+1);
                else
                    ref = value;
                break;
            }
        }
        return ref;
    }
    /**
     * Transfer an ordinary range from the old to the new STIL document
     * @param range the old range
     * @param newName the new name of the range
     */
    private void transferRange( JSONObject range, String newName )
    {
        JSONObject newRange = new JSONObject();
        int loc = ((Long)range.get("reloff")).intValue();
        // offset is the latest adjustment due to inserted page reference
        loc += offset;
        // because offsets are relative, we can reset to 0 after use
        offset = 0;
        newRange.put( "name", newName );
        newRange.put( "reloff", loc);
        newRange.put("len", range.get("len") );
        // NB we drop any attributes: not needed in MML
        destRanges.add( newRange );
    }
    /**
     * Append some data from the source text to the modified text
     * @param text the original text byte array
     * @param from the offset in text to copy from
     * @param len the length of the copied text in bytes
     * @throws Exception 
     */
    private void appendToText(byte[] text, int from, int len) throws Exception
    {
        byte[] chunk = new byte[len];
        System.arraycopy(text,from,chunk,0,chunk.length);
        sb.append(new String(chunk,"UTF-8") );
    }
    /**
     * Translate raw STIL from XML into STIL for the Harpur MML format
     * @param stil the raw XML STIL (JSON)
     * @param text the text it points to as a byte array
     * @return the new STIL standoff markup for the new text
     */
    public JSONObject translate( JSONObject stil, byte[] text ) throws Exception
    {
        JSONObject dest = new JSONObject();
        dest.put( "style", stil.get("style") );
        JSONArray ranges = (JSONArray) stil.get("ranges");
        dest.put("ranges",destRanges);
        int pos = 0;
        int lastReadPos = 0;
        for ( Object r : ranges )
        {
            JSONObject range = (JSONObject)r;
            pos += ((Long)range.get("reloff")).intValue();
            String rName = (String)range.get("name");
            if ( map.containsKey(rName) )
            {
                ArrayList list = (ArrayList) map.get(rName);
                    for ( int i=0;i<list.size();i++ )
                    {
                        JSONObject entry = (JSONObject)list.get(i);
                        JSONObject from = (JSONObject)entry.get("from");
                        if ( from.containsKey("key") )
                        {
                            String attrKey = (String)from.get("key");
                            String attrValue = (String)from.get("value");
                            if ( range.containsKey("annotations") )
                            {
                                if ( rangeHasAnnotation(range,attrKey,attrValue) )
                                {
                                    transferRange( range, (String)entry.get("to") );
                                }
                            }
                            else if ( listHasBareTag(list,rName) )
                            {
                                transferRange( range, (String)entry.get("to") );
                            }
                        }
                    }
                }
                else if ( rName.equals("pb") )
                {
                    JSONObject newRange = new JSONObject();
                    newRange.put("name","page");
                    int loc = ((Long)range.get("reloff")).intValue();
                    loc += offset;
                    offset = 0;
                    newRange.put("reloff",loc);
                    JSONArray annotations = (JSONArray) range.get("annotations");
                    String ref = getPageRef(annotations);
                    appendToText( text, lastReadPos, pos-lastReadPos );
                    lastReadPos = pos;
                    // ensure that ref is on a line by itself
                    if ( sb.charAt(sb.length()-1) != '\n' )
                        ref = "\n"+ref;
                    if ( text[pos] != (byte)'\n' )
                        ref += "\n";
                    sb.append(ref);
                    newRange.put("len",ref.length());
                    destRanges.add(newRange);
                    offset = ref.length();
                }
                else
                    System.out.println("Unknown property "+rName+" ignored");
            }
            if ( lastReadPos < text.length )
                appendToText( text, lastReadPos, pos-lastReadPos );
            return dest;
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
                JSONObject res = f.translate( markup, data2 );
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
