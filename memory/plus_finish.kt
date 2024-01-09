package com.khci.bnm.memory

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.khci.bnm.Retrofit2.APIS
import com.khci.bnm.Retrofit2.PM_modify_user_info
import com.khci.bnm.Retrofit2.PM_modify_user_info_Result
import retrofit2.Response
import com.khci.bnm.R
class plus_finish : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_plus_finish, container, false)
        //SaveSharedPreferences()
        val api = APIS.create()
        val textView = view?.findViewById<TextView>(R.id.join1text)


        val sharedPreferences = activity?.getSharedPreferences("user_plus_info", Context.MODE_PRIVATE)
        val sharedPreferences_user = activity?.getSharedPreferences("login", Context.MODE_PRIVATE)

        val editor = sharedPreferences?.edit()

        val user_id = sharedPreferences_user?.getString("user_id", "").toString()
        val hometown = sharedPreferences?.getString("user_hometown", null).toString()
        val favorite_food= listOf(sharedPreferences?.getString("favorite_food", null).toString())
        val favorite_music = listOf(sharedPreferences?.getString("favorite_music", null).toString())
        val favorite_season = listOf(sharedPreferences?.getString("favorite_season", null).toString())
        val pet = listOf(sharedPreferences?.getString("pet", null).toString())
        val past_job = listOf(sharedPreferences?.getString("past_job", null).toString())
        val details = sharedPreferences?.getString("details", null).toString()

        val data = PM_modify_user_info(user_id, hometown, favorite_food, favorite_music, favorite_season, pet, past_job, details)

        api.modify_user_info(data).enqueue(object : retrofit2.Callback<PM_modify_user_info_Result> {
            override fun onResponse(call: retrofit2.Call<PM_modify_user_info_Result>, response: Response<PM_modify_user_info_Result>) {
                //Log.d("log",response.toString())
                Log.d("log", response.body().toString())
                val result = response.body()?.result.toString()
                if(!response.body().toString().isEmpty())

                    if(result == "success"){
                        //textView?.text = response.body()?.msg.toString()
                        textView?.text = "추가정보 입력이\n\n 완료되었습니다.\n\n 다음 또는 이전을 눌러 주세요"

                        //nok_pw 부터 nok_tell 지우기
                        editor?.remove("hometown")
                        editor?.remove("favorite_food")
                        editor?.remove("favorite_music")
                        editor?.remove("favorite_season")
                        editor?.remove("pet")
                        editor?.remove("past_job")
                        editor?.remove("details")

                        //적용
                        editor?.apply()
                    }
                if(result == "error"){
                    textView?.text = response.body()?.msg.toString()
                }
            }

            override fun onFailure(call: retrofit2.Call<PM_modify_user_info_Result>, t: Throwable) {
                // 실패
                Log.d("log",t.message.toString())
                Log.d("log","fail")
            }
        })

        return view
    }
}