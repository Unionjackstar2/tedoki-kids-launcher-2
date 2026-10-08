package com.example.sms

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class SmsRepositoryTest {

    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        SmsRepository.init(context)
    }

    @Test
    fun testReceiveMessageAndMarkAsRead() {
        val testNumber = "555-9999"
        val testBody = "Test SMS Nachricht"

        // 1. Receive incoming message
        SmsRepository.receiveMessage(context, testNumber, testBody, "Fremder")

        val messages = SmsRepository.messagesFlow.value
        val received = messages.find { it.phoneNumber == testNumber && it.body == testBody }

        assertTrue("Received message should exist in repository", received != null)
        assertFalse("Received message should initially be unread", received!!.isRead)

        // 2. Mark messages for this number as read
        SmsRepository.markMessagesAsReadFor(context, testNumber)

        val updatedMessages = SmsRepository.messagesFlow.value
        val updatedReceived = updatedMessages.find { it.phoneNumber == testNumber && it.body == testBody }

        assertTrue("Received message should now be read", updatedReceived!!.isRead)
    }

    @Test
    fun testDeleteUnknownContactMessages() {
        val unknownNumber = "555-8888"
        SmsRepository.receiveMessage(context, unknownNumber, "Unbekannte SMS", "Unbekannt")

        val beforeCount = SmsRepository.messagesFlow.value.count { it.phoneNumber == unknownNumber }
        assertEquals(1, beforeCount)

        // Delete unknown contact and its messages
        SmsRepository.deleteMessagesForContact(context, unknownNumber)

        val afterCount = SmsRepository.messagesFlow.value.count { it.phoneNumber == unknownNumber }
        assertEquals(0, afterCount)
    }
}
