# Ion Schema CLI

```
Usage: ion-schema-cli [OPTIONS] COMMAND [ARGS]...

Options:
  --version   Show the version and exit
  -h, --help  Show this message and exit

```


# Building the CLI

The CLI is built during the main Gradle build.  To build it separately, execute:

```shell
./gradlew cli:build
```

After building, distributable archive files are located in the `cli/build/distributions` directory (relative to the
project root).

# Using the CLI

The following command will build any dependencies before starting the CLI.

```shell
./gradlew cli:run -q --args="<command line arguments>"
```

For convenience, `ion-schema-cli` (in the root directory of the project) wraps this command, so you can also
run the CLI without having to quote any arguments.
```shell
./ion-schema-cli --help
```

# Commands

TODO—this section will contain details about the ion-schema-cli subcommands once they have been added.
