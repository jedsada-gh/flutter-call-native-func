package com.example.my_flutter_application

import com.google.gson.annotations.SerializedName

data class Event(
  @SerializedName("name") val name: String, @SerializedName("data") val data: Any
)