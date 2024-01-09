package com.khci.bnm.memory

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import com.khci.bnm.R
class favorite_season : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_favorite_season, container, false)

        val input = view.findViewById<EditText>(R.id.inputtext)

        input.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                val favorite_season = input.text.toString()

                val imm =
                    activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
                imm?.hideSoftInputFromWindow(v.windowToken, 0)

                val sharedPreference =
                    activity?.getSharedPreferences("user_plus_info", Context.MODE_PRIVATE)
                val editor = sharedPreference?.edit()
                editor?.putString("favorite_season", favorite_season)
                editor?.apply() // 데이터 저장
                true // 이벤트 처리 완료
            } else false // 다른 키 이벤트에 대한 처리를 계속함
        }

        return view
    }
}