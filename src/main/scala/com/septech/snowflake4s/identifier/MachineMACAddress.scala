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

  override def getId: String = Try {
    val localNetworkInterface = MachineMACAddress.guessBestInterface

    localNetworkInterface.get // NPE here
      .getHardwareAddress
      .map(byte => byte & 0xFF)
      .foldLeft(0L) { (acc, item) => (acc << 8) + item }
      .toString
  } match {
    case Failure(_) => throw new MacAddressException()
    case Success(address) => address
  }

  override def getWorkerId: String = {
    ManagementFactory.getRuntimeMXBean.getName.split("@").headOption
      .fold(throw new RuntimeException("Can not get process id of application"))(pid => pid)
  }
}

private object MachineMACAddress {
  import scala.collection.JavaConverters._

  def getAvailableInterfaces: Map[String, NetworkInterface] =
    NetworkInterface.getNetworkInterfaces.asScala
      .foldLeft(Map.empty[String, NetworkInterface]){ (map, intf) =>
        if (isValidInterface(intf))
          map + ((intf.getName, intf))
        else
          map
      }

  def guessBestInterface: Option[NetworkInterface] =
    getAvailableInterfaces.headOption.map(_._2)

  private def isValidInterface(intf: NetworkInterface): Boolean =
    Option(intf.getHardwareAddress).isDefined && intf.isUp && !intf.isVirtual
}