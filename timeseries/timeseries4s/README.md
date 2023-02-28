# timeseries4s

This project is in progress and not meant to be used
or read externally. This README will be updated with
more information and documentation when you should
take a deeper look.

***Note: Installing [Bazel](https://www.bazel.io) is a pre-req for running this project locally.***

**All command examples are run from the root directory of `examples.enbnt.dev` repo.**

## Building

`$ bazel build //timeseries/timeseries4s/...`

## Testing

`$ bazel test --test_output=errors //timeseries/timeseries4s/...`

## Lint

***Note: This requires installing [scalafmt](https://scalameta.org/scalafmt/docs/installation.html)
in order to work. There are various integrations (IntelliJ, Homebrew, SBT, etc)
and is beyond the scope of this README.***

`$ scalafmt timeseries/timeseries4s`