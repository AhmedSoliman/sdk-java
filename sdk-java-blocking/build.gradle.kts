import com.google.protobuf.gradle.id

// Without these suppressions version catalog usage here and in other build
// files is marked red by IntelliJ:
// https://youtrack.jetbrains.com/issue/KTIJ-19369.
@Suppress(
    "DSL_SCOPE_VIOLATION",
    "MISSING_DEPENDENCY_CLASS",
    "UNRESOLVED_REFERENCE_WRONG_RECEIVER",
    "FUNCTION_CALL_EXPECTED")
plugins {
  `java-library`
  idea
  `maven-publish`
}

dependencies {
  api(project(":sdk-core"))

  testCompileOnly(coreLibs.javax.annotation.api)

  testImplementation(project(":sdk-core-impl"))
  testImplementation(testingLibs.junit.jupiter)
  testImplementation(testingLibs.assertj)
  testImplementation(coreLibs.protobuf.java)
  testImplementation(coreLibs.grpc.stub)
  testImplementation(coreLibs.grpc.protobuf)
  testImplementation(coreLibs.log4j.core)

  // Import test suites from sdk-core-impl
  testImplementation(project(":sdk-core-impl", "testArchive"))
  testProtobuf(project(":sdk-core-impl", "testArchive"))
}

val pluginJar =
    file(
        "${project.rootProject.rootDir}/protoc-gen-restate-java-blocking/build/libs/protoc-gen-restate-java-blocking-${project.version}-all.jar")

protobuf {
  plugins {
    id("grpc") { artifact = "io.grpc:protoc-gen-grpc-java:${coreLibs.versions.grpc.get()}" }
    id("restate") {
      // NOTE: This is not needed in a regular project configuration, you should rather use:
      // artifact = "dev.restate.sdk:protoc-gen-restate-java-blocking:1.0-SNAPSHOT:all@jar"
      path = pluginJar.path
    }
  }

  generateProtoTasks {
    ofSourceSet("test").forEach {
      // Make sure we depend on shadowJar from protoc-gen-restate-java-blocking
      it.dependsOn(":protoc-gen-restate-java-blocking:shadowJar")

      it.plugins {
        id("grpc")
        id("restate")
      }
    }
  }
}

publishing {
  publications {
    register<MavenPublication>("maven") {
      groupId = "dev.restate.sdk"
      artifactId = "sdk-java-blocking"

      from(components["java"])
    }
  }
}

// Generate test jar

configurations { register("testArchive") }

tasks.register<Jar>("testJar") {
  archiveClassifier.set("tests")

  from(project.the<SourceSetContainer>()["test"].output)
}

artifacts { add("testArchive", tasks["testJar"]) }
