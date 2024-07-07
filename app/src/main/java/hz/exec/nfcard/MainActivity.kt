package hz.exec.nfcard

import android.app.PendingIntent
import android.content.Intent
import android.content.IntentFilter
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.NfcA
import android.nfc.tech.NfcF
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.lifecycleScope
import hz.exec.nfcard.ui.theme.NFCardTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    private var nfcAdapter: NfcAdapter? = null
    private lateinit var nfcPendingIntent: PendingIntent
    private lateinit var intentFiltersArray: Array<IntentFilter>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.e("NFC", "onCreate: ${intent.action}")

        nfcAdapter = NfcAdapter.getDefaultAdapter(this)
        nfcPendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, javaClass).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
            PendingIntent.FLAG_MUTABLE
        )
        intentFiltersArray = arrayOf(
            IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED),
            IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED),
            IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED))

        setContent {
            NFCardTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Greeting("NFC Card")
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        Log.e("NFC", "onResume: ${intent.action}")

        nfcAdapter?.enableForegroundDispatch(
            this,
            nfcPendingIntent,
            intentFiltersArray,
            null
        )


    }

    override fun onPause() {
        super.onPause()
        Log.e("NFC", "onPause")

        nfcAdapter?.disableForegroundDispatch(this)
    }

    private fun handleNfcIntent(intent: Intent?) {
        Log.e("NFC", "handleNfcIntent: ${intent?.action}")

        intent?.let {
            if (NfcAdapter.ACTION_NDEF_DISCOVERED == it.action ||
                NfcAdapter.ACTION_TAG_DISCOVERED == it.action ||
                NfcAdapter.ACTION_TECH_DISCOVERED == it.action) {

                val tag: Tag? = it.getParcelableExtra(NfcAdapter.EXTRA_TAG)
                Log.e("NFC", "tag: $tag")

                tag?.let {
                    lifecycleScope.launch {
                        processNfcTag(it)
                    }
                }
            }
        }
    }

    private suspend fun processNfcTag(tag: Tag) {
        Log.e("NFC", "processNfcTag: ${tag.id}")

        withContext(Dispatchers.IO) {
            val techList = tag.techList
            for (tech in techList) {
                Log.e("NFC", "tech: $tech")
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        Log.e("NFC", "onNewIntent: ${intent.action}")

        handleNfcIntent(intent)
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    NFCardTheme {
        Greeting("Android")
    }
}
