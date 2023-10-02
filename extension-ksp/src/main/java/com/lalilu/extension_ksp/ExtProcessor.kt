package com.lalilu.extension_ksp

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
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
        val GENERATE_CLASS_NAME = "$GENERATE_PACKAGE_NAME.$GENERATE_FILE_NAME"
    }

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val symbols = resolver.getSymbolsWithAnnotation(Ext::class.qualifiedName!!).toList()
            .filterIsInstance<KSClassDeclaration>()

        if (symbols.isEmpty()) return emptyList()

        val packageName = GENERATE_PACKAGE_NAME
        val fileName = GENERATE_FILE_NAME
        val classNames = symbols.map { it.toClassName() }

        val propertyValue = "listOf(${classNames.joinToString(",") { "\"$it\"" }})"
        val property = PropertySpec.builder("classes", listType)
            .initializer(propertyValue)
            .build()
        val classType = TypeSpec.classBuilder(fileName)
            .addProperty(property)
            .build()

        FileSpec.builder(packageName, fileName)
            .addType(classType)
            .build()
            .writeTo(codeGenerator, true)
        return emptyList()
    }
}