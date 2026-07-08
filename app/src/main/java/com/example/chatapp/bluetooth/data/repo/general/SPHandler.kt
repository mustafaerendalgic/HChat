package com.example.chatapp.bluetooth.data.repo.general

interface SPHandler {
    fun saveTheAnswer(key: String, answerToSave: Int)
    fun fetchTheAnswer(key: String): Int
    fun howManyRefused()
    fun fetchUUIDRecord(): String?
    fun saveUUIDRecord(uuid: String)
}