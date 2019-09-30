package com.bestmatch.foryou

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bestmatch.foryou.fragments.GlavnayaInfaFragment
import com.bestmatch.foryou.fragments.RegaFragment
import com.bestmatch.foryou.fragments.VhodFragment

class AnketaMainActivity : AppCompatActivity() {


    private var fragmentMain = androidx.fragment.app.Fragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_anketa)

        if (intent.getStringExtra("action") == "registration") {
            fragmentMain = RegaFragment()
        }

        if (intent.getStringExtra("action") == "facebook_login"){
            fragmentMain = GlavnayaInfaFragment()
        }

        if (intent.getStringExtra("action") == "sign_in") {
            fragmentMain = VhodFragment()
        }
        setFragment(fragmentMain)
    }

    private fun setFragment(f: androidx.fragment.app.Fragment) {

        val fm: androidx.fragment.app.FragmentManager = supportFragmentManager
        val ft: androidx.fragment.app.FragmentTransaction = fm.beginTransaction()

        ft.replace(R.id.question_container, f)
        ft.commit()

    }
}
