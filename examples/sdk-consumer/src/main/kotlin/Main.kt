import androidx.compose.ui.Modifier
import com.example.duofold.model.*
import com.example.duofold.render.*

fun main() {
    val panels = FoldPanels.fromOpening(120f)
    check(panels == FoldPanels(30f, -30f))
    val state = FoldRenderState(0f, FoldPreset.CENTER_VERTICAL.resolve(0f), panels = panels)
    val modifier = Modifier.foldSurfaceEffect(state)
    check(modifier !== Modifier)
    println("Staged SDK consumer passed: independent Maven dependency, core model and Compose modifier")
}
