/*
 * Copyright 2020 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * https://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openrewrite.maven;

import lombok.EqualsAndHashCode;
import lombok.Value;
import org.openrewrite.*;
import org.openrewrite.internal.lang.Nullable;
import org.openrewrite.maven.tree.Maven;
import org.openrewrite.maven.tree.Pom;
import org.openrewrite.maven.tree.Scope;
import org.openrewrite.xml.RemoveContentVisitor;
import org.openrewrite.xml.tree.Xml;

import java.util.List;

import static java.util.stream.Collectors.toList;

@Value
@EqualsAndHashCode(callSuper = true)
public class RemoveDependency extends Recipe {

    @Option(displayName = "Group",
            description = "The first part of a dependency coordinate 'com.google.guava:guava:VERSION'.",
            example = "com.google.guava")
    String groupId;

    @Option(displayName = "Artifact",
            description = "The second part of a dependency coordinate 'com.google.guava:guava:VERSION'.",
            example = "guava")
    String artifactId;

    @Option(displayName = "Scope",
            description = "Only remove dependencies if they are in this scope. If 'runtime', this will" +
                    "also remove dependencies in the 'compile' scope because 'compile' dependencies are part of the runtime dependency set",
            valid = {"compile", "test", "runtime", "provided"},
            required = false)
    @Nullable
    String scope;

    @Override
    public String getDisplayName() {
        return "Remove Maven dependency";
    }

    @Override
    public String getDescription() {
        return "Removes a single dependency from the <dependencies> section of the pom.xml.";
    }

    @Override
    protected TreeVisitor<?, ExecutionContext> getVisitor() {
        return new RemoveDependencyVisitor();
    }

    @Override
    public Validated validate() {
        return super.validate().and(Validated.test("scope", "Scope must be one of compile, runtime, test, or provided",
                scope, s -> !Scope.Invalid.equals(Scope.fromName(s))));
    }

    private class RemoveDependencyVisitor extends MavenVisitor {

        @Override
        public Xml visitTag(Xml.Tag tag, ExecutionContext ctx) {
            if (isDependencyTag(groupId, artifactId) && (scope == null ||
                    tag.getChildValue("scope").orElse("compile").equals(scope))) {
                doAfterVisit(new RemoveContentVisitor<>(tag, true));
            }

            return super.visitTag(tag, ctx);
        }

        @Override
        public Maven visitMaven(Maven maven, ExecutionContext ctx) {
            model = maven.getModel();
            Maven m = super.visitMaven(maven, ctx);
            List<Pom.Dependency> dependencies = model.getDependencies().stream()
                    .filter(dep -> !(dep.getArtifactId().equals(artifactId) && dep.getGroupId().equals(groupId)))
                    .collect(toList());

            return m.withModel(model.withDependencies(dependencies));
        }
    }
}
