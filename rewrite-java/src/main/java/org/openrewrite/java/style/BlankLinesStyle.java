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
package org.openrewrite.java.style;

import lombok.AccessLevel;
import lombok.Value;
import lombok.With;
import lombok.experimental.FieldDefaults;
import org.openrewrite.java.JavaStyle;

@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@Value
@With
public class BlankLinesStyle implements JavaStyle {
    KeepMaximum keepMaximum;
    Minimum minimum;

    @FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
    @Value
    @With
    public static class KeepMaximum {
        Integer inDeclarations;
        Integer inCode;
        Integer beforeEndOfBlock;
        Integer betweenHeaderAndPackage;
    }

    @FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
    @Value
    @With
    public static class Minimum {
        Integer beforePackage;
        Integer afterPackage;
        Integer beforeImports;
        Integer afterImports;
        Integer aroundClass;
        Integer afterClassHeader;
        Integer beforeClassEnd;
        Integer afterAnonymousClassHeader;
        Integer aroundFieldInInterface;
        Integer aroundField;
        Integer aroundMethodInInterface;
        Integer aroundMethod;
        Integer beforeMethodBody;
        Integer aroundInitializer;
    }
}
