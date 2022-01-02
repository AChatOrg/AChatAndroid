package com.hyapp.achat.ui

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import android.text.Editable
import android.text.TextWatcher
import android.view.MotionEvent
import android.view.View
import android.view.View.*
import android.view.animation.AnimationUtils
import android.widget.ImageView
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.aghajari.rlottie.AXrLottieDrawable
import com.hyapp.achat.R
import com.hyapp.achat.bl.ChatViewModel
import com.hyapp.achat.bl.utils.TimeUtils
import com.hyapp.achat.databinding.ActivityChatBinding
import com.hyapp.achat.model.*
import com.hyapp.achat.model.event.MessageEvent
import com.hyapp.achat.ui.adapter.MessageAdapter
import com.hyapp.achat.ui.emojiview.listener.OnStickerActions
import com.hyapp.achat.ui.emojiview.listener.SimplePopupAdapter
import com.hyapp.achat.ui.emojiview.search.AXEmojiSearchView
import com.hyapp.achat.ui.emojiview.sticker.Sticker
import com.hyapp.achat.ui.emojiview.view.AXEmojiPager
import com.hyapp.achat.ui.emojiview.view.AXSingleEmojiView
import com.hyapp.achat.ui.emojiview.view.AXStickerView
import com.hyapp.achat.ui.model.SpeedyLinearLayoutManager
import com.hyapp.achat.ui.sticker.LottieSticker
import com.hyapp.achat.ui.sticker.LottieStickerProvider
import com.hyapp.achat.ui.sticker.Utils
import com.hyapp.achat.ui.sticker.a18StickerProvider
import com.hyapp.achat.ui.utils.UiUtils
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.*

class ChatActivity : EventActivity() {

    companion object {
        @JvmStatic
        fun start(context: Context, contact: Contact) {
            val intent = Intent(context, ChatActivity::class.java)
            intent.putExtras(contact.bundle)
            context.startActivity(intent)
        }
    }

    private lateinit var binding: ActivityChatBinding
    private lateinit var contact: Contact
    private lateinit var viewModel: ChatViewModel

    private lateinit var messageEditTextSizeAnimator: ValueAnimator

    private lateinit var messageAdapter: MessageAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        init()
        setupRecyclerView()
        setupContact()
        setupSendButton()
        setupMessageEditText()
        setupEmojis()
    }

    override fun onBackPressed() {
        if (!binding.emojiPopupLayout.onBackPressed())
            super.onBackPressed()
    }

    private fun init() {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_chat)
        contact = Contact(intent.extras ?: Bundle())
        viewModel = ViewModelProvider(this).get(ChatViewModel::class.java)
        viewModel.init(contact.uid)
    }

    private fun setupContact() {
        binding.run {
            name.text = contact.name
            bio.text = contact.bio
            if (contact.type == Contact.TYPE_SINGLE) {
                avatar.setAvatars(if (contact.avatars.isNotEmpty()) contact.avatars[0] else null)
                contact.setupOnlineTime(Contact.ONLINE_TYPE_CHAT)
                lastOnline.text = contact.onlineTimeStr
                lastOnline.setBackgroundResource(contact.onlineTimeRes)
            } else {
                avatar.setAvatars(*contact.avatars)
                lastOnline.visibility = GONE
            }
        }
        messageAdapter.add(ProfileMessage(contact).apply {
            contact.setupOnlineTime(Contact.ONLINE_TYPE_PROFILE)
            contact.setupRank(contact.rank)
        })
    }

    private fun setupRecyclerView() {
        val layoutManager: LinearLayoutManager = SpeedyLinearLayoutManager(this, 100)
        binding.recyclerView.layoutManager = layoutManager
        messageAdapter = MessageAdapter(this)
        binding.recyclerView.adapter = messageAdapter
        binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (recyclerView.canScrollVertically(-1)) {
                    binding.toolbarDivider.visibility = VISIBLE
                } else {
                    binding.toolbarDivider.visibility = INVISIBLE
                }
                if (recyclerView.canScrollVertically(1)) {
                    binding.editTextDivider.visibility = VISIBLE
                } else {
                    binding.editTextDivider.visibility = INVISIBLE
                }
            }
        })
        //scroll on show keyboard
        binding.recyclerView.addOnLayoutChangeListener { _: View?, _: Int, _: Int, _: Int, bottom: Int, _: Int, _: Int, _: Int, oldBottom: Int ->
            if (bottom < oldBottom) {
                binding.recyclerView.smoothScrollBy(0, oldBottom - bottom)
            }
        }
    }

    private fun setupSendButton() {
        binding.sendImageSwitcher.run {
            setFactory {
                ImageView(this@ChatActivity).apply {
                    setColorFilter(Color.BLACK)
                    setImageResource(R.drawable.ic_action_mic)
                }
            }
            inAnimation = AnimationUtils.loadAnimation(this@ChatActivity, R.anim.scale_up)
            outAnimation = AnimationUtils.loadAnimation(this@ChatActivity, R.anim.scale_down)
        }
    }

    private fun setupMessageEditText() {
        messageEditTextSizeAnimator = ValueAnimator.ofInt(0, 30).setDuration(2000)
        var isEditTextEmpty = true
        val dp46 = UiUtils.dp2px(this, if (UiUtils.isRtl(this)) -46F else 46F)
        binding.messageEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            @SuppressLint("ClickableViewAccessibility")
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (isEditTextEmpty && p0!!.isNotEmpty()) {
                    binding.sendImageSwitcher.setOnTouchListener(sendButtonTouchListener)
                    binding.sendImageSwitcher.setImageResource(R.drawable.ic_action_send)
                    binding.attach.animate().alpha(0F).translationX(dp46.toFloat()).setDuration(100)
                            .withStartAction { binding.attach.setBackgroundResource(0) }
                            .withEndAction { binding.attach.visibility = INVISIBLE }
                    isEditTextEmpty = false
                } else if (p0!!.isEmpty()) {
                    binding.sendImageSwitcher.setOnTouchListener(null)
                    binding.sendImageSwitcher.setImageResource(R.drawable.ic_action_mic)
                    binding.attach.animate().alpha(1F).translationX(0F).setDuration(100)
                            .withStartAction { binding.attach.visibility = VISIBLE }
                            .withEndAction { binding.attach.setBackgroundResource(R.drawable.chat_inputs_ripple_bg_circle) }
                    isEditTextEmpty = true
                }
            }

            override fun afterTextChanged(p0: Editable?) {
            }
        })
    }

    @SuppressLint("ClickableViewAccessibility")
    private val sendButtonTouchListener = OnTouchListener { view, event ->
        val sp1 = UiUtils.sp2px(this, 1F)
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                messageEditTextSizeAnimator.addUpdateListener {
                    val size = MessageAdapter.TEXT_SIZE_SP + messageEditTextSizeAnimator.animatedValue as Int
                    binding.messageEditText.textSize = size.toFloat()
                    binding.messageEditText.setEmojiSize((size + 3) * sp1)
                }
                messageEditTextSizeAnimator.start()
                true
            }
            MotionEvent.ACTION_UP -> {
                messageEditTextSizeAnimator.removeAllUpdateListeners()
                messageEditTextSizeAnimator.cancel()
                binding.messageEditText.textSize = MessageAdapter.TEXT_SIZE_SP.toFloat()
                binding.messageEditText.setEmojiSize((MessageAdapter.TEXT_SIZE_SP + 3) * sp1)
                if (UiUtils.isViewInBounds(binding.sendImageSwitcher, event.rawX.toInt(), event.rawY.toInt())) {
                    sendTextMessage(binding.messageEditText.text.toString(), messageEditTextSizeAnimator.animatedValue as Int)
                    binding.messageEditText.setText("")
                }
                true
            }
            else -> false
        }
    }

    private fun setupEmojis() {
        val singleEmojiView = AXSingleEmojiView(this)
        val emojiPager = AXEmojiPager(this).apply {
            addPage(singleEmojiView, R.drawable.ic_action_emoji)
            setSwipeWithFingerEnabled(true)
            editText = binding.messageEditText
            setLeftIcon(R.drawable.ic_action_search)
            setOnFooterItemClicked { _, leftIcon -> if (leftIcon) binding.emojiPopupLayout.showSearchView() }

        }
        setupStickers(emojiPager)
        binding.emojiPopupLayout.run {
            initPopupView(emojiPager)
            isPopupAnimationEnabled = true
            popupAnimationDuration = 200
            isSearchViewAnimationEnabled = true
            searchViewAnimationDuration = 200
            searchView = AXEmojiSearchView(this@ChatActivity, emojiPager.getPage(0))
            setPopupListener(object : SimplePopupAdapter() {
                override fun onDismiss() {
                    binding.emoji.setImageResource(R.drawable.ic_action_emoji)
                }

                override fun onShow() {
                    binding.emoji.setImageResource(R.drawable.ic_action_keyboard)
                }

                override fun onKeyboardOpened(height: Int) {
                    binding.emoji.setImageResource(R.drawable.ic_action_emoji)
                }

                override fun onKeyboardClosed() {
                    binding.emoji.setImageResource(if (isShowing) R.drawable.ic_action_keyboard else R.drawable.ic_action_emoji)
                }
            })
        }
        binding.emoji.setOnClickListener {
            if (binding.emojiPopupLayout.isShowing && !binding.emojiPopupLayout.isKeyboardOpen) {
                binding.emojiPopupLayout.openKeyboard()
            } else {
                binding.emojiPopupLayout.show()
            }
        }
    }

    private fun setupStickers(emojiPager: AXEmojiPager) {
        val lottieViewSize = UiUtils.dp2px(this, 150F)
        val a18StickerView = AXStickerView(this, "stickers", a18StickerProvider())
        val lottieStickerView = AXStickerView(this, "animations", LottieStickerProvider())
        emojiPager.addPage(a18StickerView, R.drawable.ic_action_sticker)
        emojiPager.addPage(lottieStickerView, R.drawable.ic_action_sticker)
        lottieStickerView.setOnStickerActionsListener(object : OnStickerActions {
            override fun onClick(view: View?, sticker: Sticker<*>?, fromRecent: Boolean) {
                if (sticker is LottieSticker) {
                    sendLottieMessage(Utils.createFromSticker(this@ChatActivity, sticker, lottieViewSize))
                }
            }

            override fun onLongClick(view: View?, sticker: Sticker<*>?, fromRecent: Boolean): Boolean {
                return false
            }
        })
    }

    private fun sendTextMessage(text: CharSequence, textSizeUnit: Int) {
//        val random = Random()
//        val message: Message
//
//        when (random.nextInt(2)) {
//            0 -> {
//                message = TextMessage(
//                        Message.TRANSFER_TYPE_SEND, System.currentTimeMillis(), "", Contact(), "", text.toString(), textSizeUnit
//                )
//                (message as ChatMessage).delivery = random.nextInt(3).toByte()
//            }
//            1 -> message = TextMessage(
//                    Message.TRANSFER_TYPE_RECEIVE, System.currentTimeMillis(), "", Contact(), "", text.toString(), textSizeUnit
//            )
//            else -> message = DetailsMessage(
//                    System.currentTimeMillis() - (random.nextDouble() * 1000).toLong() * 24 * 3600000
//            )
//        }

        val message = viewModel.sendAndGetPvTextMessage(text, textSizeUnit)
        messageAdapter.addAndScroll(message, binding.recyclerView)
    }

    private fun sendLottieMessage(lottieDrawable: AXrLottieDrawable?) {
        val random = Random()
        val message: ChatMessage

        when (random.nextInt(2)) {
            0 -> {
                message = LottieMessage(
                        Message.TRANSFER_TYPE_SEND, System.currentTimeMillis(), "", Contact(), "", lottieDrawable
                )
                message.delivery = random.nextInt(3).toByte()
            }
            else -> message = LottieMessage(
                    Message.TRANSFER_TYPE_RECEIVE, System.currentTimeMillis(), "", Contact(), "", lottieDrawable
            )
        }

        messageAdapter.add(message)
        binding.recyclerView.smoothScrollToPosition(messageAdapter.itemCount - 1)
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onReceiveMessage(event: MessageEvent) {
        if (event.action == MessageEvent.ACTION_RECEIVE) {
            val message = viewModel.setupAndGetReceiveMessage(event.json)
            messageAdapter.addAndScroll(message, binding.recyclerView)
        }
    }
}