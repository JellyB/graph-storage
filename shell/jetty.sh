#!/usr/bin/env bash
#  
# chkconfig: 345 30 01  
# description: Jetty  
#  
# File : Jetty  
#  
# Description: Starts and stops the Spring boot  
#  
  
# source /etc/rc.d/init.d/functions  
  
APP_HOME=/wdcloud/app/jetty_graph_storage
LOG_HOME=/wdcloud/log/jetty_graph_storage
APP_NAME=graph-storage-1.0
APP_USER=jetty  
APP_PORT=8013
APP_PID=$(ps -ef | grep ${APP_NAME}.jar | grep -v grep | awk '{print $2}')
function start(){
	if [ -z "$APP_PID" ]; then
		echo "start project..."
		exec nohup java -jar $APP_HOME/$APP_NAME.jar --logging.path=${LOG_HOME} --server.port=${APP_PORT} >/dev/null 2>&1 &
		echo "start project end..."
	else
		echo "warning: the spring boot server is started!"
		exit 1
	fi
}

function stop(){
	if [ -z "$APP_PID" ]; then
		echo "No spring boot server to stop"
	else
		echo "stop project..."
		kill -9 $APP_PID
		echo "spring boot server is stoped"
	fi
}


function restart(){
    stop
    sleep 3
	APP_PID=$(ps ax | grep java | grep $APP_HOME | grep -v grep | awk '{print $1}')
    start
}

function status(){
	if [ -z "$APP_PID" ]; then
		echo "stoped"
	else 
	    exec ps -ef | grep  ${APP_NAME}.jar | grep -v grep
		echo "server is running"
	fi
}

case "$1" in
    start)  
    start
    ;;
    stop)
    stop
    ;;
    restart)
    restart
    ;;
	status)
    status
    ;;
    *)  
    printf 'Usage: %s {start|stop|restart|status}\n' "$prog"
    ;;
esac
exit 1
