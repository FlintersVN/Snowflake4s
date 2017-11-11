package com.septech.snowflake4s.identifier

import com.septech.snowflake4s.MachineIdentifier
import com.typesafe.config.ConfigFactory

import scala.util.Failure
import scala.util.Success
import scala.util.Try

private[snowflake4s] class SnowflakeIdentifier extends MachineIdentifier {
  private val defaultMachineId: Long = 1L
  private val defaultWorkerId: Long = 1L

  private val config = ConfigFactory.load()

  override def getId(): String = Try(config.getString("snowflake4s.twitter.machine_id")) match {
    case Success(id) => id
    case Failure(_) => defaultMachineId.toString
  }

  def getWorkerId(): String = Try(config.getString("snowflake4s.twitter.worker_id")) match {
    case Success(id) => id
    case Failure(_) => defaultWorkerId.toString
  }

}
