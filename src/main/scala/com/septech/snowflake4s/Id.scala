package com.septech.snowflake4s

case class Id(id: Long) {

  def getLong: Long = id

  def getString: String = id.toString

  def getByte: Array[Byte] = id.toString.map(_.toByte).toArray

}
