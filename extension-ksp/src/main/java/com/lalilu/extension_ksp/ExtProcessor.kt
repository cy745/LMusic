package com.lalilu.extension_ksp

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
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
        val GENERATE_PACKAGE_NAME = "lalilu.extension_ksp"
        val GENERATE_FILE_NAME = "ExtensionsConstants"

        // 需手动修改与extension_core的保持一致
        private val TARGET_ANNOTATION = "com.lalilu.extension_core.Ext"
    }

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val symbols = resolver.getSymbolsWithAnnotation(TARGET_ANNOTATION).toList()
            .filterIsInstance<KSClassDeclaration>()

        if (symbols.isEmpty()) return emptyList()

        val packageName = GENERATE_PACKAGE_NAME
        val fileName = GENERATE_FILE_NAME
        val classNames = symbols.map { it.toClassName() }

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
        return emptyList()
    }
}