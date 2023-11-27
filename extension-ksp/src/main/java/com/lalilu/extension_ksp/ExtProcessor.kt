package com.lalilu.extension_ksp

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.validate
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.writeTo

class ExtProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger
) : SymbolProcessor {
    companion object {
        private val listType = List::class.parameterizedBy(String::class)
        const val GENERATE_PACKAGE_NAME = "lalilu.extension_ksp"
        const val GENERATE_FILE_NAME = "ExtensionsConstants"

        // 需手动修改与extension_core的保持一致
        private const val TARGET_ANNOTATION = "com.lalilu.extension_core.Ext"
    }

    private val classNames: HashSet<String> = LinkedHashSet()

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val symbols = resolver.getSymbolsWithAnnotation(TARGET_ANNOTATION)
            .filterIsInstance<KSClassDeclaration>()
            .toList()

        if (symbols.isEmpty()) return emptyList()

        classNames.addAll(symbols.map { it.toClassName().toString() })

        // 筛选返回不可解析的symbols
        return symbols.filter { !it.validate() }.toList()
    }

    override fun finish() {
        super.finish()
        val packageName = GENERATE_PACKAGE_NAME
        val fileName = GENERATE_FILE_NAME
        val listValue = "listOf(${classNames.joinToString(",") { "\"$it\"" }})"

        val function = FunSpec.builder("getClasses")
            .addKdoc("Get all extensions' className from this library")
            .addCode("return $listValue")
            .returns(listType)
            .build()

        val classType = TypeSpec.classBuilder(fileName)
            .addFunction(function)
            .build()

        FileSpec.builder(packageName, fileName)
            .addType(classType)
            .build()
            .writeTo(codeGenerator, true)
    }
}