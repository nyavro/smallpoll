// Using Android Plugin
android.Plugin.androidBuild

// Specifying the Android target Sdk version
platformTarget in Android := "android-16"

// Application Name
name := """small-poll"""

// Application Version
version := "1.0.0"

// Scala version
scalaVersion := "2.11.6"

// Repositories for dependencies
resolvers ++= Seq(Resolver.mavenLocal,
  DefaultMavenRepository,
  Resolver.typesafeRepo("releases"),
  Resolver.typesafeRepo("snapshots"),
  Resolver.typesafeIvyRepo("snapshots"),
  Resolver.sonatypeRepo("releases"),
  Resolver.sonatypeRepo("snapshots"),
  Resolver.defaultLocal)

libraryDependencies ++= Seq(
  "org.scaloid" %% "scaloid" % "4.0-RC1" withSources() withJavadoc(),
//  "com.propensive" %% "rapture-json" % "1.1.0" withSources() withJavadoc(),
  "org.apache.commons" % "commons-io" % "1.3.2"
)

// Override the run task with the android:run
run <<= run in Android

// Activate proguard for Scala
proguardScala in Android := true

// Activate proguard for Android
useProguard in Android := true

// Set proguard options
proguardOptions in Android ++= Seq(
  "-ignorewarnings",
  "-keep class scala.Dynamic")

//proguardCache in Android ++= Seq(
//  ProguardCache("org.scaloid") % "org.scaloid"
//)