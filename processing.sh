CLUSTER_NAME=$(/usr/share/google/get_metadata_value attributes/dataproc-cluster-name)
java -cp /usr/lib/kafka/libs/*:test.jar \
 com.example.bigdata.ApacheLogToAlertRequests ${CLUSTER_NAME}-w-0:9092