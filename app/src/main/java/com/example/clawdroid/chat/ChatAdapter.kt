package com.example.clawdroid.chat

import android.graphics.Color
import android.graphics.Typeface
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.BackgroundColorSpan
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.clawdroid.R
import com.example.clawdroid.model.ChatMessage
import com.example.clawdroid.model.MessageRole
import com.google.android.material.card.MaterialCardView

class ChatAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val messages = mutableListOf<ChatMessage>()
    private val expandedThinking = mutableSetOf<String>()
    private val expandedToolCall = mutableSetOf<String>()

    companion object {
        const val TYPE_USER = 0
        const val TYPE_AGENT = 1
        const val TYPE_SYSTEM = 2
        const val TYPE_THINKING = 3
        const val TYPE_TOOL_CALL = 4
    }

    fun setMessages(newMessages: List<ChatMessage>) {
        val diffCallback = MessageDiffCallback(messages, newMessages)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        messages.clear()
        messages.addAll(newMessages)
        diffResult.dispatchUpdatesTo(this)
    }

    fun addMessage(msg: ChatMessage) {
        messages.add(msg)
        notifyItemInserted(messages.size - 1)
    }

    fun clearMessages() {
        val count = messages.size
        messages.clear()
        expandedThinking.clear()
        expandedToolCall.clear()
        notifyItemRangeRemoved(0, count)
    }

    override fun getItemViewType(position: Int): Int = when (messages[position].role) {
        MessageRole.USER -> TYPE_USER
        MessageRole.AGENT -> TYPE_AGENT
        MessageRole.SYSTEM -> TYPE_SYSTEM
        MessageRole.THINKING -> TYPE_THINKING
        MessageRole.TOOL_CALL -> TYPE_TOOL_CALL
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            TYPE_USER, TYPE_AGENT -> {
                val view = inflater.inflate(R.layout.item_chat_message, parent, false)
                MessageViewHolder(view)
            }
            TYPE_SYSTEM -> {
                val tv = TextView(parent.context).apply {
                    setPadding(24, 12, 24, 12)
                    setTextColor(Color.GRAY)
                    textSize = 13f
                    gravity = android.view.Gravity.CENTER
                }
                SystemViewHolder(tv)
            }
            TYPE_THINKING -> {
                val view = inflater.inflate(R.layout.item_chat_thinking, parent, false)
                ThinkingViewHolder(view)
            }
            TYPE_TOOL_CALL -> {
                val view = inflater.inflate(R.layout.item_chat_toolcall, parent, false)
                ToolCallViewHolder(view)
            }
            else -> throw IllegalArgumentException("Unknown view type: $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val msg = messages[position]
        when (holder) {
            is MessageViewHolder -> holder.bind(msg)
            is SystemViewHolder -> holder.bind(msg.content)
            is ThinkingViewHolder -> holder.bind(msg)
            is ToolCallViewHolder -> holder.bind(msg)
        }
    }

    override fun getItemCount() = messages.size

    inner class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val bubbleAgent: MaterialCardView? = itemView.findViewById(R.id.bubble_agent)
        private val textAgent: TextView? = itemView.findViewById(R.id.text_agent)
        private val bubbleUser: MaterialCardView? = itemView.findViewById(R.id.bubble_user)
        private val textUser: TextView? = itemView.findViewById(R.id.text_user)
        private val spacerStart: View? = itemView.findViewById(R.id.spacer_start)
        private val spacerEnd: View? = itemView.findViewById(R.id.spacer_end)

        fun bind(msg: ChatMessage) {
            when (msg.role) {
                MessageRole.USER -> {
                    bubbleAgent?.isVisible = false
                    bubbleUser?.isVisible = true
                    spacerStart?.isVisible = true
                    spacerEnd?.isVisible = false
                    textUser?.text = msg.content
                }
                MessageRole.AGENT -> {
                    bubbleUser?.isVisible = false
                    bubbleAgent?.isVisible = true
                    spacerStart?.isVisible = false
                    spacerEnd?.isVisible = true
                    textAgent?.text = renderMarkdown(msg.content)
                }
                else -> {}
            }
        }
    }

    inner class SystemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textView = itemView as TextView
        fun bind(text: String) {
            textView.text = text
        }
    }

    inner class ThinkingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val header: View = itemView.findViewById(R.id.thinking_header)
        private val body: TextView = itemView.findViewById(R.id.thinking_body)
        private val chevron: ImageView = itemView.findViewById(R.id.thinking_chevron)

        fun bind(msg: ChatMessage) {
            body.text = msg.content
            val isExpanded = expandedThinking.contains(msg.id)
            body.isVisible = isExpanded
            chevron.setImageResource(
                if (isExpanded) R.drawable.ic_expand_less else R.drawable.ic_expand_more
            )
            header.setOnClickListener {
                if (expandedThinking.contains(msg.id)) {
                    expandedThinking.remove(msg.id)
                } else {
                    expandedThinking.add(msg.id)
                }
                notifyItemChanged(adapterPosition)
            }
        }
    }

    inner class ToolCallViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val header: View = itemView.findViewById(R.id.toolcall_header)
        private val toolName: TextView = itemView.findViewById(R.id.toolcall_name)
        private val chevron: ImageView = itemView.findViewById(R.id.toolcall_chevron)
        private val body: View = itemView.findViewById(R.id.toolcall_body)
        private val argsText: TextView = itemView.findViewById(R.id.toolcall_args)
        private val resultText: TextView = itemView.findViewById(R.id.toolcall_result)

        fun bind(msg: ChatMessage) {
            toolName.text = msg.toolName ?: "Unknown"
            argsText.text = msg.toolArguments ?: "{}"
            resultText.text = msg.toolResult ?: ""
            val isExpanded = expandedToolCall.contains(msg.id)
            body.isVisible = isExpanded
            chevron.setImageResource(
                if (isExpanded) R.drawable.ic_expand_less else R.drawable.ic_expand_more
            )
            header.setOnClickListener {
                if (expandedToolCall.contains(msg.id)) {
                    expandedToolCall.remove(msg.id)
                } else {
                    expandedToolCall.add(msg.id)
                }
                notifyItemChanged(adapterPosition)
            }
        }
    }

    private fun renderMarkdown(text: String): CharSequence {
        val sb = SpannableStringBuilder()
        var i = 0
        while (i < text.length) {
            when {
                text.startsWith("**", i) -> {
                    val end = text.indexOf("**", i + 2)
                    if (end != -1) {
                        val start = sb.length
                        sb.append(text.substring(i + 2, end))
                        sb.setSpan(
                            StyleSpan(Typeface.BOLD),
                            start,
                            sb.length,
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                        i = end + 2
                    } else {
                        sb.append(text[i])
                        i++
                    }
                }
                text.startsWith("*", i) && !text.startsWith("**", i) -> {
                    val end = text.indexOf("*", i + 1)
                    if (end != -1) {
                        val start = sb.length
                        sb.append(text.substring(i + 1, end))
                        sb.setSpan(
                            StyleSpan(Typeface.ITALIC),
                            start,
                            sb.length,
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                        i = end + 1
                    } else {
                        sb.append(text[i])
                        i++
                    }
                }
                text.startsWith("`", i) -> {
                    val end = text.indexOf("`", i + 1)
                    if (end != -1) {
                        val start = sb.length
                        sb.append(text.substring(i + 1, end))
                        sb.setSpan(
                            ForegroundColorSpan(Color.parseColor("#4ADE80")),
                            start,
                            sb.length,
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                        sb.setSpan(
                            BackgroundColorSpan(Color.parseColor("#1A2332")),
                            start,
                            sb.length,
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                        i = end + 1
                    } else {
                        sb.append(text[i])
                        i++
                    }
                }
                text.startsWith("```", i) -> {
                    val end = text.indexOf("```", i + 3)
                    if (end != -1) {
                        val start = sb.length
                        sb.append("\n")
                        sb.append(text.substring(i + 3, end).trim())
                        sb.append("\n")
                        sb.setSpan(
                            ForegroundColorSpan(Color.parseColor("#4ADE80")),
                            start,
                            sb.length,
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                        sb.setSpan(
                            BackgroundColorSpan(Color.parseColor("#1A2332")),
                            start,
                            sb.length,
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                        i = end + 3
                    } else {
                        sb.append(text[i])
                        i++
                    }
                }
                text.startsWith("\n- ", i) || (i == 0 && text.startsWith("- ", i)) -> {
                    sb.append("\n  • ")
                    i += if (text.startsWith("\n- ", i)) 3 else 2
                }
                text.startsWith("\n", i) -> {
                    sb.append("\n")
                    i++
                }
                else -> {
                    sb.append(text[i])
                    i++
                }
            }
        }
        return sb
    }

    private class MessageDiffCallback(
        private val oldList: List<ChatMessage>,
        private val newList: List<ChatMessage>
    ) : DiffUtil.Callback() {
        override fun getOldListSize() = oldList.size
        override fun getNewListSize() = newList.size
        override fun areItemsTheSame(oldPos: Int, newPos: Int): Boolean {
            return oldList[oldPos].id == newList[newPos].id
        }
        override fun areContentsTheSame(oldPos: Int, newPos: Int): Boolean {
            return oldList[oldPos] == newList[newPos]
        }
    }
}
