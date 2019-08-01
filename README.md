**Kafka Summit 2019 demo**

Download the last version of Confluent CLI at https://www.confluent.io/download/. Install it and then move to the installation directory. 

Create the necessary topics

      bin/kafka-topics --create --topic app-state --partitions 3 --replication-factor 1 --zookeeper localhost:2181
      bin/kafka-topics --create --topic app-events --partitions 3 --replication-factor 1 --zookeeper localhost:2181

Now you can compile the project

      mvn clean package
             
Start two instances in a separate terminal window

      java -Xmx340m -Dserver.port=4041 -Dtransaction.id=1111 -Dstate.dir=/tmp/kafka-streams-1 -jar target/kstream-app-1.0.0-SNAPSHOT.jar
      java -Xmx340m -Dserver.port=4042 -Dtransaction.id=2222 -Dstate.dir=/tmp/kafka-streams-2 -jar target/kstream-app-1.0.0-SNAPSHOT.jar
      
API (simplified)
            
      curl -X POST http://localhost:4040/app/[tenant_id]/[user_id]
           
      curl -X POST \
          http://localhost:4040/app/[tenant_id]/[user_id]/widgets \
          -H 'Content-Type: application/json' \
          -H 'cache-control: no-cache' \
          -d '{
	         "widgetId" : "user2-1",
	         "version" : 0,
	         "meta" : {
		         "meta2" : "value2"	
	         },
	         "data" : {
		         "c" : "dddd"
	         }
          }'

      curl -X GET http://localhost:4040/app/[tenant_id]/[user_id]
      
      
To delete the topics

      bin/kafka-topics --delete --topic app-state --zookeeper localhost:2181
      bin/kafka-topics --delete --topic app-events --zookeeper localhost:2181
      
State store available in /tmp/kafka-streams-1 e /tmp/kafka-streams-2