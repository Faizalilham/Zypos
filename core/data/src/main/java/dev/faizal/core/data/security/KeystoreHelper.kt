package dev.faizal.core.data.security

import android.content.Context
import android.util.Base64
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import java.security.SecureRandom

object KeystoreHelper {
    private const val KEYSTORE_ALIAS = "zypos_master_key"
    private const val PREFS_NAME = "zypos_secure_prefs"
    private const val KEY_PASSPHRASE = "db_passphrase"

    /**
     * Ambil passphrase DB — buat baru jika belum ada.
     * Passphrase disimpan di EncryptedSharedPreferences,
     * dienkripsi pakai key dari Android Keystore.
     */
    fun getDatabasePassphrase(context: Context): ByteArray {
        val appContext = context.applicationContext
        val masterKey = MasterKey.Builder(context, KEYSTORE_ALIAS)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM) // Key dibuat di Keystore hardware
            .build()

        val encryptedPrefs = EncryptedSharedPreferences.create(
            appContext,
            PREFS_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )

        val existing = encryptedPrefs.getString(KEY_PASSPHRASE, null)

        return if (existing != null) {
            // Passphrase sudah ada, decode dan return
            Base64.decode(existing, Base64.DEFAULT)
        } else {
            // Pertama kali — generate passphrase random 32 byte
            val newPassphrase = ByteArray(32).also { SecureRandom().nextBytes(it) }
            encryptedPrefs.edit()
                .putString(KEY_PASSPHRASE, Base64.encodeToString(newPassphrase, Base64.DEFAULT))
                .apply()
            newPassphrase
        }
    }
}