#!/bin/bash

# 校园二手书交易平台启动脚本
# 适用于华为云ECS环境

# 设置项目路径
PROJECT_HOME="/home/book-platform"
JAR_NAME="book-api-0.0.1-SNAPSHOT.jar"
LOG_FILE="/var/log/book-platform.log"

# 创建必要的目录
mkdir -p $PROJECT_HOME/logs
mkdir -p $PROJECT_HOME/uploads

# 设置Java环境 - 使用Java 21
export JAVA_HOME="/usr/lib/jvm/java-21"
export PATH=$JAVA_HOME/bin:$PATH

# 检查Java环境
echo "使用的Java版本: $(java -version 2>&1 | head -n 1)"
echo "JAVA_HOME: $JAVA_HOME"

if ! command -v java &> /dev/null; then
    echo "Java未安装，请先安装Java 21"
    exit 1
fi

echo "启动校园二手书交易平台..."
echo "项目路径: $PROJECT_HOME"
echo "日志文件: $LOG_FILE"

# 进入项目目录
cd $PROJECT_HOME

# 检查JAR文件是否存在
if [ ! -f "$JAR_NAME" ]; then
    echo "❌ JAR文件不存在: $JAR_NAME"
    echo "请检查文件是否存在于: $PROJECT_HOME/$JAR_NAME"
    exit 1
fi

echo "JAR文件存在: $(ls -la $JAR_NAME)"

# 直接运行Java应用（不使用nohup，由systemd管理）
exec java -jar $JAR_NAME \
    --spring.profiles.active=cloud \
    --server.port=8080 \
    >> $LOG_FILE 2>&1