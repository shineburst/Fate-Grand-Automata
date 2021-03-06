package com.mathewsachin.fategrandautomata.ui.support_img_namer

import android.net.Uri
import android.view.View
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import com.mathewsachin.fategrandautomata.R
import java.io.File

class SupportImgEntry(
    val ImgPath: File,
    val TargetDir: File,
    val Frame: View,
    val regex: Regex,
    val invalidMsg: String) {

    val checkBox = Frame.findViewById<CheckBox>(R.id.support_img_check)!!
    val imgView = Frame.findViewById<ImageView>(R.id.support_img)!!
    val textBox = Frame.findViewById<EditText>(R.id.support_img_txt)!!
    val errorTxt = Frame.findViewById<TextView>(R.id.support_img_error)!!

    init {
        if (!ImgPath.exists()) {
            hide()
        }
        else {
            imgView.setImageURI(Uri.parse(ImgPath.absolutePath))

            // Allow clicking the image to toggle the checkbox too for convenience
            imgView.setOnClickListener {
                checkBox.toggle()
            }

            textBox.visibility = View.GONE

            checkBox.setOnCheckedChangeListener { _, isChecked ->
                // Hide text field when not checked to prevent confusion for users
                textBox.visibility = if (isChecked) View.VISIBLE else View.GONE

                // Focus only, don't open soft keyboard
                if (isChecked) {
                    textBox.requestFocus()
                }
            }
        }
    }

    fun hide() {
        Frame.visibility = View.GONE
    }

    private fun showAlert(Msg: String) {
        errorTxt.text = Msg
        errorTxt.visibility = View.VISIBLE
    }

    fun isValid(): Boolean {
        if (!checkBox.isChecked) {
            return true
        }

        val oldPath = ImgPath
        val newFileName = textBox.text.toString()

        if (!oldPath.exists()) {
            // Either the file was deleted or not generated in the first place.
            return true
        }

        if (newFileName.isBlank()) {
            showAlert("One of the names is still empty. Either delete the unnamed Servant/CE or specify a name.")
            return false
        }

        if (!regex.matches(newFileName)) {
            showAlert(invalidMsg)
            return false
        }

        val newPath = File(TargetDir, "${newFileName}.png")

        if (newPath.exists()) {
            showAlert("'${newFileName}' already exists. Specify another name.")
            return false
        }

        return true
    }

    fun rename(): Boolean {
        errorTxt.visibility = View.GONE

        if (!checkBox.isChecked) {
            return true
        }

        val oldPath = ImgPath
        val newFileName = textBox.text.toString()

        if (!oldPath.exists()) {
            // Either the file was deleted or not generated in the first place.
            return true
        }

        val newPath = File(TargetDir, "${newFileName}.png")

        try {
            val newPathDir = newPath.parentFile

            if (!newPathDir.exists()) {
                newPathDir.mkdirs()
            }

            // move
            oldPath.copyTo(newPath)
            oldPath.delete()
        }
        catch (e: Exception) {
            showAlert("Failed to rename to: '${newFileName}'")
            return false
        }

        hide()

        return true
    }
}