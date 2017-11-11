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
package com.septech.snowflake4s.identifier

import java.lang.management.ManagementFactory
import java.net._

import com.septech.snowflake4s.MachineIdentifier
import com.septech.snowflake4s.exception.MacAddressException

import scala.util.Failure
import scala.util.Success
import scala.util.Try

private[snowflake4s] class MachineMACAddress extends MachineIdentifier {

  override def getId(): String = Try {
    val localNetworkInterface = NetworkInterface.getByInetAddress(InetAddress.getLocalHost)

    localNetworkInterface
      .getHardwareAddress.toList
      .map(byte => Integer.parseInt(String.format("%02x", byte.asInstanceOf[Object]), 16))
      .foldLeft(0L) { case (acc, item) => acc * 256 + item }
      .toString
  } match {
    case Failure(_) => throw new MacAddressException()
    case Success(address) => address
  }

  override def getWorkerId() = {
    ManagementFactory.getRuntimeMXBean.getName.split("@").headOption
      .fold(throw new RuntimeException("Can not get process id of application"))(pid => pid)
  }
}