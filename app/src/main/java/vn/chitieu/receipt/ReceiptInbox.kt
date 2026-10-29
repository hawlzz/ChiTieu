package vn.chitieu.receipt
import android.net.Uri
import kotlinx.coroutines.flow.MutableStateFlow
object ReceiptInbox {val incoming=MutableStateFlow<Uri?>(null)}
