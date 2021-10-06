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
import java.util.concurrent.locks.ReentrantLock
import com.google.inject.Inject
import com.google.inject.Singleton
import com.septech.snowflake4s.{Generator, Id, MachineIdentifier}
import com.septech.snowflake4s.exception.GenerateException
import com.septech.snowflake4s.exception.InvalidSystemClock

import scala.collection.mutable.ListBuffer
import scala.util.Failure
import scala.util.Success
import scala.util.Try

@Singleton
private[snowflake4s] class Snowflake @Inject()(identifier: MachineIdentifier) extends Generator {

  private final val STARTING_SEQUENCE_NUMBERS: Int = 1
  private final val lock = new ReentrantLock()
  private var sequence: Long = 0L

  final private val sequenceBits: Long = 12L
  final private val sequenceMask: Long = -1L ^ (-1L << sequenceBits)
  final private var lastTimestamp: Long = -1L

  final private val MACHINE_ID: Long = identifier.getId.toLong
  final private val WORKER_ID: Long = identifier.getWorkerId.toLong

  override def generate(): Id = bulkGenerate(1).headOption.fold[Id](throw new GenerateException)(id => id)

  override def bulkGenerate(batch: Int): List[Id] = {
    lock.lock()

    val ids = generateIds(batch)

    lock.unlock()
    ids
  }

  private def generateIds(batch: Int): List[Id] = synchronized {
    require(batch > 0, new IllegalArgumentException("batch must be a non negative number"))

    Try {
      val ids = new ListBuffer[Id]()

      (STARTING_SEQUENCE_NUMBERS to batch).foreach(_ => ids += nextId)

      ids.toList
    }  match {
      case Success(ids) => ids
      case Failure(e) => throw new GenerateException(e.getMessage)
    }
  }

  private def nextId: Id = {
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

    Id(
      WORKER_ID,
      MACHINE_ID,
      currentTimestamp,
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
