package com.septech.snowflake4s

import java.time.Clock
import java.time.LocalDateTime
import java.time.Month
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.concurrent.locks.ReentrantLock

import com.google.inject.Guice
import com.google.inject.Inject
import com.septech.snowflake4s.exception.GenerateException
import com.septech.snowflake4s.node.NodeMachine

import scala.util.Failure
import scala.util.Success
import scala.util.Try

import net.codingwell.scalaguice.InjectorExtensions._
private[snowflake4s] class Snowflake4s @Inject()(nodeIdentifier: NodeMachine) extends Generator {
  /**
    *  Custom Epoch, mean for reference time: October 18, 1989, 16:53:40 UTC
    *
    *  The date of Galileo Spacecraft was launched to explored Jupiter and its moon from Kennedy Space Center, Florida, US.
    *
    *  FYI, Galileo is also the name used for the satellite navigation system of the European Union.
    *  It uses 22 August 1999 for Epoch instead of the Unix Epoch(January 1st, 1970).
    */
  final val GALILEO_LAUNCHED_DATETIME: ZonedDateTime =
    LocalDateTime.of(1989, Month.OCTOBER, 18, 16, 53, 40).atZone(ZoneId.of("US/Eastern"))
  final val CUSTOM_EPOCH: Long = GALILEO_LAUNCHED_DATETIME.toInstant.toEpochMilli

  private final val STARTING_SEQUENCE_NUMBERS: Int = 1
  private final val lock = new ReentrantLock()
  private var lastTimestamp: Long = -1L
  private var sequence: Long = 0L

  /**
    * Twitter's Snowflake schema:
    *   ID = (Time << 22 | NodeID << 10 | sequence)
    *
    * In which:
    * - 41 bits for time in milliseconds. This mean UTC epoch gives us 69 years.
    * - 10 bits that represent the Machine or Node Id, gives us up to 1024 machines id
    * - 12 bits that represent an auto-incrementing sequence, modulus 4096.
    *   This means we can generate 4096 IDs, per Node, per millisecond
    */
  final val TOTAL_BITS: Int = 64
  final val TIMESTAMP_BITS: Int = 41
  final val WORKER_BITS: Int = 10 // up to 1024 machine
  final val SEQUENCE_BITS: Int = 12 // up to 4096 ids per machine
  final val MAX_SEQUENCE_NUMBERS: Int = Math.pow(2, SEQUENCE_BITS).toInt

  final val TIMESTAMP_BITS_SHIFT: Int = TOTAL_BITS - TIMESTAMP_BITS
  final val WORKER_ID_BITS_SHIFT: Int = TOTAL_BITS - TIMESTAMP_BITS - SEQUENCE_BITS


  override def generate(): String = {
    lock.lock()
    val ids = generateIds(1).headOption.fold("")(id => id.getString)
    lock.unlock()
    ids
  }

  private def generateIds(batch: Int): List[Id] = Try {
    require(batch <= MAX_SEQUENCE_NUMBERS)

    val currentTimestamp: Long = Clock.systemUTC().millis()
    val shiftedTimestamp: Long = (currentTimestamp - CUSTOM_EPOCH) << TIMESTAMP_BITS_SHIFT
    val workerId: Long = (nodeIdentifier.getId().toLong % MAX_SEQUENCE_NUMBERS) << WORKER_ID_BITS_SHIFT
    val ids = List.empty[Id]

    for (i <- STARTING_SEQUENCE_NUMBERS to batch) {
      Id(shiftedTimestamp | workerId | getCurrentSequenceNumber(i, currentTimestamp)) :: ids
    }
    ids
  }  match {
    case Success(ids) => ids
    case Failure(_) => throw new GenerateException
  }

  private def getCurrentSequenceNumber(i: Int, time: Long): Int = {
    if (time != lastTimestamp) {
      sequence = 0
      lastTimestamp = time
    } else {
      sequence += 1

    }

    try {
      i + 1
    } finally {

    }
  }

}

object Snowflake4s {
  private val injector = Guice.createInjector(new GeneratorModule())
  private val nodeIdentifier = injector.instance[NodeMachine]
  private val defaultInstance: Snowflake4s = new Snowflake4s(nodeIdentifier)

  def defaultGenerator(): Snowflake4s = defaultInstance
}