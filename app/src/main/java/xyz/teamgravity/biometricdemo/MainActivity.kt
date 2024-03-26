package xyz.teamgravity.biometricdemo

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import xyz.teamgravity.biometricdemo.ui.theme.BiometricDemoTheme

class MainActivity : AppCompatActivity() {

    private val biometric = BiometricManager()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BiometricDemoTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val result by biometric.result.collectAsStateWithLifecycle(initialValue = null)
                    val launcher = rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.StartActivityForResult(),
                        onResult = { launcherResult ->
                            Toast.makeText(this, launcherResult.toString(), Toast.LENGTH_LONG).show()
                        }
                    )

                    LaunchedEffect(
                        key1 = result,
                        block = {
                            if (result is BiometricManager.Result.AuthenticationNotSet) {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                                    val intent = Intent(Settings.ACTION_BIOMETRIC_ENROLL)
                                    intent.putExtra(Settings.EXTRA_BIOMETRIC_AUTHENTICATORS_ALLOWED, biometric.authenticators())
                                    launcher.launch(intent)
                                }
                            }
                        }
                    )

                    Column(
                        verticalArrangement = Arrangement.spacedBy(
                            space = 10.dp,
                            alignment = Alignment.CenterVertically
                        ),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Button(
                            onClick = {
                                biometric.show(this@MainActivity)
                            }
                        ) {
                            Text(
                                text = stringResource(id = R.string.authenticate)
                            )
                        }
                        result?.let { result ->
                            val text = when (result) {
                                BiometricManager.Result.HardwareNotSupported -> stringResource(id = R.string.hardware_not_supported)
                                BiometricManager.Result.HardwareUnavailable -> stringResource(id = R.string.hardware_unavailable)
                                is BiometricManager.Result.AuthenticationError -> result.message
                                BiometricManager.Result.AuthenticationFailed -> stringResource(id = R.string.authentication_failed)
                                BiometricManager.Result.AuthenticationSuccess -> stringResource(id = R.string.authentication_success)
                                BiometricManager.Result.AuthenticationNotSet -> stringResource(id = R.string.authentication_not_set)
                            }
                            Text(
                                text = text
                            )
                        }
                    }
                }
            }
        }
    }
}