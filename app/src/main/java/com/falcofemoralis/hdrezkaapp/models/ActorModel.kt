package com.falcofemoralis.hdrezkaapp.models

import android.util.ArrayMap
import com.falcofemoralis.hdrezkaapp.objects.Actor
import com.falcofemoralis.hdrezkaapp.objects.Film
import com.falcofemoralis.hdrezkaapp.objects.SettingsData
import org.json.JSONObject
import org.jsoup.HttpStatusException
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

object ActorModel {
    private const val POST_ACTOR = "/ajax/person_info/"
    const val NO_PHOTO = "/i/nopersonphoto.png"

    fun getActorMainInfo(actor: Actor): Actor {
        val data: ArrayMap<String, String> = ArrayMap()
        data["id"] = actor.id.toString()
        data["pid"] = actor.pid.toString()

        val result: Document? = Jsoup.connect(SettingsData.provider + POST_ACTOR)
            .data(data)
            .header("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
            .ignoreContentType(true)
            .post()

        if (result != null) {
            val bodyString: String = result.select("body").text()
            val jsonObject = JSONObject(bodyString)

            val isSuccess: Boolean = jsonObject.getBoolean("success")

            if (isSuccess) {
                val person: JSONObject = jsonObject.getJSONObject("person")
                actor.careers = getJsonString(person, "careers")
                actor.link = getJsonString(person, "link")
                actor.name = getJsonString(person, "name")
                actor.nameOrig = getJsonString(person, "name_alt")
                actor.photo = getJsonString(person, "photo")
                actor.diedOnAge = getJsonString(person, "agefull")
                actor.age = getJsonString(person, "age")
                actor.birthday = getJsonString(person, "birthday")
                actor.birthplace = getJsonString(person, "birthplace")
                actor.deathday = getJsonString(person, "deathday")
                actor.deathplace = getJsonString(person, "deathplace")
            } else {
                throw HttpStatusException("failed to get actor data with msg ${jsonObject.getString("message")}", 400, SettingsData.provider)
            }
        } else {
            throw HttpStatusException("failed to get actor data", 400, SettingsData.provider)
        }

        actor.hasMainData = true
        return actor
    }

    private fun getJsonString(person: JSONObject, key: String): String? {
        return try {
            val obj = person.getString(key)
            if (obj.isEmpty() || obj == "null") {
                null
            } else {
                obj
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun getActorFilms(actor: Actor) {
        val document: Document = Jsoup.connect(actor.link).get()

        val careerEls = document.select("div.b-person__career")
        val careers: ArrayList<Pair<String, ArrayList<Film>>> = ArrayList()
        for (el in careerEls) {
            val header = el.select("h2").text()
            // val info = el.select("span.b-person__career_stats").text()
            val films: ArrayList<Film> = FilmsListModel.getFilmsFromPage(Jsoup.parse(el.toString()))

            /*     val type: CareerType = when (name) {
                     "??????????" -> CareerType.ACTOR
                     "????????????????" -> CareerType.DIRECTOR
                     "??????????????????" -> CareerType.SCRIPTWRITER
                     "????????????????" -> CareerType.PRODUCER
                     "????????????????" -> CareerType.OPERATOR
                     "????????????????" -> CareerType.EDITOR
                     else -> CareerType.ACTOR
                 }*/
            careers.add(Pair(header, films))
        }

        actor.personCareerFilms = careers
    }
}