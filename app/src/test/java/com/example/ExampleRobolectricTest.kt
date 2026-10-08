package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Home Library", appName)
  }

  @Test
  fun `test backup json structure`() {
    val root = JSONObject().apply {
      put("version", 1)
      put("appName", "Home Library")
      put("exportedAt", System.currentTimeMillis())
      put("books", JSONArray().put(JSONObject().apply {
        put("title", "1984")
        put("author", "George Orwell")
      }))
      put("members", JSONArray().put(JSONObject().apply {
        put("name", "Alice")
        put("membershipCode", "HL-101")
      }))
      put("loans", JSONArray())
    }

    assertTrue(root.has("books"))
    assertTrue(root.has("members"))
    assertEquals(1, root.getJSONArray("books").length())
  }
}
