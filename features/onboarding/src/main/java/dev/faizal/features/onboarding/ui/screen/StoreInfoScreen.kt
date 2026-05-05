package dev.faizal.features.onboarding.ui.screen

import dev.faizal.features.onboarding.domain.model.OnboardingStep
import dev.faizal.features.onboarding.ui.AboutQuestionConfig
import dev.faizal.features.onboarding.ui.OnboardingScaffold
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import dev.faizal.core.designsystem.PrimaryBlue

/**
 * Step 4: Identitas toko (form input).
 */
@Composable
fun StoreInfoScreen(
    storeName: String,
    storeAddress: String,
    storePhone: String,
    isAboutExpanded: Boolean,
    onToggleAbout: () -> Unit,
    onStoreNameChange: (String) -> Unit,
    onStoreAddressChange: (String) -> Unit,
    onStorePhoneChange: (String) -> Unit,
    onBack: () -> Unit,
    onNext: () -> Unit,
) {
    val isValid = storeName.isNotBlank() &&
            storeAddress.isNotBlank() &&
            storePhone.isNotBlank()

    OnboardingScaffold(
        step = OnboardingStep.STORE_INFO,
        title = "Beritahu kami\ntentang toko Anda",
        onBack = onBack,
        onNext = onNext,
        nextEnabled = isValid,
        aboutQuestion = AboutQuestionConfig(
            summary = "Data ini akan tampil di struk transaksi...",
            fullExplanation = "Data ini akan tampil di struk transaksi dan " +
                    "laporan PDF Anda. Pastikan informasi yang Anda masukkan " +
                    "akurat agar pelanggan tidak bingung saat membaca struk.",
        ),
        isAboutExpanded = isAboutExpanded,
        onToggleAbout = onToggleAbout,
    ) {
        val scrollState = rememberScrollState()

        androidx.compose.foundation.layout.Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            FormField(
                label = "Nama Toko",
                value = storeName,
                onValueChange = onStoreNameChange,
                placeholder = "Contoh: Warung Sederhana",
            )

            FormField(
                label = "Alamat Toko",
                value = storeAddress,
                onValueChange = onStoreAddressChange,
                placeholder = "Jl. Sudirman No. 123",
                singleLine = false,
            )

            FormField(
                label = "Nomor HP/Kontak",
                value = storePhone,
                onValueChange = onStorePhoneChange,
                placeholder = "08xxxxxxxxxx",
                keyboardType = KeyboardType.Phone,
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun FormField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    singleLine: Boolean = true,
    keyboardType: KeyboardType = KeyboardType.Text,
) {
    androidx.compose.foundation.layout.Column {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text(
                    text = placeholder,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            },
            shape = RoundedCornerShape(12.dp),
            singleLine = singleLine,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PrimaryBlue,
                unfocusedBorderColor = Color.Transparent,
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
            ),
        )
    }
}