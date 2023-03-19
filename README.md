![Build & Test Workflow Status Badge](https://github.com/enbnt/examples.enbnt.dev/actions/workflows/main.yml/badge.svg)

# examples.enbnt.dev
This is a Monorepo for https://www.enbnt.dev blog post examples, which uses
[Bazel](https://www.bazel.io) to build and test all artifacts.

## tracing-and-testing
This [project](https://github.com/enbnt/examples.enbnt.dev/tree/main/tracing-and-testing)
is an example [Finatra](https://www.github.com/twitter/finatra)
project, which illustrates some of the built-in distributed trace annotation
testing utilities. See the [Distributed Tracing and Testing](https://www.enbnt.dev/posts/tracing-and-testing/)
blog post for a deeper look into this project.

## Command Reference

To generate a code coverage report (requires `lcov` and `genhtml` to be installed):

``` 
$ bazel coverage --cache_test_results=no\
    --combined_report=lcov \
    --coverage_report_generator="@bazel_tools//tools/test/CoverageOutputGenerator/java/com/google/devtools/coverageoutputgenerator:Main" \
    --instrumentation_filter="-/src/test/scala[/:]" \
    //...
$ genhtml --output genhtml "$(bazel info output_path)/_coverage/_coverage_report.dat"
$ open genhtml/index.html   
```