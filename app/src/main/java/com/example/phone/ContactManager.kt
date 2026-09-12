package com.example.phone

import android.content.Context
import android.content.pm.PackageManager
import android.database.Cursor
import android.provider.ContactsContract
import androidx.core.content.ContextCompat

data class ContactEntry(
    val name: String,
    val phoneNumber: String
)

object ContactManager {

    fun findContactByName(context: Context, query: String): List<ContactEntry> {
        if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.READ_CONTACTS)
            != PackageManager.PERMISSION_GRANTED) {
            return emptyList()
        }

        val results = mutableListOf<ContactEntry>()
        val cleanQuery = query.trim().lowercase()

        val uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI
        val projection = arrayOf(
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.NUMBER
        )
        val selection = "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} LIKE ?"
        val selectionArgs = arrayOf("%$cleanQuery%")

        var cursor: Cursor? = null
        try {
            cursor = context.contentResolver.query(
                uri,
                projection,
                selection,
                selectionArgs,
                "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} ASC"
            )

            cursor?.let {
                val nameIdx = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                val numIdx = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                while (it.moveToNext()) {
                    val name = if (nameIdx >= 0) it.getString(nameIdx) ?: "" else ""
                    val num = if (numIdx >= 0) it.getString(numIdx) ?: "" else ""
                    if (num.isNotBlank()) {
                        results.add(ContactEntry(name = name, phoneNumber = num))
                    }
                }
            }
        } catch (e: Exception) {
            // Handle error safely
        } finally {
            cursor?.close()
        }

        return results.distinctBy { it.phoneNumber }
    }
}
