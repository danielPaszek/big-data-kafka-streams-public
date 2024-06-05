CLUSTER_NAME=$(/usr/share/google/get_metadata_value attributes/dataproc-cluster-name)
kafka-topics.sh --bootstrap-server ${CLUSTER_NAME}-w-0:9092 --create \
 --replication-factor 2 --partitions 3 --topic netflix-in

kafka-topics.sh --bootstrap-server ${CLUSTER_NAME}-w-0:9092 --create \
--replication-factor 2 --partitions 3 --topic etl-out

kafka-topics.sh --bootstrap-server ${CLUSTER_NAME}-w-0:9092 --create \
--replication-factor 2 --partitions 3 --topic anomaly-out

kafka-topics.sh --bootstrap-server ${CLUSTER_NAME}-w-0:9092 --create \
--replication-factor 2 --partitions 3 --topic movies-in

mkdir ./data
hadoop fs -copyToLocal gs://big-data-24/netflix-prize-data/part-00.csv ./data/
mkdir ./data2
hadoop fs -copyToLocal gs://big-data-24/movie_titles.csv ./data2/