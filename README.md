# Snowflake4s
**A k-sortable unique ID generation lib using Scala.**

Snowflake4s can generate the roughly-sorted 64 bits ids as Long type written by Scala. The name of this lib was inspired by a project from Twitter called "Snowflake". So it's using the same encoded ID format as Twitter's Snowflake, but Snowflake4s providing the easier way to use Snowflake schema.

## Get Started
You should add the following dependency.
```sbtshell
resolvers +=
  "Sonatype OSS Snapshots" at "https://s01.oss.sonatype.org/content/repositories/snapshots"

libraryDependencies += "io.github.flintersvn" %% "snowflake4s" % "0.1.0-SNAPSHOT"
```

Add manual config to `application.conf`:
```hocon
snowflake4s {
  twitter { # using Twitter's algorithm
    machine_id = 1 # from 0 to 31
    machine_id = ${?SNOWFLAKE4S_MACHINE_ID} # Set machine id from env
    
    worker_id = 1 # from 0 to 31 
    worker_id = ${?SNOWFLAKE4S_WORKER_ID} # Set worker id from env

    # Default Epoch is October 18, 1989, 16:53:40 UTC
    # You can change to a different epoch by below setting
    # epoch = "2021-01-01T00:00:00Z" 
  }
}
```

## How to use
to generate id:
```scala
val IdGenerator = Snowflake4s.generator

val id = IdGenerator.generate()
id.toBase62 // 52nlGCNq00n
id.toLong // 4234436103643992065

// Revert info from saved Id

val id = Id.fromBase62("52nlGCNq00n")
println(id.workerId) // 5
```

You also could bulk generate 10 ids with the following snippet: 
```scala
val IdGenerator = Snowflake4s.generator

val ids = IdGenerator.bulkGenerate(10)
```

## License
The Snowflake4s is released under version 2.0 of the Apache License.

## References
- Slide:
  - https://www.slideshare.net/nguyentungniit/distributed-unique-id-generation
- Twitter Snowflake
  - https://github.com/twitter/snowflake/tree/scala_28
  - https://blog.twitter.com/2010/announcing-snowflake
- K-ordered
  - http://www.geeksforgeeks.org/nearly-sorted-algorithm/
