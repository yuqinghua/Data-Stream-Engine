# Data-Stream-Engine
Data Stream Engine is created for data collection and publishing. It design for Java developer who can develop easily based on it. Developers can write their own Implementation of Input \ Output \ Event for their business. 


[config]
mryu.tunnel1.input=Input-Java-Tail
mryu.tunnel1.events=['Event-Consume','Event-TextLine']
mryu.tunnel1.output=Output-Console-Log
mryu.tunnel1.size=20000
#default 20000
mryu.tunnel2.input=Input-Java-Tail
mryu.tunnel2.events=['Event-Consume']
mryu.tunnel2.output=Output-Console-Log
mryu.tunnel2.size=30000

#input=com.mryu.flune.input.JavaTailInput
#output=com.mryu.flune.output.ConsoleOutput
#logPathPattern=[
#				'C:\\Users\\Lucas\\Desktop\\mm.log',
#				'C:\\Users\\Lucas\\Desktop\\mm\\*.log'
#			]
#posPath=C:/Users/Lucas/Desktop/tail.pos			
##readMode:FROM_BEGIN  APPEND_ONLY
#readMode=APPEND_ONLY
#


[Input-Java-Tail]
class=com.mryu.flune.input.JavaTailInput
logPathPattern=[
				'C:\\Users\\Lucas\\Desktop\\mm.log',
				'C:\\Users\\Lucas\\Desktop\\mm\\*.log'
			]
posPath=C:/Users/Lucas/Desktop/tail.pos			
#readMode:FROM_BEGIN  APPEND_ONLY
readMode=APPEND_ONLY


[Output-Console-Log]
class=com.mryu.flune.output.ConsoleOutput
workers=10

# Configure File
[Event-Consume]
class=com.mryu.flune.event.RegularExpressionEvent
regexp=.*action:3,player_id:([0-9]+),time:(?<time>[0-9]+),serverId:([0-9]+),device_id:(.+),consume_diamond:([-]{0,1}\d+),now_diamond:(\d+),way:(\d+)
fileds=[
		{"name":"player_id", "col":1, "type":"integer"},
		{"name":"time", "col":2, "type":"long"},
		{"name":"serverId", "col":3, "type" :"integer"},
		{"name":"device_id","col":4, "type" :"string"},
		{"name":"consume_diamond","col":5,"type"  :"integer"},
		{"name":"now_diamond","col":6,"type"  :"integer"},
		{"name":"way","col":7,"type" :"integer"}
		]
	
fileds_add=[
		{"name":"create_time","type":"string","value":"${sysdate}"},			#yestoday sysdate, hostname, ip
		{"name":"server","type" :"string","value":"$path{1}"},
		{"name":"server_2","type":"string","value":"$group{1}"}
		]

[Event-TextLine]
class=com.mryu.flune.event.TextLineEvent
fileds=[{"name":"text", "col":1, "type":"string"}]
