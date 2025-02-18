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
package org.openrewrite.java.tree

import org.junit.jupiter.api.Test
import org.openrewrite.Issue
import org.openrewrite.java.JavaParser
import org.openrewrite.java.JavaTreeTest
import org.openrewrite.java.JavaTreeTest.NestingLevel.CompilationUnit

interface AnnotationTest : JavaTreeTest {

    @Test
    fun annotationWithDefaultArgument(jp: JavaParser) = assertParsePrintAndProcess(
        jp, CompilationUnit, """
           @SuppressWarnings("ALL")
           public class A {}
        """
    )

    @Test
    fun annotationWithArgument(jp: JavaParser) = assertParsePrintAndProcess(
        jp, CompilationUnit, """
           @SuppressWarnings(value = "ALL")
           public class A {}
        """
    )

    @Test
    fun preserveOptionalEmptyParentheses(jp: JavaParser) = assertParsePrintAndProcess(
        jp, CompilationUnit, """
           @Deprecated ( )
           public class A {}
        """
    )

    @Test
    fun newArrayArgument(jp: JavaParser) = assertParsePrintAndProcess(
        jp, CompilationUnit, """
            import java.lang.annotation.Target;
            import static java.lang.annotation.ElementType.*;

            @Target({ FIELD, PARAMETER })
            public @interface Annotation {}
        """
    )

    @Test
    fun annotationsInManyLocations(jp: JavaParser) = assertParsePrintAndProcess(
            jp, CompilationUnit, """
                import java.lang.annotation.*;
                @Ho
                public @Ho final @Ho class Test {
                    @Ho private @Ho transient @Ho String s;
                    @Ho
                    public @Ho final @Ho <T> @Ho T merryChristmas() {
                        return null;
                    }
                    @Ho
                    public @Ho Test() {
                    }
                }
                @Target({ElementType.TYPE_USE, ElementType.TYPE, ElementType.FIELD})
                @interface Hos {
                    Ho[] value();
                }
                @Target({ElementType.TYPE_USE, ElementType.TYPE, ElementType.FIELD})
                @Repeatable(Hos.class)
                @interface Ho {
                }
            """
    )

    @Test
    fun multipleAnnotations(jp: JavaParser) = assertParsePrintAndProcess(
            jp, CompilationUnit, """
                import java.lang.annotation.*;
                @B
                @C
                public class A {
                }
                @Target({ElementType.TYPE_USE, ElementType.TYPE, ElementType.FIELD})
                @interface B {
                }
                @Target({ElementType.TYPE_USE, ElementType.TYPE, ElementType.FIELD})
                @interface C {
                }
            """
    )

    @Issue("https://github.com/openrewrite/rewrite/issues/377")
    @Test
    fun typeParameterAnnotations(jp: JavaParser) = assertParsePrintAndProcess(
        jp, CompilationUnit, """
            import java.util.List;
            import java.lang.annotation.*;
            class TypeAnnotationTest {
                List<@A ? extends @A String> list;
           
                @Target({ ElementType.FIELD, ElementType.TYPE_USE, ElementType.TYPE_PARAMETER })
                private static @interface A {
                }
            }
        """
    )

    @Test
    fun annotationsWithComments(jp: JavaParser) = assertParsePrintAndProcess(
            jp, CompilationUnit, """
                import java.lang.annotation.*;
                @Yo
                // doc
                @Ho
                public @Yo /* grumpy */ @Ho final @Yo
                // happy
                @Ho class Test {
                    @Yo /* sleepy */ @Ho private @Yo /* bashful */ @Ho transient @Yo /* sneezy */ @Ho String s;
                    @Yo /* dopey */ @Ho
                    public @Yo /* evil queen */ @Ho final @Yo /* mirror */ @Ho <T> @Yo /* apple */ @Ho T itsOffToWorkWeGo() {
                        return null;
                    }
                    @Yo /* snow white */ @Ho
                    public @Yo /* prince */ @Ho Test() {
                    }
                }
                @Target({ElementType.TYPE_USE, ElementType.TYPE, ElementType.FIELD})
                @interface Hos {
                    Ho[] value();
                }
                @Target({ElementType.TYPE_USE, ElementType.TYPE, ElementType.FIELD})
                @Repeatable(Hos.class)
                @interface Ho {
                }
                @Target({ElementType.TYPE_USE, ElementType.TYPE, ElementType.FIELD})
                @interface Yos {
                    Yo[] value();
                }
                @Target({ElementType.TYPE_USE, ElementType.TYPE, ElementType.FIELD})
                @Repeatable(Yos.class)
                @interface Yo {
                }
            """
    )
}
