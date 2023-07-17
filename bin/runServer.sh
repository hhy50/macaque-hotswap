
CURRENT_DIR=$(cd `dirname $0`; pwd)
java -jar macaque-server-1.0.jar --agentpath=$CURRENT_DIR/macaque-agent.jar --debug