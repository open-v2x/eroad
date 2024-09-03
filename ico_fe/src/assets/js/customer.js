/**
 * 对RabbitMQ的封装
 */
 let amqp = require('amqplib');

 class RabbitMQ {
     constructor() {
        this.hosts = [];
        this.index = 0;
        this.length = this.hosts.length;
        this.open = amqp.connect(this.hosts[this.index]);
     }
     receiveQueueMsg(queueName, receiveCallBack, errCallBack) {
         let self = this;

         self.open
             .then(function (conn) {
                 return conn.createChannel();
             })
             .then(function (channel) {
                 return channel.assertQueue(queueName)
                     .then(function (ok) {
                         return channel.consume(queueName, function (msg) {
                             if (msg !== null) {
                                 let data = msg.content.toString();
                                 channel.ack(msg);
                                 receiveCallBack && receiveCallBack(data);
                             }
                         })
                             .finally(function () {
                                 setTimeout(() => {
                                     if (channel) {
                                         channel.close();
                                     }
                                 }, 500)
                             });
                     })
             })
             .catch(function () {
                 let num = self.index++;
                 if (num <= self.length - 1) {
                     self.open = amqp.connect(self.hosts[num]);
                 } else {
                     self.index = 0;
                     self.open = amqp.connect(self.hosts[0]);
                 }
             });
     }
    }
    let mq = new RabbitMQ();
    mq.receiveQueueMsg('test7', res => {
        console.log(res, 'aaa')
    })

