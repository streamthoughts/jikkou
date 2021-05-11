package io.streamthoughts.kafka.specs.command;

import io.streamthoughts.kafka.specs.KafkaSpecs;
import io.streamthoughts.kafka.specs.internal.AdminClientUtils;
import org.apache.kafka.clients.admin.AdminClient;
import picocli.CommandLine;
import picocli.CommandLine.ParentCommand;

import java.util.function.Function;

public class WithAdminClientCommand {

    @ParentCommand
    private KafkaSpecs specs;

    public Integer withAdminClient(final Function<AdminClient, Integer> function) {
        try (AdminClient client = AdminClientUtils.newAdminClient(
                specs.options.bootstrapServer,
                specs.options.clientCommandConfig,
                specs.options.clientCommandProperties
        )) {
            return function.apply(client);
        }
    }

}
