package com.example.data.contact

import android.content.Context
import android.content.pm.PackageManager
import android.database.Cursor
import android.provider.ContactsContract
import androidx.core.content.ContextCompat

data class DeviceContactAccount(
    val accountName: String,
    val accountType: String
) {
    val displayTitle: String
        get() = when {
            accountType.contains("nextcloud", ignoreCase = true) || accountType.contains("davx5", ignoreCase = true) || accountType.contains("owncloud", ignoreCase = true) -> "Nextcloud ($accountName)"
            accountType.contains("google", ignoreCase = true) -> "Google ($accountName)"
            accountType.contains("whatsapp", ignoreCase = true) -> "WhatsApp ($accountName)"
            accountType.contains("telegram", ignoreCase = true) -> "Telegram ($accountName)"
            accountType.contains("sim", ignoreCase = true) -> "SIM-Karte ($accountName)"
            accountName.isBlank() -> "Gerätespeicher / Lokal"
            else -> "$accountName ($accountType)"
        }
}

data class AndroidDeviceContact(
    val id: String,
    val name: String,
    val phoneNumber: String,
    val photoUri: String? = null,
    val accountName: String = "",
    val accountType: String = "",
    val linkedAccounts: List<String> = emptyList()
)

object AndroidContactsRepository {

    fun hasContactsPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.READ_CONTACTS
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Query all available contact accounts on this device (e.g., Nextcloud, Google, Device, SIM)
     */
    fun getAvailableContactAccounts(context: Context): List<DeviceContactAccount> {
        if (!hasContactsPermission(context)) return emptyList()

        val accounts = linkedSetOf<DeviceContactAccount>()
        val uri = ContactsContract.RawContacts.CONTENT_URI
        val projection = arrayOf(
            ContactsContract.RawContacts.ACCOUNT_NAME,
            ContactsContract.RawContacts.ACCOUNT_TYPE
        )

        try {
            val cursor: Cursor? = context.contentResolver.query(
                uri,
                projection,
                null,
                null,
                "${ContactsContract.RawContacts.ACCOUNT_NAME} ASC"
            )
            cursor?.use {
                val nameIdx = it.getColumnIndex(ContactsContract.RawContacts.ACCOUNT_NAME)
                val typeIdx = it.getColumnIndex(ContactsContract.RawContacts.ACCOUNT_TYPE)
                while (it.moveToNext()) {
                    val name = if (nameIdx != -1) it.getString(nameIdx) ?: "Gerätespeicher" else "Gerätespeicher"
                    val type = if (typeIdx != -1) it.getString(typeIdx) ?: "local" else "local"
                    accounts.add(DeviceContactAccount(accountName = name, accountType = type))
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Always provide at least local device storage entry if none found
        if (accounts.isEmpty()) {
            accounts.add(DeviceContactAccount(accountName = "Gerätespeicher", accountType = "local"))
        }

        return accounts.toList()
    }

    /**
     * Query all contacts matching allowed accounts (or all contacts if allowedAccounts is empty).
     * Supports linked/aggregated contacts (e.g. Nextcloud + Google merged into one contact):
     * If ANY of the contact's linked accounts is allowed, the contact will be loaded.
     */
    fun loadContacts(
        context: Context,
        allowedAccountNames: List<String> = emptyList()
    ): List<AndroidDeviceContact> {
        if (!hasContactsPermission(context)) return emptyList()

        val contactsList = mutableListOf<AndroidDeviceContact>()
        val allowedSet = allowedAccountNames.toSet()

        // Step 1: Pre-populate map of Contact ID -> Set of all linked accounts (name & type)
        // A single merged contact (CONTACT_ID) can have multiple RawContacts across accounts (e.g. Google AND Nextcloud)
        val contactIdToAccounts = mutableMapOf<String, MutableSet<Pair<String, String>>>()
        try {
            val rawUri = ContactsContract.RawContacts.CONTENT_URI
            val rawProjection = arrayOf(
                ContactsContract.RawContacts.CONTACT_ID,
                ContactsContract.RawContacts.ACCOUNT_NAME,
                ContactsContract.RawContacts.ACCOUNT_TYPE
            )
            val rawCursor = context.contentResolver.query(rawUri, rawProjection, null, null, null)
            rawCursor?.use { c ->
                val cIdIdx = c.getColumnIndex(ContactsContract.RawContacts.CONTACT_ID)
                val accNameIdx = c.getColumnIndex(ContactsContract.RawContacts.ACCOUNT_NAME)
                val accTypeIdx = c.getColumnIndex(ContactsContract.RawContacts.ACCOUNT_TYPE)

                while (c.moveToNext()) {
                    val contactId = if (cIdIdx != -1) c.getString(cIdIdx) ?: "" else ""
                    val accName = if (accNameIdx != -1) c.getString(accNameIdx) ?: "Gerätespeicher" else "Gerätespeicher"
                    val accType = if (accTypeIdx != -1) c.getString(accTypeIdx) ?: "local" else "local"
                    if (contactId.isNotBlank()) {
                        contactIdToAccounts.getOrPut(contactId) { mutableSetOf() }.add(Pair(accName, accType))
                    }
                }
            }
        } catch (e: Throwable) {
            e.printStackTrace()
        }

        // Step 2: Query Phone entries with valid CommonDataKinds.Phone columns
        val uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI
        val projection = arrayOf(
            ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME_PRIMARY,
            ContactsContract.CommonDataKinds.Phone.NUMBER,
            ContactsContract.CommonDataKinds.Phone.PHOTO_URI
        )

        try {
            val cursor: Cursor? = context.contentResolver.query(
                uri,
                projection,
                null,
                null,
                "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME_PRIMARY} ASC"
            )

            val seenNumbers = mutableSetOf<String>()

            cursor?.use {
                val idIdx = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID)
                val nameIdx = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME_PRIMARY)
                val phoneIdx = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                val photoIdx = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_URI)

                while (it.moveToNext()) {
                    val contactId = if (idIdx != -1) it.getString(idIdx) ?: "" else ""
                    val name = if (nameIdx != -1) it.getString(nameIdx) ?: "" else ""
                    val rawPhone = if (phoneIdx != -1) it.getString(phoneIdx) ?: "" else ""
                    val photoUri = if (photoIdx != -1) it.getString(photoIdx) else null

                    val linkedAccountPairs = contactIdToAccounts[contactId] ?: emptySet()
                    val linkedNames = linkedAccountPairs.map { it.first }

                    // Best primary account name/type representation (prioritizing an allowed account if any)
                    val primaryAccount = linkedAccountPairs.firstOrNull { it.first in allowedSet }
                        ?: linkedAccountPairs.firstOrNull()
                    val accName = primaryAccount?.first ?: "Gerätespeicher"
                    val accType = primaryAccount?.second ?: "local"

                    val normalizedPhone = rawPhone.replace("\\s".toRegex(), "")
                    if (normalizedPhone.isBlank() || name.isBlank()) continue

                    // Account filter:
                    // If allowedSet is not empty: include if ANY of the linked accounts is in allowedSet!
                    // (Handles linked contacts e.g. Nextcloud + Google, where Nextcloud is allowed)
                    if (allowedSet.isNotEmpty()) {
                        val isAnyAccountAllowed = if (linkedAccountPairs.isEmpty()) {
                            // Fallback if no raw accounts found for this contactId
                            allowedSet.contains(accName)
                        } else {
                            linkedAccountPairs.any { allowedSet.contains(it.first) }
                        }
                        if (!isAnyAccountAllowed) {
                            continue
                        }
                    }

                    // Avoid duplicate phone entries for the same person
                    val uniqueKey = "$name:$normalizedPhone"
                    if (seenNumbers.add(uniqueKey)) {
                        contactsList.add(
                            AndroidDeviceContact(
                                id = contactId.ifBlank { uniqueKey },
                                name = name,
                                phoneNumber = rawPhone,
                                photoUri = photoUri,
                                accountName = accName,
                                accountType = accType,
                                linkedAccounts = linkedNames
                            )
                        )
                    }
                }
            }
        } catch (e: Throwable) {
            e.printStackTrace()
        }

        return contactsList
    }
}
