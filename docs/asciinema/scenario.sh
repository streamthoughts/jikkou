echo "# See Jikkou in action!" | pv -qL 12
echo "# Jikkou is available as a native command line interface (CLI)" | pv -qL 12
echo "# that you can install easily via SDKMan!" | pv -qL 12
echo "sdk install jikkou" | pv -qL 12
echo '# And it can be used to manage an Apache Kafka cluster.' | pv -qL 12
echo 'jikkou config set-context localhost --config-props=kafka.client.bootstrap.servers=localhost:9092' | pv -qL 12
echo 'jikkou config use-context localhost' | pv -qL 12
echo 'jikkou health get kafka'
jikkou health get kafka | lolcat
echo '------------------------' | pv -qL 6
echo "# Let's say you would like to create and manage a Kafka Topic with Jikkou" | pv -qL 12
echo '# First, write a KafkaTopic descriptor file:' | pv -qL 12
echo 'cat topic.yaml' | pv -qL 12
cat topic.yaml | lolcat
echo ''
echo '------------------------' | pv -qL 6
echo '# And apply it to your Kafka Cluster:' | pv -qL 12
echo 'jikkou create -f topic.yaml -o YAML' | pv -qL 12
jikkou apply -f topic.yaml -o YAML | lolcat
echo '# Next, you can use Jikkou to describe your Kafka Topics:' | pv -qL 12
echo 'jikkou get kafkatopics --name my-topic' | pv -qL 12
jikkou get kafkatopics --name my-topic  | lolcat
echo '# Of course, you can use Jikkou to modify your Kafka Topic (e.g., adding property: retention.ms=86400000)' | pv -qL 12
echo "# Let's update our descriptor file:" | pv -qL 12
cp topic.yaml topic.bk.yaml
echo "    retention.ms: 86400000 # updated" >> topic.yaml
echo 'cat topic.yaml' | pv -qL 12
cat topic.yaml | lolcat
echo ''
echo '------------------------' | pv -qL 6
echo '# And apply it to your Kafka cluster as previously:' | pv -qL 12
echo 'jikkou apply -f topic.yaml -o yaml' | pv -qL 12
jikkou apply -f topic.yaml -o yaml | lolcat
echo '# And it is done :)' | pv -qL 12
echo '------------------------' | pv -qL 6
echo '# Jikkou is not limited to Kafka Topic, but can be used for' | pv -qL 12
echo '# managing Schema Registry, Kafka Connect, etc...' | pv -qL 12
echo '# You can run Jikkou command manually, or execute it ' | pv -qL 12
echo '# part of your CI/CD pipeline.' | pv -qL 12
echo '# Jikkou has a Github Action and Server API as well' | pv -qL 12
echo '# Learn more about Jikkou at ' | pv -qL 12
echo '# https://streamthoughts.github.io/jikkou/docs' | cat | pv -qL 12 | lolcat
rm topic.yaml && mv topic.bk.yaml topic.yaml