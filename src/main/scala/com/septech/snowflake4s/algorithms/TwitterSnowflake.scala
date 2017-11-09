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
import com.septech.snowflake4s.exception.GenerateException
import com.septech.snowflake4s.exception.InvalidSystemClock
import com.septech.snowflake4s.node.NodeMachine

import scala.collection.mutable.ListBuffer
import scala.util.Failure
import scala.util.Success
import scala.util.Try

@Singleton
private[snowflake4s] class TwitterSnowflake @Inject()(nodeIdentifier: NodeMachine) extends Generator {
  /**
    *  This is a Custom Epoch, mean for reference time: October 18, 1989, 16:53:40 UTC -
    *  The date of Galileo Spacecraft was launched to explored Jupiter and its moon from Kennedy Space Center, Florida, US.
    *
    *  Galileo is also the name used for the satellite navigation system of the European Union.
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
  final private val TOTAL_BITS: Int = 64
  final private val TIMESTAMP_BITS: Int = 41
  final private val WORKER_BITS: Int = 10 // up to 1024 machine
  final private val SEQUENCE_BITS: Int = 12 // up to 4096 ids per machine
  final private val MAX_SEQUENCE_NUMBERS: Int = Math.pow(2, SEQUENCE_BITS).toInt

  final private val TIMESTAMP_BITS_SHIFT: Int = TOTAL_BITS - TIMESTAMP_BITS
  final private val WORKER_ID_BITS_SHIFT: Int = TOTAL_BITS - TIMESTAMP_BITS - SEQUENCE_BITS
  final private val SEQUENCE_MASK = -1L ^ (-1L << MAX_SEQUENCE_NUMBERS)

  final private val WORKER_ID: Long = (nodeIdentifier.getId().toLong % MAX_SEQUENCE_NUMBERS) << WORKER_ID_BITS_SHIFT

  override def generate(): String = generateByBatch(1).headOption.fold[String](throw new GenerateException)(_ => _)

  override def generateByBatch(batch: Int): List[String] = {
    lock.lock()

    val ids = generateIds(batch).map(_.getString)

    lock.unlock()
    ids
  }

  private def generateIds(batch: Int): List[IdEntity] = synchronized {
    Try {
      require(batch <= MAX_SEQUENCE_NUMBERS)

      val currentTimestamp: Long = Clock.systemUTC().millis()
      val shiftedTimestamp: Long = (currentTimestamp - CUSTOM_EPOCH) << TIMESTAMP_BITS_SHIFT

      val ids = new ListBuffer[IdEntity]()

      (STARTING_SEQUENCE_NUMBERS to batch).map(_ => ids += nextId(currentTimestamp, shiftedTimestamp, WORKER_ID))

      ids.toList
    }  match {
      case Success(ids) => ids
      case Failure(e) => throw new GenerateException(e.getMessage)
    }
  }

  private def nextId(currentTimestamp: Long, shiftedTimestamp: Long, workerId: Long): IdEntity = {
    var timestamp = Clock.systemUTC().millis()

    if (timestamp < lastTimestamp) {
      val timeDiff = lastTimestamp - timestamp
      throw new InvalidSystemClock("Clock moved backwards. Refusing to generate id for %d milliseconds".format(timeDiff))
    }

    if (lastTimestamp == timestamp) {
      sequence = (sequence + 1) & SEQUENCE_MASK
      if (sequence == 0) timestamp = nextMillis(lastTimestamp)
    } else sequence = 0

    lastTimestamp = timestamp

    IdEntity(shiftedTimestamp | workerId | sequence)
  }

  private def nextMillis(lastTimestamp: Long): Long = {
    var timestamp = Clock.systemUTC().millis()
    while (timestamp <= lastTimestamp) {
      timestamp = Clock.systemUTC().millis()
    }
    timestamp
  }

}