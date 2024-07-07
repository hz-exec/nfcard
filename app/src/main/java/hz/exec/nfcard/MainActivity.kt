package hz.exec.nfcard

import android.app.PendingIntent
import android.content.Intent
import android.content.IntentFilter
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.MifareClassic
import android.nfc.tech.MifareUltralight
import android.nfc.tech.Ndef
import android.nfc.tech.NdefFormatable
import android.nfc.tech.NfcA
import android.nfc.tech.NfcB
import android.nfc.tech.NfcF
import android.nfc.tech.NfcV
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
    private lateinit var techListsArray: Array<Array<String>>

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

        techListsArray = arrayOf(
            arrayOf(NfcA::class.java.name),
            arrayOf(NfcB::class.java.name),
            arrayOf(NfcF::class.java.name),
            arrayOf(NfcV::class.java.name),
            arrayOf(Ndef::class.java.name),
            arrayOf(NdefFormatable::class.java.name),
            arrayOf(MifareClassic::class.java.name),
            arrayOf(MifareUltralight::class.java.name)
        )

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
            techListsArray
        )
    }

    override fun onPause() {
        super.onPause()
        Log.e("NFC", "onPause")

        nfcAdapter?.disableForegroundDispatch(this)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        Log.e("NFC", "onNewIntent: ${intent.action}")

        handleNfcIntent(intent)
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
        Log.e("NFC", "processNfcTag")

        withContext(Dispatchers.IO) {
            Log.e("NFC", "processNfcTag: ${tag.id}")

            val techList = tag.techList
            for (tech in techList) {
                Log.e("NFC", "tech: $tech")
            }

            readNdef(tag)
            readNfcA(tag)
            readNfcB(tag)
            readNfcF(tag)
            readNfcV(tag)
            readMifareClassic(tag)
            readMifareUltralight(tag)
            readNdefFormatable(tag)
        }
    }

    private fun readNdef(tag: Tag) {
        val ndef = Ndef.get(tag)
        Log.e("NFC", "ndef: $ndef")

        ndef?.let {
            ndef.connect()
            val ndefMessage = ndef.ndefMessage
            ndef.close()

            Log.e("NFC", "ndefMessage: $ndefMessage")

            ndefMessage?.let {
                for (record in it.records) {
                    Log.e("NFC", "record: ${String(record.payload)}")
                }
            }
        }
    }

    @OptIn(ExperimentalStdlibApi::class)
    private fun readNfcA(tag: Tag) {
        val nfcA = NfcA.get(tag)
        Log.e("NFC", "nfcA: $nfcA")

        nfcA?.let {
            nfcA.connect()
            val id = tag.id
            val atqa = nfcA.atqa
            val sak = nfcA.sak
            nfcA.close()

            Log.e("NFC", "id: ${id.toHexString()}")
            Log.e("NFC", "atqa: ${atqa.toHexString()}")
            Log.e("NFC", "sak: ${sak.toHexString()}")
        }
    }

    private fun readNfcB(tag: Tag) {
        val nfcB = NfcB.get(tag)
        Log.e("NFC", "nfcB: $nfcB")

        nfcB?.let {
            nfcB.connect()
            val appData = nfcB.applicationData
            val protInfo = nfcB.protocolInfo
            nfcB.close()

            Log.e("NFC", "appData: $appData")
            Log.e("NFC", "protInfo: $protInfo")
        }
    }

    private fun readNfcF(tag: Tag) {
        val nfcF = NfcF.get(tag)
        Log.e("NFC", "nfcF: $nfcF")

        nfcF?.let {
            nfcF.connect()
            val systemCode = nfcF.systemCode
            val manufacturer = nfcF.manufacturer
            nfcF.close()

            Log.e("NFC", "systemCode: $systemCode")
            Log.e("NFC", "manufacturer: $manufacturer")
        }
    }

    private fun readNfcV(tag: Tag) {
        val nfcV = NfcV.get(tag)
        Log.e("NFC", "nfcV: $nfcV")

        nfcV?.let {
            nfcV.connect()
            val dsfId = nfcV.dsfId
            val responseFlags = nfcV.responseFlags
            nfcV.close()

            Log.e("NFC", "dsfId: $dsfId")
            Log.e("NFC", "responseFlags: $responseFlags")
        }
    }

    @OptIn(ExperimentalStdlibApi::class)
    private fun readMifareClassic(tag: Tag) {
        val mifareClassic = MifareClassic.get(tag)
        Log.e("NFC", "mifareClassic: $mifareClassic")

        mifareClassic?.let {
            mifareClassic.connect()
            val auth = mifareClassic.authenticateSectorWithKeyA(0, MifareClassic.KEY_DEFAULT)
            Log.e("NFC", "auth: $auth")
            if (auth) {
                val data = mifareClassic.readBlock(0)
                Log.e("NFC", "data: ${data.toHexString()}")
            } else {
                Log.e("NFC", "auth: failed")
            }
            mifareClassic.close()
        }
    }

    private fun readMifareUltralight(tag: Tag) {
        val mifareUltralight = MifareUltralight.get(tag)
        Log.e("NFC", "mifareUltralight: $mifareUltralight")

        mifareUltralight?.let {
            mifareUltralight.connect()
            val type = mifareUltralight.type
            mifareUltralight.close()

            Log.e("NFC", "type: $type")
        }
    }

    private fun readNdefFormatable(tag: Tag) {
        val ndefFormatable = NdefFormatable.get(tag)
        Log.e("NFC", "ndefFormatable: $ndefFormatable")

        ndefFormatable?.let {
            ndefFormatable.connect()
            ndefFormatable.close()

            Log.e("NFC", "format: success")
        }
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
