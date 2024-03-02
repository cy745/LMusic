package com.lalilu.lmusic.ui.shadertoy

import android.opengl.Matrix

interface ScaleType {
    fun updateMatrix(
        mvpMatrix: FloatArray,
        modelMatrix: FloatArray,
        viewMatrix: FloatArray,
        projectionMatrix: FloatArray,
        screenWidth: Int,
        screenHeight: Int,
        textureWidth: Int,
        textureHeight: Int
    )
}

sealed class StaticScaleType : ScaleType {
    protected fun updateMvpMatrix(
        mvpMatrix: FloatArray,
        modelMatrix: FloatArray,
        viewMatrix: FloatArray,
        projectionMatrix: FloatArray,
    ) {
        Matrix.setLookAtM(viewMatrix, 0, 0f, 0f, 7.0f, 0f, 0f, 0f, 0f, 1.0f, 0.0f)
        Matrix.multiplyMM(mvpMatrix, 0, viewMatrix, 0, modelMatrix, 0)
        Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, mvpMatrix, 0)
    }

    data object Crop : StaticScaleType() {
        override fun updateMatrix(
            mvpMatrix: FloatArray,
            modelMatrix: FloatArray,
            viewMatrix: FloatArray,
            projectionMatrix: FloatArray,
            screenWidth: Int,
            screenHeight: Int,
            textureWidth: Int,
            textureHeight: Int
        ) {
            val textureAspectRatio = textureWidth.toFloat() / textureHeight.toFloat()
            val screenAspectRatio = screenWidth.toFloat() / screenHeight.toFloat()

            if (screenWidth > screenHeight) {
                if (textureAspectRatio > screenAspectRatio) {
                    Matrix.orthoM(
                        projectionMatrix, 0,
                        -screenAspectRatio * textureAspectRatio,
                        screenAspectRatio * textureAspectRatio,
                        -1f, 1f, 3f, 7f
                    )
                } else {
                    Matrix.orthoM(
                        projectionMatrix, 0,
                        -screenAspectRatio / textureAspectRatio,
                        screenAspectRatio / textureAspectRatio,
                        -1f, 1f, 3f, 7f
                    )
                }
            } else {
                if (textureAspectRatio > screenAspectRatio) {
                    Matrix.orthoM(
                        projectionMatrix, 0,
                        -screenAspectRatio / textureAspectRatio,
                        screenAspectRatio / textureAspectRatio,
                        -1f, 1f, 3f, 7f
                    )
                } else {
                    Matrix.orthoM(
                        projectionMatrix, 0, -1f, 1f,
                        -textureAspectRatio / screenAspectRatio,
                        textureAspectRatio / screenAspectRatio,
                        3f, 7f
                    )
                }
            }
            updateMvpMatrix(mvpMatrix, modelMatrix, viewMatrix, projectionMatrix)
        }
    }

    data object Center : StaticScaleType() {
        override fun updateMatrix(
            mvpMatrix: FloatArray,
            modelMatrix: FloatArray,
            viewMatrix: FloatArray,
            projectionMatrix: FloatArray,
            screenWidth: Int,
            screenHeight: Int,
            textureWidth: Int,
            textureHeight: Int
        ) {
            val textureAspectRatio = textureWidth.toFloat() / textureHeight.toFloat()
            val screenAspectRatio = screenWidth.toFloat() / screenHeight.toFloat()

            if (screenWidth > screenHeight) {
                if (textureAspectRatio > screenAspectRatio) {
                    Matrix.orthoM(
                        projectionMatrix, 0,
                        -screenAspectRatio * textureAspectRatio,
                        screenAspectRatio * textureAspectRatio,
                        -1f, 1f, 3f, 7f
                    )
                } else {
                    Matrix.orthoM(
                        projectionMatrix, 0,
                        -screenAspectRatio / textureAspectRatio,
                        screenAspectRatio / textureAspectRatio,
                        -1f, 1f, 3f, 7f
                    )
                }
            } else {
                if (textureAspectRatio > screenAspectRatio) {
                    Matrix.orthoM(
                        projectionMatrix, 0, -1f, 1f,
                        -1 / screenAspectRatio * textureAspectRatio,
                        1 / screenAspectRatio * textureAspectRatio,
                        3f, 7f
                    )
                } else {
                    Matrix.orthoM(
                        projectionMatrix, 0, -1f, 1f,
                        -textureAspectRatio / screenAspectRatio,
                        textureAspectRatio / screenAspectRatio,
                        3f, 7f
                    )
                }
            }

            updateMvpMatrix(mvpMatrix, modelMatrix, viewMatrix, projectionMatrix)
        }
    }
}