RATIONALE
---------

When importing XML files into the ecdosis system the XML is first split 
into layers, so removing tags like add, del, abbrev, expan etc., that 
is, all those that cause the base text to split into alternatives. And 
those alternatives are placed into separate layers of the one document. 
The layers are treated as sub-versions of the main versions of a work. 
This leaves, however, much markup such as \<hi rend="it"\>...\</hi\>, which 
means "put this in italics". Such elements are split into base text and 
standoff markup, called STIL (standard interval markup). This defines a 
property called "hi" for the text with a range in the base text, and an 
"annotation" with a key-value pair {"key":"rend","value":"it"} (in 
JSON). Since the objective is to be able to edit the text plus its 
markup using the simplified MML (minimal markup languages) editor (see 
MML project), it is necessary to convert the raw XML property names and 
annotations into simple property names. So \<hi rend="it"\> becomes 
"italics". This can then be attached to an MML markup tag such as 
"\*" and we can produce the "italics" property in the editor by 
typing \*word\* to make "word" italics. This greatly 
simplies transcription. No more angle-brackets!!

However, to make this work, a filter needs to be defined, one per 
project at least, which will be defined with the package name 
mml.filters.{docid}, where {docid} is normally the docid of the entire 
project, such as "english/harpur". So the full path name for the Harpur 
edition filter will be mml.filters.english.harpur.Filter. Now when the 
user asks the MML service for the version of a document recently 
uploaded from XML, e.g. with the docid: english/harpur/h642/h642j, the 
merged corcode for english/harpur/h642 will be fetched from the Mongo 
database. If the BSON document contains the key "converted": true then 
the relevant version (h642j) will be retrieved from the corcode. But if 
not, the MML service will extract all versions from the corcode and then 
attempt to instantiate an instance of the class 
mml.filters.english.harpur.Filter. If this succeeds (and the filters can 
be stored in a separate jar, simply added to the /lib folder of the 
webapp) then the filter will be created (call to Filter()), and its 
translate method called on each STIL document in the original corcode. 
The result will then be repacked as a new corcode, which will replace 
the old one, and the "convert":true key will be set in the BSON file. 
Meanwhile, the converted STIL document can be passed onto the MML 
service for conversion to the MML language defined by the dialect 
specified in the dialects collection with the docid: "english/harpur".

EXTENSIONS
----------

At the moment the STILFilter library only contains a translator for the 
Harpur edition. To add new formats define a class that implements 
mml.filters.Filter interface. Basically this just takes a JSONObject 
STIL document and spits back the revised copy. Also the text can be 
optionally modified. But be sure that the encoding of the XML is 
absolutely consistent, otherwise the filter will not work reliably. One 
filter per project should suffice, but the mechanism in the MML service 
will recognise specific filters for specific documents, falling back to 
the project-wide one if that is not available. That is, it will look 
first for a filter called mml.filters.english.harpur.h642.Filter, and 
failing that, mml.filters.english.harpur.Filter. Doing it this way is 
easy, even if it is a bit specific to projects. But it provides 
excellent customisability. Simple tags can be converted via a table, and 
more complex ones like pg (page-break) can be handled separately.

Note that in STIL offsets are *relative* to the *preceding* property 
start OR the start of the file (whichever comes first going backwards). 
Also offsets are in bytes NOT characters (so beware of UTF-8 encoding!). 
Unless you are modifying the source text you don't need to change these.

USING
-----

Just drop the STILFilter.jar file into your installation of the MML 
service lib folder and restart the service via Tomcat. If you named your 
project correctly the appropriate filter will then be loaded when needed.

A copy of JSCompactor has been included to turn a JSON file containing 
the table.json document for Harpur (but you would need to define your 
own if you used this technique in your filter) into a String suitable 
for inclusion into a Java program. Invoke it thus:

java JSCompactor.jar table.json

Copy the result from the terminal. it screws up the end a bit but it is 
usable.

SAMPLE FILES
------------

The files beginning with A89#h642d* are example files from the Harpur 
project. They contain a source XML file, and the STIL and plain text 
split via the splitter program in the standoff repository on 
AustESE-Infrastructure. They are just examples, and contain \<add\> and 
\<del\> codes etc because they have not bee passed first through the 
splitter in calliope, which is the core import facility for ecdosis. But 
it works as an example.
