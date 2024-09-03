# IOC-Flink


## toDevelopers

### Common
~~~
1、Flink版本为1.15.1 Scala编译版本2.12
2、避免使用过时类/方法
3、KafkaProducer/Consumer已过时，可以使用cn.eroad.utils.KafkaUtil
   工具类或自行创建KafkaSource
4、设计到CheckPoint的程序务必给所有有状态算子赋予UID
~~~

### Java
~~~
1、编写Java的Streaming程序继承cn.eroad.impl.AbstractFlinkStreamingApp
2、JDK版本1.8
~~~

### Scala
~~~
1、编写Scala的Streaming程序继承cn.eroad.impl.scala.AbstractFlinkStreamingApp
2、Scala版本选择Scala 2.12
3、在状态更新时会涉及到需要转换为java.util.List 请使用scala.collection.JavaConverters
~~~
