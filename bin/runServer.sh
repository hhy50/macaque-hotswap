
CURRENT_DIR=$(cd `dirname $0`; pwd)
JAVA_HOME=''
$JAVA_HOME/bin/java -Djava.ext.dirs=$JAVA_HOME/lib -jar macaque-server.jar --server --serverPort=2023  --agentpath=$CURRENT_DIR/macaque-agent.jar --debug