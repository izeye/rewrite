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
package org.openrewrite.yaml;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.yaml.search.FindIndentYamlVisitor;
import org.openrewrite.yaml.tree.Yaml;

import java.util.ArrayList;
import java.util.List;

public class CoalesceProperties extends Recipe {

    @Override
    public String getDisplayName() {
        return "Coalesce YAML properties";
    }

    @Override
    public String getDescription() {
        return "Simplify nested map hierarchies into their simplest dot separated property form, i.e. as Spring Boot interprets " +
                "application.yml files.";
    }

    @Override
    protected TreeVisitor<?, ExecutionContext> getVisitor() {
        return new CoalescePropertiesVisitor<>();
    }

    public static class CoalescePropertiesVisitor<P> extends YamlVisitor<P> {
        public CoalescePropertiesVisitor() {
        }

        private final FindIndentYamlVisitor<P> findIndent = new FindIndentYamlVisitor<>(0);

        @Override
        public Yaml visitDocument(Yaml.Document document, P p) {
            findIndent.visit(document, p);
            return super.visitDocument(document, p);
        }

        @Override
        public Yaml visitMapping(Yaml.Mapping mapping, P p) {
            Yaml.Mapping m = (Yaml.Mapping) super.visitMapping(mapping, p);

            boolean changed = false;
            List<Yaml.Mapping.Entry> entries = new ArrayList<>();

            for (Yaml.Mapping.Entry entry : m.getEntries()) {
                if (entry.getValue() instanceof Yaml.Mapping) {
                    Yaml.Mapping valueMapping = (Yaml.Mapping) entry.getValue();
                    if (valueMapping.getEntries().size() == 1) {
                        Yaml.Mapping.Entry subEntry = valueMapping.getEntries().iterator().next();
                        Yaml.Scalar coalescedKey = entry.getKey().withValue(entry.getKey().getValue() + "." + subEntry.getKey().getValue());

                        entries.add(entry.withKey(coalescedKey)
                                .withValue(subEntry.getValue()));

                        int indentToUse = findIndent.getMostCommonIndent() > 0 ?
                                findIndent.getMostCommonIndent() : 4;
                        doAfterVisit(new ShiftFormatLeftVisitor<>(subEntry.getValue(), indentToUse));

                        changed = true;
                    } else {
                        entries.add(entry);
                    }
                } else {
                    entries.add(entry);
                }
            }

            if (changed) {
                m = m.withEntries(entries);
            }

            return m;
        }
    }

}
