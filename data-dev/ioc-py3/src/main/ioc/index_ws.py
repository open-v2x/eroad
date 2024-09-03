import asyncio
import json
from aiokafka import AIOKafkaConsumer
import websockets

bootstrap_servers = ['bootstrap_servers']
topic = 'topic'
ws_port = 1651


async def consume_kafka_send_websocket(websocket, path):
    consumer = AIOKafkaConsumer(
        topic,
        bootstrap_servers=bootstrap_servers,
        value_deserializer=lambda m: json.loads(m.decode('utf-8'))
    )
    await consumer.start()
    try:
        async for msg in consumer:
            message = msg.value
            print(f"收到Kafka消息: {message}")
            await websocket.send(json.dumps(message))
    except websockets.exceptions.ConnectionClosed:
        print("客户端断开连接")
    finally:
        await consumer.stop()
async def main():
    server = await websockets.serve(consume_kafka_send_websocket, '0.0.0.0', ws_port)
    print(f"WebSocket服务器已启动，监听端口 {ws_port}")
    await asyncio.Future()

if __name__ == '__main__':
    asyncio.run(main())
