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
import org.openrewrite.Incubating;
import org.openrewrite.java.JavaStyle;

@Incubating(since = "7.0.0")
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@Value
@With
public class UnnecessaryParenthesesStyle implements JavaStyle {
    Boolean expr;
    Boolean ident;
    Boolean numDouble;
    Boolean numFloat;
    Boolean numInt;
    Boolean numLong;
    Boolean stringLiteral;
    Boolean literalNull;
    Boolean literalFalse;
    Boolean literalTrue;
    Boolean assign;
    Boolean bandAssign;
    Boolean borAssign;
    Boolean bsrAssign;
    Boolean bxorAssign;
    Boolean divAssign;
    Boolean minusAssign;
    Boolean modAssign;
    Boolean plusAssign;
    Boolean slAssign;
    Boolean srAssign;
    Boolean starAssign;
    Boolean lambda;
}
