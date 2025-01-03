package taxi.tap30.compose.preview.composepreviewgenerator.setting

import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.util.maximumWidth
import com.intellij.ui.util.preferredHeight
import com.intellij.ui.util.preferredWidth
import java.awt.BorderLayout
import java.awt.Image
import javax.swing.*

class StartupDialog : DialogWrapper(true) {
    init {
        title = "Welcome to Compose Preview Generator Plugin"
        init()
    }
    override fun createActions(): Array<Action> {
        // Obtain the default OK action
        val okAction = super.getOKAction()
        val cancelAction = super.getCancelAction()
        okAction.putValue(Action.NAME, "Next")
        cancelAction.putValue(Action.NAME, "Close")
        return arrayOf(okAction, cancelAction) // Ensure to include cancel action if needed
    }
    override fun createCenterPanel(): JComponent {
        val panel = JPanel()
        panel.layout = BoxLayout(panel, BoxLayout.Y_AXIS)
        val imageUrl = javaClass.classLoader.getResource("META-INF/banner.png")
        if (imageUrl != null) {
            val originalImageIcon = ImageIcon(imageUrl)
            val originalImage = originalImageIcon.image

            // Specify your desired width and height for the image
            val targetWidth = 1024
            val targetHeight = 400

            // Resize the image
            val resizedImage = originalImage.getScaledInstance(targetWidth, targetHeight, Image.SCALE_DEFAULT)
            val resizedImageIcon = ImageIcon(resizedImage)

            // Add the resized image to the JLabel
            val label = JLabel(resizedImageIcon)
            panel.add(label)
        }
        val title = JLabel("Complete the setup to get started.\n")
        val currentFont = title.font
        val newFont = currentFont.deriveFont(currentFont.style, 18f)
        title.font = newFont
        panel.add(title)
        panel.add(JLabel("To use interactive preview generation, put these codes in a place that all of your compose components can access to it.\n"))
        return JScrollPane(panel)
    }

    override fun doOKAction() {
        super.doOKAction()
        PropertiesDialog().show()
    }
}