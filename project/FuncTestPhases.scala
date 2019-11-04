import sbt.Tests.{Group, SubProcess}
import sbt.{ForkOptions, TestDefinition}

private object FuncTestPhases {

  def oneForkedJvmPerTest(tests: Seq[TestDefinition]): Seq[Group] =
    tests map { test =>
      Group(test.name, Seq(test), SubProcess(ForkOptions(runJVMOptions = Seq("-Dtest.name=" + test.name, "-Dlogger.resource=logback-test.xml"))))
    }
}
