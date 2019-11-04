setup() {
    . shellmock
    skipIfNot "$BATS_TEST_DESCRIPTION"
    shellmock_clean

    echo "build_image: toolchain:test" > shippable.yml
    touch /usr/local/bin/docker
    chmod +x /usr/local/bin/docker
}

teardown() {
    if [ -z "$TEST_FUNCTION" ];then
        shellmock_clean
    fi

    rm -rf shippable.yml /usr/local/bin/docker
}

@test "developer_exists_run_developer_tag" {
    shellmock_expect docker --status 0 --match 'manifest inspect toolchain:developer-test'
    shellmock_expect docker --status 0 --type regex --match 'run.* -t toolchain:developer-test'
    run ./tools/testenv
    [ "$status" = "0" ]
    shellmock_verify
    [[ "${capture[1]}" =~ docker-stub.manifest.*toolchain:developer-test ]]
    [[ "${capture[2]}" =~ docker-stub.run.--rm.*toolchain:developer-test ]]
}

@test "developer_does_not_exists_run_normal_tag" {
    shellmock_expect docker --status 1 --match 'manifest inspect toolchain:developer-test'
    shellmock_expect docker --status 0 --type regex --match 'run.* -t toolchain:test'
    run ./tools/testenv
    [ "$status" = "0" ]
    shellmock_verify
    [[ "${capture[1]}" =~ docker-stub.manifest.*toolchain:developer-test ]]
    [[ "${capture[2]}" =~ docker-stub.run.--rm.*toolchain:test ]]
}

