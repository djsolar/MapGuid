package com.twinflag.mapguid.utils

import android.content.Context
import android.content.SharedPreferences
import com.twinflag.mapguid.MyApplication
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.Charset
import kotlin.reflect.KProperty

class Preference<T>(val name: String, private val default: T) {

    companion object {
        private const val file_name = "start_node_file"

        private val prefs: SharedPreferences by lazy {
            MyApplication.context.getSharedPreferences(file_name, Context.MODE_PRIVATE)
        }

        fun clearPreference() {
            prefs.edit().clear().apply()
        }

        fun clearPreference(key: String) {
            prefs.edit().remove(key).apply()
        }
    }

    operator fun getValue(thisRef: Any?, property: KProperty<*>): T {
        return getSharedPreference(name, default)
    }

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        putSharedPreference(name, default, value)
    }

    private fun putSharedPreference(name: String, default: T, value: T) = with(prefs.edit()) {
        when(value) {
            is Long -> putLong(name, value)
            is String -> putString(name, value)
            is Int -> putInt(name, value)
            is Boolean -> putBoolean(name, value)
            is Float -> putFloat(name, value)
            else -> putString(name,serialize(value))
        }.apply()
    }

    private fun getSharedPreference(name: String, default: T): T = with(prefs) {
        val res = when(default) {
            is Long -> getLong(name, default)
            is String -> getString(name, default)
            is Int -> getInt(name, default)
            is Boolean -> getBoolean(name, default)
            is Float -> getFloat(name, default)
            else ->  deSerialization(getString(name,serialize(default)))
        }
        return res as T
    }

    fun <A> serialize(obj: A): String {
        val byteArrayOutputStream = ByteArrayOutputStream()
        val objectOutputStream = ObjectOutputStream(byteArrayOutputStream)
        objectOutputStream.writeObject(obj)
        val serStr = byteArrayOutputStream.toString("ISO-8859-1")
        val encodeStr = URLEncoder.encode(serStr, "UTF-8")
        objectOutputStream.close()
        byteArrayOutputStream.close()
        return encodeStr
    }
    fun <A> deSerialization(str: String): A {
        val redStr = URLDecoder.decode(str, "UTF-8")
        val byteArrayInputStream = ByteArrayInputStream(redStr.toByteArray(Charset.forName("ISO-8859-1")))
        val objectInputStream = ObjectInputStream(byteArrayInputStream)
        val obj = objectInputStream.readObject() as A
        objectInputStream.close()
        byteArrayInputStream.close()
        return obj
    }

    fun contains(name: String): Boolean {
        return prefs.contains(name)
    }

    fun getAll(): Map<String, *> {
        return prefs.all
    }
}