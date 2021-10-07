package com.septech.snowflake4s

import java.time.{Instant, LocalDateTime, Month, ZoneId, ZonedDateTime}

private [snowflake4s] object Encoding {

  /**
    *  This is a Custom Epoch, mean for reference time: October 18, 1989, 16:53:40 UTC -
    *  The date of Galileo Spacecraft was launched to explored Jupiter and its moon from Kennedy Space Center, Florida, US.
    *
    *  Galileo is also the name used for the satellite navigation system of the European Union.
    *  It uses 22 August 1999 for Epoch instead of the Unix Epoch(January 1st, 1970).
    */
  final val zoneId = ZoneId.of("US/Eastern")
  final val GALILEO_LAUNCHED_DATETIME: ZonedDateTime =
    LocalDateTime.of(1989, Month.OCTOBER, 18, 16, 53, 40).atZone(zoneId)

  final val EPOCH: Long = Option(System.getProperty("snowflake4s.twitter.epoch"))
    .map(ZonedDateTime.parse)
    .map(_.toInstant.toEpochMilli)
    .getOrElse(GALILEO_LAUNCHED_DATETIME.toInstant.toEpochMilli)

  final private val workerIdBits: Long = 5L
  final private val datacenterIdBits: Long = 5L
  final private val sequenceBits: Long = 12L
  final private val workerIdShift: Long = sequenceBits
  final private val machineIdShift: Long = sequenceBits + workerIdBits
  final private val timestampLeftShift: Long = sequenceBits + workerIdBits + datacenterIdBits
  final private val sequenceMask: Long = -1L ^ (-1L << sequenceBits)

  def encode(id: Id): Long = {
    (id.timestamp - EPOCH) << timestampLeftShift |
      id.machineId << machineIdShift |
      id.workerId << workerIdShift |
      id.counter
  }

  def decode(value: Long): Id = {
    Id(
      value >> sequenceBits & (-1L ^ (-1L << workerIdBits)),
      (value >> machineIdShift) & (-1L ^ (-1L << datacenterIdBits)),
      (value >> timestampLeftShift) + EPOCH,
      value & sequenceMask
    )
  }

  def getDateTime(id: Id): ZonedDateTime = {
    Instant.ofEpochMilli(id.timestamp).atZone(zoneId)
  }
}

