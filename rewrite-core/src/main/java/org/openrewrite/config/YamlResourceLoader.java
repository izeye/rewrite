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
package org.openrewrite.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.cfg.ConstructorDetector;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.module.kotlin.KotlinModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import org.openrewrite.Recipe;
import org.openrewrite.RecipeException;
import org.openrewrite.internal.PropertyPlaceholderHelper;
import org.openrewrite.internal.RecipeIntrospectionUtils;
import org.openrewrite.internal.lang.Nullable;
import org.openrewrite.style.NamedStyles;
import org.openrewrite.style.Style;
import org.yaml.snakeyaml.Yaml;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static java.util.stream.Collectors.toList;
import static org.openrewrite.Validated.invalid;

public class YamlResourceLoader implements ResourceLoader {
    int refCount = 0;

    private static final ObjectMapper mapper = JsonMapper.builder()
            .constructorDetector(ConstructorDetector.USE_PROPERTIES_BASED)
            .build()
            .registerModule(new ParameterNamesModule())
            .registerModule(new KotlinModule())
            .enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES)
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

    private static final PropertyPlaceholderHelper propertyPlaceholderHelper =
            new PropertyPlaceholderHelper("${", "}", ":");

    private final URI source;
    private final String yamlSource;

    private enum ResourceType {
        Recipe("specs.openrewrite.org/v1beta/recipe"),
        Style("specs.openrewrite.org/v1beta/style");

        private final String spec;

        ResourceType(String spec) {
            this.spec = spec;
        }

        public String getSpec() {
            return spec;
        }

        @Nullable
        public static ResourceType fromSpec(@Nullable String spec) {
            return Arrays.stream(values())
                    .filter(type -> type.getSpec().equals(spec))
                    .findAny()
                    .orElse(null);
        }
    }

    public YamlResourceLoader(InputStream yamlInput, URI source, Properties properties) throws UncheckedIOException {
        this.source = source;

        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            int nRead;
            byte[] data = new byte[1024];
            while ((nRead = yamlInput.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }
            this.yamlSource = propertyPlaceholderHelper.replacePlaceholders(
                    new String(buffer.toByteArray(), StandardCharsets.UTF_8), properties);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private Collection<Map<String, Object>> loadResources(ResourceType resourceType) {
        Collection<Map<String, Object>> resources = new ArrayList<>();

        Yaml yaml = new Yaml();
        for (Object resource : yaml.loadAll(yamlSource)) {
            if (resource instanceof Map) {
                @SuppressWarnings("unchecked") Map<String, Object> resourceMap = (Map<String, Object>) resource;
                if (resourceType.equals(ResourceType.fromSpec((String) resourceMap.get("type")))) {
                    resources.add(resourceMap);
                }
            }
        }

        return resources;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Collection<Recipe> listRecipes() {
        return loadResources(ResourceType.Recipe).stream()
                .filter(r -> r.containsKey("name"))
                .map(r -> {
                    String name = (String) r.get("name");
                    String displayName = (String) r.get("displayName");
                    if (displayName == null) {
                        displayName = name;
                    }
                    String description = (String) r.get("description");
                    Set<String> tags = Collections.emptySet();
                    List<String> rawTags = (List<String>) r.get("tags");
                    if (rawTags != null) {
                        tags = new HashSet<>(rawTags);
                    }
                    DeclarativeRecipe recipe = new DeclarativeRecipe(name, displayName, description, tags, source);
                    List<Object> recipeList = (List<Object>) r.get("recipeList");
                    if (recipeList == null) {
                        throw new RecipeException("Invalid Recipe [" + name + "] recipeList is null");
                    }
                    for (int i = 0; i < recipeList.size(); i++) {
                        Object next = recipeList.get(i);
                        if (next instanceof String) {
                            recipe.doNext((String) next);
                        } else if (next instanceof Map) {
                            Map.Entry<String, Object> nameAndConfig = ((Map<String, Object>) next).entrySet().iterator().next();
                            try {
                                Map<Object, Object> withJsonType = new HashMap<>((Map<String, Object>) nameAndConfig.getValue());
                                withJsonType.put("@c", nameAndConfig.getKey());
                                recipe.doNext(mapper.convertValue(withJsonType, Recipe.class));
                            } catch (Exception e) {
                                // TODO error handling?
                                e.printStackTrace();
                            }
                        } else {
                            recipe.addValidation(invalid(
                                    name + ".recipeList[" + i + "] (in " + source + ")",
                                    next,
                                    "is an object type that isn't recognized as a recipe.",
                                    null));
                        }
                    }
                    return recipe;
                })
                .collect(toList());
    }

    @Override
    public Collection<RecipeDescriptor> listRecipeDescriptors() {
        Collection<Recipe> recipes = listRecipes();
        List<RecipeDescriptor> recipeDescriptors = new ArrayList<>();
        for (Recipe recipe : recipes) {
            DeclarativeRecipe declarativeRecipe = (DeclarativeRecipe) recipe;
            declarativeRecipe.initialize(recipes);
            recipeDescriptors.add(RecipeIntrospectionUtils.recipeDescriptorFromDeclarativeRecipe(declarativeRecipe));
        }
        return recipeDescriptors;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Collection<NamedStyles> listStyles() {
        return loadResources(ResourceType.Style).stream()
                .filter(r -> r.containsKey("name"))
                .map(s -> {
                    List<Style> styles = new ArrayList<>();
                    String name = (String) s.get("name");
                    String displayName = (String) s.get("displayName");
                    if (displayName == null) {
                        displayName = name;
                    }
                    String description = (String) s.get("description");
                    Set<String> tags = Collections.emptySet();
                    List<String> rawTags = (List<String>) s.get("tags");
                    if (rawTags != null) {
                        tags = new HashSet<>(rawTags);
                    }
                    DeclarativeNamedStyles namedStyles = new DeclarativeNamedStyles(name, displayName, description, tags, styles);
                    List<Object> styleConfigs = (List<Object>) s.get("styleConfigs");
                    if (styleConfigs != null) {
                        for (int i = 0; i < styleConfigs.size(); i++) {
                            Object next = styleConfigs.get(i);
                            if (next instanceof String) {
                                String styleClassName = (String) next;
                                try {
                                    styles.add((Style) Class.forName(styleClassName).getDeclaredConstructor().newInstance());
                                } catch (Exception e) {
                                    namedStyles.addValidation(invalid(
                                            name + ".styleConfigs[" + i + "] (in " + source + ")",
                                            next,
                                            "is a style that cannot be constructed.",
                                            e));
                                }
                            } else if (next instanceof Map) {
                                Map.Entry<String, Object> nameAndConfig = ((Map<String, Object>) next).entrySet().iterator().next();
                                try {
                                    Map<Object, Object> withJsonType = new HashMap<>((Map<String, Object>) nameAndConfig.getValue());
                                    withJsonType.put("@c", nameAndConfig.getKey());
                                    withJsonType.put("@ref", refCount++);
                                    Style e = mapper.convertValue(withJsonType, Style.class);
                                    styles.add(e);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            } else {
                                namedStyles.addValidation(invalid(
                                        name + ".styleConfigs[" + i + "] (in " + source + ")",
                                        next,
                                        "is an object type that isn't recognized as a style.",
                                        null));
                            }
                        }
                    }
                    return namedStyles;
                })
                .collect(toList());
    }
}
