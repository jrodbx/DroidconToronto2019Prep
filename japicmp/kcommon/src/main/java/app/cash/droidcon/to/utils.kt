package app.cash.droidcon.to

import kotlinx.metadata.jvm.KotlinClassHeader
import kotlinx.metadata.jvm.KotlinClassMetadata

fun Class<*>.readMetadataHeader(): KotlinClassHeader {
  return getAnnotation(Metadata::class.java).run {
    KotlinClassHeader(
        kind, metadataVersion, bytecodeVersion, data1, data2, extraString, packageName, extraInt
    )
  }
}

fun Class<*>.readMetadata(): KotlinClassMetadata.Class {
  return KotlinClassMetadata.read(readMetadataHeader()) as KotlinClassMetadata.Class
}