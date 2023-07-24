
CURRENT_DIR=$(cd `dirname $0`; pwd)
JAVA_HOME=''
$JAVA_HOME/bin/java -Djava.ext.dirs=$JAVA_HOME/lib -jar --serverPort=2023 macaque-server-1.0.jar --agentpath=$CURRENT_DIR/macaque-agent.jar --server --debug