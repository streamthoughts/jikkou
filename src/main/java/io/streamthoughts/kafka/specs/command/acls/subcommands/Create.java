package io.streamthoughts.kafka.specs.command.acls.subcommands;


import io.streamthoughts.kafka.specs.OperationResult;
import io.streamthoughts.kafka.specs.acl.AclRule;
import io.streamthoughts.kafka.specs.command.acls.AclsCommand;
import io.streamthoughts.kafka.specs.operation.CreateAclsOperation;
import io.streamthoughts.kafka.specs.operation.ResourceOperationOptions;
import io.streamthoughts.kafka.specs.resources.ResourcesIterable;
import org.apache.kafka.clients.admin.AdminClient;
import picocli.CommandLine.Command;

import java.util.Collection;
import java.util.List;

@Command(name = "create",
         description = "Create all ACLs on remote cluster."
)
public class Create extends AclsCommand.Base {

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<OperationResult<AclRule>> execute(final List<AclRule> rules,
                                                        final AdminClient client) {
        return new CreateAclsOperation()
                .execute(client, new ResourcesIterable<>(rules), new ResourceOperationOptions() {
                });
    }
}
