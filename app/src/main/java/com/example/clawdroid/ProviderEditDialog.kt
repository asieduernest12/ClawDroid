package com.example.clawdroid

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import com.example.clawdroid.model.ModelProvider
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class ProviderEditDialog(
    private val existingProvider: ModelProvider? = null,
    private val onSave: (ModelProvider) -> Unit
) : BottomSheetDialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return BottomSheetDialog(requireContext(), theme)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.dialog_provider, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val title = view.findViewById<android.widget.TextView>(R.id.dialog_title)
        val inputName = view.findViewById<TextInputEditText>(R.id.input_name)
        val inputModel = view.findViewById<TextInputEditText>(R.id.input_model)
        val inputKey = view.findViewById<TextInputEditText>(R.id.input_key)
        val inputUrl = view.findViewById<TextInputEditText>(R.id.input_url)
        val inputPreset = view.findViewById<AutoCompleteTextView>(R.id.input_preset)
        val layoutModel = view.findViewById<TextInputLayout>(R.id.input_layout_model)
        val btnSave = view.findViewById<com.google.android.material.button.MaterialButton>(R.id.btn_save)
        val btnCancel = view.findViewById<com.google.android.material.button.MaterialButton>(R.id.btn_cancel)

        val isEdit = existingProvider != null
        title.text = if (isEdit) getString(R.string.providers_dialog_edit_title)
                     else getString(R.string.providers_dialog_add_title)

        existingProvider?.let {
            inputName.setText(it.modelName)
            inputModel.setText(it.model)
            if (it.apiKey.isNotBlank()) inputKey.setText(it.apiKey)
            if (it.apiBase.isNotBlank()) inputUrl.setText(it.apiBase)
        }

        val presetAdapter = ArrayAdapter(requireContext(),
            android.R.layout.simple_dropdown_item_1line, PREDEFINED_NAMES)
        inputPreset.setAdapter(presetAdapter)

        inputPreset.setOnItemClickListener { _, _, position, _ ->
            val preset = PREDEFINED[position]
            inputName.setText(preset.first)
            inputModel.setText(preset.second)
            if (preset.third.isNotBlank()) inputUrl.setText(preset.third)
        }

        btnCancel.setOnClickListener { dismiss() }

        btnSave.setOnClickListener {
            val name = inputName.text.toString().trim()
            val model = inputModel.text.toString().trim()

            layoutModel.error = null
            if (model.isBlank()) {
                layoutModel.error = getString(R.string.providers_error_model_required)
                return@setOnClickListener
            }

            val provider = ModelProvider(
                modelName = if (name.isNotBlank()) name else model.substringAfter("/"),
                model = model,
                apiKey = inputKey.text.toString().trim(),
                apiBase = inputUrl.text.toString().trim()
            )
            onSave(provider)
            dismiss()
        }
    }

    companion object {
        val PREDEFINED_NAMES = arrayOf(
            "OpenAI",
            "Anthropic",
            "DeepSeek",
            "Google Gemini",
            "Azure OpenAI",
            "Venice",
            "LongCat",
            "Modelscope Qwen",
            "LM Studio (local)",
            "Custom"
        )

        val PREDEFINED = listOf(
            Triple("gpt-5.4", "openai/gpt-5.4", "https://api.openai.com/v1"),
            Triple("claude-sonnet-4.6", "anthropic/claude-sonnet-4.6", "https://api.anthropic.com/v1"),
            Triple("deepseek", "deepseek/deepseek-chat", ""),
            Triple("gemini", "antigravity/gemini-2.0-flash", ""),
            Triple("azure-gpt5", "azure/my-deployment", "https://your-resource.openai.azure.com"),
            Triple("venice-uncensored", "venice/venice-uncensored", ""),
            Triple("longcat", "longcat/LongCat-Flash-Thinking", ""),
            Triple("modelscope-qwen", "modelscope/Qwen/Qwen3-235B-A22B-Instruct-2507",
                "https://api-inference.modelscope.cn/v1"),
            Triple("lmstudio-local", "lmstudio/openai/gpt-oss-20b", ""),
            Triple("", "", "")
        )
    }
}
