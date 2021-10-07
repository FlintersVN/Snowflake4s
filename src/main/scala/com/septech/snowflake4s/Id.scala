package com.septech.snowflake4s

import java.time.ZonedDateTime
import com.github.tototoshi.base62.Base62

case class Id(workerId: Long, machineId: Long, timestamp: Long, counter: Long) {

  def toLong: Long = encode

  override def toString: String = encode.toString

  def encode: Long =  {
    Encoding.encode(this)
  }

  def getDate: ZonedDateTime = {
    Encoding.getDateTime(this)
  }

  def toBase62: String = new Base62().encode(encode)
}

object Id {
  def from(id: Long): Id = {
    Encoding.decode(id)
  }

  def fromBase62(id: String): Id = from(new Base62().decode(id))

  def next: Id = Snowflake4s.generator.generate()

  def bulk(numOfIds: Int): List[Id] = Snowflake4s.generator.bulkGenerate(numOfIds)
}
