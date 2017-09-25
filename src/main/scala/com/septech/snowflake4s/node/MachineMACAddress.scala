package com.septech.snowflake4s.node

import java.net._

import com.septech.snowflake4s.exception.MacAdddressException

import scala.util.Failure
import scala.util.Success
import scala.util.Try

class MachineMACAddress extends NodeMachine {

  override def getId(): String = Try {
    val localNetworkInterface = NetworkInterface.getByInetAddress(InetAddress.getLocalHost)

    localNetworkInterface.getHardwareAddress.toList.map(b => String.format("%02x", b.asInstanceOf[Object]))
      .map(Integer.parseInt(_, 16)).foldLeft(0L) {
        case (acc, item) => acc * 256 + item
      }.toString
  } match {
    case Failure(_) => throw new MacAdddressException()
    case Success(address) => address
  }

}
