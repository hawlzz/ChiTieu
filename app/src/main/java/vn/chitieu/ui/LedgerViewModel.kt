package vn.chitieu.ui
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import vn.chitieu.data.*
import javax.inject.Inject
@HiltViewModel class LedgerViewModel @Inject constructor(val repo:Repository,val preferences:Preferences,val registry:vn.chitieu.qr.BankRegistry):ViewModel() {
 val entries=repo.entries.stateIn(viewModelScope,SharingStarted.WhileSubscribed(5000),emptyList())
 val categories=repo.dao.categories().stateIn(viewModelScope,SharingStarted.WhileSubscribed(5000),emptyList())
 val wallets=repo.dao.wallets().stateIn(viewModelScope,SharingStarted.WhileSubscribed(5000),emptyList())
 val settings=preferences.settings.stateIn(viewModelScope,SharingStarted.Eagerly,Settings())
 val rules=repo.dao.observeRules().stateIn(viewModelScope,SharingStarted.WhileSubscribed(5000),emptyList())
 val logs=repo.dao.logs().stateIn(viewModelScope,SharingStarted.WhileSubscribed(5000),emptyList())
 val message=MutableStateFlow("")
 init {work {repo.seed();repo.expire()}}
 fun work(block:suspend ()->Unit) {viewModelScope.launch {try {block()} catch(_:Exception) {message.value="Không thể thực hiện. Kiểm tra dữ liệu hoặc dung lượng thiết bị."}}}
 fun save(e:Entry)=work {repo.save(e)}
 fun delete(id:String)=work {repo.dao.delete(id)}
}


