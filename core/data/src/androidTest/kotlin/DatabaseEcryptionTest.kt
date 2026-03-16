package dev.faizal.core.testing

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import dev.faizal.core.data.database.AppDatabase
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertNotEquals
import org.junit.Before
import org.junit.Test
import java.security.KeyStore
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
class DatabaseEncryptionTest {

    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
    }

    @Test
    fun database_file_should_be_encrypted() {
        val db = AppDatabase.getDatabase(context)
        db.close()

        val dbFile = context.getDatabasePath("zypos_database")
        assert(dbFile.exists()) { "File database tidak ditemukan" }

        val header = dbFile.inputStream().use { stream ->
            ByteArray(16).also { stream.read(it) }
        }

        val plaintextSQLiteHeader = "SQLite format 3\u0000"

        assertNotEquals(
            "❌ Database TIDAK terenkripsi — header SQLite terbaca!",
            plaintextSQLiteHeader,
            String(header)
        )
    }

    @Test
    fun keystore_key_should_exist_after_db_init() {
        AppDatabase.getDatabase(context)

        val keyStore = KeyStore
            .getInstance("AndroidKeyStore")
            .apply { load(null) }

        assert(keyStore.containsAlias("zypos_master_key")) {
            "❌ Key tidak ditemukan di Android Keystore"
        }
    }

    @Test
    fun database_should_reopen_with_same_passphrase() {
        val db1 = AppDatabase.getDatabase(context)
        db1.close()
        AppDatabase.resetInstance()

        val db2 = AppDatabase.getDatabase(context)
        assert(db2.isOpen) {
            "❌ Database gagal dibuka ulang — passphrase tidak konsisten"
        }
    }
}