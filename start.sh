#!/usr/bin/env bash
# Brings up every service mansys-bigdata depends on, then starts the app.

echo "=============================================="
echo " SWE 307 — mansys-bigdata startup"
echo "=============================================="

echo
echo "[1/5] MySQL"
sudo systemctl start mysql
systemctl is-active mysql

echo
echo "[2/5] Redis"
sudo systemctl start redis-server
redis-cli ping

echo
echo "[3/5] Hadoop HDFS"
if /usr/local/hadoop/bin/hdfs dfs -ls / >/dev/null 2>&1; then
    echo "already running"
else
    sudo su - hadoop -c "/usr/local/hadoop/sbin/start-dfs.sh"
    sleep 5
fi
/usr/local/hadoop/bin/hdfs dfs -ls /swe307/images | tail -n +2 | wc -l | xargs echo "images in HDFS:"

echo
echo "[4/5] Ollama"
if curl -s --max-time 3 http://localhost:11434/api/tags >/dev/null 2>&1; then
    echo "already running"
else
    nohup ollama serve >/tmp/ollama.log 2>&1 &
    sleep 4
fi
curl -s http://localhost:11434/api/tags | grep -o '"name":"[^"]*"' | head -1

echo
echo "[5/5] Freeing port 8080"
pkill -f mansys-bigdata 2>/dev/null
sleep 1
echo "ok"

echo
echo "=============================================="
echo " Starting application on http://localhost:8080"
echo "=============================================="
mvn spring-boot:run
