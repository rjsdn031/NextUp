package lab.p4c.nextup.ui.overlay

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner

class OverlayLifecycleOwner : LifecycleOwner, SavedStateRegistryOwner, ViewModelStoreOwner {

    private val _lifecycle = LifecycleRegistry(this)
    private val _savedState = SavedStateRegistryController.create(this)
    private val _vmStore = ViewModelStore()

    // ⬇⬇⬇ 인터페이스가 요구하는 'val' 프로퍼티로 override
    override val lifecycle: Lifecycle
        get() = _lifecycle

    override val savedStateRegistry: SavedStateRegistry
        get() = _savedState.savedStateRegistry

    override val viewModelStore: ViewModelStore
        get() = _vmStore

    init {
        // SavedStateRegistry는 attach 후 restore를 호출하는 패턴이 안전함
        _savedState.performAttach()
        _savedState.performRestore(null)
        _lifecycle.currentState = Lifecycle.State.INITIALIZED
    }

    fun onAttach() {
        _lifecycle.currentState = Lifecycle.State.CREATED
        _lifecycle.currentState = Lifecycle.State.STARTED
        _lifecycle.currentState = Lifecycle.State.RESUMED
    }

    fun onDetach() {
        _lifecycle.currentState = Lifecycle.State.DESTROYED
        _vmStore.clear()
    }
}
