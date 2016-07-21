package com.netflix.java.refactor

import com.netflix.java.refactor.ast.AstParser
import eu.infomas.annotation.AnnotationDetector
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.net.URLClassLoader
import java.util.*

data class SourceSet(val allSourceFiles: Iterable<File>, val classpath: Iterable<File>) {
    val logger: Logger = LoggerFactory.getLogger(SourceSet::class.java)
    
    private val parser = AstParser(classpath)
    private val compilationUnits by lazy { 
        parser.parseFiles(allSourceFiles.toList()).map { CompilationUnit(it, parser) }
    }
    
    fun allJava() = compilationUnits.map { cu -> JavaSource(cu) }

    fun findScanners(): Iterable<JavaSourceScanner<*>> {
        val scanners = ArrayList<JavaSourceScanner<*>>()
        val classLoader = URLClassLoader(classpath.map { it.toURI().toURL() }.toTypedArray(), javaClass.classLoader)

        val reporter = object: AnnotationDetector.TypeReporter {
            override fun annotations() = arrayOf(AutoRefactor::class.java)

            override fun reportTypeAnnotation(annotation: Class<out Annotation>, className: String) {
                val clazz = Class.forName(className, false, classLoader)
                val refactor = clazz.getAnnotation(AutoRefactor::class.java)
                
                try {
                    scanners.add(clazz.newInstance() as JavaSourceScanner<*>)
                } catch(ignored: ReflectiveOperationException) {
                    logger.warn("Unable to construct @AutoRefactor with id '${refactor.value}'. It must extend either JavaSourceScanner or JavaSourceVisitor.")
                }
            }
        }
        
        AnnotationDetector(reporter).detect(*classpath.toList().toTypedArray())
        return scanners
    }
}
