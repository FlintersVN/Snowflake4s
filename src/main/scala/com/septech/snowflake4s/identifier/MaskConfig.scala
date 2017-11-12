package com.septech.snowflake4s.identifier

import com.septech.snowflake4s.MachineIdentifier

private[snowflake4s] class MaskConfig extends MachineIdentifier {
  private val defaultMachineId: Long = 1L
  private val defaultWorkerId: Long = 1L

  override def getId(): String = {
    Option(System.getProperty("snowflake4s.twitter.machine_id")).getOrElse(defaultMachineId.toString)
  }

  def getWorkerId(): String = {
    Option(System.getProperty("snowflake4s.twitter.worker_id")).getOrElse(defaultWorkerId.toString)
  }

}
