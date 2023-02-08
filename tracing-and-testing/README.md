# Tracing And Testing

Example project for Distributed Tracing and Testing Blog Post at
[enbnt.dev](https://www.enbnt.dev). Please note that I am not a
Bazel build expert and no guarantees of efficiency or accuracy are
made as a result of using these example projects. Pull requests are
welcome!

***Note: Installing [Bazel](https://www.bazel.io) is a pre-req for running this project locally.***

**All command examples are run from the root directory of `examples.enbnt.dev` repo.**

## Building

`$ bazel build //tracing-and-testing/...`

## Testing

`$ bazel test --test_output=errors //tracing-and-testing/...`

## Run the Server

`$ bazel run //tracing-and-testing/src/main/scala/dev/enbnt/example:bin`

The server will be accessible via port 8888 by default. You can access it
via

`$ curl 'http://localhost:8888/score?name=$USER'`

You can also view the TwitterServer Admin in your web browser at

'http://localhost:9990/admin' and 'http://localhost:9990/admin/lint'
will show any configuration lint issues with your server runtime.

## Lint

***Note: This requires installing [scalafmt](https://scalameta.org/scalafmt/docs/installation.html)
in order to work. There are various integrations (IntelliJ, Homebrew, SBT, etc)
and is beyond the scope of this README.***

`$ scalafmt tracing-and-testing`