package sanicrollers.downloader

const val urlRoot = "http://dl.sega-pc.com/srn/app"
const val version = "2.0.3_049"
const val android = "android"

interface Platform {
    val name: String get

    val urlAbList: String
        get() = "$urlAbRoot/ablist.txt"
    val urlAbRoot: String
        get() = "$urlRoot/assets/$version/assetbundle/$name"
}

object Android : Platform {
    override val name: String = "android"
}

object IOS : Platform {
    override val name: String = "iphone"
}