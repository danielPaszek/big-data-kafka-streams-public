## cluster creation
gcloud dataproc clusters create ${CLUSTER_NAME} \
--enable-component-gateway --region ${REGION} --subnet default \
--master-machine-type n1-standard-2 --master-boot-disk-size 50 \
--num-workers 2 --worker-machine-type n1-standard-2 --worker-boot-disk-size 50 \
--image-version 2.1-debian11 --optional-components DOCKER,ZEPPELIN,ZOOKEEPER \
--project ${PROJECT_ID} --max-age=3h \
--metadata "run-on-master=true" \
--initialization-actions \
gs://goog-dataproc-initialization-actions-${REGION}/kafka/kafka.sh

## Terminal 1
Zmień init.sh na odpowiedni bucket!
```
CLUSTER_NAME=$(/usr/share/google/get_metadata_value attributes/dataproc-cluster-name)
chmod +x init.sh
chmod +x data_producers.sh
./init.sh
```
`./data_producers.sh`

## Terminal 2
Run ```
CLUSTER_NAME=$(/usr/share/google/get_metadata_value attributes/dataproc-cluster-name)
java -cp /usr/lib/kafka/libs/*:test.jar \
com.example.bigdata.ApacheLogToAlertRequests ${CLUSTER_NAME}-w-0:9092 30 100 4```
30 - D
100 - L
4 - O

## Terminal 3
Pobierz baze
```
CLUSTER_NAME=$(/usr/share/google/get_metadata_value attributes/dataproc-cluster-name)
mkdir /tmp/datadir
docker run --name mymysql -v /tmp/datadir:/var/lib/mysql -p 6033:3306 \
 -e MYSQL_ROOT_PASSWORD=my-secret-pw -d mysql:debian
```
`docker exec -it mymysql bash`

`mysql -uroot -pmy-secret-pw`
```
CREATE USER 'streamuser'@'%' IDENTIFIED BY 'stream';
CREATE DATABASE IF NOT EXISTS streamdb CHARACTER SET utf8;
GRANT ALL ON streamdb.* TO 'streamuser'@'%';
exit;
```
Zaloguje się:
`mysql -u streamuser -p streamdb`

`stream` - hasło

```
create table etl_sink ( id int primary key,
 title varchar(512), rating_count int,
 rating_sum int);
 ```
```
create table anomaly_sink ( id int primary key,
 title varchar(512), rating_count int,
 rating_avg double, start_time varchar(100), end_time varchar(100));
 ```
By odczytać etl: `select * from etl_sink;`
By odczytać anomalie: `select * from anomaly_sink;`
## Terminal 4
connect-standalone
`CLUSTER_NAME=$(/usr/share/google/get_metadata_value attributes/dataproc-cluster-name)`

`wget https://repo1.maven.org/maven2/com/mysql/mysql-connector-j/\
8.0.33/mysql-connector-j-8.0.33.jar`

`sudo cp mysql-connector-j-8.0.33.jar /usr/lib/kafka/libs`

`wget https://packages.confluent.io/maven/io/confluent/kafka-connect-jdbc/\
10.7.0/kafka-connect-jdbc-10.7.0.jar`

`sudo mkdir /usr/lib/kafka/plugin`

`sudo cp kafka-connect-jdbc-10.7.0.jar /usr/lib/kafka/plugin`

`sudo cp /usr/lib/kafka/config/tools-log4j.properties \
/usr/lib/kafka/config/connect-log4j.properties`

`echo "log4j.logger.org.reflections=ERROR" | \
sudo tee -a /usr/lib/kafka/config/connect-log4j.properties`

Do nasłuchiwania etl
`/usr/lib/kafka/bin/connect-standalone.sh connect-standalone.properties \
connect-jdbc-sink.properties`
Do nasłuchiwania anomali
`/usr/lib/kafka/bin/connect-standalone.sh connect-standalone.properties \
connect-jdbc-sink-anomaly.properties`