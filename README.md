# Data-Stream-Engine
Data Stream Engine is created for data collection and publishing. It design for Java developer who can develop easily based on it. Developers can write their own Implementation of Input \ Output \ Event for their business. 


#In version 0.0.1
1、Input
the author create an class "JavaTailInput.java" implements of AbstractInput, this class is based on Java NIO2 to read text line from files which can be special as an regular Expression. And many files are monitored by this input(absolutely , I test 10 thousands of text file, it read 40 thousands line from an file per seconds and it's benifit of NIO2). the message of read while post to events to filter and process it;
2、Event
RegularExpressEvent is implements of AbstractEvent. it can resolve textline and create an json object which is past to Ouput Instance.
3、Output
ConsoleOutput is an implementation of AbstractOutput. just log the result of json. 

#NOTIFICATION
you can write your own output input and event yourself.
