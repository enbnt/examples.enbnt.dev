# Tracing And Testing

Example project for Distributed Tracing and Testing Blog Post at
[enbnt.dev](https://www.enbnt.dev). Please note that I am not a
Bazel build expert and no guarantees of efficiency or accuracy are
made as a result of using these example projects. Pull requests are
welcome!

*Note: Installing [Bazel](https://www.bazel.io) is a pre-req for running this project locally.*

## Building

`$ bazel build //tracing-and-testing/...`

## Testing

`$ bazel test //tracing-and-testing/...`

## Run the Server

`$ bazel run //tracing-and-testing/src/main/scala/dev/enbnt/example:bin`