package cn.eroad.ioc_spark_utils

import java.text.SimpleDateFormat
import java.util.{Calendar, Date}
object utilsSpark {
  val kafkaBootstrapServersRadSdk: String = sys.env.getOrElse("KAFKA_BOOTSTRAP_SERVERS_RAD_SDK", "default_value")
  val url: String = sys.env.getOrElse("URL", "default_value")
  val username: String = sys.env.getOrElse("USERNAME", "default_value")
  val password: String = sys.env.getOrElse("PASSWORD", "default_value")
  val tmpPath: String = sys.env.getOrElse("TMP_PATH", "default_value")

  def nowday: String = {
    val time = new Date().getTime
    val format = new SimpleDateFormat("dd")
    val nowday = format.format(time)
    nowday
  }

  def agoyue: String = {
    var period: String = ""
    var cal: Calendar = Calendar.getInstance()
    var df: SimpleDateFormat = new SimpleDateFormat("yyyyMM")
    cal.add(Calendar.MONTH, -1)
    cal.set(Calendar.DATE, 1) 
    period = df.format(cal.getTime)
    period
  }
  
  def agoyue2: String = {
    var period: String = ""
    var cal: Calendar = Calendar.getInstance()
    var df: SimpleDateFormat = new SimpleDateFormat("yyyyMM")
    cal.add(Calendar.MONTH, -2)
    cal.set(Calendar.DATE, 1) 
    period = df.format(cal.getTime)
    period
  }

  def tenMinutesAgo: String = {
    val format = new SimpleDateFormat("yyyy-MM-dd HH:mm")
    val cal = Calendar.getInstance()
    cal.add(Calendar.MINUTE, -10)
    val qonehour = format.format(cal.getTime)
    qonehour.dropRight(1) + "0" + ":00"
  }

  def weekago: Int = {
    val format = new SimpleDateFormat("yyyyMMdd")
    val cal = Calendar.getInstance()
    cal.add(Calendar.DATE, -7)
    val yesterhour = format.format(cal.getTime)
    yesterhour.toInt
  }
}
