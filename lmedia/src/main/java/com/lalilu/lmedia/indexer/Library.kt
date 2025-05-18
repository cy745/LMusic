package com.lalilu.lmedia.indexer

import androidx.media3.common.MediaItem

interface Library {

    /**
     * 根据媒体项的ID获取对应的媒体项信息
     *
     * @param mediaId 媒体项的唯一标识符
     * @return 返回对应的媒体项实例，如果没有找到则返回null
     */
    fun getItem(mediaId: String): MediaItem? {
        return null
    }

    /**
     * 根据给定的媒体ID映射媒体项目列表
     *
     * @param mediaIds 媒体的唯一标识符
     * @return 返回与给定媒体ID相关的媒体项目列表如果找不到相关的媒体项目，则返回空列表
     */
    fun mapItems(mediaIds: List<String>): List<MediaItem> {
        return emptyList()
    }

    /**
     * 根据父项ID获取所有子媒体项的列表
     *
     * @param parentId 父媒体项的ID，根目录可以为空或特定值表示无父项
     * @return 返回一个包含所有子媒体项的列表
     */
    fun getChildren(parentId: String): List<MediaItem> {
        return emptyList()
    }
}