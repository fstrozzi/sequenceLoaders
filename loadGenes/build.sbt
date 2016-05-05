

scalaVersion := "2.11.6"

name := "loadGenes"

version := "1.0.0"

libraryDependencies ++= Seq(
   "org.scalikejdbc" %% "scalikejdbc" % "2.2.7",
   "mysql" % "mysql-connector-java" % "5.1.35"
)
//libraryDependencies ++= Seq(
//	groupID % artifactID % revision,
//	groupID % otherID % otherRevision
//)

//resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"
				

