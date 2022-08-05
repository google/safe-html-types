package com.google.common.html.types;

import static com.google.common.truth.Truth.assertThat;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.annotations.SerializedName;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public final class SafeScriptsForJsonTest {

  @Test
  public void fromJsonElement_escapesHtml() throws Exception {
    JsonObject object = new JsonObject();
    object.add("tag", new JsonPrimitive("<script></script>"));
    SafeScript safeScript = SafeScriptsForJson.fromJsonElement(object);
    // HTML metacharacters are JSON-string escaped, to permit safe embedding in a <script> element.
    assertThat(safeScript.getSafeScriptString())
        .isEqualTo("{\"tag\":\"\\u003cscript\\u003e\\u003c/script\\u003e\"}");
  }

  @Test
  public void fromJsonElement_escapesHtmlInFieldNames() throws Exception {
    SafeScript safeScript =
        SafeScriptsForJson.fromJsonElement(new Gson().toJsonTree(new JsonScriptTagAsFieldName()));
    assertThat(safeScript.getSafeScriptString()).isEqualTo("{\"\\u003cscript\\u003e\":\"value\"}");
  }

  private static final class JsonScriptTagAsFieldName {
    @SerializedName("<script>")
    private final String value = "value";
  }
}
