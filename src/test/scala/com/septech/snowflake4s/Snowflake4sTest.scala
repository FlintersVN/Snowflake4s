package com.septech.snowflake4s

import org.scalatest.flatspec.AnyFlatSpec

import java.time.{LocalDateTime, ZoneId}

class Snowflake4sTest extends AnyFlatSpec {
  it can "convert to various forms" in {

    val timezone = ZoneId.of("US/Eastern")
    val date = LocalDateTime.of(2021, 10, 15, 12, 30, 59, 127000000).atZone(timezone)
    val timestamp = date.toInstant.toEpochMilli
    // 1634315459127

    val id = Id(1, 5, timestamp, 1)

    assert(Id.from(4234436103643992065L) == id)
    assert(id.toLong == 4234436103643992065L)
    assert(id.getDate == date)
    assert(id.toBase62 == "52nlGCNq00n")
    assert(Id.fromBase62("52nlGCNq00n") == id)
  }
}
