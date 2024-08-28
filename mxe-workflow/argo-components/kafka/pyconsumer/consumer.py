#!/usr/bin/env python3
import os
from datetime import datetime
import uuid
import time
import json
import random
from confluent_kafka import Consumer, TopicPartition
import threading
from multiprocessing import Process, Manager
import time
from queue import Queue
from collections import defaultdict
import uuid
from kazoo.client import KazooClient
import boto3
from botocore.client import Config
from botocore.exceptions import ClientError
import logging
import sys

klogger = logging.getLogger("mee_kafka_consumer")
klogger.setLevel(logging.WARN)

handler = logging.StreamHandler(sys.stdout)
handler.setLevel(logging.DEBUG)
formatter = logging.Formatter('%(asctime)s - PID:%(process)d - %(message)s')
handler.setFormatter(formatter)
klogger.addHandler(handler)

debuglogger = lambda *x: klogger.debug(" ".join([str(e) for e in x]))
warnlogger = lambda *x: klogger.warn(" ".join([str(e) for e in x]))
errorlogger = lambda *x: klogger.error(" ".join([str(e) for e in x]))

def get_env(key, default_value=None):
   value =  os.getenv(key, default_value)
   if value is not None:
      return value
   else:
     raise ValueError("Env Var: "+str(key)+" is not set")

"""
kafka_seed_brokers =  get_env("KAFKA_SEED_BROKER")
k_topic =  get_env("KAFKA_TOPIC")
consumer_group_id =  get_env("KAFKA_CONSUMER_GROUP")
zookeeper_ep = None #get_env("ZOOKEEPER_CONNECT", None)
minio_service_ep =  get_env("MINIO_EP")
minio_bucket_name =  get_env("MINIO_BUCKET")
minio_access_key=  get_env("MINIO_ACCESS_KEY")
minio_secret_key=  get_env("MINIO_SECRET_KEY")

config = {
    'bootstrap.servers': kafka_seed_brokers,
    'k_topic': k_topic,
    'group.id': consumer_group_id,
    'enable.auto.commit': False,
    'enable.auto.offset.store': False,
    'auto.offset.reset': 'earliest',
    'k_local_data_path': '/home/mxe/data',
    "k_fileprefix":  get_env("FILE_PREFIX", 'batch'),
    "k_max_batch_size":  int(get_env("MAX_BATCH_SIZE", '200')),
    "k_number_of_workers": int(get_env("NO_OF_BATCHES", '2')),
    "k_max_file_age": float(get_env("MAX_FILE_AGE_MINUTES", '0.7')),
    "k_minio_service_ep": minio_service_ep,
    "k_minio_bucket_name": minio_bucket_name,
    "k_minio_access_key": "xxxxxxxx" if minio_access_key else None,
    "k_minio_secret_key": "xxxxxxxx" if minio_secret_key else None,
    "k_kafka_consume_timeout_seconds": int(get_env("KAFKA_TIMEOUT", 10))
}
"""

def load_config_file(config_file=get_env("CONFIG_FILE_PATH", "/consumer/config.json")):
    with open(config_file) as conf:
        config = json.load(conf)
        config["minio_config"]["accesskey"] = "xxxxxxxx" if get_env("MINIO_ACCESS_KEY") else None
        config["minio_config"]["secretkey"] = "xxxxxxxx" if get_env("MINIO_SECRET_KEY") else None
    return config

def get_property(key, config=load_config_file(), config_section="consumer_config", default_value=None):
    if key in config[config_section]:
        return config[config_section][key]
    else:
        if default_value is None:
            raise ValueError(f"{key} is not set in {config_section}")
        else:
            debuglogger("config:", key, "is not set in", config_section)
            return default_value

class KMessage(object):
    """Message object holding payload and metadata received from kafka

    Args:
        payload (bytes): payload data bytes
        offset (int): message offset number from kafka
        partition (string): partition of kafka from which message was consumed
        topic (string): kafka topic from which message was consumed
        data_fromat (string): format of payload default value: json
    """    
    def __init__(self, payload, offset, partition, topic, data_format="json"):
        self.payload = payload
        self.offset = offset
        self.partition = partition
        self.topic = topic
        self.data_format = "json"
        self.unique_identifier = "_".join([str(self.partition), str(self.offset)])
        debuglogger("received Offset", self.offset, "partition:",
              self.partition, "topic:", self.topic)

    def getMsg(self):
        if self.data_format == "json":
            msg = self.payload.decode('utf-8')
            msg = json.dumps(json.loads(msg), separators=(',', ':'))
        else:
            msg = self.payload
        return msg

    def getOffSet(self):
        return self.offset

    def printMsg(self):
        debuglogger('Received message: {}'.format(self.getMsg().decode('utf-8')))

    def getUniqueIdentifier(self):
        return self.unique_identifier


class FileHandler(object):
    """Handler objects that manages the file pointers

    Args:
        topic (string): kafka topic of messages written to the fil
        partition (string): kafka parition of messages written to the fil
        folder_path (string): absoute path (temp local storage) to save files
        remote_folder (string): folder path to save under in remote object storage
        file_ext (string): file extension (*.json) to saved with
    """    

    def __init__(self, topic, folder_path, remote_folder, file_prefix, file_ext):
        self.topic = topic
        #self.partition = partition
        self.local_file_name = "_".join([file_prefix])
        self.path = os.path.join(
            folder_path, self.local_file_name)
        if remote_folder:
            self.destined_file_name = os.path.join(remote_folder, "_".join([file_prefix]))
        else:
            self.destined_file_name = os.path.join("_".join([file_prefix]))
        if file_ext and len(file_ext):
            self.path += "."+file_ext
            self.destined_file_name += "."+file_ext
        self.file_handler = open(self.path, "a")
        self.line_count = 0

    def write_msg(self, msg):
        self.file_handler.write(msg.getMsg() + "\n")
        self.line_count += 1
        return self.line_count

    def close(self):
        self.file_handler.close()

    def delete(self):
        self.close()
        os.remove(self.path)
        debuglogger("purged local files", self.path)

    def getAgeInMinutes(self):
        st = (time.time()- os.stat(self.path).st_mtime)
        #currlogger("AgeinMinutes:", (st/60))
        return st/60

class KConsumer(object):
    """Consumer to poll kafka, consume and persists in data in local and remote

    Args:
        kafka_config_dict (dict): map of config key value for kafk consumer
    """    

    def __init__(self, kafka_config_dict):
        self.config = kafka_config_dict
        self.config['kafka_config']['enable.auto.commit'] = False
        self.config['kafka_config']['enable.auto.offset.store'] = False
        self.config['kafka_config']['auto.offset.reset'] = 'earliest'
        self.topic = get_property("topic", self.config)
        self.consumer = Consumer(self.config['kafka_config'])
        self.consumer_object_id = str(uuid.uuid4())
        self.record_queue = Queue(maxsize=0)
        self.files = defaultdict(lambda: None, {})
        self.file_handler = None
        self.topic_partitions = self.refresh_topic_metadata()
        self.offset_marker = defaultdict(lambda: 0, {})
        self.last_seen_offset = defaultdict(lambda: None, {})
        self.message_counter = 0
        if "zookeeper_endpoint" in self.config["consumer_config"]:
            self.zk_client = KazooClient(hosts=self.config["consumer_config"]["zookeeper_endpoint"])
            self.zk_client.start()
            self.zk_path_stop_marker_key = None
            self.intialise_zookeeper_markers()
        debuglogger("Initiliased KConsumer", self.consumer_object_id, "ConsumerGroup:", get_property("group.id", self.config, "kafka_config"))

    def refresh_topic_metadata(self):
        all_topics_metadata = dict([(e[1].topic, len(e[1].partitions))
                                    for e in self.consumer.list_topics(timeout=4).topics.items()])
        debuglogger("all topics metadata:", json.dumps(
            all_topics_metadata, indent=1))
        if self.topic not in all_topics_metadata:
            errorlogger("cannot find topic in kafka cluster")
            raise ValueError("cannot find topic in kafka cluster")
        else:
            warnlogger("current topic partitions:", json.dumps(all_topics_metadata[self.topic], indent=1))
        return all_topics_metadata[self.topic]

    def intialise_zookeeper_markers(self):
        zk_path = "/"+os.path.join("mxe_kafka_consumer", get_property("group.id", self.config, "kafka_config"), self.topic)
        for each_partition in range(self.topic_partitions):
            zk_path_stop_marker_key = os.path.join(zk_path, str(each_partition), "stop")
            debuglogger("zk_path_stop_marker_key:", zk_path_stop_marker_key)
            if not self.zk_client.exists(zk_path_stop_marker_key):
                self.zk_client.ensure_path(zk_path_stop_marker_key)
                self.zk_client.set(zk_path_stop_marker_key, b"0")
            else:
                self.offset_marker[str(each_partition)] = int(self.zk_client.get(zk_path_stop_marker_key)[0].decode("utf-8"))
        self.zk_path_stop_marker_key = zk_path

    def subcribe(self):
        self.consumer.subscribe([self.topic])

    def assign_partitions(self):
        partition = random.randrange(0, self.topic_partitions)
        tp_ref = TopicPartition(self.topic, partition, self.offset_marker[str(partition)]+1)
        self.consumer.assign([tp_ref])
        tp_ref = self.consumer.assignment()
        debuglogger("assigned to "+self.topic,
              "to partition", self.consumer.assignment())
        debuglogger("Seeking to ", tp_ref)
        if not self.consumer.assignment():
            raise AssertionError("Kafka partition assignment failed")

    def consume_by_poll(self):
        warnlogger("started consuming loop", "PID:", os.getpid(), [e.partition for e in self.consumer.assignment()])
        #self.assign_partitions()
        start_time = time.time()
        while True:
            msg = self.consumer.poll(1.0)
            debuglogger("PID:", os.getpid(), "polling to "+self.topic,
                  "got hold of partitions", [e.partition for e in self.consumer.assignment()])
            if int((time.time() - start_time) % 5) and self.check_partition_success_condition():
                for e in self.files:
                    if self.files[e]:
                        warnlogger("closing", self.files[e].path)
                        self.files[e].close()
                warnlogger("exiting polling loop after success condition")
                self.uploader()
                break
            if msg is None:
                continue
            if msg.error():
                errorlogger("Consumer error: {}".format(msg.error()))
                continue
            k_msg = KMessage(msg.value(), msg.offset(), msg.partition(), msg.topic())
            if k_msg.getUniqueIdentifier() not in self.last_seen_offset:
                self.last_seen_offset[k_msg.getUniqueIdentifier()] = k_msg.getOffSet()
            else:
                warnlogger("Detected possible retendant message", k_msg.getUniqueIdentifier(), "Discarding it.")
                continue
            self.record_queue.put(k_msg)
            filewriting_threader = threading.Thread(target=self.persist)
            filewriting_threader.start()

    def consume_by_batch(self):
        warnlogger("started consuming loop", "PID:", os.getpid(), [e.partition for e in self.consumer.assignment()])
        msg_list = self.consumer.consume(get_property("max_batch_size", self.config),timeout=get_property("consume_timeout_seconds", self.config))
        warnlogger("Consuming", self.topic, "by BatchCall, got hold of partitions", [e.partition for e in self.consumer.assignment()])
        warnlogger("Received", len(msg_list), "messages from consume by batch")
        if not msg_list:
            errorlogger("No Messages found in topic", self.topic)
        for msg in msg_list:
            if msg is None:
                continue
            if msg.error():
                debuglogger("Consumer error: {}".format(msg.error()))
                continue
            k_msg = KMessage(msg.value(), msg.offset(), msg.partition(), msg.topic())
            if k_msg.getUniqueIdentifier() not in self.last_seen_offset:
                self.last_seen_offset[k_msg.getUniqueIdentifier()] = k_msg.getOffSet()
            else:
                warnlogger("Detected possible retendant message", k_msg.getUniqueIdentifier(), "Discarding it.")
                continue
            #self.record_queue.put(k_msg)
            self.persist_msg(k_msg)
            #filewriting_threader = threading.Thread(target=self.persist)
            #filewriting_threader.start()
        warnlogger("Processed", len(msg_list), "at", self.consumer_object_id)
        for e in self.files:
            if self.files[e]:
                debuglogger("closing", self.files[e].path)
                self.files[e].close()
            warnlogger("closed file after persisting", len(msg_list))
            self.uploader()

        
    def commit_offsets_zookeeper(self, uploaded_file_handler):
        try:
            debuglogger("zoo commit", os.path.join(self.zk_path_stop_marker_key, str(uploaded_file_handler.partition), "stop"), self.offset_marker[str(uploaded_file_handler.partition)])
            self.zk_client.set(os.path.join(self.zk_path_stop_marker_key, str(uploaded_file_handler.partition), "stop"), 
                bytes(str(self.offset_marker[str(uploaded_file_handler.partition)]), "utf-8"))
            return True
        except Exception as e:
            errorlogger("zk commit error", e)
            return False

    def commit_offsets_native(self, uploaded_file_handler):
        try:
            #if int(uploaded_file_handler.partition) in [1]:
            #    return True
            
            #parition based files
            # tp_ref = TopicPartition(self.topic, uploaded_file_handler.partition, self.offset_marker[str(uploaded_file_handler.partition)]+1)
            # self.consumer.store_offsets(offsets=[tp_ref])
            # self.consumer.commit()

            #consumer based files
            tp_refs = [TopicPartition(self.topic, int(each_partition), self.offset_marker[str(each_partition)]+1) for each_partition in self.offset_marker]
            self.consumer.store_offsets(offsets=tp_refs)
            self.consumer.commit()
            warnlogger("commit_offsets_native:", tp_refs)
            return True
        except Exception as e:
            errorlogger("kafka commit error", e)
            return False

    def uploader(self):
        minio_service_ep = get_property("minio_endpoint", config=self.config, config_section="minio_config")
        minio_access_key= get_env("MINIO_ACCESS_KEY")
        minio_secret_key= get_env("MINIO_SECRET_KEY")
        minio_bucket_name= get_property("minio_bucket", config=self.config, config_section="minio_config")

        if get_property("mTLS_enabled", config=self.config, config_section="minio_config"):
            minio_http = "https://"
        else:
            minio_http = "http://"
        s3_client = boto3.resource('s3',
                    endpoint_url=minio_http+minio_service_ep if not minio_service_ep.startswith("http") else minio_service_ep,
                    aws_access_key_id=minio_access_key,
                    aws_secret_access_key=minio_secret_key,
                    config=Config(signature_version='s3v4'))
        for each_file_item in self.files.items():
            each_file = each_file_item[1]
            if not each_file.line_count > 0:
                continue
            remote_file_path = os.path.join(self.topic, each_file.destined_file_name)
            try:
                warnlogger("uploading from", each_file.path, "to", remote_file_path)
                s3_client.Bucket(minio_bucket_name).upload_file(each_file.path, remote_file_path)
                #commit_status = self.commit_offsets_zookeeper(each_file)
                commit_status = self.commit_offsets_native(each_file)
                if not commit_status:
                    response = s3_client.Bucket(minio_bucket_name).delete_objects(
                                    Delete={
                                        'Objects': [
                                            {
                                                'Key': remote_file_path
                                            },
                                        ],
                                        'Quiet': True
                                    }
                                )
                    errorlogger("Offset failed, deleting uploadded object:", remote_file_path)
                    debuglogger(response)
                else:
                    self.message_counter += each_file.line_count
                    warnlogger("deleteing local-file:", each_file.path)
                    each_file.delete()
            except ClientError as e:
                debuglogger(e)
                return False

    def get_file_handler(self, topic, partition):
        if not self.files[self.consumer_object_id]:
            self.files[self.consumer_object_id] = FileHandler(self.topic, get_property("local_data_path", config=self.config), None, "_".join(
                [get_property("file_prefix"), self.consumer_object_id]), "json")
            debuglogger(os.getpid(), "creating file:", self.files[self.consumer_object_id].path)
        return self.files[self.consumer_object_id]

    def check_partition_success_condition(self):
        batch_size_condition = False
        file_age_condition = False

        if self.config["k_max_batch_size"] == sum([e[1].line_count for e in self.files.items() if e[1] is not None]):
            batch_size_condition = True
        
        if any([True if e[1].getAgeInMinutes() >= self.config["k_max_file_age"] else False for e in self.files.items() if e[1] is not None]):
            file_age_condition = True
            debuglogger("FileAge", [e[1].getAgeInMinutes() for e in self.files.items()])

        if any([batch_size_condition, file_age_condition]):
            return True
        else:
            return False

    def persist(self):
        msg = self.record_queue.get(timeout=60)
        writer = self.get_file_handler(msg.topic, msg.partition)
        writer.write_msg(msg)
        self.offset_marker[str(msg.partition)] = msg.getOffSet()
        debuglogger("write-mark-offset", msg.partition, self.offset_marker[str(msg.partition)])

    def persist_msg(self, msg):
        writer = self.get_file_handler(msg.topic, msg.partition)
        writer.write_msg(msg)
        self.offset_marker[str(msg.partition)] = msg.getOffSet()
        debuglogger("write-mark-offset", msg.partition, self.offset_marker[str(msg.partition)])

    def start_consumer(self):
        self.subcribe()
        self.consume_by_poll()

    def close(self):
        self.consumer.close()
        self.zk_client.stop()

def consumer_starter(shared_dict):
    consumer = KConsumer(config)
    consumer.subcribe()
    consumer.consume_by_batch()
    shared_dict[consumer.consumer_object_id] = consumer.message_counter


def start_consumer_process_loop():
    consumer_workers = []

    warnlogger("Starting ConsumerRun ", "\nusing config parameters: ", json.dumps(config, indent=2))
    m_mgr = Manager()
    shared_dict = m_mgr.dict()
    while len(consumer_workers) < get_property("number_of_workers"):
        num_alive = len([w for w in consumer_workers if w.is_alive()])
        p = Process(target=consumer_starter, daemon=True, args=(shared_dict,))
        p.start()
        consumer_workers.append(p)
        warnlogger('Starting worker #', p.pid, "Alive:", num_alive)

    for each_process in consumer_workers:
        each_process.join()
    
    count_summary_dict = shared_dict.copy()
    warnlogger("Consumers Completed batch run \nSummary Of Consumer Message counts:", json.dumps(count_summary_dict, indent=2))
    list_of_count = list(count_summary_dict.values())
    count = sum(list_of_count)
    files = len(list_of_count)
    if count > 0:
        warnlogger("Messages persisted in minio/s3:", count, ", No_Of_Files:", files)
    else:
        warnlogger("No messages persisted, exiting")
    sys.exit(0)
    
if __name__ == '__main__':
    config = load_config_file()
    start_consumer_process_loop()

