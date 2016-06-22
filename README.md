# Shard

A reactive in-memory database based on Akka Persistence.

```
val users = new Shard[User] {
    val schema = 
         UniqueIndex("fullName", (u: User) => u.firstName+ " " +u.lastName).
         RangeIndex("age", (u: User) => u.age).
         StorageEngine(HashMap)
}

users ! Insert(Seq(
   User("Ben", "Neil", 32),
   User("Nick", "Connor", 24),
   User("Susy", "McQue", 29
 ))
 
users ? Where( u => u.firstName == "Nick" )

users ? Where("age" < 30).Sort( u => u.lastName )
```


## Getting Started

The current stable version is None, and is built against Scala 2.11.X and Akka 2.4.X

For SBT, add the following to your build:

```
"com.shard" %% "com.shard.db" % "0.0.0"
``` 

## Roadmap
### 0.1

* Build simple configuration
    * snapshot
* Refactor code
* Develop Schema
    * schema builder for shards
    * storage engines: e.g. LinkedHashMap for a capped Shard
* Develop query execution engine
    * api support for Sort, Limit, Skip, Joins, Wheres etc
    * use indexes where applicable
* Migration plan
    * allow mods to underlying data structure, e.g. adding field to Case Class
* Command line access
    
### 0.2

Not well defined but may include:

* Clustering support `val users = Cluster[User]`
* Replication support