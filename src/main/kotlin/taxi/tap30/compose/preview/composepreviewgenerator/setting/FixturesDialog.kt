package taxi.tap30.compose.preview.composepreviewgenerator.setting

import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.util.maximumWidth
import com.intellij.ui.util.minimumWidth
import com.intellij.ui.util.preferredHeight
import com.intellij.ui.util.preferredWidth
import java.awt.BorderLayout
import java.awt.Image
import javax.swing.*

class FixturesDialog : DialogWrapper(true) {
    init {
        title = "Welcome to Compose Preview Generator Plugin"
        init()
    }
    override fun createActions(): Array<Action> {
        val okAction = super.getOKAction()
        okAction.putValue(Action.NAME, "Finish")
        return arrayOf(okAction)
    }
    override fun createCenterPanel(): JComponent {
        val panel = JPanel()
        panel.layout = BoxLayout(panel, BoxLayout.Y_AXIS)
        panel.preferredWidth = 700
        panel.preferredHeight = 500
        val propertiesCode = JTextArea(
            """
            val intFixtures = listOf(10, 100, 1000000)
            val longFixtures = listOf(1000L, 1000000L, 1000000000000L)
            val doubleFixtures = listOf(10.0, 100.0, 1000000.0)
            val floatFixtures = listOf(10.0f, 100.0f, 1000000.0f)
            val stringFixtures = listOf(
                "\"متن ساختگی\"",
                "\"لورم ایپسوم متن ساختگی با تولید سادگی نامفهوم از صنعت چاپ\"",
                "\"لورم ایپسوم متن ساختگی با تولید سادگی نامفهوم از صنعت چاپ، و با استفاده از طراحان گرافیک است، چاپگرها و متون بلکه روزنامه و مجله در ستون و سطرآنچنان که لازم است، و برای شرایط فعلی تکنولوژی مورد نیاز، و کاربردهای متنوع با هدف بهبود ابزارهای کاربردی می باشد، کتابهای زیادی در شصت و سه درصد گذشته حال و آینده، شناخت فراوان جامعه و متخصصان را می طلبد، تا با نرم افزارها شناخت بیشتری را برای طراحان رایانه ای علی الخصوص طراحان خلاقی، و فرهنگ پیشرو در زبان فارسی ایجاد کرد، در این صورت می توان امید داشت که تمام و دشواری موجود در ارائه راهکارها، و شرایط سخت تایپ به پایان رسد و زمان مورد نیاز شامل حروفچینی دستاوردهای اصلی، و جوابگوی سوالات پیوسته اهل دنیای موجود طراحی اساسا مورد استفاده قرار گیرد.\""
            )
        """.trimIndent()
        ).apply {
            isEditable = false  // Make text areas read-only
            lineWrap = true     // Enable line wrapping
            wrapStyleWord = true
            minimumWidth = 1000
            border = BorderFactory.createTitledBorder("Properties codes")
        }
        panel.add(propertiesCode)
        // Add additional UI components as needed
        return JScrollPane(panel)
    }

}