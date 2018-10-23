package com.twinflag.mapguid

import org.junit.Test

import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {

    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun testMethod() {
        val dataSet = setOf<String>("F1", "F3", "F4", "F2")
        val sortData = dataSet.sorted()
        println(sortData)
        val a = Math.atan(0.5645)
        println(a / Math.PI * 180)
    }
}
