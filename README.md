# Snowflake4s
**A k-sortable unique ID generation lib using Scala.**

Snowflake4s can generate the roughly-sorted 64 bits ids as Long type.
The name of this lib was inspired by a project from Twitter called "Snowflake".
So, it's using the same encoded ID format as Twitter's Snowflake.
But Snowflake4s providing the easier way to use Snowflake schema.

##Get Started
You should add the following dependency.
```sbtshell
libraryDependencies += "com.septech" %% "snowflake4s_2.12" % "0.0.1-ALPHA"
```
Add manual config to `application.conf`:
```hocon
snowflake4s {
  twitter { # using Twitter's algorithm
    machine_id = 1 # from 1 to 31
    worker_id = 1 # from 1 to 31 
  }
}
```

##How to use
to generate id:
```scala
val IdGenerator = Snowflake4s.generator

val id = IdGenerator.generate()
```

You also could bulk generate 10 ids with the following snippet: 
```scala
val IdGenerator = Snowflake4s.generator

val ids = IdGenerator.bulkGenerate(10)
```

##License
The Snowflake4s is released under version 2.0 of the Apache License.

##References
- Twitter Snowflake
  - https://github.com/twitter/snowflake/tree/scala_28
  - https://blog.twitter.com/2010/announcing-snowflake
- K-ordered
  - http://www.geeksforgeeks.org/nearly-sorted-algorithm/
