package com.septech.snowflake4s

import com.google.inject.AbstractModule
import com.septech.snowflake4s.node.MachineMACAddress
import com.septech.snowflake4s.node.NodeMachine
import net.codingwell.scalaguice.ScalaModule

class GeneratorModule extends AbstractModule with ScalaModule {

  override def configure() = {
    bind[NodeMachine].to[MachineMACAddress]
  }

}
