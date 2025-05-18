
#include "taglibWrapper.h"

using namespace std;

jstring toString(JNIEnv *env, TagLib::String str) {
    return env->NewStringUTF(str.toCString(true));
}

extern "C"
JNIEXPORT jstring JNICALL
Java_com_lalilu_lmedia_wrapper_Taglib_getLyricWithFD(JNIEnv *env, jobject thiz,
                                                     jint file_descriptor) {
    TagLib::FileStream fileStream(file_descriptor, true);
    TagLib::FileRef fileRef(&fileStream, true, TagLib::AudioProperties::ReadStyle::Fast);
    if (fileRef.isNull()) return env->NewStringUTF("File is not supported");

    auto map = fileRef.tag()->properties();

    auto lyrics = map["LYRICS"];
    if (lyrics.size() > 0 && lyrics[0].size() > 0) {
        return env->NewStringUTF(lyrics[0].toCString(true));
    }
    return nullptr;
}

extern "C"
JNIEXPORT jboolean JNICALL
Java_com_lalilu_lmedia_wrapper_Taglib_writeLyricInto(JNIEnv *env, jobject thiz,
                                                     jint file_descriptor,
                                                     jstring lyric) {
    TagLib::FileStream fileStream(file_descriptor, true);
    TagLib::FileRef fileRef(&fileStream, true, TagLib::AudioProperties::ReadStyle::Fast);
    if (fileRef.isNull()) return JNI_FALSE;

    auto lyricStr = env->GetStringUTFChars(lyric, nullptr);

    auto map = fileRef.file()->properties();
    map.replace("LYRICS", TagLib::String(lyricStr, TagLib::String::Type::UTF8));
    fileRef.file()->setProperties(map);

    return fileRef.file()->save() ? JNI_TRUE : JNI_FALSE;
}

extern "C"
JNIEXPORT jobject JNICALL
Java_com_lalilu_lmedia_wrapper_Taglib_retrieveMetadataWithFD(JNIEnv *env, jobject thiz,
                                                             jint file_descriptor) {
    TagLib::FileStream fileStream(file_descriptor, true);
    TagLib::FileRef fileRef(&fileStream, true, TagLib::AudioProperties::ReadStyle::Fast);
    if (fileRef.isNull()) return nullptr;           // 文件读取失败，返回空

    auto tag = fileRef.tag();
    auto map = tag->properties();
    auto audioProperties = fileRef.audioProperties();

//    for (auto item = map.cbegin(); item != map.cend(); ++item) {
//        LOGE("[%s]\n", item->first.toCString(true));
//    }

    // 获取该文件信息
    struct stat fileStat;
    fstat(file_descriptor, &fileStat);
    auto dateAdded = (jlong) fileStat.st_ctim.tv_sec;
    auto dateModified = (jlong) fileStat.st_mtim.tv_sec;

    // TODO 针对部分格式TagLib无法完全正确解析部分数据，待完善TagLib的部分扩展
    // https://www.jthink.net/jaudiotagger/tagmapping.html
    auto title_str = toString(env, tag->title());
    auto album_str = toString(env, tag->album());
    auto artist_str = toString(env, tag->artist());
    auto comment_str = toString(env, tag->comment());
    auto duration = (jlong) audioProperties->lengthInMilliseconds();
    auto track_num = toString(env, to_string(tag->track()));
    auto disc_num = toString(env, map["DISCNUMBER"].toString());
    auto album_artist_str = toString(env, map["ALBUMARTIST"].toString());
    auto composer_str = toString(env, map["COMPOSER"].toString());
    auto lyricist_str = toString(env, map["LYRICIST"].toString());
    auto genre_str = toString(env, map["GENRE"].toString());
    auto date = toString(env, map["DATE"].toString());

    // 获取需要创建的jclass
    jclass metadata_class = env->FindClass("com/lalilu/lmedia/entity/Metadata");

    // 获取构造器方法ID
    jmethodID constructor = env->GetMethodID(metadata_class, "<init>",
                                             "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;JJJ)V");

    // 创建对象传入并参数
    jobject metadata_obj_j = env->NewObject(
            metadata_class,
            constructor,
            title_str,
            album_str,
            artist_str,
            album_artist_str,
            composer_str,
            lyricist_str,
            comment_str,
            genre_str,
            track_num,
            disc_num,
            date,
            duration,
            dateAdded,
            dateModified
    );

    return metadata_obj_j;
}

extern "C"
JNIEXPORT jbyteArray JNICALL
Java_com_lalilu_lmedia_wrapper_Taglib_getPictureWithFD(JNIEnv *env, jobject thiz,
                                                       jint file_descriptor) {
    TagLib::FileStream fileStream(file_descriptor, true);
    TagLib::FileRef fileRef(&fileStream, true, TagLib::AudioProperties::ReadStyle::Fast);
    if (fileRef.isNull()) return nullptr;           // 文件读取失败，返回空

    auto pictures = fileRef.complexProperties("PICTURE");
    if (pictures.isEmpty()) return nullptr;

    auto picture = pictures.front().value("data").toByteVector();
    auto length = static_cast<jint>(picture.size());

    jbyteArray bytes = env->NewByteArray(length);
    env->SetByteArrayRegion(bytes, 0, length, reinterpret_cast<const jbyte *>(picture.data()));

    return bytes;
}