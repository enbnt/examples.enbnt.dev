# This is a workaround to https://github.com/bazelbuild/rules_jvm_external/issues/704
# in order to correctly gathering Netty TCNative dependencies. This result is inspired by
# @ddelnano's work in https://github.com/pixie-io/pixie, as well as @dmivankov's workarounds
# listed in the above linked issue.

load("@bazel_tools//tools/build_defs/repo:http.bzl", "http_jar")

def fetch_netty_tcnative_jars(version):
    http_jar(
        name = "netty_tcnative_boringssl_static",
        url = "https://repo1.maven.org/maven2/io/netty/netty-tcnative-boringssl-static/%s/netty-tcnative-boringssl-static-%s.jar" % (version, version),
        sha256 = "0d8d16adadb19e065a5ac05738f0c2503c685cf3edafba14f7a1c246aafa09ef",
    )

    http_jar(
        name = "netty_tcnative_boringssl_static_linux_x86_64",
        url = "https://repo1.maven.org/maven2/io/netty/netty-tcnative-boringssl-static/%s/netty-tcnative-boringssl-static-%s-linux-x86_64.jar" % (version, version),
        sha256 = "83e3357da5567a93cb5ff6cb807d70573359d7ef9676fa8169405121bae05723",
    )

    http_jar(
        name = "netty_tcnative_boringssl_static_linux_aarch_64",
        url = "https://repo1.maven.org/maven2/io/netty/netty-tcnative-boringssl-static/%s/netty-tcnative-boringssl-static-%s-linux-aarch_64.jar" % (version, version),
        sha256 = "ce3d12e3ae2ad0b9225df347b55715b0ad24342d0195bb238f5e3f60b3f6b868",
    )

    http_jar(
        name = "netty_tcnative_boringssl_static_osx_aarch_64",
        url = "https://repo1.maven.org/maven2/io/netty/netty-tcnative-boringssl-static/%s/netty-tcnative-boringssl-static-%s-osx-aarch_64.jar" % (version, version),
        sha256 = "9d9cf706e89b81e07e9983a06b8ff5348a658a89752ec6f426925cc645e54b54",
    )

    http_jar(
        name = "netty_tcnative_boringssl_static_osx_x86_64",
        url = "https://repo1.maven.org/maven2/io/netty/netty-tcnative-boringssl-static/%s/netty-tcnative-boringssl-static-%s-osx-x86_64.jar" % (version, version),
        sha256 = "51b43e8e178e94de9ec27017e03173dfb19bd1aaf15677a90347188cf60e799b",
    )

    http_jar(
        name = "netty_tcnative_boringssl_static_windows_x86_64",
        url = "https://repo1.maven.org/maven2/io/netty/netty-tcnative-boringssl-static/%s/netty-tcnative-boringssl-static-%s-windows-x86_64.jar" % (version, version),
        sha256 = "e348fcfab697ffc30bacc083e9393e5b6bd398029acac1a570e53267f89804a3",
    )
