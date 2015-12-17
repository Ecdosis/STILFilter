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
package mml.filters;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

/**
 * The Filter Interface
 * @author desmond
 */
public abstract class Filter {
/** hold the modified text here */
    protected StringBuilder sb;
    /** quick lookup table of tags to properties */
    protected String table;
    /** the ranges of the destination STIL document */
    protected ArrayList<JSONObject> destRanges;
    /** the quick lookup map */
    protected HashMap<String,ArrayList> map;
    protected abstract String getPageRef( JSONArray annotations );
    /**
     * Initialise a Filter object
     */
    public Filter()
    {
        this.sb = new StringBuilder();
        this.destRanges = new ArrayList<JSONObject>();   
    }
    /**
     * Initialise the map (only callable if table has been set)
     */
    protected void initMap()
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
    /**
     * Clean a range in situ
     * @param range the range to clean
     */
    private void cleanRange( JSONObject range )
    {
        JSONArray annotations = (JSONArray) range.get("annotations");
        if ( annotations != null )
        {
            int removal = -1;
            for ( int i=0;i<annotations.size();i++)
            {
                JSONObject ann = (JSONObject)annotations.get(i);
                if ( ann.containsKey("_done")  )
                {
                    removal = i;
                    break;
                }
            }
            if ( removal != -1 )
                annotations.remove(removal);
            if ( annotations.size()==0 )
                range.remove("annotations");
        }
    }
    /**
     * Translate raw STIL from XML into STIL for the Harpur MML format
     * @param stil the raw XML STIL (JSON) with CHARACTER offsets
     * @param text the text it points to as a String
     * @return the new STIL standoff markup for the new text
     */
    public JSONObject translate( JSONObject stil, String text ) throws Exception
    {
        JSONObject dest = new JSONObject();
        Stack<JSONObject> stack = new Stack<JSONObject>();
        dest.put( "style", stil.get("style") );
        JSONArray ranges = (JSONArray) stil.get("ranges");
        dest.put("ranges",destRanges);
        int pos = 0;
        int nextOff = 0;
        int lastReadPos = 0;
//        if ( !verifyCorCode(stil.toJSONString(),text) )
//            System.out.println("corcode is invalid BEFORE STILFilter conversion");
        int count = 0;
        for ( Object r : ranges )
        {
            JSONObject range = (JSONObject)r;
            cleanRange(range);
            count++;
            if ( count % 100 ==0 )
                System.out.println("count="+count);
            pos += ((Number)range.get("reloff")).intValue();
            // maintain a stack of currently overlapping ranges
            while ( !stack.isEmpty() 
                && ((Number)stack.peek().get("tagEnd")).intValue()<=pos )
            {
                stack.pop();
            }
            if ( ((Number)range.get("len")).intValue() > 0 )
            {
                range.put("tagEnd",((Number)range.get("len")).intValue()+pos);
                stack.push(range);
            }
            // now look at the range name and decide what to do...
            String rName = (String)range.get("name");
            if ( map.containsKey(rName) )
            {
                ArrayList list = (ArrayList) map.get(rName);
                if ( !range.containsKey("annotations") )
                {
                    if ( listHasBareTag(list) )
                    {
                        transferRange( range, 
                            (String)getBareEntry(list).get("to"), nextOff );
                    }
                }
                else
                {
                    boolean matched = false;
                    for ( int i=0;i<list.size();i++ )
                    {
                        JSONObject entry = (JSONObject)list.get(i);
                        JSONObject from = (JSONObject)entry.get("from");
                        if ( from.containsKey("key") )
                        {
                            String attrKey = (String)from.get("key");
                            String attrValue = (String)from.get("value");
                            if ( rangeHasAnnotation(range,attrKey,attrValue) )
                            {
                                transferRange( range, (String)entry.get("to"), 
                                    nextOff );
                                matched = true;
                                break;
                            }
                        }
                    }
                    // in case attribute was wrong
                    if ( !matched )
                    {
                        if ( listHasBareTag(list) )
                        {
                            transferRange( range, 
                                (String)getBareEntry(list).get("to"), nextOff );
                            nextOff = 0;
                        }
                        else
                            System.out.println("Couldn't match "+rName);
                    }
                }
                nextOff = 0;
            }
            else if ( rName.equals("pb") )
            {
                JSONObject newRange = new JSONObject();
                newRange.put("name","page");
                JSONArray annotations = (JSONArray) range.get("annotations");
                String ref = getPageRef(annotations);
                // ensure preceding text is written out
                appendToText( text, lastReadPos, pos-lastReadPos );
                lastReadPos = pos;
                // ensure that ref is on a line by itself
                if ( sb.length()>0 && sb.charAt(sb.length()-1) != '\n' )
                    ref = "\n"+ref;
                // pb could be at end of text
                if ( pos < text.length() && text.charAt(pos) != '\n' )
                    ref += "\n";
                // insert the pageref into the text and adjust ranges
                if ( destRanges.size()>0 )
                {
                    newRange.put("len",ref.length());
                    int pgRelOff = ((Number)range.get("reloff")).intValue();
                    JSONObject lastRange = null;
                    if ( pgRelOff == 0 )
                    {
                        // if our reloff is 0 we can just swap 
                        // ourself with the preceding range
                        lastRange = destRanges.get(destRanges.size()-1);
                        int lrRelOff = ((Number)lastRange.get("reloff")).intValue();
                        newRange.put("reloff",lrRelOff);
                        lastRange.put("reloff",ref.length());
                        destRanges.add(destRanges.size()-1,newRange);
                    }
                    else
                    {
                        // we're in the middle of some ranges
                        // increase offset of next range
                        nextOff = ref.length();
                        //int oldRelOff = ((Number)range.get("reloff")).intValue();
                        newRange.put("reloff",pgRelOff);
                        destRanges.add(newRange);
                    }
                    // lengthen overlapping ranges on stack
                    for ( JSONObject r2 : stack )
                    {
                        // the stack is a stack of OLD ranges
                        // get the new range built from r2 and update THAT
                        JSONObject nr = (JSONObject)r2.get("new");
                        if ( nr != null && nr != lastRange )
                        {
                            int oldLen = ((Number)nr.get("len")).intValue();
                            nr.put("len",oldLen+ref.length());
                        }
                    }
                }
                else
                {
                    // we're the first, so adjust the 2nd range only
                    destRanges.add( newRange );
                    nextOff = ref.length();
                }
                sb.append(ref);
            }
            else
                System.out.println("Unknown property "+rName+" ignored");
        }
        if ( lastReadPos < text.length() )
            appendToText( text, lastReadPos, text.length()-lastReadPos );
//        if ( !verifyCorCode(dest.toJSONString(),sb.toString()) )
//            System.out.println("corcode is invalid AFTER STILFilter conversion");
        return dest;
    }
    /**
     * Does the range have the specified annotation?
     * @param range the old range in question
     * @param attrKey the attribute key
     * @param attrValue the attribute value - both must match
     * @return true if both attribute name and value were found
     */
    protected boolean rangeHasAnnotation( JSONObject range, String attrKey, 
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
     * @return true if there is at least one definition with no attributes
     */
    protected boolean listHasBareTag( ArrayList<JSONObject> list )
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
     * Append some data from the source text to the modified text
     * @param text the original text 
     * @param from the offset in text to copy from
     * @param len the length of the copied text in characters
     */
    protected void appendToText(String text, int from, int len)
    {
        sb.append(text.substring(from,from+len));
    }
    /**
     * Get the entry without attributes
     * @param list the list to get it from
     * @return the entry or null if not found
     */
    protected JSONObject getBareEntry( ArrayList<JSONObject> list )
    {
        for ( int i=0;i<list.size();i++ )
        {
            JSONObject jObj = list.get(i);
            JSONObject from = (JSONObject) jObj.get("from");
            if ( !from.containsKey("key") )
                return jObj;
        }
        return null;
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
     * Transfer an ordinary range from the old to the new STIL document
     * @param range the old range
     * @param newName the new name of the range
     * @param adjust additional amount to offset reloff by
     */
    protected void transferRange( JSONObject range, String newName, int adjust )
    {
        JSONObject newRange = new JSONObject();
        int loc = ((Number)range.get("reloff")).intValue();
        // stuff a copy of the new range in the old range
        range.put("new",newRange);
        newRange.put( "name", newName );
        newRange.put( "reloff", loc+adjust);
        newRange.put("len", range.get("len") );
        // NB we drop any attributes: not needed in MML
        destRanges.add( newRange );
    }
    protected boolean verifyCorCode(String stil, String text )
    {
        JSONObject jObj = (JSONObject)JSONValue.parse(stil);
        JSONArray ranges = (JSONArray)jObj.get("ranges");
        int offset = 0;
        for ( int i=0;i<ranges.size();i++ )
        {
            JSONObject range = (JSONObject)ranges.get(i);
            offset += ((Number)range.get("reloff")).intValue();
            int len = ((Number)range.get("len")).intValue();
            if ( offset+len > text.length() )
            {
                System.out.println(" offset+len="+(offset+len)
                    +" text.length()="+text.length());
                return false;
            }
        }
        return true;
    }
}
