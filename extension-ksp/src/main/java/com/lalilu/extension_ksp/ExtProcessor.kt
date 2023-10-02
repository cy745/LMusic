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
    private val listType = List::class.parameterizedBy(String::class)

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val symbols = resolver.getSymbolsWithAnnotation(Ext::class.qualifiedName!!).toList()
            .filterIsInstance<KSClassDeclaration>()

        if (symbols.isEmpty()) return emptyList()

        val packageName = "com.lalilu.extension_ksp"
        val fileName = "LMusicExtensions"
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