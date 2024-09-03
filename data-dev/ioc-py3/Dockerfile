# 使用基础镜像
FROM  balenalib/aarch64-alpine-python

# 设置工作目录
WORKDIR /app

# 将当前目录下的文件添加到工作目录中
ADD . /app


# 安装所需的 Python 包
RUN pip3 install flask pymysql  -i https://pypi.doubanio.com/simple

# 定义容器启动时运行的命令
CMD ["python3", "index.py"]