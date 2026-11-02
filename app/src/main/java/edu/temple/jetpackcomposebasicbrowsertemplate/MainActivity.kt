package edu.temple.jetpackcomposebasicbrowsertemplate

import android.net.Uri
import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import edu.temple.jetpackcomposebasicbrowsertemplate.ui.theme.JetpackComposeBasicBrowserTemplateTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            JetpackComposeBasicBrowserTemplateTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Browser(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun Browser(modifier: Modifier = Modifier) {
    var urlText by rememberSaveable { mutableStateOf("") }
    var invalidUrl by rememberSaveable { mutableStateOf(false) }
    var webView by remember { mutableStateOf<WebView?>(null) }

    Column(
        modifier = modifier.fillMaxSize().padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = urlText,
                onValueChange = {
                    urlText = it
                    invalidUrl = false
                },
                modifier = Modifier.weight(1f),
                label = { Text(stringResource(R.string.url_label)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                isError = invalidUrl,
                supportingText = if (invalidUrl) {
                    { Text(stringResource(R.string.invalid_url)) }
                } else null
            )
            Button(onClick = {
                val url = normalizeUrl(urlText)
                invalidUrl = url == null
                if (url != null) {
                    urlText = url
                    webView?.loadUrl(url)
                }
            }) {
                Text(stringResource(R.string.go))
            }
        }
        AndroidView(
            factory = { context ->
                WebView(context).apply {
                    webViewClient = WebViewClient()
                    webView = this
                }
            },
            modifier = Modifier.fillMaxWidth().weight(1f),
            onRelease = { view ->
                webView = null
                view.destroy()
            }
        )
    }
}

private fun normalizeUrl(input: String): String? {
    val trimmed = input.trim()
    if (trimmed.isEmpty()) return null

    val url = when {
        trimmed.startsWith("//") -> "https:$trimmed"
        trimmed.contains("://") -> trimmed
        else -> "https://$trimmed"
    }
    val uri = Uri.parse(url)
    return url.takeIf {
        (uri.scheme.equals("http", ignoreCase = true)
                || uri.scheme.equals("https", ignoreCase = true))
                && !uri.host.isNullOrBlank()
                && url.none { it.isWhitespace() }
    }
}
