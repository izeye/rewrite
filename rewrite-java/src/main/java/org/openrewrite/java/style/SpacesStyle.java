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
public class SpacesStyle implements JavaStyle {
    BeforeParentheses beforeParentheses;
    AroundOperators aroundOperators;
    BeforeLeftBrace beforeLeftBrace;
    BeforeKeywords beforeKeywords;
    Within within;
    TernaryOperator ternaryOperator;
    TypeArguments typeArguments;
    Other other;
    TypeParameters typeParameters;

    @FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
    @Value
    @With
    public static class BeforeParentheses {
        Boolean methodDeclaration;
        Boolean methodCall;
        Boolean ifParentheses;
        Boolean forParentheses;
        Boolean whileParentheses;
        Boolean switchParentheses;
        Boolean tryParentheses;
        Boolean catchParentheses;
        Boolean synchronizedParentheses;
        Boolean annotationParameters;
    }

    @FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
    @Value
    @With
    public static class AroundOperators {
        Boolean assignment;
        Boolean logical;
        Boolean equality;
        Boolean relational;
        Boolean bitwise;
        Boolean additive;
        Boolean multiplicative;
        Boolean shift;
        Boolean unary;
        Boolean lambdaArrow;
        Boolean methodReferenceDoubleColon;
    }

    @FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
    @Value
    @With
    public static class BeforeLeftBrace {
        Boolean classLeftBrace;
        Boolean methodLeftBrace;
        Boolean ifLeftBrace;
        Boolean elseLeftBrace;
        Boolean forLeftBrace;
        Boolean whileLeftBrace;
        Boolean doLeftBrace;
        Boolean switchLeftBrace;
        Boolean tryLeftBrace;
        Boolean catchLeftBrace;
        Boolean finallyLeftBrace;
        Boolean synchronizedLeftBrace;
        Boolean arrayInitializerLeftBrace;
        Boolean annotationArrayInitializerLeftBrace;
    }

    @FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
    @Value
    @With
    public static class BeforeKeywords {
        Boolean elseKeyword;
        Boolean whileKeyword;
        Boolean catchKeyword;
        Boolean finallyKeyword;
    }

    @FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
    @Value
    @With
    public static class Within {
        /**
         * Based on testing in IntelliJ, within codeBraces only affects whether or not a space is inserted
         * into empty interface and class braces, not functions, logical blocks, or lambda braces.
         */
        Boolean codeBraces;

        Boolean brackets;
        Boolean arrayInitializerBraces;
        Boolean emptyArrayInitializerBraces;
        Boolean groupingParentheses;
        Boolean methodDeclarationParentheses;
        Boolean emptyMethodDeclarationParentheses;
        Boolean methodCallParentheses;
        Boolean emptyMethodCallParentheses;
        Boolean ifParentheses;
        Boolean forParentheses;
        Boolean whileParentheses;
        Boolean switchParentheses;
        Boolean tryParentheses;
        Boolean catchParentheses;
        Boolean synchronizedParentheses;
        Boolean typeCastParentheses;
        Boolean annotationParentheses;
        Boolean angleBrackets;

        // Records are a java 14 preview feature (and still a preview language feature in java 15)
        // rewrite does not consult this style setting for now
        Boolean recordHeader;
    }

    @FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
    @Value
    @With
    public static class TernaryOperator {
        Boolean beforeQuestionMark;
        Boolean afterQuestionMark;
        Boolean beforeColon;
        Boolean afterColon;
    }

    @FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
    @Value
    @With
    public static class TypeArguments {
        Boolean afterComma;
        Boolean beforeOpeningAngleBracket;
        Boolean afterClosingAngleBracket;
    }

    @FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
    @Value
    @With
    public static class Other {
        Boolean beforeComma;
        Boolean afterComma;
        Boolean beforeForSemicolon;
        Boolean afterForSemicolon;
        Boolean afterTypeCast;
        Boolean beforeColonInForEach;
        Boolean insideOneLineEnumBraces;
    }

    @FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
    @Value
    @With
    public static class TypeParameters {
        Boolean beforeOpeningAngleBracket;
        Boolean aroundTypeBounds;
    }
}
