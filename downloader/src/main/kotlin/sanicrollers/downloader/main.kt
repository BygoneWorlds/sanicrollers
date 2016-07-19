package sanicrollers.downloader

import com.github.kittinunf.fuel.httpGet
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser
import java.io.File
import java.io.FileOutputStream
import java.io.FileWriter
import java.io.StringReader
import java.net.URL

data class AssetBundleEntry(val fileName: String, val version: Int, val cacheBuster: Long)
data class StreamingFileEntry(val fileName: String, val hash: String)

fun downloadAssetBundles(platform: Platform) {
    val ablist = URL(platform.urlAbList).readText()
    val records = CSVParser(StringReader(ablist), CSVFormat.DEFAULT).records
    val entries = records.map { AssetBundleEntry(it[0], it[1].toInt(), it[2].toLong()) }

    val rootDir = "dl.sega-pc.com/srn/app/assets/$version/assetbundle/${platform.name}"
    val rootDirFile = File(rootDir)
    rootDirFile.mkdirs()

    FileWriter(File("dl.sega-pc.com/srn/app/assets/$version/assetbundle/${platform.name}/ablist.txt")).use {
        it.write(ablist)
    }

    entries.filter {
        val path = "dl.sega-pc.com/srn/app/assets/$version/assetbundle/${platform.name}/${it.fileName}"
        val file = File(path)

        !file.exists()
    }.forEach {
        "${platform.urlAbRoot}/${it.fileName}".httpGet().response { request, response, result ->
            val path = "dl.sega-pc.com/srn/app/assets/$version/assetbundle/${platform.name}/${it.fileName}"
            val file = File(path)

            if (!file.createNewFile()) {
                println("Skipping ${platform.name} ${it.fileName} because it already exists.")
                return@response
            }

            FileOutputStream(file).use { stream ->
                stream.write(response.data)
            }
            println("Downloaded ${platform.name}'s ${it.fileName}")
        }.taskFuture?.get()
        // need to slow the hell down...
    }
}

fun downloadSound(platform: Platform) {
    val actualPlatform = when (platform) {
        Android -> "Android"
        IOS -> "iPhone"
        else -> throw RuntimeException("Unknown platform???")
    }

    val rootDir = File("dl.sega-pc.com/srn/app/assets/$version/sound/$actualPlatform/")
    rootDir.mkdirs()

    val soundList = URL("http://dl.sega-pc.com/srn/app/assets/$version/sound/$actualPlatform/StreamingDataList.txt").readText()
    FileWriter(File("dl.sega-pc.com/srn/app/assets/$version/sound/$actualPlatform/StreamingDataList.txt")).use {
        it.write(soundList)
    }

    val records = CSVParser(StringReader(soundList), CSVFormat.DEFAULT).records
    val entries = records.map { StreamingFileEntry(it[0], it[1]) }

    entries.filter {
        val path = "dl.sega-pc.com/srn/app/assets/$version/sound/$actualPlatform/${it.fileName}"
        val file = File(path)

        if (!file.exists()) {
            true
        } else {
            println("Skipping ${platform.name} sound file ${it.fileName} because it already exists.")
            false
        }
    }.forEach {
        "http://dl.sega-pc.com/srn/app/assets/$version/sound/$actualPlatform/${it.fileName}".httpGet().response { request, response, result ->
            val path = "dl.sega-pc.com/srn/app/assets/$version/sound/$actualPlatform/${it.fileName}"
            val file = File(path)

            if (!file.createNewFile()) {
                println("Skipping ${platform.name} sound file ${it.fileName} because it already exists.")
                return@response
            }

            FileOutputStream(file).use { stream ->
                stream.write(response.data)
            }
            println("Downloaded ${platform.name}'s sound file ${it.fileName}")
        }.taskFuture?.get()
        // need to slow the hell down...
    }
}

fun getFile(rootPath: String, skipIfExists: Boolean = true) {
    val file = File("dl.sega-pc.com$rootPath")
    if (skipIfExists && file.exists()) {
        println("Skipping $rootPath")
        return
    } else {
        file.parentFile.mkdirs()
        file.createNewFile()
    }

    "http://dl.sega-pc.com$rootPath".httpGet().response { request, response, result ->
        FileOutputStream(file).use { stream ->
            stream.write(response.data)
        }
        println("Downloaded $rootPath")
    }.taskFuture?.get()
}

fun main(args: Array<String>) {
    with(Android) {
        println("Downloading android/Android assets")
        downloadAssetBundles(this)
        downloadSound(this)
    }
    with(IOS) {
        println("Downloading iphone/iPhone assets.")
        downloadAssetBundles(this)
        downloadSound(this)
    }

    listOf(
            "/srn/app/information/017/InformationDataTable.bytes"
    ).forEach {
        getFile(it)
    }
}