#!/bin/bash

echo "=== 校园二手书系统前端快速部署脚本 ==="

# 设置变量
REMOTE_HOST="121.36.98.215"
REMOTE_USER="root"
LOCAL_FRONTEND_DIR="校园二手书"
REMOTE_PROJECT_DIR="/opt/book-platform"
REMOTE_WEB_DIR="/var/www/html/book-platform"

echo "云服务器地址: $REMOTE_HOST"
echo "前端项目目录: $LOCAL_FRONTEND_DIR"

# 检查本地前端文件
if [ ! -d "$LOCAL_FRONTEND_DIR" ]; then
    echo "错误: 找不到前端项目目录 $LOCAL_FRONTEND_DIR"
    echo "请确保在正确的目录下运行此脚本"
    exit 1
fi

echo "步骤 1: 上传前端文件到云服务器..."
scp -r $LOCAL_FRONTEND_DIR $REMOTE_USER@$REMOTE_HOST:$REMOTE_PROJECT_DIR/

echo "步骤 2: 在云服务器上安装Node.js和构建前端..."
ssh $REMOTE_USER@$REMOTE_HOST << 'EOF'
    # 安装Node.js (如果还没有安装)
    if ! command -v node &> /dev/null; then
        echo "安装Node.js..."
        curl -fsSL https://rpm.nodesource.com/setup_18.x | sudo bash - 2>/dev/null || curl -fsSL https://deb.nodesource.com/setup_18.x | sudo -E bash - 2>/dev/null
        sudo yum install -y nodejs 2>/dev/null || sudo apt-get install -y nodejs 2>/dev/null
    fi
    
    # 验证安装
    echo "Node.js版本: $(node --version)"
    echo "npm版本: $(npm --version)"
    
    # 安装依赖和构建
    cd /opt/book-platform/校园二手书
    echo "安装前端依赖..."
    npm install
    
    echo "构建生产版本..."
    npm run build
    
    # 创建Nginx目录
    mkdir -p /var/www/html/book-platform
    
    # 复制构建文件
    echo "复制构建文件到Nginx目录..."
    cp -r dist/* /var/www/html/book-platform/
    
    echo "前端构建完成！"
EOF

echo "步骤 3: 检查部署状态..."
echo "前端文件已部署到: http://$REMOTE_HOST"
echo ""
echo "部署完成！现在可以访问: http://$REMOTE_HOST"
echo ""
echo "如果需要配置Nginx，请参考前端部署指南.md"