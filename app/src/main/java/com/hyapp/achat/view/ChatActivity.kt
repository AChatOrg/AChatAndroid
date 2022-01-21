package com.hyapp.achat.view

import android.animation.LayoutTransition
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.View.*
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageView
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.aghajari.rlottie.AXrLottieDrawable
import com.hyapp.achat.R
import com.hyapp.achat.databinding.ActivityChatBinding
import com.hyapp.achat.model.*
import com.hyapp.achat.model.entity.*
import com.hyapp.achat.view.adapter.MessageAdapter
import com.hyapp.achat.view.component.SpeedyLinearLayoutManager
import com.hyapp.achat.view.component.emojiview.listener.OnStickerActions
import com.hyapp.achat.view.component.emojiview.listener.SimplePopupAdapter
import com.hyapp.achat.view.component.emojiview.search.AXEmojiSearchView
import com.hyapp.achat.view.component.emojiview.sticker.Sticker
import com.hyapp.achat.view.component.emojiview.view.AXEmojiPager
import com.hyapp.achat.view.component.emojiview.view.AXSingleEmojiView
import com.hyapp.achat.view.component.emojiview.view.AXStickerView
import com.hyapp.achat.view.component.sticker.LottieSticker
import com.hyapp.achat.view.component.sticker.LottieStickerProvider
import com.hyapp.achat.view.component.sticker.Utils
import com.hyapp.achat.view.component.sticker.a18StickerProvider
import com.hyapp.achat.view.utils.UiUtils
import com.hyapp.achat.viewmodel.ChatViewModel
import com.hyapp.achat.viewmodel.utils.TimeUtils
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*

class ChatActivity : EventActivity() {

    companion object {
        const val EXTRA_CONTACT = "contact"

        @JvmStatic
        fun start(context: Context, contact: Contact) {
            val intent = Intent(context, ChatActivity::class.java)
            intent.putExtra(EXTRA_CONTACT, contact)
            context.startActivity(intent)
        }
    }

    private lateinit var binding: ActivityChatBinding
    private lateinit var contact: Contact
    private lateinit var viewModel: ChatViewModel

    private lateinit var messageEditTextSizeAnimator: ValueAnimator

    private lateinit var messageAdapter: MessageAdapter
    private var unreadCount = 0

    private lateinit var layoutTransition: LayoutTransition
    private var isMessageSentAndNotTyping = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        init()
        setupRecyclerView()
        setupContact()
        setupSendButton()
        setupMessageEditText()
        setupEmojis()
        setupFastScrollFab()
        observeMessages()
        observeConnectivity()
        observeOnlineTime()
    }

    override fun onBackPressed() {
        if (!binding.emojiPopupLayout.onBackPressed())
            super.onBackPressed()
    }

    override fun onStart() {
        super.onStart()
        viewModel.activityStarted()
//        val lastVisiblePosition = (binding.recyclerView.layoutManager as LinearLayoutManager)
//            .findLastCompletelyVisibleItemPosition()
//        if (lastVisiblePosition < messageAdapter.itemCount) {
//            viewModel.readMessagesUntilPosition(lastVisiblePosition)
//        }
    }

    override fun onStop() {
        super.onStop()
        viewModel.activityStopped()
    }

    private fun init() {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_chat)
        contact = intent.getParcelableExtra(EXTRA_CONTACT) ?: Contact()
        viewModel = ViewModelProvider(
            this,
            ChatViewModel.Factory(contact.getUser())
        )[ChatViewModel::class.java]
        layoutTransition = (binding.root as ViewGroup).layoutTransition
    }

    private fun setupContact() {
        binding.run {
            name.text = contact.name
            bio.text = contact.bio
            if (contact.type == Contact.TYPE_SINGLE) {
                avatar.setAvatars(contact.avatars)
                if (contact.onlineTime == Contact.TIME_ONLINE) {
                    onlineTime.text = ""
                    onlineTime.setBackgroundResource(R.drawable.last_online_chat_bg_green)
                } else {
                    onlineTime.text =
                        TimeUtils.timeAgoShort(System.currentTimeMillis() - contact.onlineTime)
                    onlineTime.setBackgroundResource(R.drawable.last_online_chat_bg_grey)
                }
            } else {
                avatar.setAvatars(contact.avatars)
                onlineTime.visibility = GONE
            }
        }
    }

    private fun setupRecyclerView() {
        val layoutManager = SpeedyLinearLayoutManager(this, 100)
        binding.recyclerView.layoutManager = layoutManager
        binding.recyclerView.setHasFixedSize(true)
        messageAdapter = MessageAdapter(this, binding.recyclerView)
        messageAdapter.onLoadMore = { viewModel.loadPagedMessages() }
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

                val lastVisiblePosition = layoutManager.findLastVisibleItemPosition()
                if (binding.fastScrollCard.visibility == VISIBLE && lastVisiblePosition >= messageAdapter.itemCount - 2) {
                    binding.fastScrollCard.visibility = INVISIBLE
                    binding.unreadBadgeCard.visibility = INVISIBLE
                } else if (binding.fastScrollCard.visibility != VISIBLE
                    && lastVisiblePosition < messageAdapter.itemCount - 2
                ) {
                    binding.fastScrollCard.visibility = VISIBLE
                }

                readMessages(layoutManager.findFirstVisibleItemPosition(), lastVisiblePosition)
            }
        })
        //scroll on show keyboard
        binding.recyclerView.addOnLayoutChangeListener { _: View?, _: Int, _: Int, _: Int, bottom: Int, _: Int, _: Int, _: Int, oldBottom: Int ->
            if (bottom < oldBottom) {
                binding.recyclerView.smoothScrollBy(0, oldBottom - bottom)
            }
        }
    }

    private fun readMessages(firstVisiblePosition: Int, lastVisiblePosition: Int) {
        for (i in firstVisiblePosition..lastVisiblePosition) {
            try {
                val message = messageAdapter.getMessage(i)
                if (message.transfer == Message.TRANSFER_RECEIVE && message.delivery != Message.DELIVERY_READ
                    && message.delivery != Message.DELIVERY_SENT && message.isChatMessage && ChatViewModel.isActivityStarted
                ) {
                    viewModel.markMessageAsRead(message)
                    if (unreadCount > 0) {
                        unreadCount--
                        binding.unreadBadgeTextView.text = unreadCount.toString()
                    }
                }
            } catch (e: ArrayIndexOutOfBoundsException) {
                e.printStackTrace()
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
        var lastSentTypingTime = 0L
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
                        .withEndAction {
                            layoutTransition.disableTransitionType(LayoutTransition.DISAPPEARING)
                            binding.attach.visibility = INVISIBLE
                            layoutTransition.enableTransitionType(LayoutTransition.DISAPPEARING)
                        }
                    isEditTextEmpty = false
                    isMessageSentAndNotTyping = false
                } else if (p0!!.isEmpty()) {
                    binding.sendImageSwitcher.setOnTouchListener(null)
                    binding.sendImageSwitcher.setImageResource(R.drawable.ic_action_mic)
                    binding.attach.animate().alpha(1F).translationX(0F).setDuration(100)
                        .withStartAction {
                            layoutTransition.disableTransitionType(LayoutTransition.APPEARING)
                            binding.attach.visibility = VISIBLE
                            layoutTransition.enableTransitionType(LayoutTransition.APPEARING)
                        }
                        .withEndAction { binding.attach.setBackgroundResource(R.drawable.chat_inputs_ripple_bg_circle) }
                    isEditTextEmpty = true
                    lastSentTypingTime = 0L
                }
            }

            override fun afterTextChanged(p0: Editable?) {
                if (!isMessageSentAndNotTyping && System.currentTimeMillis() - lastSentTypingTime >= 3000) {
                    viewModel.sendTyping()
                    lastSentTypingTime = System.currentTimeMillis()
                }
            }
        })
    }

    @SuppressLint("ClickableViewAccessibility")
    private val sendButtonTouchListener = OnTouchListener { _, event ->
        val sp1 = UiUtils.sp2px(this, 1F)
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                isMessageSentAndNotTyping = true
                messageEditTextSizeAnimator.addUpdateListener {
                    val size = Message.TEXT_SIZE_SP + it.animatedValue as Int
                    binding.messageEditText.textSize = size.toFloat()
                    binding.messageEditText.setEmojiSize((size + 3) * sp1)
                }
                messageEditTextSizeAnimator.start()
                true
            }
            MotionEvent.ACTION_UP -> {
                messageEditTextSizeAnimator.removeAllUpdateListeners()
                messageEditTextSizeAnimator.cancel()
                binding.messageEditText.textSize = Message.TEXT_SIZE_SP.toFloat()
                binding.messageEditText.setEmojiSize((Message.TEXT_SIZE_SP + 3) * sp1)
                if (UiUtils.isViewInBounds(
                        binding.sendImageSwitcher,
                        event.rawX.toInt(),
                        event.rawY.toInt()
                    )
                ) {
                    sendTextMessage(
                        binding.messageEditText.text.toString(),
                        messageEditTextSizeAnimator.animatedValue as Int
                    )
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
                    sendLottieMessage(
                        Utils.createFromSticker(
                            this@ChatActivity,
                            sticker,
                            lottieViewSize
                        )
                    )
                }
            }

            override fun onLongClick(
                view: View?,
                sticker: Sticker<*>?,
                fromRecent: Boolean
            ): Boolean {
                return false
            }
        })
    }


    private fun setupFastScrollFab() {
        binding.fastScrollCard.setOnClickListener {
            (binding.recyclerView.layoutManager as SpeedyLinearLayoutManager).setSmoothScrollSpeedDefault(
                true
            )
            (binding.recyclerView.layoutManager as SpeedyLinearLayoutManager).setOnScrollStop {
                it.setSmoothScrollSpeedDefault(false)
                it.setOnScrollStop(null)
            }
            binding.recyclerView.smoothScrollToPosition(messageAdapter.itemCount - 1)
        }
        unreadCount = 0
    }

    private fun sendTextMessage(text: CharSequence, textSizeUnit: Int) {
        viewModel.sendPvTextMessage(text, textSizeUnit)
    }

    private fun sendLottieMessage(lottieDrawable: AXrLottieDrawable?) {

    }

    private fun observeConnectivity() {
        ConnLive.singleton().observe(this, { status ->
            when (status) {
                ConnLive.Status.CONNECTING -> binding.bio.setText(R.string.connecting)
                ConnLive.Status.CONNECTED -> binding.bio.text = contact.bio
                ConnLive.Status.DISCONNECTED -> binding.bio.setText(R.string.disconnected)
                ConnLive.Status.NO_NET -> binding.bio.setText(R.string.no_network_connection)
                null -> {}
            }
        })
    }

    private fun observeMessages() {
        viewModel.messagesLive.observe(this, { res ->
            when (res.action) {
                Resource.Action.ADD -> addSingleMessage(res)
                Resource.Action.ADD_PAGING -> addPagingMessages(res)
                Resource.Action.UPDATE -> messageAdapter.submitList(res.data)
            }
        })
    }

    private fun addSingleMessage(res: Resource<MessageList>) {
        messageAdapter.onListChanged = {
            val lastVisiblePosition = (binding.recyclerView.layoutManager as LinearLayoutManager)
                .findLastVisibleItemPosition()
            if (res.bool) {
                if (lastVisiblePosition >= messageAdapter.itemCount - 3) {
                    binding.recyclerView.smoothScrollToPosition(messageAdapter.itemCount - 1)
                } else {
                    unreadCount++
                    binding.unreadBadgeTextView.text = unreadCount.toString()
                    if (binding.fastScrollCard.visibility == VISIBLE) {
                        binding.unreadBadgeCard.visibility = VISIBLE
                    }
                }
            } else {
                if (lastVisiblePosition < messageAdapter.itemCount - 10) {
                    (binding.recyclerView.layoutManager as SpeedyLinearLayoutManager).setSmoothScrollSpeedDefault(
                        true
                    )
                    (binding.recyclerView.layoutManager as SpeedyLinearLayoutManager).setOnScrollStop {
                        it.setSmoothScrollSpeedDefault(false)
                        it.setOnScrollStop(null)
                    }
                    binding.recyclerView.smoothScrollToPosition(messageAdapter.itemCount - 1)
                } else {
                    binding.recyclerView.smoothScrollToPosition(messageAdapter.itemCount - 1)
                }
            }
            messageAdapter.onListChanged = null
        }
        messageAdapter.submitList(res.data)
    }

    private fun addPagingMessages(res: Resource<MessageList>) {
        messageAdapter.isLoadingMore = !res.bool
        if (res.bool2) {
            messageAdapter.onListChanged = {
                binding.recyclerView.scrollToPosition(messageAdapter.itemCount - 1)
                messageAdapter.onListChanged = null
            }
        }
        messageAdapter.submitList(res.data)
    }

    private fun observeOnlineTime() {
        viewModel.onlineTimeLive.observe(this) { time ->
            if (time == Contact.TIME_ONLINE) {
                binding.onlineTime.text = ""
                binding.onlineTime.setBackgroundResource(R.drawable.last_online_chat_bg_green)
            } else {
                binding.onlineTime.text =
                    TimeUtils.timeAgoShort(System.currentTimeMillis() - time - 1000)
                binding.onlineTime.setBackgroundResource(R.drawable.last_online_chat_bg_grey)
            }
        }
    }
}