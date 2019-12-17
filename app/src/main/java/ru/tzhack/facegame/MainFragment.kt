package ru.tzhack.facegame

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.fragment_main.*

interface StartGameListener {
    fun onStartClick()
}

class MainFragment : Fragment() {

    companion object {
        val TAG: String = MainFragment::class.java.simpleName

        fun createFragment() =
            MainFragment().apply {
                arguments = Bundle().apply { }
            }
    }

    private var listener: StartGameListener? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_main, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Glide.with(img_logo)
            .asGif()
            .centerCrop()
            .circleCrop()
            .load(R.drawable.logo_anim)
            .into(img_logo)

        view.setOnClickListener { listener?.onStartClick() }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        when (context) {
            is StartGameListener -> listener = context
        }
    }
}