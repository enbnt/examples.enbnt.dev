# WORKSPACE
load("@bazel_tools//tools/build_defs/repo:http.bzl", "http_archive")

skylib_version = "1.0.3"

http_archive(
    name = "bazel_skylib",
    sha256 = "1c531376ac7e5a180e0237938a2536de0c54d93f5c278634818e0efc952dd56c",
    type = "tar.gz",
    url = "https://mirror.bazel.build/github.com/bazelbuild/bazel-skylib/releases/download/{}/bazel-skylib-{}.tar.gz".format(skylib_version, skylib_version),
)

# Setup Buildifier
# buildifier is written in Go and hence needs rules_go to be built.
# See https://github.com/bazelbuild/rules_go for the up to date setup instructions.
http_archive(
    name = "io_bazel_rules_go",
    sha256 = "d6b2513456fe2229811da7eb67a444be7785f5323c6708b38d851d2b51e54d83",
    urls = [
        "https://mirror.bazel.build/github.com/bazelbuild/rules_go/releases/download/v0.30.0/rules_go-v0.30.0.zip",
        "https://github.com/bazelbuild/rules_go/releases/download/v0.30.0/rules_go-v0.30.0.zip",
    ],
)

load("@io_bazel_rules_go//go:deps.bzl", "go_rules_dependencies")

go_rules_dependencies()

load("@io_bazel_rules_go//go:deps.bzl", "go_register_toolchains")

go_register_toolchains(version = "1.17.2")

http_archive(
    name = "bazel_gazelle",
    sha256 = "de69a09dc70417580aabf20a28619bb3ef60d038470c7cf8442fafcf627c21cb",
    urls = [
        "https://mirror.bazel.build/github.com/bazelbuild/bazel-gazelle/releases/download/v0.24.0/bazel-gazelle-v0.24.0.tar.gz",
        "https://github.com/bazelbuild/bazel-gazelle/releases/download/v0.24.0/bazel-gazelle-v0.24.0.tar.gz",
    ],
)

load("@bazel_gazelle//:deps.bzl", "gazelle_dependencies")

# If you use WORKSPACE.bazel, use the following line instead of the bare gazelle_dependencies():
# gazelle_dependencies(go_repository_default_config = "@//:WORKSPACE.bazel")
gazelle_dependencies()

http_archive(
    name = "com_google_protobuf",
    sha256 = "3bd7828aa5af4b13b99c191e8b1e884ebfa9ad371b0ce264605d347f135d2568",
    strip_prefix = "protobuf-3.19.4",
    urls = [
        "https://github.com/protocolbuffers/protobuf/archive/v3.19.4.tar.gz",
    ],
)

load("@com_google_protobuf//:protobuf_deps.bzl", "protobuf_deps")

protobuf_deps()

http_archive(
    name = "com_github_bazelbuild_buildtools",
    sha256 = "ae34c344514e08c23e90da0e2d6cb700fcd28e80c02e23e4d5715dddcb42f7b3",
    strip_prefix = "buildtools-4.2.2",
    urls = [
        "https://github.com/bazelbuild/buildtools/archive/refs/tags/4.2.2.tar.gz",
    ],
)

# See https://github.com/bazelbuild/rules_scala/releases for up to date version information.
http_archive(
    name = "io_bazel_rules_scala",
    sha256 = "141a3919b37c80a846796f792dcf6ea7cd6e7b7ca4297603ca961cd22750c951",
    strip_prefix = "rules_scala-5.0.0",
    url = "https://github.com/bazelbuild/rules_scala/archive/refs/tags/v5.0.0.tar.gz",
)

# Stores Scala version and other configuration
# 2.12 is a default version, other versions can be use by passing them explicitly:
# scala_config(scala_version = "2.13.6")
load("@io_bazel_rules_scala//:scala_config.bzl", "scala_config")

scala_config(scala_version = "2.13.6")

load("@io_bazel_rules_scala//scala:scala.bzl", "rules_scala_setup", "rules_scala_toolchain_deps_repositories")

# loads other rules Rules Scala depends on
rules_scala_setup()

# Loads Maven deps like Scala compiler and standard libs. On production projects you should consider
# defining a custom deps toolchains to use your project libs instead
rules_scala_toolchain_deps_repositories(fetch_sources = True)

load("@rules_proto//proto:repositories.bzl", "rules_proto_dependencies", "rules_proto_toolchains")

rules_proto_dependencies()

rules_proto_toolchains()

load("@io_bazel_rules_scala//scala:toolchains.bzl", "scala_register_toolchains")

scala_register_toolchains()

# optional: setup ScalaTest toolchain and dependencies
load("@io_bazel_rules_scala//testing:scalatest.bzl", "scalatest_repositories", "scalatest_toolchain")

scalatest_repositories()

scalatest_toolchain()

# Setup Maven external dependencies
RULES_JVM_EXTERNAL_TAG = "4.5"

RULES_JVM_EXTERNAL_SHA = "b17d7388feb9bfa7f2fa09031b32707df529f26c91ab9e5d909eb1676badd9a6"

http_archive(
    name = "rules_jvm_external",
    sha256 = RULES_JVM_EXTERNAL_SHA,
    strip_prefix = "rules_jvm_external-%s" % RULES_JVM_EXTERNAL_TAG,
    url = "https://github.com/bazelbuild/rules_jvm_external/archive/%s.zip" % RULES_JVM_EXTERNAL_TAG,
)

load("@rules_jvm_external//:repositories.bzl", "rules_jvm_external_deps")

rules_jvm_external_deps()

load("@rules_jvm_external//:setup.bzl", "rules_jvm_external_setup")

rules_jvm_external_setup()


NETTY_TCNATIVE_VERSION = "2.0.53.Final"

load("//third_party/netty:deps_workaround.bzl", "fetch_netty_tcnative_jars")
fetch_netty_tcnative_jars(NETTY_TCNATIVE_VERSION)

load("@rules_jvm_external//:defs.bzl", "maven_install")

twitter_scala_version = "2.13"
twitter_release_version = "22.12.0"

TWITTER_LIBRARY_ARTIFACTS = [
    "com.twitter:finatra-http-server_%s:%s" % (twitter_scala_version, twitter_release_version),
    "com.twitter:finagle-stats_%s:%s" % (twitter_scala_version, twitter_release_version),
    "com.twitter:finagle-stats-core_%s:%s" % (twitter_scala_version, twitter_release_version),
    "com.twitter:inject-slf4j_%s:%s" % (twitter_scala_version, twitter_release_version),
    "com.twitter:inject-logback_%s:%s" % (twitter_scala_version, twitter_release_version),
]

NETTY_TCNATIVE_OVERRIDE_TARGETS = {
    "io.netty:netty-tcnative-boringssl-static": "@//third_party/netty:io_netty_netty_tcnative_boringssl_static",
    "io.netty:netty-tcnative-boringssl-static:osx-x86_64": "@//third_party/netty:io_netty_netty_tcnative_boringssl_static_osx_x86_64",
    "io.netty:netty-tcnative-boringssl-static:osx-aarch_64": "@//third_party/netty:io_netty_netty_tcnative_boringssl_static_osx_aarch_64",
    "io.netty:netty-tcnative-boringssl-static:windows-x86_64": "@//third_party/netty:io_netty_netty_tcnative_boringssl_static_windows_x86_64",
    "io.netty:netty-tcnative-boringssl-static:linux-x86_64": "@//third_party/netty:io_netty_netty_tcnative_boringssl_static_linux_x86_64",
    "io.netty:netty-tcnative-boringssl-static:linux-aarch_64": "@//third_party/netty:io_netty_netty_tcnative_boringssl_static_linux_aarch_64",
}

maven_install(
    artifacts = [
        "junit:junit:4.12",
        "com.google.inject:guice:4.2.3",
        "org.slf4j:slf4j-api:1.7.30",
        "org.slf4j:slf4j-nop:1.7.30",
        "ch.qos.logback:logback-classic:1.2.11",
    ] + TWITTER_LIBRARY_ARTIFACTS,
    repositories = [
        "https://repo1.maven.org/maven2/",
    ],
    override_targets = {} | NETTY_TCNATIVE_OVERRIDE_TARGETS,
)

load("@rules_jvm_external//:specs.bzl", "maven")

# We need a way of accessing test-scoped jar artifacts
maven_install(
    name = "testJars",
    artifacts = [
        maven.artifact(
            group = "com.twitter",
            artifact = "finatra-http-server_%s" % (twitter_scala_version),
            packaging = "jar",
            classifier = "tests",
            version = twitter_release_version,
            testonly = True,
        ),
        maven.artifact(
            group = "com.twitter",
            artifact = "inject-server_%s" % (twitter_scala_version),
            packaging = "jar",
            classifier = "tests",
            version = twitter_release_version,
            testonly = True,
        ),
        maven.artifact(
            group = "com.twitter",
            artifact = "inject-app_%s" % (twitter_scala_version),
            packaging = "jar",
            classifier = "tests",
            version = twitter_release_version,
            testonly = True,
        ),
        maven.artifact(
            group = "com.twitter",
            artifact = "inject-core_%s" % (twitter_scala_version),
            packaging = "jar",
            classifier = "tests",
            version = twitter_release_version,
            testonly = True,
        ),
        maven.artifact(
            group = "com.twitter",
            artifact = "inject-modules_%s" % (twitter_scala_version),
            packaging = "jar",
            classifier = "tests",
            version = twitter_release_version,
            testonly = True,
        ),
    ],
    fetch_sources = True,
    repositories = [
        "https://repo1.maven.org/maven2",
    ],
    override_targets = {} | NETTY_TCNATIVE_OVERRIDE_TARGETS,
)
