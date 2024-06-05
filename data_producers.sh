CLUSTER_NAME=$(/usr/share/google/get_metadata_value attributes/dataproc-cluster-name)
java -cp /usr/lib/kafka/libs/*:KafkaProducer.jar \
com.example.bigdata.TestProducer data2 15 movies-in \
1 ${CLUSTER_NAME}-w-0:9092

java -cp /usr/lib/kafka/libs/*:KafkaProducer.jar \
com.example.bigdata.TestProducer data 15 netflix-in \
1 ${CLUSTER_NAME}-w-0:9092