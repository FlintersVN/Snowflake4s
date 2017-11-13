/*
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
 */
package com.septech.snowflake4s.algorithms

import java.time.Clock
import java.time.LocalDateTime
import java.time.Month
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.concurrent.locks.ReentrantLock

import com.google.inject.Inject
import com.google.inject.Singleton
import com.septech.snowflake4s.Generator
import com.septech.snowflake4s.IdEntity
import com.septech.snowflake4s.MachineIdentifier
import com.septech.snowflake4s.exception.GenerateException
import com.septech.snowflake4s.exception.InvalidSystemClock

import scala.collection.mutable.ListBuffer
import scala.util.Failure
import scala.util.Success
import scala.util.Try

@Singleton
private[snowflake4s] class Snowflake @Inject()(identifier: MachineIdentifier) extends Generator {
  /**
    *  This is a Custom Epoch, mean for reference time: October 18, 1989, 16:53:40 UTC -
    *  The date of Galileo Spacecraft was launched to explored Jupiter and its moon from Kennedy Space Center, Florida, US.
    *
    *  Galileo is also the name used for the satellite navigation system of the European Union.
    *  It uses 22 August 1999 for Epoch instead of the Unix Epoch(January 1st, 1970).
    */
  final val GALILEO_LAUNCHED_DATETIME: ZonedDateTime =
    LocalDateTime.of(1989, Month.OCTOBER, 18, 16, 53, 40).atZone(ZoneId.of("US/Eastern"))
  final val EPOCH: Long = GALILEO_LAUNCHED_DATETIME.toInstant.toEpochMilli

  private final val STARTING_SEQUENCE_NUMBERS: Int = 1
  private final val lock = new ReentrantLock()
  private var sequence: Long = 0L

  final private val workerIdBits: Long = 5L
  final private val datacenterIdBits: Long = 5L
  final private val sequenceBits: Long = 12L
  final private val workerIdShift: Long = sequenceBits
  final private val machineIdShift: Long = sequenceBits + workerIdBits
  final private val timestampLeftShift: Long = sequenceBits + workerIdBits + datacenterIdBits
  final private val sequenceMask: Long = -1L ^ (-1L << sequenceBits)
  final private var lastTimestamp: Long = -1L

  final private val MACHINE_ID: Long = identifier.getId.toLong
  final private val WORKER_ID: Long = identifier.getWorkerId.toLong

  override def generate(): String = bulkGenerate(1).headOption.fold[String](throw new GenerateException)(id => id)

  override def bulkGenerate(batch: Int): List[String] = {
    lock.lock()

    val ids = generateIds(batch).map(_.getString)

    lock.unlock()
    ids
  }

  private def generateIds(batch: Int): List[IdEntity] = synchronized {
    require(batch > 0, new IllegalArgumentException("batch must be a non negative number"))

    Try {
      val ids = new ListBuffer[IdEntity]()

      (STARTING_SEQUENCE_NUMBERS to batch).foreach(_ => ids += nextId)

      ids.toList
    }  match {
      case Success(ids) => ids
      case Failure(e) => throw new GenerateException(e.getMessage)
    }
  }

  private def nextId: IdEntity = {
    var currentTimestamp: Long = Clock.systemUTC().millis()

    if (currentTimestamp < lastTimestamp) {
      val timeDiff = lastTimestamp - currentTimestamp
      throw new InvalidSystemClock("Clock moved backwards. Refusing to generate id for %d milliseconds".format(timeDiff))
    }

    if (lastTimestamp == currentTimestamp) {
      sequence = (sequence + 1) & sequenceMask

      if (sequence == 0) {
        currentTimestamp = nextMillis(lastTimestamp)
      }
    } else{
      sequence = 0
    }

    lastTimestamp = currentTimestamp

    IdEntity(
      (currentTimestamp - EPOCH) << timestampLeftShift |
      MACHINE_ID << machineIdShift |
      WORKER_ID << workerIdShift |
      sequence
    )
  }

  private def nextMillis(lastTimestamp: Long): Long = {
    var timestamp = Clock.systemUTC().millis()
    while (timestamp <= lastTimestamp) {
      timestamp = Clock.systemUTC().millis()
    }
    timestamp
  }

}