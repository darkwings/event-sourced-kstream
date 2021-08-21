## Kafka Summit 2019 demo

This is the companion code of my talk at Kafka Summit London 2019, called "The source of truth... twice", updated to
- Most recent versions of Kafka/Confluent libraries
- Commands as messages published on a dedicated topic.

The command si published on the topic app-commands. A stream processor performs a lookup on the state table (fed by the app-state topic) to check the current state and generating the corresponding event. The event is published then on app-events topic and the updated state is published on app-state topic, updating the state of the aggregate.

### Installation

Download the last version of the Confluent CLI at https://www.confluent.io/download/. 
Install it and then move to the installation directory. 

Create the topics

      bin/kafka-topics --create --topic app-state --partitions 3 --replication-factor 1 --zookeeper localhost:2181
      bin/kafka-topics --create --topic app-events --partitions 3 --replication-factor 1 --zookeeper localhost:2181
      bin/kafka-topics --create --topic app-commands --partitions 3 --replication-factor 1 --zookeeper localhost:2181
      bin/kafka-topics --create --topic app-command-failures --partitions 3 --replication-factor 1 --zookeeper localhost:2181

Compile the project

      mvn clean package
             
Start two instances in a separate terminal window

      java -Dserver.port=4041 -Dtransaction.id=1111 -Dstate.dir=/tmp/kafka-streams-1 -jar target/kstream-app-2.0.0.jar
      java -Dserver.port=4042 -Dtransaction.id=2222 -Dstate.dir=/tmp/kafka-streams-2 -jar target/kstream-app-2.0.0.jar
      
### API (simplified)
            
#### Create an app
            
      curl -X POST http://localhost:4041/app/[tenant_id]/[user_id]

As an example

      curl -X POST http://localhost:4041/app/app00/user1

#### Add a widget to an app
           
Let's use app00, owned by user1

      curl -X POST \
          http://localhost:4041/app/app00/user1/widgets \
          -H 'Content-Type: application/json' \
          -d '{
	         "widgetId" : "user1-1",
	         "version" : 0,
	         "meta" : {
		         "meta2" : "value2"	
	         },
	         "data" : {
		         "c" : "dddd"
	         }
          }'

      curl -X POST \
          http://localhost:4041/app/app00/user1/widgets \
          -H 'Content-Type: application/json' \
          -d '{
	         "widgetId" : "user1-3",
	         "version" : 1,
	         "meta" : {
		         "meta2" : "value2-a"	
	         },
	         "data" : {
		         "c" : "eeee"
	         }
          }'

#### Retrieve an app

      curl -X GET http://localhost:4041/app/[tenant_id]/[user_id]
      
or (should be indifferent because of interactive query support)

      curl -X GET http://localhost:4042/app/[tenant_id]/[user_id]

The APIs above returns also the current version. This is the number you should use in a command call

#### Delete an app

You should pass the right version

      curl -X DELETE http://localhost:4041/app/[tenant_id]/[user_id]/[version]

As an example

      curl -X DELETE http://localhost:4041/app/app00/user1/1

Now if you try to perform a GET, you should obtain an HTTP 404

### State stores
      
State stores are available in /tmp/kafka-streams-1 e /tmp/kafka-streams-2
