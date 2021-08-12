package com.falcofemoralis.hdrezkaapp.objects

import android.util.ArrayMap
import java.io.Serializable

open class Voice : Serializable {
    constructor(name: String, id: String) {
        this.name = name
        this.id = id
    }

    constructor(streams: String) {
        this.streams = streams
    }

    constructor(id: String, seasons: HashMap<String, ArrayList<String>>) {
        this.id = id
        this.seasons = seasons
    }

    var name: String? = null
    var id: String? = null
    var streams: String? = null
    var seasons: HashMap<String, ArrayList<String>>? = null
}
