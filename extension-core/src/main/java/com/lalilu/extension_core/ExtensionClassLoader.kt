package com.lalilu.extension_core

import dalvik.system.PathClassLoader

class ExtensionClassLoader(
    dexPath: String,
    parent: ClassLoader,
) : PathClassLoader(dexPath, null, parent) {
    override fun loadClass(name: String, resolve: Boolean): Class<*> =
        runCatching { findClass(name) }.getOrElse {
            if (name == Constants.EXTENSION_SOURCES_CLASS) throw ClassNotFoundException("${Constants.EXTENSION_SOURCES_CLASS} not exist in the Extension, try load classList by getExtensionListFromMeta()")
            else super.loadClass(name, resolve)
        }
}